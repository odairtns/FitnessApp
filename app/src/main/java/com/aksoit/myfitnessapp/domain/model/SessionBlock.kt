package com.aksoit.myfitnessapp.domain.model

data class SessionBlock(
    val id: Long = 0,
    val sessionId: Long = 0,
    val sourceBlockId: Long? = null,
    val blockNameSnapshot: String,
    val blockTypeSnapshot: BlockType,
    val position: Int,
    val status: BlockExecutionStatus = BlockExecutionStatus.PENDING,
    val performances: List<ExercisePerformance> = emptyList(),
    // Resultados de protocolos contínuos
    val amrapResult: AmrapResult? = null,
    val forTimeResult: ForTimeResult? = null,
    val emomResults: List<EmomIntervalResult> = emptyList()
)
