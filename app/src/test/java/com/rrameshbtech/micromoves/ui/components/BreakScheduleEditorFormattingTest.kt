package com.rrameshbtech.micromoves.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class BreakScheduleEditorFormattingTest {

    @Test
    fun underOneHour_showsMinutesOnly() {
        assertEquals("30 min", formatFrequencyStepperLabel(30))
    }

    @Test
    fun exactHour_showsHoursOnly() {
        assertEquals("2 hr", formatFrequencyStepperLabel(120))
    }

    @Test
    fun hourWithRemainder_showsHoursAndMinutes() {
        assertEquals("1 hr 30 min", formatFrequencyStepperLabel(90))
    }
}
