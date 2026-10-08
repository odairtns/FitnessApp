package com.aksoit.myfitnessapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HiitTarget(
    val type: HiitTargetType,
    val value: String, // String para comportar formatações como Pace "04:30" ou velocidades "14.5"
    val unit: HiitTargetUnit
)

@Serializable
enum class HiitTargetType {
    SPEED, INCLINE, PACE, RPM, SPM, RESISTANCE, DAMPER, HEART_RATE_ZONE
}

@Serializable
enum class HiitTargetUnit {
    KM_H, MPH, PERCENT, MIN_PER_KM, MIN_PER_MILE, RPM, STROKES_PER_MIN, LEVEL, ZONE
}
