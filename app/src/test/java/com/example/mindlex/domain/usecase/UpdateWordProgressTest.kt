package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.ReviewResult
import com.example.mindlex.domain.model.WordStatus
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.minus
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateWordProgressTest {

    @Test
    fun `saves new progress`() = runTest {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val progressRepo = mockk<WordProgressRepository>(relaxed = true) {
            coEvery { getProgressForWord("w1") } returns null
        }
        val settings = mockk<SettingsRepository> {
            coEvery { getLastStudyDate() } returns flowOf(today)
            coEvery { getCurrentStreak() } returns flowOf(0)
        }
        val calc = CalculateNextReview()
        val useCase = UpdateWordProgress(
            progressRepository = progressRepo,
            settingsRepository = settings,
            calculateNextReview = calc
        )
        val result = ReviewResult(
            wordId = "w1",
            quality = 5,
            nextReviewAt = now,
            newStatus = WordStatus.LEARNING
        )
        val r = useCase(result)
        assertTrue(r.isSuccess)
        coVerify { progressRepo.saveProgress(any()) }
    }

    @Test
    fun `increments streak when first study of day`() = runTest {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val yesterday = today.minus(DatePeriod(days = 1)).toString()
        val progressRepo = mockk<WordProgressRepository>(relaxed = true) {
            coEvery { getProgressForWord("w1") } returns null
        }
        val settings = mockk<SettingsRepository>(relaxed = true) {
            coEvery { getLastStudyDate() } returns flowOf(yesterday)
            coEvery { getCurrentStreak() } returns flowOf(2)
        }
        val useCase = UpdateWordProgress(
            progressRepository = progressRepo,
            settingsRepository = settings,
            calculateNextReview = CalculateNextReview()
        )
        val r = useCase(
            ReviewResult(
                wordId = "w1",
                quality = 4,
                nextReviewAt = now,
                newStatus = WordStatus.LEARNING
            )
        )
        assertTrue(r.isSuccess)
        coVerify { settings.setCurrentStreak(3) }
        coVerify { settings.setLastStudyDate(today.toString()) }
    }
}
