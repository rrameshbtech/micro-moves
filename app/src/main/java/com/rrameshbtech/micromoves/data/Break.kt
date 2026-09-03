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
    val enabled: Boolean = true,
    val state: BreakState = BreakState.Active,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    fun nextTriggerTimeInMins(now: LocalDateTime = LocalDateTime.now()): Long =
        Duration.between(now, schedule.nextOccurrence(now)).toMinutes().coerceAtLeast(0)
}

/** Outcome of one watcher tick's evaluation of a single break. */
data class WatcherTickResult(val updatedBreak: Break? = null, val firedBreakId: Long? = null)

/**
 * Evaluates whether [now] means this break should fire, silently consume one paused slot, or
 * auto-resume — anchored off [Break.updatedAt], which is advanced to [nowMillis] (never to the
 * due slot's own time) whenever a slot is consumed. However long the app was away, the next check
 * is always against a fresh anchor, so a break can fire/consume at most once per tick regardless
 * of how many schedule periods actually elapsed — this is what caps catch-up to "most recent only."
 */
internal fun Break.evaluateForWatcherTick(now: LocalDateTime, nowMillis: Long): WatcherTickResult {
    if (!enabled) return WatcherTickResult()
    val anchor = Instant.ofEpochMilli(updatedAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return when (val currentState = state) {
        is BreakState.PausedUntil ->
            if (nowMillis < currentState.timestampMillis) WatcherTickResult()
            else WatcherTickResult(updatedBreak = copy(state = BreakState.Active, updatedAt = nowMillis))

        is BreakState.PausedForOccurrences -> {
            if (schedule.nextOccurrence(anchor).isAfter(now)) return WatcherTickResult()
            val remaining = currentState.occurrences - 1
            val newState = if (remaining <= 0) BreakState.Active else BreakState.PausedForOccurrences(remaining)
            WatcherTickResult(updatedBreak = copy(state = newState, updatedAt = nowMillis))
        }

        BreakState.Active -> {
            if (schedule.nextOccurrence(anchor).isAfter(now)) return WatcherTickResult()
            WatcherTickResult(updatedBreak = copy(updatedAt = nowMillis), firedBreakId = id)
        }
    }
}
