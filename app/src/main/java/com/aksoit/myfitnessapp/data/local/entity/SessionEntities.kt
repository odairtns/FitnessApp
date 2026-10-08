package com.aksoit.myfitnessapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["template_id"]), Index(value = ["started_at_epoch_ms"])]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "template_id") val templateId: Long?,
    @ColumnInfo(name = "template_name_snapshot") val templateNameSnapshot: String,
    @ColumnInfo(name = "modality_snapshot") val modalitySnapshot: String,
    @ColumnInfo(name = "protocol_snapshot") val protocolSnapshot: String,
    @ColumnInfo(name = "started_at_epoch_ms") val startedAtEpochMs: Long,
    @ColumnInfo(name = "completed_at_epoch_ms") val completedAtEpochMs: Long?,
    @ColumnInfo(name = "status") val status: String, // DRAFT / IN_PROGRESS / COMPLETED / CANCELLED
    @ColumnInfo(name = "notes") val notes: String
)

@Entity(
    tableName = "session_blocks",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_id"])]
)
data class SessionBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "source_block_id") val sourceBlockId: Long?,
    @ColumnInfo(name = "block_name_snapshot") val blockNameSnapshot: String,
    @ColumnInfo(name = "block_type_snapshot") val blockTypeSnapshot: String,
    @ColumnInfo(name = "position") val position: Int,
    @ColumnInfo(name = "status") val status: String
)

@Entity(
    tableName = "exercise_performances",
    foreignKeys = [
        ForeignKey(
            entity = SessionBlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_block_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["session_block_id"]), Index(value = ["exercise_id"])]
)
data class ExercisePerformanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_block_id") val sessionBlockId: Long,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long?,
    @ColumnInfo(name = "exercise_name_snapshot") val exerciseNameSnapshot: String,
    @ColumnInfo(name = "position") val position: Int
)

@Entity(
    tableName = "performed_sets",
    foreignKeys = [
        ForeignKey(
            entity = ExercisePerformanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["performance_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["performance_id"])]
)
data class PerformedSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "performance_id") val performanceId: Long,
    @ColumnInfo(name = "set_number") val setNumber: Int,
    @ColumnInfo(name = "actual_load_kg") val actualLoadKg: Double?, // Estritamente em kg
    @ColumnInfo(name = "actual_reps") val actualReps: Int?,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Int?,
    @ColumnInfo(name = "side") val side: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    @ColumnInfo(name = "logged_at_epoch_ms") val loggedAtEpochMs: Long,
    @ColumnInfo(name = "notes") val notes: String
)
