package com.aksoit.myfitnessapp.ui.screens.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksoit.myfitnessapp.application.timer.IndependentTimerUseCase
import com.aksoit.myfitnessapp.application.xml.ImportPreviewData
import com.aksoit.myfitnessapp.application.xml.ImportWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.xml.PreviewXmlImportUseCase
import com.aksoit.myfitnessapp.domain.timer.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ToolsUiState(
    val timerMode: ToolMode = ToolMode.STOPWATCH,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val formattedTime: String = "00:00",
    val countdownMinutesInput: String = "5",
    val importPreview: ImportPreviewData? = null,
    val importSuccessMessage: String? = null,
    val importErrorMessage: String? = null
)

enum class ToolMode {
    STOPWATCH,
    COUNTDOWN,
    XML_EXCHANGE
}

class ToolsViewModel(
    private val independentTimerUseCase: IndependentTimerUseCase,
    private val previewXmlImportUseCase: PreviewXmlImportUseCase,
    private val importWorkoutTemplateUseCase: ImportWorkoutTemplateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToolsUiState())
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    init {
        observeTimer()
    }

    private fun observeTimer() {
        viewModelScope.launch {
            independentTimerUseCase.state.collect { state ->
                _uiState.value = _uiState.value.copy(
                    isRunning = state == TimerState.RUNNING,
                    isPaused = state == TimerState.PAUSED
                )
            }
        }
        viewModelScope.launch {
            independentTimerUseCase.elapsedMs.collect { ms ->
                if (_uiState.value.timerMode == ToolMode.STOPWATCH) {
                    val sec = ms / 1000L
                    _uiState.value = _uiState.value.copy(
                        formattedTime = String.format("%02d:%02d", sec / 60, sec % 60)
                    )
                }
            }
        }
        viewModelScope.launch {
            independentTimerUseCase.remainingMs.collect { ms ->
                if (_uiState.value.timerMode == ToolMode.COUNTDOWN) {
                    val sec = (ms + 999L) / 1000L
                    _uiState.value = _uiState.value.copy(
                        formattedTime = String.format("%02d:%02d", sec / 60, sec % 60)
                    )
                }
            }
        }
    }

    fun setMode(mode: ToolMode) {
        independentTimerUseCase.stop()
        _uiState.value = _uiState.value.copy(timerMode = mode, formattedTime = "00:00")
    }

    fun startStopwatch() {
        independentTimerUseCase.startStopwatch()
    }

    fun startCountdown(minutes: Int) {
        independentTimerUseCase.startCountdown(minutes * 60L)
    }

    fun pause() {
        independentTimerUseCase.pause()
    }

    fun resume() {
        independentTimerUseCase.resume()
    }

    fun reset() {
        independentTimerUseCase.reset()
        _uiState.value = _uiState.value.copy(formattedTime = "00:00")
    }

    fun previewXml(xmlContent: String) {
        viewModelScope.launch {
            try {
                val preview = previewXmlImportUseCase(xmlContent)
                _uiState.value = _uiState.value.copy(
                    importPreview = preview,
                    importErrorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    importErrorMessage = "XML inválido: ${e.message}"
                )
            }
        }
    }

    fun confirmImport(replaceExisting: Boolean) {
        val preview = _uiState.value.importPreview ?: return
        viewModelScope.launch {
            try {
                val replaceId = if (replaceExisting) preview.existingTemplateId else null
                importWorkoutTemplateUseCase(preview.template, replaceId)
                _uiState.value = _uiState.value.copy(
                    importPreview = null,
                    importSuccessMessage = "Treino importado com sucesso!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    importErrorMessage = "Erro na importação: ${e.message}"
                )
            }
        }
    }

    fun clearImportMessages() {
        _uiState.value = _uiState.value.copy(
            importSuccessMessage = null,
            importErrorMessage = null,
            importPreview = null
        )
    }
}
