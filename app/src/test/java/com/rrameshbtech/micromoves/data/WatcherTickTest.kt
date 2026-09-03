package com.rrameshbtech.micromoves.data

import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WatcherTickTest {

    private val schedule = BreakSchedule(frequencyMinutes = 15, activeStartHour = 9, activeEndHour = 17)

    private fun millisAt(dateTime: LocalDateTime): Long =
        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun breakAt(updatedAt: LocalDateTime, state: BreakState = BreakState.Active) = Break(
        id = 42,
        name = "Test Break",
        schedule = schedule,
        state = state,
        updatedAt = millisAt(updatedAt),
    )

    @Test
    fun notYetDue_isNoOp() {
        val breakItem = breakAt(LocalDateTime.of(2024, 1, 2, 10, 3))
        val now = LocalDateTime.of(2024, 1, 2, 10, 10)

        val result = breakItem.evaluateForWatcherTick(now, millisAt(now))

        assertNull(result.updatedBreak)
        assertTrue(result.firedOccurrences.isEmpty())
    }

    @Test
    fun due_firesAndReanchors() {
        val breakItem = breakAt(LocalDateTime.of(2024, 1, 2, 10, 3))
        val now = LocalDateTime.of(2024, 1, 2, 10, 20)
        val nowMillis = millisAt(now)

        val result = breakItem.evaluateForWatcherTick(now, nowMillis)

        assertEquals(listOf(millisAt(LocalDateTime.of(2024, 1, 2, 10, 15))), result.firedOccurrences)
        assertEquals(nowMillis, result.updatedBreak?.updatedAt)
        assertEquals(BreakState.Active, result.updatedBreak?.state)
    }

    @Test
    fun staleByLongAbsence_firesForEveryMissedSlot() {
        val breakItem = breakAt(LocalDateTime.of(2024, 1, 2, 9, 3))
        val now = LocalDateTime.of(2024, 1, 2, 10, 7)
        val nowMillis = millisAt(now)

        val first = breakItem.evaluateForWatcherTick(now, nowMillis)
        val expectedSlots = listOf(15, 30, 45, 60)
            .map { minutesPastNine -> LocalDateTime.of(2024, 1, 2, 9, 0).plusMinutes(minutesPastNine.toLong()) }
            .map(::millisAt)
        assertEquals(expectedSlots, first.firedOccurrences)
        val updated = requireNotNull(first.updatedBreak)

        val second = updated.evaluateForWatcherTick(now, nowMillis)
        assertNull(second.updatedBreak)
        assertTrue(second.firedOccurrences.isEmpty())
    }

    @Test
    fun pausedUntil_beforeTimestamp_isNoOp() {
        val timestamp = LocalDateTime.of(2024, 1, 2, 15, 0)
        val breakItem = breakAt(
            LocalDateTime.of(2024, 1, 2, 10, 0),
            state = BreakState.PausedUntil(millisAt(timestamp)),
        )
        val now = LocalDateTime.of(2024, 1, 2, 14, 0)

        val result = breakItem.evaluateForWatcherTick(now, millisAt(now))

        assertNull(result.updatedBreak)
        assertTrue(result.firedOccurrences.isEmpty())
    }

    @Test
    fun pausedUntil_atOrAfterTimestamp_resumesToActive() {
        val timestamp = LocalDateTime.of(2024, 1, 2, 14, 0)
        val breakItem = breakAt(
            LocalDateTime.of(2024, 1, 2, 10, 0),
            state = BreakState.PausedUntil(millisAt(timestamp)),
        )
        val now = timestamp
        val nowMillis = millisAt(now)

        val result = breakItem.evaluateForWatcherTick(now, nowMillis)

        assertEquals(BreakState.Active, result.updatedBreak?.state)
        assertEquals(nowMillis, result.updatedBreak?.updatedAt)
        assertTrue(result.firedOccurrences.isEmpty())
    }

    @Test
    fun pausedForOccurrences_dueSlot_decrementsWithoutFiring() {
        val breakItem = breakAt(
            LocalDateTime.of(2024, 1, 2, 10, 3),
            state = BreakState.PausedForOccurrences(2),
        )
        val now = LocalDateTime.of(2024, 1, 2, 10, 20)

        val result = breakItem.evaluateForWatcherTick(now, millisAt(now))

        assertEquals(BreakState.PausedForOccurrences(1), result.updatedBreak?.state)
        assertTrue(result.firedOccurrences.isEmpty())
    }

    @Test
    fun pausedForOccurrences_reachesZero_becomesActive() {
        val breakItem = breakAt(
            LocalDateTime.of(2024, 1, 2, 10, 3),
            state = BreakState.PausedForOccurrences(1),
        )
        val now = LocalDateTime.of(2024, 1, 2, 10, 20)

        val result = breakItem.evaluateForWatcherTick(now, millisAt(now))

        assertEquals(BreakState.Active, result.updatedBreak?.state)
        assertTrue(result.firedOccurrences.isEmpty())
    }

    @Test
    fun pausedForOccurrences_exhaustedByLongAbsence_firesLeftoverSlots() {
        val breakItem = breakAt(
            LocalDateTime.of(2024, 1, 2, 9, 3),
            state = BreakState.PausedForOccurrences(2),
        )
        val now = LocalDateTime.of(2024, 1, 2, 10, 7)

        val result = breakItem.evaluateForWatcherTick(now, millisAt(now))

        // Missed slots are 09:15, 09:30, 09:45, 10:00 — the first 2 consume the pause silently,
        // the remaining 2 have already elapsed while active and must be backfilled, not dropped.
        assertEquals(BreakState.Active, result.updatedBreak?.state)
        val expectedFired = listOf(45, 60)
            .map { minutesPastNine -> LocalDateTime.of(2024, 1, 2, 9, 0).plusMinutes(minutesPastNine.toLong()) }
            .map(::millisAt)
        assertEquals(expectedFired, result.firedOccurrences)
    }

    @Test
    fun disabledBreak_alwaysNoOp() {
        val breakItem = breakAt(LocalDateTime.of(2020, 1, 1, 0, 0)).copy(enabled = false)
        val now = LocalDateTime.of(2024, 1, 2, 14, 7)

        val result = breakItem.evaluateForWatcherTick(now, millisAt(now))

        assertNull(result.updatedBreak)
        assertTrue(result.firedOccurrences.isEmpty())
    }
}
