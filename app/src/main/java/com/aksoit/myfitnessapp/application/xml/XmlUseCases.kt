package com.aksoit.myfitnessapp.application.xml

import com.aksoit.myfitnessapp.domain.model.Exercise
import com.aksoit.myfitnessapp.domain.model.InvalidTemplateException
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import com.aksoit.myfitnessapp.domain.model.normalizeExerciseName
import com.aksoit.myfitnessapp.domain.repository.ExerciseRepository
import com.aksoit.myfitnessapp.domain.repository.WorkoutRepository
import java.util.UUID

data class ExerciseMatchInfo(
    val xmlExerciseName: String,
    val xmlKey: String?,
    val matchedExercise: Exercise?,
    val isExactStableKeyMatch: Boolean,
    val isNormalizedNameMatch: Boolean
)

data class ImportPreviewData(
    val template: WorkoutTemplate,
    val isDuplicateUuid: Boolean,
    val existingTemplateId: Long?,
    val exerciseMatches: List<ExerciseMatchInfo>
)

class ExportWorkoutTemplateXmlUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(templateId: Long): String {
        val template = workoutRepository.getTemplateById(templateId)
            ?: throw InvalidTemplateException("Treino não encontrado para exportação: $templateId")
        return WorkoutTemplateXmlSerializer.serialize(template)
    }
}

class ValidateXmlUseCase {
    operator fun invoke(xmlContent: String): Result<WorkoutTemplate> = try {
        Result.success(WorkoutTemplateXmlParser.parse(xmlContent))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class PreviewXmlImportUseCase(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(xmlContent: String): ImportPreviewData {
        val parsed = WorkoutTemplateXmlParser.parse(xmlContent)
        val existingByUuid = workoutRepository.getTemplateByUuid(parsed.templateUuid)

        val matches = mutableListOf<ExerciseMatchInfo>()
        for (block in parsed.blocks) {
            for (ex in block.exercises) {
                var matched: Exercise? = null
                var isKeyMatch = false
                var isNameMatch = false

                // Estágio 1: Match por stableKey
                if (!ex.resolvedStableKey.isNullOrBlank()) {
                    matched = exerciseRepository.getExerciseByStableKey(ex.resolvedStableKey)
                    if (matched != null) isKeyMatch = true
                }

                // Estágio 2: Match por nome normalizado
                if (matched == null) {
                    val normName = normalizeExerciseName(ex.displayName)
                    matched = exerciseRepository.findByNormalizedName(normName)
                    if (matched != null) isNameMatch = true
                }

                matches.add(
                    ExerciseMatchInfo(
                        xmlExerciseName = ex.displayName,
                        xmlKey = ex.resolvedStableKey,
                        matchedExercise = matched,
                        isExactStableKeyMatch = isKeyMatch,
                        isNormalizedNameMatch = isNameMatch
                    )
                )
            }
        }

        return ImportPreviewData(
            template = parsed,
            isDuplicateUuid = existingByUuid != null,
            existingTemplateId = existingByUuid?.id,
            exerciseMatches = matches
        )
    }
}

class ImportWorkoutTemplateUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(template: WorkoutTemplate, replaceExistingId: Long? = null): Long {
        val templateToImport = if (replaceExistingId == null) {
            // Nova importação: se o UUID colide ou por padrão, gera novo UUID para garantir isolamento
            template.copy(templateUuid = UUID.randomUUID().toString())
        } else {
            template
        }
        return workoutRepository.importTemplate(templateToImport, replaceExistingId)
    }
}
