package com.aksoit.myfitnessapp.application.session

import com.aksoit.myfitnessapp.domain.execution.ExecutionCompiler
import com.aksoit.myfitnessapp.domain.execution.ExecutionPlan
import com.aksoit.myfitnessapp.domain.model.AmrapResult
import com.aksoit.myfitnessapp.domain.model.EmomIntervalResult
import com.aksoit.myfitnessapp.domain.model.ForTimeResult
import com.aksoit.myfitnessapp.domain.model.PerformedSet
import com.aksoit.myfitnessapp.domain.model.RecoverableSessionData
import com.aksoit.myfitnessapp.domain.model.RecoveryCheckpoint
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import com.aksoit.myfitnessapp.domain.repository.SessionRepository
import com.aksoit.myfitnessapp.domain.timer.WallClock

data class WorkoutSessionStartResult(
    val sessionId: Long,
    val plan: ExecutionPlan
)

class StartWorkoutUseCase(
    private val compiler: ExecutionCompiler,
    private val sessionRepository: SessionRepository,
    private val wallClock: WallClock
) {
    suspend operator fun invoke(template: WorkoutTemplate): WorkoutSessionStartResult {
        val plan = compiler.compile(template)
        val now = wallClock.currentTimeMillis()
        val sessionId = sessionRepository.createDraft(plan, now)
        return WorkoutSessionStartResult(sessionId = sessionId, plan = plan)
    }
}

class ConfirmSetUseCase(
    private val sessionRepository: SessionRepository,
    private val wallClock: WallClock
) {
    suspend operator fun invoke(
        sessionId: Long,
        blockIndex: Int,
        exercisePosition: Int,
        setNumber: Int,
        actualLoadKg: Double?,
        actualReps: Int?,
        durationSeconds: Int? = null,
        notes: String = ""
    ): Long {
        sessionRepository.promoteToInProgress(sessionId)
        val set = PerformedSet(
            id = 0L,
            performanceId = 0L,
            setNumber = setNumber,
            actualLoadKg = actualLoadKg,
            actualReps = actualReps,
            durationSeconds = durationSeconds,
            isCompleted = true,
            loggedAtEpochMs = wallClock.currentTimeMillis(),
            notes = notes
        )
        return sessionRepository.recordPerformedSet(sessionId, blockIndex, exercisePosition, set)
    }
}

class SaveAmrapResultUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long, blockIndex: Int, result: AmrapResult) {
        sessionRepository.promoteToInProgress(sessionId)
        sessionRepository.saveAmrapResult(sessionId, blockIndex, result)
    }
}

class SaveForTimeResultUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long, blockIndex: Int, result: ForTimeResult) {
        sessionRepository.promoteToInProgress(sessionId)
        sessionRepository.saveForTimeResult(sessionId, blockIndex, result)
    }
}

class SaveEmomIntervalResultUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long, blockIndex: Int, result: EmomIntervalResult) {
        sessionRepository.promoteToInProgress(sessionId)
        sessionRepository.saveEmomIntervalResult(sessionId, blockIndex, result)
    }
}

class FinishWorkoutUseCase(
    private val sessionRepository: SessionRepository,
    private val wallClock: WallClock
) {
    suspend operator fun invoke(sessionId: Long, notes: String = "") {
        val now = wallClock.currentTimeMillis()
        sessionRepository.completeSession(sessionId, now, notes)
        sessionRepository.clearRecoveryState(sessionId)
    }
}

class CancelWorkoutUseCase(
    private val sessionRepository: SessionRepository,
    private val wallClock: WallClock
) {
    suspend operator fun invoke(sessionId: Long) {
        val now = wallClock.currentTimeMillis()
        sessionRepository.cancelSession(sessionId, now)
        sessionRepository.clearRecoveryState(sessionId)
    }
}

class DiscardWorkoutUseCase(
    private val sessionRepository: SessionRepository
) {
    /**
     * DELETE CASCADE físico de toda a árvore da sessão.
     */
    suspend operator fun invoke(sessionId: Long) {
        sessionRepository.clearRecoveryState(sessionId)
        sessionRepository.discardSession(sessionId)
    }
}

class SaveRecoveryCheckpointUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(checkpoint: RecoveryCheckpoint) {
        sessionRepository.saveRecoveryCheckpoint(checkpoint)
    }
}

class GetRecoverableSessionUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): RecoverableSessionData? = sessionRepository.getRecoverableSession()
}

class ClearRecoveryStateUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long) {
        sessionRepository.clearRecoveryState(sessionId)
    }
}
