package com.example.mindlex.feature.dashboard

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

object DashboardDestinations {
    const val ROOT = "dashboard_root"
    const val DASHBOARD = "dashboard_screen"
    const val LEARNING = "learning_screen"
    const val NOTIFICATIONS = "notifications_screen"
    const val CUSTOM_DATASET = "dashboard_custom_dataset_screen"
}

fun NavGraphBuilder.dashboardGraph(
    onOpenSettings: () -> Unit,
    onStartLearning: () -> Unit,
    onQuickTraining: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenCustomDataset: () -> Unit,
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
                onOpenNotifications = onOpenNotifications,
                onOpenCustomDataset = onOpenCustomDataset
            )
        }
        composable(DashboardDestinations.NOTIFICATIONS) {
            com.example.mindlex.feature.notifications.NotificationsScreen(
                onBackClick = onBackFromLearning
            )
        }
        composable(DashboardDestinations.CUSTOM_DATASET) {
            com.example.mindlex.feature.settings.CustomDatasetScreen(onBackClick = onBackFromLearning)
        }
        composable(DashboardDestinations.LEARNING) {
            com.example.mindlex.feature.learning.LearningWordsScreen(
                onBackClick = onBackFromLearning
            )
        }
    }
}
