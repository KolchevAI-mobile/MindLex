package com.example.mindlex.data.remote.supabase

import com.example.mindlex.data.remote.supabase.api.SupabaseVocabularyApi
import com.example.mindlex.data.remote.supabase.models.SupabaseVocabularyDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * DataSource для работы с таблицей vocabulary в Supabase.
 * Инкапсулирует сетевые вызовы и оборачивает результат в Result.
 *
 * Важно: из-за отличий версий supabase-kt фильтрация по языку
 * реализована на стороне клиента (в Kotlin-коде), а не в SQL.
 */
class SupabaseVocabularyRemoteDataSource(
    private val client: SupabaseClient
) : SupabaseVocabularyApi {

    private val tableName = "vocabulary"

    override suspend fun getRandomWords(
        targetLang: String,
        limit: Int
    ): List<SupabaseVocabularyDto> = withContext(Dispatchers.IO) {
        // Загружаем строки и фильтруем по нужному языку на клиенте
        val all = client.postgrest.from(tableName)
            .select()
            .decodeList<SupabaseVocabularyDto>()

        all
            .filter { dto ->
                // Оставляем только те записи, где слово на целевом языке не null
                when (targetLang) {
                    "en" -> dto.word_en != null
                    "de" -> dto.word_de != null
                    "fr" -> dto.word_fr != null
                    "es" -> dto.word_es != null
                    else -> dto.word_en != null || dto.word_ru.isNotBlank()
                }
            }
            .shuffled()
            .take(limit)
    }

    override suspend fun getWordsByCategory(
        targetLang: String,
        category: String,
        limit: Int
    ): List<SupabaseVocabularyDto> = withContext(Dispatchers.IO) {
        val all = client.postgrest.from(tableName)
            .select()
            .decodeList<SupabaseVocabularyDto>()

        all
            .filter { dto ->
                val hasTargetWord = when (targetLang) {
                    "en" -> dto.word_en != null
                    "de" -> dto.word_de != null
                    "fr" -> dto.word_fr != null
                    "es" -> dto.word_es != null
                    else -> dto.word_en != null || dto.word_ru.isNotBlank()
                }
                val matchesCategory = (dto.category ?: "general") == category
                hasTargetWord && matchesCategory
            }
            .shuffled()
            .take(limit)
    }

    /**
     * Обёртка, которая возвращает Result и логирует ошибки.
     */
    suspend fun safeGetRandomWords(
        targetLang: String,
        limit: Int
    ): Result<List<SupabaseVocabularyDto>> {
        return runCatching {
            getRandomWords(targetLang, limit)
        }.onFailure { throwable ->
            Timber.d(throwable, "Ошибка загрузки слов из Supabase (random)")
        }
    }

    suspend fun safeGetWordsByCategory(
        targetLang: String,
        category: String,
        limit: Int
    ): Result<List<SupabaseVocabularyDto>> {
        return runCatching {
            getWordsByCategory(targetLang, category, limit)
        }.onFailure { throwable ->
            Timber.d(throwable, "Ошибка загрузки слов из Supabase (category=$category)")
        }
    }
}

