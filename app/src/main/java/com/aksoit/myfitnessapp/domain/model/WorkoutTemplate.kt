package com.aksoit.myfitnessapp.domain.model

data class WorkoutTemplate(
    val id: Long = 0,
    val templateUuid: String, // UUID v4 único e exportável via XML
    val name: String,
    val modality: WorkoutModality,
    val protocol: WorkoutProtocol,
    val description: String = "",
    val estimatedDurationSeconds: Int? = null,
    val blocks: List<WorkoutBlock> = emptyList(),
    val isArchived: Boolean = false,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
