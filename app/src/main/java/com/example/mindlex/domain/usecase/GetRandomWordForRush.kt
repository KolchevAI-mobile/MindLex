package com.example.mindlex.domain.usecase

import com.example.mindlex.data.local.mapper.VocabularyToWordMapper
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

/**
 * Случайное слово для спринта из кэша/сети по выбранной категории.
 * Исключает недавние [excludedIds], при необходимости допускает повтор.
 */
class GetRandomWordForRush @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(excludedIds: Set<String>): Result<Word> {
        val category = settingsRepository.getSelectedCategory().firstOrNull() ?: "general"
        val flowResult = vocabularyRepository.getWordsByCategory(category, limit = 80)
            .firstOrNull()
            ?: return Result.failure(IllegalStateException("Нет потока словаря"))

        return flowResult.fold(
            onSuccess = { list ->
                if (list.isEmpty()) {
                    return@fold Result.failure(NoSuchElementException("Нет слов в категории $category"))
                }
                val fresh = list.filter { it.id !in excludedIds }
                val vocab = fresh.randomOrNull() ?: list.random()
                val word = VocabularyToWordMapper.toWord(vocab)
                Timber.d("[Rush] Слово: ${word.wordNative} → ${word.wordForeign}")
                Result.success(word)
            },
            onFailure = { Result.failure(it) }
        )
    }
}
