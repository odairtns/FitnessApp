package com.aksoit.myfitnessapp.domain.model

data class PersonalRecord(
    val id: Long = 0,
    val exerciseId: Long?,
    val exerciseNameSnapshot: String,
    val recordType: PersonalRecordType,
    val value: Double,          // kg para MAX_LOAD/ESTIMATED_1RM; repetições para MAX_REPS
    val reps: Int?,
    val loadKg: Double?,
    val sessionId: Long?,
    val achievedAtEpochMs: Long
)

/** Fórmula de Epley para 1RM estimado. */
fun estimateOneRepMaxKg(loadKg: Double, reps: Int): Double =
    if (reps <= 1) loadKg else loadKg * (1.0 + reps / 30.0)
