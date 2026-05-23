package com.rrameshbtech.micromoves.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breaks")
data class Break(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @Embedded val schedule: BreakSchedule = BreakSchedule(),
    val routine: BreakRoutine = BreakRoutine(),
    val state: BreakState = BreakState.Active,
    val nextTriggerTime: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
