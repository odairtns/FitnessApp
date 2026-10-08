package com.aksoit.myfitnessapp.domain.model

data class WorkoutSession(
    val id: Long = 0,
    val templateId: Long? = null, // Opcional: mera referência, sem acoplamento rígido
    val templateNameSnapshot: String,
    val modalitySnapshot: WorkoutModality,
    val protocolSnapshot: WorkoutProtocol,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long? = null,
    val status: SessionStatus = SessionStatus.DRAFT,
    val notes: String = "",
    val blocks: List<SessionBlock> = emptyList()
) {
    /** Tonelagem total Σ(carga_kg × reps) de séries concluídas. */
    val totalVolumeKg: Double
        get() = blocks.sumOf { b ->
            b.performances.sumOf { p ->
                p.performedSets.filter { it.isCompleted }
                    .sumOf { (it.actualLoadKg ?: 0.0) * (it.actualReps ?: 0) }
            }
        }

    val totalCompletedSets: Int
        get() = blocks.sumOf { b -> b.performances.sumOf { p -> p.performedSets.count { it.isCompleted } } }
}

/** Projeção leve usada em listas de histórico. */
data class WorkoutSessionSummary(
    val id: Long,
    val templateId: Long?,
    val templateNameSnapshot: String,
    val protocolSnapshot: WorkoutProtocol,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long?,
    val status: SessionStatus,
    val totalVolumeKg: Double,
    val totalSets: Int
)

/** Dados mínimos para restaurar uma sessão interrompida (Spec 04 §6). */
data class RecoverableSessionData(
    val sessionId: Long,
    val templateId: Long?,
    val templateName: String,
    val startedAtEpochMs: Long,
    val currentStepId: String?,
    val remainingMs: Long,
    val isPaused: Boolean,
    val isStopwatch: Boolean,
    val circuitRoundsCount: Int,
    val elapsedTotalMs: Long
)

/** Checkpoint gravado a cada mudança crítica de passo/timer. */
data class RecoveryCheckpoint(
    val sessionId: Long,
    val currentStepId: String,
    val targetEndElapsedRealtime: Long,
    val remainingDurationMs: Long,
    val isPaused: Boolean,
    val isStopwatch: Boolean,
    val circuitRoundsCount: Int,
    val elapsedTotalMs: Long,
    val updatedAtEpochMs: Long
)
