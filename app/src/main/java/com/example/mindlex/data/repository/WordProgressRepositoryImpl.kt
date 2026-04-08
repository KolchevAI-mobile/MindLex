package com.example.mindlex.data.repository

import com.example.mindlex.data.local.dao.WordProgressDao
import com.example.mindlex.data.local.entity.WordProgressEntity
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.model.WordProgress
import com.example.mindlex.domain.model.WordStatus
import com.example.mindlex.domain.repository.WordProgressRepository
import com.example.mindlex.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * Реализация репозитория прогресса слов с использованием Room.
 */
class WordProgressRepositoryImpl @Inject constructor(
    private val wordProgressDao: WordProgressDao,
    private val wordRepository: WordRepository
) : WordProgressRepository {

    override suspend fun saveProgress(progress: WordProgress) {
        wordProgressDao.insertOrUpdate(progress.toEntity())
    }

    override suspend fun getProgressForWord(wordId: String): WordProgress? {
        return wordProgressDao.getProgressForWord(wordId)?.toDomain()
    }

    override fun getDueReviews(now: Instant): Flow<List<WordProgress>> {
        return wordProgressDao.getDueReviews(now).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getNewWords(limit: Int): Flow<List<WordProgress>> {
        return wordProgressDao.getNewWords(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getLearningWords(limit: Int): Flow<List<WordProgress>> {
        return wordProgressDao.getLearningWords(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeKnownWordsCount(): Flow<Int> = wordProgressDao.observeKnownWordsCount()

    override fun observeTotalCorrectCount(): Flow<Int> = wordProgressDao.observeTotalCorrectCount()

    override fun observeReviewedWordsCountBetween(start: Instant, end: Instant): Flow<Int> =
        wordProgressDao.observeReviewedWordsCountBetween(start, end)

    override suspend fun countDueReviews(now: Instant): Int = wordProgressDao.countDueReviews(now)

    override suspend fun getWordById(wordId: String): Word? {
        return wordRepository.getWordById(wordId)
    }

    /**
     * Преобразует Entity в доменную модель.
     */
    private fun WordProgressEntity.toDomain(): WordProgress {
        return WordProgress(
            id = this.id,
            wordId = this.wordId,
            status = WordStatus.valueOf(this.status),
            level = this.level,
            easeFactor = this.easeFactor,
            intervalDays = this.intervalDays,
            nextReviewAt = this.nextReviewAt,
            lastReviewedAt = this.lastReviewedAt,
            correctCount = this.correctCount,
            incorrectCount = this.incorrectCount,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    /**
     * Преобразует доменную модель в Entity.
     */
    private fun WordProgress.toEntity(): WordProgressEntity {
        return WordProgressEntity(
            id = this.id,
            wordId = this.wordId,
            status = this.status.name,
            level = this.level,
            easeFactor = this.easeFactor,
            intervalDays = this.intervalDays,
            nextReviewAt = this.nextReviewAt,
            lastReviewedAt = this.lastReviewedAt,
            correctCount = this.correctCount,
            incorrectCount = this.incorrectCount,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
