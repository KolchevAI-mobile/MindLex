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

class VocabularyLocalDataSource @Inject constructor(
    private val dao: VocabularyDao
) {

    private val maxCacheSize: Int = 500

    fun getRandomWordsBySource(
        lang: String,
        limit: Int,
        source: String
    ): Flow<List<Vocabulary>> {
        return dao.getRandomWordsBySource(lang = lang, source = source, limit = limit)
            .map { entities -> entities.map(VocabularyEntity::toDomain) }
    }

    fun getWordsByCategoryBySource(
        lang: String,
        category: String,
        limit: Int,
        source: String
    ): Flow<List<Vocabulary>> {
        return dao.getWordsByCategoryBySource(lang = lang, cat = category, source = source, limit = limit)
            .map { entities -> entities.map(VocabularyEntity::toDomain) }
    }

    suspend fun findByForeignWord(lang: String, foreignWord: String): Vocabulary? =
        withContext(Dispatchers.IO) {
            val trimmed = foreignWord.trim()
            if (trimmed.isEmpty()) return@withContext null
            dao.findByWordIgnoreCase(lang, trimmed)?.toDomain()
        }

    suspend fun cacheWords(words: List<VocabularyEntity>) {
        if (words.isEmpty()) {
            return
        }

        withContext(Dispatchers.IO) {
            dao.insertAll(words)
            val now = Clock.System.now()
            val ids = words.map { it.id }
            dao.updateLastAccessed(ids = ids, now = now)
            dao.trimCacheToSize(maxCacheSize)
        }
    }

    suspend fun replaceAll(words: List<VocabularyEntity>) {
        withContext(Dispatchers.IO) {
            dao.replaceAll(words)
        }
    }

    suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            dao.clearAll()
        }
    }
}
