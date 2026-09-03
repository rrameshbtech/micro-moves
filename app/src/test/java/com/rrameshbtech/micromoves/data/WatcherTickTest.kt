package com.rrameshbtech.micromoves.data

import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        assertNull(result.firedBreakId)
    }

    @Test
    fun due_firesAndReanchors() {
        val breakItem = breakAt(LocalDateTime.of(2024, 1, 2, 10, 3))
        val now = LocalDateTime.of(2024, 1, 2, 10, 20)
        val nowMillis = millisAt(now)

        val result = breakItem.evaluateForWatcherTick(now, nowMillis)

        assertEquals(42L, result.firedBreakId)
        assertEquals(nowMillis, result.updatedBreak?.updatedAt)
        assertEquals(BreakState.Active, result.updatedBreak?.state)
    }

    @Test
    fun staleByLongAbsence_firesExactlyOnce() {
        val breakItem = breakAt(LocalDateTime.of(2024, 1, 1, 10, 3))
        val now = LocalDateTime.of(2024, 1, 2, 14, 7)
        val nowMillis = millisAt(now)

        val first = breakItem.evaluateForWatcherTick(now, nowMillis)
        assertEquals(42L, first.firedBreakId)
        val updated = requireNotNull(first.updatedBreak)

        val second = updated.evaluateForWatcherTick(now, nowMillis)
        assertNull(second.updatedBreak)
        assertNull(second.firedBreakId)
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
        assertNull(result.firedBreakId)
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
        assertNull(result.firedBreakId)
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
        assertNull(result.firedBreakId)
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
        assertNull(result.firedBreakId)
    }

    @Test
    fun disabledBreak_alwaysNoOp() {
        val breakItem = breakAt(LocalDateTime.of(2020, 1, 1, 0, 0)).copy(enabled = false)
        val now = LocalDateTime.of(2024, 1, 2, 14, 7)

        val result = breakItem.evaluateForWatcherTick(now, millisAt(now))

        assertNull(result.updatedBreak)
        assertNull(result.firedBreakId)
    }
}
