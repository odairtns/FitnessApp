package com.aksoit.myfitnessapp.data.mapper

import com.aksoit.myfitnessapp.data.local.converter.RoomTypeConverters
import com.aksoit.myfitnessapp.data.local.entity.AmrapResultEntity
import com.aksoit.myfitnessapp.data.local.entity.EmomIntervalResultEntity
import com.aksoit.myfitnessapp.data.local.entity.ExerciseEntity
import com.aksoit.myfitnessapp.data.local.entity.PerformedSetEntity
import com.aksoit.myfitnessapp.data.local.entity.PersonalRecordEntity
import com.aksoit.myfitnessapp.data.local.entity.PlannedSetEntity
import com.aksoit.myfitnessapp.data.local.entity.SessionSummaryRow
import com.aksoit.myfitnessapp.data.local.entity.WorkoutBlockEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutExerciseEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutTemplateEntity
import com.aksoit.myfitnessapp.data.local.relation.ForTimeResultWithItems
import com.aksoit.myfitnessapp.data.local.relation.SessionWithPerformanceRelation
import com.aksoit.myfitnessapp.data.local.relation.TemplateWithBlocksRelation
import com.aksoit.myfitnessapp.domain.model.AmrapPartialResult
import com.aksoit.myfitnessapp.domain.model.AmrapResult
import com.aksoit.myfitnessapp.domain.model.BlockExecutionStatus
import com.aksoit.myfitnessapp.domain.model.BlockType
import com.aksoit.myfitnessapp.domain.model.EmomIntervalResult
import com.aksoit.myfitnessapp.domain.model.EmomIntervalStatus
import com.aksoit.myfitnessapp.domain.model.EquipmentType
import com.aksoit.myfitnessapp.domain.model.Exercise
import com.aksoit.myfitnessapp.domain.model.ExerciseCategory
import com.aksoit.myfitnessapp.domain.model.ExercisePerformance
import com.aksoit.myfitnessapp.domain.model.ForTimeRemainingItem
import com.aksoit.myfitnessapp.domain.model.ForTimeResult
import com.aksoit.myfitnessapp.domain.model.ForTimeStatus
import com.aksoit.myfitnessapp.domain.model.GroupType
import com.aksoit.myfitnessapp.domain.model.MovementPattern
import com.aksoit.myfitnessapp.domain.model.MuscleGroup
import com.aksoit.myfitnessapp.domain.model.PerformedSet
import com.aksoit.myfitnessapp.domain.model.PersonalRecord
import com.aksoit.myfitnessapp.domain.model.PersonalRecordType
import com.aksoit.myfitnessapp.domain.model.PlannedSet
import com.aksoit.myfitnessapp.domain.model.ScalingType
import com.aksoit.myfitnessapp.domain.model.SessionBlock
import com.aksoit.myfitnessapp.domain.model.SessionStatus
import com.aksoit.myfitnessapp.domain.model.Side
import com.aksoit.myfitnessapp.domain.model.SideMode
import com.aksoit.myfitnessapp.domain.model.WorkoutBlock
import com.aksoit.myfitnessapp.domain.model.WorkoutExercise
import com.aksoit.myfitnessapp.domain.model.WorkoutModality
import com.aksoit.myfitnessapp.domain.model.WorkoutProtocol
import com.aksoit.myfitnessapp.domain.model.WorkoutSession
import com.aksoit.myfitnessapp.domain.model.WorkoutSessionSummary
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate

/** Conversão tolerante String -> Enum (dados antigos ou corrompidos não derrubam a leitura). */
inline fun <reified T : Enum<T>> String?.toEnumOr(default: T): T =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default

