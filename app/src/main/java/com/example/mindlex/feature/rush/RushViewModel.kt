package com.example.mindlex.feature.rush

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.UserAnswer
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.CalculateRushScore
import com.example.mindlex.domain.usecase.EvaluateAnswer
import com.example.mindlex.domain.usecase.GetRandomWordForRush
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
import kotlin.math.roundToInt

@HiltViewModel
class RushViewModel @Inject constructor(
    private val getRandomWordForRush: GetRandomWordForRush,
    private val evaluateAnswer: EvaluateAnswer,
    private val updateProgress: UpdateWordProgress,
    private val calculateRushScore: CalculateRushScore,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    data class UiState(
        val currentWord: Word? = null,
        val userInput: String = "",
        val sessionRunning: Boolean = false,
        val sessionFinished: Boolean = false,
        val timerSecondsRemaining: Int = 90,
        val timerTotalSeconds: Int = 90,
        val score: Int = 0,
        val comboStreak: Int = 0,
        val sessionMaxCombo: Int = 0,
        val correctCount: Int = 0,
        val incorrectCount: Int = 0,
        val skipCount: Int = 0,
        val isLoading: Boolean = true,
        val loadError: String? = null,
        
        val milestonePulse: Int = 0,
        val recordBestScore: Int = 0,
        val recordMaxCombo: Int = 0,
        val wordsPerMinute: Int = 0
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val recentWordIds = mutableListOf<String>()
    private var timerJob: Job? = null
    private var sessionEndHandled = false

    init {
        viewModelScope.launch {
            val best = settingsRepository.getRushBestScore().firstOrNull() ?: 0
            val comboRec = settingsRepository.getRushMaxComboRecord().firstOrNull() ?: 0
            _uiState.update {
                it.copy(recordBestScore = best, recordMaxCombo = comboRec)
            }
            beginNewSession()
        }
    }

    fun onUserInputChanged(s: String) {
        _uiState.update { it.copy(userInput = s) }
    }

    private suspend fun beginNewSession() {
        cancelTimer()
        sessionEndHandled = false
        val total = (settingsRepository.getRushSessionSeconds().firstOrNull() ?: 90).coerceIn(60, 90)
        val best = settingsRepository.getRushBestScore().firstOrNull() ?: 0
        val comboRec = settingsRepository.getRushMaxComboRecord().firstOrNull() ?: 0

        _uiState.update {
            it.copy(
                timerTotalSeconds = total,
                timerSecondsRemaining = total,
                sessionRunning = false,
                sessionFinished = false,
                score = 0,
                comboStreak = 0,
                sessionMaxCombo = 0,
                correctCount = 0,
                incorrectCount = 0,
                skipCount = 0,
                userInput = "",
                currentWord = null,
                loadError = null,
                isLoading = true,
                milestonePulse = 0,
                wordsPerMinute = 0,
                recordBestScore = best,
                recordMaxCombo = comboRec
            )
        }

        val loadResult = loadNextWordInternal()
        if (loadResult.isSuccess) {
            _uiState.update { it.copy(sessionRunning = true, isLoading = false) }
            startSessionTimer()
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadError = loadResult.exceptionOrNull()?.message ?: "Ошибка загрузки"
                )
            }
        }
    }

    private suspend fun loadNextWordInternal(): Result<Unit> {
        if (_uiState.value.sessionFinished) {
            return Result.failure(IllegalStateException("finished"))
        }
        val exclude = recentWordIds.toSet()
        return getRandomWordForRush(exclude).fold(
            onSuccess = { word ->
                recentWordIds.add(word.id)
                while (recentWordIds.size > MAX_RECENT_EXCLUDE) {
                    recentWordIds.removeAt(0)
                }
                _uiState.update { it.copy(currentWord = word, userInput = "") }
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    }

    private fun startSessionTimer() {
        cancelTimer()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val state = _uiState.value
                if (!state.sessionRunning || state.sessionFinished) break
                val left = state.timerSecondsRemaining - 1
                if (left <= 0) {
                    _uiState.update { it.copy(timerSecondsRemaining = 0) }
                    finishSession()
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

    private fun finishSession() {
        if (sessionEndHandled) return
        sessionEndHandled = true
        cancelTimer()
        viewModelScope.launch {
            val state = _uiState.value
            val totalSec = state.timerTotalSeconds.coerceAtLeast(1)
            val wordsSeen = state.correctCount + state.incorrectCount + state.skipCount
            val wpm = (wordsSeen / (totalSec / 60.0)).roundToInt()

            var newBest = state.recordBestScore
            if (state.score > newBest) {
                newBest = state.score
                settingsRepository.setRushBestScore(newBest)
            }
            var newComboRec = state.recordMaxCombo
            if (state.sessionMaxCombo > newComboRec) {
                newComboRec = state.sessionMaxCombo
                settingsRepository.setRushMaxComboRecord(newComboRec)
            }

            _uiState.update {
                it.copy(
                    sessionRunning = false,
                    sessionFinished = true,
                    currentWord = null,
                    wordsPerMinute = wpm,
                    recordBestScore = newBest,
                    recordMaxCombo = newComboRec
                )
            }
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        if (!state.sessionRunning || state.sessionFinished) return
        val word = state.currentWord ?: return
        val input = state.userInput.trim()
        if (input.isBlank()) return

        val userAnswer = UserAnswer(
            wordId = word.id,
            userInput = input,
            isCorrect = false,
            responseTimeMs = System.currentTimeMillis(),
            timestamp = Clock.System.now()
        )
        val review = evaluateAnswer(userAnswer, word)
        val correct = review.quality >= 3

        viewModelScope.launch {
            updateProgress(review)
        }

        if (correct) {
            val newCombo = state.comboStreak + 1
            val points = calculateRushScore(newCombo, state.timerSecondsRemaining)
            val milestone = if (newCombo in MILESTONES) newCombo else 0

            _uiState.update {
                it.copy(
                    score = it.score + points,
                    comboStreak = newCombo,
                    sessionMaxCombo = maxOf(it.sessionMaxCombo, newCombo),
                    correctCount = it.correctCount + 1,
                    milestonePulse = milestone,
                    userInput = ""
                )
            }
            if (milestone > 0) {
                viewModelScope.launch {
                    delay(500)
                    _uiState.update { it.copy(milestonePulse = 0) }
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    comboStreak = 0,
                    incorrectCount = it.incorrectCount + 1,
                    userInput = ""
                )
            }
        }

        viewModelScope.launch {
            if (!_uiState.value.sessionRunning || _uiState.value.sessionFinished) return@launch
            loadNextWordInternal().onFailure {
                finishSession()
            }
        }
    }

    fun skipWord() {
        val state = _uiState.value
        if (!state.sessionRunning || state.sessionFinished) return
        _uiState.update {
            it.copy(
                comboStreak = 0,
                skipCount = it.skipCount + 1,
                userInput = ""
            )
        }
        viewModelScope.launch {
            if (!_uiState.value.sessionRunning || _uiState.value.sessionFinished) return@launch
            loadNextWordInternal().onFailure {
                finishSession()
            }
        }
    }

    fun retryLoad() {
        viewModelScope.launch {
            recentWordIds.clear()
            beginNewSession()
        }
    }

    fun playAgain() {
        viewModelScope.launch {
            recentWordIds.clear()
            beginNewSession()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimer()
    }

    companion object {
        private const val MAX_RECENT_EXCLUDE = 24
        private val MILESTONES = setOf(5, 10, 15, 20)
    }
}
