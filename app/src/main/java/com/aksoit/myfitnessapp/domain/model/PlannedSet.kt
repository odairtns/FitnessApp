package com.aksoit.myfitnessapp.domain.model

data class PlannedSet(
    val id: Long = 0,
    val workoutExerciseId: Long = 0,
    val setNumber: Int,
    val targetReps: Int? = null,
    val targetLoadKg: Double? = null, // Estritamente em kg
    val targetDurationSeconds: Int? = null,
    val restSeconds: Int? = null,
    val sideMode: SideMode = SideMode.BILATERAL
) {
    init {
        require(setNumber >= 1) { "setNumber deve ser >= 1." }
        require(targetLoadKg == null || targetLoadKg >= 0.0) { "targetLoadKg deve ser >= 0 kg." }
        require(targetReps == null || targetReps >= 0) { "targetReps inválido." }
        require(restSeconds == null || restSeconds >= 0) { "restSeconds inválido." }
    }
}
