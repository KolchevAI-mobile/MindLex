package com.example.mindlex.feature.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.Vocabulary
import com.example.mindlex.domain.usecase.GetLearningWords
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel для экрана обучения, работающего через Supabase + Room кэш.
 */
@HiltViewModel
class LearningWordsViewModel @Inject constructor(
    private val getLearningWords: GetLearningWords
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val words: List<Vocabulary> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        Timber.d("[LearningWordsVM] init - вызываю loadWords()")
        loadWords()
    }

    /**
     * Загрузить слова для обучения с учётом текущих настроек пользователя.
     */
    fun loadWords() {
        Timber.d("[LearningWordsVM] loadWords() начало")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            Timber.d("[LearningWordsVM] isLoading=true, запускаю getLearningWords()")

            getLearningWords().collect { result ->
                Timber.d("[LearningWordsVM] Получен результат от getLearningWords()")
                result
                    .onSuccess { words ->
                        Timber.d("[LearningWordsVM] onSuccess: получено ${words.size} слов")
                        if (words.isEmpty()) {
                            Timber.e("[LearningWordsVM] СПИСОК СЛОВ ПУСТ! Покажу 'Слова не найдены'")
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                words = words,
                                error = null
                            )
                        }
                    }
                    .onFailure { throwable ->
                        Timber.e(throwable, "[LearningWordsVM] onFailure: ${throwable.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = throwable.message
                            )
                        }
                    }
            }
        }
    }
}

