package com.aksoit.myfitnessapp.platform.timer

import android.os.SystemClock
import com.aksoit.myfitnessapp.domain.timer.MonotonicClock
import com.aksoit.myfitnessapp.domain.timer.TimerEngine
import com.aksoit.myfitnessapp.domain.timer.TimerEvent
import com.aksoit.myfitnessapp.domain.timer.TimerMode
import com.aksoit.myfitnessapp.domain.timer.TimerState
import com.aksoit.myfitnessapp.domain.timer.TimerWarningPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MonotonicTimerEngineImpl(
    private val scope: CoroutineScope,
    private val clock: MonotonicClock = MonotonicClock { SystemClock.elapsedRealtime() }
) : TimerEngine {

    private val _state = MutableStateFlow(TimerState.IDLE)
    override val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<TimerEvent> = _events.asSharedFlow()

    private val _remainingMs = MutableStateFlow(0L)
    override val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    override val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private var _mode: TimerMode = TimerMode.COUNTDOWN
    override val mode: TimerMode get() = _mode

    private var targetEndElapsedRealtime: Long = 0L
    private var pausedRemainingMs: Long = 0L
    private var stopwatchStartElapsedRealtime: Long = 0L
    private var pausedElapsedMs: Long = 0L
    private var totalDurationMs: Long = 0L

    private val warningPolicy = TimerWarningPolicy()
    private var loopJob: Job? = null

    override fun startCountdown(durationMs: Long) {
        loopJob?.cancel()
        _mode = TimerMode.COUNTDOWN
        totalDurationMs = durationMs
        pausedRemainingMs = durationMs
        warningPolicy.reset(totalDurationMs / 1000L)
        targetEndElapsedRealtime = clock.elapsedRealtime() + durationMs

        _remainingMs.value = durationMs
        _elapsedMs.value = 0L
        _state.value = TimerState.RUNNING
        _events.tryEmit(TimerEvent.Started)

        startLoop()
    }

    override fun startStopwatch(initialElapsedMs: Long) {
        loopJob?.cancel()
        _mode = TimerMode.STOPWATCH
        pausedElapsedMs = initialElapsedMs
        stopwatchStartElapsedRealtime = clock.elapsedRealtime() - initialElapsedMs

        _remainingMs.value = 0L
        _elapsedMs.value = initialElapsedMs
        _state.value = TimerState.RUNNING
        _events.tryEmit(TimerEvent.Started)

        startLoop()
    }

    override fun pause() {
        if (_state.value == TimerState.RUNNING) {
            loopJob?.cancel()
            val now = clock.elapsedRealtime()
            if (_mode == TimerMode.COUNTDOWN) {
                pausedRemainingMs = maxOf(0L, targetEndElapsedRealtime - now)
                _remainingMs.value = pausedRemainingMs
            } else {
                pausedElapsedMs = maxOf(0L, now - stopwatchStartElapsedRealtime)
                _elapsedMs.value = pausedElapsedMs
            }
            _state.value = TimerState.PAUSED
            _events.tryEmit(TimerEvent.Paused)
        }
    }

    override fun resume() {
        if (_state.value == TimerState.PAUSED) {
            val now = clock.elapsedRealtime()
            if (_mode == TimerMode.COUNTDOWN) {
                targetEndElapsedRealtime = now + pausedRemainingMs
            } else {
                stopwatchStartElapsedRealtime = now - pausedElapsedMs
            }
            _state.value = TimerState.RUNNING
            _events.tryEmit(TimerEvent.Resumed)
            startLoop()
        }
    }

    override fun cancel() {
        loopJob?.cancel()
        _state.value = TimerState.CANCELLED
        _remainingMs.value = 0L
        _elapsedMs.value = 0L
        _events.tryEmit(TimerEvent.Cancelled)
    }

    override fun currentRemainingMs(): Long = when (_state.value) {
        TimerState.RUNNING -> if (_mode == TimerMode.COUNTDOWN) maxOf(0L, targetEndElapsedRealtime - clock.elapsedRealtime()) else 0L
        TimerState.PAUSED -> pausedRemainingMs
        else -> 0L
    }

    override fun currentElapsedMs(): Long = when (_state.value) {
        TimerState.RUNNING -> if (_mode == TimerMode.STOPWATCH) maxOf(0L, clock.elapsedRealtime() - stopwatchStartElapsedRealtime) else maxOf(0L, totalDurationMs - (targetEndElapsedRealtime - clock.elapsedRealtime()))
        TimerState.PAUSED -> pausedElapsedMs
        else -> 0L
    }

    private fun startLoop() {
        loopJob = scope.launch {
            while (isActive && _state.value == TimerState.RUNNING) {
                val now = clock.elapsedRealtime()
                if (_mode == TimerMode.COUNTDOWN) {
                    val remaining = maxOf(0L, targetEndElapsedRealtime - now)
                    val elapsed = maxOf(0L, totalDurationMs - remaining)
                    _remainingMs.value = remaining
                    _elapsedMs.value = elapsed

                    val remainingSec = (remaining + 999L) / 1000L
                    val emittedEvents = warningPolicy.check(remainingSec)
                    for (evt in emittedEvents) {
                        _events.tryEmit(evt)
                    }

                    if (remaining <= 0L) {
                        _state.value = TimerState.COMPLETED
                        _events.tryEmit(TimerEvent.Completed)
                        break
                    }
                } else {
                    val elapsed = maxOf(0L, now - stopwatchStartElapsedRealtime)
                    _elapsedMs.value = elapsed
                }
                delay(100)
            }
        }
    }
}
