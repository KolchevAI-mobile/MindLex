package com.example.mindlex.data.remote.supabase

import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.data.remote.supabase.api.SupabaseVocabularyApi
import com.example.mindlex.data.remote.supabase.models.SupabaseVocabularyDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseVocabularyRemoteDataSource(
    private val client: SupabaseClient
) : SupabaseVocabularyApi {

    private val tableName = "words"

    private fun fetchCap(requestedLimit: Int): Long {
        val cap = requestedLimit * LearningDefaults.REMOTE_FETCH_MULTIPLIER
        return cap.coerceIn(LearningDefaults.REMOTE_FETCH_CAP_MIN, LearningDefaults.REMOTE_FETCH_CAP_MAX).toLong()
    }

    override suspend fun getRandomWords(
        targetLang: String,
        limit: Int
    ): List<SupabaseVocabularyDto> = withContext(Dispatchers.IO) {
        val pool = client.postgrest.from(tableName)
            .select(columns = Columns.ALL) {
                filter {
                    ilike("category", LearningDefaults.FALLBACK_CATEGORY)
                }
                limit(count = fetchCap(limit))
            }
            .decodeList<SupabaseVocabularyDto>()

        val filteredByLang = pool.filter { dto ->
            when (targetLang) {
                "en" -> !dto.word_en.isNullOrBlank()
                "de" -> !dto.word_de.isNullOrBlank()
                "fr" -> !dto.word_fr.isNullOrBlank()
                "es" -> !dto.word_es.isNullOrBlank()
                else -> !dto.word_en.isNullOrBlank()
            }
        }

        filteredByLang
            .shuffled()
            .take(limit)
    }

    override suspend fun getWordsByCategory(
        targetLang: String,
        category: String,
        limit: Int
    ): List<SupabaseVocabularyDto> = withContext(Dispatchers.IO) {
        val normalizedCategory = category.lowercase()

        val pool = client.postgrest.from(tableName)
            .select(columns = Columns.ALL) {
                filter {
                    ilike("category", normalizedCategory)
                }
                limit(count = fetchCap(limit))
            }
            .decodeList<SupabaseVocabularyDto>()

        val filtered = pool.filter { dto ->
            when (targetLang) {
                "en" -> !dto.word_en.isNullOrBlank()
                "de" -> !dto.word_de.isNullOrBlank()
                "fr" -> !dto.word_fr.isNullOrBlank()
                "es" -> !dto.word_es.isNullOrBlank()
                else -> !dto.word_en.isNullOrBlank()
            }
        }

        filtered
            .shuffled()
            .take(limit)
    }

    suspend fun safeGetRandomWords(
        targetLang: String,
        limit: Int
    ): Result<List<SupabaseVocabularyDto>> = runCatching {
        getRandomWords(targetLang, limit)
    }

    suspend fun safeGetWordsByCategory(
        targetLang: String,
        category: String,
        limit: Int
    ): Result<List<SupabaseVocabularyDto>> = runCatching {
        getWordsByCategory(targetLang, category, limit)
    }
}
