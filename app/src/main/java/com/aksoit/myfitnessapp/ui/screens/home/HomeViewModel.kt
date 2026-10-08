package com.aksoit.myfitnessapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksoit.myfitnessapp.application.history.ObserveWeeklyVolumeUseCase
import com.aksoit.myfitnessapp.application.session.DiscardWorkoutUseCase
import com.aksoit.myfitnessapp.application.session.GetRecoverableSessionUseCase
import com.aksoit.myfitnessapp.application.workout.ObserveActiveTemplatesUseCase
import com.aksoit.myfitnessapp.domain.model.RecoverableSessionData
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val weeklyTonnageKg: Double = 0.0,
    val activeTemplates: List<WorkoutTemplate> = emptyList(),
    val recoverableSession: RecoverableSessionData? = null,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val observeActiveTemplatesUseCase: ObserveActiveTemplatesUseCase,
    private val observeWeeklyVolumeUseCase: ObserveWeeklyVolumeUseCase,
    private val getRecoverableSessionUseCase: GetRecoverableSessionUseCase,
    private val discardWorkoutUseCase: DiscardWorkoutUseCase
) : ViewModel() {

    private val _recoverableSession = MutableStateFlow<RecoverableSessionData?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        observeActiveTemplatesUseCase(),
        observeWeeklyVolumeUseCase(),
        _recoverableSession
    ) { templates, volume, recoverable ->
        HomeUiState(
            weeklyTonnageKg = volume,
            activeTemplates = templates,
            recoverableSession = recoverable,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    fun checkRecoverableSession() {
        viewModelScope.launch {
            _recoverableSession.value = getRecoverableSessionUseCase()
        }
    }

    fun discardRecoverableSession(sessionId: Long) {
        viewModelScope.launch {
            discardWorkoutUseCase(sessionId)
            _recoverableSession.value = null
        }
    }
}
