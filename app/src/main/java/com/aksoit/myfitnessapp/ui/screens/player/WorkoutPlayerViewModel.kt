package com.aksoit.myfitnessapp.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksoit.myfitnessapp.application.history.GetLastLoadByExerciseAndSetUseCase
import com.aksoit.myfitnessapp.application.session.CancelWorkoutUseCase
import com.aksoit.myfitnessapp.application.session.ConfirmSetUseCase
import com.aksoit.myfitnessapp.application.session.DiscardWorkoutUseCase
import com.aksoit.myfitnessapp.application.session.FinishWorkoutUseCase
import com.aksoit.myfitnessapp.application.session.GetRecoverableSessionUseCase
import com.aksoit.myfitnessapp.application.session.SaveAmrapResultUseCase
import com.aksoit.myfitnessapp.application.session.SaveForTimeResultUseCase
import com.aksoit.myfitnessapp.application.session.SaveRecoveryCheckpointUseCase
import com.aksoit.myfitnessapp.application.session.StartWorkoutUseCase
import com.aksoit.myfitnessapp.application.workout.GetWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.domain.execution.ExecutionPlan
import com.aksoit.myfitnessapp.domain.execution.ExecutionStep
import com.aksoit.myfitnessapp.domain.execution.ExecutionStepType
import com.aksoit.myfitnessapp.domain.model.AmrapResult
import com.aksoit.myfitnessapp.domain.model.ForTimeResult
import com.aksoit.myfitnessapp.domain.model.ForTimeStatus
import com.aksoit.myfitnessapp.domain.model.RecoveryCheckpoint
import com.aksoit.myfitnessapp.domain.timer.MonotonicClock
import com.aksoit.myfitnessapp.domain.timer.TimerEngine
import com.aksoit.myfitnessapp.domain.timer.TimerEvent
import com.aksoit.myfitnessapp.domain.timer.TimerState
import com.aksoit.myfitnessapp.domain.timer.WallClock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutPlayerUiState(
    val sessionId: Long = 0L,
    val plan: ExecutionPlan? = null,
    val currentStepIndex: Int = 0,
    val currentStep: ExecutionStep? = null,
    val totalSteps: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val remainingSeconds: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val actualLoadInput: String = "",
    val actualRepsInput: String = "",
    val suggestedLoadKg: Double? = null,
    val circuitRoundsCount: Int = 0,
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false
)

sealed interface WorkoutPlayerEffect {
    data class NavigateToDetail(val sessionId: Long) : WorkoutPlayerEffect
    data object NavigateBack : WorkoutPlayerEffect
    data class ShowToast(val message: String) : WorkoutPlayerEffect
}

