package com.example.mindlex.domain.usecase

import com.example.mindlex.core.notifications.StudyNotificationScheduler
import com.example.mindlex.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Test

class RescheduleStudyRemindersTest {

    @Test
    fun `passes settings into scheduler`() = runTest {
        val t = LocalTime(9, 15)
        val settings = mockk<SettingsRepository> {
            coEvery { isNotificationsEnabled() } returns flowOf(true)
            coEvery { getPreferredStudyTime() } returns flowOf(t)
            coEvery { getDailyGoal() } returns flowOf(12)
        }
        val scheduler = mockk<StudyNotificationScheduler>(relaxed = true)
        val useCase = RescheduleStudyReminders(settings, scheduler)
        useCase()
        coVerify { scheduler.rescheduleDailyNotifications(true, t, 12) }
    }
}
