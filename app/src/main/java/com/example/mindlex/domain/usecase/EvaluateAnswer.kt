package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.UserAnswer
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.model.WordStatus
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.math.min

/**
 * Use case для оценки ответа пользователя.
 * Сравнивает ввод с правильным ответом и вычисляет качество ответа.
 */
class EvaluateAnswer @Inject constructor() {

    /**
     * Оценивает ответ пользователя.
     *
     * @param userAnswer Ответ пользователя
     * @param correctWord Правильное слово с возможными альтернативами
     * @return Результат оценки с качеством ответа
     */
    operator fun invoke(
        userAnswer: UserAnswer,
        correctWord: Word
    ): ReviewResult {
        // Собираем все правильные варианты (основной + альтернативы)
        val allCorrectAnswers = listOf(correctWord.wordForeign) + correctWord.alternativeTranslations
        
        // Находим лучшее совпадение (минимальное расстояние Левенштейна)
        val bestMatch = allCorrectAnswers.minByOrNull { answer ->
            levenshteinDistance(userAnswer.userInput.lowercase(), answer.lowercase())
        }
        
        val distance = bestMatch?.let { answer ->
            levenshteinDistance(userAnswer.userInput.lowercase(), answer.lowercase())
        } ?: Int.MAX_VALUE

        // Определяем качество ответа (1-5)
        val quality = when {
            distance == 0 -> 5 // Идеально
            distance <= 1 -> 4 // Почти идеально (1 опечатка)
            distance <= 2 -> 3 // Хорошо (2 опечатки)
            distance <= 4 -> 2 // Плохо
            else -> 1          // Очень плохо
        }

        return ReviewResult(
            wordId = correctWord.id,
            quality = quality,
            nextReviewAt = Clock.System.now(),
            newStatus = WordStatus.LEARNING
        )
    }

    /**
     * Вычисляет расстояние Левенштейна между двумя строками.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length

        if (m == 0) return n
        if (n == 0) return m

        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    dp[i - 1][j] + 1,      // удаление
                    min(
                        dp[i][j - 1] + 1,  // вставка
                        dp[i - 1][j - 1] + cost // замена
                    )
                )
            }
        }

        return dp[m][n]
    }
}
