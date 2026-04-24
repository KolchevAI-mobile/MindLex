package com.example.mindlex.domain.usecase

import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Расчёт очков за правильный ответ в спринте «перевод на скорость».
 * База × комбо-множитель; бонус +20% если до конца сессии осталось больше 30 сек.
 */
class CalculateRushScore @Inject constructor() {

    /**
     * @param comboAfterCorrect текущая серия правильных подряд после засчитанного ответа
     * @param timeRemainingSeconds оставшееся время сессии в момент ответа
     */
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

    /** Множитель для отображения (x1, x1.5, x2, x3). */
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
