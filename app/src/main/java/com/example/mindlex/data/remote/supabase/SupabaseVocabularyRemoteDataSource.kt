package com.example.mindlex.data.remote.supabase

import com.example.mindlex.data.remote.supabase.api.SupabaseVocabularyApi
import com.example.mindlex.data.remote.supabase.models.SupabaseVocabularyDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
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

        // Загружаем все слова из таблицы (клиентская фильтрация)
        val all = client.postgrest.from(tableName)
            .select()
            .decodeList<SupabaseVocabularyDto>()

        Timber.d("[SupabaseAPI] Загружено ${all.size} записей из таблицы '$tableName'")

        // Логируем примеры для отладки
        if (all.isNotEmpty()) {
            val sample = all.take(3).map { dto ->
                "(id=${dto.id.take(8)}..., word_en=${dto.word_en}, word_ru=${dto.word_ru}, category=${dto.category})"
            }
            Timber.d("[SupabaseAPI] Примеры: $sample")
        } else {
            Timber.e("[SupabaseAPI] ⚠️ ТАБЛИЦА ПУСТА! Проверьте импорт данных в Supabase")
        }

        // Фильтрация: слово не должно быть пустым на целевом языке
        val filteredByLang = all.filter { dto ->
            when (targetLang) {
                "en" -> !dto.word_en.isNullOrBlank()
                "de" -> !dto.word_de.isNullOrBlank()
                "fr" -> !dto.word_fr.isNullOrBlank()
                "es" -> !dto.word_es.isNullOrBlank()
                else -> !dto.word_en.isNullOrBlank()
            }
        }

        Timber.d("[SupabaseAPI] После фильтрации по языку '$targetLang': ${filteredByLang.size} записей")

        // Фильтрация по категории (по умолчанию 'general')
        val filteredByCategory = filteredByLang.filter { dto ->
            dto.category?.lowercase() == "general"
        }

        Timber.d("[SupabaseAPI] После фильтрации по категории 'general': ${filteredByCategory.size} записей")

        // Возвращаем случайные слова в нужном количестве
        filteredByCategory
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

        // Загружаем все слова
        val all = client.postgrest.from(tableName)
            .select()
            .decodeList<SupabaseVocabularyDto>()

        Timber.d("[SupabaseAPI] Загружено ${all.size} записей")

        // Клиентская фильтрация
        val filtered = all.filter { dto ->
            // 1. Слово на целевом языке не пустое
            val hasWord = when (targetLang) {
                "en" -> !dto.word_en.isNullOrBlank()
                "de" -> !dto.word_de.isNullOrBlank()
                "fr" -> !dto.word_fr.isNullOrBlank()
                "es" -> !dto.word_es.isNullOrBlank()
                else -> !dto.word_en.isNullOrBlank()
            }
            // 2. Категория совпадает (регистронезависимо)
            val hasCategory = dto.category?.lowercase() == normalizedCategory

            hasWord && hasCategory
        }

        Timber.d("[SupabaseAPI] После фильтрации: ${filtered.size} записей")

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