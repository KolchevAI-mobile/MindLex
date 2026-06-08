package com.example.mindlex.feature.mechanics

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

object MechanicsDestinations {
    const val ROOT = "mechanics_root"
    const val MECHANICS = "mechanics_screen"
}

/** Выбор режима обучения. */
fun NavGraphBuilder.mechanicsGraph(
    onBackClick: () -> Unit,
    onMechanicSelected: (MechanicType) -> Unit
) {
    navigation(
        startDestination = MechanicsDestinations.MECHANICS,
        route = MechanicsDestinations.ROOT
    ) {
        composable(MechanicsDestinations.MECHANICS) {
            MechanicsScreen(
                onBackClick = onBackClick,
                onMechanicSelected = onMechanicSelected
            )
        }
    }
}
