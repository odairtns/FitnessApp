package com.aksoit.myfitnessapp.domain.timer

sealed interface TimerEvent {
    data object Started : TimerEvent
    data object Paused : TimerEvent
    data object Resumed : TimerEvent
    data object Warning10Seconds : TimerEvent
    data class CountdownTick(val second: Int) : TimerEvent // 3, 2, 1
    data object Completed : TimerEvent
    data object Cancelled : TimerEvent
}
