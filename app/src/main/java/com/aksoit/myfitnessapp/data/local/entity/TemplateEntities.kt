package com.aksoit.myfitnessapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_templates",
    indices = [Index(value = ["template_uuid"], unique = true)]
)
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "template_uuid") val templateUuid: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "modality") val modality: String,
    @ColumnInfo(name = "protocol") val protocol: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "estimated_duration_seconds") val estimatedDurationSeconds: Int?,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMs: Long,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long
)

@Entity(
    tableName = "workout_blocks",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["template_id"])]
)
data class WorkoutBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "template_id") val templateId: Long,
    @ColumnInfo(name = "block_type") val blockType: String,
    @ColumnInfo(name = "position") val position: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "rounds") val rounds: Int,
    // Coluna universal de trabalho em segundos (HIIT work, AMRAP duration, EMOM interval, For Time cap)
    @ColumnInfo(name = "work_duration_seconds") val workDurationSeconds: Int?,
    // Coluna universal de descanso
    @ColumnInfo(name = "rest_duration_seconds") val restDurationSeconds: Int?,
    // JSON tipado estrito via RoomTypeConverters (ProtocolConfig)
    @ColumnInfo(name = "protocol_config_json") val protocolConfigJson: String?
)

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutBlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["block_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["block_id"]), Index(value = ["exercise_id"])]
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "block_id") val blockId: Long,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long?,
    @ColumnInfo(name = "exercise_name_custom") val exerciseNameCustom: String?,
    @ColumnInfo(name = "position") val position: Int,
    @ColumnInfo(name = "group_id") val groupId: String?,
    @ColumnInfo(name = "group_type") val groupType: String
)

@Entity(
    tableName = "planned_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workout_exercise_id"])]
)
data class PlannedSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "workout_exercise_id") val workoutExerciseId: Long,
    @ColumnInfo(name = "set_number") val setNumber: Int,
    @ColumnInfo(name = "target_reps") val targetReps: Int?,
    @ColumnInfo(name = "target_load_kg") val targetLoadKg: Double?, // Canônico em kg
    @ColumnInfo(name = "target_duration_seconds") val targetDurationSeconds: Int?,
    @ColumnInfo(name = "rest_seconds") val restSeconds: Int?,
    @ColumnInfo(name = "side_mode") val sideMode: String
)
