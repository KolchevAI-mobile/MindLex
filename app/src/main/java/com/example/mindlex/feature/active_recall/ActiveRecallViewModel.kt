package com.example.mindlex.feature.active_recall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.UserAnswer
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.model.WordStatus
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.EvaluateAnswer
import com.example.mindlex.domain.usecase.GetNextWordForPractice
import com.example.mindlex.domain.usecase.UpdateWordProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class ActiveRecallViewModel @Inject constructor(
    private val getNextWord: GetNextWordForPractice,
    private val evaluateAnswer: EvaluateAnswer,
    private val updateProgress: UpdateWordProgress,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val shownWordIds = mutableSetOf<String>()


    data class UiState(
        val currentWord: Word? = null,
        val userInput: String = "",
        val feedback: Feedback? = null,
        val currentWordIndex: Int = 0,
        val totalWords: Int = LearningDefaults.DAILY_GOAL_FALLBACK,
        val isLoading: Boolean = false,
        val hintShown: Boolean = false,
        val sessionComplete: Boolean = false,
        val correctCount: Int = 0,
        val incorrectCount: Int = 0,
        val hintUsedCount: Int = 0
    )

    data class Feedback(
        val isCorrect: Boolean,
        val quality: Int,
        val usedHint: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            launch {
                val dailyGoal =
                    settingsRepository.getDailyGoal().firstOrNull() ?: LearningDefaults.DAILY_GOAL_FALLBACK
                _uiState.update { it.copy(totalWords = dailyGoal) }
            }
            loadNextWord()
        }
    }

    fun onUserInputChanged(input: String) {
        _uiState.update { it.copy(userInput = input) }
    }

    fun showHint() {
        val currentWord = _uiState.value.currentWord ?: return

        val feedback = Feedback(
            isCorrect = true,
            quality = EvaluateAnswer.HINT_RESPONSE_QUALITY,
            usedHint = true
        )

        _uiState.update {
            it.copy(
                hintShown = true,
                feedback = feedback,
                hintUsedCount = it.hintUsedCount + 1
            )
        }

        viewModelScope.launch {
            val reviewResult = ReviewResult(
                wordId = currentWord.id,
                quality = EvaluateAnswer.HINT_RESPONSE_QUALITY,
                nextReviewAt = Clock.System.now(),
                newStatus = WordStatus.LEARNING
            )
            updateProgress(reviewResult)
        }
    }

    fun checkAnswer() {
        val currentState = _uiState.value
        val currentWord = currentState.currentWord ?: return
        val userInput = currentState.userInput.trim()
        if (userInput.isBlank()) {
            return
        }

        val userAnswer = UserAnswer(
            wordId = currentWord.id,
            userInput = userInput,
            isCorrect = false,
            responseTimeMs = System.currentTimeMillis(),
            timestamp = Clock.System.now()
        )

        val reviewResult = evaluateAnswer(userAnswer, currentWord)

        viewModelScope.launch {
            updateProgress(reviewResult)
        }

        val usedHint = currentState.hintShown
        val isCorrect = reviewResult.quality >= EvaluateAnswer.ACCEPTANCE_QUALITY_MIN
        val feedback = createFeedback(isCorrect, reviewResult.quality, usedHint)

        when {
            usedHint -> _uiState.update { it.copy(hintUsedCount = it.hintUsedCount + 1) }
            isCorrect -> _uiState.update { it.copy(correctCount = it.correctCount + 1) }
            else -> _uiState.update { it.copy(incorrectCount = it.incorrectCount + 1) }
        }

        _uiState.update {
            it.copy(feedback = feedback, hintShown = false)
        }
    }

    fun nextWord() {
        _uiState.update {
            it.copy(userInput = "", feedback = null, hintShown = false)
        }

        val currentState = _uiState.value
        if (currentState.currentWordIndex >= currentState.totalWords) {
            _uiState.update { it.copy(sessionComplete = true) }
            return
        }

        loadNextWord()
    }

    private fun loadNextWord() {
        viewModelScope.launch {
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
                .onFailure {
                    val exhausted = it is NoSuchElementException
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            sessionComplete = exhausted || s.sessionComplete
                        )
                    }
                }
        }
    }

    private fun createFeedback(isCorrect: Boolean, quality: Int, usedHint: Boolean): Feedback {
        return if (usedHint) {
            Feedback(
                isCorrect = true,
                quality = quality.coerceAtMost(EvaluateAnswer.HINT_RESPONSE_QUALITY),
                usedHint = true
            )
        } else {
            Feedback(isCorrect = isCorrect, quality = quality, usedHint = false)
        }
    }

    fun retrySession() {
        shownWordIds.clear()
        _uiState.update {
            UiState(
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
}
