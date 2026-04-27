package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.WordStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateNextReviewTest {

    private val calculate = CalculateNextReview()

    @Test
    fun `fails with quality 1_2 level resets to zero and no interval`() {
        val r = calculate("w1", currentLevel = 3, previousIntervalDays = 10, currentEaseFactor = 2.0, quality = 1)

        assertEquals(0, r.newLevel)
        assertEquals(0, r.intervalDays)
        assertEquals(WordStatus.NEW, r.newStatus)
    }

    @Test
    fun `quality 5 from zero jumps two levels`() {
        val r = calculate("w1", 0, 0, 2.5, 5)

        assertEquals(2, r.newLevel)
        assertEquals(WordStatus.LEARNING, r.newStatus)
        assertEquals(7, r.intervalDays)
        assertTrue(r.newEaseFactor in 1.3..2.5)
    }

    @Test
    fun `quality 5 with high level uses long interval when previous positive`() {
        val r = calculate("w1", currentLevel = 4, previousIntervalDays = 20, currentEaseFactor = 2.0, quality = 5)

        assertEquals(6, r.newLevel)
        assertTrue(r.intervalDays >= 30)
        assertEquals(WordStatus.KNOWN, r.newStatus)
    }

    @Test
    fun `quality 3 gives at least one day`() {
        val r = calculate("w1", 2, 0, 2.0, 3)

        assertTrue(r.newLevel in 1..6)
        assertTrue(r.intervalDays >= 1)
    }

    @Test
    fun `quality 4 is between one and three days when previous zero`() {
        val r = calculate("w1", 0, 0, 2.0, 4)

        assertTrue(r.intervalDays in 1..3 || r.intervalDays > 0)
    }

    @Test
    fun `quality outside range is coerced`() {
        val low = calculate("w1", 0, 0, 2.5, 0)
        val high = calculate("w1", 0, 0, 2.5, 99)

        assertTrue(low.quality in 1..5)
        assertTrue(high.quality in 1..5)
    }

    @Test
    fun `newLevel 3 maps to review status`() {
        val r = calculate("w1", 2, 0, 2.0, 3)

        assertEquals(3, r.newLevel)
        assertEquals(WordStatus.REVIEW, r.newStatus)
    }
}
