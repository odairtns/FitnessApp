package com.aksoit.myfitnessapp.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.aksoit.myfitnessapp.data.local.entity.ExerciseEntity
import com.aksoit.myfitnessapp.data.local.entity.PlannedSetEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutBlockEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutExerciseEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutTemplateEntity

data class WorkoutExerciseWithSets(
    @Embedded val workoutExercise: WorkoutExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "workout_exercise_id")
    val sets: List<PlannedSetEntity>,
    @Relation(parentColumn = "exercise_id", entityColumn = "id")
    val exercise: ExerciseEntity?
)

data class BlockWithExercises(
    @Embedded val block: WorkoutBlockEntity,
    @Relation(entity = WorkoutExerciseEntity::class, parentColumn = "id", entityColumn = "block_id")
    val exercises: List<WorkoutExerciseWithSets>
)

data class TemplateWithBlocksRelation(
    @Embedded val template: WorkoutTemplateEntity,
    @Relation(entity = WorkoutBlockEntity::class, parentColumn = "id", entityColumn = "template_id")
    val blocks: List<BlockWithExercises>
)
