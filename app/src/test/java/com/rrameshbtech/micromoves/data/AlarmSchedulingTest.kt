package com.rrameshbtech.micromoves.data

import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmSchedulingTest {

    private val schedule = BreakSchedule(frequencyMinutes = 15, activeStartHour = 9, activeEndHour = 17)

    private fun millisAt(dateTime: LocalDateTime): Long =
        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun breakAt(id: Long, updatedAt: LocalDateTime, state: BreakState = BreakState.Active) = Break(
        id = id,
        name = "Test Break $id",
        schedule = schedule,
        state = state,
        updatedAt = millisAt(updatedAt),
    )

    @Test
    fun active_wakeTimeIsNextOccurrenceFromNow() {
        val breakItem = breakAt(1, LocalDateTime.of(2024, 1, 2, 9, 3))
        val now = LocalDateTime.of(2024, 1, 2, 10, 7)

        assertEquals(schedule.nextOccurrence(now), breakItem.nextAlarmWakeTime(now))
    }

    @Test
    fun pausedUntil_wakeTimeIsTheTimestamp() {
        val timestamp = LocalDateTime.of(2024, 1, 2, 15, 0)
        val breakItem = breakAt(
            2,
            LocalDateTime.of(2024, 1, 2, 10, 0),
            state = BreakState.PausedUntil(millisAt(timestamp)),
        )
        val now = LocalDateTime.of(2024, 1, 2, 11, 0)

        assertEquals(timestamp, breakItem.nextAlarmWakeTime(now))
    }

    @Test
    fun pausedForOccurrences_wakeTimeAnchorsOffUpdatedAtNotNow() {
        val anchor = LocalDateTime.of(2024, 1, 2, 9, 3)
        val breakItem = breakAt(3, anchor, state = BreakState.PausedForOccurrences(2))
        val now = LocalDateTime.of(2030, 1, 1, 0, 0)

        assertEquals(schedule.resumeOccurrenceAfterSkipping(anchor, 2), breakItem.nextAlarmWakeTime(now))
    }

    @Test
    fun earliestAlarmWakeTime_returnsTheMinimumAcrossBreaks() {
        val now = LocalDateTime.of(2024, 1, 2, 9, 0)
        val earliestTimestamp = LocalDateTime.of(2024, 1, 2, 8, 50)
        val breaks = listOf(
            breakAt(1, now), // Active: nextOccurrence(now) == 09:00
            breakAt(2, now, state = BreakState.PausedUntil(millisAt(earliestTimestamp))), // 08:50, the minimum
            breakAt(3, LocalDateTime.of(2024, 1, 2, 9, 3), state = BreakState.PausedForOccurrences(1)), // 09:30
        )

        assertEquals(earliestTimestamp, breaks.earliestAlarmWakeTime(now))
    }

    @Test
    fun earliestAlarmWakeTime_emptyList_isNull() {
        assertNull(emptyList<Break>().earliestAlarmWakeTime(LocalDateTime.of(2024, 1, 2, 9, 0)))
    }

    @Test
    fun nextOccurrence_withNonZeroSeconds_isStrictlyAfterFrom() {
        // Regression test: LocalDateTime.now() almost never lands on :00 seconds. Truncating to
        // whole minutes here used to silently drop those seconds and floor to the step boundary
        // already passed (09:30 here), which is at-or-before `from` — rearm() would then clamp the
        // "next" alarm to right now and refire in a tight loop instead of waiting for the real next
        // slot (09:45).
        val from = LocalDateTime.of(2024, 1, 2, 9, 30, 5)

        val next = schedule.nextOccurrence(from)

        assertTrue(next.isAfter(from))
        assertEquals(LocalDateTime.of(2024, 1, 2, 9, 45), next)
    }
}
