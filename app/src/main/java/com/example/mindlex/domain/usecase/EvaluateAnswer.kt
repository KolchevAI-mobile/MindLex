package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.UserAnswer
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.model.WordStatus
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.math.min

class EvaluateAnswer @Inject constructor() {

    companion object {
        const val ACCEPTANCE_QUALITY_MIN: Int = 3
        const val HINT_RESPONSE_QUALITY: Int = 3
    }

    operator fun invoke(
        userAnswer: UserAnswer,
        correctWord: Word
    ): ReviewResult {
        val allCorrectAnswers = listOf(correctWord.wordForeign) + correctWord.alternativeTranslations

        val bestMatch = allCorrectAnswers.minByOrNull { answer ->
            levenshteinDistance(userAnswer.userInput.lowercase(), answer.lowercase())
        }
        
        val distance = bestMatch?.let { answer ->
            levenshteinDistance(userAnswer.userInput.lowercase(), answer.lowercase())
        } ?: Int.MAX_VALUE

        val quality = when {
            distance == 0 -> 5
            distance <= 1 -> 4
            distance <= 2 -> 3
            distance <= 4 -> 2
            else -> 1
        }

        return ReviewResult(
            wordId = correctWord.id,
            quality = quality,
            nextReviewAt = Clock.System.now(),
            newStatus = WordStatus.LEARNING
        )
    }

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
                    dp[i - 1][j] + 1,
                    min(
                        dp[i][j - 1] + 1,
                        dp[i - 1][j - 1] + cost
                    )
                )
            }
        }

        return dp[m][n]
    }
}
