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

@HiltViewModel
class ClozeViewModel @Inject constructor(
    private val getNextClozeExercise: GetNextClozeExercise,
    private val evaluateAnswer: EvaluateAnswer,
    private val updateProgress: UpdateWordProgress,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClozeUiState())
    val uiState: StateFlow<ClozeUiState> = _uiState

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

    fun onTimeExpired() {
        cancelTimer()
        val state = _uiState.value
        val exercise = state.exercise ?: return
        val word = state.evaluationWord ?: return
        if (state.feedback != null) return

        viewModelScope.launch {
            updateProgress(
                ReviewResult(
                    wordId = word.id,
                    quality = 3,
                    nextReviewAt = Clock.System.now(),
                    newStatus = WordStatus.LEARNING
                )
            )
        }

        _uiState.update {
            it.copy(
                feedback = ClozeFeedback(
                    isCorrect = false,
                    quality = 3,
                    message = "Время вышло",
                    fullSentence = filledSentence(exercise),
                    timedOut = true
                ),
                incorrectCount = it.incorrectCount + 1
            )
        }
    }

    fun checkAnswer() {
        cancelTimer()
        val state = _uiState.value
        val exercise = state.exercise ?: return
        val word = state.evaluationWord ?: return
        val trimmed = state.userInput.trim()
        if (trimmed.isBlank() || state.feedback != null) return

        val reviewResult = evaluateAnswer(
            UserAnswer(
                wordId = word.id,
                userInput = trimmed,
                isCorrect = false,
                responseTimeMs = System.currentTimeMillis(),
                timestamp = Clock.System.now()
            ),
            word
        )

        viewModelScope.launch { updateProgress(reviewResult) }

        val correct = reviewResult.quality >= 3
        _uiState.update {
            it.copy(
                feedback = ClozeFeedback(
                    isCorrect = correct,
                    quality = reviewResult.quality,
                    message = if (correct) "Правильно!" else "Неправильно",
                    fullSentence = filledSentence(exercise)
                ),
                correctCount = it.correctCount + if (correct) 1 else 0,
                incorrectCount = it.incorrectCount + if (!correct) 1 else 0
            )
        }
    }

    fun nextExercise() {
        cancelTimer()
        if (_uiState.value.currentIndex >= _uiState.value.totalExercises) {
            _uiState.update { it.copy(sessionComplete = true) }
            return
        }
        _uiState.update {
            it.copy(userInput = "", feedback = null, exercise = null, evaluationWord = null)
        }
        loadNextExercise()
    }

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
            _uiState.value = ClozeUiState(
                totalExercises = goal,
                timerTotalSeconds = timerSec,
                timerSecondsRemaining = timerSec
            )
            loadNextExercise()
        }
    }

    override fun onCleared() {
        cancelTimer()
        super.onCleared()
    }

    private fun loadNextExercise() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.currentIndex >= state.totalExercises) {
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

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun filledSentence(exercise: ClozeExercise): String {
        val sentence = exercise.sentenceWithBlank
        return if (sentence.contains("___")) {
            sentence.replaceFirst("___", exercise.correctAnswer)
        } else {
            "$sentence ${exercise.correctAnswer}"
        }
    }
}
