package com.aksoit.myfitnessapp.application.exercise

import com.aksoit.myfitnessapp.data.seed.NativeExerciseCatalog
import com.aksoit.myfitnessapp.domain.model.Exercise
import com.aksoit.myfitnessapp.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow

class ObserveExercisesUseCase(
    private val exerciseRepository: ExerciseRepository
) {
    operator fun invoke(): Flow<List<Exercise>> = exerciseRepository.observeAllExercises()
}

class SearchExercisesUseCase(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(query: String): List<Exercise> = exerciseRepository.searchExercises(query)
}

class SaveExerciseUseCase(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(exercise: Exercise): Long = exerciseRepository.saveExercise(exercise)
}

class ArchiveExerciseUseCase(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(id: Long) = exerciseRepository.archiveExercise(id)
}

class EnsureNativeCatalogSeededUseCase(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke() {
        exerciseRepository.seedCatalog(NativeExerciseCatalog.exercises)
    }
}
