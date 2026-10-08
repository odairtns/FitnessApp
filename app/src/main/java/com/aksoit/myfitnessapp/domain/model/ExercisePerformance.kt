package com.aksoit.myfitnessapp.domain.model

data class ExercisePerformance(
    val id: Long = 0,
    val sessionBlockId: Long = 0,
    val exerciseId: Long? = null,
    val exerciseNameSnapshot: String,
    val position: Int,
    val performedSets: List<PerformedSet> = emptyList()
)
