package com.example.mindlex.feature.rush

import com.example.mindlex.domain.model.Word
import com.example.mindlex.feature.mechanics.common.formatMmSs

/** Спринт: таймер, комбо и итоговая статистика. */
data class RushUiState(
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
) {
    val timerLabel: String
        get() = formatMmSs(timerSecondsRemaining)

    val timerTotalLabel: String
        get() = formatMmSs(timerTotalSeconds)

    val timerProgress: Float
        get() = if (timerTotalSeconds > 0) {
            timerSecondsRemaining.toFloat() / timerTotalSeconds
        } else {
            0f
        }

    val timerUrgent: Boolean
        get() = timerSecondsRemaining in 1..15 && sessionRunning && !sessionFinished

    val comboMultiplier: Double
        get() = comboMultiplierForStreak(comboStreak)

    val comboMultiplierLabel: String
        get() {
            val mult = comboMultiplier
            return if (mult % 1.0 == 0.0) mult.toInt().toString() else "%.1f".format(mult)
        }

    val canAnswer: Boolean
        get() = sessionRunning && !sessionFinished && !isLoading && currentWord != null
}

private fun comboMultiplierForStreak(streak: Int): Double = when {
    streak >= 20 -> 3.0
    streak >= 10 -> 2.0
    streak >= 5 -> 1.5
    else -> 1.0
}
