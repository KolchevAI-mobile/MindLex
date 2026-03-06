package com.example.mindlex.data.local.repository

import com.example.mindlex.data.local.dao.WordDao
import com.example.mindlex.data.local.entity.WordEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class WordLocalDataSource @Inject constructor(
    private val wordDao: WordDao
) {

    suspend fun saveWord(word: WordEntity) {
        wordDao.insertWord(word)
    }

    suspend fun getWordById(id: String): WordEntity? {
        return wordDao.getWordById(id)
    }

    fun searchWords(query: String): Flow<List<WordEntity>> {
        val pattern = "%$query%"
        return wordDao.searchWords(pattern)
    }

    suspend fun getAllWords(): List<WordEntity> {
        return wordDao.getAllWords()
    }
}
