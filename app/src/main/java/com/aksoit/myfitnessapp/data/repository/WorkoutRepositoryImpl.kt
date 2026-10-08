package com.aksoit.myfitnessapp.data.repository

import androidx.room.withTransaction
import com.aksoit.myfitnessapp.data.local.FitnessDatabase
import com.aksoit.myfitnessapp.data.local.dao.ExerciseDao
import com.aksoit.myfitnessapp.data.local.dao.WorkoutBlockDao
import com.aksoit.myfitnessapp.data.local.dao.WorkoutTemplateDao
import com.aksoit.myfitnessapp.data.mapper.toDomain
import com.aksoit.myfitnessapp.data.mapper.toEntity
import com.aksoit.myfitnessapp.domain.model.DomainException
import com.aksoit.myfitnessapp.domain.model.EquipmentType
import com.aksoit.myfitnessapp.domain.model.Exercise
import com.aksoit.myfitnessapp.domain.model.ExerciseCategory
import com.aksoit.myfitnessapp.domain.model.InvalidTemplateException
import com.aksoit.myfitnessapp.domain.model.MuscleGroup
import com.aksoit.myfitnessapp.domain.model.PersistenceException
import com.aksoit.myfitnessapp.domain.model.TemplateNotFoundException
import com.aksoit.myfitnessapp.domain.model.WorkoutBlock
import com.aksoit.myfitnessapp.domain.model.WorkoutModality
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import com.aksoit.myfitnessapp.domain.model.normalizeExerciseName
import com.aksoit.myfitnessapp.domain.repository.WorkoutRepository
import com.aksoit.myfitnessapp.domain.timer.WallClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl(
    private val database: FitnessDatabase,
    private val templateDao: WorkoutTemplateDao,
    private val blockDao: WorkoutBlockDao,
    private val exerciseDao: ExerciseDao,
    private val wallClock: WallClock
) : WorkoutRepository {

    override fun observeActiveTemplates(): Flow<List<WorkoutTemplate>> =
        templateDao.observeActive().map { list -> list.map { it.toDomain() } }

    override suspend fun getTemplateById(id: Long): WorkoutTemplate? = templateDao.getById(id)?.toDomain()

    override suspend fun getTemplateByUuid(uuid: String): WorkoutTemplate? = templateDao.getByUuid(uuid)?.toDomain()

    override suspend fun saveTemplate(template: WorkoutTemplate): Long = guarded {
        if (template.name.isBlank()) throw InvalidTemplateException("O nome do treino é obrigatório.")
        database.withTransaction {
            val now = wallClock.currentTimeMillis()
            val templateId = if (template.id == 0L) {
                templateDao.insert(template.copy(createdAtEpochMs = now, updatedAtEpochMs = now).toEntity())
            } else {
                val existing = templateDao.getEntityById(template.id) ?: throw TemplateNotFoundException(template.id)
                templateDao.update(
                    template.toEntity().copy(createdAtEpochMs = existing.createdAtEpochMs, updatedAtEpochMs = now)
                )
                blockDao.deleteBlocksForTemplate(template.id)
                template.id
            }
            insertTree(templateId, template.blocks) { it }
            templateId
        }
    }

    override suspend fun importTemplate(template: WorkoutTemplate, replaceTemplateId: Long?): Long = guarded {
        database.withTransaction {
            val now = wallClock.currentTimeMillis()
            val createdInThisImport = mutableMapOf<String, Long>()

            // 1. Cria exercícios customizados aprovados pelo usuário (se houver)
            suspend fun resolve(exerciseId: Long?, customName: String?): Long? {
                if (exerciseId != null && exerciseDao.getById(exerciseId) != null) return exerciseId
                val name = customName?.trim().orEmpty()
                if (name.isEmpty()) return null
                val key = normalizeExerciseName(name)
                createdInThisImport[key]?.let { return it }
                val newId = exerciseDao.insert(
                    Exercise(
                        name = name,
                        category = categoryFor(template.modality),
                        muscleGroup = MuscleGroup.FULL_BODY,
                        equipment = EquipmentType.OTHER,
                        isCustom = true
                    ).toEntity()
                )
                createdInThisImport[key] = newId
                return newId
            }

            val resolvedBlocks = template.blocks.map { block ->
                block.copy(exercises = block.exercises.map { ex ->
                    ex.copy(exerciseId = resolve(ex.exerciseId, ex.exerciseNameCustom ?: ex.resolvedName))
                })
            }

            // 2. Insere/substitui o cabeçalho do WorkoutTemplate
            val templateId = if (replaceTemplateId != null) {
                val existing = templateDao.getEntityById(replaceTemplateId)
                    ?: throw TemplateNotFoundException(replaceTemplateId)
                templateDao.update(
                    template.copy(
                        id = existing.id,
                        templateUuid = existing.templateUuid,
                        isArchived = false
                    ).toEntity().copy(createdAtEpochMs = existing.createdAtEpochMs, updatedAtEpochMs = now)
                )
                blockDao.deleteBlocksForTemplate(existing.id)
                existing.id
            } else {
                templateDao.insert(template.copy(id = 0, createdAtEpochMs = now, updatedAtEpochMs = now).toEntity())
            }

            // 3. Insere todos os Blocos, Exercícios e Séries Planejadas em lote
            insertTree(templateId, resolvedBlocks) { it }
            templateId // Se qualquer insert falhar, o Room efetua rollback total
        }
    }

    private suspend fun insertTree(templateId: Long, blocks: List<WorkoutBlock>, resolveId: (Long?) -> Long?) {
        blocks.sortedBy { it.position }.forEachIndexed { bIdx, block ->
            val blockId = blockDao.insertBlock(block.toEntity(templateId, bIdx))
            block.exercises.sortedBy { it.position }.forEachIndexed { eIdx, ex ->
                val exId = blockDao.insertExercise(ex.toEntity(blockId, eIdx, resolveId(ex.exerciseId)))
                val sets = ex.plannedSets.sortedBy { it.setNumber }
                    .mapIndexed { sIdx, set -> set.toEntity(exId, sIdx + 1) }
                if (sets.isNotEmpty()) blockDao.insertPlannedSets(sets)
            }
        }
    }

    override suspend fun archiveTemplate(id: Long) = guarded {
        templateDao.archive(id, wallClock.currentTimeMillis())
    }

    override suspend fun deleteTemplate(id: Long): Boolean = guarded {
        database.withTransaction {
            if (templateDao.countSessionsForTemplate(id) > 0) {
                // Histórico ativo: força arquivamento (nunca quebra relatórios).
                templateDao.archive(id, wallClock.currentTimeMillis())
                false
            } else {
                templateDao.deleteById(id)
                true
            }
        }
    }

    override suspend fun hasSessions(templateId: Long): Boolean = templateDao.countSessionsForTemplate(templateId) > 0

    override suspend fun countTemplates(): Int = templateDao.count()

    private fun categoryFor(modality: WorkoutModality): ExerciseCategory = when (modality) {
        WorkoutModality.STRENGTH -> ExerciseCategory.STRENGTH
        WorkoutModality.CARDIO -> ExerciseCategory.CARDIO
        WorkoutModality.CROSS_TRAINING -> ExerciseCategory.CONDITIONING
        WorkoutModality.MOBILITY -> ExerciseCategory.MOBILITY
    }
}

/** Mapeia exceções de infraestrutura (SQLite) para DomainException. */
internal suspend inline fun <T> guarded(crossinline block: suspend () -> T): T = try {
    block()
} catch (e: DomainException) {
    throw e
} catch (e: kotlinx.coroutines.CancellationException) {
    throw e
} catch (e: Exception) {
    throw PersistenceException(e.message ?: "Falha de persistência", e)
}
