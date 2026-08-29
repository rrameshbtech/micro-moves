package com.rrameshbtech.micromoves.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakOccurrence
import com.rrameshbtech.micromoves.data.Exercise
import com.rrameshbtech.micromoves.data.ExerciseOccurrence
import com.rrameshbtech.micromoves.data.ExerciseOutcome
import com.rrameshbtech.micromoves.data.RoutineStep
import kotlinx.coroutines.flow.Flow

@Dao
interface BreakDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(breakItem: Break): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breaks: List<Break>)

    @Update
    suspend fun update(breakItem: Break)

    @Delete
    suspend fun delete(breakItem: Break)

    @Query("SELECT * FROM breaks WHERE id = :id LIMIT 1")
    suspend fun getBreakById(id: Long): Break?

    @Query("SELECT * FROM breaks WHERE id = :id LIMIT 1")
    fun getBreakByIdFlow(id: Long): Flow<Break?>

    @Query("SELECT * FROM breaks ORDER BY name ASC")
    fun getAllBreaks(): Flow<List<Break>>

    @Query("SELECT * FROM breaks WHERE enabled=1 ORDER BY name ASC")
    fun getActiveBreaks(): Flow<List<Break>>

    @Query("SELECT COUNT(*) FROM breaks")
    suspend fun getBreakCount(): Int

    @Query("DELETE FROM breaks WHERE id = :id")
    suspend fun deleteBreak(id: Long)
}

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: Long): Exercise?

    @Query("SELECT * FROM exercises WHERE id IN (:ids)")
    suspend fun getExercisesByIds(ids: List<Long>): List<Exercise>

    @Delete
    suspend fun delete(exerciseItem: Exercise)
}

@Dao
interface RoutineStepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(steps: List<RoutineStep>)

    @Query("DELETE FROM routine_steps WHERE breakId = :breakId")
    suspend fun clearForBreak(breakId: Long)

    @Query("SELECT * FROM routine_steps WHERE breakId = :breakId ORDER BY position ASC")
    suspend fun getStepsForBreak(breakId: Long): List<RoutineStep>
}

@Dao
interface BreakOccurrenceDao {

    @Insert
    suspend fun insert(occurrence: BreakOccurrence): Long
}

@Dao
interface ExerciseOccurrenceDao {

    @Insert
    suspend fun insert(occurrence: ExerciseOccurrence)

    @Insert
    suspend fun insertAll(occurrences: List<ExerciseOccurrence>)

    @Query(
        """
        SELECT eo.exerciseId AS exerciseId, e.name AS exerciseName, eo.durationMs AS durationMs, eo.outcome AS outcome
        FROM exercise_occurrences eo
        JOIN break_occurrences bo ON bo.id = eo.breakOccurrenceId
        JOIN exercises e ON e.id = eo.exerciseId
        WHERE bo.triggeredAt >= :sinceMillis
        """
    )
    suspend fun getEntriesSince(sinceMillis: Long): List<ExerciseOccurrenceDetail>
}

data class ExerciseOccurrenceDetail(
    val exerciseId: Long,
    val exerciseName: String,
    val durationMs: Long,
    val outcome: ExerciseOutcome,
)
