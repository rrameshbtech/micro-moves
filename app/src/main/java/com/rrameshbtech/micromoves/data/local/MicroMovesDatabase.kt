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
import kotlinx.coroutines.flow.all
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

@Database(
    entities = [Break::class, Exercise::class, RoutineStep::class, BreakOccurrence::class, ExerciseOccurrence::class],
    version = 8,
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
                        DatabaseSeeder.seed(
                            context,
                            database.exerciseDao(),
                            database.breakDao(),
                            database.routineStepDao()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Snapshots a break's current live routine into one [BreakOccurrence] row per requested time
 * (breakId + ordered exercise ids at fire time), enforcing "at most one open occurrence per
 * breakId": any still-open occurrence for the break is marked skipped, and when backfilling
 * multiple missed [triggeredAtTimes] at once, every one of them except the newest is inserted
 * already marked skipped — only the newest is left open. The single call site for occurrence
 * creation, used by both the watcher (schedule fires, possibly several missed at once after a
 * long absence) and the manual long-press flow (always a single time).
 */
suspend fun MicroMovesDatabase.createBreakOccurrenceSnapshots(breakId: Long, triggeredAtTimes: List<Long>): List<BreakOccurrence> {
    if (triggeredAtTimes.isEmpty()) return emptyList()
    val routine = getBreakRoutine(breakId) ?: return emptyList()
    if (routine.steps.isEmpty()) return emptyList()
    val exerciseIds = routine.steps.map { it.exercise.id }
    return withTransaction {
        breakOccurrenceDao().getPendingForBreak(breakId)?.let { open ->
            breakOccurrenceDao().markSkipped(open.id, System.currentTimeMillis())
        }
        val sorted = triggeredAtTimes.sorted()
        sorted.mapIndexed { index, triggeredAt ->
            val occurrence = BreakOccurrence(
                breakId = breakId,
                triggeredAt = triggeredAt,
                exerciseIds = exerciseIds,
                skippedAt = if (index == sorted.lastIndex) null else triggeredAt,
            )
            occurrence.copy(id = breakOccurrenceDao().insert(occurrence))
        }
    }
}

suspend fun MicroMovesDatabase.createBreakOccurrenceSnapshot(breakId: Long): BreakOccurrence? =
    createBreakOccurrenceSnapshots(breakId, listOf(System.currentTimeMillis())).lastOrNull()

suspend fun MicroMovesDatabase.getBreakRoutine(breakId: Long): BreakRoutine? {
    val breakEntity = breakDao().getBreakById(breakId) ?: return null
    return withTransaction {
        val steps = routineStepDao().getStepsForBreak(breakId)
        val exercisesById = exerciseDao().getExercisesByIds(steps.map { it.exerciseId }).associateBy { it.id }
        BreakRoutine(
            breakItem = breakEntity,
            steps = steps.mapNotNull { step ->
                exercisesById[step.exerciseId]?.let { exercise ->
                    ResolvedRoutineStep(
                        exercise = exercise,
                        position = step.position,
                        pauseAfterStep = step.pauseAfterStep
                    )
                }
            },
        )
    }
}
