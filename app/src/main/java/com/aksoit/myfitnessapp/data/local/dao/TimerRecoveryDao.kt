package com.aksoit.myfitnessapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aksoit.myfitnessapp.data.local.entity.TimerRecoveryEntity

@Dao
interface TimerRecoveryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TimerRecoveryEntity)

    @Query("SELECT * FROM timer_recovery WHERE session_id = :sessionId")
    suspend fun get(sessionId: Long): TimerRecoveryEntity?

    @Query("DELETE FROM timer_recovery WHERE session_id = :sessionId")
    suspend fun delete(sessionId: Long)
}
