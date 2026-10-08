package com.aksoit.myfitnessapp.domain.model

data class PersonalRecord(
    val id: Long = 0,
    val exerciseId: Long?,
    val exerciseNameSnapshot: String,
    val recordType: PersonalRecordType,
    val value: Double,          // kg para MAX_LOAD/MAX_VOLUME; reps para MAX_REPS; segundos para BEST_TIME
    val reps: Int?,
    val loadKg: Double?,
    val sessionId: Long?,
    val achievedAtEpochMs: Long
)
