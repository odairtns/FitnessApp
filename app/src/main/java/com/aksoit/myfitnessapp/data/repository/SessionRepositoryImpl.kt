package com.aksoit.myfitnessapp.data.repository

import androidx.room.withTransaction
import com.aksoit.myfitnessapp.data.local.FitnessDatabase
import com.aksoit.myfitnessapp.data.local.dao.HistoryDao
import com.aksoit.myfitnessapp.data.local.dao.TimerRecoveryDao
import com.aksoit.myfitnessapp.data.local.dao.WorkoutSessionDao
import com.aksoit.myfitnessapp.data.local.entity.ExercisePerformanceEntity
import com.aksoit.myfitnessapp.data.local.entity.ForTimeRemainingItemEntity
import com.aksoit.myfitnessapp.data.local.entity.ForTimeResultEntity
import com.aksoit.myfitnessapp.data.local.entity.SessionBlockEntity
import com.aksoit.myfitnessapp.data.local.entity.TimerRecoveryEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutSessionEntity
import com.aksoit.myfitnessapp.data.mapper.toEntity
import com.aksoit.myfitnessapp.domain.execution.ExecutionPlan
import com.aksoit.myfitnessapp.domain.model.AmrapResult
import com.aksoit.myfitnessapp.domain.model.BlockExecutionStatus
import com.aksoit.myfitnessapp.domain.model.EmomIntervalResult
import com.aksoit.myfitnessapp.domain.model.ForTimeResult
import com.aksoit.myfitnessapp.domain.model.PerformedSet
import com.aksoit.myfitnessapp.domain.model.PersistenceException
import com.aksoit.myfitnessapp.domain.model.RecoverableSessionData
import com.aksoit.myfitnessapp.domain.model.RecoveryCheckpoint
import com.aksoit.myfitnessapp.domain.model.SessionStatus
import com.aksoit.myfitnessapp.domain.repository.SessionRepository
import com.aksoit.myfitnessapp.domain.timer.MonotonicClock

