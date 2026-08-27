package com.rrameshbtech.micromoves.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_occurrences",
    foreignKeys = [
        ForeignKey(entity = BreakOccurrence::class, parentColumns = ["id"], childColumns = ["breakOccurrenceId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Exercise::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("breakOccurrenceId"), Index("exerciseId")],
)
data class ExerciseOccurrence(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val breakOccurrenceId: Long,
    val exerciseId: Long,
    val position: Int,
    val outcome: ExerciseOutcome = ExerciseOutcome.Completed,
    val durationMs: Long = 0L,
)
