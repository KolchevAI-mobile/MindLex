package com.example.mindlex.feature.cloze

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

object ClozeDestinations {
    const val ROOT = "cloze_root"
    const val SCREEN = "cloze_screen"
}

/** Навигация cloze-упражнений. */
fun NavGraphBuilder.clozeGraph(
    onBackClick: () -> Unit
) {
    navigation(
        startDestination = ClozeDestinations.SCREEN,
        route = ClozeDestinations.ROOT
    ) {
        composable(ClozeDestinations.SCREEN) {
            ClozeScreen(onBackClick = onBackClick)
        }
    }
}
