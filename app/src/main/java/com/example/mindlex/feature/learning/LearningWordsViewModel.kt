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

/** ViewModel для экрана обучения слов. */
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
        loadWords()
    }

    /** Загружает слова для обучения на основе текущих настроек пользователя. */
    fun loadWords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getLearningWords().collect { result ->
                result
                    .onSuccess { words ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                words = words,
                                error = null
                            )
                        }
                    }
                    .onFailure { throwable ->
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

