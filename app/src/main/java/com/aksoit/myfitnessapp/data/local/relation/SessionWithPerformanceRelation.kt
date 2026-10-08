package com.aksoit.myfitnessapp.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.aksoit.myfitnessapp.data.local.entity.AmrapResultEntity
import com.aksoit.myfitnessapp.data.local.entity.EmomIntervalResultEntity
import com.aksoit.myfitnessapp.data.local.entity.ExercisePerformanceEntity
import com.aksoit.myfitnessapp.data.local.entity.ForTimeRemainingItemEntity
import com.aksoit.myfitnessapp.data.local.entity.ForTimeResultEntity
import com.aksoit.myfitnessapp.data.local.entity.PerformedSetEntity
import com.aksoit.myfitnessapp.data.local.entity.SessionBlockEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutSessionEntity

data class PerformanceWithSets(
    @Embedded val performance: ExercisePerformanceEntity,
    @Relation(parentColumn = "id", entityColumn = "performance_id")
    val sets: List<PerformedSetEntity>
)

data class ForTimeResultWithItems(
    @Embedded val result: ForTimeResultEntity,
    @Relation(parentColumn = "id", entityColumn = "result_id")
    val items: List<ForTimeRemainingItemEntity>
)

data class SessionBlockWithDetails(
    @Embedded val block: SessionBlockEntity,
    @Relation(entity = ExercisePerformanceEntity::class, parentColumn = "id", entityColumn = "session_block_id")
    val performances: List<PerformanceWithSets>,
    @Relation(parentColumn = "id", entityColumn = "session_block_id")
    val amrapResults: List<AmrapResultEntity>,
    @Relation(entity = ForTimeResultEntity::class, parentColumn = "id", entityColumn = "session_block_id")
    val forTimeResults: List<ForTimeResultWithItems>,
    @Relation(parentColumn = "id", entityColumn = "session_block_id")
    val emomResults: List<EmomIntervalResultEntity>
)

data class SessionWithPerformanceRelation(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(entity = SessionBlockEntity::class, parentColumn = "id", entityColumn = "session_id")
    val blocks: List<SessionBlockWithDetails>
)
