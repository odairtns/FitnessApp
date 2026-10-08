package com.aksoit.myfitnessapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "amrap_results",
    foreignKeys = [
        ForeignKey(
            entity = SessionBlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_block_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_block_id"], unique = true)]
)
data class AmrapResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_block_id") val sessionBlockId: Long,
    @ColumnInfo(name = "completed_rounds") val completedRounds: Int,
    @ColumnInfo(name = "partial_exercise_id") val partialExerciseId: Long?,
    @ColumnInfo(name = "partial_exercise_name_snapshot") val partialExerciseNameSnapshot: String?,
    @ColumnInfo(name = "partial_reps") val partialReps: Int?,
    @ColumnInfo(name = "planned_duration_seconds") val plannedDurationSeconds: Long,
    @ColumnInfo(name = "actual_duration_seconds") val actualDurationSeconds: Long,
    @ColumnInfo(name = "scaling_type") val scalingType: String?
)

@Entity(
    tableName = "emom_interval_results",
    foreignKeys = [
        ForeignKey(
            entity = SessionBlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_block_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_block_id", "interval_number"], unique = true)]
)
data class EmomIntervalResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_block_id") val sessionBlockId: Long,
    @ColumnInfo(name = "interval_number") val intervalNumber: Int,
    @ColumnInfo(name = "exercise_name_snapshot") val exerciseNameSnapshot: String,
    @ColumnInfo(name = "target_reps") val targetReps: Int,
    @ColumnInfo(name = "actual_reps") val actualReps: Int?,
    @ColumnInfo(name = "target_load_kg") val targetLoadKg: Double?,
    @ColumnInfo(name = "actual_load_kg") val actualLoadKg: Double?,
    @ColumnInfo(name = "status") val status: String
)

@Entity(
    tableName = "for_time_results",
    foreignKeys = [
        ForeignKey(
            entity = SessionBlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_block_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_block_id"], unique = true)]
)
data class ForTimeResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_block_id") val sessionBlockId: Long,
    @ColumnInfo(name = "elapsed_seconds") val elapsedSeconds: Long,
    @ColumnInfo(name = "time_cap_seconds") val timeCapSeconds: Long?,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "scaling_type") val scalingType: String?
)

@Entity(
    tableName = "for_time_remaining_items",
    foreignKeys = [
        ForeignKey(
            entity = ForTimeResultEntity::class,
            parentColumns = ["id"],
            childColumns = ["result_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["result_id"])]
)
data class ForTimeRemainingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "result_id") val resultId: Long,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long?,
    @ColumnInfo(name = "exercise_name_snapshot") val exerciseNameSnapshot: String,
    @ColumnInfo(name = "target_amount") val targetAmount: Int,
    @ColumnInfo(name = "completed_amount") val completedAmount: Int,
    @ColumnInfo(name = "remaining_amount") val remainingAmount: Int,
    @ColumnInfo(name = "unit") val unit: String
)

@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["exercise_name_snapshot", "record_type"], unique = true),
        Index(value = ["session_id"]),
        Index(value = ["exercise_id"])
    ]
)
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long?,
    @ColumnInfo(name = "exercise_name_snapshot") val exerciseNameSnapshot: String,
    @ColumnInfo(name = "record_type") val recordType: String,
    @ColumnInfo(name = "value") val value: Double,
    @ColumnInfo(name = "reps") val reps: Int?,
    @ColumnInfo(name = "load_kg") val loadKg: Double?,
    @ColumnInfo(name = "session_id") val sessionId: Long?,
    @ColumnInfo(name = "achieved_at_epoch_ms") val achievedAtEpochMs: Long
)

@Entity(
    tableName = "timer_recovery",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_id"], unique = true)]
)
data class TimerRecoveryEntity(
    @PrimaryKey @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "current_step_id") val currentStepId: String,
    @ColumnInfo(name = "target_end_elapsed_realtime") val targetEndElapsedRealtime: Long,
    @ColumnInfo(name = "remaining_duration_ms") val remainingDurationMs: Long,
    @ColumnInfo(name = "is_paused") val isPaused: Boolean,
    @ColumnInfo(name = "is_stopwatch") val isStopwatch: Boolean,
    @ColumnInfo(name = "circuit_rounds_count") val circuitRoundsCount: Int,
    @ColumnInfo(name = "elapsed_total_ms") val elapsedTotalMs: Long,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long
)

/** Linha achatada usada para recomputar recordes pessoais. */
data class SetFactRow(
    @ColumnInfo(name = "exercise_id") val exerciseId: Long?,
    @ColumnInfo(name = "exercise_name_snapshot") val exerciseNameSnapshot: String,
    @ColumnInfo(name = "actual_load_kg") val actualLoadKg: Double?,
    @ColumnInfo(name = "actual_reps") val actualReps: Int?,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "logged_at_epoch_ms") val loggedAtEpochMs: Long
)

/** Projeção de resumo de sessão para listas. */
data class SessionSummaryRow(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "template_id") val templateId: Long?,
    @ColumnInfo(name = "template_name_snapshot") val templateNameSnapshot: String,
    @ColumnInfo(name = "protocol_snapshot") val protocolSnapshot: String,
    @ColumnInfo(name = "started_at_epoch_ms") val startedAtEpochMs: Long,
    @ColumnInfo(name = "completed_at_epoch_ms") val completedAtEpochMs: Long?,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "total_volume") val totalVolume: Double,
    @ColumnInfo(name = "total_sets") val totalSets: Int
)
