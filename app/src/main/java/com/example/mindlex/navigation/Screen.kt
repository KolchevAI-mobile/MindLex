package com.example.mindlex.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")

    data object Dashboard : Screen("dashboard")
}