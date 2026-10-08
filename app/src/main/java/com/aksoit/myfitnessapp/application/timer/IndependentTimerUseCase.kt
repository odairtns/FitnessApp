package com.aksoit.myfitnessapp.application.timer

import com.aksoit.myfitnessapp.domain.timer.TimerEngine
import com.aksoit.myfitnessapp.domain.timer.TimerEvent
import com.aksoit.myfitnessapp.domain.timer.TimerState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Controla cronômetro, timers regressivos e timers avulsos (AMRAP/EMOM/Tabata) da aba Tools.
 *
 * REGRA INEGOCIÁVEL 5: Ferramentas avulsas NUNCA injetam nem invocam SessionRepository,
 * garantindo isolamento total do histórico.
 */
class IndependentTimerUseCase(
    private val timerEngine: TimerEngine
) {
    val state: StateFlow<TimerState> = timerEngine.state
    val remainingMs: StateFlow<Long> = timerEngine.remainingMs
    val elapsedMs: StateFlow<Long> = timerEngine.elapsedMs
    val events: SharedFlow<TimerEvent> = timerEngine.events

    fun startStopwatch() {
        timerEngine.startStopwatch()
    }

    fun startCountdown(durationSeconds: Long) {
        timerEngine.startCountdown(durationSeconds * 1000L)
    }

    fun pause() {
        timerEngine.pause()
    }

    fun resume() {
        timerEngine.resume()
    }

    fun stop() {
        timerEngine.cancel()
    }

    fun reset() {
        timerEngine.cancel()
    }
}
