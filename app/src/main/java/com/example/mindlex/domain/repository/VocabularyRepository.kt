package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.Vocabulary
import kotlinx.coroutines.flow.Flow

/** Репозиторий для получения слов обучения с офлайн-кэшированием. */
interface VocabularyRepository {

    fun getRandomWords(
        limit: Int
    ): Flow<Result<List<Vocabulary>>>

    fun getWordsByCategory(
        category: String,
        limit: Int
    ): Flow<Result<List<Vocabulary>>>
}
