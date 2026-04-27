package com.example.mindlex.domain.usecase

import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.model.WordProgress
import com.example.mindlex.domain.model.WordStatus
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetNextWordForPracticeTest {

    private val now = Clock.System.now()
    private val w1 = Word("w1", "a", "а", emptyList(), "en")
    private val pDue = sampleProgress("p1", "w1")

    @Test
    fun `picks from due reviews first`() = runTest {
        val progressRepo = mockk<WordProgressRepository> {
            coEvery { getDueReviews(any()) } returns flowOf(listOf(pDue))
            coEvery { getWordById("w1") } returns w1
            coEvery { getNewWords(any()) } returns flowOf(emptyList())
            coEvery { getLearningWords(any()) } returns flowOf(emptyList())
        }
        val settings = mockk<SettingsRepository> {
            coEvery { getSelectedCategory() } returns flowOf("general")
        }
        val vocab = mockk<VocabularyRepository> {
            coEvery {
                getRandomWordByCategoryExcluding(
                    any(),
                    LearningDefaults.VOCABULARY_FETCH_LIMIT,
                    any(),
                    any()
                )
            } returns Result.failure(Exception("unused"))
        }
        val r = GetNextWordForPractice(
            vocabularyRepository = vocab,
            progressRepository = progressRepo,
            settingsRepository = settings
        )()

        assertTrue(r.isSuccess)
        assertEquals(w1, r.getOrNull())
    }

    @Test
    fun `excludes ids from due list`() = runTest {
        val progressRepo = mockk<WordProgressRepository> {
            coEvery { getDueReviews(any()) } returns flowOf(listOf(pDue))
            coEvery { getNewWords(LearningDefaults.PROGRESS_CANDIDATE_LIMIT) } returns flowOf(
                listOf(sampleProgress("np", "w2"))
            )
            coEvery { getWordById("w2") } returns Word("w2", "b", "б", emptyList(), "en")
            coEvery { getLearningWords(any()) } returns flowOf(emptyList())
        }
        val settings = mockk<SettingsRepository> {
            coEvery { getSelectedCategory() } returns flowOf("general")
        }
        val vocab = mockk<VocabularyRepository>(relaxed = true)
        val r = GetNextWordForPractice(
            vocabularyRepository = vocab,
            progressRepository = progressRepo,
            settingsRepository = settings
        )(excludedIds = setOf("w1"))

        assertTrue(r.isSuccess)
        assertEquals("w2", r.getOrNull()!!.id)
    }

    private fun sampleProgress(id: String, wordId: String) = WordProgress(
        id = id,
        wordId = wordId,
        status = WordStatus.REVIEW,
        level = 2,
        easeFactor = 2.0,
        intervalDays = 1,
        nextReviewAt = now,
        lastReviewedAt = now,
        correctCount = 0,
        incorrectCount = 0,
        createdAt = now,
        updatedAt = now
    )
}
