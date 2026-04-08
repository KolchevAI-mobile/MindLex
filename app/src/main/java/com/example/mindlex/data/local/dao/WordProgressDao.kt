package com.example.mindlex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mindlex.data.local.entity.WordProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * DAO для работы с прогрессом изучения слов.
 */
@Dao
interface WordProgressDao {

    /**
     * Вставляет или обновляет прогресс слова.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: WordProgressEntity)

    /**
     * Получает прогресс для конкретного слова.
     */
    @Query("SELECT * FROM word_progress WHERE wordId = :wordId")
    suspend fun getProgressForWord(wordId: String): WordProgressEntity?

    /**
     * Получает слова, готовые к повторению.
     */
    @Query("SELECT * FROM word_progress WHERE status = 'REVIEW' AND nextReviewAt <= :now")
    fun getDueReviews(now: Instant): Flow<List<WordProgressEntity>>

    /**
     * Получает новые слова для изучения.
     */
    @Query("SELECT * FROM word_progress WHERE status = 'NEW' LIMIT :limit")
    fun getNewWords(limit: Int): Flow<List<WordProgressEntity>>

    /**
     * Получает слова в процессе изучения.
     */
    @Query("SELECT * FROM word_progress WHERE status = 'LEARNING' LIMIT :limit")
    fun getLearningWords(limit: Int): Flow<List<WordProgressEntity>>

    @Query("SELECT COUNT(*) FROM word_progress WHERE status = 'KNOWN'")
    fun observeKnownWordsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(correctCount), 0) FROM word_progress")
    fun observeTotalCorrectCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM word_progress WHERE lastReviewedAt BETWEEN :start AND :end")
    fun observeReviewedWordsCountBetween(start: Instant, end: Instant): Flow<Int>

    @Query("SELECT COUNT(*) FROM word_progress WHERE nextReviewAt <= :now")
    suspend fun countDueReviews(now: Instant): Int
}
