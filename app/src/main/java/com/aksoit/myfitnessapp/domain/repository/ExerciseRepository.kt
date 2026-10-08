package com.aksoit.myfitnessapp.domain.repository

import com.aksoit.myfitnessapp.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeAllExercises(): Flow<List<Exercise>>
    suspend fun searchExercises(query: String): List<Exercise>
    suspend fun getExerciseById(id: Long): Exercise?
    suspend fun getExerciseByStableKey(key: String): Exercise?
    suspend fun findByNormalizedName(name: String): Exercise?
    suspend fun saveExercise(exercise: Exercise): Long
    suspend fun archiveExercise(id: Long)

    /** Insere o catálogo nativo de forma idempotente (ignora stableKeys existentes). */
    suspend fun seedCatalog(exercises: List<Exercise>)
}
