package com.aksoit.myfitnessapp.application.history

import com.aksoit.myfitnessapp.domain.model.PersonalRecord
import com.aksoit.myfitnessapp.domain.model.WorkoutSession
import com.aksoit.myfitnessapp.domain.model.WorkoutSessionSummary
import com.aksoit.myfitnessapp.domain.repository.HistoryRepository
import com.aksoit.myfitnessapp.domain.timer.WallClock
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ObserveSessionsUseCase(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(limit: Int = 100): Flow<List<WorkoutSessionSummary>> =
        historyRepository.observeRecentSessions(limit)
}

class GetSessionDetailUseCase(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(sessionId: Long): WorkoutSession? =
        historyRepository.getSessionDetail(sessionId)
}

class GetLastLoadByExerciseAndSetUseCase(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(exerciseId: Long?, exerciseName: String, setNumber: Int): Double? =
        historyRepository.getLastLoad(exerciseId, exerciseName, setNumber)
}

class ObserveWeeklyVolumeUseCase(
    private val historyRepository: HistoryRepository,
    private val wallClock: WallClock
) {
    operator fun invoke(): Flow<Double> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = wallClock.currentTimeMillis()
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return historyRepository.observeWeeklyTonnage(calendar.timeInMillis)
    }
}

class ObservePersonalRecordsUseCase(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): Flow<List<PersonalRecord>> =
        historyRepository.observePersonalRecords()
}