// ------------------------------------------------------------------------------------------------
// Exercise
// ------------------------------------------------------------------------------------------------

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    stableKey = stableKey,
    name = name,
    category = category.toEnumOr(ExerciseCategory.STRENGTH),
    muscleGroup = muscleGroup.toEnumOr(MuscleGroup.FULL_BODY),
    equipment = equipment.toEnumOr(EquipmentType.OTHER),
    movementPattern = movementPattern?.let { enumValues<MovementPattern>().firstOrNull { p -> p.name == it } },
    description = description,
    defaultRestSeconds = defaultRestSeconds,
    isBodyweight = isBodyweight,
    isCustom = isCustom,
    isArchived = isArchived
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    stableKey = stableKey,
    name = name.trim(),
    category = category.name,
    muscleGroup = muscleGroup.name,
    equipment = equipment.name,
    movementPattern = movementPattern?.name,
    description = description,
    defaultRestSeconds = defaultRestSeconds,
    isBodyweight = isBodyweight,
    isCustom = isCustom,
    isArchived = isArchived
)

// ------------------------------------------------------------------------------------------------
// Template
// ------------------------------------------------------------------------------------------------

fun TemplateWithBlocksRelation.toDomain(): WorkoutTemplate = WorkoutTemplate(
    id = template.id,
    templateUuid = template.templateUuid,
    name = template.name,
    modality = template.modality.toEnumOr(WorkoutModality.STRENGTH),
    protocol = template.protocol.toEnumOr(WorkoutProtocol.STANDARD_STRENGTH),
    description = template.description,
    estimatedDurationSeconds = template.estimatedDurationSeconds,
    isArchived = template.isArchived,
    createdAtEpochMs = template.createdAtEpochMs,
    updatedAtEpochMs = template.updatedAtEpochMs,
    blocks = blocks.sortedBy { it.block.position }.map { b ->
        WorkoutBlock(
            id = b.block.id,
            templateId = b.block.templateId,
            blockType = b.block.blockType.toEnumOr(BlockType.WORK),
            position = b.block.position,
            name = b.block.name,
            rounds = b.block.rounds.coerceAtLeast(1),
            workDurationSeconds = b.block.workDurationSeconds,
            restDurationSeconds = b.block.restDurationSeconds,
            protocolConfig = RoomTypeConverters.decodeProtocolConfig(b.block.protocolConfigJson),
            exercises = b.exercises.sortedBy { it.workoutExercise.position }.map { e ->
                WorkoutExercise(
                    id = e.workoutExercise.id,
                    blockId = e.workoutExercise.blockId,
                    exerciseId = e.workoutExercise.exerciseId,
                    exerciseNameCustom = e.workoutExercise.exerciseNameCustom,
                    position = e.workoutExercise.position,
                    groupId = e.workoutExercise.groupId,
                    groupType = e.workoutExercise.groupType.toEnumOr(GroupType.NONE),
                    plannedSets = e.sets.sortedBy { it.setNumber }.map { it.toDomain() },
                    resolvedName = e.exercise?.name,
                    resolvedStableKey = e.exercise?.stableKey
                )
            }
        )
    }
)

fun PlannedSetEntity.toDomain(): PlannedSet = PlannedSet(
    id = id,
    workoutExerciseId = workoutExerciseId,
    setNumber = setNumber.coerceAtLeast(1),
    targetReps = targetReps,
    targetLoadKg = targetLoadKg,
    targetDurationSeconds = targetDurationSeconds,
    restSeconds = restSeconds,
    sideMode = sideMode.toEnumOr(SideMode.BILATERAL)
)

fun WorkoutTemplate.toEntity(): WorkoutTemplateEntity = WorkoutTemplateEntity(
    id = id,
    templateUuid = templateUuid,
    name = name.trim(),
    modality = modality.name,
    protocol = protocol.name,
    description = description,
    estimatedDurationSeconds = estimatedDurationSeconds,
    isArchived = isArchived,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs
)

fun WorkoutBlock.toEntity(templateId: Long, position: Int): WorkoutBlockEntity = WorkoutBlockEntity(
    templateId = templateId,
    blockType = blockType.name,
    position = position,
    name = name,
    rounds = rounds,
    workDurationSeconds = workDurationSeconds,
    restDurationSeconds = restDurationSeconds,
    protocolConfigJson = RoomTypeConverters.encodeProtocolConfig(protocolConfig)
)

