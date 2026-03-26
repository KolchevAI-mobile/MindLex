package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.model.WordProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * Репозиторий для управления прогрессом изучения слов.
 */
interface WordProgressRepository {

    /**
     * Сохраняет или обновляет прогресс слова.
     */
    suspend fun saveProgress(progress: WordProgress)

    /**
     * Получает прогресс для конкретного слова.
     */
    suspend fun getProgressForWord(wordId: String): WordProgress?

    /**
     * Получает слова, готовые к повторению.
     */
    fun getDueReviews(now: Instant): Flow<List<WordProgress>>

    /**
     * Получает новые слова для изучения.
     */
    fun getNewWords(limit: Int): Flow<List<WordProgress>>

    /**
     * Получает слова в процессе изучения.
     */
    fun getLearningWords(limit: Int): Flow<List<WordProgress>>

    fun observeKnownWordsCount(): Flow<Int>

    fun observeReviewedWordsCountBetween(start: Instant, end: Instant): Flow<Int>

    suspend fun countDueReviews(now: Instant): Int

    /**
     * Получает слово по ID из словаря.
     */
    suspend fun getWordById(wordId: String): Word?
}
