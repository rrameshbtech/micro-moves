package com.rrameshbtech.micromoves.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.LocalDateTime

@Entity(tableName = "breaks")
data class Break(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @Embedded val schedule: BreakSchedule = BreakSchedule(),
    val enabled: Boolean = true,
    val state: BreakState = BreakState.Active,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    fun nextTriggerTimeInMins(now: LocalDateTime = LocalDateTime.now()): Long =
        Duration.between(now, schedule.nextOccurrence(now)).toMinutes().coerceAtLeast(0)
}
