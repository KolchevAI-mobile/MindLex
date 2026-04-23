package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.UserAnswer
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.model.WordStatus
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluateAnswerTest {

    private val evaluateAnswer = EvaluateAnswer()

    @Test
    fun `returns highest quality for exact match ignoring case and punctuation`() {
        val word = sampleWord(
            wordForeign = "Bonjour",
            alternatives = listOf("Salut")
        )
        val answer = sampleAnswer(userInput = "  bonjour!!! ")

        val result = evaluateAnswer(userAnswer = answer, correctWord = word)

        assertEquals(5, result.quality)
        assertEquals(WordStatus.LEARNING, result.newStatus)
    }

    @Test
    fun `accepts one typo as near correct answer`() {
        val word = sampleWord(wordForeign = "bonjour")
        val answer = sampleAnswer(userInput = "bonjor")

        val result = evaluateAnswer(userAnswer = answer, correctWord = word)

        assertEquals(4, result.quality)
    }

    @Test
    fun `matches answer from alternative translations list`() {
        val word = sampleWord(
            wordForeign = "hello",
            alternatives = listOf("hi", "hey")
        )
        val answer = sampleAnswer(userInput = "hey")

        val result = evaluateAnswer(userAnswer = answer, correctWord = word)

        assertEquals(5, result.quality)
        assertTrue(result.nextReviewAt <= Clock.System.now())
    }

    private fun sampleWord(
        wordForeign: String,
        alternatives: List<String> = emptyList()
    ): Word {
        return Word(
            id = "w1",
            wordForeign = wordForeign,
            wordNative = "пример",
            alternativeTranslations = alternatives,
            targetLanguage = "en"
        )
    }

    private fun sampleAnswer(userInput: String): UserAnswer {
        return UserAnswer(
            wordId = "w1",
            userInput = userInput,
            isCorrect = false,
            responseTimeMs = 500,
            timestamp = Clock.System.now()
        )
    }
}