class SessionRepositoryImpl(
    private val database: FitnessDatabase,
    private val sessionDao: WorkoutSessionDao,
    private val recoveryDao: TimerRecoveryDao,
    private val historyDao: HistoryDao,
    private val monotonicClock: MonotonicClock
) : SessionRepository {

    private val prCalculator = PersonalRecordCalculator()

    override suspend fun createDraft(plan: ExecutionPlan, startedAtEpochMs: Long): Long = guarded {
        database.withTransaction {
            val sessionId = sessionDao.insertSession(
                WorkoutSessionEntity(
                    templateId = plan.templateId,
                    templateNameSnapshot = plan.templateName,
                    modalitySnapshot = plan.modality.name,
                    protocolSnapshot = plan.protocol.name,
                    startedAtEpochMs = startedAtEpochMs,
                    completedAtEpochMs = null,
                    status = SessionStatus.DRAFT.name,
                    notes = ""
                )
            )
            plan.blocks.forEach { block ->
                val sessionBlockId = sessionDao.insertSessionBlock(
                    SessionBlockEntity(
                        sessionId = sessionId,
                        sourceBlockId = block.sourceBlockId,
                        blockNameSnapshot = block.name,
                        blockTypeSnapshot = block.blockType.name,
                        position = block.index,
                        status = BlockExecutionStatus.PENDING.name
                    )
                )
                block.exercises.forEach { ex ->
                    sessionDao.insertPerformance(
                        ExercisePerformanceEntity(
                            sessionBlockId = sessionBlockId,
                            exerciseId = ex.exerciseId,
                            exerciseNameSnapshot = ex.name,
                            position = ex.position
                        )
                    )
                }
            }
            sessionId
        }
    }

    override suspend fun promoteToInProgress(sessionId: Long) = guarded {
        sessionDao.promoteToInProgress(sessionId)
    }

    override suspend fun recordPerformedSet(
        sessionId: Long,
        blockIndex: Int,
        exercisePosition: Int,
        set: PerformedSet
    ): Long = guarded {
        database.withTransaction {
            sessionDao.promoteToInProgress(sessionId)
            val sessionBlockId = requireBlock(sessionId, blockIndex)
            val performanceId = sessionDao.getPerformanceId(sessionBlockId, exercisePosition)
                ?: throw PersistenceException("Exercício $exercisePosition do bloco $blockIndex não encontrado na sessão.")
            val id = sessionDao.insertPerformedSet(set.toEntity(performanceId))
            sessionDao.getPerformanceNameSnapshot(performanceId)?.let { recomputePersonalRecords(it) }
            id
        }
    }

    override suspend fun saveAmrapResult(sessionId: Long, blockIndex: Int, result: AmrapResult) = guarded {
        database.withTransaction {
            sessionDao.promoteToInProgress(sessionId)
            val sessionBlockId = requireBlock(sessionId, blockIndex)
            sessionDao.deleteAmrapForBlock(sessionBlockId)
            sessionDao.insertAmrapResult(result.toEntity(sessionBlockId))
            Unit
        }
    }

    override suspend fun saveForTimeResult(sessionId: Long, blockIndex: Int, result: ForTimeResult) = guarded {
        database.withTransaction {
            sessionDao.promoteToInProgress(sessionId)
            val sessionBlockId = requireBlock(sessionId, blockIndex)
            sessionDao.deleteForTimeForBlock(sessionBlockId)
            val resultId = sessionDao.insertForTimeResult(
                ForTimeResultEntity(
                    sessionBlockId = sessionBlockId,
                    elapsedSeconds = result.elapsedSeconds,
                    timeCapSeconds = result.timeCapSeconds,
                    status = result.status.name,
                    scalingType = result.scalingType?.name
                )
            )
            if (result.remainingItems.isNotEmpty()) {
                sessionDao.insertRemainingItems(result.remainingItems.map {
                    ForTimeRemainingItemEntity(
                        resultId = resultId,
                        exerciseId = it.exerciseId,
                        exerciseNameSnapshot = it.exerciseNameSnapshot,
                        targetAmount = it.targetAmount,
                        completedAmount = it.completedAmount,
                        remainingAmount = it.remainingAmount,
                        unit = it.unit
                    )
                })
            }
        }
    }

    override suspend fun saveEmomIntervalResult(sessionId: Long, blockIndex: Int, result: EmomIntervalResult) = guarded {
        database.withTransaction {
            sessionDao.promoteToInProgress(sessionId)
            val sessionBlockId = requireBlock(sessionId, blockIndex)
            sessionDao.upsertEmomResult(result.toEntity(sessionBlockId))
            Unit
        }
    }

    override suspend fun completeSession(sessionId: Long, completedAtEpochMs: Long, notes: String) = guarded {
        database.withTransaction {
            sessionDao.markAsCompleted(sessionId, completedAtEpochMs, notes)
            sessionDao.setAllBlockStatuses(sessionId, BlockExecutionStatus.SKIPPED.name)
            val withFacts = sessionDao.getBlockIdsWithFacts(sessionId)
            if (withFacts.isNotEmpty()) sessionDao.setBlockStatuses(withFacts, BlockExecutionStatus.COMPLETED.name)
            recoveryDao.delete(sessionId)
        }
    }

    override suspend fun cancelSession(sessionId: Long, cancelledAtEpochMs: Long) = guarded {
        database.withTransaction {
            sessionDao.markAsCancelled(sessionId, cancelledAtEpochMs)
            sessionDao.setAllBlockStatuses(sessionId, BlockExecutionStatus.PENDING.name)
            val withFacts = sessionDao.getBlockIdsWithFacts(sessionId)
            if (withFacts.isNotEmpty()) sessionDao.setBlockStatuses(withFacts, BlockExecutionStatus.COMPLETED.name)
            recoveryDao.delete(sessionId)
        }
    }

    override suspend fun discardSession(sessionId: Long) = guarded {
        database.withTransaction {
            // DELETE CASCADE físico: blocos, performances, séries, resultados, PRs e recovery.
            sessionDao.discardSessionPhysically(sessionId)
            recomputeAllPersonalRecords()
        }
    }

    override suspend fun getRecoverableSession(): RecoverableSessionData? = guarded {
        val session = sessionDao.getLatestInProgress() ?: return@guarded null
        val recovery = recoveryDao.get(session.id)
        val now = monotonicClock.elapsedRealtime()
        if (recovery == null) {
            RecoverableSessionData(
                sessionId = session.id,
                templateId = session.templateId,
                templateName = session.templateNameSnapshot,
                startedAtEpochMs = session.startedAtEpochMs,
                currentStepId = null,
                remainingMs = 0L,
                isPaused = true,
                isStopwatch = false,
                circuitRoundsCount = 0,
                elapsedTotalMs = 0L
            )
        } else {
            val value = when {
                recovery.isPaused -> recovery.remainingDurationMs
                recovery.isStopwatch -> {
                    // targetEnd guarda a referência de início virtual do cronômetro.
                    val candidate = now - recovery.targetEndElapsedRealtime
                    if (candidate < recovery.remainingDurationMs) recovery.remainingDurationMs else candidate
                }
                else -> (recovery.targetEndElapsedRealtime - now).coerceIn(0L, recovery.remainingDurationMs)
            }
            RecoverableSessionData(
                sessionId = session.id,
                templateId = session.templateId,
                templateName = session.templateNameSnapshot,
                startedAtEpochMs = session.startedAtEpochMs,
                currentStepId = recovery.currentStepId,
                remainingMs = value,
                isPaused = recovery.isPaused,
                isStopwatch = recovery.isStopwatch,
                circuitRoundsCount = recovery.circuitRoundsCount,
                elapsedTotalMs = recovery.elapsedTotalMs
            )
        }
    }

    override suspend fun saveRecoveryCheckpoint(checkpoint: RecoveryCheckpoint) = guarded {
        recoveryDao.upsert(
            TimerRecoveryEntity(
                sessionId = checkpoint.sessionId,
                currentStepId = checkpoint.currentStepId,
                targetEndElapsedRealtime = checkpoint.targetEndElapsedRealtime,
                remainingDurationMs = checkpoint.remainingDurationMs,
                isPaused = checkpoint.isPaused,
                isStopwatch = checkpoint.isStopwatch,
                circuitRoundsCount = checkpoint.circuitRoundsCount,
                elapsedTotalMs = checkpoint.elapsedTotalMs,
                updatedAtEpochMs = checkpoint.updatedAtEpochMs
            )
        )
    }

    override suspend fun clearRecoveryState(sessionId: Long) = guarded {
        recoveryDao.delete(sessionId)
    }

    override suspend fun discardOrphanDrafts() = guarded {
        sessionDao.deleteDrafts()
    }

    override suspend fun countSessions(): Int = sessionDao.countAllSessions()

    private suspend fun requireBlock(sessionId: Long, blockIndex: Int): Long =
        sessionDao.getSessionBlockId(sessionId, blockIndex)
            ?: throw PersistenceException("Bloco $blockIndex não encontrado na sessão $sessionId.")

    private suspend fun recomputePersonalRecords(exerciseName: String) {
        historyDao.deletePersonalRecordsForExercise(exerciseName)
        val records = prCalculator.compute(historyDao.getSetFactsForExercise(exerciseName))
        if (records.isNotEmpty()) historyDao.insertPersonalRecords(records)
    }

    private suspend fun recomputeAllPersonalRecords() {
        historyDao.deleteAllPersonalRecords()
        val records = historyDao.getAllSetFacts()
            .groupBy { it.exerciseNameSnapshot }
            .flatMap { (_, facts) -> prCalculator.compute(facts) }
        if (records.isNotEmpty()) historyDao.insertPersonalRecords(records)
    }
}
