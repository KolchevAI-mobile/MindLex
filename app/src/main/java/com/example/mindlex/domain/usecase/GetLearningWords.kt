package com.example.mindlex.domain.usecase

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

/** Получает слова для обучения на основе настроек пользователя (язык и категория). */
class GetLearningWords @Inject constructor(
    private val repository: VocabularyRepository,
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(limit: Int = 50): Flow<Result<List<Vocabulary>>> = flow {
        // Читаем язык и категорию из настроек
        val lang = settingsRepository.getSelectedLanguage().first()
        val category = settingsRepository.getSelectedCategory().first()

        Timber.d("[GetLearningWords] Настройки: lang=$lang, category=$category, limit=$limit")

        val normalizedCategory = category.lowercase()
        Timber.d("[GetLearningWords] Нормализованная категория: $normalizedCategory")

        val result: Result<List<Vocabulary>> =
            if (normalizedCategory == "general") {
                Timber.d("[GetLearningWords] Вызов getRandomWords() для категории 'general'")
                repository.getRandomWords(limit = limit).first()
            } else {
                Timber.d("[GetLearningWords] Вызов getWordsByCategory() для категории '$normalizedCategory'")
                repository.getWordsByCategory(
                    category = normalizedCategory,
                    limit = limit
                ).first()
            }

        result.fold(
            onSuccess = { words ->
                Timber.d("[GetLearningWords] Успех: получено ${words.size} слов")
            },
            onFailure = { error ->
                Timber.e(error, "[GetLearningWords] Ошибка: ${error.message}")
            }
        )

        emit(result)
    }.flowOn(Dispatchers.Default)
}

