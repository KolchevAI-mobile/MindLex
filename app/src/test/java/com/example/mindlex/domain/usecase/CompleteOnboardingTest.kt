package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.repository.OnboardingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteOnboardingTest {

    @Test
    fun `rejects blank name`() = runTest {
        val repo = mockk<OnboardingRepository>(relaxed = true)
        val useCase = CompleteOnboarding(repo)

        val r = useCase("  ", "en")

        assertTrue(r.isFailure)
        coVerify(exactly = 0) { repo.completeOnboarding(any(), any()) }
    }

    @Test
    fun `trims and completes`() = runTest {
        val repo = mockk<OnboardingRepository>(relaxed = true) {
            coEvery { completeOnboarding(any(), any()) } coAnswers { }
        }
        val useCase = CompleteOnboarding(repo)

        val r = useCase("  Ann  ", "de")

        assertTrue(r.isSuccess)
        coVerify { repo.completeOnboarding("Ann", "de") }
    }

    @Test
    fun `maps repository error`() = runTest {
        val repo = mockk<OnboardingRepository> {
            coEvery { completeOnboarding(any(), any()) } throws RuntimeException("net")
        }
        val useCase = CompleteOnboarding(repo)

        val r = useCase("Bob", "en")

        assertTrue(r.isFailure)
    }

}
