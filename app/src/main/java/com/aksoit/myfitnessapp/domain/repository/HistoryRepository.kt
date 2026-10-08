package com.aksoit.myfitnessapp.domain.repository

import com.aksoit.myfitnessapp.domain.model.PersonalRecord
import com.aksoit.myfitnessapp.domain.model.WorkoutSession
import com.aksoit.myfitnessapp.domain.model.WorkoutSessionSummary
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observeRecentSessions(limit: Int): Flow<List<WorkoutSessionSummary>>
    suspend fun getSessionDetail(sessionId: Long): WorkoutSession?

    /** Busca em 2 etapas: mesmo exercício + mesma série; fallback para última carga do exercício. */
    suspend fun getLastLoad(exerciseId: Long?, exerciseName: String, setNumber: Int): Double?
    fun observeWeeklyTonnage(startOfWeekEpochMs: Long): Flow<Double>
    fun observePersonalRecords(): Flow<List<PersonalRecord>>
}
