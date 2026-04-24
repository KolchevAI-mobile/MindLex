package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

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
        return vocabularyRepository.getRandomWordByCategoryExcluding(
            category = category,
            limit = 80,
            excludedIds = excludedIds,
            reuseIfAllExcluded = true
        )
    }
}
