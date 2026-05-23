package com.rrameshbtech.micromoves.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rrameshbtech.micromoves.data.Break
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

    @Query("SELECT COUNT(*) FROM breaks")
    suspend fun getBreakCount(): Int

    @Query("DELETE FROM breaks WHERE id = :id")
    suspend fun deleteBreak(id: Long)
}
