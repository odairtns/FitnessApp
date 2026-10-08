package com.aksoit.myfitnessapp.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksoit.myfitnessapp.application.workout.CreateWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.GetWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.UpdateWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.domain.model.BlockType
import com.aksoit.myfitnessapp.domain.model.Exercise
import com.aksoit.myfitnessapp.domain.model.PlannedSet
import com.aksoit.myfitnessapp.domain.model.ProtocolConfig
import com.aksoit.myfitnessapp.domain.model.SideMode
import com.aksoit.myfitnessapp.domain.model.WorkoutBlock
import com.aksoit.myfitnessapp.domain.model.WorkoutExercise
import com.aksoit.myfitnessapp.domain.model.WorkoutModality
import com.aksoit.myfitnessapp.domain.model.WorkoutProtocol
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class TemplateEditorUiState(
    val templateId: Long? = null,
    val name: String = "",
    val description: String = "",
    val modality: WorkoutModality = WorkoutModality.STRENGTH,
    val protocol: WorkoutProtocol = WorkoutProtocol.STANDARD_STRENGTH,
    val blocks: List<WorkoutBlock> = emptyList(),
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class TemplateEditorViewModel(
    private val getWorkoutTemplateUseCase: GetWorkoutTemplateUseCase,
    private val createWorkoutTemplateUseCase: CreateWorkoutTemplateUseCase,
    private val updateWorkoutTemplateUseCase: UpdateWorkoutTemplateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateEditorUiState())
    val uiState: StateFlow<TemplateEditorUiState> = _uiState.asStateFlow()

    fun loadTemplate(id: Long?) {
        if (id == null || id <= 0L) {
            val initialProtocol = WorkoutProtocol.STANDARD_STRENGTH
            _uiState.value = TemplateEditorUiState(
                modality = initialProtocol.defaultModality,
                protocol = initialProtocol,
                blocks = createDefaultBlocksForProtocol(initialProtocol)
            )
            return
        }

        viewModelScope.launch {
            val template = getWorkoutTemplateUseCase(id)
            if (template != null) {
                _uiState.value = TemplateEditorUiState(
                    templateId = template.id,
                    name = template.name,
                    description = template.description,
                    modality = template.modality,
                    protocol = template.protocol,
                    blocks = if (template.blocks.isEmpty()) createDefaultBlocksForProtocol(template.protocol) else template.blocks
                )
            }
        }
    }

    private fun createDefaultBlocksForProtocol(protocol: WorkoutProtocol): List<WorkoutBlock> {
        return when (protocol) {
            WorkoutProtocol.STANDARD_STRENGTH,
            WorkoutProtocol.HYPERTROPHY -> listOf(
                WorkoutBlock(
                    blockType = BlockType.WORK,
                    position = 0,
                    name = "Bloco Principal",
                    rounds = 1,
                    restDurationSeconds = 90
                )
            )
            WorkoutProtocol.AMRAP -> listOf(
                WorkoutBlock(
                    blockType = BlockType.CIRCUIT,
                    position = 0,
                    name = "Circuito AMRAP",
                    rounds = 1,
                    workDurationSeconds = 600 // 10 minutos padrão
                )
            )
            WorkoutProtocol.HIIT_INTERVALS -> listOf(
                WorkoutBlock(
                    blockType = BlockType.WORK,
                    position = 0,
                    name = "Tiros HIIT",
                    rounds = 8,
                    workDurationSeconds = 30,
                    restDurationSeconds = 15,
                    protocolConfig = ProtocolConfig(warmupSeconds = 180, cooldownSeconds = 120)
                )
            )
            WorkoutProtocol.EMOM -> listOf(
                WorkoutBlock(
                    blockType = BlockType.CIRCUIT,
                    position = 0,
                    name = "EMOM",
                    rounds = 10,
                    workDurationSeconds = 60
                )
            )
            WorkoutProtocol.FOR_TIME -> listOf(
                WorkoutBlock(
                    blockType = BlockType.CIRCUIT,
                    position = 0,
                    name = "For Time",
                    rounds = 1,
                    workDurationSeconds = 900 // 15 minutos cap
                )
            )
            WorkoutProtocol.STRETCH -> listOf(
                WorkoutBlock(
                    blockType = BlockType.WORK,
                    position = 0,
                    name = "Alongamento",
                    rounds = 1,
                    workDurationSeconds = 30,
                    protocolConfig = ProtocolConfig(switchSideSeconds = 5)
                )
            )
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateDescription(desc: String) = _uiState.update { it.copy(description = desc) }

    fun updateModality(modality: WorkoutModality) {
        _uiState.update { it.copy(modality = modality) }
    }

    fun updateProtocol(protocol: WorkoutProtocol) {
        _uiState.update { state ->
            val newModality = protocol.defaultModality
            // Se o usuário ainda não adicionou exercícios, regenera o bloco apropriado para o protocolo
            val hasExercises = state.blocks.any { it.exercises.isNotEmpty() }
            val newBlocks = if (!hasExercises || state.templateId == null) {
                createDefaultBlocksForProtocol(protocol)
            } else {
                state.blocks
            }
            state.copy(protocol = protocol, modality = newModality, blocks = newBlocks)
        }
    }

    fun updateBlockRounds(blockIndex: Int, rounds: Int) {
        _uiState.update { state ->
            val updated = state.blocks.toMutableList()
            val block = updated.getOrNull(blockIndex) ?: return@update state
            updated[blockIndex] = block.copy(rounds = maxOf(1, rounds))
            state.copy(blocks = updated)
        }
    }

    fun updateBlockWorkDurationSeconds(blockIndex: Int, seconds: Int?) {
        _uiState.update { state ->
            val updated = state.blocks.toMutableList()
            val block = updated.getOrNull(blockIndex) ?: return@update state
            updated[blockIndex] = block.copy(workDurationSeconds = seconds)
            state.copy(blocks = updated)
        }
    }

    fun updateBlockRestDurationSeconds(blockIndex: Int, seconds: Int?) {
        _uiState.update { state ->
            val updated = state.blocks.toMutableList()
            val block = updated.getOrNull(blockIndex) ?: return@update state
            updated[blockIndex] = block.copy(restDurationSeconds = seconds)
            state.copy(blocks = updated)
        }
    }

    fun updateBlockProtocolConfig(blockIndex: Int, config: ProtocolConfig) {
        _uiState.update { state ->
            val updated = state.blocks.toMutableList()
            val block = updated.getOrNull(blockIndex) ?: return@update state
            updated[blockIndex] = block.copy(protocolConfig = config)
            state.copy(blocks = updated)
        }
    }

    fun addBlock(name: String = "Novo Bloco", blockType: BlockType = BlockType.WORK) {
        _uiState.update { state ->
            val nextPos = state.blocks.size
            state.copy(
                blocks = state.blocks + WorkoutBlock(
                    blockType = blockType,
                    position = nextPos,
                    name = name,
                    rounds = 1
                )
            )
        }
    }

    fun removeBlock(blockIndex: Int) {
        _uiState.update { state ->
            state.copy(blocks = state.blocks.filterIndexed { index, _ -> index != blockIndex })
        }
    }

    fun addExerciseToBlock(blockIndex: Int, exercise: Exercise) {
        _uiState.update { state ->
            val updatedBlocks = state.blocks.toMutableList()
            val block = updatedBlocks.getOrNull(blockIndex) ?: return@update state
            val nextPos = block.exercises.size

            val plannedSets = when (state.protocol) {
                WorkoutProtocol.AMRAP,
                WorkoutProtocol.EMOM,
                WorkoutProtocol.FOR_TIME -> listOf(
                    PlannedSet(setNumber = 1, targetReps = 10, targetLoadKg = null, sideMode = SideMode.BILATERAL)
                )
                WorkoutProtocol.STRETCH -> listOf(
                    PlannedSet(setNumber = 1, targetDurationSeconds = block.workDurationSeconds ?: 30, sideMode = SideMode.ALTERNATING)
                )
                else -> listOf(
                    PlannedSet(setNumber = 1, targetReps = 10, targetLoadKg = 20.0, restSeconds = 60, sideMode = SideMode.BILATERAL),
                    PlannedSet(setNumber = 2, targetReps = 10, targetLoadKg = 20.0, restSeconds = 60, sideMode = SideMode.BILATERAL),
                    PlannedSet(setNumber = 3, targetReps = 10, targetLoadKg = 20.0, restSeconds = 60, sideMode = SideMode.BILATERAL)
                )
            }

            val updatedExList = block.exercises + WorkoutExercise(
                exerciseId = exercise.id.takeIf { it > 0 },
                exerciseNameCustom = exercise.name,
                position = nextPos,
                plannedSets = plannedSets,
                resolvedName = exercise.name,
                resolvedStableKey = exercise.stableKey
            )
            updatedBlocks[blockIndex] = block.copy(exercises = updatedExList)
            state.copy(blocks = updatedBlocks)
        }
    }

    fun removeExercise(blockIndex: Int, exerciseIndex: Int) {
        _uiState.update { state ->
            val updatedBlocks = state.blocks.toMutableList()
            val block = updatedBlocks.getOrNull(blockIndex) ?: return@update state
            val updatedExList = block.exercises.filterIndexed { idx, _ -> idx != exerciseIndex }
            updatedBlocks[blockIndex] = block.copy(exercises = updatedExList)
            state.copy(blocks = updatedBlocks)
        }
    }

    /** Atualiza reps alvo de um exercício de circuito (AMRAP/EMOM/For Time) */
    fun updateCircuitExerciseTargetReps(blockIndex: Int, exerciseIndex: Int, reps: Int) {
        _uiState.update { state ->
            val updatedBlocks = state.blocks.toMutableList()
            val block = updatedBlocks.getOrNull(blockIndex) ?: return@update state
            val ex = block.exercises.getOrNull(exerciseIndex) ?: return@update state
            val set = ex.plannedSets.firstOrNull() ?: PlannedSet(setNumber = 1)
            val updatedSet = set.copy(targetReps = reps)
            val updatedEx = ex.copy(plannedSets = listOf(updatedSet))
            val updatedExercises = block.exercises.toMutableList().apply { set(exerciseIndex, updatedEx) }
            updatedBlocks[blockIndex] = block.copy(exercises = updatedExercises)
            state.copy(blocks = updatedBlocks)
        }
    }

    fun addSet(blockIndex: Int, exerciseIndex: Int) {
        _uiState.update { state ->
            val updatedBlocks = state.blocks.toMutableList()
            val block = updatedBlocks.getOrNull(blockIndex) ?: return@update state
            val ex = block.exercises.getOrNull(exerciseIndex) ?: return@update state
            val nextSetNum = ex.plannedSets.size + 1
            val lastSet = ex.plannedSets.lastOrNull()
            val newSet = PlannedSet(
                setNumber = nextSetNum,
                targetReps = lastSet?.targetReps ?: 10,
                targetLoadKg = lastSet?.targetLoadKg ?: 20.0,
                restSeconds = lastSet?.restSeconds ?: 60,
                sideMode = lastSet?.sideMode ?: SideMode.BILATERAL
            )
            val updatedEx = ex.copy(plannedSets = ex.plannedSets + newSet)
            val updatedExList = block.exercises.toMutableList().apply { set(exerciseIndex, updatedEx) }
            updatedBlocks[blockIndex] = block.copy(exercises = updatedExList)
            state.copy(blocks = updatedBlocks)
        }
    }

    fun removeSet(blockIndex: Int, exerciseIndex: Int, setIndex: Int) {
        _uiState.update { state ->
            val updatedBlocks = state.blocks.toMutableList()
            val block = updatedBlocks.getOrNull(blockIndex) ?: return@update state
            val ex = block.exercises.getOrNull(exerciseIndex) ?: return@update state
            if (ex.plannedSets.size <= 1) return@update state // Mantém pelo menos 1 série
            val updatedSets = ex.plannedSets.filterIndexed { idx, _ -> idx != setIndex }
                .mapIndexed { idx, s -> s.copy(setNumber = idx + 1) }
            val updatedEx = ex.copy(plannedSets = updatedSets)
            val updatedExList = block.exercises.toMutableList().apply { set(exerciseIndex, updatedEx) }
            updatedBlocks[blockIndex] = block.copy(exercises = updatedExList)
            state.copy(blocks = updatedBlocks)
        }
    }

    fun save() {
        val current = _uiState.value
        if (current.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Digite um nome para o treino.") }
            return
        }

        viewModelScope.launch {
            try {
                val template = WorkoutTemplate(
                    id = current.templateId ?: 0L,
                    templateUuid = UUID.randomUUID().toString(),
                    name = current.name,
                    modality = current.modality,
                    protocol = current.protocol,
                    description = current.description,
                    blocks = current.blocks,
                    createdAtEpochMs = System.currentTimeMillis(),
                    updatedAtEpochMs = System.currentTimeMillis()
                )

                if (current.templateId != null && current.templateId > 0L) {
                    updateWorkoutTemplateUseCase(template)
                } else {
                    createWorkoutTemplateUseCase(template)
                }
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Erro ao salvar treino.") }
            }
        }
    }
}
