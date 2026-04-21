package com.example.mindlex.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/**
 * Навигационные destinations для фичи Settings.
 */
object SettingsDestinations {
    const val ROOT = "settings_root"
    const val SETTINGS = "settings_screen" // Навигационный граф Settings с вложенными destinations.
    const val CUSTOM_DATASET = "custom_dataset_screen"
}

/**
 * Навигационный граф Settings с вложенными destinations.
 */
fun NavGraphBuilder.settingsGraph(
    onBackClick: () -> Unit,
    onOpenCustomDataset: () -> Unit
) {
    navigation(
        startDestination = SettingsDestinations.SETTINGS,
        route = SettingsDestinations.ROOT
    ) {
        composable(SettingsDestinations.SETTINGS) {
            SettingsScreen(
                onBackClick = onBackClick,
                onOpenCustomDataset = onOpenCustomDataset
            )
        }
        composable(SettingsDestinations.CUSTOM_DATASET) {
            CustomDatasetScreen(onBackClick = onBackClick)
        }
    }
}
