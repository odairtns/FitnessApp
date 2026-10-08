package com.aksoit.myfitnessapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aksoit.myfitnessapp.data.local.entity.AmrapResultEntity
import com.aksoit.myfitnessapp.data.local.entity.EmomIntervalResultEntity
import com.aksoit.myfitnessapp.data.local.entity.ExercisePerformanceEntity
import com.aksoit.myfitnessapp.data.local.entity.ForTimeRemainingItemEntity
import com.aksoit.myfitnessapp.data.local.entity.ForTimeResultEntity
import com.aksoit.myfitnessapp.data.local.entity.PerformedSetEntity
import com.aksoit.myfitnessapp.data.local.entity.SessionBlockEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutSessionEntity

@Dao
interface WorkoutSessionDao {
    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert
    suspend fun insertSessionBlock(block: SessionBlockEntity): Long

    @Insert
    suspend fun insertPerformance(performance: ExercisePerformanceEntity): Long

    @Insert
    suspend fun insertPerformedSet(set: PerformedSetEntity): Long

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: Long): WorkoutSessionEntity?

    @Query("UPDATE workout_sessions SET status = 'IN_PROGRESS' WHERE id = :sessionId AND status = 'DRAFT'")
    suspend fun promoteToInProgress(sessionId: Long)

    @Query("SELECT id FROM session_blocks WHERE session_id = :sessionId AND position = :position LIMIT 1")
    suspend fun getSessionBlockId(sessionId: Long, position: Int): Long?

    @Query("SELECT id FROM exercise_performances WHERE session_block_id = :sessionBlockId AND position = :position LIMIT 1")
    suspend fun getPerformanceId(sessionBlockId: Long, position: Int): Long?

    @Query("SELECT exercise_name_snapshot FROM exercise_performances WHERE id = :performanceId")
    suspend fun getPerformanceNameSnapshot(performanceId: Long): String?

    @Query("DELETE FROM amrap_results WHERE session_block_id = :sessionBlockId")
    suspend fun deleteAmrapForBlock(sessionBlockId: Long)

    @Insert
    suspend fun insertAmrapResult(result: AmrapResultEntity): Long

    @Query("DELETE FROM for_time_results WHERE session_block_id = :sessionBlockId")
    suspend fun deleteForTimeForBlock(sessionBlockId: Long)

    @Insert
    suspend fun insertForTimeResult(result: ForTimeResultEntity): Long

    @Insert
    suspend fun insertRemainingItems(items: List<ForTimeRemainingItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmomResult(result: EmomIntervalResultEntity): Long

    @Query(
        """
        UPDATE workout_sessions
        SET status = 'COMPLETED', completed_at_epoch_ms = :completedAt, notes = :notes
        WHERE id = :sessionId
        """
    )
    suspend fun markAsCompleted(sessionId: Long, completedAt: Long, notes: String)

    // Cancelamento: Mantém registro para preservar dados do histórico parcial
    @Query(
        """
        UPDATE workout_sessions 
        SET status = 'CANCELLED', completed_at_epoch_ms = :completedAt 
        WHERE id = :sessionId
        """
    )
    suspend fun markAsCancelled(sessionId: Long, completedAt: Long)

    // Descarte Físico: DELETE CASCADE completo de toda a árvore
    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun discardSessionPhysically(sessionId: Long)

    @Query(
        """
        SELECT sb.id FROM session_blocks sb
        WHERE sb.session_id = :sessionId AND (
            EXISTS (SELECT 1 FROM exercise_performances ep
                    INNER JOIN performed_sets ps ON ps.performance_id = ep.id
                    WHERE ep.session_block_id = sb.id)
            OR EXISTS (SELECT 1 FROM amrap_results ar WHERE ar.session_block_id = sb.id)
            OR EXISTS (SELECT 1 FROM for_time_results fr WHERE fr.session_block_id = sb.id)
            OR EXISTS (SELECT 1 FROM emom_interval_results er WHERE er.session_block_id = sb.id)
        )
        """
    )
    suspend fun getBlockIdsWithFacts(sessionId: Long): List<Long>

    @Query("UPDATE session_blocks SET status = :status WHERE session_id = :sessionId")
    suspend fun setAllBlockStatuses(sessionId: Long, status: String)

    @Query("UPDATE session_blocks SET status = :status WHERE id IN (:blockIds)")
    suspend fun setBlockStatuses(blockIds: List<Long>, status: String)

    @Query("SELECT * FROM workout_sessions WHERE status = 'IN_PROGRESS' ORDER BY started_at_epoch_ms DESC LIMIT 1")
    suspend fun getLatestInProgress(): WorkoutSessionEntity?

    @Query("DELETE FROM workout_sessions WHERE status = 'DRAFT'")
    suspend fun deleteDrafts()

    @Query("SELECT COUNT(*) FROM workout_sessions")
    suspend fun countAllSessions(): Int
}
