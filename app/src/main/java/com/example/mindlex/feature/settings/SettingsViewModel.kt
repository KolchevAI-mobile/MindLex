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

    // Текущий язык обучения (target_language) для всего приложения
    val selectedLanguage: StateFlow<String> =
        settingsRepository.getSelectedLanguage()
            .stateIn(viewModelScope, SharingStarted.Lazily, "en")

    // Текущая выбранная категория слов для обучения
    val selectedCategory: StateFlow<String> =
        settingsRepository.getSelectedCategory()
            .stateIn(viewModelScope, SharingStarted.Lazily, "general")

    val dailyGoal: StateFlow<Int> =
        settingsRepository.getDailyGoal()
            .stateIn(viewModelScope, SharingStarted.Lazily, 10)

    val notificationsEnabled: StateFlow<Boolean> =
        settingsRepository.isNotificationsEnabled()
            .stateIn(viewModelScope, SharingStarted.Lazily, true)

    /**
     * Обновление языка обучения пользователя.
     */
    fun onLanguageSelected(language: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedLanguage(language)
        }
    }

    /**
     * Обновление выбранной категории словаря.
     */
    fun onCategorySelected(category: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedCategory(category)
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
