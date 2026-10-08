package com.aksoit.myfitnessapp.domain.execution

import com.aksoit.myfitnessapp.domain.model.EmomIntervalStatus
import com.aksoit.myfitnessapp.domain.model.ForTimeRemainingItem
import com.aksoit.myfitnessapp.domain.model.RecoverableSessionData
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.StateFlow

interface WorkoutExecutionController {
    val state: StateFlow<ExecutionState>
    fun dispatch(action: ExecutionAction)
}

sealed interface ExecutionState {
    data object Idle : ExecutionState

    data class Active(
        val sessionId: Long,
        val plan: ExecutionPlan,
        val currentStep: ExecutionStep,
        val isPaused: Boolean = false,
        val elapsedTotalMs: Long = 0L,
        val currentStepRemainingMs: Long? = null,
        val currentStepElapsedMs: Long = 0L,
        val circuitRoundsCount: Int = 0,
        /** Sugestão de carga: última carga executada (mesma série → fallback exercício) ou alvo. */
        val suggestedLoadKg: Double? = null,
        val lastLoadKg: Double? = null,
        /** Status pendente do intervalo EMOM corrente (gravado ao fim do intervalo). */
        val pendingEmomStatus: EmomIntervalStatus = EmomIntervalStatus.NOT_LOGGED,
        val pendingEmomReps: Int? = null,
        /** Duração efetiva do último relógio contínuo (AMRAP/For Time) em ms. */
        val lastClockElapsedMs: Long = 0L,
        /** For Time: o cap foi atingido antes de o atleta terminar. */
        val forTimeCapReached: Boolean = false,
        val isRecovered: Boolean = false,
        val completedSetsCount: Int = 0
    ) : ExecutionState {
        val stepIndex: Int get() = plan.indexOf(currentStep.id)
        val totalSteps: Int get() = plan.steps.size
        val nextStep: ExecutionStep? get() = plan.steps.getOrNull(stepIndex + 1)
    }

    data class Completed(val sessionId: Long) : ExecutionState
    data class Cancelled(val sessionId: Long) : ExecutionState
    data class Discarded(val sessionId: Long) : ExecutionState
    data class Failed(val message: String) : ExecutionState
}

sealed interface ExecutionAction {
    data class Start(val template: WorkoutTemplate) : ExecutionAction
    data class Recover(val template: WorkoutTemplate, val data: RecoverableSessionData) : ExecutionAction
    data object CompleteCurrentStep : ExecutionAction
    data class ConfirmSet(val actualLoadKg: Double?, val actualReps: Int?) : ExecutionAction
    data object IncrementCircuitRound : ExecutionAction
    data object DecrementCircuitRound : ExecutionAction
    data class LogEmomInterval(val status: EmomIntervalStatus, val actualReps: Int?) : ExecutionAction
    data class SubmitAmrapResult(
        val rounds: Int,
        val partialExerciseId: Long?,
        val partialExerciseName: String?,
        val partialReps: Int?
    ) : ExecutionAction
    data class SubmitForTimeResult(
        val elapsedSeconds: Long,
        val isCapped: Boolean,
        val remainingItems: List<ForTimeRemainingItem> = emptyList()
    ) : ExecutionAction
    data object Pause : ExecutionAction
    data object Resume : ExecutionAction
    data object SkipCurrentStep : ExecutionAction
    data class Finish(val notes: String = "") : ExecutionAction
    data object CancelSession : ExecutionAction
    data object DiscardSession : ExecutionAction
}
