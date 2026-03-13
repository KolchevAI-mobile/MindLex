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
import timber.log.Timber

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
        Timber.d("[VocabularyRepository] Запрос слов: lang=$lang, limit=$limit")

        val safeResult = remoteDataSource.safeGetRandomWords(lang, limit)

        safeResult
            .onSuccess { remoteWords ->
                Timber.d("[VocabularyRepository] Получено ${remoteWords.size} слов из Supabase")
                // Успех сети: кэшируем в Room и возвращаем доменные модели
                val entities: List<VocabularyEntity> =
                    remoteWords.map { it.toVocabularyEntity(lang) }
                Timber.d("[VocabularyRepository] Кэширую ${entities.size} слов в Room")
                localDataSource.cacheWords(entities)
                Timber.d("[VocabularyRepository] Возвращаю ${remoteWords.size} слов в UI")
                emit(Result.success(remoteWords.map { it.toVocabulary(lang) }))
            }
            .onFailure { throwable ->
                Timber.e(throwable, "[VocabularyRepository] Ошибка сети/декодирования при загрузке random слов (lang=$lang)")
                // Ошибка сети: пробуем достать слова из локального кэша
                Timber.d("[VocabularyRepository] Пробую загрузить из Room...")
                val cached = localDataSource.getRandomWords(lang, limit).first()
                Timber.d("[VocabularyRepository] В кэше найдено ${cached.size} слов")

                if (cached.isNotEmpty()) {
                    Timber.d("[VocabularyRepository] Возвращаю ${cached.size} слов из кэша")
                    emit(Result.success(cached))
                } else {
                    Timber.d("[VocabularyRepository] Кэш пуст, возвращаю пустой список")
                    emit(Result.success(emptyList()))
                }
            }
    }.flowOn(Dispatchers.IO)

    override fun getWordsByCategory(
        lang: String,
        category: String,
        limit: Int
    ): Flow<Result<List<Vocabulary>>> = flow {
        Timber.d("[VocabularyRepository] Запрос слов по категории: lang=$lang, category=$category, limit=$limit")

        val safeResult = remoteDataSource.safeGetWordsByCategory(lang, category, limit)

        safeResult
            .onSuccess { remoteWords ->
                Timber.d("[VocabularyRepository] Получено ${remoteWords.size} слов из Supabase по категории '$category'")
                val entities: List<VocabularyEntity> =
                    remoteWords.map { it.toVocabularyEntity(lang) }
                Timber.d("[VocabularyRepository] Кэширую ${entities.size} слов в Room")
                localDataSource.cacheWords(entities)
                Timber.d("[VocabularyRepository] Возвращаю ${remoteWords.size} слов в UI")
                emit(Result.success(remoteWords.map { it.toVocabulary(lang) }))
            }
            .onFailure { throwable ->
                Timber.e(throwable, "[VocabularyRepository] Ошибка сети/декодирования при загрузке слов по категории (lang=$lang, category=$category)")
                Timber.d("[VocabularyRepository] Пробую загрузить из Room...")
                val cached = localDataSource.getWordsByCategory(lang, category, limit).first()
                Timber.d("[VocabularyRepository] В кэше найдено ${cached.size} слов")

                if (cached.isNotEmpty()) {
                    Timber.d("[VocabularyRepository] Возвращаю ${cached.size} слов из кэша")
                    emit(Result.success(cached))
                } else {
                    Timber.d("[VocabularyRepository] Кэш пуст, возвращаю пустой список")
                    emit(Result.success(emptyList()))
                }
            }
    }.flowOn(Dispatchers.IO)
}

