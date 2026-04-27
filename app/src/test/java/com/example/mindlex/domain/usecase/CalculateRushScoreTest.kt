package com.example.mindlex.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateRushScoreTest {

    private val calculateRushScore = CalculateRushScore()

    @Test
    fun `returns base points when no combo and no time bonus`() {
        val result = calculateRushScore(comboAfterCorrect = 0, timeRemainingSeconds = 10)

        assertEquals(10, result)
    }

    @Test
    fun `applies combo multiplier and time bonus`() {
        val result = calculateRushScore(comboAfterCorrect = 10, timeRemainingSeconds = 45)

        assertEquals(24, result)
    }

    @Test
    fun `returns expected multiplier labels for score ui`() {
        assertEquals(1.5, calculateRushScore.comboMultiplier(comboAfterCorrect = 5), 0.0)
        assertEquals(2.0, calculateRushScore.comboMultiplier(comboAfterCorrect = 10), 0.0)
        assertEquals(3.0, calculateRushScore.comboMultiplier(comboAfterCorrect = 20), 0.0)
    }

    @Test
    fun `no time bonus at or below 30 seconds`() {
        val without = calculateRushScore(5, 30)
        val with = calculateRushScore(5, 31)
        assertTrue(with > without)
    }

    @Test
    fun `at least one point`() {
        assertTrue(calculateRushScore(0, 0) >= 1)
    }
}
