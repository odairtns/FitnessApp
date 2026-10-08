package com.aksoit.myfitnessapp.domain.repository

import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeActiveTemplates(): Flow<List<WorkoutTemplate>>
    suspend fun getTemplateById(id: Long): WorkoutTemplate?
    suspend fun getTemplateByUuid(uuid: String): WorkoutTemplate?

    /** Insere (id == 0) ou atualiza atomicamente cabeçalho + árvore de blocos. */
    suspend fun saveTemplate(template: WorkoutTemplate): Long

    /**
     * Importação atômica (Spec 07 §5): exercícios com exerciseId nulo e exerciseNameCustom
     * preenchido são criados como customizados na mesma transação. Se [replaceTemplateId]
     * for informado, o template existente é substituído.
     */
    suspend fun importTemplate(template: WorkoutTemplate, replaceTemplateId: Long?): Long

    suspend fun archiveTemplate(id: Long)
    suspend fun deleteTemplate(id: Long): Boolean // Retorna false se impedido por histórico
    suspend fun hasSessions(templateId: Long): Boolean
    suspend fun countTemplates(): Int
}
