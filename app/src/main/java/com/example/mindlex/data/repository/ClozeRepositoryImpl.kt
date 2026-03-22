package com.example.mindlex.data.repository

import com.example.mindlex.data.local.mapper.VocabularyToWordMapper
import com.example.mindlex.data.remote.supabase.ClozeRemoteDataSource
import com.example.mindlex.domain.model.ClozeExercise
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.ClozeRepository
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import timber.log.Timber

class ClozeRepositoryImpl @Inject constructor(
    private val remoteDataSource: ClozeRemoteDataSource,
    private val settingsRepository: SettingsRepository,
    private val vocabularyRepository: VocabularyRepository
) : ClozeRepository {

    override suspend fun getNextExercise(excludedIds: Set<String>): Result<Pair<ClozeExercise, Word>> {
        val category = settingsRepository.getSelectedCategory().first().lowercase()
        val lang = settingsRepository.getSelectedLanguage().first()

        val remoteResult = remoteDataSource.safeGetAllExercises()
        val dtos = remoteResult.getOrElse { error ->
            return Result.failure(error)
        }

        if (dtos.isEmpty()) {
            return Result.failure(NoSuchElementException("Нет упражнений cloze в базе"))
        }

        val inCategory = dtos
            .map { it.toDomain() }
            .filter { it.category.lowercase() == category }

        val pool: List<ClozeExercise> = if (inCategory.isNotEmpty()) inCategory else {
            Timber.w("[ClozeRepo] Категория '$category' пуста, берём все упражнения")
            dtos.map { it.toDomain() }
        }

        val fresh = pool.filter { it.id !in excludedIds }
        val exercise = fresh.randomOrNull()
            ?: pool.randomOrNull()
            ?: return Result.failure(NoSuchElementException("Не удалось выбрать упражнение"))

        val linkedVocab = vocabularyRepository.findVocabularyByForeignWord(exercise.correctAnswer)
        val evaluationWord = if (linkedVocab != null) {
            VocabularyToWordMapper.toWord(linkedVocab)
        } else {
            // Отдельный ключ прогресса для упражнения без совпадения в словаре
            Word(
                id = "cloze:${exercise.id}",
                wordForeign = exercise.correctAnswer,
                wordNative = "",
                targetLanguage = lang,
                category = exercise.category
            )
        }

        Timber.d("[ClozeRepo] Выбрано упражнение id=${exercise.id}, progressWordId=${evaluationWord.id}")
        return Result.success(exercise to evaluationWord)
    }
}
