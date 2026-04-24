package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.AppNotification
import com.example.mindlex.domain.repository.AppNotificationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Singleton
class ManageTodaysNotifications @Inject constructor(
    private val appNotificationRepository: AppNotificationRepository
) {
    private val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val dayStartEpochMs = todayDate.atStartOfDayIn(TimeZone.currentSystemDefault()).epochSeconds * 1000L
    private val dayEndEpochMs = todayDate
        .plus(DatePeriod(days = 1))
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .epochSeconds * 1000L

    fun observeNotificationsForToday(): Flow<List<AppNotification>> =
        appNotificationRepository.observeNotificationsBetween(
            startEpochMs = dayStartEpochMs,
            endEpochMs = dayEndEpochMs
        )

    suspend fun markAsRead(notificationId: Long) {
        appNotificationRepository.markAsRead(notificationId)
    }

    suspend fun markAllAsReadForToday() {
        appNotificationRepository.markAllAsReadBetween(
            startEpochMs = dayStartEpochMs,
            endEpochMs = dayEndEpochMs
        )
    }

    suspend fun deleteNotification(notificationId: Long) {
        appNotificationRepository.deleteNotification(notificationId)
    }
}
