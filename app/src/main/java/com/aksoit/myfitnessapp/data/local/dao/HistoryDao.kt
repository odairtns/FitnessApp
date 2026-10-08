package com.aksoit.myfitnessapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.aksoit.myfitnessapp.data.local.entity.PersonalRecordEntity
import com.aksoit.myfitnessapp.data.local.entity.SessionSummaryRow
import com.aksoit.myfitnessapp.data.local.entity.SetFactRow
import com.aksoit.myfitnessapp.data.local.relation.SessionWithPerformanceRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    // 1. Resolução Primária: Mesmo Exercício + Mesmo Número da Série
    @Query(
        """
        SELECT ps.actual_load_kg 
        FROM performed_sets ps
        INNER JOIN exercise_performances ep ON ps.performance_id = ep.id
        INNER JOIN session_blocks sb ON ep.session_block_id = sb.id
        INNER JOIN workout_sessions ws ON sb.session_id = ws.id
        WHERE (ep.exercise_id = :exerciseId OR ep.exercise_name_snapshot = :exerciseName)
          AND ps.set_number = :setNumber
          AND ps.is_completed = 1
          AND ps.actual_load_kg IS NOT NULL
          AND ws.status IN ('COMPLETED', 'CANCELLED')
        ORDER BY ws.started_at_epoch_ms DESC, ps.logged_at_epoch_ms DESC
        LIMIT 1
        """
    )
    suspend fun getLastLoadForSet(exerciseId: Long?, exerciseName: String, setNumber: Int): Double?

    // 2. Resolução Secundária (Fallback): Última Carga Válida de Qualquer Série do Exercício
    @Query(
        """
        SELECT ps.actual_load_kg 
        FROM performed_sets ps
        INNER JOIN exercise_performances ep ON ps.performance_id = ep.id
        INNER JOIN session_blocks sb ON ep.session_block_id = sb.id
        INNER JOIN workout_sessions ws ON sb.session_id = ws.id
        WHERE (ep.exercise_id = :exerciseId OR ep.exercise_name_snapshot = :exerciseName)
          AND ps.is_completed = 1
          AND ps.actual_load_kg IS NOT NULL
          AND ws.status IN ('COMPLETED', 'CANCELLED')
        ORDER BY ws.started_at_epoch_ms DESC, ps.logged_at_epoch_ms DESC
        LIMIT 1
        """
    )
    suspend fun getLastLoadFallback(exerciseId: Long?, exerciseName: String): Double?

    @Query(
        """
        SELECT ws.id AS id, ws.template_id AS template_id, ws.template_name_snapshot AS template_name_snapshot,
               ws.protocol_snapshot AS protocol_snapshot, ws.started_at_epoch_ms AS started_at_epoch_ms,
               ws.completed_at_epoch_ms AS completed_at_epoch_ms, ws.status AS status,
               COALESCE((SELECT SUM(COALESCE(ps.actual_load_kg, 0.0) * COALESCE(ps.actual_reps, 0))
                         FROM performed_sets ps
                         INNER JOIN exercise_performances ep ON ps.performance_id = ep.id
                         INNER JOIN session_blocks sb ON ep.session_block_id = sb.id
                         WHERE sb.session_id = ws.id AND ps.is_completed = 1), 0.0) AS total_volume,
               (SELECT COUNT(*)
                FROM performed_sets ps
                INNER JOIN exercise_performances ep ON ps.performance_id = ep.id
                INNER JOIN session_blocks sb ON ep.session_block_id = sb.id
                WHERE sb.session_id = ws.id AND ps.is_completed = 1) AS total_sets
        FROM workout_sessions ws
        WHERE ws.status IN ('COMPLETED', 'CANCELLED')
        ORDER BY ws.started_at_epoch_ms DESC
        LIMIT :limit
        """
    )
    fun observeSessionSummaries(limit: Int): Flow<List<SessionSummaryRow>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionDetail(sessionId: Long): SessionWithPerformanceRelation?

    @Query(
        """
        SELECT COALESCE(SUM(COALESCE(ps.actual_load_kg, 0.0) * COALESCE(ps.actual_reps, 0)), 0.0)
        FROM performed_sets ps
        INNER JOIN exercise_performances ep ON ps.performance_id = ep.id
        INNER JOIN session_blocks sb ON ep.session_block_id = sb.id
        INNER JOIN workout_sessions ws ON sb.session_id = ws.id
        WHERE ws.started_at_epoch_ms >= :startOfWeekEpochMs
          AND ps.is_completed = 1
          AND ws.status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')
        """
    )
    fun observeTonnageSince(startOfWeekEpochMs: Long): Flow<Double>

    @Query("SELECT * FROM personal_records ORDER BY exercise_name_snapshot COLLATE NOCASE, record_type")
    fun observePersonalRecords(): Flow<List<PersonalRecordEntity>>

    @Query(
        """
        SELECT ep.exercise_id AS exercise_id, ep.exercise_name_snapshot AS exercise_name_snapshot,
               ps.actual_load_kg AS actual_load_kg, ps.actual_reps AS actual_reps,
               ws.id AS session_id, ps.logged_at_epoch_ms AS logged_at_epoch_ms
        FROM performed_sets ps
        INNER JOIN exercise_performances ep ON ps.performance_id = ep.id
        INNER JOIN session_blocks sb ON ep.session_block_id = sb.id
        INNER JOIN workout_sessions ws ON sb.session_id = ws.id
        WHERE ps.is_completed = 1
          AND ws.status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')
          AND ep.exercise_name_snapshot = :exerciseName
        ORDER BY ps.logged_at_epoch_ms ASC
        """
    )
    suspend fun getSetFactsForExercise(exerciseName: String): List<SetFactRow>

    @Query(
        """
        SELECT ep.exercise_id AS exercise_id, ep.exercise_name_snapshot AS exercise_name_snapshot,
               ps.actual_load_kg AS actual_load_kg, ps.actual_reps AS actual_reps,
               ws.id AS session_id, ps.logged_at_epoch_ms AS logged_at_epoch_ms
        FROM performed_sets ps
        INNER JOIN exercise_performances ep ON ps.performance_id = ep.id
        INNER JOIN session_blocks sb ON ep.session_block_id = sb.id
        INNER JOIN workout_sessions ws ON sb.session_id = ws.id
        WHERE ps.is_completed = 1
          AND ws.status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')
        ORDER BY ps.logged_at_epoch_ms ASC
        """
    )
    suspend fun getAllSetFacts(): List<SetFactRow>

    @Query("DELETE FROM personal_records WHERE exercise_name_snapshot = :exerciseName")
    suspend fun deletePersonalRecordsForExercise(exerciseName: String)

    @Query("DELETE FROM personal_records")
    suspend fun deleteAllPersonalRecords()

    @Insert
    suspend fun insertPersonalRecords(records: List<PersonalRecordEntity>)
}
