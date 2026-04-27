package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.repository.AppNotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ManageTodaysNotificationsTest {

    @Test
    fun `mark as read delegates`() = runTest {
        val repo = mockk<AppNotificationRepository>(relaxed = true)
        val uc = ManageTodaysNotifications(repo)
        uc.markAsRead(44L)
        coVerify { repo.markAsRead(44L) }
    }

    @Test
    fun `delete delegates`() = runTest {
        val repo = mockk<AppNotificationRepository>(relaxed = true)
        ManageTodaysNotifications(repo).deleteNotification(3L)
        coVerify { repo.deleteNotification(3L) }
    }

    @Test
    fun `observe for today uses between`() = runTest {
        val repo = mockk<AppNotificationRepository> {
            every { observeNotificationsBetween(any(), any()) } returns emptyFlow()
        }
        ManageTodaysNotifications(repo).observeNotificationsForToday()
        verify { repo.observeNotificationsBetween(any(), any()) }
    }
}
