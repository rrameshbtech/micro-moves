package com.rrameshbtech.micromoves.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rrameshbtech.micromoves.data.ActiveBreak
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.Slide
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Break entities.
 * Handles all database operations for Break definitions.
 */
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

    @Query("SELECT * FROM breaks WHERE enabled = 1 ORDER BY name ASC")
    fun getAllEnabledBreaks(): Flow<List<Break>>

    @Query("SELECT * FROM breaks ORDER BY name ASC")
    fun getAllBreaks(): Flow<List<Break>>

    @Query("SELECT COUNT(*) FROM breaks")
    suspend fun getBreakCount(): Int

    @Query("DELETE FROM breaks WHERE id = :id")
    suspend fun deleteBreak(id: Long)
}

/**
 * Data Access Object for Slide entities.
 * Handles all database operations for Slides.
 */
@Dao
interface SlideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(slide: Slide): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slides: List<Slide>)

    @Update
    suspend fun update(slide: Slide)

    @Delete
    suspend fun delete(slide: Slide)

    @Query("SELECT * FROM slides WHERE id = :id LIMIT 1")
    suspend fun getSlideById(id: Long): Slide?

    @Query("SELECT * FROM slides WHERE breakId = :breakId ORDER BY `order` ASC")
    suspend fun getSlidesByBreakId(breakId: Long): List<Slide>

    @Query("SELECT * FROM slides WHERE breakId = :breakId ORDER BY `order` ASC")
    fun getSlidesByBreakIdFlow(breakId: Long): Flow<List<Slide>>

    @Query("DELETE FROM slides WHERE breakId = :breakId")
    suspend fun deleteSlidesByBreakId(breakId: Long)

    @Query("DELETE FROM slides WHERE id = :id")
    suspend fun delete(id: Long)
}

/**
 * Data Access Object for ActiveBreak entities.
 * Handles all database operations for tracking active breaks state.
 */
@Dao
interface ActiveBreakDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activeBreak: ActiveBreak)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activeBreaks: List<ActiveBreak>)

    @Update
    suspend fun update(activeBreak: ActiveBreak)

    @Delete
    suspend fun delete(activeBreak: ActiveBreak)

    @Query("SELECT * FROM active_breaks WHERE breakId = :breakId LIMIT 1")
    suspend fun getActiveBreakById(breakId: Long): ActiveBreak?

    @Query("SELECT * FROM active_breaks WHERE breakId = :breakId LIMIT 1")
    fun getActiveBreakByIdFlow(breakId: Long): Flow<ActiveBreak?>

    @Query("SELECT * FROM active_breaks ORDER BY breakName ASC")
    fun getAllActiveBreaks(): Flow<List<ActiveBreak>>

    @Query("SELECT * FROM active_breaks WHERE isPaused = 0 ORDER BY breakName ASC")
    fun getAllUnpausedActiveBreaks(): Flow<List<ActiveBreak>>

    @Query("SELECT * FROM active_breaks WHERE isPaused = 1 ORDER BY breakName ASC")
    fun getAllPausedActiveBreaks(): Flow<List<ActiveBreak>>

    @Query("UPDATE active_breaks SET isPaused = :isPaused, pausedForCycles = :cycles, updatedAt = :timestamp WHERE breakId = :breakId")
    suspend fun updatePauseState(breakId: Long, isPaused: Boolean, cycles: Int, timestamp: Long)

    @Query("UPDATE active_breaks SET minutesUntilNext = :minutes, lastTriggeredAt = :timestamp, triggerCount = triggerCount + 1 WHERE breakId = :breakId")
    suspend fun updateTriggerInfo(breakId: Long, minutes: Int, timestamp: Long)

    @Query("DELETE FROM active_breaks WHERE breakId = :breakId")
    suspend fun delete(breakId: Long)
}