fun WorkoutExercise.toEntity(blockId: Long, position: Int, exerciseIdOverride: Long? = exerciseId): WorkoutExerciseEntity =
    WorkoutExerciseEntity(
        blockId = blockId,
        exerciseId = exerciseIdOverride,
        exerciseNameCustom = exerciseNameCustom,
        position = position,
        groupId = groupId,
        groupType = groupType.name
    )

fun PlannedSet.toEntity(workoutExerciseId: Long, setNumber: Int): PlannedSetEntity = PlannedSetEntity(
    workoutExerciseId = workoutExerciseId,
    setNumber = setNumber,
    targetReps = targetReps,
    targetLoadKg = targetLoadKg,
    targetDurationSeconds = targetDurationSeconds,
    restSeconds = restSeconds,
    sideMode = sideMode.name
)

// ------------------------------------------------------------------------------------------------
// Session
// ------------------------------------------------------------------------------------------------

fun SessionWithPerformanceRelation.toDomain(): WorkoutSession = WorkoutSession(
    id = session.id,
    templateId = session.templateId,
    templateNameSnapshot = session.templateNameSnapshot,
    modalitySnapshot = session.modalitySnapshot.toEnumOr(WorkoutModality.STRENGTH),
    protocolSnapshot = session.protocolSnapshot.toEnumOr(WorkoutProtocol.STANDARD_STRENGTH),
    startedAtEpochMs = session.startedAtEpochMs,
    completedAtEpochMs = session.completedAtEpochMs,
    status = session.status.toEnumOr(SessionStatus.DRAFT),
    notes = session.notes,
    blocks = blocks.sortedBy { it.block.position }.map { b ->
        SessionBlock(
            id = b.block.id,
            sessionId = b.block.sessionId,
            sourceBlockId = b.block.sourceBlockId,
            blockNameSnapshot = b.block.blockNameSnapshot,
            blockTypeSnapshot = b.block.blockTypeSnapshot.toEnumOr(BlockType.WORK),
            position = b.block.position,
            status = b.block.status.toEnumOr(BlockExecutionStatus.PENDING),
            performances = b.performances.sortedBy { it.performance.position }.map { p ->
                ExercisePerformance(
                    id = p.performance.id,
                    sessionBlockId = p.performance.sessionBlockId,
                    exerciseId = p.performance.exerciseId,
                    exerciseNameSnapshot = p.performance.exerciseNameSnapshot,
                    position = p.performance.position,
                    performedSets = p.sets.sortedBy { it.loggedAtEpochMs }.map { it.toDomain() }
                )
            },
            amrapResult = b.amrapResults.firstOrNull()?.toDomain(),
            forTimeResult = b.forTimeResults.firstOrNull()?.toDomain(),
            emomResults = b.emomResults.sortedBy { it.intervalNumber }.map { it.toDomain() }
        )
    }
)

fun PerformedSetEntity.toDomain(): PerformedSet = PerformedSet(
    id = id,
    performanceId = performanceId,
    setNumber = setNumber.coerceAtLeast(1),
    actualLoadKg = actualLoadKg,
    actualReps = actualReps,
    durationSeconds = durationSeconds,
    side = side.toEnumOr(Side.NONE),
    isCompleted = isCompleted,
    loggedAtEpochMs = loggedAtEpochMs,
    notes = notes
)

fun PerformedSet.toEntity(performanceId: Long): PerformedSetEntity = PerformedSetEntity(
    performanceId = performanceId,
    setNumber = setNumber,
    actualLoadKg = actualLoadKg,
    actualReps = actualReps,
    durationSeconds = durationSeconds,
    side = side.name,
    isCompleted = isCompleted,
    loggedAtEpochMs = loggedAtEpochMs,
    notes = notes
)

