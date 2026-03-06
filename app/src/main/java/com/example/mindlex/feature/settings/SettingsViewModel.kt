package com.example.mindlex.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val selectedLanguage: StateFlow<String> =
        settingsRepository.getSelectedLanguage()
            .stateIn(viewModelScope, SharingStarted.Lazily, "en")

    val dailyGoal: StateFlow<Int> =
        settingsRepository.getDailyGoal()
            .stateIn(viewModelScope, SharingStarted.Lazily, 10)

    val notificationsEnabled: StateFlow<Boolean> =
        settingsRepository.isNotificationsEnabled()
            .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun onLanguageSelected(language: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedLanguage(language)
        }
    }

    fun onDailyGoalChanged(goal: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyGoal(goal)
        }
    }

    fun onNotificationsToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }
}
