package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.WordProgress
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Use case для обновления прогресса слова после ответа.
 */
class UpdateWordProgress @Inject constructor(
    private val progressRepository: WordProgressRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateNextReview: CalculateNextReview
) {

    /**
     * Сохраняет результат повторения в базу данных.
     *
     * @param result Результат повторения
     * @return Результат операции
     */
    suspend operator fun invoke(result: ReviewResult): Result<Unit> {
        return try {
            val now = Clock.System.now()
            val existingProgress = progressRepository.getProgressForWord(result.wordId)
            val srs = calculateNextReview(
                wordId = result.wordId,
                currentLevel = existingProgress?.level ?: 0,
                previousIntervalDays = existingProgress?.intervalDays ?: 0,
                currentEaseFactor = existingProgress?.easeFactor ?: 2.5,
                quality = result.quality
            )

            val newProgress = WordProgress(
                id = existingProgress?.id ?: java.util.UUID.randomUUID().toString(),
                wordId = result.wordId,
                status = srs.newStatus,
                level = srs.newLevel,
                easeFactor = srs.newEaseFactor,
                intervalDays = srs.intervalDays,
                nextReviewAt = srs.nextReviewAt,
                lastReviewedAt = now,
                correctCount = (existingProgress?.correctCount ?: 0) + if (result.quality >= 3) 1 else 0,
                incorrectCount = (existingProgress?.incorrectCount ?: 0) + if (result.quality < 3) 1 else 0,
                createdAt = existingProgress?.createdAt ?: now,
                updatedAt = now
            )

            progressRepository.saveProgress(newProgress)
            updateStreak(now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateStreak(now: kotlinx.datetime.Instant) {
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val lastDateRaw = settingsRepository.getLastStudyDate().firstOrNull()
        val lastDate = lastDateRaw?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val currentStreak = settingsRepository.getCurrentStreak().firstOrNull() ?: 0

        if (lastDate == today) return

        val yesterday = today.minus(DatePeriod(days = 1))
        val nextStreak = if (lastDate == yesterday) currentStreak + 1 else 1
        settingsRepository.setCurrentStreak(nextStreak)
        settingsRepository.setLastStudyDate(today.toString())
    }
}