fun AmrapResultEntity.toDomain(): AmrapResult {
    val partial = if (partialReps != null && partialReps > 0 && !partialExerciseNameSnapshot.isNullOrBlank()) {
        AmrapPartialResult(partialExerciseId, partialExerciseNameSnapshot, partialReps)
    } else null
    return AmrapResult(
        completedRounds = completedRounds.coerceAtLeast(0),
        partial = partial,
        plannedDurationSeconds = plannedDurationSeconds,
        actualDurationSeconds = actualDurationSeconds,
        scalingType = scalingType?.let { s -> enumValues<ScalingType>().firstOrNull { it.name == s } }
    )
}

fun AmrapResult.toEntity(sessionBlockId: Long): AmrapResultEntity = AmrapResultEntity(
    sessionBlockId = sessionBlockId,
    completedRounds = completedRounds,
    partialExerciseId = partial?.exerciseId,
    partialExerciseNameSnapshot = partial?.exerciseNameSnapshot,
    partialReps = partial?.partialReps,
    plannedDurationSeconds = plannedDurationSeconds,
    actualDurationSeconds = actualDurationSeconds,
    scalingType = scalingType?.name
)

fun ForTimeResultWithItems.toDomain(): ForTimeResult {
    val status = result.status.toEnumOr(ForTimeStatus.COMPLETED)
    return ForTimeResult(
        elapsedSeconds = result.elapsedSeconds.coerceAtLeast(0),
        timeCapSeconds = result.timeCapSeconds,
        status = status,
        scalingType = result.scalingType?.let { s -> enumValues<ScalingType>().firstOrNull { it.name == s } },
        remainingItems = if (status == ForTimeStatus.COMPLETED) emptyList() else items.map {
            ForTimeRemainingItem(
                exerciseId = it.exerciseId,
                exerciseNameSnapshot = it.exerciseNameSnapshot,
                targetAmount = it.targetAmount,
                completedAmount = it.completedAmount,
                remainingAmount = it.remainingAmount,
                unit = it.unit
            )
        }
    )
}

fun EmomIntervalResultEntity.toDomain(): EmomIntervalResult = EmomIntervalResult(
    id = id,
    sessionBlockId = sessionBlockId,
    intervalNumber = intervalNumber,
    exerciseNameSnapshot = exerciseNameSnapshot,
    targetReps = targetReps,
    actualReps = actualReps,
    targetLoadKg = targetLoadKg,
    actualLoadKg = actualLoadKg,
    status = status.toEnumOr(EmomIntervalStatus.NOT_LOGGED)
)

fun EmomIntervalResult.toEntity(sessionBlockId: Long): EmomIntervalResultEntity = EmomIntervalResultEntity(
    sessionBlockId = sessionBlockId,
    intervalNumber = intervalNumber,
    exerciseNameSnapshot = exerciseNameSnapshot,
    targetReps = targetReps,
    actualReps = actualReps,
    targetLoadKg = targetLoadKg,
    actualLoadKg = actualLoadKg,
    status = status.name
)

fun SessionSummaryRow.toDomain(): WorkoutSessionSummary = WorkoutSessionSummary(
    id = id,
    templateId = templateId,
    templateNameSnapshot = templateNameSnapshot,
    protocolSnapshot = protocolSnapshot.toEnumOr(WorkoutProtocol.STANDARD_STRENGTH),
    startedAtEpochMs = startedAtEpochMs,
    completedAtEpochMs = completedAtEpochMs,
    status = status.toEnumOr(SessionStatus.COMPLETED),
    totalVolumeKg = totalVolume,
    totalSets = totalSets
)

fun PersonalRecordEntity.toDomain(): PersonalRecord = PersonalRecord(
    id = id,
    exerciseId = exerciseId,
    exerciseNameSnapshot = exerciseNameSnapshot,
    recordType = recordType.toEnumOr(PersonalRecordType.MAX_LOAD),
    value = value,
    reps = reps,
    loadKg = loadKg,
    sessionId = sessionId,
    achievedAtEpochMs = achievedAtEpochMs
)
