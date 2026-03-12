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

/**
 * UseCase, инкапсулирующий логику получения слов для обучения.
 * Учитывает текущий язык и категорию из настроек пользователя.
 */
class GetLearningWords @Inject constructor(
    private val repository: VocabularyRepository,
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(limit: Int = 50): Flow<Result<List<Vocabulary>>> = flow {
        val lang = settingsRepository.getSelectedLanguage().first()
        val category = settingsRepository.getSelectedCategory().first()

        val result: Result<List<Vocabulary>> =
            if (category == "general") {
                repository.getRandomWords(lang = lang, limit = limit).first()
            } else {
                repository.getWordsByCategory(
                    lang = lang,
                    category = category,
                    limit = limit
                ).first()
            }

        emit(result)
    }.flowOn(Dispatchers.Default)
}

