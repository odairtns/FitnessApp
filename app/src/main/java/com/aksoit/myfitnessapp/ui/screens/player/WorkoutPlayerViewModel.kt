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
import com.aksoit.myfitnessapp.domain.model.AmrapPartialResult
import com.aksoit.myfitnessapp.domain.model.AmrapResult
import com.aksoit.myfitnessapp.domain.model.ForTimeResult
import com.aksoit.myfitnessapp.domain.model.ForTimeStatus
import com.aksoit.myfitnessapp.domain.model.RecoveryCheckpoint
import com.aksoit.myfitnessapp.domain.timer.MonotonicClock
import com.aksoit.myfitnessapp.domain.timer.TimerEngine
import com.aksoit.myfitnessapp.domain.timer.TimerEvent
import com.aksoit.myfitnessapp.domain.timer.WallClock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PlanExerciseStatus {
    PENDING,
    CURRENT,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED
}

data class PlanExerciseItem(
    val blockIndex: Int,
    val exercisePosition: Int,
    val exerciseId: Long?,
    val exerciseName: String,
    val completedSets: Int,
    val totalSets: Int,
    val status: PlanExerciseStatus,
    val firstStepId: String
)

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
    val completedStepIds: Set<String> = emptySet(),
    val skippedStepIds: Set<String> = emptySet(),
    val isPlanSheetOpen: Boolean = false,
    val isAmrapResultDialogOpen: Boolean = false,
    val selectedAmrapPartialExerciseId: Long? = null,
    val selectedAmrapPartialExerciseName: String = "",
    val amrapPartialRepsInput: String = "",
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false
) {
    /** Itens estruturados do plano do treino (COR-003, COR-010). */
    val planExerciseItems: List<PlanExerciseItem>
        get() {
            val p = plan ?: return emptyList()
            val items = mutableListOf<PlanExerciseItem>()
            for (block in p.blocks) {
                for (ex in block.exercises) {
                    val workSteps = p.steps.filter {
                        it.blockIndex == block.index &&
                        it.exercisePosition == ex.position &&
                        (it.stepType == ExecutionStepType.SET_WORK || it.stepType == ExecutionStepType.STRETCH_HOLD)
                    }
                    val totalSets = workSteps.size.coerceAtLeast(1)
                    val completedSets = workSteps.count { it.id in completedStepIds }
                    val isCurrent = currentStep?.blockIndex == block.index && currentStep?.exercisePosition == ex.position
                    val isAllSkipped = workSteps.isNotEmpty() && workSteps.all { it.id in skippedStepIds }

                    val status = when {
                        completedSets == totalSets && totalSets > 0 -> PlanExerciseStatus.COMPLETED
                        isCurrent -> PlanExerciseStatus.CURRENT
                        completedSets > 0 -> PlanExerciseStatus.IN_PROGRESS
                        isAllSkipped -> PlanExerciseStatus.SKIPPED
                        else -> PlanExerciseStatus.PENDING
                    }

                    val firstStepId = workSteps.firstOrNull { it.id !in completedStepIds }?.id
                        ?: workSteps.firstOrNull()?.id
                        ?: p.steps.firstOrNull { it.blockIndex == block.index && it.exercisePosition == ex.position }?.id
                        ?: ""

                    items += PlanExerciseItem(
                        blockIndex = block.index,
                        exercisePosition = ex.position,
                        exerciseId = ex.exerciseId,
                        exerciseName = ex.name,
                        completedSets = completedSets,
                        totalSets = totalSets,
                        status = status,
                        firstStepId = firstStepId
                    )
                }
            }
            return items
        }

    val currentExerciseDisplayIndex: Int
        get() {
            val items = planExerciseItems
            val idx = items.indexOfFirst { it.status == PlanExerciseStatus.CURRENT }
            return if (idx >= 0) idx + 1 else 1
        }

    val totalExercisesCount: Int
        get() = planExerciseItems.size.coerceAtLeast(1)
}

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
            ExecutionStepType.EMOM_INTERVAL -> {
                advanceToNextStep()
            }
            ExecutionStepType.AMRAP_CLOCK -> {
                // Ao terminar o tempo do AMRAP, abre diálogo para registrar reps parciais (COR-002)
                _uiState.update {
                    it.copy(
                        isAmrapResultDialogOpen = true,
                        selectedAmrapPartialExerciseName = current.circuitItems.firstOrNull()?.exerciseName ?: "",
                        selectedAmrapPartialExerciseId = current.circuitItems.firstOrNull()?.exerciseId
                    )
                }
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
                _uiState.update { it.copy(completedStepIds = it.completedStepIds + current.id) }
                advanceToNextStep()
            } else if (current.stepType == ExecutionStepType.AMRAP_CLOCK) {
                // Solicita dados parciais antes de salvar ou salva diretamente
                _uiState.update {
                    it.copy(
                        isAmrapResultDialogOpen = true,
                        selectedAmrapPartialExerciseName = current.circuitItems.firstOrNull()?.exerciseName ?: "",
                        selectedAmrapPartialExerciseId = current.circuitItems.firstOrNull()?.exerciseId
                    )
                }
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
                advanceToNextStep()
            } else {
                advanceToNextStep()
            }
        }
    }

    fun skipCurrentStep() {
        val current = _uiState.value.currentStep
        if (current != null) {
            _uiState.update { it.copy(skippedStepIds = it.skippedStepIds + current.id) }
        }
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

    /**
     * COR-004 & COR-009: Navegar até um exercício selecionado no plano sem artificialmente
     * concluí-lo nem alterar séries ou templates.
     */
    fun selectExercise(blockIndex: Int, exercisePosition: Int) {
        val state = _uiState.value
        val plan = state.plan ?: return

        // Procura a primeira série de trabalho ainda não realizada desse exercício
        val targetStep = plan.steps.firstOrNull {
            it.blockIndex == blockIndex &&
            it.exercisePosition == exercisePosition &&
            (it.stepType == ExecutionStepType.SET_WORK || it.stepType == ExecutionStepType.STRETCH_HOLD) &&
            it.id !in state.completedStepIds
        } ?: plan.steps.firstOrNull {
            it.blockIndex == blockIndex && it.exercisePosition == exercisePosition
        } ?: return

        val targetIdx = plan.steps.indexOfFirst { it.id == targetStep.id }.coerceAtLeast(0)

        _uiState.update {
            it.copy(
                currentStepIndex = targetIdx,
                currentStep = targetStep,
                isPlanSheetOpen = false,
                actualRepsInput = (targetStep.targetReps ?: 10).toString(),
                actualLoadInput = (targetStep.targetLoadKg ?: 0.0).toString()
            )
        }

        viewModelScope.launch {
            resolveSuggestedLoad(targetStep)
            saveRecoveryCheckpoint(targetStep)
            // Se o passo tem timer e não é descanso continuado, inicia o timer apropriado
            if (targetStep.isTimed && targetStep.stepType != ExecutionStepType.REST_SET) {
                startStepTimer(targetStep)
            }
        }
    }

    /**
     * COR-009: Selecionar um step específico pelo ID.
     */
    fun selectStep(stepId: String) {
        val plan = _uiState.value.plan ?: return
        val targetStep = plan.getStep(stepId) ?: return
        val targetIdx = plan.indexOf(stepId).coerceAtLeast(0)

        _uiState.update {
            it.copy(
                currentStepIndex = targetIdx,
                currentStep = targetStep,
                isPlanSheetOpen = false,
                actualRepsInput = (targetStep.targetReps ?: 10).toString(),
                actualLoadInput = (targetStep.targetLoadKg ?: 0.0).toString()
            )
        }

        viewModelScope.launch {
            resolveSuggestedLoad(targetStep)
            saveRecoveryCheckpoint(targetStep)
            if (targetStep.isTimed && targetStep.stepType != ExecutionStepType.REST_SET) {
                startStepTimer(targetStep)
            }
        }
    }

    fun openPlanSheet() = _uiState.update { it.copy(isPlanSheetOpen = true) }
    fun closePlanSheet() = _uiState.update { it.copy(isPlanSheetOpen = false) }

    fun openAmrapResultDialog() = _uiState.update { it.copy(isAmrapResultDialogOpen = true) }
    fun closeAmrapResultDialog() = _uiState.update { it.copy(isAmrapResultDialogOpen = false) }
    fun updateAmrapPartialReps(reps: String) = _uiState.update { it.copy(amrapPartialRepsInput = reps) }
    fun selectAmrapPartialExercise(exerciseId: Long?, exerciseName: String) = _uiState.update {
        it.copy(selectedAmrapPartialExerciseId = exerciseId, selectedAmrapPartialExerciseName = exerciseName)
    }

    fun confirmAmrapResult() {
        val state = _uiState.value
        val current = state.currentStep ?: return
        val partialReps = state.amrapPartialRepsInput.toIntOrNull()
        val partialResult = if (partialReps != null && partialReps > 0 && state.selectedAmrapPartialExerciseName.isNotBlank()) {
            AmrapPartialResult(
                exerciseId = state.selectedAmrapPartialExerciseId,
                exerciseNameSnapshot = state.selectedAmrapPartialExerciseName,
                partialReps = partialReps
            )
        } else null

        viewModelScope.launch {
            saveAmrapResultUseCase(
                sessionId = state.sessionId,
                blockIndex = current.blockIndex,
                result = AmrapResult(
                    completedRounds = state.circuitRoundsCount,
                    partial = partialResult,
                    plannedDurationSeconds = current.durationSeconds?.toLong() ?: 600L,
                    actualDurationSeconds = current.durationSeconds?.toLong() ?: 600L,
                    scalingType = current.scalingType
                )
            )
            _uiState.update { it.copy(isAmrapResultDialogOpen = false) }
            advanceToNextStep()
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
