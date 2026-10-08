package com.aksoit.myfitnessapp.domain.timer

/**
 * Regra da Guarda do Alerta de 10 Segundos (Spec 05 §3).
 *
 * - Warning10Seconds só é emitido se a fase tiver duração total > 15 s.
 * - Contagem 3, 2, 1 é sempre emitida (uma vez por segundo).
 */
class TimerWarningPolicy {
    private var warning10Emitted = false
    private val countdownTicksEmitted = mutableSetOf<Long>()
    private var totalPhaseDurationSeconds: Long = 0L

    fun reset(totalPhaseDurationSeconds: Long) {
        this.totalPhaseDurationSeconds = totalPhaseDurationSeconds
        warning10Emitted = false
        countdownTicksEmitted.clear()
    }

    fun check(remainingSeconds: Long): List<TimerEvent> {
        val events = mutableListOf<TimerEvent>()
        // Guarda de Duração Curta: Suprime TTS de 10s se a fase for <= 15s
        if (remainingSeconds == 10L && totalPhaseDurationSeconds > 15L && !warning10Emitted) {
            warning10Emitted = true
            events += TimerEvent.Warning10Seconds
        }
        // Contagem regressiva acústica final (3, 2, 1) emite sempre
        if (remainingSeconds in 1L..3L && !countdownTicksEmitted.contains(remainingSeconds)) {
            countdownTicksEmitted.add(remainingSeconds)
            events += TimerEvent.CountdownTick(remainingSeconds.toInt())
        }
        return events
    }
}
