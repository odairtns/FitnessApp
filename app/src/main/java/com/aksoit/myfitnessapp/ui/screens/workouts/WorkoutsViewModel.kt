package com.aksoit.myfitnessapp.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksoit.myfitnessapp.application.workout.ArchiveWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.DeleteWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.DuplicateWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.ObserveActiveTemplatesUseCase
import com.aksoit.myfitnessapp.application.xml.ExportWorkoutTemplateXmlUseCase
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutsViewModel(
    observeActiveTemplatesUseCase: ObserveActiveTemplatesUseCase,
    private val duplicateWorkoutTemplateUseCase: DuplicateWorkoutTemplateUseCase,
    private val archiveWorkoutTemplateUseCase: ArchiveWorkoutTemplateUseCase,
    private val deleteWorkoutTemplateUseCase: DeleteWorkoutTemplateUseCase,
    private val exportWorkoutTemplateXmlUseCase: ExportWorkoutTemplateXmlUseCase
) : ViewModel() {

    val templates: StateFlow<List<WorkoutTemplate>> = observeActiveTemplatesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun duplicateTemplate(id: Long) {
        viewModelScope.launch {
            duplicateWorkoutTemplateUseCase(id)
        }
    }

    fun archiveTemplate(id: Long) {
        viewModelScope.launch {
            archiveWorkoutTemplateUseCase(id)
        }
    }

    fun deleteTemplate(id: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val deletedPhysically = deleteWorkoutTemplateUseCase(id)
            onResult(deletedPhysically)
        }
    }

    fun exportTemplate(id: Long, onXmlReady: (String) -> Unit) {
        viewModelScope.launch {
            val xml = exportWorkoutTemplateXmlUseCase(id)
            onXmlReady(xml)
        }
    }
}
