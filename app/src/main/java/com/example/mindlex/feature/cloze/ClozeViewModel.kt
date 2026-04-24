package com.example.mindlex.feature.cloze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.ClozeExercise
import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.UserAnswer
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.model.WordStatus
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.EvaluateAnswer
import com.example.mindlex.domain.usecase.GetNextClozeExercise
import com.example.mindlex.domain.usecase.UpdateWordProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel для механики «Контекстный пропуск».
 * Таймер, проверка через [EvaluateAnswer], прогресс — через [UpdateWordProgress].
 */
@HiltViewModel
class ClozeViewModel @Inject constructor(
    private val getNextClozeExercise: GetNextClozeExercise,
    private val evaluateAnswer: EvaluateAnswer,
    private val updateProgress: UpdateWordProgress,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    data class Feedback(
        val isCorrect: Boolean,
        val quality: Int,
        val message: String,
        val fullSentence: String,
        /** Истёк таймер: в сессии считаем ошибкой, в SRS — без жёсткого штрафа (см. [onTimeExpired]). */
        val timedOut: Boolean = false
    )

    data class UiState(
        val exercise: ClozeExercise? = null,
        val evaluationWord: Word? = null,
        val userInput: String = "",
        val feedback: Feedback? = null,
        val currentIndex: Int = 0,
        val totalExercises: Int = 10,
        val isLoading: Boolean = false,
        val sessionComplete: Boolean = false,
        val correctCount: Int = 0,
        val incorrectCount: Int = 0,
        val hintUsedCount: Int = 0,
        val timerSecondsRemaining: Int = 35,
        val timerTotalSeconds: Int = 35,
        val loadError: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val shownExerciseIds = mutableSetOf<String>()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val goal = settingsRepository.getDailyGoal().firstOrNull() ?: 10
            val timerSec = (settingsRepository.getClozeTimerSeconds().firstOrNull() ?: 35)
                .coerceIn(30, 45)
            _uiState.update {
                it.copy(
                    totalExercises = goal,
                    timerTotalSeconds = timerSec,
                    timerSecondsRemaining = timerSec
                )
            }
            loadNextExercise()
        }
    }

    fun onUserInputChanged(input: String) {
        _uiState.update { it.copy(userInput = input) }
    }

    private fun fullSentence(exercise: ClozeExercise): String {
        val s = exercise.sentenceWithBlank
        return if (s.contains("___")) {
            s.replaceFirst("___", exercise.correctAnswer)
        } else {
            "$s ${exercise.correctAnswer}"
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun startTimer() {
        cancelTimer()
        val total = _uiState.value.timerTotalSeconds
        if (total <= 0) return
        _uiState.update { it.copy(timerSecondsRemaining = total) }
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val left = _uiState.value.timerSecondsRemaining - 1
                if (left <= 0) {
                    _uiState.update { it.copy(timerSecondsRemaining = 0) }
                    onTimeExpired()
                    break
                }
                _uiState.update { it.copy(timerSecondsRemaining = left) }
            }
        }
    }

    fun onTimeExpired() {
        cancelTimer()
        val state = _uiState.value
        if (state.feedback != null || state.exercise == null || state.evaluationWord == null) return

        val exercise = state.exercise
        val evaluationWord = state.evaluationWord

        // Сессия: неправильно; WordProgress: quality 3 — без инкремента incorrectCount (мягкий SRS)
        val reviewResult = ReviewResult(
            wordId = evaluationWord.id,
            quality = 3,
            nextReviewAt = Clock.System.now(),
            newStatus = WordStatus.LEARNING
        )
        viewModelScope.launch {
            updateProgress(reviewResult)
        }

        val fb = Feedback(
            isCorrect = false,
            quality = 3,
            message = "Время вышло",
            fullSentence = fullSentence(exercise),
            timedOut = true
        )
        _uiState.update {
            it.copy(
                feedback = fb,
                incorrectCount = it.incorrectCount + 1
            )
        }
    }

    fun checkAnswer() {
        cancelTimer()
        val state = _uiState.value
        val exercise = state.exercise ?: return
        val evaluationWord = state.evaluationWord ?: return
        val trimmed = state.userInput.trim()
        if (trimmed.isBlank() || state.feedback != null) {
            return
        }

        val userAnswer = UserAnswer(
            wordId = evaluationWord.id,
            userInput = trimmed,
            isCorrect = false,
            responseTimeMs = System.currentTimeMillis(),
            timestamp = Clock.System.now()
        )
        val reviewResult = evaluateAnswer(userAnswer, evaluationWord)
        viewModelScope.launch {
            updateProgress(reviewResult)
        }

        val isCorrect = reviewResult.quality >= 3
        val message = if (isCorrect) "Правильно!" else "Неправильно"

        _uiState.update {
            it.copy(
                feedback = Feedback(
                    isCorrect = isCorrect,
                    quality = reviewResult.quality,
                    message = message,
                    fullSentence = fullSentence(exercise)
                ),
                correctCount = it.correctCount + if (isCorrect) 1 else 0,
                incorrectCount = it.incorrectCount + if (!isCorrect) 1 else 0
            )
        }
    }

    fun nextExercise() {
        cancelTimer()
        val state = _uiState.value
        if (state.currentIndex >= state.totalExercises) {
            _uiState.update { it.copy(sessionComplete = true) }
            return
        }
        _uiState.update {
            it.copy(
                userInput = "",
                feedback = null,
                exercise = null,
                evaluationWord = null
            )
        }
        loadNextExercise()
    }

    private fun loadNextExercise() {
        viewModelScope.launch {
            val stateBefore = _uiState.value
            if (stateBefore.currentIndex >= stateBefore.totalExercises) {
                _uiState.update { it.copy(sessionComplete = true) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            getNextClozeExercise(shownExerciseIds.toSet())
                .onSuccess { (exercise, word) ->
                    shownExerciseIds.add(exercise.id)
                    _uiState.update {
                        it.copy(
                            exercise = exercise,
                            evaluationWord = word,
                            isLoading = false,
                            currentIndex = it.currentIndex + 1
                        )
                    }
                    startTimer()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = error.message ?: "Ошибка загрузки"
                        )
                    }
                }
        }
    }

    /** Повтор после сетевой ошибки. */
    fun retryLoad() {
        _uiState.update { it.copy(loadError = null) }
        loadNextExercise()
    }

    fun retrySession() {
        cancelTimer()
        shownExerciseIds.clear()
        viewModelScope.launch {
            val goal = settingsRepository.getDailyGoal().firstOrNull() ?: 10
            val timerSec = (settingsRepository.getClozeTimerSeconds().firstOrNull() ?: 35)
                .coerceIn(30, 45)
            _uiState.value = UiState(
                totalExercises = goal,
                timerTotalSeconds = timerSec,
                timerSecondsRemaining = timerSec
            )
            loadNextExercise()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimer()
    }
}
