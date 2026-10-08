package com.aksoit.myfitnessapp.domain.timer

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    CANCELLED
}

enum class TimerMode {
    COUNTDOWN,
    STOPWATCH
}
