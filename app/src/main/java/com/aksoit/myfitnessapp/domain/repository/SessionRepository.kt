package com.aksoit.myfitnessapp.domain.repository

import com.aksoit.myfitnessapp.domain.execution.ExecutionPlan
import com.aksoit.myfitnessapp.domain.model.AmrapResult
import com.aksoit.myfitnessapp.domain.model.EmomIntervalResult
import com.aksoit.myfitnessapp.domain.model.ForTimeResult
import com.aksoit.myfitnessapp.domain.model.PerformedSet
import com.aksoit.myfitnessapp.domain.model.RecoverableSessionData
import com.aksoit.myfitnessapp.domain.model.RecoveryCheckpoint

interface SessionRepository {
    /** Cria sessão DRAFT com snapshots de blocos e exercícios. */
    suspend fun createDraft(plan: ExecutionPlan, startedAtEpochMs: Long): Long
    suspend fun promoteToInProgress(sessionId: Long)

    /** Persiste imediatamente um fato de série (Spec 04 §6.2). */
    suspend fun recordPerformedSet(sessionId: Long, blockIndex: Int, exercisePosition: Int, set: PerformedSet): Long
    suspend fun saveAmrapResult(sessionId: Long, blockIndex: Int, result: AmrapResult)
    suspend fun saveForTimeResult(sessionId: Long, blockIndex: Int, result: ForTimeResult)
    suspend fun saveEmomIntervalResult(sessionId: Long, blockIndex: Int, result: EmomIntervalResult)

    suspend fun completeSession(sessionId: Long, completedAtEpochMs: Long, notes: String)
    suspend fun cancelSession(sessionId: Long, cancelledAtEpochMs: Long)
    suspend fun discardSession(sessionId: Long) // DELETE CASCADE físico

    suspend fun getRecoverableSession(): RecoverableSessionData?
    suspend fun saveRecoveryCheckpoint(checkpoint: RecoveryCheckpoint)
    suspend fun clearRecoveryState(sessionId: Long)

    /** Remove rascunhos órfãos (DRAFT sem nenhum fato) deixados por encerramentos inesperados. */
    suspend fun discardOrphanDrafts()
    suspend fun countSessions(): Int
}
