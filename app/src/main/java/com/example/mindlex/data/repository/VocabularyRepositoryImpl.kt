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

/** Реализация репозитория со стратегией offline-first кэширования. */
class VocabularyRepositoryImpl @Inject constructor(
    private val remoteDataSource: SupabaseVocabularyRemoteDataSource,
    private val localDataSource: VocabularyLocalDataSource,
    private val settingsRepository: SettingsRepository
) : VocabularyRepository {

    override fun getRandomWords(
        limit: Int
    ): Flow<Result<List<Vocabulary>>> = flow {
        // Read current language from settings
        val lang = settingsRepository.getSelectedLanguage().first()
        Timber.d("[VocabularyRepository] Запрос: lang=$lang, limit=$limit")

        val safeResult = remoteDataSource.safeGetRandomWords(lang, limit)

        safeResult
            .onSuccess { remoteWords ->
                Timber.d("[VocabularyRepository] Получено ${remoteWords.size} слов из Supabase")
                // Успех сети: кэшируем в Room и возвращаем
                val entities: List<VocabularyEntity> =
                    remoteWords.map { it.toVocabularyEntity(lang) }
                Timber.d("[VocabularyRepository] Кэширую ${entities.size} слов в Room")
                localDataSource.cacheWords(entities)
                Timber.d("[VocabularyRepository] Возвращаю ${remoteWords.size} слов в UI")
                emit(Result.success(remoteWords.map { it.toVocabulary(lang) }))
            }
            .onFailure { throwable ->
                Timber.e(throwable, "[VocabularyRepository] Ошибка загрузки слов (lang=$lang)")
                // Ошибка сети: пробуем загрузить из кэша
                Timber.d("[VocabularyRepository] Пробую загрузить из Room...")
                val cached = localDataSource.getRandomWords(lang, limit).first()
                Timber.d("[VocabularyRepository] Найдено ${cached.size} слов в кэше")

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
        category: String,
        limit: Int
    ): Flow<Result<List<Vocabulary>>> = flow {
        val lang = settingsRepository.getSelectedLanguage().first()
        val cat = category.lowercase()
        val poolLimit = (limit * 4).coerceAtLeast(40).coerceAtMost(400)
        Timber.d("[VocabularyRepository] Запрос по категории: lang=$lang, category=$cat, limit=$limit")

        val cached = localDataSource.getWordsByCategory(lang, cat, poolLimit).first()
        if (cached.size >= limit) {
            Timber.d("[VocabularyRepository] Категория '$cat': ${cached.size} в Room — без сети")
            emit(Result.success(cached.shuffled().take(limit)))
            return@flow
        }

        val safeResult = remoteDataSource.safeGetWordsByCategory(lang, cat, poolLimit.coerceAtLeast(limit))

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
                Timber.e(throwable, "[VocabularyRepository] Ошибка загрузки по категории (lang=$lang, category=$category)")
                Timber.d("[VocabularyRepository] Пробую загрузить из Room...")
                val cached = localDataSource.getWordsByCategory(lang, cat, poolLimit.coerceAtLeast(limit)).first()
                Timber.d("[VocabularyRepository] Найдено ${cached.size} слов в кэше")

                if (cached.isNotEmpty()) {
                    Timber.d("[VocabularyRepository] Возвращаю ${cached.size} слов из кэша")
                    emit(Result.success(cached))
                } else {
                    Timber.d("[VocabularyRepository] Кэш пуст, возвращаю пустой список")
                    emit(Result.success(emptyList()))
                }
            }
    }.flowOn(Dispatchers.IO)

    override suspend fun findVocabularyByForeignWord(foreignWord: String): Vocabulary? {
        val lang = settingsRepository.getSelectedLanguage().first()
        return localDataSource.findByForeignWord(lang, foreignWord)
    }
}

