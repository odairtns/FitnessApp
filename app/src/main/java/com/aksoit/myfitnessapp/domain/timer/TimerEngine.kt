package com.aksoit.myfitnessapp.domain.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato do motor de tempo monotônico (Spec 05 §1).
 * Implementação Android em platform/timer (SystemClock.elapsedRealtime()).
 */
interface TimerEngine {
    val state: StateFlow<TimerState>
    val events: SharedFlow<TimerEvent>
    val remainingMs: StateFlow<Long>
    val elapsedMs: StateFlow<Long>
    val mode: TimerMode

    fun startCountdown(durationMs: Long)
    fun startStopwatch(initialElapsedMs: Long = 0L)
    fun pause()
    fun resume()
    fun cancel()

    /** Leitura instantânea (não depende do último tick emitido). */
    fun currentRemainingMs(): Long
    fun currentElapsedMs(): Long
}

fun interface TimerEngineFactory {
    fun create(scope: CoroutineScope): TimerEngine
}

/** Fonte monotônica de tempo (SystemClock.elapsedRealtime() na plataforma). */
fun interface MonotonicClock {
    fun elapsedRealtime(): Long
}

/** Relógio de calendário — restrito a carimbos de data, nunca para medir duração (Regra 6). */
fun interface WallClock {
    fun currentTimeMillis(): Long
}
