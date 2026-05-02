package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.Vocabulary
import com.example.mindlex.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {

    fun getRandomWords(
        limit: Int
    ): Flow<Result<List<Vocabulary>>>

    fun getWordsByCategory(
        category: String,
        limit: Int
    ): Flow<Result<List<Vocabulary>>>

    suspend fun findVocabularyByForeignWord(foreignWord: String): Vocabulary?

    suspend fun getRandomWordByCategoryExcluding(
        category: String,
        limit: Int,
        excludedIds: Set<String>,
        reuseIfAllExcluded: Boolean
    ): Result<Word>
}
