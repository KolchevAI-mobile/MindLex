package com.example.mindlex.domain.usecase

import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateRecommendedTimesTest {

    private val calc = CalculateRecommendedTimes()

    @Test
    fun `low goal single session at preferred time`() {
        val t = LocalTime(20, 30)
        assertEquals(listOf(t), calc(t, dailyGoal = 5))
        assertEquals(listOf(t), calc(t, dailyGoal = 10))
    }

    @Test
    fun `medium goal two sessions`() {
        val t = LocalTime(12, 0)
        val r = calc(t, dailyGoal = 15)
        assertEquals(2, r.size)
        assertEquals(LocalTime(8, 0), r[0])
        assertEquals(t, r[1])
    }

    @Test
    fun `high goal three sessions`() {
        val t = LocalTime(14, 0)
        val r = calc(t, dailyGoal = 30)
        assertEquals(3, r.size)
        assertEquals(LocalTime(10, 0), r[0])
        assertEquals(t, r[1])
        assertEquals(LocalTime(18, 0), r[2])
    }

    @Test
    fun `wraps midnight when shifting from early morning`() {
        val t = LocalTime(2, 0)
        val r = calc(t, dailyGoal = 30)
        assertEquals(LocalTime(22, 0), r[0])
    }
}
