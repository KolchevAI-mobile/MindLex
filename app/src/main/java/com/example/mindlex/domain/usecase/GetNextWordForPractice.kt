package com.example.mindlex.domain.usecase

import com.example.mindlex.data.local.mapper.VocabularyToWordMapper
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case для получения следующего слова для практики.
 * Приоритет: REVIEW → NEW → LEARNING
 */
class GetNextWordForPractice @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: WordProgressRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(): Result<Word> {
        val now = Clock.System.now()

        // Получаем выбранную категорию из настроек
        val selectedCategory = settingsRepository.getSelectedCategory().firstOrNull() ?: "general"
        Timber.d("[GetNextWord] Категория: $selectedCategory")

        // 1. Сначала ищем слова на повторение (REVIEW)
        val dueReviews = progressRepository.getDueReviews(now).firstOrNull()
        if (dueReviews != null && dueReviews.isNotEmpty()) {
            val progress = dueReviews.random()
            val word = progressRepository.getWordById(progress.wordId)
            if (word != null) {
                Timber.d("[GetNextWord] Найдено слово на повторение: ${word.wordNative}")
                return Result.success(word)
            }
        }

        // 2. Если нет — новые слова (NEW)
        val newWords = progressRepository.getNewWords(limit = 10).firstOrNull()
        if (newWords != null && newWords.isNotEmpty()) {
            val progress = newWords.random()
            val word = progressRepository.getWordById(progress.wordId)
            if (word != null) {
                Timber.d("[GetNextWord] Найдено новое слово: ${word.wordNative}")
                return Result.success(word)
            }
        }

        // 3. Если нет — слова в изучении (LEARNING)
        val learningWords = progressRepository.getLearningWords(limit = 10).firstOrNull()
        if (learningWords != null && learningWords.isNotEmpty()) {
            val progress = learningWords.random()
            val word = progressRepository.getWordById(progress.wordId)
            if (word != null) {
                Timber.d("[GetNextWord] Найдено слово в изучении: ${word.wordNative}")
                return Result.success(word)
            }
        }

        // 4. Если ничего не найдено — загружаем из словаря по категории
        Timber.d("[GetNextWord] Нет слов в прогрессе, загружаем из словаря (категория: $selectedCategory)")
        
        return try {
            // Используем getWordsByCategory вместо getRandomWords (у него нет параметра category)
            val vocabularyResult = vocabularyRepository.getWordsByCategory(
                category = selectedCategory,
                limit = 20
            ).firstOrNull()
            
            vocabularyResult?.fold(
                onSuccess = { words ->
                    if (words.isNotEmpty()) {
                        val randomVocabulary = words.random()
                        // Конвертируем Vocabulary в Word через маппер
                        val word = VocabularyToWordMapper.toWord(randomVocabulary)
                        Timber.d("[GetNextWord] Загружено слово из словаря: ${word.wordNative}")
                        Result.success(word)
                    } else {
                        Result.failure(NoSuchElementException("Нет слов в категории $selectedCategory"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            ) ?: Result.failure(NoSuchElementException("Не удалось загрузить слова из словаря"))
        } catch (e: Exception) {
            Timber.e(e, "[GetNextWord] Ошибка загрузки из словаря")
            Result.failure(NoSuchElementException("Нет доступных слов для практики. Добавьте слова в словарь."))
        }
    }
}
