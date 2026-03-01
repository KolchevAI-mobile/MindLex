package com.example.mindlex.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mindlex.feature.dashboard.DashboardScreen
import com.example.mindlex.feature.onboarding.OnboardingScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onStartClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}