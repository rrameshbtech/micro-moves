package com.rrameshbtech.micromoves.data

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime

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

/**
 * Next moment on or after [from] that falls on an active day, within the active hour
 * window, and aligned to a [BreakSchedule.frequencyMinutes] step from the window's start.
 * Searches up to two weeks ahead; falls back to [from] + one step if no active day is found
 * (only possible if [BreakSchedule.daysOfWeek] is empty).
 */
fun BreakSchedule.nextOccurrence(from: LocalDateTime): LocalDateTime {
    val step = frequencyMinutes.toLong().coerceAtLeast(1)
    for (dayOffset in 0..13) {
        val date = from.toLocalDate().plusDays(dayOffset.toLong())
        if (!daysOfWeek.contains(date.dayOfWeek)) continue

        val windowStart = date.atTime(activeStartHour, 0)
        val windowEnd = date.atTime(activeEndHour, 0)
        if (!windowEnd.isAfter(windowStart)) continue

        val earliestOnDay = if (dayOffset == 0) maxOf(windowStart, from) else windowStart
        val minutesPastStart = Duration.between(windowStart, earliestOnDay).toMinutes()
        val stepsPassed = (minutesPastStart + step - 1) / step
        val slot = windowStart.plusMinutes(stepsPassed * step)

        if (!slot.isAfter(windowEnd)) return slot
    }
    return from.plusMinutes(step)
}

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
