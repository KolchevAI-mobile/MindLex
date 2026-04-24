package com.example.mindlex.domain.usecase

import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock

class GetNextWordForPractice @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: WordProgressRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(excludedIds: Set<String> = emptySet()): Result<Word> {
        val now = Clock.System.now()

        val selectedCategory =
            settingsRepository.getSelectedCategory().firstOrNull() ?: LearningDefaults.FALLBACK_CATEGORY

        val dueReviews = progressRepository.getDueReviews(now).firstOrNull()
        val dueCandidates = dueReviews.orEmpty().filterNot { it.wordId in excludedIds }
        if (dueCandidates.isNotEmpty()) {
            val progress = dueCandidates.random()
            val word = progressRepository.getWordById(progress.wordId)
            if (word != null) {
                return Result.success(word)
            }
        }

        val newWords = progressRepository.getNewWords(limit = LearningDefaults.PROGRESS_CANDIDATE_LIMIT).firstOrNull()
        val newCandidates = newWords.orEmpty().filterNot { it.wordId in excludedIds }
        if (newCandidates.isNotEmpty()) {
            val progress = newCandidates.random()
            val word = progressRepository.getWordById(progress.wordId)
            if (word != null) {
                return Result.success(word)
            }
        }

        val learningWords =
            progressRepository.getLearningWords(limit = LearningDefaults.PROGRESS_CANDIDATE_LIMIT).firstOrNull()
        val learningCandidates = learningWords.orEmpty().filterNot { it.wordId in excludedIds }
        if (learningCandidates.isNotEmpty()) {
            val progress = learningCandidates.random()
            val word = progressRepository.getWordById(progress.wordId)
            if (word != null) {
                return Result.success(word)
            }
        }

        return vocabularyRepository.getRandomWordByCategoryExcluding(
            category = selectedCategory,
            limit = LearningDefaults.VOCABULARY_FETCH_LIMIT,
            excludedIds = excludedIds,
            reuseIfAllExcluded = false
        )
    }
}
