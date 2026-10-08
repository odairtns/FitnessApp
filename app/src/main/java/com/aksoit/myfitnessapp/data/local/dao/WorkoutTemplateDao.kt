package com.aksoit.myfitnessapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aksoit.myfitnessapp.data.local.entity.PlannedSetEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutBlockEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutExerciseEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutTemplateEntity
import com.aksoit.myfitnessapp.data.local.relation.TemplateWithBlocksRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutTemplateDao {
    @Insert
    suspend fun insert(template: WorkoutTemplateEntity): Long

    @Update
    suspend fun update(template: WorkoutTemplateEntity)

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE is_archived = 0 ORDER BY updated_at_epoch_ms DESC")
    fun observeActive(): Flow<List<TemplateWithBlocksRelation>>

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getById(id: Long): TemplateWithBlocksRelation?

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE template_uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): TemplateWithBlocksRelation?

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getEntityById(id: Long): WorkoutTemplateEntity?

    @Query("UPDATE workout_templates SET is_archived = 1, updated_at_epoch_ms = :now WHERE id = :id")
    suspend fun archive(id: Long, now: Long)

    @Query("DELETE FROM workout_templates WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE template_id = :templateId")
    suspend fun countSessionsForTemplate(templateId: Long): Int

    @Query("SELECT COUNT(*) FROM workout_templates")
    suspend fun count(): Int
}

@Dao
interface WorkoutBlockDao {
    @Insert
    suspend fun insertBlock(block: WorkoutBlockEntity): Long

    @Insert
    suspend fun insertExercise(exercise: WorkoutExerciseEntity): Long

    @Insert
    suspend fun insertPlannedSets(sets: List<PlannedSetEntity>)

    @Query("DELETE FROM workout_blocks WHERE template_id = :templateId")
    suspend fun deleteBlocksForTemplate(templateId: Long)
}
