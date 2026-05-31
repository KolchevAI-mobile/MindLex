package com.example.mindlex.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val uiState: StateFlow<NotificationsUiState> = manageTodaysNotifications
        .observeNotificationsForToday()
        .map(NotificationsUiState::from)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotificationsUiState()
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
