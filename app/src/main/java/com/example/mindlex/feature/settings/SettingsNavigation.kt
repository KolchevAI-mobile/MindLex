package com.example.mindlex.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

object SettingsDestinations {
    const val ROOT = "settings_root"
    const val SETTINGS = "settings_screen"
}

fun NavGraphBuilder.settingsGraph(
    onBackClick: () -> Unit
) {
    navigation(
        startDestination = SettingsDestinations.SETTINGS,
        route = SettingsDestinations.ROOT
    ) {
        composable(SettingsDestinations.SETTINGS) {
            SettingsScreen(
                onBackClick = onBackClick
            )
        }
    }
}
