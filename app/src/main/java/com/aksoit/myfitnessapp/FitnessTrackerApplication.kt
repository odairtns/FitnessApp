package com.aksoit.myfitnessapp

import android.app.Application
import com.aksoit.myfitnessapp.di.AppContainer
import com.aksoit.myfitnessapp.di.DefaultAppContainer
import com.aksoit.myfitnessapp.domain.model.BlockType
import com.aksoit.myfitnessapp.domain.model.PlannedSet
import com.aksoit.myfitnessapp.domain.model.SideMode
import com.aksoit.myfitnessapp.domain.model.WorkoutBlock
import com.aksoit.myfitnessapp.domain.model.WorkoutExercise
import com.aksoit.myfitnessapp.domain.model.WorkoutModality
import com.aksoit.myfitnessapp.domain.model.WorkoutProtocol
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class FitnessTrackerApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        CoroutineScope(Dispatchers.IO).launch {
            // Garante que o catálogo nativo está povoado
            container.ensureNativeCatalogSeededUseCase()

            // Pre-seed de treino demonstrativo caso não haja nenhum template
            if (!container.preferencesRepository.isSampleDataSeeded()) {
                seedSampleTemplates()
                container.preferencesRepository.markSampleDataSeeded()
            }
        }
    }

    private suspend fun seedSampleTemplates() {
        val benchPress = container.exerciseRepository.getExerciseByStableKey("barbell-bench-press")
        val inclineDumbbell = container.exerciseRepository.getExerciseByStableKey("incline-dumbbell-bench-press")
        val skullCrusher = container.exerciseRepository.getExerciseByStableKey("skull-crusher")

        val sampleStrengthTemplate = WorkoutTemplate(
            templateUuid = UUID.randomUUID().toString(),
            name = "Treino Superior Hipertrofia",
            modality = WorkoutModality.STRENGTH,
            protocol = WorkoutProtocol.STANDARD_STRENGTH,
            description = "Foco em peito, ombros e tríceps com descansos estritos.",
            estimatedDurationSeconds = 3600,
            blocks = listOf(
                WorkoutBlock(
                    blockType = BlockType.WORK,
                    position = 0,
                    name = "Peitoral & Tríceps",
                    rounds = 1,
                    exercises = listOf(
                        WorkoutExercise(
                            exerciseId = benchPress?.id,
                            exerciseNameCustom = benchPress?.name ?: "Supino Reto com Barra",
                            position = 0,
                            plannedSets = listOf(
                                PlannedSet(setNumber = 1, targetReps = 10, targetLoadKg = 60.0, restSeconds = 90, sideMode = SideMode.BILATERAL),
                                PlannedSet(setNumber = 2, targetReps = 8, targetLoadKg = 70.0, restSeconds = 90, sideMode = SideMode.BILATERAL),
                                PlannedSet(setNumber = 3, targetReps = 6, targetLoadKg = 80.0, restSeconds = 120, sideMode = SideMode.BILATERAL)
                            )
                        ),
                        WorkoutExercise(
                            exerciseId = inclineDumbbell?.id,
                            exerciseNameCustom = inclineDumbbell?.name ?: "Supino Inclinado com Halteres",
                            position = 1,
                            plannedSets = listOf(
                                PlannedSet(setNumber = 1, targetReps = 10, targetLoadKg = 24.0, restSeconds = 60, sideMode = SideMode.BILATERAL),
                                PlannedSet(setNumber = 2, targetReps = 10, targetLoadKg = 24.0, restSeconds = 60, sideMode = SideMode.BILATERAL),
                                PlannedSet(setNumber = 3, targetReps = 8, targetLoadKg = 26.0, restSeconds = 90, sideMode = SideMode.BILATERAL)
                            )
                        ),
                        WorkoutExercise(
                            exerciseId = skullCrusher?.id,
                            exerciseNameCustom = skullCrusher?.name ?: "Tríceps Testa",
                            position = 2,
                            plannedSets = listOf(
                                PlannedSet(setNumber = 1, targetReps = 12, targetLoadKg = 30.0, restSeconds = 60, sideMode = SideMode.BILATERAL),
                                PlannedSet(setNumber = 2, targetReps = 10, targetLoadKg = 34.0, restSeconds = 60, sideMode = SideMode.BILATERAL)
                            )
                        )
                    )
                )
            ),
            createdAtEpochMs = System.currentTimeMillis(),
            updatedAtEpochMs = System.currentTimeMillis()
        )

        val burpee = container.exerciseRepository.getExerciseByStableKey("burpee")
        val kettlebell = container.exerciseRepository.getExerciseByStableKey("kettlebell-swing")
        val sampleAmrapTemplate = WorkoutTemplate(
            templateUuid = UUID.randomUUID().toString(),
            name = "Condicionamento AMRAP 10 Min",
            modality = WorkoutModality.CROSS_TRAINING,
            protocol = WorkoutProtocol.AMRAP,
            description = "O máximo de rounds possíveis em 10 minutos.",
            estimatedDurationSeconds = 600,
            blocks = listOf(
                WorkoutBlock(
                    blockType = BlockType.CIRCUIT,
                    position = 0,
                    name = "AMRAP 10'",
                    rounds = 1,
                    workDurationSeconds = 600,
                    exercises = listOf(
                        WorkoutExercise(
                            exerciseId = burpee?.id,
                            exerciseNameCustom = burpee?.name ?: "Burpee",
                            position = 0,
                            plannedSets = listOf(PlannedSet(setNumber = 1, targetReps = 10))
                        ),
                        WorkoutExercise(
                            exerciseId = kettlebell?.id,
                            exerciseNameCustom = kettlebell?.name ?: "Kettlebell Swing",
                            position = 1,
                            plannedSets = listOf(PlannedSet(setNumber = 1, targetReps = 15, targetLoadKg = 20.0))
                        )
                    )
                )
            ),
            createdAtEpochMs = System.currentTimeMillis(),
            updatedAtEpochMs = System.currentTimeMillis()
        )

        container.createWorkoutTemplateUseCase(sampleStrengthTemplate)
        container.createWorkoutTemplateUseCase(sampleAmrapTemplate)
    }
}
