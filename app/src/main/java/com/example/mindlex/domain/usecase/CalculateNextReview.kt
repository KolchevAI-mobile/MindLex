package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.WordStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlin.math.min

/**
 * Use case для расчёта следующего повторения слова.
 *
 * Алгоритм интервального повторения (упрощённый SM-2):
 * - Качество 1-2 (плохо): уровень сбрасывается, повторить сегодня
 * - Качество 3 (нормально): уровень +1, интервал 1 день
 * - Качество 4 (хорошо): уровень +1, интервал 3 дня
 * - Качество 5 (отлично): уровень +2, интервал 7 дней
 *
 * Максимальный уровень: 5 (слово "выучено", интервал 30 дней)
 */
class CalculateNextReview {

    /**
     * Рассчитывает следующее повторение на основе текущего уровня и качества ответа.
     *
     * @param wordId ID слова
     * @param currentLevel Текущий уровень (0-5)
     * @param quality Качество ответа (1-5)
     * @return Результат с новым статусом и временем следующего повторения
     */
    operator fun invoke(
        wordId: String,
        currentLevel: Int,
        quality: Int
    ): ReviewResult {
        val (newLevel, intervalDays) = when {
            quality <= 2 -> Pair(0, 0)           // Сброс, повторить сегодня
            quality == 3 -> Pair(currentLevel + 1, 1)  // 1 день
            quality == 4 -> Pair(currentLevel + 1, 3)  // 3 дня
            quality >= 5 -> Pair(
                min(currentLevel + 2, 5),  // Макс. уровень 5
                if (currentLevel >= 4) 30 else 7  // 7 или 30 дней
            )
            else -> Pair(currentLevel, 1)
        }

        val nextReviewAt = Clock.System.now()
            .plus(DateTimePeriod(days = intervalDays), TimeZone.currentSystemDefault())

        val newStatus = when {
            newLevel == 0 -> WordStatus.NEW
            newLevel <= 2 -> WordStatus.LEARNING
            newLevel >= 5 -> WordStatus.KNOWN
            else -> WordStatus.REVIEW
        }

        return ReviewResult(
            wordId = wordId,
            quality = quality,
            nextReviewAt = nextReviewAt,
            newStatus = newStatus
        )
    }
}
