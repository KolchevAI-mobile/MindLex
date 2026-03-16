package com.example.mindlex.feature.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.mindlex.feature.active_recall.ActiveRecallDestinations
import com.example.mindlex.feature.active_recall.activeRecallGraph
import com.example.mindlex.feature.dashboard.DashboardDestinations
import com.example.mindlex.feature.dashboard.dashboardGraph
import com.example.mindlex.feature.mechanics.MechanicType
import com.example.mindlex.feature.mechanics.MechanicsDestinations
import com.example.mindlex.feature.mechanics.mechanicsGraph
import com.example.mindlex.feature.onboarding.OnboardingDestinations
import com.example.mindlex.feature.onboarding.onboardingGraph
import com.example.mindlex.feature.settings.SettingsDestinations
import com.example.mindlex.feature.settings.settingsGraph
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
                navController.navigate(SettingsDestinations.ROOT)
            },
            onStartLearning = {
                navController.navigate(MechanicsDestinations.ROOT)
            },
            onBackFromLearning = {
                navController.popBackStack()
            }
        )

        mechanicsGraph(
            onBackClick = {
                navController.popBackStack()
            },
            onMechanicSelected = { mechanicType ->
                when (mechanicType) {
                    MechanicType.ACTIVE_RECALL -> {
                        navController.navigate(ActiveRecallDestinations.ROOT)
                    }
                    else -> {
                        // Другие механики пока не реализованы
                    }
                }
            }
        )

        activeRecallGraph(
            onBackClick = {
                navController.popBackStack()
            }
        )

        settingsGraph(
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
}