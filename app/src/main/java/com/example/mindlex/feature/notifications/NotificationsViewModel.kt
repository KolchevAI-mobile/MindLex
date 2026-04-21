package com.example.mindlex.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.AppNotification
import com.example.mindlex.domain.repository.AppNotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val appNotificationRepository: AppNotificationRepository
) : ViewModel() {

    data class UiState(
        val notifications: List<AppNotification> = emptyList()
    ) {
        val hasUnread: Boolean
            get() = notifications.any { !it.isRead }
    }

    private val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val dayStartEpochMs = todayDate.atStartOfDayIn(TimeZone.currentSystemDefault()).epochSeconds * 1000L
    private val dayEndEpochMs = todayDate
        .plus(DatePeriod(days = 1))
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .epochSeconds * 1000L

    val uiState: StateFlow<UiState> = appNotificationRepository
        .observeNotificationsBetween(
            startEpochMs = dayStartEpochMs,
            endEpochMs = dayEndEpochMs
        )
        .map { notifications ->
            UiState(notifications = notifications)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState()
        )

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            appNotificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            appNotificationRepository.markAllAsReadBetween(
                startEpochMs = dayStartEpochMs,
                endEpochMs = dayEndEpochMs
            )
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            appNotificationRepository.deleteNotification(notificationId)
        }
    }
}
