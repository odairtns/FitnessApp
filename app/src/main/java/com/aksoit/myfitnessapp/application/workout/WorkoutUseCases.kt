package com.aksoit.myfitnessapp.application.workout

import com.aksoit.myfitnessapp.domain.model.InvalidTemplateException
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import com.aksoit.myfitnessapp.domain.repository.WorkoutRepository
import com.aksoit.myfitnessapp.domain.timer.WallClock
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ObserveActiveTemplatesUseCase(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<WorkoutTemplate>> = workoutRepository.observeActiveTemplates()
}

class GetWorkoutTemplateUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(id: Long): WorkoutTemplate? = workoutRepository.getTemplateById(id)
}

class CreateWorkoutTemplateUseCase(
    private val workoutRepository: WorkoutRepository,
    private val wallClock: WallClock
) {
    suspend operator fun invoke(template: WorkoutTemplate): Long {
        if (template.name.isBlank()) throw InvalidTemplateException("Nome do treino é obrigatório.")
        val now = wallClock.currentTimeMillis()
        val templateToSave = template.copy(
            id = 0L,
            templateUuid = if (template.templateUuid.isBlank()) UUID.randomUUID().toString() else template.templateUuid,
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
        return workoutRepository.saveTemplate(templateToSave)
    }
}

class UpdateWorkoutTemplateUseCase(
    private val workoutRepository: WorkoutRepository,
    private val wallClock: WallClock
) {
    suspend operator fun invoke(template: WorkoutTemplate): Long {
        if (template.name.isBlank()) throw InvalidTemplateException("Nome do treino é obrigatório.")
        val now = wallClock.currentTimeMillis()
        val templateToSave = template.copy(updatedAtEpochMs = now)
        return workoutRepository.saveTemplate(templateToSave)
    }
}

class DuplicateWorkoutTemplateUseCase(
    private val workoutRepository: WorkoutRepository,
    private val wallClock: WallClock
) {
    suspend operator fun invoke(templateId: Long): Long {
        val original = workoutRepository.getTemplateById(templateId)
            ?: throw InvalidTemplateException("Treino não encontrado para duplicação: $templateId")
        val now = wallClock.currentTimeMillis()
        val duplicated = original.copy(
            id = 0L,
            templateUuid = UUID.randomUUID().toString(),
            name = "${original.name} (Cópia)",
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
            blocks = original.blocks.map { block ->
                block.copy(
                    id = 0L,
                    exercises = block.exercises.map { ex ->
                        ex.copy(
                            id = 0L,
                            plannedSets = ex.plannedSets.map { set -> set.copy(id = 0L) }
                        )
                    }
                )
            }
        )
        return workoutRepository.saveTemplate(duplicated)
    }
}

class ArchiveWorkoutTemplateUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(id: Long) {
        workoutRepository.archiveTemplate(id)
    }
}

class DeleteWorkoutTemplateUseCase(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * Retorna true se foi excluído fisicamente; false se foi arquivado por conter histórico de sessões.
     */
    suspend operator fun invoke(id: Long): Boolean = workoutRepository.deleteTemplate(id)
}
