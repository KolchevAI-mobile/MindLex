package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.WordStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.math.max
import kotlin.math.min

class CalculateNextReview {

    operator fun invoke(
        wordId: String,
        currentLevel: Int,
        previousIntervalDays: Int,
        currentEaseFactor: Double,
        quality: Int
    ): ReviewResult {
        val normalizedQuality = quality.coerceIn(1, 5)
        val easeFactor = calculateEaseFactor(currentEaseFactor, normalizedQuality)

        val newLevel = when {
            normalizedQuality >= 5 -> min(currentLevel + 2, 6)
            normalizedQuality >= 3 -> min(currentLevel + 1, 6)
            else -> 0
        }

        val intervalDays = when (normalizedQuality) {
            5 -> if (newLevel >= 4) {
                if (previousIntervalDays <= 0) 30 else max((previousIntervalDays * easeFactor).toInt(), 30)
            } else {
                if (previousIntervalDays <= 0) 7 else max((previousIntervalDays * easeFactor).toInt(), 7)
            }
            4 -> if (previousIntervalDays <= 0) 3 else max((previousIntervalDays * easeFactor).toInt(), 3)
            3 -> if (previousIntervalDays <= 0) 1 else max((previousIntervalDays * easeFactor).toInt(), 1)
            else -> 0
        }

        val nextReviewAt = Clock.System.now()
            .plus(DateTimePeriod(days = intervalDays), TimeZone.currentSystemDefault())

        val newStatus = when {
            newLevel <= 0 -> WordStatus.NEW
            newLevel in 1..2 -> WordStatus.LEARNING
            newLevel >= 5 -> WordStatus.KNOWN
            else -> WordStatus.REVIEW
        }

        return ReviewResult(
            wordId = wordId,
            quality = normalizedQuality,
            nextReviewAt = nextReviewAt,
            newStatus = newStatus,
            newLevel = newLevel,
            newEaseFactor = easeFactor,
            intervalDays = intervalDays
        )
    }

    private fun calculateEaseFactor(current: Double, quality: Int): Double {
        val updated = current + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        return updated.coerceIn(1.3, 2.5)
    }
}
