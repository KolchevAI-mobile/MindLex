package com.example.mindlex.data.remote.supabase.api

import com.example.mindlex.data.remote.supabase.models.SupabaseVocabularyDto

interface SupabaseVocabularyApi {

    suspend fun getRandomWords(
        targetLang: String,
        limit: Int
    ): List<SupabaseVocabularyDto>

    suspend fun getWordsByCategory(
        targetLang: String,
        category: String,
        limit: Int
    ): List<SupabaseVocabularyDto>
}
