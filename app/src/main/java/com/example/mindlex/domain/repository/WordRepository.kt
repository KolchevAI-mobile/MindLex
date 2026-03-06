package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.UserProgress
import com.example.mindlex.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {

    suspend fun saveWord(word: Word)

    suspend fun getWordById(id: String): Word?

    fun searchWords(query: String): Flow<List<Word>>

    suspend fun getAllWords(): List<Word>

    fun observeDueReviews(currentTimeMillis: Long): Flow<List<Word>>

    suspend fun updateProgress(progress: UserProgress)

    fun observeProgressForWord(wordId: String): Flow<UserProgress?>
}
