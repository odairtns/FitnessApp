package com.aksoit.myfitnessapp

import com.aksoit.myfitnessapp.domain.execution.ExecutionPlan
import com.aksoit.myfitnessapp.domain.execution.ExecutionStep
import com.aksoit.myfitnessapp.domain.execution.ExecutionStepType
import com.aksoit.myfitnessapp.domain.execution.PlanBlock
import com.aksoit.myfitnessapp.domain.execution.PlanExercise
import com.aksoit.myfitnessapp.domain.model.BlockType
import com.aksoit.myfitnessapp.domain.model.WorkoutModality
import com.aksoit.myfitnessapp.domain.model.WorkoutProtocol
import com.aksoit.myfitnessapp.ui.screens.player.PlanExerciseStatus
import com.aksoit.myfitnessapp.ui.screens.player.WorkoutPlayerUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerNavigationTest {

    @Test
    fun planExerciseItemsDeriveStatusesAndCountsCorrectly() {
        val plan = ExecutionPlan(
            templateId = 1L,
            templateName = "Treino A",
            modality = WorkoutModality.STRENGTH,
            protocol = WorkoutProtocol.STANDARD_STRENGTH,
            blocks = listOf(
                PlanBlock(
                    index = 0,
                    sourceBlockId = 10L,
                    name = "Bloco 1",
                    blockType = BlockType.WORK,
                    exercises = listOf(
                        PlanExercise(position = 0, exerciseId = 100L, name = "Supino Reto"),
                        PlanExercise(position = 1, exerciseId = 101L, name = "Supino Inclinado"),
                        PlanExercise(position = 2, exerciseId = 102L, name = "Tríceps Testa")
                    )
                )
            ),
            steps = listOf(
                ExecutionStep(id = "s0", blockIndex = 0, blockName = "B1", stepType = ExecutionStepType.SET_WORK, exercisePosition = 0, exerciseName = "Supino Reto", setNumber = 1),
                ExecutionStep(id = "s1", blockIndex = 0, blockName = "B1", stepType = ExecutionStepType.SET_WORK, exercisePosition = 0, exerciseName = "Supino Reto", setNumber = 2),
                ExecutionStep(id = "s2", blockIndex = 0, blockName = "B1", stepType = ExecutionStepType.SET_WORK, exercisePosition = 1, exerciseName = "Supino Inclinado", setNumber = 1),
                ExecutionStep(id = "s3", blockIndex = 0, blockName = "B1", stepType = ExecutionStepType.SET_WORK, exercisePosition = 1, exerciseName = "Supino Inclinado", setNumber = 2),
                ExecutionStep(id = "s4", blockIndex = 0, blockName = "B1", stepType = ExecutionStepType.SET_WORK, exercisePosition = 2, exerciseName = "Tríceps Testa", setNumber = 1)
            )
        )

        // Supino Reto totalmente concluído (s0 e s1); Supino Inclinado é o atual (s2); Tríceps Testa pendente
        val state = WorkoutPlayerUiState(
            sessionId = 1L,
            plan = plan,
            currentStepIndex = 2,
            currentStep = plan.steps[2],
            totalSteps = plan.steps.size,
            completedStepIds = setOf("s0", "s1")
        )

        val items = state.planExerciseItems
        assertEquals(3, items.size)

        // Supino Reto: 2/2 concluído -> COMPLETED
        assertEquals("Supino Reto", items[0].exerciseName)
        assertEquals(2, items[0].completedSets)
        assertEquals(2, items[0].totalSets)
        assertEquals(PlanExerciseStatus.COMPLETED, items[0].status)

        // Supino Inclinado: atual -> CURRENT
        assertEquals("Supino Inclinado", items[1].exerciseName)
        assertEquals(0, items[1].completedSets)
        assertEquals(2, items[1].totalSets)
        assertEquals(PlanExerciseStatus.CURRENT, items[1].status)

        // Tríceps Testa: 0/1 -> PENDING
        assertEquals("Tríceps Testa", items[2].exerciseName)
        assertEquals(0, items[2].completedSets)
        assertEquals(1, items[2].totalSets)
        assertEquals(PlanExerciseStatus.PENDING, items[2].status)

        // Display index
        assertEquals(2, state.currentExerciseDisplayIndex)
        assertEquals(3, state.totalExercisesCount)
    }

    @Test
    fun roundCountersDoNotGoBelowZero() {
        var state = WorkoutPlayerUiState(circuitRoundsCount = 0)
        state = state.copy(circuitRoundsCount = maxOf(0, state.circuitRoundsCount - 1))
        assertEquals(0, state.circuitRoundsCount)

        state = state.copy(circuitRoundsCount = state.circuitRoundsCount + 1)
        assertEquals(1, state.circuitRoundsCount)
    }
}
