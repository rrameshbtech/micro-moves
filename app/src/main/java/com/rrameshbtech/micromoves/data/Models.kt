package com.rrameshbtech.micromoves.data

import java.time.DayOfWeek

data class Slide(
    val imageUri: String? = null,
    val durationMs: Long = 3000L,
    val description: String = ""
)

data class DaysOfWeek(val days: Set<DayOfWeek> = DayOfWeek.entries.toSet()) {
    fun contains(day: DayOfWeek): Boolean = day in days
    fun toBitmask(): Int = days.fold(0) { mask, day -> mask or (1 shl day.value) }

    companion object {
        val EVERY_DAY = DaysOfWeek(DayOfWeek.entries.toSet())

        fun fromBitmask(mask: Int): DaysOfWeek =
            DaysOfWeek(DayOfWeek.entries.filter { mask and (1 shl it.value) != 0 }.toSet())
    }
}

data class BreakSchedule(
    val frequencyMinutes: Int = 15,
    val activeStartHour: Int = 9,
    val activeEndHour: Int = 17,
    val daysOfWeek: DaysOfWeek = DaysOfWeek.EVERY_DAY,
)

sealed class BreakState {
    object Active : BreakState()
    data class PausedForOccurrences(val occurrences: Int) : BreakState()
    data class PausedUntil(val timestampMillis: Long) : BreakState()
}

sealed class ExerciseOutcome {
    object Completed : ExerciseOutcome()
    object Skipped : ExerciseOutcome()
    object Paused : ExerciseOutcome()
}

data class ResolvedRoutineStep(
    val exercise: Exercise,
    val position: Int,
    val pauseAfterStep: Boolean,
)

data class BreakRoutine(
    val breakItem: Break,
    val steps: List<ResolvedRoutineStep>,
) {
    val totalDuration: Long get() = steps.sumOf { it.exercise.totalDuration }
}
