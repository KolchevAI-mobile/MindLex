package com.example.mindlex.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

object SettingsDestinations {
    const val ROOT = "settings_root"
    const val SETTINGS = "settings_screen" 
    const val CUSTOM_DATASET = "custom_dataset_screen"
}

/** Граф настроек: главный экран и свой датасет. */
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
