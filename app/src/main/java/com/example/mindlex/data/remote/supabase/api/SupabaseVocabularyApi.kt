package com.example.mindlex.data.remote.supabase.api

import com.example.mindlex.data.remote.supabase.models.SupabaseVocabularyDto

/**
 * Контракт для работы с таблицей vocabulary в Supabase.
 * Выделен в отдельный интерфейс для удобства моков и тестирования.
 */
interface SupabaseVocabularyApi {

    /**
     * Получить случайный набор слов для заданного языка обучения.
     */
    suspend fun getRandomWords(
        targetLang: String,
        limit: Int
    ): List<SupabaseVocabularyDto>

    /**
     * Получить слова для заданной категории и языка обучения.
     */
    suspend fun getWordsByCategory(
        targetLang: String,
        category: String,
        limit: Int
    ): List<SupabaseVocabularyDto>
}

