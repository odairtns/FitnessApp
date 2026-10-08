package com.aksoit.myfitnessapp.domain.execution

import com.aksoit.myfitnessapp.domain.model.HiitTarget
import com.aksoit.myfitnessapp.domain.model.ScalingType
import com.aksoit.myfitnessapp.domain.model.Side

data class ExecutionStep(
    val id: String,                         // Chave determinística (ex: "b0_e1_s1_work")
    val blockIndex: Int,
    val blockName: String,
    val stepType: ExecutionStepType,
    val exerciseId: Long? = null,
    val exerciseName: String = "",
    val exercisePosition: Int? = null,      // Posição do exercício no bloco (vínculo com o snapshot)
    val setNumber: Int? = null,
    val totalSets: Int? = null,
    val targetLoadKg: Double? = null,       // Em kg
    val targetReps: Int? = null,
    val durationSeconds: Int? = null,       // Duração para timers de descanso ou fases HIIT / cap
    val side: Side = Side.NONE,             // Lateralidade do passo
    val hiitTargets: List<HiitTarget> = emptyList(),
    val roundNumber: Int? = null,           // Round HIIT / intervalo EMOM
    val totalRounds: Int? = null,
    val circuitItems: List<CircuitItem> = emptyList(), // Itens de AMRAP / For Time / EMOM
    val scalingType: ScalingType? = null,
    val nextStepId: String? = null
) {
    /** Passos que rodam um relógio automaticamente. */
    val isTimed: Boolean
        get() = when (stepType) {
            ExecutionStepType.SET_WORK,
            ExecutionStepType.RESULT_ENTRY,
            ExecutionStepType.COMPLETE -> false
            ExecutionStepType.FOR_TIME_CLOCK -> true
            else -> (durationSeconds ?: 0) > 0
        }
}

/** Item de circuito (AMRAP, For Time, EMOM). */
data class CircuitItem(
    val exerciseId: Long?,
    val exerciseName: String,
    val position: Int,
    val targetReps: Int?,
    val targetLoadKg: Double?
)

enum class ExecutionStepType {
    PREPARE,
    SET_WORK,           // Musculação / Repetições
    REST_SET,           // Descanso entre séries
    REST_BLOCK,         // Descanso entre blocos
    HIIT_WARMUP,
    HIIT_WORK,
    HIIT_REST,
    HIIT_COOLDOWN,
    AMRAP_CLOCK,        // Bloco AMRAP contínuo
    EMOM_INTERVAL,      // Intervalo de 1 minuto EMOM
    FOR_TIME_CLOCK,     // Bloco For Time contra o relógio
    STRETCH_HOLD,       // Sustentação de alongamento
    SWITCH_SIDE_REST,   // Troca de lado (3 a 5 segundos)
    RESULT_ENTRY,       // Tela de registro de rounds/reps ou saldo
    COMPLETE            // Conclusão
}
