package com.aksoit.myfitnessapp

import com.aksoit.myfitnessapp.domain.timer.TimerEvent
import com.aksoit.myfitnessapp.domain.timer.TimerWarningPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerWarningPolicyTest {

    @Test
    fun warning10SecondsIsSuppressedWhenPhaseDurationIsLessOrEqualTo15Seconds() {
        val policy = TimerWarningPolicy()
        policy.reset(totalPhaseDurationSeconds = 10L) // Fase de 10s (ex: descanso Tabata)

        val eventsAt10 = policy.check(10L)
        assertFalse(eventsAt10.contains(TimerEvent.Warning10Seconds))

        val eventsAt3 = policy.check(3L)
        assertTrue(eventsAt3.contains(TimerEvent.CountdownTick(3)))

        val eventsAt2 = policy.check(2L)
        assertTrue(eventsAt2.contains(TimerEvent.CountdownTick(2)))

        val eventsAt1 = policy.check(1L)
        assertTrue(eventsAt1.contains(TimerEvent.CountdownTick(1)))
    }

    @Test
    fun warning10SecondsIsEmittedWhenPhaseDurationIsGreaterThan15Seconds() {
        val policy = TimerWarningPolicy()
        policy.reset(totalPhaseDurationSeconds = 60L) // Fase de 60s

        val eventsAt10 = policy.check(10L)
        assertTrue(eventsAt10.contains(TimerEvent.Warning10Seconds))

        val eventsAt3 = policy.check(3L)
        assertTrue(eventsAt3.contains(TimerEvent.CountdownTick(3)))
    }
}
