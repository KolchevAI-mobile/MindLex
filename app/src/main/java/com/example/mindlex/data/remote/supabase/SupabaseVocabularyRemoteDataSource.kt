package com.example.mindlex.data.remote.supabase

import com.example.mindlex.data.remote.supabase.api.SupabaseVocabularyApi
import com.example.mindlex.data.remote.supabase.models.SupabaseVocabularyDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/** Удаленный источник данных для таблицы vocabulary в Supabase с клиентской фильтрацией. */
class SupabaseVocabularyRemoteDataSource(
    private val client: SupabaseClient
) : SupabaseVocabularyApi {

    private val tableName = "words"

    override suspend fun getRandomWords(
        targetLang: String,
        limit: Int
    ): List<SupabaseVocabularyDto> = withContext(Dispatchers.IO) {
        Timber.d("[SupabaseAPI] Запрос к таблице '$tableName': targetLang=$targetLang, limit=$limit")

        val fetchCap = (limit * 25).coerceIn(60, 500)
        val pool = client.postgrest.from(tableName)
            .select(columns = Columns.ALL) {
                filter {
                    ilike("category", "general")
                }
                limit(count = fetchCap.toLong())
            }
            .decodeList<SupabaseVocabularyDto>()

        Timber.d("[SupabaseAPI] Получено ${pool.size} записей (category~general, cap=$fetchCap)")

        val filteredByLang = pool.filter { dto ->
            when (targetLang) {
                "en" -> !dto.word_en.isNullOrBlank()
                "de" -> !dto.word_de.isNullOrBlank()
                "fr" -> !dto.word_fr.isNullOrBlank()
                "es" -> !dto.word_es.isNullOrBlank()
                else -> !dto.word_en.isNullOrBlank()
            }
        }

        Timber.d("[SupabaseAPI] После фильтрации по языку '$targetLang': ${filteredByLang.size} записей")

        filteredByLang
            .shuffled()
            .take(limit)
            .also { result ->
                Timber.d("[SupabaseAPI] Возвращаю ${result.size} слов")
            }
    }

    override suspend fun getWordsByCategory(
        targetLang: String,
        category: String,
        limit: Int
    ): List<SupabaseVocabularyDto> = withContext(Dispatchers.IO) {
        val normalizedCategory = category.lowercase()
        Timber.d("[SupabaseAPI] Запрос по категории: lang=$targetLang, category=$normalizedCategory, limit=$limit")

        val fetchCap = (limit * 25).coerceIn(60, 500)
        val pool = client.postgrest.from(tableName)
            .select(columns = Columns.ALL) {
                filter {
                    ilike("category", normalizedCategory)
                }
                limit(count = fetchCap.toLong())
            }
            .decodeList<SupabaseVocabularyDto>()

        Timber.d("[SupabaseAPI] Получено ${pool.size} записей с сервера (cap=$fetchCap)")

        val filtered = pool.filter { dto ->
            when (targetLang) {
                "en" -> !dto.word_en.isNullOrBlank()
                "de" -> !dto.word_de.isNullOrBlank()
                "fr" -> !dto.word_fr.isNullOrBlank()
                "es" -> !dto.word_es.isNullOrBlank()
                else -> !dto.word_en.isNullOrBlank()
            }
        }

        Timber.d("[SupabaseAPI] После фильтрации по языку и категории: ${filtered.size} записей")

        filtered
            .shuffled()
            .take(limit)
            .also { result ->
                Timber.d("[SupabaseAPI] Возвращаю ${result.size} слов по категории")
            }
    }

    // Обертки для обработки ошибок
    suspend fun safeGetRandomWords(
        targetLang: String,
        limit: Int
    ): Result<List<SupabaseVocabularyDto>> = runCatching {
        getRandomWords(targetLang, limit)
    }.onFailure { e ->
        Timber.e(e, "[SupabaseAPI] Error: getRandomWords(lang=$targetLang)")
    }

    suspend fun safeGetWordsByCategory(
        targetLang: String,
        category: String,
        limit: Int
    ): Result<List<SupabaseVocabularyDto>> = runCatching {
        getWordsByCategory(targetLang, category, limit)
    }.onFailure { e ->
        Timber.e(e, "[SupabaseAPI] Error: getWordsByCategory(cat=$category)")
    }
}