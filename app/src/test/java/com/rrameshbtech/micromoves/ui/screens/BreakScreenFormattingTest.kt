package com.rrameshbtech.micromoves.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class BreakScreenFormattingTest {

    @Test
    fun formatCountdown_roundsDownToWholeSeconds() {
        assertEquals("0:20", formatCountdown(20_500))
    }

    @Test
    fun formatCountdown_padsSecondsUnderTen() {
        assertEquals("1:05", formatCountdown(65_000))
    }

    @Test
    fun formatCountdown_flooredAtZero() {
        assertEquals("0:00", formatCountdown(-100))
    }
}
