package com.example.mindlex.feature.active_recall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.UserAnswer
import com.example.mindlex.domain.model.WordStatus
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.EvaluateAnswer
import com.example.mindlex.domain.usecase.GetNextWordForPractice
import com.example.mindlex.domain.usecase.UpdateWordProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@HiltViewModel
class ActiveRecallViewModel @Inject constructor(
    private val getNextWord: GetNextWordForPractice,
    private val evaluateAnswer: EvaluateAnswer,
    private val updateProgress: UpdateWordProgress,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val shownWordIds = mutableSetOf<String>()
    private var loadingWord = false

    private val _uiState = MutableStateFlow(ActiveRecallUiState())
    val uiState: StateFlow<ActiveRecallUiState> = _uiState

    init {
        viewModelScope.launch {
            val goal = settingsRepository.getDailyGoal().firstOrNull()
                ?: LearningDefaults.DAILY_GOAL_FALLBACK
            _uiState.update { it.copy(totalWords = goal) }
            loadNextWord()
        }
    }

    fun onUserInputChanged(input: String) {
        _uiState.update { it.copy(userInput = input) }
    }

    fun showHint() {
        val word = _uiState.value.currentWord ?: return
        if (!_uiState.value.canShowHint) return

        _uiState.update {
            it.copy(
                hintShown = true,
                hintUsedCount = it.hintUsedCount + 1,
                feedback = ActiveRecallFeedback(
                    isCorrect = true,
                    quality = EvaluateAnswer.HINT_RESPONSE_QUALITY,
                    usedHint = true
                )
            )
        }

        viewModelScope.launch {
            updateProgress(
                ReviewResult(
                    wordId = word.id,
                    quality = EvaluateAnswer.HINT_RESPONSE_QUALITY,
                    nextReviewAt = Clock.System.now(),
                    newStatus = WordStatus.LEARNING
                )
            )
        }
    }

    fun checkAnswer() {
        val state = _uiState.value
        val word = state.currentWord ?: return
        val input = state.userInput.trim()
        if (!state.awaitingAnswer || input.isBlank()) return

        val reviewResult = evaluateAnswer(
            UserAnswer(
                wordId = word.id,
                userInput = input,
                isCorrect = false,
                responseTimeMs = System.currentTimeMillis(),
                timestamp = Clock.System.now()
            ),
            word
        )

        viewModelScope.launch { updateProgress(reviewResult) }

        val correct = reviewResult.quality >= EvaluateAnswer.ACCEPTANCE_QUALITY_MIN
        _uiState.update {
            it.copy(
                feedback = ActiveRecallFeedback(
                    isCorrect = correct,
                    quality = reviewResult.quality,
                    usedHint = false
                ),
                hintShown = false,
                correctCount = it.correctCount + if (correct) 1 else 0,
                incorrectCount = it.incorrectCount + if (!correct) 1 else 0
            )
        }
    }

    fun nextWord() {
        _uiState.update {
            it.copy(userInput = "", feedback = null, hintShown = false)
        }

        if (_uiState.value.currentWordIndex >= _uiState.value.totalWords) {
            _uiState.update { it.copy(sessionComplete = true) }
            return
        }
        loadNextWord()
    }

    fun retrySession() {
        shownWordIds.clear()
        _uiState.update {
            ActiveRecallUiState(
                totalWords = it.totalWords,
                correctCount = 0,
                incorrectCount = 0,
                hintUsedCount = 0
            )
        }
        loadNextWord()
    }

    suspend fun shouldShowTutorial(): Boolean =
        settingsRepository.isActiveRecallTutorialShown().firstOrNull() != true

    fun markTutorialShown() {
        viewModelScope.launch {
            settingsRepository.setActiveRecallTutorialShown(true)
        }
    }

    private fun loadNextWord() {
        if (loadingWord) return
        loadingWord = true
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                getNextWord(excludedIds = shownWordIds.toSet())
                    .onSuccess { word ->
                        shownWordIds.add(word.id)
                        _uiState.update {
                            it.copy(
                                currentWord = word,
                                isLoading = false,
                                currentWordIndex = it.currentWordIndex + 1
                            )
                        }
                    }
                    .onFailure { error ->
                        val exhausted = error is NoSuchElementException
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                sessionComplete = exhausted || it.sessionComplete
                            )
                        }
                    }
            } finally {
                loadingWord = false
            }
        }
    }
}
