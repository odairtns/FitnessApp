package com.aksoit.myfitnessapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aksoit.myfitnessapp.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insert(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoringConflicts(exercises: List<ExerciseEntity>): List<Long>

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises WHERE is_archived = 0 ORDER BY name COLLATE NOCASE")
    fun observeActive(): Flow<List<ExerciseEntity>>

    @Query(
        """
        SELECT * FROM exercises
        WHERE is_archived = 0 AND name LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE
        """
    )
    suspend fun search(query: String): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE stable_key = :key LIMIT 1")
    suspend fun getByStableKey(key: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE LOWER(TRIM(name)) = :normalizedName ORDER BY is_archived ASC, is_custom ASC LIMIT 1")
    suspend fun findByNormalizedName(normalizedName: String): ExerciseEntity?

    @Query("UPDATE exercises SET is_archived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
}
