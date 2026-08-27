package com.rrameshbtech.micromoves.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val slides: List<Slide> = emptyList(),
    @Embedded(prefix = "suggested_") val suggestedSchedule: BreakSchedule = BreakSchedule(),
) {
    val totalDuration: Long get() = slides.sumOf { it.durationMs }
}
