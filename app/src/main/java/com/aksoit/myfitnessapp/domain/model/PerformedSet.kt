package com.aksoit.myfitnessapp.domain.model

data class PerformedSet(
    val id: Long = 0,
    val performanceId: Long = 0,
    val setNumber: Int,
    val actualLoadKg: Double? = null, // Estritamente armazenado em kg
    val actualReps: Int? = null,
    val durationSeconds: Int? = null,
    val side: Side = Side.NONE,
    val isCompleted: Boolean = true,
    val loggedAtEpochMs: Long,
    val notes: String = ""
) {
    init {
        require(setNumber >= 1) { "setNumber deve ser >= 1." }
        require(actualLoadKg == null || actualLoadKg >= 0.0) { "actualLoadKg deve ser >= 0 kg." }
        require(actualReps == null || actualReps >= 0) { "actualReps inválido." }
    }
}
