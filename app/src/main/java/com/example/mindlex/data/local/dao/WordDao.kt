package com.example.mindlex.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mindlex.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: String): WordEntity?

    @Query("SELECT * FROM words WHERE word LIKE :query LIMIT 20")
    fun searchWords(query: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntity>

    @Query("SELECT * FROM words")
    fun observeAllWords(): Flow<List<WordEntity>>
}
