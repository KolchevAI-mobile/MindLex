package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.Vocabulary
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для получения слов для обучения.
 * Инкапсулирует логику offline-first (Supabase + Room кэш).
 */
interface VocabularyRepository {

    fun getRandomWords(
        lang: String,
        limit: Int
    ): Flow<Result<List<Vocabulary>>>

    fun getWordsByCategory(
        lang: String,
        category: String,
        limit: Int
    ): Flow<Result<List<Vocabulary>>>
}

