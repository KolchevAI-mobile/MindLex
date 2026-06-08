package com.example.mindlex.feature.active_recall

import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.domain.model.Word

data class ActiveRecallFeedback(
    val isCorrect: Boolean,
    val quality: Int,
    val usedHint: Boolean = false
)

/** Состояние сессии активного вспоминания. */
data class ActiveRecallUiState(
    val currentWord: Word? = null,
    val userInput: String = "",
    val feedback: ActiveRecallFeedback? = null,
    val currentWordIndex: Int = 0,
    val totalWords: Int = LearningDefaults.DAILY_GOAL_FALLBACK,
    val isLoading: Boolean = false,
    val hintShown: Boolean = false,
    val sessionComplete: Boolean = false,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val hintUsedCount: Int = 0
) {
    val progressFraction: Float
        get() = if (totalWords > 0) currentWordIndex.toFloat() / totalWords else 0f

    val canInteract: Boolean
        get() = !isLoading && !sessionComplete && feedback == null

    val canShowHint: Boolean
        get() = canInteract && !hintShown && currentWord != null

    val awaitingAnswer: Boolean
        get() = canInteract && !hintShown
}
