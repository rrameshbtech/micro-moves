package com.rrameshbtech.micromoves.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakOccurrence
import com.rrameshbtech.micromoves.data.BreakRoutine
import com.rrameshbtech.micromoves.data.Exercise
import com.rrameshbtech.micromoves.data.ExerciseOccurrence
import com.rrameshbtech.micromoves.data.ResolvedRoutineStep
import com.rrameshbtech.micromoves.data.RoutineStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Break::class, Exercise::class, RoutineStep::class, BreakOccurrence::class, ExerciseOccurrence::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(MicroMovesDBConverters::class)
abstract class MicroMovesDatabase : RoomDatabase() {

    abstract fun breakDao(): BreakDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineStepDao(): RoutineStepDao
    abstract fun breakOccurrenceDao(): BreakOccurrenceDao
    abstract fun exerciseOccurrenceDao(): ExerciseOccurrenceDao

    companion object {
        @Volatile
        private var INSTANCE: MicroMovesDatabase? = null

        fun getDatabase(context: Context): MicroMovesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MicroMovesDatabase::class.java,
                    "micromoves_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .addCallback(SeedCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    if (database.breakDao().getBreakCount() == 0) {
                        DatabaseSeeder.seed(context, database.exerciseDao(), database.breakDao(), database.routineStepDao())
                    }
                }
            }
        }
    }
}

suspend fun MicroMovesDatabase.getBreakRoutine(breakId: Long): BreakRoutine? {
    val breakEntity = breakDao().getBreakById(breakId) ?: return null
    return withTransaction {
        val steps = routineStepDao().getStepsForBreak(breakId)
        val exercisesById = exerciseDao().getExercisesByIds(steps.map { it.exerciseId }).associateBy { it.id }
        BreakRoutine(
            breakItem = breakEntity,
            steps = steps.mapNotNull { step ->
                exercisesById[step.exerciseId]?.let { exercise ->
                    ResolvedRoutineStep(exercise = exercise, position = step.position, pauseAfterStep = step.pauseAfterStep)
                }
            },
        )
    }
}
