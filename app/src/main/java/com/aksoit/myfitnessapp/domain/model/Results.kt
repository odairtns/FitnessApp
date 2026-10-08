package com.aksoit.myfitnessapp.domain.model

data class AmrapResult(
    val completedRounds: Int,
    val partial: AmrapPartialResult?, // Só preenchido se houver reps parciais válidas
    val plannedDurationSeconds: Long,
    val actualDurationSeconds: Long,
    val scalingType: ScalingType?
) {
    init {
        require(completedRounds >= 0) { "completedRounds não pode ser negativo." }
    }
}

data class AmrapPartialResult(
    val exerciseId: Long?,
    val exerciseNameSnapshot: String,
    val partialReps: Int
) {
    init {
        require(partialReps > 0) { "partialReps deve ser maior que zero." }
        require(exerciseNameSnapshot.isNotBlank()) { "exerciseNameSnapshot obrigatório para reps parciais." }
    }
}

data class ForTimeResult(
    val elapsedSeconds: Long,
    val timeCapSeconds: Long?,
    val status: ForTimeStatus,
    val scalingType: ScalingType?,
    val remainingItems: List<ForTimeRemainingItem> = emptyList()
) {
    init {
        require(elapsedSeconds >= 0) { "elapsedSeconds não pode ser negativo." }
        if (status == ForTimeStatus.COMPLETED) {
            require(remainingItems.isEmpty()) { "For Time concluído não pode ter saldo restante." }
        }
    }
}

data class ForTimeRemainingItem(
    val exerciseId: Long?,
    val exerciseNameSnapshot: String,
    val targetAmount: Int,
    val completedAmount: Int,
    val remainingAmount: Int,
    val unit: String = "reps"
) {
    init {
        require(targetAmount >= 0 && completedAmount >= 0 && remainingAmount >= 0) { "Quantidades inválidas." }
    }
}

data class EmomIntervalResult(
    val id: Long = 0,
    val sessionBlockId: Long = 0,
    val intervalNumber: Int,
    val exerciseNameSnapshot: String = "",
    val targetReps: Int,
    val actualReps: Int? = null,
    val targetLoadKg: Double? = null,
    val actualLoadKg: Double? = null,
    val status: EmomIntervalStatus = EmomIntervalStatus.NOT_LOGGED
)
