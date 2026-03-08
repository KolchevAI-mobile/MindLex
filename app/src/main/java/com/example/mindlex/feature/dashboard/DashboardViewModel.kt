package com.example.mindlex.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.UserSettings
import com.example.mindlex.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted

@HiltViewModel
class DashboardViewModel @Inject constructor(
    onboardingRepository: OnboardingRepository
) : ViewModel() {

    data class UiState(
        val userName: String = "",
        val selectedLanguage: String = "en",
        val wordsLearned: Int = 0,
        val currentStreak: Int = 0,
        val dailyProgress: Int = 0
    )

    private val userSettingsFlow: Flow<UserSettings> =
        onboardingRepository.getUserSettings()

    val uiState: StateFlow<UiState> =
        userSettingsFlow
            .map { settings ->
                UiState(
                    userName = settings.userName,
                    selectedLanguage = settings.selectedLanguage,
                    wordsLearned = 0,
                    currentStreak = 0,
                    dailyProgress = 0
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UiState()
            )
}