class WorkoutPlayerViewModel(
    private val templateId: Long,
    private val getWorkoutTemplateUseCase: GetWorkoutTemplateUseCase,
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val confirmSetUseCase: ConfirmSetUseCase,
    private val saveAmrapResultUseCase: SaveAmrapResultUseCase,
    private val saveForTimeResultUseCase: SaveForTimeResultUseCase,
    private val finishWorkoutUseCase: FinishWorkoutUseCase,
    private val cancelWorkoutUseCase: CancelWorkoutUseCase,
    private val discardWorkoutUseCase: DiscardWorkoutUseCase,
    private val getRecoverableSessionUseCase: GetRecoverableSessionUseCase,
    private val saveRecoveryCheckpointUseCase: SaveRecoveryCheckpointUseCase,
    private val getLastLoadByExerciseAndSetUseCase: GetLastLoadByExerciseAndSetUseCase,
    val timerEngine: TimerEngine,
    private val wallClock: WallClock,
    private val monotonicClock: MonotonicClock
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutPlayerUiState())
    val uiState: StateFlow<WorkoutPlayerUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<WorkoutPlayerEffect>(extraBufferCapacity = 8)
    val effect: SharedFlow<WorkoutPlayerEffect> = _effect.asSharedFlow()

    init {
        initializePlayer()
        observeTimer()
    }

    private fun initializePlayer() {
        viewModelScope.launch {
            val template = getWorkoutTemplateUseCase(templateId)
            if (template == null) {
                _effect.emit(WorkoutPlayerEffect.NavigateBack)
                return@launch
            }

            // Verifica se há sessão recuperável correspondente
            val recoverable = getRecoverableSessionUseCase()
            if (recoverable != null && recoverable.templateId == templateId) {
                val startResult = startWorkoutUseCase(template)
                setupPlan(recoverable.sessionId, startResult.plan, recoverable.currentStepId)
            } else {
                val startResult = startWorkoutUseCase(template)
                setupPlan(startResult.sessionId, startResult.plan, null)
            }
        }
    }

    private suspend fun setupPlan(sessionId: Long, plan: ExecutionPlan, resumeStepId: String?) {
        val initialIdx = if (resumeStepId != null) {
            plan.steps.indexOfFirst { it.id == resumeStepId }.coerceAtLeast(0)
        } else 0

        val initialStep = plan.steps.getOrNull(initialIdx) ?: plan.initialStep

        _uiState.update {
            it.copy(
                sessionId = sessionId,
                plan = plan,
                currentStepIndex = initialIdx,
                currentStep = initialStep,
                totalSteps = plan.steps.size,
                isRunning = true,
                actualRepsInput = (initialStep.targetReps ?: 10).toString(),
                actualLoadInput = (initialStep.targetLoadKg ?: 0.0).toString()
            )
        }

        resolveSuggestedLoad(initialStep)
        startStepTimer(initialStep)
    }

    private suspend fun resolveSuggestedLoad(step: ExecutionStep) {
        if (step.stepType == ExecutionStepType.SET_WORK) {
            val lastLoad = getLastLoadByExerciseAndSetUseCase(
                step.exerciseId,
                step.exerciseName,
                step.setNumber ?: 1
            )
            val loadToUse = lastLoad ?: step.targetLoadKg ?: 0.0
            _uiState.update {
                it.copy(
                    suggestedLoadKg = lastLoad,
                    actualLoadInput = if (loadToUse > 0.0) loadToUse.toString() else it.actualLoadInput
                )
            }
        }
    }

    private fun startStepTimer(step: ExecutionStep) {
        val duration = step.durationSeconds ?: 0
        if (duration > 0) {
            timerEngine.startCountdown(duration * 1000L)
        } else if (step.stepType == ExecutionStepType.FOR_TIME_CLOCK) {
            timerEngine.startStopwatch()
        }
    }

    private fun observeTimer() {
        viewModelScope.launch {
            timerEngine.remainingMs.collect { ms ->
                _uiState.update { it.copy(remainingSeconds = (ms + 999L) / 1000L) }
            }
        }
        viewModelScope.launch {
            timerEngine.elapsedMs.collect { ms ->
                _uiState.update { it.copy(elapsedSeconds = ms / 1000L) }
            }
        }
        viewModelScope.launch {
            timerEngine.events.collect { event ->
                if (event is TimerEvent.Completed) {
                    onTimerCompleted()
                }
            }
        }
    }

    private fun onTimerCompleted() {
        val current = _uiState.value.currentStep ?: return
        when (current.stepType) {
            ExecutionStepType.REST_SET,
            ExecutionStepType.REST_BLOCK,
            ExecutionStepType.PREPARE,
            ExecutionStepType.HIIT_REST,
            ExecutionStepType.SWITCH_SIDE_REST -> {
                advanceToNextStep()
            }
            ExecutionStepType.HIIT_WORK,
            ExecutionStepType.STRETCH_HOLD,
            ExecutionStepType.EMOM_INTERVAL,
            ExecutionStepType.AMRAP_CLOCK -> {
                advanceToNextStep()
            }
            else -> {}
        }
    }

    fun completeCurrentStep() {
        val state = _uiState.value
        val current = state.currentStep ?: return

        viewModelScope.launch {
            if (current.stepType == ExecutionStepType.SET_WORK) {
                // Grava o fato da série realizada
                val load = state.actualLoadInput.toDoubleOrNull()
                val reps = state.actualRepsInput.toIntOrNull()
                confirmSetUseCase(
                    sessionId = state.sessionId,
                    blockIndex = current.blockIndex,
                    exercisePosition = current.exercisePosition ?: 0,
                    setNumber = current.setNumber ?: 1,
                    actualLoadKg = load,
                    actualReps = reps
                )
            } else if (current.stepType == ExecutionStepType.AMRAP_CLOCK) {
                saveAmrapResultUseCase(
                    sessionId = state.sessionId,
                    blockIndex = current.blockIndex,
                    result = AmrapResult(
                        completedRounds = state.circuitRoundsCount,
                        partial = null,
                        plannedDurationSeconds = current.durationSeconds?.toLong() ?: 600L,
                        actualDurationSeconds = current.durationSeconds?.toLong() ?: 600L,
                        scalingType = current.scalingType
                    )
                )
            } else if (current.stepType == ExecutionStepType.FOR_TIME_CLOCK) {
                saveForTimeResultUseCase(
                    sessionId = state.sessionId,
                    blockIndex = current.blockIndex,
                    result = ForTimeResult(
                        elapsedSeconds = state.elapsedSeconds,
                        timeCapSeconds = current.durationSeconds?.toLong(),
                        status = ForTimeStatus.COMPLETED,
                        scalingType = current.scalingType
                    )
                )
            }

            advanceToNextStep()
        }
    }

    fun skipCurrentStep() {
        advanceToNextStep()
    }

    private fun advanceToNextStep() {
        val state = _uiState.value
        val plan = state.plan ?: return
        val nextIdx = state.currentStepIndex + 1

        if (nextIdx >= plan.steps.size || plan.steps[nextIdx].stepType == ExecutionStepType.COMPLETE) {
            finishWorkout()
            return
        }

        val nextStep = plan.steps[nextIdx]
        _uiState.update {
            it.copy(
                currentStepIndex = nextIdx,
                currentStep = nextStep,
                actualRepsInput = (nextStep.targetReps ?: 10).toString(),
                actualLoadInput = (nextStep.targetLoadKg ?: 0.0).toString()
            )
        }

        viewModelScope.launch {
            resolveSuggestedLoad(nextStep)
            saveRecoveryCheckpoint(nextStep)
            startStepTimer(nextStep)
        }
    }

    private suspend fun saveRecoveryCheckpoint(step: ExecutionStep) {
        saveRecoveryCheckpointUseCase(
            RecoveryCheckpoint(
                sessionId = _uiState.value.sessionId,
                currentStepId = step.id,
                targetEndElapsedRealtime = monotonicClock.elapsedRealtime() + (_uiState.value.remainingSeconds * 1000L),
                remainingDurationMs = _uiState.value.remainingSeconds * 1000L,
                isPaused = _uiState.value.isPaused,
                isStopwatch = step.stepType == ExecutionStepType.FOR_TIME_CLOCK,
                circuitRoundsCount = _uiState.value.circuitRoundsCount,
                elapsedTotalMs = _uiState.value.elapsedSeconds * 1000L,
                updatedAtEpochMs = wallClock.currentTimeMillis()
            )
        )
    }

    fun updateActualLoad(load: String) = _uiState.update { it.copy(actualLoadInput = load) }
    fun updateActualReps(reps: String) = _uiState.update { it.copy(actualRepsInput = reps) }

    fun incrementCircuitRound() = _uiState.update { it.copy(circuitRoundsCount = it.circuitRoundsCount + 1) }
    fun decrementCircuitRound() = _uiState.update { it.copy(circuitRoundsCount = maxOf(0, it.circuitRoundsCount - 1)) }

    fun togglePlayPause() {
        if (_uiState.value.isPaused) {
            timerEngine.resume()
            _uiState.update { it.copy(isPaused = false) }
        } else {
            timerEngine.pause()
            _uiState.update { it.copy(isPaused = true) }
        }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            timerEngine.cancel()
            finishWorkoutUseCase(_uiState.value.sessionId)
            _uiState.update { it.copy(isCompleted = true) }
            _effect.emit(WorkoutPlayerEffect.NavigateToDetail(_uiState.value.sessionId))
        }
    }

    fun cancelWorkout() {
        viewModelScope.launch {
            timerEngine.cancel()
            cancelWorkoutUseCase(_uiState.value.sessionId)
            _uiState.update { it.copy(isCancelled = true) }
            _effect.emit(WorkoutPlayerEffect.NavigateToDetail(_uiState.value.sessionId))
        }
    }

    fun discardWorkout() {
        viewModelScope.launch {
            timerEngine.cancel()
            discardWorkoutUseCase(_uiState.value.sessionId)
            _effect.emit(WorkoutPlayerEffect.NavigateBack)
        }
    }
}
