package com.aksoit.myfitnessapp.data.repository

import com.aksoit.myfitnessapp.data.local.dao.HistoryDao
import com.aksoit.myfitnessapp.data.mapper.toDomain
import com.aksoit.myfitnessapp.domain.model.PersonalRecord
import com.aksoit.myfitnessapp.domain.model.WorkoutSession
import com.aksoit.myfitnessapp.domain.model.WorkoutSessionSummary
import com.aksoit.myfitnessapp.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun observeRecentSessions(limit: Int): Flow<List<WorkoutSessionSummary>> =
        historyDao.observeSessionSummaries(limit).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getSessionDetail(sessionId: Long): WorkoutSession? =
        historyDao.getSessionDetail(sessionId)?.toDomain()

    override suspend fun getLastLoad(exerciseId: Long?, exerciseName: String, setNumber: Int): Double? =
        historyDao.getLastLoadForSet(exerciseId, exerciseName, setNumber)
            ?: historyDao.getLastLoadFallback(exerciseId, exerciseName)

    override fun observeWeeklyTonnage(startOfWeekEpochMs: Long): Flow<Double> =
        historyDao.observeTonnageSince(startOfWeekEpochMs)

    override fun observePersonalRecords(): Flow<List<PersonalRecord>> =
        historyDao.observePersonalRecords().map { list -> list.map { it.toDomain() } }
}
