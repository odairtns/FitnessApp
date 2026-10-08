package com.aksoit.myfitnessapp.domain.execution

import com.aksoit.myfitnessapp.domain.model.BlockType
import com.aksoit.myfitnessapp.domain.model.WorkoutModality
import com.aksoit.myfitnessapp.domain.model.WorkoutProtocol

/**
 * Representação imutável e linear de execução, pré-calculada antes do treino iniciar.
 */
data class ExecutionPlan(
    val templateId: Long?,
    val templateName: String,
    val modality: WorkoutModality,
    val protocol: WorkoutProtocol,
    val blocks: List<PlanBlock>,
    val steps: List<ExecutionStep>
) {
    init {
        require(steps.isNotEmpty()) { "Um ExecutionPlan precisa de pelo menos um passo." }
        require(steps.map { it.id }.toSet().size == steps.size) { "IDs de passos devem ser únicos." }
    }

    fun getStep(stepId: String): ExecutionStep? = steps.find { it.id == stepId }
    fun indexOf(stepId: String): Int = steps.indexOfFirst { it.id == stepId }
    val initialStep: ExecutionStep get() = steps.first()
}

/** Snapshot de bloco usado para criar a árvore da sessão (Regra 2 — independência histórica). */
data class PlanBlock(
    val index: Int,
    val sourceBlockId: Long?,
    val name: String,
    val blockType: BlockType,
    val exercises: List<PlanExercise>
)

data class PlanExercise(
    val position: Int,
    val exerciseId: Long?,
    val name: String
)
