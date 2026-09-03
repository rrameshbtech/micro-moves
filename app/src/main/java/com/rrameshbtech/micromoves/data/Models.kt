package com.rrameshbtech.micromoves.data

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime

data class Slide(
    val imageUri: String? = null,
    val durationMs: Long = 3000L,
    val description: String = "",
    val subText: String = ""
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
 * How a break alerts the user when it fires. A high-priority notification always fires and isn't
 * represented here — there is nothing to toggle, so no field for it.
 */
data class AlertSettings(
    val chimeEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
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

/**
 * The occurrence at which this break would next fire if its next [skipCount] occurrences
 * (from [from]) were skipped — i.e. the resume point behind a `PausedForOccurrences(skipCount)`.
 */
fun BreakSchedule.resumeOccurrenceAfterSkipping(from: LocalDateTime, skipCount: Int): LocalDateTime {
    var occurrence = nextOccurrence(from)
    repeat(skipCount) {
        occurrence = nextOccurrence(occurrence.plusMinutes(1))
    }
    return occurrence
}

/**
 * Every slot on or after [from] and on or before [to] — used to catch up on however many
 * occurrences fired while the app was away (crash/offline/killed) instead of only the latest one.
 * ponytail: capped at 200 slots to guard against an unbounded loop for a pathological schedule
 * after a very long gap; raise the cap (or page results) if real usage ever hits it.
 */
fun BreakSchedule.occurrencesBetween(from: LocalDateTime, to: LocalDateTime): List<LocalDateTime> {
    val slots = mutableListOf<LocalDateTime>()
    var anchor = from
    while (slots.size < MAX_BACKFILL_OCCURRENCES) {
        val slot = nextOccurrence(anchor)
        if (slot.isAfter(to)) break
        slots.add(slot)
        anchor = slot.plusMinutes(1)
    }
    return slots
}

private const val MAX_BACKFILL_OCCURRENCES = 200

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

/**
 * Resolves a [BreakOccurrence] snapshot's ordered exercise ids against a lookup map, preserving
 * snapshot order and silently dropping any id no longer present (e.g. exercise deleted since the
 * occurrence fired) rather than crashing playback.
 */
internal fun List<Long>.toResolvedSteps(exercisesById: Map<Long, Exercise>): List<ResolvedRoutineStep> =
    mapIndexedNotNull { index, exerciseId ->
        exercisesById[exerciseId]?.let { exercise -> ResolvedRoutineStep(exercise, position = index, pauseAfterStep = false) }
    }

data class BreakRoutine(
    val breakItem: Break,
    val steps: List<ResolvedRoutineStep>,
) {
    val totalDuration: Long get() = steps.sumOf { it.exercise.totalDuration }
}

sealed class BreakPlaybackItem {
    data class ExerciseIntro(val exerciseName: String, val exerciseIndex: Int) : BreakPlaybackItem()
    data class SlideItem(
        val slide: Slide,
        val exerciseIndex: Int,
        val exerciseId: Long,
        val isLastSlideOfExercise: Boolean,
    ) : BreakPlaybackItem()
    data class Summary(val anyExerciseCompleted: Boolean = false) : BreakPlaybackItem()
}

/**
 * Flattens an ordered list of routine steps into a playback list: an intro before every
 * exercise except the first, then that exercise's slides, then a terminal [BreakPlaybackItem.Summary].
 */
fun List<ResolvedRoutineStep>.toPlaybackItems(): List<BreakPlaybackItem> = buildList {
    this@toPlaybackItems.forEachIndexed { exerciseIndex, step ->
        if (exerciseIndex > 0) add(BreakPlaybackItem.ExerciseIntro(step.exercise.name, exerciseIndex))
        step.exercise.slides.forEachIndexed { slideIndex, slide ->
            add(
                BreakPlaybackItem.SlideItem(
                    slide = slide,
                    exerciseIndex = exerciseIndex,
                    exerciseId = step.exercise.id,
                    isLastSlideOfExercise = slideIndex == step.exercise.slides.lastIndex,
                )
            )
        }
    }
    add(BreakPlaybackItem.Summary())
}
