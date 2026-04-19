package com.example.mindlex.feature.active_recall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.UserAnswer
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.EvaluateAnswer
import com.example.mindlex.domain.usecase.GetNextWordForPractice
import com.example.mindlex.domain.usecase.UpdateWordProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel для экрана активного вспоминания.
 */
@HiltViewModel
class ActiveRecallViewModel @Inject constructor(
    private val getNextWord: GetNextWordForPractice,
    private val evaluateAnswer: EvaluateAnswer,
    private val updateProgress: UpdateWordProgress,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /**
     * Состояние UI экрана.
     */
    data class UiState(
        val currentWord: Word? = null,
        val userInput: String = "",
        val feedback: Feedback? = null,
        val currentWordIndex: Int = 0,
        val totalWords: Int = 10,
        val isLoading: Boolean = false,
        val hintShown: Boolean = false,
        val sessionComplete: Boolean = false,
        // Статистика сессии
        val correctCount: Int = 0,
        val incorrectCount: Int = 0,
        val hintUsedCount: Int = 0
    )

    /**
     * Обратная связь после проверки ответа.
     *
     * @property isCorrect Правильный ли ответ
     * @property quality Качество ответа (0-5)
     * @property message Сообщение для пользователя
     * @property usedHint Была ли использована подсказка
     */
    data class Feedback(
        val isCorrect: Boolean,
        val quality: Int,
        val message: String,
        val usedHint: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            launch {
                val dailyGoal = settingsRepository.getDailyGoal().firstOrNull() ?: 10
                _uiState.update { it.copy(totalWords = dailyGoal) }
                Timber.d("[ActiveRecallVM] Daily goal loaded: $dailyGoal")
            }
            loadNextWord()
        }
    }

    /**
     * Обновляет текст, введённый пользователем.
     */
    fun onUserInputChanged(input: String) {
        _uiState.update { it.copy(userInput = input) }
    }

    /**
     * Показывает подсказку (перевод слова).
     * Сразу создаёт feedback с флагом usedHint, минуя этап ввода.
     */
    fun showHint() {
        val currentState = _uiState.value
        val currentWord = currentState.currentWord ?: run {
            Timber.w("[ActiveRecallVM] Попытка показать подсказку без текущего слова")
            return
        }

        // Создаём нейтральный feedback для использования подсказки
        val feedback = Feedback(
            isCorrect = true, // Технически правильно, но без подсказки не засчитывается  
            quality = 3, // Среднее качество при использовании подсказки
            message = "Слово запомнено. Повторим позже.",
            usedHint = true
        )

        // Обновляем статистику (подсказка считается как отдельная категория)
        _uiState.update { 
            it.copy(
                hintShown = true,
                feedback = feedback,
                hintUsedCount = it.hintUsedCount + 1
            ) 
        }

        // Сохраняем прогресс с коротким интервалом повторения
        viewModelScope.launch {
            val reviewResult = com.example.mindlex.domain.model.ReviewResult(
                wordId = currentWord.id,
                quality = 3, // Низкое качество при подсказке
                nextReviewAt = kotlinx.datetime.Clock.System.now(),
                newStatus = com.example.mindlex.domain.model.WordStatus.LEARNING
            )
            
            updateProgress(reviewResult)
                .onSuccess {
                    Timber.d("[ActiveRecallVM] Прогресс сохранён для слова ${currentWord.id} (с подсказкой)")
                }
                .onFailure { error ->
                    Timber.e(error, "[ActiveRecallVM] Ошибка сохранения прогресса")
                }
        }

        Timber.d("[ActiveRecallVM] Подсказка показана для слова: ${currentWord.wordNative}")
    }

    /**
     * Проверяет ответ пользователя.
     */
    fun checkAnswer() {
        val currentState = _uiState.value
        val currentWord = currentState.currentWord ?: run {
            Timber.w("[ActiveRecallVM] Попытка проверки без текущего слова")
            return
        }
        val userInput = currentState.userInput.trim()

        if (userInput.isBlank()) {
            Timber.w("[ActiveRecallVM] Пустой ввод")
            return
        }

        val responseTimeMs = System.currentTimeMillis()

        val userAnswer = UserAnswer(
            wordId = currentWord.id,
            userInput = userInput,
            isCorrect = false,
            responseTimeMs = responseTimeMs,
            timestamp = Clock.System.now()
        )

        // Оцениваем ответ
        val reviewResult = evaluateAnswer(userAnswer, currentWord)
        Timber.d("[ActiveRecallVM] Оценка ответа: quality=${reviewResult.quality}, word=${currentWord.wordForeign}")

        // Сохраняем прогресс
        viewModelScope.launch {
            updateProgress(reviewResult)
                .onSuccess {
                    Timber.d("[ActiveRecallVM] Прогресс сохранён для слова ${currentWord.id}")
                }
                .onFailure { error ->
                    Timber.e(error, "[ActiveRecallVM] Ошибка сохранения прогресса")
                }
        }

        // Формируем обратную связь
        val usedHint = currentState.hintShown
        val isCorrect = reviewResult.quality >= 3  // quality >= 3 считается правильным ответом
        val feedback = createFeedback(isCorrect, reviewResult.quality, usedHint)

        // Обновляем статистику
        when {
            usedHint -> _uiState.update { it.copy(hintUsedCount = it.hintUsedCount + 1) }
            isCorrect -> _uiState.update { it.copy(correctCount = it.correctCount + 1) }
            else -> _uiState.update { it.copy(incorrectCount = it.incorrectCount + 1) }
        }

        _uiState.update {
            it.copy(
                feedback = feedback,
                hintShown = false // Сбрасываем подсказку для следующего слова
            )
        }
    }

    /**
     * Загружает следующее слово.
     */
    fun nextWord() {
        _uiState.update {
            it.copy(
                userInput = "",
                feedback = null,
                hintShown = false
            )
        }
        
        // Проверяем завершение сессии
        val currentState = _uiState.value
        if (currentState.currentWordIndex >= currentState.totalWords) {
            _uiState.update { it.copy(sessionComplete = true) }
            Timber.d("[ActiveRecallVM] Сессия завершена: ${currentState.currentWordIndex}/${currentState.totalWords}")
            return
        }
        
        loadNextWord()
    }

    private fun loadNextWord() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getNextWord()
                .onSuccess { word ->
                    Timber.d("[ActiveRecallVM] Загружено слово: wordNative=${word.wordNative}, wordForeign=${word.wordForeign}")
                    _uiState.update {
                        it.copy(
                            currentWord = word,
                            isLoading = false,
                            currentWordIndex = it.currentWordIndex + 1
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error, "[ActiveRecallVM] Ошибка загрузки слова")
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                }
        }
    }

    /**
     * Создаёт обратную связь в зависимости от правильности ответа и использования подсказки.
     * 
     * Если использована подсказка — показываем нейтральное сообщение без акцента на правильности.
     */
    private fun createFeedback(isCorrect: Boolean, quality: Int, usedHint: Boolean): Feedback {
        return if (usedHint) {
            // При использовании подсказки — нейтральное сообщение
            Feedback(
                isCorrect = true, // Технически правильно, но без подсказки не засчитывается
                quality = quality.coerceAtMost(3), // Ограничиваем качество при подсказке
                message = "Слово запомнено. Повторим позже.",
                usedHint = true
            )
        } else {
            // Без подсказки — полноценная обратная связь
            val message = when {
                quality >= 4 -> "Отлично!"
                quality >= 3 -> "Хорошо!"
                quality >= 2 -> "Верно, но повторим"
                else -> "Повторим сегодня"
            }
            Feedback(
                isCorrect = isCorrect,
                quality = quality,
                message = message,
                usedHint = false
            )
        }
    }

    /**
     * Перезапускает сессию (кнопка "Попробовать снова").
     */
    fun retrySession() {
        _uiState.update {
            UiState(
                totalWords = it.totalWords,
                correctCount = 0,
                incorrectCount = 0,
                hintUsedCount = 0
            )
        }
        loadNextWord()
        Timber.d("[ActiveRecallVM] Сессия перезапущена")
    }
    suspend fun shouldShowTutorial(): Boolean {
        return settingsRepository.isActiveRecallTutorialShown().firstOrNull() != true
    }

    /**
     * Отмечает, что туториал был показан.
     */
    fun markTutorialShown() {
        viewModelScope.launch {
            settingsRepository.setActiveRecallTutorialShown(true)
            Timber.d("[ActiveRecall] Туториал отмечен как показанный")
        }
    }
}
