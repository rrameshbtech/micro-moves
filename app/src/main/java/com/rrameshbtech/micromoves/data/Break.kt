package com.rrameshbtech.micromoves.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Entity(tableName = "breaks")
data class Break(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @Embedded val schedule: BreakSchedule = BreakSchedule(),
    @Embedded val alertSettings: AlertSettings = AlertSettings(),
    val enabled: Boolean = true,
    val state: BreakState = BreakState.Active,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    fun nextTriggerTimeInMins(now: LocalDateTime = LocalDateTime.now()): Long =
        Duration.between(now, schedule.nextOccurrence(now)).toMinutes().coerceAtLeast(0)
}

/** Outcome of one watcher tick's evaluation of a single break. */
data class WatcherTickResult(val updatedBreak: Break? = null, val firedOccurrences: List<Long> = emptyList())

/**
 * Evaluates whether [now] means this break should fire, silently consume paused slots, or
 * auto-resume — anchored off [Break.updatedAt], which is advanced to [nowMillis] (never to a due
 * slot's own time) whenever slots are consumed. Every slot that elapsed since the anchor is
 * accounted for (not just the most recent one), so however long the app was away
 * (crash/offline/killed), reopening it backfills every missed occurrence instead of silently
 * dropping all but the latest. For [BreakState.PausedForOccurrences], slots consume the pause
 * count first; any slots left over once the pause is exhausted are reported as fired, same as
 * [BreakState.Active].
 */
internal fun Break.evaluateForWatcherTick(now: LocalDateTime, nowMillis: Long): WatcherTickResult {
    if (!enabled) return WatcherTickResult()
    val anchor = Instant.ofEpochMilli(updatedAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return when (val currentState = state) {
        is BreakState.PausedUntil ->
            if (nowMillis < currentState.timestampMillis) WatcherTickResult()
            else WatcherTickResult(updatedBreak = copy(state = BreakState.Active, updatedAt = nowMillis))

        is BreakState.PausedForOccurrences -> {
            val missedSlots = schedule.occurrencesBetween(anchor, now)
            if (missedSlots.isEmpty()) return WatcherTickResult()
            val consumed = minOf(missedSlots.size, currentState.occurrences)
            val remaining = currentState.occurrences - consumed
            val newState = if (remaining <= 0) BreakState.Active else BreakState.PausedForOccurrences(remaining)
            WatcherTickResult(
                updatedBreak = copy(state = newState, updatedAt = nowMillis),
                firedOccurrences = missedSlots.drop(consumed).toEpochMillis(),
            )
        }

        BreakState.Active -> {
            val missedSlots = schedule.occurrencesBetween(anchor, now)
            if (missedSlots.isEmpty()) return WatcherTickResult()
            WatcherTickResult(updatedBreak = copy(updatedAt = nowMillis), firedOccurrences = missedSlots.toEpochMillis())
        }
    }
}

private fun List<LocalDateTime>.toEpochMillis(): List<Long> =
    map { it.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }

/**
 * The next moment this break needs re-evaluating — used to arm a single background alarm, not to
 * decide whether it's currently due (that's [evaluateForWatcherTick]'s job once the alarm fires).
 * [BreakState.PausedForOccurrences] anchors off [Break.updatedAt] (via
 * [resumeOccurrenceAfterSkipping], the same helper used to compute when a pause is exhausted) —
 * anchoring off [now] instead would under-count slots already elapsed toward exhausting the pause.
 */
internal fun Break.nextAlarmWakeTime(now: LocalDateTime): LocalDateTime = when (val currentState = state) {
    BreakState.Active -> schedule.nextOccurrence(now)
    is BreakState.PausedUntil -> Instant.ofEpochMilli(currentState.timestampMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    is BreakState.PausedForOccurrences -> {
        val anchor = Instant.ofEpochMilli(updatedAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
        schedule.resumeOccurrenceAfterSkipping(anchor, currentState.occurrences)
    }
}

/** The single earliest wake time across every break, or null if there are none to wake for. */
internal fun List<Break>.earliestAlarmWakeTime(now: LocalDateTime): LocalDateTime? =
    minOfOrNull { it.nextAlarmWakeTime(now) }
