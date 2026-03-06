package com.example.mindlex.data.repository

import com.example.mindlex.data.local.dao.ProgressDao
import com.example.mindlex.data.local.dao.WordDao
import com.example.mindlex.data.local.mapper.ProgressLocalMapper
import com.example.mindlex.data.local.mapper.WordLocalMapper
import com.example.mindlex.data.local.repository.WordLocalDataSource
import com.example.mindlex.domain.model.UserProgress
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.WordRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class WordRepositoryImpl @Inject constructor(
    private val localDataSource: WordLocalDataSource,
    private val progressDao: ProgressDao,
    private val wordDao: WordDao
) : WordRepository {

    override suspend fun saveWord(word: Word) {
        localDataSource.saveWord(
            WordLocalMapper.fromDomain(word)
        )
    }

    override suspend fun getWordById(id: String): Word? {
        return localDataSource.getWordById(id)?.let {
            WordLocalMapper.toDomain(it)
        }
    }

    override fun searchWords(query: String): Flow<List<Word>> {
        return localDataSource.searchWords(query).map { entities ->
            entities.map(WordLocalMapper::toDomain)
        }
    }

    override suspend fun getAllWords(): List<Word> {
        return localDataSource.getAllWords().map(WordLocalMapper::toDomain)
    }

    override suspend fun updateProgress(progress: UserProgress) {
        progressDao.insertOrUpdateProgress(ProgressLocalMapper.fromDomain(progress))
    }

    override fun observeProgressForWord(wordId: String): Flow<UserProgress?> {
        return progressDao.observeProgressForWord(wordId)
            .map { it?.let(ProgressLocalMapper::toDomain) }
    }

    override fun observeDueReviews(currentTimeMillis: Long): Flow<List<Word>> {
        return progressDao.observeDueReviews(Instant.fromEpochMilliseconds(currentTimeMillis))
            .map { progressEntities ->
                progressEntities.mapNotNull { progress ->
                    wordDao.getWordById(progress.wordId)?.let(WordLocalMapper::toDomain)
                }
            }
    }
}
