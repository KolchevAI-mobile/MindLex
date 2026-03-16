package com.example.mindlex.feature.active_recall

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

/**
 * Навигационные destinations для Active Recall.
 */
object ActiveRecallDestinations {
    const val ROOT = "active_recall_root"
    const val SCREEN = "active_recall_screen"
}

/**
 * Навигационный граф для экрана активного вспоминания.
 *
 * @param onBackClick Callback для возврата назад
 */
fun NavGraphBuilder.activeRecallGraph(
    onBackClick: () -> Unit
) {
    navigation(
        startDestination = ActiveRecallDestinations.SCREEN,
        route = ActiveRecallDestinations.ROOT
    ) {
        composable(ActiveRecallDestinations.SCREEN) {
            ActiveRecallScreen(onBackClick = onBackClick)
        }
    }
}
