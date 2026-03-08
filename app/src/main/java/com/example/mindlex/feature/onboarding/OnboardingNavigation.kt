package com.example.mindlex.feature.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

object OnboardingDestinations {
    const val ROOT = "onboarding_root"
    const val ONBOARDING = "onboarding_screen"
}

fun NavGraphBuilder.onboardingGraph(
    onCompleted: () -> Unit
) {
    navigation(
        startDestination = OnboardingDestinations.ONBOARDING,
        route = OnboardingDestinations.ROOT
    ) {
        composable(OnboardingDestinations.ONBOARDING) {
            OnboardingScreen(onCompleted = onCompleted)
        }
    }
}
