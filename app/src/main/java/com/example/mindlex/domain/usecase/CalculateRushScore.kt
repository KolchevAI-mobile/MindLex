package com.example.mindlex.domain.usecase

import javax.inject.Inject
import kotlin.math.roundToInt

class CalculateRushScore @Inject constructor() {

    operator fun invoke(
        comboAfterCorrect: Int,
        timeRemainingSeconds: Int
    ): Int {
        val mult = when {
            comboAfterCorrect >= 20 -> 3.0
            comboAfterCorrect >= 10 -> 2.0
            comboAfterCorrect >= 5 -> 1.5
            else -> 1.0
        }
        var total = BASE_POINTS * mult
        if (timeRemainingSeconds > 30) {
            total *= 1.2
        }
        return total.roundToInt().coerceAtLeast(1)
    }

    fun comboMultiplier(comboAfterCorrect: Int): Double = when {
        comboAfterCorrect >= 20 -> 3.0
        comboAfterCorrect >= 10 -> 2.0
        comboAfterCorrect >= 5 -> 1.5
        else -> 1.0
    }

    companion object {
        const val BASE_POINTS = 10
    }
}
