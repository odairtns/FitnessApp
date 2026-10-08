package com.aksoit.myfitnessapp.data.repository

import com.aksoit.myfitnessapp.data.local.dao.ExerciseDao
import com.aksoit.myfitnessapp.data.mapper.toDomain
import com.aksoit.myfitnessapp.data.mapper.toEntity
import com.aksoit.myfitnessapp.domain.model.Exercise
import com.aksoit.myfitnessapp.domain.model.normalizeExerciseName
import com.aksoit.myfitnessapp.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun observeAllExercises(): Flow<List<Exercise>> =
        exerciseDao.observeActive().map { list -> list.map { it.toDomain() } }

    override suspend fun searchExercises(query: String): List<Exercise> =
        exerciseDao.search(query.trim()).map { it.toDomain() }

    override suspend fun getExerciseById(id: Long): Exercise? = exerciseDao.getById(id)?.toDomain()

    override suspend fun getExerciseByStableKey(key: String): Exercise? = exerciseDao.getByStableKey(key)?.toDomain()

    override suspend fun findByNormalizedName(name: String): Exercise? =
        exerciseDao.findByNormalizedName(normalizeExerciseName(name))?.toDomain()

    override suspend fun saveExercise(exercise: Exercise): Long = guarded {
        if (exercise.id == 0L) exerciseDao.insert(exercise.toEntity())
        else {
            exerciseDao.update(exercise.toEntity())
            exercise.id
        }
    }

    override suspend fun archiveExercise(id: Long) = guarded { exerciseDao.archive(id) }

    override suspend fun seedCatalog(exercises: List<Exercise>) = guarded {
        exerciseDao.insertAllIgnoringConflicts(exercises.map { it.toEntity() })
        Unit
    }
}
