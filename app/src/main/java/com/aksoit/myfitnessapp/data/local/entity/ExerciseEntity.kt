package com.aksoit.myfitnessapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["stable_key"], unique = true),
        Index(value = ["name"]),
        Index(value = ["category"])
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "stable_key") val stableKey: String?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "muscle_group") val muscleGroup: String,
    @ColumnInfo(name = "equipment") val equipment: String,
    @ColumnInfo(name = "movement_pattern") val movementPattern: String?,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "default_rest_seconds") val defaultRestSeconds: Int,
    @ColumnInfo(name = "is_bodyweight") val isBodyweight: Boolean,
    @ColumnInfo(name = "is_custom") val isCustom: Boolean,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false
)
