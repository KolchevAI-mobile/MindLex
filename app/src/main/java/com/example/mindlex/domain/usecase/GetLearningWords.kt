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

/**
 * UseCase, инкапсулирующий логику получения слов для обучения.
 * Учитывает текущий язык и категорию из настроек пользователя.
 */
class GetLearningWords @Inject constructor(
    private val repository: VocabularyRepository,
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(limit: Int = 50): Flow<Result<List<Vocabulary>>> = flow {
        // ВРЕМЕННЫЙ ДЕБАГ: принудительно устанавливаем значения
        val lang = "en"  // ПРИНУДИТЕЛЬНО en для теста
        val category = "general"  // ПРИНУДИТЕЛЬНО general для теста

        // Закомментировано чтение из настроек для дебага:
        // val lang = settingsRepository.getSelectedLanguage().first()
        // val category = settingsRepository.getSelectedCategory().first()

        Timber.d("[GetLearningWords] ===== ДЕБАГ РЕЖИМ =====")
        Timber.d("[GetLearningWords] Настройки: lang=$lang, category=$category, limit=$limit")

        // ИСПРАВЛЕНИЕ: регистронезависимое сравнение
        val normalizedCategory = category.lowercase()
        Timber.d("[GetLearningWords] Нормализованная категория: $normalizedCategory")

        val result: Result<List<Vocabulary>> =
            if (normalizedCategory == "general") {
                Timber.d("[GetLearningWords] Вызов getRandomWords() для категории 'general'")
                repository.getRandomWords(lang = lang, limit = limit).first()
            } else {
                Timber.d("[GetLearningWords] Вызов getWordsByCategory() для категории '$normalizedCategory'")
                repository.getWordsByCategory(
                    lang = lang,
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

