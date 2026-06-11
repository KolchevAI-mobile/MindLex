package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.model.WordProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface WordProgressRepository {

    suspend fun saveProgress(progress: WordProgress)

    suspend fun getProgressForWord(wordId: String): WordProgress?

    fun getDueReviews(now: Instant): Flow<List<WordProgress>>

    fun getNewWords(limit: Int): Flow<List<WordProgress>>

    fun getLearningWords(limit: Int): Flow<List<WordProgress>>

    fun observeKnownWordsCount(): Flow<Int>

    fun observeTotalCorrectCount(): Flow<Int>

    fun observeReviewedWordsCountBetween(start: Instant, end: Instant): Flow<Int>

    suspend fun countDueReviews(now: Instant): Int

    suspend fun getWordById(wordId: String): Word?

    suspend fun clearAll()
}
