package com.example.mindlex.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardStreakPolicyTest {

    private val today = LocalDate(2026, 4, 27)

    @Test
    fun `null or bad date returns zero`() {
        assertEquals(0, resolveVisibleDashboardStreak(5, null, today))
        assertEquals(0, resolveVisibleDashboardStreak(5, "not-a-date", today))
    }

    @Test
    fun `today keeps stored streak`() {
        assertEquals(7, resolveVisibleDashboardStreak(7, "2026-04-27", today))
    }

    @Test
    fun `yesterday keeps stored streak`() {
        val y = today.minus(DatePeriod(days = 1))
        assertEquals(4, resolveVisibleDashboardStreak(4, y.toString(), today))
    }

    @Test
    fun `older than yesterday shows zero`() {
        val old = today.minus(DatePeriod(days = 2))
        assertEquals(0, resolveVisibleDashboardStreak(9, old.toString(), today))
    }
}
