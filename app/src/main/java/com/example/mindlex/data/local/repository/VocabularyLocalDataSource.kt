package com.example.mindlex.data.local.repository

import com.example.mindlex.data.local.dao.VocabularyDao
import com.example.mindlex.data.local.entity.VocabularyEntity
import com.example.mindlex.data.local.entity.toDomain
import com.example.mindlex.domain.model.Vocabulary
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import timber.log.Timber

/** Локальный источник данных для кэша слов с LRU стратегией. */
class VocabularyLocalDataSource @Inject constructor(
    private val dao: VocabularyDao
) {

    private val maxCacheSize: Int = 500

    fun getRandomWords(
        lang: String,
        limit: Int
    ): Flow<List<Vocabulary>> {
        Timber.d("[RoomCache] Запрос случайных слов: lang=$lang, limit=$limit")
        return dao.getRandomWords(lang = lang, limit = limit)
            .map { entities ->
                Timber.d("[RoomCache] Получено ${entities.size} слов из кэша (random)")
                entities.map(VocabularyEntity::toDomain)
            }
    }

    fun getWordsByCategory(
        lang: String,
        category: String,
        limit: Int
    ): Flow<List<Vocabulary>> {
        Timber.d("[RoomCache] Запрос слов по категории: lang=$lang, category=$category, limit=$limit")
        return dao.getWordsByCategory(lang = lang, cat = category, limit = limit)
            .map { entities ->
                Timber.d("[RoomCache] Получено ${entities.size} слов из кэша (category=$category)")
                entities.map(VocabularyEntity::toDomain)
            }
    }

    /** Поиск слова в кэше по форме на целевом языке (для связи cloze → WordProgress). */
    suspend fun findByForeignWord(lang: String, foreignWord: String): Vocabulary? =
        withContext(Dispatchers.IO) {
            val trimmed = foreignWord.trim()
            if (trimmed.isEmpty()) return@withContext null
            dao.findByWordIgnoreCase(lang, trimmed)?.toDomain()
        }

    /** Кэширует слова и применяет LRU стратегию. */
    suspend fun cacheWords(words: List<VocabularyEntity>) {
        if (words.isEmpty()) {
            Timber.d("[RoomCache] Нечего кэшировать, список пуст")
            return
        }

        Timber.d("[RoomCache] Кэширую ${words.size} слов...")

        withContext(Dispatchers.IO) {
            dao.insertAll(words)
            val now = Clock.System.now()
            val ids = words.map { it.id }
            dao.updateLastAccessed(ids = ids, now = now)
            dao.trimCacheToSize(maxCacheSize)
            Timber.d("[RoomCache] Закэшировано ${words.size} слов, LRU применён")
        }
    }
}

