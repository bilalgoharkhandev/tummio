package com.example.reminder

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class SmartReminderPolicyTest {
    @Test fun intervalSuggestionsAreAgeAwareButNotMedicalTargets() {
        assertEquals(150, SmartReminderManager.ageStartingPoint(3, ReminderKind.FEED))
        assertEquals(180, SmartReminderManager.ageStartingPoint(60, ReminderKind.FEED))
        assertEquals(240, SmartReminderManager.ageStartingPoint(210, ReminderKind.FEED))
        assertEquals(120, SmartReminderManager.ageStartingPoint(15, ReminderKind.SLEEP))
        assertEquals(180, SmartReminderManager.ageStartingPoint(15, ReminderKind.DIAPER))
    }

    @Test fun fixedTimesRejectInvalidClockInput() {
        assertTrue(SmartReminderManager.validateTimes("08:00, 12:30"))
        assertTrue(SmartReminderManager.validateTimes(""))
        assertFalse(SmartReminderManager.validateTimes("25:00"))
        assertFalse(SmartReminderManager.validateTimes("8pm"))
        assertFalse(SmartReminderManager.validateTimes("12:89"))
    }

    @Test fun nextFixedTimeAlwaysReturnsFutureLocalTime() {
        val start = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 6, 8, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val due = SmartReminderManager.nextFixedTime("08:00, 09:00, 22:30", start)!!
        val expected = Calendar.getInstance().apply {
            timeInMillis = start
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
        assertEquals(expected, due)
        assertTrue(due > start)
    }

    @Test fun fixedTimesWrapToNextDay() {
        val start = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 6, 23, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val due = SmartReminderManager.nextFixedTime("08:00", start)!!
        assertTrue(due > start)
    }
}
