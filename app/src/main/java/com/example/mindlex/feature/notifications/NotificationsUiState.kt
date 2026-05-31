package com.example.mindlex.feature.notifications

import com.example.mindlex.domain.model.AppNotification
import java.util.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class NotificationRowState(
    val id: Long,
    val title: String,
    val message: String,
    val timeLabel: String,
    val isRead: Boolean
)

/** Список уведомлений за сегодня и счётчики для шапки. */
data class NotificationsUiState(
    val items: List<NotificationRowState> = emptyList(),
    val unreadCount: Int = 0,
    val hasUnread: Boolean = false,
    val unreadSummary: String? = null
) {
    companion object {
        fun from(notifications: List<AppNotification>): NotificationsUiState {
            val items = notifications.map(::toRow)
            val unread = items.count { !it.isRead }
            return NotificationsUiState(
                items = items,
                unreadCount = unread,
                hasUnread = unread > 0,
                unreadSummary = when {
                    unread <= 0 -> null
                    unread > 9 -> "9+"
                    else -> unread.toString()
                }
            )
        }

        private fun toRow(notification: AppNotification): NotificationRowState {
            val time = Instant.fromEpochMilliseconds(notification.createdAtEpochMs)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            return NotificationRowState(
                id = notification.id,
                title = notification.title,
                message = notification.message,
                timeLabel = String.format(Locale.getDefault(), "%02d:%02d", time.hour, time.minute),
                isRead = notification.isRead
            )
        }
    }
}
