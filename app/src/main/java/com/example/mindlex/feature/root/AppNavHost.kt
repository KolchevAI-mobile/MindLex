package com.example.mindlex.feature.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.mindlex.feature.dashboard.DashboardDestinations
import com.example.mindlex.feature.dashboard.dashboardGraph
import com.example.mindlex.feature.onboarding.OnboardingDestinations
import com.example.mindlex.feature.onboarding.onboardingGraph
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.mindlex.domain.usecase.IsOnboardingCompleted

@HiltViewModel
class RootViewModel @Inject constructor(
    isOnboardingCompleted: IsOnboardingCompleted
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String>(OnboardingDestinations.ROOT)
    val startDestination: StateFlow<String> = _startDestination

    init {
        viewModelScope.launch {
            isOnboardingCompleted()
                .collect { completed ->
                    _startDestination.value =
                        if (completed) DashboardDestinations.ROOT
                        else OnboardingDestinations.ROOT
                }
        }
    }
}

@Composable
fun MindLexAppNavHost(
    navController: NavHostController,
    rootViewModel: RootViewModel = hiltViewModel()
) {
    val startDestination by rootViewModel.startDestination.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        onboardingGraph(
            onCompleted = {
                navController.navigate(DashboardDestinations.ROOT) {
                    popUpTo(OnboardingDestinations.ROOT) { inclusive = true }
                }
            }
        )

        dashboardGraph(
            onOpenSettings = {
                // TODO: добавить экран настроек
            }
        )
    }
}