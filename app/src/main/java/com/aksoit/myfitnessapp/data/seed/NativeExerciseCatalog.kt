package com.aksoit.myfitnessapp.data.seed

import com.aksoit.myfitnessapp.domain.model.EquipmentType
import com.aksoit.myfitnessapp.domain.model.EquipmentType.BARBELL
import com.aksoit.myfitnessapp.domain.model.EquipmentType.BIKE
import com.aksoit.myfitnessapp.domain.model.EquipmentType.BODYWEIGHT
import com.aksoit.myfitnessapp.domain.model.EquipmentType.CABLE
import com.aksoit.myfitnessapp.domain.model.EquipmentType.DUMBBELL
import com.aksoit.myfitnessapp.domain.model.EquipmentType.JUMP_ROPE
import com.aksoit.myfitnessapp.domain.model.EquipmentType.KETTLEBELL
import com.aksoit.myfitnessapp.domain.model.EquipmentType.MACHINE
import com.aksoit.myfitnessapp.domain.model.EquipmentType.ROWER
import com.aksoit.myfitnessapp.domain.model.EquipmentType.TREADMILL
import com.aksoit.myfitnessapp.domain.model.Exercise
import com.aksoit.myfitnessapp.domain.model.ExerciseCategory
import com.aksoit.myfitnessapp.domain.model.ExerciseCategory.CARDIO
import com.aksoit.myfitnessapp.domain.model.ExerciseCategory.CONDITIONING
import com.aksoit.myfitnessapp.domain.model.ExerciseCategory.MOBILITY
import com.aksoit.myfitnessapp.domain.model.ExerciseCategory.PLYOMETRIC
import com.aksoit.myfitnessapp.domain.model.ExerciseCategory.STRENGTH
import com.aksoit.myfitnessapp.domain.model.MovementPattern
import com.aksoit.myfitnessapp.domain.model.MovementPattern.HINGE
import com.aksoit.myfitnessapp.domain.model.MovementPattern.ISOMETRIC
import com.aksoit.myfitnessapp.domain.model.MovementPattern.LOCOMOTION
import com.aksoit.myfitnessapp.domain.model.MovementPattern.LUNGE
import com.aksoit.myfitnessapp.domain.model.MovementPattern.PULL
import com.aksoit.myfitnessapp.domain.model.MovementPattern.PUSH
import com.aksoit.myfitnessapp.domain.model.MovementPattern.ROTATION
import com.aksoit.myfitnessapp.domain.model.MovementPattern.SQUAT
import com.aksoit.myfitnessapp.domain.model.MuscleGroup
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.BACK
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.BICEPS
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.CALVES
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.CARDIOVASCULAR
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.CHEST
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.CORE
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.FULL_BODY
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.GLUTES
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.HAMSTRINGS
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.QUADRICEPS
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.SHOULDERS
import com.aksoit.myfitnessapp.domain.model.MuscleGroup.TRICEPS

/** Catálogo nativo com chaves estáveis (usadas no Estágio 1 de importação XML). */
object NativeExerciseCatalog {

    private fun ex(
        key: String,
        name: String,
        category: ExerciseCategory,
        muscle: MuscleGroup,
        equipment: EquipmentType,
        pattern: MovementPattern?,
        rest: Int = 90,
        bodyweight: Boolean = equipment == BODYWEIGHT
    ) = Exercise(
        stableKey = key,
        name = name,
        category = category,
        muscleGroup = muscle,
        equipment = equipment,
        movementPattern = pattern,
        defaultRestSeconds = rest,
        isBodyweight = bodyweight,
        isCustom = false
    )

