package com.example.mindlex.domain.usecase

import org.junit.Assert.assertEquals
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
}
