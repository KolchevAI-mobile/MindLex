package com.example.mindlex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mindlex.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/** DAO для кэша слов с поддержкой LRU. */
@Dao
interface VocabularyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<VocabularyEntity>)

    @Query(
        "SELECT * FROM vocabulary_cache " +
            "WHERE targetLanguage = :lang " +
            "ORDER BY RANDOM() " +
            "LIMIT :limit"
    )
    fun getRandomWords(
        lang: String,
        limit: Int
    ): Flow<List<VocabularyEntity>>

    @Query(
        "SELECT * FROM vocabulary_cache " +
            "WHERE targetLanguage = :lang AND category = :cat " +
            "ORDER BY RANDOM() " +
            "LIMIT :limit"
    )
    fun getWordsByCategory(
        lang: String,
        cat: String,
        limit: Int
    ): Flow<List<VocabularyEntity>>

    @Query(
        "SELECT * FROM vocabulary_cache WHERE targetLanguage = :lang AND LOWER(word) = LOWER(:w) LIMIT 1"
    )
    suspend fun findByWordIgnoreCase(lang: String, w: String): VocabularyEntity?

    @Query("UPDATE vocabulary_cache SET lastAccessed = :now WHERE id IN (:ids)")
    suspend fun updateLastAccessed(
        ids: List<String>,
        now: Instant
    )

    @Query(
        """
        DELETE FROM vocabulary_cache 
        WHERE id NOT IN (
            SELECT id FROM vocabulary_cache 
            ORDER BY lastAccessed DESC 
            LIMIT :maxSize
        )
        """
    )
    suspend fun trimCacheToSize(maxSize: Int)
}

