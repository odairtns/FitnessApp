package com.aksoit.myfitnessapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aksoit.myfitnessapp.data.local.converter.RoomTypeConverters
import com.aksoit.myfitnessapp.data.local.dao.ExerciseDao
import com.aksoit.myfitnessapp.data.local.dao.HistoryDao
import com.aksoit.myfitnessapp.data.local.dao.TimerRecoveryDao
import com.aksoit.myfitnessapp.data.local.dao.WorkoutBlockDao
import com.aksoit.myfitnessapp.data.local.dao.WorkoutSessionDao
import com.aksoit.myfitnessapp.data.local.dao.WorkoutTemplateDao
import com.aksoit.myfitnessapp.data.local.entity.AmrapResultEntity
import com.aksoit.myfitnessapp.data.local.entity.EmomIntervalResultEntity
import com.aksoit.myfitnessapp.data.local.entity.ExerciseEntity
import com.aksoit.myfitnessapp.data.local.entity.ExercisePerformanceEntity
import com.aksoit.myfitnessapp.data.local.entity.ForTimeRemainingItemEntity
import com.aksoit.myfitnessapp.data.local.entity.ForTimeResultEntity
import com.aksoit.myfitnessapp.data.local.entity.PerformedSetEntity
import com.aksoit.myfitnessapp.data.local.entity.PersonalRecordEntity
import com.aksoit.myfitnessapp.data.local.entity.PlannedSetEntity
import com.aksoit.myfitnessapp.data.local.entity.SessionBlockEntity
import com.aksoit.myfitnessapp.data.local.entity.TimerRecoveryEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutBlockEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutExerciseEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutSessionEntity
import com.aksoit.myfitnessapp.data.local.entity.WorkoutTemplateEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutTemplateEntity::class,
        WorkoutBlockEntity::class,
        WorkoutExerciseEntity::class,
        PlannedSetEntity::class,
        WorkoutSessionEntity::class,
        SessionBlockEntity::class,
        ExercisePerformanceEntity::class,
        PerformedSetEntity::class,
        AmrapResultEntity::class,
        EmomIntervalResultEntity::class,
        ForTimeResultEntity::class,
        ForTimeRemainingItemEntity::class,
        PersonalRecordEntity::class,
        TimerRecoveryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters::class)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun workoutBlockDao(): WorkoutBlockDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun historyDao(): HistoryDao
    abstract fun timerRecoveryDao(): TimerRecoveryDao

    companion object {
        const val DATABASE_NAME = "fitness_tracker_pro_v1.db"

        /**
         * Migrações explícitas (Spec 10 §2). fallbackToDestructiveMigration() é proibido.
         * Adicione aqui: val MIGRATION_1_2 = object : Migration(1, 2) { ... }
         */
        private val MIGRATIONS = arrayOf<androidx.room.migration.Migration>()

        fun buildDatabase(context: Context): FitnessDatabase =
            Room.databaseBuilder(context.applicationContext, FitnessDatabase::class.java, DATABASE_NAME)
                .addMigrations(*MIGRATIONS)
                .build()
    }
}
