package com.example.mindlex.feature.synonym_chain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.SynonymChain
import com.example.mindlex.domain.model.WordStatus
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.GetNextChainStep
import com.example.mindlex.domain.usecase.UpdateWordProgress
import com.example.mindlex.domain.usecase.ValidateSynonym
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber

@HiltViewModel
class SynonymChainViewModel @Inject constructor(
    private val getNextChainStep: GetNextChainStep,
    private val validateSynonym: ValidateSynonym,
    private val updateWordProgress: UpdateWordProgress,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    data class ChainSession(
        val chainId: String = "",
        val startStep: Int = 1,
        val currentStep: Int = 1,
        val collectedWords: List<String> = emptyList(),
        val targetWord: String = "",
        val validSynonyms: List<String> = emptyList()
    )

    data class UiState(
        val isLoading: Boolean = true,
        val userInput: String = "",
        val chainSession: ChainSession? = null,
        val progressInChain: Int = 1,
        val targetChainLength: Int = 3,
        val shownHints: List<String> = emptyList(),
        val hintVisible: Boolean = false,
        val hintUsedCurrentStep: Boolean = false,
        val incorrectMessage: String? = null,
        val chainsCompletedSession: Int = 0,
        val hintsUsedSession: Int = 0,
        val skipCountSession: Int = 0,
        val chainCompleted: Boolean = false,
        val loadError: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        startNewChain()
    }

    fun onUserInputChanged(value: String) {
        _uiState.update { it.copy(userInput = value, incorrectMessage = null) }
    }

    fun showHint() {
        val session = _uiState.value.chainSession ?: return
        if (_uiState.value.hintVisible) return

        val hints = session.validSynonyms.shuffled().take(3)
        _uiState.update {
            it.copy(
                shownHints = hints,
                hintVisible = true,
                hintUsedCurrentStep = true,
                hintsUsedSession = it.hintsUsedSession + 1
            )
        }
    }

    fun checkAnswer() {
        val state = _uiState.value
        val session = state.chainSession ?: return
        val input = state.userInput.trim()
        if (input.isBlank()) return

        val isValid = validateSynonym(input, session.validSynonyms)
        if (!isValid) {
            _uiState.update { it.copy(incorrectMessage = "Попробуйте другой синоним") }
            return
        }

        // Сохраняем в цепочке именно ввод пользователя, чтобы не подменять его случайным вариантом.
        val acceptedWord = input

        val updatedWords = session.collectedWords + acceptedWord
        saveStepProgress(
            chainId = session.chainId,
            stepNumber = session.currentStep,
            isCorrect = true
        )

        if (updatedWords.size >= state.targetChainLength) {
            _uiState.update {
                it.copy(
                    chainSession = session.copy(collectedWords = updatedWords),
                    progressInChain = state.targetChainLength,
                    chainCompleted = true,
                    userInput = "",
                    incorrectMessage = null,
                    hintVisible = false
                )
            }
            onChainCompleted(updatedWords.size)
            return
        }

        loadNextStep(
            chainId = session.chainId,
            stepNumber = session.currentStep + 1,
            collectedWords = updatedWords
        )
    }

    fun skipCurrentWord() {
        val session = _uiState.value.chainSession ?: return
        saveStepProgress(
            chainId = session.chainId,
            stepNumber = session.currentStep,
            isCorrect = false
        )
        _uiState.update { it.copy(skipCountSession = it.skipCountSession + 1) }
        startNewChain()
    }

    fun continueWithNextChain() {
        startNewChain()
    }

    private fun startNewChain() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    userInput = "",
                    chainCompleted = false,
                    hintVisible = false,
                    shownHints = emptyList(),
                    hintUsedCurrentStep = false,
                    progressInChain = 1,
                    loadError = null,
                    incorrectMessage = null,
                    chainSession = null
                )
            }

            val language = settingsRepository.getSelectedLanguage().firstOrNull() ?: "en"
            val category = settingsRepository.getSelectedCategory().firstOrNull() ?: "general"

            getNextChainStep.getRandomChainStartStep(language, category)
                .onSuccess { startStep ->
                    val initialWords = listOf(startStep.word)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chainSession = ChainSession(
                                chainId = startStep.chainId,
                                startStep = startStep.stepNumber,
                                currentStep = startStep.stepNumber,
                                collectedWords = initialWords,
                                targetWord = startStep.word,
                                validSynonyms = startStep.validSynonyms
                            ),
                            progressInChain = 1
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error, "[SynonymChainVM] Ошибка старта цепочки")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = error.message ?: "Не удалось загрузить цепочку"
                        )
                    }
                }
        }
    }

    private fun loadNextStep(
        chainId: String,
        stepNumber: Int,
        collectedWords: List<String>
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    userInput = "",
                    hintVisible = false,
                    shownHints = emptyList(),
                    hintUsedCurrentStep = false,
                    incorrectMessage = null
                )
            }

            val language = settingsRepository.getSelectedLanguage().firstOrNull() ?: "en"
            val nextStepResult = getNextChainStep(chainId, stepNumber, language)

            nextStepResult
                .onSuccess { nextStep ->
                    val fallbackStep = buildFallbackStep(chainId, stepNumber, collectedWords.last())
                    val resolved = nextStep ?: fallbackStep
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chainSession = ChainSession(
                                chainId = chainId,
                                startStep = it.chainSession?.startStep ?: 1,
                                currentStep = stepNumber,
                                collectedWords = collectedWords,
                                targetWord = resolved.word,
                                validSynonyms = resolved.validSynonyms
                            ),
                            progressInChain = collectedWords.size
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error, "[SynonymChainVM] Ошибка загрузки шага цепочки")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = error.message ?: "Не удалось загрузить следующий шаг"
                        )
                    }
                }
        }
    }

    private fun buildFallbackStep(
        chainId: String,
        stepNumber: Int,
        currentWord: String
    ): SynonymChain {
        // Если следующий шаг в БД не найден, даём мягкий fallback, чтобы сессия не прерывалась.
        return SynonymChain(
            id = "fallback:$chainId:$stepNumber",
            chainId = chainId,
            stepNumber = stepNumber,
            word = currentWord,
            validSynonyms = listOf(currentWord),
            difficulty = 1,
            category = "general"
        )
    }

    private fun saveStepProgress(
        chainId: String,
        stepNumber: Int,
        isCorrect: Boolean
    ) {
        viewModelScope.launch {
            val review = ReviewResult(
                wordId = "synonym_chain:$chainId:$stepNumber",
                quality = if (isCorrect) 4 else 2,
                nextReviewAt = Clock.System.now(),
                newStatus = if (isCorrect) WordStatus.KNOWN else WordStatus.LEARNING
            )
            updateWordProgress(review).onFailure {
                Timber.e(it, "[SynonymChainVM] Ошибка сохранения прогресса шага")
            }
        }
    }

    private fun onChainCompleted(chainLength: Int) {
        viewModelScope.launch {
            val completedNow = (_uiState.value.chainsCompletedSession + 1)
            _uiState.update {
                it.copy(
                    chainsCompletedSession = completedNow
                )
            }

            val currentCompleted = settingsRepository.getSynonymChainsCompleted().firstOrNull() ?: 0
            val currentAvg = settingsRepository.getSynonymChainAvgLength().firstOrNull() ?: 0.0

            val newCompleted = currentCompleted + 1
            val newAvg = if (currentCompleted <= 0) {
                chainLength.toDouble()
            } else {
                ((currentAvg * currentCompleted) + chainLength) / newCompleted
            }

            settingsRepository.setSynonymChainsCompleted(newCompleted)
            settingsRepository.setSynonymChainAvgLength(newAvg)
        }
    }
}
