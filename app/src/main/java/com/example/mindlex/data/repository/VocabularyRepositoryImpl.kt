package com.example.mindlex.data.repository

import com.example.mindlex.data.local.entity.VocabularyEntity
import com.example.mindlex.data.local.repository.VocabularyLocalDataSource
import com.example.mindlex.data.remote.supabase.SupabaseVocabularyRemoteDataSource
import com.example.mindlex.domain.model.Vocabulary
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Имплементация репозитория словаря с логикой offline-first.
 */
class VocabularyRepositoryImpl @Inject constructor(
    private val remoteDataSource: SupabaseVocabularyRemoteDataSource,
    private val localDataSource: VocabularyLocalDataSource,
    private val settingsRepository: SettingsRepository
) : VocabularyRepository {

    override fun getRandomWords(
        lang: String,
        limit: Int
    ): Flow<Result<List<Vocabulary>>> = flow {
        val safeResult = remoteDataSource.safeGetRandomWords(lang, limit)

        safeResult
            .onSuccess { remoteWords ->
                // Успех сети: кэшируем в Room и возвращаем доменные модели
                val entities: List<VocabularyEntity> =
                    remoteWords.map { it.toVocabularyEntity(lang) }
                localDataSource.cacheWords(entities)
                emit(Result.success(remoteWords.map { it.toVocabulary(lang) }))
            }
            .onFailure {
                // Ошибка сети: пробуем достать слова из локального кэша
                val cached = localDataSource.getRandomWords(lang, limit).first()
                if (cached.isNotEmpty()) {
                    emit(Result.success(cached))
                } else {
                    // Пусто везде: возвращаем пустой список без ошибки
                    emit(Result.success(emptyList()))
                }
            }
    }.flowOn(Dispatchers.IO)

    override fun getWordsByCategory(
        lang: String,
        category: String,
        limit: Int
    ): Flow<Result<List<Vocabulary>>> = flow {
        val safeResult = remoteDataSource.safeGetWordsByCategory(lang, category, limit)

        safeResult
            .onSuccess { remoteWords ->
                val entities: List<VocabularyEntity> =
                    remoteWords.map { it.toVocabularyEntity(lang) }
                localDataSource.cacheWords(entities)
                emit(Result.success(remoteWords.map { it.toVocabulary(lang) }))
            }
            .onFailure {
                val cached = localDataSource.getWordsByCategory(lang, category, limit).first()
                if (cached.isNotEmpty()) {
                    emit(Result.success(cached))
                } else {
                    emit(Result.success(emptyList()))
                }
            }
    }.flowOn(Dispatchers.IO)
}

