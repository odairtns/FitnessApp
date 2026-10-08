package com.aksoit.myfitnessapp.di

import android.content.Context
import android.os.SystemClock
import com.aksoit.myfitnessapp.application.exercise.ArchiveExerciseUseCase
import com.aksoit.myfitnessapp.application.exercise.EnsureNativeCatalogSeededUseCase
import com.aksoit.myfitnessapp.application.exercise.ObserveExercisesUseCase
import com.aksoit.myfitnessapp.application.exercise.SaveExerciseUseCase
import com.aksoit.myfitnessapp.application.exercise.SearchExercisesUseCase
import com.aksoit.myfitnessapp.application.history.GetLastLoadByExerciseAndSetUseCase
import com.aksoit.myfitnessapp.application.history.GetSessionDetailUseCase
import com.aksoit.myfitnessapp.application.history.ObservePersonalRecordsUseCase
import com.aksoit.myfitnessapp.application.history.ObserveSessionsUseCase
import com.aksoit.myfitnessapp.application.history.ObserveWeeklyVolumeUseCase
import com.aksoit.myfitnessapp.application.session.CancelWorkoutUseCase
import com.aksoit.myfitnessapp.application.session.ClearRecoveryStateUseCase
import com.aksoit.myfitnessapp.application.session.ConfirmSetUseCase
import com.aksoit.myfitnessapp.application.session.DiscardWorkoutUseCase
import com.aksoit.myfitnessapp.application.session.FinishWorkoutUseCase
import com.aksoit.myfitnessapp.application.session.GetRecoverableSessionUseCase
import com.aksoit.myfitnessapp.application.session.SaveAmrapResultUseCase
import com.aksoit.myfitnessapp.application.session.SaveEmomIntervalResultUseCase
import com.aksoit.myfitnessapp.application.session.SaveForTimeResultUseCase
import com.aksoit.myfitnessapp.application.session.SaveRecoveryCheckpointUseCase
import com.aksoit.myfitnessapp.application.session.StartWorkoutUseCase
import com.aksoit.myfitnessapp.application.timer.IndependentTimerUseCase
import com.aksoit.myfitnessapp.application.workout.ArchiveWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.CreateWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.DeleteWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.DuplicateWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.GetWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.workout.ObserveActiveTemplatesUseCase
import com.aksoit.myfitnessapp.application.workout.UpdateWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.xml.ExportWorkoutTemplateXmlUseCase
import com.aksoit.myfitnessapp.application.xml.ImportWorkoutTemplateUseCase
import com.aksoit.myfitnessapp.application.xml.PreviewXmlImportUseCase
import com.aksoit.myfitnessapp.application.xml.ValidateXmlUseCase
import com.aksoit.myfitnessapp.data.local.FitnessDatabase
import com.aksoit.myfitnessapp.data.preferences.SharedPreferencesRepository
import com.aksoit.myfitnessapp.data.repository.ExerciseRepositoryImpl
import com.aksoit.myfitnessapp.data.repository.HistoryRepositoryImpl
import com.aksoit.myfitnessapp.data.repository.SessionRepositoryImpl
import com.aksoit.myfitnessapp.data.repository.WorkoutRepositoryImpl
import com.aksoit.myfitnessapp.domain.execution.DefaultExecutionCompiler
import com.aksoit.myfitnessapp.domain.execution.ExecutionCompiler
import com.aksoit.myfitnessapp.domain.repository.ExerciseRepository
import com.aksoit.myfitnessapp.domain.repository.HistoryRepository
import com.aksoit.myfitnessapp.domain.repository.PreferencesRepository
import com.aksoit.myfitnessapp.domain.repository.SessionRepository
import com.aksoit.myfitnessapp.domain.repository.WorkoutRepository
import com.aksoit.myfitnessapp.domain.timer.MonotonicClock
import com.aksoit.myfitnessapp.domain.timer.WallClock
import com.aksoit.myfitnessapp.platform.timer.MonotonicTimerEngineImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface AppContainer {
    val database: FitnessDatabase
    val wallClock: WallClock
    val monotonicClock: MonotonicClock

    val workoutRepository: WorkoutRepository
    val sessionRepository: SessionRepository
    val exerciseRepository: ExerciseRepository
    val historyRepository: HistoryRepository
    val preferencesRepository: PreferencesRepository
    val executionCompiler: ExecutionCompiler

    // Workout
    val observeActiveTemplatesUseCase: ObserveActiveTemplatesUseCase
    val getWorkoutTemplateUseCase: GetWorkoutTemplateUseCase
    val createWorkoutTemplateUseCase: CreateWorkoutTemplateUseCase
    val updateWorkoutTemplateUseCase: UpdateWorkoutTemplateUseCase
    val duplicateWorkoutTemplateUseCase: DuplicateWorkoutTemplateUseCase
    val archiveWorkoutTemplateUseCase: ArchiveWorkoutTemplateUseCase
    val deleteWorkoutTemplateUseCase: DeleteWorkoutTemplateUseCase

    // Session
    val startWorkoutUseCase: StartWorkoutUseCase
    val confirmSetUseCase: ConfirmSetUseCase
    val saveAmrapResultUseCase: SaveAmrapResultUseCase
    val saveForTimeResultUseCase: SaveForTimeResultUseCase
    val saveEmomIntervalResultUseCase: SaveEmomIntervalResultUseCase
    val finishWorkoutUseCase: FinishWorkoutUseCase
    val cancelWorkoutUseCase: CancelWorkoutUseCase
    val discardWorkoutUseCase: DiscardWorkoutUseCase
    val getRecoverableSessionUseCase: GetRecoverableSessionUseCase
    val saveRecoveryCheckpointUseCase: SaveRecoveryCheckpointUseCase
    val clearRecoveryStateUseCase: ClearRecoveryStateUseCase

    // History
    val observeSessionsUseCase: ObserveSessionsUseCase
    val getSessionDetailUseCase: GetSessionDetailUseCase
    val getLastLoadByExerciseAndSetUseCase: GetLastLoadByExerciseAndSetUseCase
    val observeWeeklyVolumeUseCase: ObserveWeeklyVolumeUseCase
    val observePersonalRecordsUseCase: ObservePersonalRecordsUseCase

    // Exercise
    val observeExercisesUseCase: ObserveExercisesUseCase
    val searchExercisesUseCase: SearchExercisesUseCase
    val saveExerciseUseCase: SaveExerciseUseCase
    val archiveExerciseUseCase: ArchiveExerciseUseCase
    val ensureNativeCatalogSeededUseCase: EnsureNativeCatalogSeededUseCase

    // Timer & Tools
    val independentTimerUseCase: IndependentTimerUseCase

    // XML
    val exportWorkoutTemplateXmlUseCase: ExportWorkoutTemplateXmlUseCase
    val validateXmlUseCase: ValidateXmlUseCase
    val previewXmlImportUseCase: PreviewXmlImportUseCase
    val importWorkoutTemplateUseCase: ImportWorkoutTemplateUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val database: FitnessDatabase by lazy {
        FitnessDatabase.buildDatabase(context)
    }

    override val wallClock: WallClock = WallClock { System.currentTimeMillis() }
    override val monotonicClock: MonotonicClock = MonotonicClock { SystemClock.elapsedRealtime() }

    override val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(
            database = database,
            templateDao = database.workoutTemplateDao(),
            blockDao = database.workoutBlockDao(),
            exerciseDao = database.exerciseDao(),
            wallClock = wallClock
        )
    }

    override val sessionRepository: SessionRepository by lazy {
        SessionRepositoryImpl(
            database = database,
            sessionDao = database.workoutSessionDao(),
            recoveryDao = database.timerRecoveryDao(),
            historyDao = database.historyDao(),
            monotonicClock = monotonicClock
        )
    }

    override val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepositoryImpl(database.exerciseDao())
    }

    override val historyRepository: HistoryRepository by lazy {
        HistoryRepositoryImpl(database.historyDao())
    }

    override val preferencesRepository: PreferencesRepository by lazy {
        SharedPreferencesRepository(context)
    }

    override val executionCompiler: ExecutionCompiler by lazy {
        DefaultExecutionCompiler()
    }

    // Workout
    override val observeActiveTemplatesUseCase by lazy { ObserveActiveTemplatesUseCase(workoutRepository) }
    override val getWorkoutTemplateUseCase by lazy { GetWorkoutTemplateUseCase(workoutRepository) }
    override val createWorkoutTemplateUseCase by lazy { CreateWorkoutTemplateUseCase(workoutRepository, wallClock) }
    override val updateWorkoutTemplateUseCase by lazy { UpdateWorkoutTemplateUseCase(workoutRepository, wallClock) }
    override val duplicateWorkoutTemplateUseCase by lazy { DuplicateWorkoutTemplateUseCase(workoutRepository, wallClock) }
    override val archiveWorkoutTemplateUseCase by lazy { ArchiveWorkoutTemplateUseCase(workoutRepository) }
    override val deleteWorkoutTemplateUseCase by lazy { DeleteWorkoutTemplateUseCase(workoutRepository) }

    // Session
    override val startWorkoutUseCase by lazy { StartWorkoutUseCase(executionCompiler, sessionRepository, wallClock) }
    override val confirmSetUseCase by lazy { ConfirmSetUseCase(sessionRepository, wallClock) }
    override val saveAmrapResultUseCase by lazy { SaveAmrapResultUseCase(sessionRepository) }
    override val saveForTimeResultUseCase by lazy { SaveForTimeResultUseCase(sessionRepository) }
    override val saveEmomIntervalResultUseCase by lazy { SaveEmomIntervalResultUseCase(sessionRepository) }
    override val finishWorkoutUseCase by lazy { FinishWorkoutUseCase(sessionRepository, wallClock) }
    override val cancelWorkoutUseCase by lazy { CancelWorkoutUseCase(sessionRepository, wallClock) }
    override val discardWorkoutUseCase by lazy { DiscardWorkoutUseCase(sessionRepository) }
    override val getRecoverableSessionUseCase by lazy { GetRecoverableSessionUseCase(sessionRepository) }
    override val saveRecoveryCheckpointUseCase by lazy { SaveRecoveryCheckpointUseCase(sessionRepository) }
    override val clearRecoveryStateUseCase by lazy { ClearRecoveryStateUseCase(sessionRepository) }

    // History
    override val observeSessionsUseCase by lazy { ObserveSessionsUseCase(historyRepository) }
    override val getSessionDetailUseCase by lazy { GetSessionDetailUseCase(historyRepository) }
    override val getLastLoadByExerciseAndSetUseCase by lazy { GetLastLoadByExerciseAndSetUseCase(historyRepository) }
    override val observeWeeklyVolumeUseCase by lazy { ObserveWeeklyVolumeUseCase(historyRepository, wallClock) }
    override val observePersonalRecordsUseCase by lazy { ObservePersonalRecordsUseCase(historyRepository) }

    // Exercise
    override val observeExercisesUseCase by lazy { ObserveExercisesUseCase(exerciseRepository) }
    override val searchExercisesUseCase by lazy { SearchExercisesUseCase(exerciseRepository) }
    override val saveExerciseUseCase by lazy { SaveExerciseUseCase(exerciseRepository) }
    override val archiveExerciseUseCase by lazy { ArchiveExerciseUseCase(exerciseRepository) }
    override val ensureNativeCatalogSeededUseCase by lazy { EnsureNativeCatalogSeededUseCase(exerciseRepository) }

    // Timer & Tools
    override val independentTimerUseCase by lazy {
        IndependentTimerUseCase(MonotonicTimerEngineImpl(applicationScope, monotonicClock))
    }

    // XML
    override val exportWorkoutTemplateXmlUseCase by lazy { ExportWorkoutTemplateXmlUseCase(workoutRepository) }
    override val validateXmlUseCase by lazy { ValidateXmlUseCase() }
    override val previewXmlImportUseCase by lazy { PreviewXmlImportUseCase(workoutRepository, exerciseRepository) }
    override val importWorkoutTemplateUseCase by lazy { ImportWorkoutTemplateUseCase(workoutRepository) }
}
