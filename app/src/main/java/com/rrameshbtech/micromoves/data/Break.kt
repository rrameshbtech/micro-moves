package com.rrameshbtech.micromoves.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breaks")
data class Break(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequency: Int = 15,
    val activeStartHour: Int = 9,
    val activeEndHour: Int = 17,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPaused: Boolean = false,
    val pausedForCycles: Int = 0,
    val minutesUntilNext: Int = 0,
)
