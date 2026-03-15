package com.example.mindlex.data.remote.supabase.api

import com.example.mindlex.data.remote.supabase.models.SupabaseVocabularyDto

/** Контракт API для таблицы vocabulary в Supabase. */
interface SupabaseVocabularyApi {

    /** Получает случайные слова для целевого языка. */
    suspend fun getRandomWords(
        targetLang: String,
        limit: Int
    ): List<SupabaseVocabularyDto>

    /** Получает слова по категории и целевому языку. */
    suspend fun getWordsByCategory(
        targetLang: String,
        category: String,
        limit: Int
    ): List<SupabaseVocabularyDto>
}

