package com.example.mindlex.data.repository

import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.data.local.entity.VocabularyEntity
import com.example.mindlex.data.local.mapper.VocabularyToWordMapper
import com.example.mindlex.data.local.repository.VocabularyLocalDataSource
import com.example.mindlex.data.remote.supabase.SupabaseVocabularyRemoteDataSource
import com.example.mindlex.domain.model.Vocabulary
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

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
        val selectedCategory = settingsRepository.getSelectedCategory().first()

        if (selectedCategory == LearningDefaults.CUSTOM_DATASET_CATEGORY) {
            val cached = localDataSource.getRandomWords(lang, limit).first()
            emit(Result.success(cached))
            return@flow
        }

        val safeResult = remoteDataSource.safeGetRandomWords(lang, limit)

        safeResult
            .onSuccess { remoteWords ->
                val entities: List<VocabularyEntity> =
                    remoteWords.map { it.toVocabularyEntity(lang) }
                localDataSource.cacheWords(entities)
                emit(Result.success(remoteWords.map { it.toVocabulary(lang) }))
            }
            .onFailure {
                val cached = localDataSource.getRandomWords(lang, limit).first()
                if (cached.isNotEmpty()) {
                    emit(Result.success(cached))
                } else {
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
        val poolLimit = (limit * LearningDefaults.ROOM_POOL_MULTIPLIER)
            .coerceAtLeast(LearningDefaults.ROOM_POOL_MIN)
            .coerceAtMost(LearningDefaults.ROOM_POOL_MAX)
        if (cat == LearningDefaults.CUSTOM_DATASET_CATEGORY) {
            val customWords = localDataSource.getRandomWords(lang, limit).first()
            emit(Result.success(customWords))
            return@flow
        }

        val cached = localDataSource.getWordsByCategory(lang, cat, poolLimit).first()
        if (cached.size >= limit) {
            emit(Result.success(cached.shuffled().take(limit)))
            return@flow
        }

        val safeResult = remoteDataSource.safeGetWordsByCategory(lang, cat, poolLimit.coerceAtLeast(limit))

        safeResult
            .onSuccess { remoteWords ->
                val entities: List<VocabularyEntity> =
                    remoteWords.map { it.toVocabularyEntity(lang) }
                localDataSource.cacheWords(entities)
                emit(Result.success(remoteWords.map { it.toVocabulary(lang) }))
            }
            .onFailure {
                val cached = localDataSource.getWordsByCategory(lang, cat, poolLimit.coerceAtLeast(limit)).first()
                if (cached.isNotEmpty()) {
                    emit(Result.success(cached))
                } else {
                    emit(Result.success(emptyList()))
                }
            }
    }.flowOn(Dispatchers.IO)

    override suspend fun findVocabularyByForeignWord(foreignWord: String): Vocabulary? {
        val lang = settingsRepository.getSelectedLanguage().first()
        return localDataSource.findByForeignWord(lang, foreignWord)
    }

    override suspend fun getRandomWordByCategoryExcluding(
        category: String,
        limit: Int,
        excludedIds: Set<String>,
        reuseIfAllExcluded: Boolean
    ): Result<Word> = withContext(Dispatchers.IO) {
        getWordsByCategory(category, limit).first().fold(
            onSuccess = { list ->
                if (list.isEmpty()) {
                    Result.failure(NoSuchElementException("Нет слов в категории $category"))
                } else {
                    val fresh = list.filter { it.id !in excludedIds }
                    val pick = when {
                        fresh.isNotEmpty() -> fresh.random()
                        reuseIfAllExcluded -> list.random()
                        else -> return@withContext Result.failure(
                            NoSuchElementException("Нет свободных слов в категории $category")
                        )
                    }
                    Result.success(VocabularyToWordMapper.toWord(pick))
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}

