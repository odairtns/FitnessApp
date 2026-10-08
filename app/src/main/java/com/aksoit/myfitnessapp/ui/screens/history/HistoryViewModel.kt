package com.aksoit.myfitnessapp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksoit.myfitnessapp.application.history.GetSessionDetailUseCase
import com.aksoit.myfitnessapp.application.history.ObservePersonalRecordsUseCase
import com.aksoit.myfitnessapp.application.history.ObserveSessionsUseCase
import com.aksoit.myfitnessapp.domain.model.PersonalRecord
import com.aksoit.myfitnessapp.domain.model.WorkoutSession
import com.aksoit.myfitnessapp.domain.model.WorkoutSessionSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    observeSessionsUseCase: ObserveSessionsUseCase,
    observePersonalRecordsUseCase: ObservePersonalRecordsUseCase,
    private val getSessionDetailUseCase: GetSessionDetailUseCase
) : ViewModel() {

    val sessions: StateFlow<List<WorkoutSessionSummary>> = observeSessionsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalRecords: StateFlow<List<PersonalRecord>> = observePersonalRecordsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSession = MutableStateFlow<WorkoutSession?>(null)
    val selectedSession: StateFlow<WorkoutSession?> = _selectedSession.asStateFlow()

    fun loadSessionDetail(sessionId: Long) {
        viewModelScope.launch {
            _selectedSession.value = getSessionDetailUseCase(sessionId)
        }
    }
}
