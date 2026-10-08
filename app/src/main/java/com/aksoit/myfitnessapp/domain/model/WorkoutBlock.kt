package com.aksoit.myfitnessapp.domain.model

import kotlinx.serialization.Serializable

data class WorkoutBlock(
    val id: Long = 0,
    val templateId: Long = 0,
    val blockType: BlockType,
    val position: Int,
    val name: String,
    val rounds: Int = 1,
    // Coluna universal de tempo de execução/trabalho em segundos
    // (HIIT work, AMRAP duration, EMOM interval, For Time cap)
    val workDurationSeconds: Int? = null,
    // Coluna universal de descanso entre séries/estações
    val restDurationSeconds: Int? = null,
    // Configuração tipada específica do protocolo (ex: alvos HIIT, regras de escala)
    val protocolConfig: ProtocolConfig? = null,
    val exercises: List<WorkoutExercise> = emptyList()
) {
    init {
        require(rounds >= 1) { "rounds deve ser >= 1." }
        require(workDurationSeconds == null || workDurationSeconds >= 0) { "workDurationSeconds inválido." }
        require(restDurationSeconds == null || restDurationSeconds >= 0) { "restDurationSeconds inválido." }
    }
}

data class WorkoutExercise(
    val id: Long = 0,
    val blockId: Long = 0,
    val exerciseId: Long?,
    val exerciseNameCustom: String? = null,
    val position: Int,
    val groupId: String? = null,
    val groupType: GroupType = GroupType.NONE,
    val plannedSets: List<PlannedSet> = emptyList(),
    /** Nome resolvido a partir do catálogo (preenchido pelo repositório, não persistido). */
    val resolvedName: String? = null,
    /** Chave estável do catálogo (preenchida pelo repositório, usada na exportação XML). */
    val resolvedStableKey: String? = null
) {
    val displayName: String get() = resolvedName ?: exerciseNameCustom ?: "Exercício"
}

/**
 * Configuração tipada por protocolo, persistida como JSON estrito (Spec 03 §2.1).
 */
@Serializable
data class ProtocolConfig(
    val warmupSeconds: Int? = null,
    val cooldownSeconds: Int? = null,
    val switchSideSeconds: Int? = null,
    val scalingType: ScalingType? = null,
    val workTargets: List<HiitTarget> = emptyList(),
    val restTargets: List<HiitTarget> = emptyList()
)
