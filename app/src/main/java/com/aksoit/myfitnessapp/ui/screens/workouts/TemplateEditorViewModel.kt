package com.aksoit.myfitnessapp.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksoit.myfitnessapp.application.workout.CreateWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.GetWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.UpdateWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.domain.model.BlockType
import com.aksoit.myfitnessapp.domain.model.Exercise
import com.aksoit.myfitnessapp.domain.model.PlannedSet
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
            // Inicializa template vazio com um bloco de musculação
            _uiState.value = TemplateEditorUiState(
                blocks = listOf(
                    WorkoutBlock(
                        blockType = BlockType.WORK,
                        position = 0,
                        name = "Bloco Principal",
                        rounds = 1
                    )
                )
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
                    blocks = template.blocks
                )
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateDescription(desc: String) = _uiState.update { it.copy(description = desc) }
    fun updateProtocol(protocol: WorkoutProtocol) = _uiState.update { it.copy(protocol = protocol) }

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
            val defaultSets = listOf(
                PlannedSet(setNumber = 1, targetReps = 10, targetLoadKg = 20.0, restSeconds = 60, sideMode = SideMode.BILATERAL),
                PlannedSet(setNumber = 2, targetReps = 10, targetLoadKg = 20.0, restSeconds = 60, sideMode = SideMode.BILATERAL),
                PlannedSet(setNumber = 3, targetReps = 10, targetLoadKg = 20.0, restSeconds = 60, sideMode = SideMode.BILATERAL)
            )
            val updatedExList = block.exercises + WorkoutExercise(
                exerciseId = exercise.id.takeIf { it > 0 },
                exerciseNameCustom = exercise.name,
                position = nextPos,
                plannedSets = defaultSets,
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
