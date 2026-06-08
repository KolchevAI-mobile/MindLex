package com.example.mindlex.feature.cloze

import com.example.mindlex.domain.model.ClozeExercise
import com.example.mindlex.domain.model.Word
import com.example.mindlex.feature.mechanics.common.formatMmSs

data class ClozeFeedback(
    val isCorrect: Boolean,
    val quality: Int,
    val message: String,
    val fullSentence: String,
    val timedOut: Boolean = false
)

/** Сессия cloze: упражнение, таймер и итоги. */
data class ClozeUiState(
    val exercise: ClozeExercise? = null,
    val evaluationWord: Word? = null,
    val userInput: String = "",
    val feedback: ClozeFeedback? = null,
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
) {
    val progressFraction: Float
        get() = if (totalExercises > 0) currentIndex.toFloat() / totalExercises else 0f

    val timerLabel: String
        get() = formatMmSs(timerSecondsRemaining)

    val timerUrgent: Boolean
        get() = timerSecondsRemaining in 1..10 && feedback == null && exercise != null

    val showTimerInHeader: Boolean
        get() = !sessionComplete && feedback == null && !isLoading && exercise != null

    val awaitingAnswer: Boolean
        get() = feedback == null && exercise != null && !isLoading
}
