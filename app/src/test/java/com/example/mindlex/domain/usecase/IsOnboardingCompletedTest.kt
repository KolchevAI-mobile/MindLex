package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.repository.OnboardingRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsOnboardingCompletedTest {

    @Test
    fun `exposes repository flow`() = runTest {
        val repo = mockk<OnboardingRepository> {
            every { isOnboardingCompleted() } returns flowOf(true)
        }
        assertTrue(IsOnboardingCompleted(repo)().first())
    }

    @Test
    fun `can emit false`() = runTest {
        val repo = mockk<OnboardingRepository> {
            every { isOnboardingCompleted() } returns flowOf(false)
        }
        assertFalse(IsOnboardingCompleted(repo)().first())
    }
}
