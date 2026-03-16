package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.WordProgress
import com.example.mindlex.domain.repository.WordProgressRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * Use case для обновления прогресса слова после ответа.
 */
class UpdateWordProgress @Inject constructor(
    private val progressRepository: WordProgressRepository
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

            val newProgress = WordProgress(
                id = existingProgress?.id ?: java.util.UUID.randomUUID().toString(),
                wordId = result.wordId,
                status = result.newStatus,
                level = result.newStatus.level,
                nextReviewAt = result.nextReviewAt,
                lastReviewedAt = now,
                correctCount = (existingProgress?.correctCount ?: 0) + if (result.quality >= 3) 1 else 0,
                incorrectCount = (existingProgress?.incorrectCount ?: 0) + if (result.quality < 3) 1 else 0,
                createdAt = existingProgress?.createdAt ?: now,
                updatedAt = now
            )

            progressRepository.saveProgress(newProgress)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
