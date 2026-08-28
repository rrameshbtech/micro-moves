package com.rrameshbtech.micromoves.ui.screens

import com.rrameshbtech.micromoves.data.BreakSchedule
import com.rrameshbtech.micromoves.data.DaysOfWeek
import com.rrameshbtech.micromoves.ui.components.toggling
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomizeBreaksFormattingTest {

    @Test
    fun everyDaySchedule_describesFrequencyHoursAndEveryDay() {
        val schedule = BreakSchedule(frequencyMinutes = 15, activeStartHour = 9, activeEndHour = 19)

        assertEquals("Every day • 9 AM–7 PM, every 15 min", formatScheduleSubtext(schedule))
    }

    @Test
    fun weekdaySchedule_describesAsWeekdays() {
        val schedule = BreakSchedule(
            frequencyMinutes = 45,
            activeStartHour = 9,
            activeEndHour = 19,
            daysOfWeek = DaysOfWeek(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)),
        )

        assertEquals("Weekdays • 9 AM–7 PM, every 45 min", formatScheduleSubtext(schedule))
    }

    @Test
    fun singleDaySchedule_describesAsThatDayOnly() {
        val schedule = BreakSchedule(
            frequencyMinutes = 30,
            activeStartHour = 6,
            activeEndHour = 9,
            daysOfWeek = DaysOfWeek(setOf(DayOfWeek.THURSDAY)),
        )

        assertEquals("Thursdays only • 6–9 AM, every 30 min", formatScheduleSubtext(schedule))
    }

    @Test
    fun arbitrarySubsetSchedule_listsAbbreviatedDays() {
        val schedule = BreakSchedule(
            frequencyMinutes = 60,
            activeStartHour = 0,
            activeEndHour = 23,
            daysOfWeek = DaysOfWeek(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
        )

        assertEquals("Mon, Wed, Fri • every 1 hr", formatScheduleSubtext(schedule))
    }

    @Test
    fun toggling_addsDayWhenAbsent() {
        val days = DaysOfWeek(emptySet())

        assertTrue(days.toggling(DayOfWeek.MONDAY).contains(DayOfWeek.MONDAY))
    }

    @Test
    fun toggling_removesDayWhenPresent() {
        val days = DaysOfWeek(setOf(DayOfWeek.MONDAY))

        assertFalse(days.toggling(DayOfWeek.MONDAY).contains(DayOfWeek.MONDAY))
    }
}
