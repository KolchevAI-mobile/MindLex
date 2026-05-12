package com.example.mindlex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mindlex.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface VocabularyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<VocabularyEntity>)

    @Query(
        "SELECT * FROM vocabulary_cache " +
            "WHERE targetLanguage = :lang AND IFNULL(source, 'remote') = :source " +
            "ORDER BY RANDOM() " +
            "LIMIT :limit"
    )
    fun getRandomWordsBySource(
        lang: String,
        source: String,
        limit: Int
    ): Flow<List<VocabularyEntity>>

    @Query(
        "SELECT * FROM vocabulary_cache " +
            "WHERE targetLanguage = :lang AND category = :cat AND IFNULL(source, 'remote') = :source " +
            "ORDER BY RANDOM() " +
            "LIMIT :limit"
    )
    fun getWordsByCategoryBySource(
        lang: String,
        cat: String,
        source: String,
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

    @Query("DELETE FROM vocabulary_cache")
    suspend fun clearAll()

    @Query("DELETE FROM vocabulary_cache WHERE source = :source")
    suspend fun clearBySource(source: String)

    @Transaction
    suspend fun replaceAll(words: List<VocabularyEntity>) {
        clearAll()
        if (words.isNotEmpty()) {
            insertAll(words)
        }
    }
}
