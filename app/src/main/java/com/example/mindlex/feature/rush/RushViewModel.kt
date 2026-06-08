package com.example.mindlex.feature.rush

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.UserAnswer
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.CalculateRushScore
import com.example.mindlex.domain.usecase.EvaluateAnswer
import com.example.mindlex.domain.usecase.GetRandomWordForRush
import com.example.mindlex.domain.usecase.UpdateWordProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
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
class RushViewModel @Inject constructor(
    private val getRandomWordForRush: GetRandomWordForRush,
    private val evaluateAnswer: EvaluateAnswer,
    private val updateProgress: UpdateWordProgress,
    private val calculateRushScore: CalculateRushScore,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RushUiState())
    val uiState: StateFlow<RushUiState> = _uiState

    private val recentWordIds = mutableListOf<String>()
    private var timerJob: Job? = null
    private var sessionEndHandled = false

    init {
        viewModelScope.launch {
            val best = settingsRepository.getRushBestScore().firstOrNull() ?: 0
            val comboRec = settingsRepository.getRushMaxComboRecord().firstOrNull() ?: 0
            _uiState.update { it.copy(recordBestScore = best, recordMaxCombo = comboRec) }
            beginNewSession()
        }
    }

    fun onUserInputChanged(value: String) {
        _uiState.update { it.copy(userInput = value) }
    }

    fun submitAnswer() {
        val state = _uiState.value
        if (!state.canAnswer) return
        val word = state.currentWord ?: return
        val input = state.userInput.trim()
        if (input.isBlank()) return

        val review = evaluateAnswer(
            UserAnswer(
                wordId = word.id,
                userInput = input,
                isCorrect = false,
                responseTimeMs = System.currentTimeMillis(),
                timestamp = Clock.System.now()
            ),
            word
        )
        val correct = review.quality >= 3

        viewModelScope.launch { updateProgress(review) }

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
            loadNextWordInternal().onFailure { finishSession() }
        }
    }

    fun skipWord() {
        val state = _uiState.value
        if (!state.canAnswer) return
        _uiState.update {
            it.copy(comboStreak = 0, skipCount = it.skipCount + 1, userInput = "")
        }
        viewModelScope.launch {
            if (!_uiState.value.sessionRunning || _uiState.value.sessionFinished) return@launch
            loadNextWordInternal().onFailure { finishSession() }
        }
    }

    fun retryLoad() = playAgain()

    fun playAgain() {
        viewModelScope.launch {
            recentWordIds.clear()
            beginNewSession()
        }
    }

    override fun onCleared() {
        cancelTimer()
        super.onCleared()
    }

    private suspend fun beginNewSession() {
        cancelTimer()
        sessionEndHandled = false
        val total = (settingsRepository.getRushSessionSeconds().firstOrNull() ?: 90).coerceIn(60, 90)
        val best = settingsRepository.getRushBestScore().firstOrNull() ?: 0
        val comboRec = settingsRepository.getRushMaxComboRecord().firstOrNull() ?: 0

        _uiState.update {
            RushUiState(
                timerTotalSeconds = total,
                timerSecondsRemaining = total,
                recordBestScore = best,
                recordMaxCombo = comboRec,
                isLoading = true
            )
        }

        loadNextWordInternal()
            .onSuccess {
                _uiState.update { it.copy(sessionRunning = true, isLoading = false) }
                startSessionTimer()
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

    private suspend fun loadNextWordInternal(): Result<Unit> {
        if (_uiState.value.sessionFinished) return Result.failure(IllegalStateException("finished"))
        return getRandomWordForRush(recentWordIds.toSet()).fold(
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

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    companion object {
        private const val MAX_RECENT_EXCLUDE = 24
        private val MILESTONES = setOf(5, 10, 15, 20)
    }
}