    val exercises: List<Exercise> = listOf(
        // Peito
        ex("barbell-bench-press", "Supino Reto com Barra", STRENGTH, CHEST, BARBELL, PUSH, 120),
        ex("incline-barbell-bench-press", "Supino Inclinado com Barra", STRENGTH, CHEST, BARBELL, PUSH, 120),
        ex("dumbbell-bench-press", "Supino Reto com Halteres", STRENGTH, CHEST, DUMBBELL, PUSH),
        ex("incline-dumbbell-bench-press", "Supino Inclinado com Halteres", STRENGTH, CHEST, DUMBBELL, PUSH),
        ex("dumbbell-fly", "Crucifixo com Halteres", STRENGTH, CHEST, DUMBBELL, PUSH, 60),
        ex("cable-crossover", "Crossover na Polia", STRENGTH, CHEST, CABLE, PUSH, 60),
        ex("push-up", "Flexão de Braço", STRENGTH, CHEST, BODYWEIGHT, PUSH, 60),
        // Costas
        ex("barbell-deadlift", "Levantamento Terra", STRENGTH, BACK, BARBELL, HINGE, 180),
        ex("pull-up", "Barra Fixa", STRENGTH, BACK, BODYWEIGHT, PULL, 120),
        ex("lat-pulldown", "Puxada Frontal na Polia", STRENGTH, BACK, CABLE, PULL),
        ex("barbell-row", "Remada Curvada com Barra", STRENGTH, BACK, BARBELL, PULL, 120),
        ex("dumbbell-row", "Remada Unilateral com Halter", STRENGTH, BACK, DUMBBELL, PULL),
        ex("seated-cable-row", "Remada Baixa na Polia", STRENGTH, BACK, CABLE, PULL),
        // Ombros
        ex("overhead-press", "Desenvolvimento Militar com Barra", STRENGTH, SHOULDERS, BARBELL, PUSH, 120),
        ex("dumbbell-shoulder-press", "Desenvolvimento com Halteres", STRENGTH, SHOULDERS, DUMBBELL, PUSH),
        ex("dumbbell-lateral-raise", "Elevação Lateral com Halteres", STRENGTH, SHOULDERS, DUMBBELL, PUSH, 60),
        ex("face-pull", "Face Pull na Polia", STRENGTH, SHOULDERS, CABLE, PULL, 60),
        // Braços
        ex("barbell-curl", "Rosca Direta com Barra", STRENGTH, BICEPS, BARBELL, PULL, 60),
        ex("dumbbell-hammer-curl", "Rosca Martelo", STRENGTH, BICEPS, DUMBBELL, PULL, 60),
        ex("triceps-pushdown", "Tríceps na Polia", STRENGTH, TRICEPS, CABLE, PUSH, 60),
        ex("skull-crusher", "Tríceps Testa", STRENGTH, TRICEPS, BARBELL, PUSH, 60),
        ex("bench-dip", "Mergulho no Banco", STRENGTH, TRICEPS, BODYWEIGHT, PUSH, 60),
        // Pernas
        ex("barbell-back-squat", "Agachamento Livre com Barra", STRENGTH, QUADRICEPS, BARBELL, SQUAT, 180),
        ex("front-squat", "Agachamento Frontal", STRENGTH, QUADRICEPS, BARBELL, SQUAT, 150),
        ex("leg-press", "Leg Press 45°", STRENGTH, QUADRICEPS, MACHINE, SQUAT, 120),
        ex("leg-extension", "Cadeira Extensora", STRENGTH, QUADRICEPS, MACHINE, SQUAT, 60),
        ex("leg-curl", "Mesa Flexora", STRENGTH, HAMSTRINGS, MACHINE, HINGE, 60),
        ex("romanian-deadlift", "Stiff / Terra Romeno", STRENGTH, HAMSTRINGS, BARBELL, HINGE, 120),
        ex("walking-lunge", "Avanço Caminhando", STRENGTH, QUADRICEPS, DUMBBELL, LUNGE),
        ex("hip-thrust", "Elevação Pélvica com Barra", STRENGTH, GLUTES, BARBELL, HINGE),
        ex("standing-calf-raise", "Panturrilha em Pé", STRENGTH, CALVES, MACHINE, null, 60),
        // Core
        ex("plank", "Prancha Abdominal", STRENGTH, CORE, BODYWEIGHT, ISOMETRIC, 45),
        ex("hanging-leg-raise", "Elevação de Pernas na Barra", STRENGTH, CORE, BODYWEIGHT, null, 60),
        ex("russian-twist", "Giro Russo", STRENGTH, CORE, BODYWEIGHT, ROTATION, 45),
        // Cross training / condicionamento
        ex("burpee", "Burpee", CONDITIONING, FULL_BODY, BODYWEIGHT, null, 60),
        ex("air-squat", "Agachamento Livre (Peso Corporal)", CONDITIONING, QUADRICEPS, BODYWEIGHT, SQUAT, 60),
        ex("kettlebell-swing", "Kettlebell Swing", CONDITIONING, FULL_BODY, KETTLEBELL, HINGE, 60),
        ex("thruster", "Thruster", CONDITIONING, FULL_BODY, BARBELL, SQUAT, 90),
        ex("wall-ball", "Wall Ball", CONDITIONING, FULL_BODY, EquipmentType.OTHER, SQUAT, 60),
        ex("box-jump", "Salto na Caixa", PLYOMETRIC, QUADRICEPS, EquipmentType.OTHER, SQUAT, 60),
        ex("double-under", "Pular Corda (Double Under)", CONDITIONING, CALVES, JUMP_ROPE, LOCOMOTION, 60),
        ex("sit-up", "Abdominal Sit-up", CONDITIONING, CORE, BODYWEIGHT, null, 45),
        // Cardio (HIIT — apenas metas, sem medição)
        ex("treadmill-run", "Corrida na Esteira", CARDIO, CARDIOVASCULAR, TREADMILL, LOCOMOTION, 60, bodyweight = false),
        ex("stationary-bike", "Bicicleta Ergométrica", CARDIO, CARDIOVASCULAR, BIKE, LOCOMOTION, 60, bodyweight = false),
        ex("rowing-machine", "Remo Ergométrico", CARDIO, CARDIOVASCULAR, ROWER, PULL, 60, bodyweight = false),
        // Mobilidade / alongamento
        ex("hamstring-stretch", "Alongamento de Posterior de Coxa", MOBILITY, HAMSTRINGS, BODYWEIGHT, ISOMETRIC, 15),
        ex("quad-stretch", "Alongamento de Quadríceps em Pé", MOBILITY, QUADRICEPS, BODYWEIGHT, ISOMETRIC, 15),
        ex("hip-flexor-stretch", "Alongamento de Flexores do Quadril", MOBILITY, GLUTES, BODYWEIGHT, ISOMETRIC, 15),
        ex("shoulder-cross-stretch", "Alongamento de Ombro Cruzado", MOBILITY, SHOULDERS, BODYWEIGHT, ISOMETRIC, 15),
        ex("child-pose", "Postura da Criança", MOBILITY, BACK, BODYWEIGHT, ISOMETRIC, 15),
        ex("pigeon-stretch", "Alongamento do Pombo", MOBILITY, GLUTES, BODYWEIGHT, ISOMETRIC, 15)
    )
}
