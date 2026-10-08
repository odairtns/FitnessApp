package com.aksoit.myfitnessapp.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksoit.myfitnessapp.application.exercise.ObserveExercisesUseCase
import com.aksoit.myfitnessapp.application.exercise.SaveExerciseUseCase
import com.aksoit.myfitnessapp.domain.model.EquipmentType
import com.aksoit.myfitnessapp.domain.model.Exercise
import com.aksoit.myfitnessapp.domain.model.ExerciseCategory
import com.aksoit.myfitnessapp.domain.model.MuscleGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExerciseLibraryUiState(
    val query: String = "",
    val exercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList()
)

class ExerciseLibraryViewModel(
    observeExercisesUseCase: ObserveExercisesUseCase,
    private val saveExerciseUseCase: SaveExerciseUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _allExercises = observeExercisesUseCase()

    val uiState: StateFlow<ExerciseLibraryUiState> = combine(
        _query,
        _allExercises
    ) { query, list ->
        val filtered = if (query.isBlank()) list else {
            list.filter { it.name.contains(query, ignoreCase = true) || it.muscleGroup.name.contains(query, ignoreCase = true) }
        }
        ExerciseLibraryUiState(
            query = query,
            exercises = list,
            filteredExercises = filtered
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExerciseLibraryUiState())

    fun updateQuery(q: String) {
        _query.value = q
    }

    fun createCustomExercise(name: String, muscleGroup: MuscleGroup, equipment: EquipmentType) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val ex = Exercise(
                name = name.trim(),
                category = ExerciseCategory.STRENGTH,
                muscleGroup = muscleGroup,
                equipment = equipment,
                isCustom = true
            )
            saveExerciseUseCase(ex)
        }
    }
}
