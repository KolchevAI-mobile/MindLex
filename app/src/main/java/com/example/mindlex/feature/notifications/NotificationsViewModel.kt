package com.example.mindlex.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.AppNotification
import com.example.mindlex.domain.usecase.ManageTodaysNotifications
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val manageTodaysNotifications: ManageTodaysNotifications
) : ViewModel() {

    data class UiState(
        val notifications: List<AppNotification> = emptyList()
    ) {
        val hasUnread: Boolean
            get() = notifications.any { !it.isRead }
    }

    val uiState: StateFlow<UiState> = manageTodaysNotifications
        .observeNotificationsForToday()
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
            manageTodaysNotifications.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            manageTodaysNotifications.markAllAsReadForToday()
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            manageTodaysNotifications.deleteNotification(notificationId)
        }
    }
}
