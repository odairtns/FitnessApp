package com.aksoit.myfitnessapp.domain.model

data class Exercise(
    val id: Long = 0,
    val stableKey: String? = null, // Chave semântica para catálogo nativo (ex: "barbell-bench-press")
    val name: String,
    val category: ExerciseCategory = ExerciseCategory.STRENGTH,
    val muscleGroup: MuscleGroup = MuscleGroup.FULL_BODY,
    val equipment: EquipmentType = EquipmentType.OTHER,
    val movementPattern: MovementPattern? = null,
    val description: String = "",
    val defaultRestSeconds: Int = 60,
    val isBodyweight: Boolean = false,
    val isCustom: Boolean = false,
    val isArchived: Boolean = false
) {
    init {
        require(name.isNotBlank()) { "O nome do exercício é obrigatório." }
        require(defaultRestSeconds >= 0) { "defaultRestSeconds não pode ser negativo." }
    }

    /** Nome normalizado usado na resolução de duplicidade (Spec 07 §3). */
    val normalizedName: String get() = normalizeExerciseName(name)
}

fun normalizeExerciseName(name: String): String = name.trim().lowercase().replace(Regex("\\s+"), " ")
