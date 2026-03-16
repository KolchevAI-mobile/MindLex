package com.example.mindlex.feature.mechanics

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.mindlex.feature.mechanics.MechanicType

/**
 * Навигационные destinations для экрана выбора механик.
 */
object MechanicsDestinations {
    const val ROOT = "mechanics_root"
    const val MECHANICS = "mechanics_screen"
}

/**
 * Навигационный граф для выбора механик обучения.
 *
 * @param onBackClick Callback для возврата назад
 * @param onMechanicSelected Callback при выборе механики
 */
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
