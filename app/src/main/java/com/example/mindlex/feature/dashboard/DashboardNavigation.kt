package com.example.mindlex.feature.dashboard

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

object DashboardDestinations {
    const val ROOT = "dashboard_root"
    const val DASHBOARD = "dashboard_screen"
    const val LEARNING = "learning_screen"
}

fun NavGraphBuilder.dashboardGraph(
    onOpenSettings: () -> Unit,
    onStartLearning: () -> Unit,
    onQuickTraining: () -> Unit,
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
                onQuickTraining = onQuickTraining
            )
        }
        composable(DashboardDestinations.LEARNING) {
            com.example.mindlex.feature.learning.LearningWordsScreen(
                onBackClick = onBackFromLearning
            )
        }
    }
}
