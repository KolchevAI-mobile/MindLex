package com.example.mindlex.feature.rush

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

object RushDestinations {
    const val ROOT = "rush_root"
    const val SCREEN = "rush_screen"
}

fun NavGraphBuilder.rushGraph(
    onBackClick: () -> Unit
) {
    navigation(
        startDestination = RushDestinations.SCREEN,
        route = RushDestinations.ROOT
    ) {
        composable(RushDestinations.SCREEN) {
            RushScreen(onBackClick = onBackClick)
        }
    }
}
