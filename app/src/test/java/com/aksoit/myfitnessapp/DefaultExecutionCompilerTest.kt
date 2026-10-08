package com.aksoit.myfitnessapp

import com.aksoit.myfitnessapp.domain.execution.DefaultExecutionCompiler
import com.aksoit.myfitnessapp.domain.execution.ExecutionStepType
import com.aksoit.myfitnessapp.domain.model.BlockType
import com.aksoit.myfitnessapp.domain.model.PlannedSet
import com.aksoit.myfitnessapp.domain.model.Side
import com.aksoit.myfitnessapp.domain.model.SideMode
import com.aksoit.myfitnessapp.domain.model.WorkoutBlock
import com.aksoit.myfitnessapp.domain.model.WorkoutExercise
import com.aksoit.myfitnessapp.domain.model.WorkoutModality
import com.aksoit.myfitnessapp.domain.model.WorkoutProtocol
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class DefaultExecutionCompilerTest {

    private val compiler = DefaultExecutionCompiler()

    @Test
    fun strengthProtocolHasNoFinalRestAfterLastSet() {
        val template = WorkoutTemplate(
            templateUuid = UUID.randomUUID().toString(),
            name = "Treino Força",
            modality = WorkoutModality.STRENGTH,
            protocol = WorkoutProtocol.STANDARD_STRENGTH,
            blocks = listOf(
                WorkoutBlock(
                    blockType = BlockType.WORK,
                    position = 0,
                    name = "Bloco 1",
                    exercises = listOf(
                        WorkoutExercise(
                            position = 0,
                            exerciseId = 1L,
                            exerciseNameCustom = "Supino",
                            plannedSets = listOf(
                                PlannedSet(setNumber = 1, targetReps = 10, restSeconds = 90),
                                PlannedSet(setNumber = 2, targetReps = 8, restSeconds = 90),
                                PlannedSet(setNumber = 3, targetReps = 6, restSeconds = 120)
                            )
                        )
                    )
                )
            ),
            createdAtEpochMs = 0L,
            updatedAtEpochMs = 0L
        )

        val plan = compiler.compile(template)
        val steps = plan.steps

        assertEquals(ExecutionStepType.SET_WORK, steps[0].stepType)
        assertEquals(ExecutionStepType.REST_SET, steps[1].stepType)
        assertEquals(ExecutionStepType.SET_WORK, steps[2].stepType)
        assertEquals(ExecutionStepType.REST_SET, steps[3].stepType)
        assertEquals(ExecutionStepType.SET_WORK, steps[4].stepType)
        // Regra de Ouro: Sem descanso intermediário após a última série do bloco
        assertEquals(ExecutionStepType.COMPLETE, steps[5].stepType)
    }

    @Test
    fun alternatingStretchCompilesToRightThenSwitchRestThenLeft() {
        val template = WorkoutTemplate(
            templateUuid = UUID.randomUUID().toString(),
            name = "Alongamento",
            modality = WorkoutModality.MOBILITY,
            protocol = WorkoutProtocol.STRETCH,
            blocks = listOf(
                WorkoutBlock(
                    blockType = BlockType.WORK,
                    position = 0,
                    name = "Alongamento Pernas",
                    exercises = listOf(
                        WorkoutExercise(
                            position = 0,
                            exerciseId = 2L,
                            exerciseNameCustom = "Posterior",
                            plannedSets = listOf(
                                PlannedSet(setNumber = 1, targetDurationSeconds = 30, sideMode = SideMode.ALTERNATING)
                            )
                        )
                    )
                )
            ),
            createdAtEpochMs = 0L,
            updatedAtEpochMs = 0L
        )

        val plan = compiler.compile(template)
        val steps = plan.steps

        assertEquals(Side.RIGHT, steps[0].side)
        assertEquals(ExecutionStepType.STRETCH_HOLD, steps[0].stepType)

        assertEquals(ExecutionStepType.SWITCH_SIDE_REST, steps[1].stepType)

        assertEquals(Side.LEFT, steps[2].side)
        assertEquals(ExecutionStepType.STRETCH_HOLD, steps[2].stepType)
    }

    @Test
    fun executionCompilerNeverMutatesWorkoutTemplate() {
        val template = WorkoutTemplate(
            templateUuid = "uuid-test",
            name = "Treino Imutável",
            modality = WorkoutModality.STRENGTH,
            protocol = WorkoutProtocol.STANDARD_STRENGTH,
            blocks = listOf(
                WorkoutBlock(
                    blockType = BlockType.WORK,
                    position = 0,
                    name = "Bloco",
                    exercises = listOf(
                        WorkoutExercise(
                            position = 0,
                            exerciseId = 1L,
                            exerciseNameCustom = "Agachamento",
                            plannedSets = listOf(PlannedSet(setNumber = 1, targetReps = 5, targetLoadKg = 100.0))
                        )
                    )
                )
            ),
            createdAtEpochMs = 12345L,
            updatedAtEpochMs = 12345L
        )

        val clone = template.copy()
        compiler.compile(template)

        assertEquals(clone, template)
    }
}
