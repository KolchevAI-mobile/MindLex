package com.example.mindlex.feature.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.mindlex.domain.usecase.IsOnboardingCompleted

@HiltViewModel
class RootViewModel @Inject constructor(
    private val isOnboardingCompleted: IsOnboardingCompleted
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination

    init {
        viewModelScope.launch {
            val completed = isOnboardingCompleted().first()
            _startDestination.value = if (completed) {
                DashboardDestinations.ROOT
            } else {
                OnboardingDestinations.ROOT
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

    // Показываем загрузку, пока startDestination не определен
    if (startDestination == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!! // Safe because we return early if null
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