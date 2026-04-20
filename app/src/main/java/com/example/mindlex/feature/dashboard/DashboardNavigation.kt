package com.example.mindlex.feature.dashboard

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

object DashboardDestinations {
    const val ROOT = "dashboard_root"
    const val DASHBOARD = "dashboard_screen"
    const val LEARNING = "learning_screen"
    const val NOTIFICATIONS = "notifications_screen"
}

fun NavGraphBuilder.dashboardGraph(
    onOpenSettings: () -> Unit,
    onStartLearning: () -> Unit,
    onQuickTraining: () -> Unit,
    onOpenNotifications: () -> Unit,
    onBackFromLearning: () -> Unit
) {
    navigation(
        startDestination = DashboardDestinations.DASHBOARD,
        route = DashboardDestinations.ROOT
    ) {
        composable(DashboardDestinations.DASHBOARD) {
            DashboardScreen(
                onOpenSettings = onOpenSettings,
                onStartLearning = onStartLearning,
                onQuickTraining = onQuickTraining,
                onOpenNotifications = onOpenNotifications
            )
        }
        composable(DashboardDestinations.NOTIFICATIONS) {
            com.example.mindlex.feature.notifications.NotificationsScreen(
                onBackClick = onBackFromLearning
            )
        }
        composable(DashboardDestinations.LEARNING) {
            com.example.mindlex.feature.learning.LearningWordsScreen(
                onBackClick = onBackFromLearning
            )
        }
    }
}
