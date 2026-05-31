package com.example.mindlex.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.R
import com.example.mindlex.ui.components.BookOpenDecorLayer
import com.example.mindlex.ui.components.MechanicSessionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MechanicSessionHeader(
                title = stringResource(R.string.notifications_title),
                onBackClick = onBackClick,
                trailingContent = {
                    NotificationsTopBarActions(
                        hasUnread = state.hasUnread,
                        onMarkAllAsRead = viewModel::markAllAsRead
                    )
                },
                subtitle = state.unreadSummary?.let { count ->
                    {
                        Badge { Text(count) }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            BookOpenDecorLayer()
            if (state.items.isEmpty()) {
                NotificationsEmptyState(modifier = Modifier.fillMaxSize())
            } else {
                NotificationsList(
                    items = state.items,
                    onMarkRead = viewModel::markAsRead,
                    onDelete = viewModel::deleteNotification
                )
            }
        }
    }
}
