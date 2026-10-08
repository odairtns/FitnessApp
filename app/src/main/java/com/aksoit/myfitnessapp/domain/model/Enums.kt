package com.aksoit.myfitnessapp.domain.model

/**
 * Vocabulário controlado canônico do domínio (Spec 02 §2).
 */

enum class SessionStatus {
    DRAFT,          // Criada ao abrir o player; não confirmou evidência factual
    IN_PROGRESS,    // Converteu-se em treino ativo ao completar primeira série/iniciar relógio
    COMPLETED,      // Treino concluído normalmente pelo atleta
    CANCELLED       // Treino interrompido voluntariamente; preserva histórico parcial
    // Nota: DISCARDED não é um status persistido, é uma ação de DELETE CASCADE físico
}

enum class ExecutionPlayerState {
    READY,
    RUNNING,
    PAUSED,
    COMPLETING,
    COMPLETED,
    CANCELLED,
    RECOVERY_REQUIRED
}

enum class BlockExecutionStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED
}

enum class GroupType {
    NONE,
    SUPERSET // Único tipo de agrupamento na V1
}

enum class EmomIntervalStatus {
    COMPLETED,
    PARTIAL,
    SKIPPED,
    NOT_LOGGED
}

enum class ForTimeStatus {
    COMPLETED,
    CAP_REACHED
}

enum class ScalingType {
    RX,
    SCALED,
    CUSTOM
}

enum class SideMode {
    BILATERAL,
    ALTERNATING,
    LEFT,
    RIGHT,
    NONE
}

enum class Side {
    BILATERAL,
    LEFT,
    RIGHT,
    NONE
}

enum class WorkoutModality {
    STRENGTH,
    CARDIO,
    CROSS_TRAINING,
    MOBILITY
}

enum class WorkoutProtocol {
    STANDARD_STRENGTH,
    HYPERTROPHY,
    HIIT_INTERVALS,
    AMRAP,
    EMOM,
    FOR_TIME,
    STRETCH;

    val isStrength: Boolean get() = this == STANDARD_STRENGTH || this == HYPERTROPHY

    /** Modalidade natural sugerida para o protocolo. */
    val defaultModality: WorkoutModality
        get() = when (this) {
            STANDARD_STRENGTH, HYPERTROPHY -> WorkoutModality.STRENGTH
            HIIT_INTERVALS -> WorkoutModality.CARDIO
            AMRAP, EMOM, FOR_TIME -> WorkoutModality.CROSS_TRAINING
            STRETCH -> WorkoutModality.MOBILITY
        }
}

enum class BlockType {
    WARMUP,
    WORK,
    SUPERSET,
    CIRCUIT,
    COOLDOWN
}

enum class ExerciseCategory {
    STRENGTH,
    CARDIO,
    MOBILITY,
    PLYOMETRIC,
    OLYMPIC,
    CONDITIONING
}

enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    QUADRICEPS,
    HAMSTRINGS,
    GLUTES,
    CALVES,
    CORE,
    FULL_BODY,
    CARDIOVASCULAR
}

enum class EquipmentType {
    BARBELL,
    DUMBBELL,
    MACHINE,
    CABLE,
    KETTLEBELL,
    BODYWEIGHT,
    BAND,
    TREADMILL,
    BIKE,
    ROWER,
    JUMP_ROPE,
    OTHER
}

enum class MovementPattern {
    PUSH,
    PULL,
    SQUAT,
    HINGE,
    LUNGE,
    CARRY,
    ROTATION,
    ISOMETRIC,
    LOCOMOTION
}

enum class PersonalRecordType {
    MAX_LOAD,        // Maior carga registrada (kg)
    ESTIMATED_1RM,   // 1RM estimado (Epley) em kg
    MAX_REPS         // Maior número de repetições numa série
}
