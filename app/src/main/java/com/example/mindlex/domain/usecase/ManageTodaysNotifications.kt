package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.AppNotification
import com.example.mindlex.domain.repository.AppNotificationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Singleton
class ManageTodaysNotifications @Inject constructor(
    private val appNotificationRepository: AppNotificationRepository
) {

    fun observeNotificationsForToday(): Flow<List<AppNotification>> {
        val bounds = currentDayBounds()
        return appNotificationRepository.observeNotificationsBetween(
            startEpochMs = bounds.start.toEpochMillis(),
            endEpochMs = bounds.end.toEpochMillis()
        )
    }

    suspend fun markAsRead(notificationId: Long) {
        appNotificationRepository.markAsRead(notificationId)
    }

    suspend fun markAllAsReadForToday() {
        val bounds = currentDayBounds()
        appNotificationRepository.markAllAsReadBetween(
            startEpochMs = bounds.start.toEpochMillis(),
            endEpochMs = bounds.end.toEpochMillis()
        )
    }

    suspend fun deleteNotification(notificationId: Long) {
        appNotificationRepository.deleteNotification(notificationId)
    }

    private fun currentDayBounds(): DayBounds {
        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone).date
        return DayBounds(
            date = today,
            start = today.atStartOfDayIn(zone),
            end = today.plus(DatePeriod(days = 1)).atStartOfDayIn(zone)
        )
    }

    private data class DayBounds(
        val date: LocalDate,
        val start: Instant,
        val end: Instant
    )
}

private fun Instant.toEpochMillis(): Long = epochSeconds * 1000L
