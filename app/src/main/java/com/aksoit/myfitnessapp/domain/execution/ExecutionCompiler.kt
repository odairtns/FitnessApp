package com.aksoit.myfitnessapp.domain.execution

import com.aksoit.myfitnessapp.domain.model.InvalidTemplateException
import com.aksoit.myfitnessapp.domain.model.Side
import com.aksoit.myfitnessapp.domain.model.SideMode
import com.aksoit.myfitnessapp.domain.model.WorkoutBlock
import com.aksoit.myfitnessapp.domain.model.WorkoutExercise
import com.aksoit.myfitnessapp.domain.model.GroupType
import com.aksoit.myfitnessapp.domain.model.WorkoutProtocol
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate

interface ExecutionCompiler {
    fun compile(template: WorkoutTemplate): ExecutionPlan
}

/**
 * Compilador determinístico Template -> ExecutionPlan (Spec 04 §4).
 *
 * Kotlin puro: nunca muta o template recebido (Regra Inegociável 1).
 */
class DefaultExecutionCompiler(
    private val prepareSeconds: Int = 10,
    private val defaultRestSeconds: Int = 60,
    private val defaultHoldSeconds: Int = 30,
    private val defaultSwitchSideSeconds: Int = 5,
    private val defaultAmrapSeconds: Int = 600,
    private val defaultEmomIntervalSeconds: Int = 60,
    private val defaultHiitWorkSeconds: Int = 30
) : ExecutionCompiler {

    override fun compile(template: WorkoutTemplate): ExecutionPlan {
        val blocks = template.blocks.sortedBy { it.position }
        if (blocks.isEmpty()) throw InvalidTemplateException("O treino \"${template.name}\" não possui blocos.")

        val planBlocks = blocks.mapIndexed { index, block ->
            PlanBlock(
                index = index,
                sourceBlockId = block.id.takeIf { it > 0 },
                name = block.name,
                blockType = block.blockType,
                exercises = snapshotExercises(block, template.protocol)
            )
        }

        val steps = mutableListOf<ExecutionStep>()
        blocks.forEachIndexed { index, block ->
            val isLastBlock = index == blocks.lastIndex
            when (template.protocol) {
                WorkoutProtocol.STANDARD_STRENGTH,
                WorkoutProtocol.HYPERTROPHY -> compileStrength(index, block, steps)
                WorkoutProtocol.STRETCH -> compileStretch(index, block, steps)
                WorkoutProtocol.HIIT_INTERVALS -> compileHiit(index, block, steps)
                WorkoutProtocol.AMRAP -> compileAmrap(index, block, isLastBlock, steps)
                WorkoutProtocol.EMOM -> compileEmom(index, block, isLastBlock, steps)
                WorkoutProtocol.FOR_TIME -> compileForTime(index, block, isLastBlock, steps)
            }
        }

        if (steps.isEmpty()) {
            throw InvalidTemplateException("O treino \"${template.name}\" não possui passos executáveis.")
        }
        // Remove eventual descanso fantasma no fim do plano.
        while (steps.isNotEmpty() && steps.last().stepType in GHOST_REST_TYPES) steps.removeAt(steps.lastIndex)

        steps += ExecutionStep(
            id = "complete",
            blockIndex = blocks.lastIndex,
            blockName = "Conclusão",
            stepType = ExecutionStepType.COMPLETE
        )

        val linked = steps.mapIndexed { i, step -> step.copy(nextStepId = steps.getOrNull(i + 1)?.id) }

        return ExecutionPlan(
            templateId = template.id.takeIf { it > 0 },
            templateName = template.name,
            modality = template.modality,
            protocol = template.protocol,
            blocks = planBlocks,
            steps = linked
        )
    }

    // ---------------------------------------------------------------------------------------------
    // Snapshot
    // ---------------------------------------------------------------------------------------------

    private fun orderedExercises(block: WorkoutBlock): List<WorkoutExercise> =
        block.exercises.sortedBy { it.position }

    private fun snapshotExercises(block: WorkoutBlock, protocol: WorkoutProtocol): List<PlanExercise> {
        val list = orderedExercises(block).mapIndexed { i, ex ->
            PlanExercise(position = i, exerciseId = ex.exerciseId, name = ex.displayName)
        }
        return if (list.isEmpty() && protocol == WorkoutProtocol.HIIT_INTERVALS) {
            listOf(PlanExercise(position = 0, exerciseId = null, name = block.name))
        } else list
    }

    private fun circuitItems(block: WorkoutBlock): List<CircuitItem> =
        orderedExercises(block).mapIndexed { i, ex ->
            val first = ex.plannedSets.minByOrNull { it.setNumber }
            CircuitItem(
                exerciseId = ex.exerciseId,
                exerciseName = ex.displayName,
                position = i,
                targetReps = first?.targetReps,
                targetLoadKg = first?.targetLoadKg
            )
        }

    // ---------------------------------------------------------------------------------------------
    // 4.1 / 4.2 Musculação tradicional e Supersets
    // ---------------------------------------------------------------------------------------------

    private fun compileStrength(b: Int, block: WorkoutBlock, out: MutableList<ExecutionStep>) {
        val indexed = orderedExercises(block).withIndex().filter { it.value.plannedSets.isNotEmpty() }
        val units = groupUnits(indexed)
        if (units.isEmpty()) return

        for (round in 0 until block.rounds) {
            units.forEachIndexed { u, unit ->
                val maxSets = unit.maxOf { it.value.plannedSets.size }
                for (s in 0 until maxSets) {
                    var lastRest: Int? = null
                    unit.forEach { (exIdx, ex) ->
                        val sets = ex.plannedSets.sortedBy { it.setNumber }
                        val set = sets.getOrNull(s) ?: return@forEach
                        out += ExecutionStep(
                            id = "b${b}_r${round}_e${exIdx}_s${set.setNumber}_work",
                            blockIndex = b,
                            blockName = block.name,
                            stepType = ExecutionStepType.SET_WORK,
                            exerciseId = ex.exerciseId,
                            exerciseName = ex.displayName,
                            exercisePosition = exIdx,
                            setNumber = set.setNumber,
                            totalSets = sets.size,
                            targetLoadKg = set.targetLoadKg,
                            targetReps = set.targetReps,
                            durationSeconds = set.targetDurationSeconds,
                            side = sideFor(set.sideMode)
                        )
                        lastRest = set.restSeconds ?: block.restDurationSeconds ?: defaultRestSeconds
                    }
                    // Regra de Ouro: sem descanso fantasma após a última série do último exercício do bloco.
                    val isLastOfBlock = round == block.rounds - 1 && u == units.lastIndex && s == maxSets - 1
                    val rest = lastRest ?: 0
                    if (!isLastOfBlock && rest > 0) {
                        out += ExecutionStep(
                            id = "b${b}_r${round}_u${u}_s${s + 1}_rest",
                            blockIndex = b,
                            blockName = block.name,
                            stepType = ExecutionStepType.REST_SET,
                            durationSeconds = rest
                        )
                    }
                }
            }
        }
    }

    /** Agrupa exercícios consecutivos com mesmo groupId SUPERSET numa unidade intercalada. */
    private fun groupUnits(exercises: List<IndexedValue<WorkoutExercise>>): List<List<IndexedValue<WorkoutExercise>>> {
        val units = mutableListOf<MutableList<IndexedValue<WorkoutExercise>>>()
        exercises.forEach { iv ->
            val ex = iv.value
            val current = units.lastOrNull()
            val currentGroup = current?.firstOrNull()?.value
            val joins = ex.groupType == GroupType.SUPERSET && ex.groupId != null &&
                currentGroup != null && currentGroup.groupType == GroupType.SUPERSET &&
                currentGroup.groupId == ex.groupId
            if (joins) current!!.add(iv) else units.add(mutableListOf(iv))
        }
        return units
    }

    private fun sideFor(mode: SideMode): Side = when (mode) {
        SideMode.LEFT -> Side.LEFT
        SideMode.RIGHT -> Side.RIGHT
        SideMode.BILATERAL -> Side.BILATERAL
        SideMode.ALTERNATING, SideMode.NONE -> Side.NONE
    }

    // ---------------------------------------------------------------------------------------------
    // 4.3 Alongamento e lateralidade
    // ---------------------------------------------------------------------------------------------

    private fun compileStretch(b: Int, block: WorkoutBlock, out: MutableList<ExecutionStep>) {
        val exercises = orderedExercises(block).withIndex().filter { it.value.plannedSets.isNotEmpty() }
        if (exercises.isEmpty()) return
        val switchSeconds = (block.protocolConfig?.switchSideSeconds ?: defaultSwitchSideSeconds).coerceIn(3, 5)

        for (round in 0 until block.rounds) {
            exercises.forEachIndexed { listIdx, (exIdx, ex) ->
                val sets = ex.plannedSets.sortedBy { it.setNumber }
                sets.forEachIndexed { sIdx, set ->
                    val hold = set.targetDurationSeconds ?: block.workDurationSeconds ?: defaultHoldSeconds
                    val base = "b${b}_r${round}_e${exIdx}_s${set.setNumber}"
                    fun hold(side: Side, suffix: String) = ExecutionStep(
                        id = "${base}_hold_$suffix",
                        blockIndex = b,
                        blockName = block.name,
                        stepType = ExecutionStepType.STRETCH_HOLD,
                        exerciseId = ex.exerciseId,
                        exerciseName = ex.displayName,
                        exercisePosition = exIdx,
                        setNumber = set.setNumber,
                        totalSets = sets.size,
                        durationSeconds = hold,
                        side = side
                    )
                    when (set.sideMode) {
                        SideMode.ALTERNATING -> {
                            out += hold(Side.RIGHT, "r")
                            out += ExecutionStep(
                                id = "${base}_switch",
                                blockIndex = b,
                                blockName = block.name,
                                stepType = ExecutionStepType.SWITCH_SIDE_REST,
                                exerciseId = ex.exerciseId,
                                exerciseName = ex.displayName,
                                exercisePosition = exIdx,
                                durationSeconds = switchSeconds,
                                side = Side.LEFT
                            )
                            out += hold(Side.LEFT, "l")
                        }
                        SideMode.LEFT -> out += hold(Side.LEFT, "l")
                        SideMode.RIGHT -> out += hold(Side.RIGHT, "r")
                        SideMode.BILATERAL, SideMode.NONE -> out += hold(Side.BILATERAL, "b")
                    }
                    val isLast = round == block.rounds - 1 && listIdx == exercises.lastIndex && sIdx == sets.lastIndex
                    val rest = set.restSeconds ?: block.restDurationSeconds ?: 0
                    if (!isLast && rest > 0) {
                        out += ExecutionStep(
                            id = "${base}_rest",
                            blockIndex = b,
                            blockName = block.name,
                            stepType = ExecutionStepType.REST_SET,
                            durationSeconds = rest
                        )
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 4.4 HIIT intervalado
    // ---------------------------------------------------------------------------------------------

    private fun compileHiit(b: Int, block: WorkoutBlock, out: MutableList<ExecutionStep>) {
        val config = block.protocolConfig
        val first = orderedExercises(block).firstOrNull()
        val name = first?.displayName ?: block.name
        val work = block.workDurationSeconds?.takeIf { it > 0 } ?: defaultHiitWorkSeconds
        val rest = block.restDurationSeconds ?: 0
        val warmup = config?.warmupSeconds ?: 0
        val cooldown = config?.cooldownSeconds ?: 0

        fun step(id: String, type: ExecutionStepType, duration: Int, round: Int? = null) = ExecutionStep(
            id = "b${b}_$id",
            blockIndex = b,
            blockName = block.name,
            stepType = type,
            exerciseId = first?.exerciseId,
            exerciseName = name,
            exercisePosition = 0,
            durationSeconds = duration,
            roundNumber = round,
            totalRounds = block.rounds,
            hiitTargets = when (type) {
                ExecutionStepType.HIIT_WORK -> config?.workTargets.orEmpty()
                ExecutionStepType.HIIT_REST -> config?.restTargets.orEmpty()
                else -> emptyList()
            }
        )

        if (warmup > 0) out += step("warmup", ExecutionStepType.HIIT_WARMUP, warmup)
        else out += step("prepare", ExecutionStepType.PREPARE, prepareSeconds)

        for (r in 1..block.rounds) {
            out += step("r${r}_work", ExecutionStepType.HIIT_WORK, work, r)
            if (rest > 0 && r < block.rounds) out += step("r${r}_rest", ExecutionStepType.HIIT_REST, rest, r)
        }
        if (cooldown > 0) out += step("cooldown", ExecutionStepType.HIIT_COOLDOWN, cooldown)
    }

    // ---------------------------------------------------------------------------------------------
    // 4.5 Circuitos contínuos (AMRAP, For Time) e EMOM
    // ---------------------------------------------------------------------------------------------

    private fun prepare(b: Int, block: WorkoutBlock) = ExecutionStep(
        id = "b${b}_prepare",
        blockIndex = b,
        blockName = block.name,
        stepType = ExecutionStepType.PREPARE,
        exerciseName = block.name,
        durationSeconds = prepareSeconds
    )

    private fun restBlock(b: Int, block: WorkoutBlock, isLast: Boolean, out: MutableList<ExecutionStep>) {
        val rest = block.restDurationSeconds ?: 0
        if (!isLast && rest > 0) {
            out += ExecutionStep(
                id = "b${b}_restblock",
                blockIndex = b,
                blockName = block.name,
                stepType = ExecutionStepType.REST_BLOCK,
                durationSeconds = rest
            )
        }
    }

    private fun compileAmrap(b: Int, block: WorkoutBlock, isLast: Boolean, out: MutableList<ExecutionStep>) {
        val items = circuitItems(block)
        val duration = block.workDurationSeconds?.takeIf { it > 0 } ?: defaultAmrapSeconds
        val scaling = block.protocolConfig?.scalingType
        out += prepare(b, block)
        out += ExecutionStep(
            id = "b${b}_amrap",
            blockIndex = b,
            blockName = block.name,
            stepType = ExecutionStepType.AMRAP_CLOCK,
            exerciseName = block.name,
            durationSeconds = duration,
            circuitItems = items,
            scalingType = scaling
        )
        out += ExecutionStep(
            id = "b${b}_amrap_result",
            blockIndex = b,
            blockName = block.name,
            stepType = ExecutionStepType.RESULT_ENTRY,
            exerciseName = block.name,
            durationSeconds = duration,
            circuitItems = items,
            scalingType = scaling
        )
        restBlock(b, block, isLast, out)
    }

    private fun compileForTime(b: Int, block: WorkoutBlock, isLast: Boolean, out: MutableList<ExecutionStep>) {
        val items = circuitItems(block)
        val cap = block.workDurationSeconds?.takeIf { it > 0 }
        val scaling = block.protocolConfig?.scalingType
        out += prepare(b, block)
        out += ExecutionStep(
            id = "b${b}_fortime",
            blockIndex = b,
            blockName = block.name,
            stepType = ExecutionStepType.FOR_TIME_CLOCK,
            exerciseName = block.name,
            durationSeconds = cap,
            totalRounds = block.rounds,
            circuitItems = items,
            scalingType = scaling
        )
        out += ExecutionStep(
            id = "b${b}_fortime_result",
            blockIndex = b,
            blockName = block.name,
            stepType = ExecutionStepType.RESULT_ENTRY,
            exerciseName = block.name,
            durationSeconds = cap,
            totalRounds = block.rounds,
            circuitItems = items,
            scalingType = scaling
        )
        restBlock(b, block, isLast, out)
    }

    private fun compileEmom(b: Int, block: WorkoutBlock, isLast: Boolean, out: MutableList<ExecutionStep>) {
        val items = circuitItems(block)
        val interval = block.workDurationSeconds?.takeIf { it > 0 } ?: defaultEmomIntervalSeconds
        out += prepare(b, block)
        for (i in 1..block.rounds) {
            val item = items.getOrNull((i - 1) % items.size.coerceAtLeast(1))
            out += ExecutionStep(
                id = "b${b}_emom_$i",
                blockIndex = b,
                blockName = block.name,
                stepType = ExecutionStepType.EMOM_INTERVAL,
                exerciseId = item?.exerciseId,
                exerciseName = item?.exerciseName ?: block.name,
                exercisePosition = item?.position,
                targetReps = item?.targetReps,
                targetLoadKg = item?.targetLoadKg,
                durationSeconds = interval,
                roundNumber = i,
                totalRounds = block.rounds,
                circuitItems = listOfNotNull(item),
                scalingType = block.protocolConfig?.scalingType
            )
        }
        restBlock(b, block, isLast, out)
    }

    private companion object {
        val GHOST_REST_TYPES = setOf(
            ExecutionStepType.REST_SET,
            ExecutionStepType.REST_BLOCK,
            ExecutionStepType.HIIT_REST
        )
    }
}
