package com.example.mindlex.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    data class UiState(
        val userName: String = "Ученик",
        val selectedLanguage: String = "en",
        val wordsLearned: Int = 0,
        val currentStreak: Int = 0,
        val dailyProgress: Int = 0,
        /** Рекорд спринта (очки и серия) для блока на главной. */
        val rushBestScore: Int = 0,
        val rushMaxCombo: Int = 0
    )

    val uiState: StateFlow<UiState> = combine(
        settingsRepository.getUserName(),
        settingsRepository.getSelectedLanguage(),
        settingsRepository.getDailyGoal(),
        settingsRepository.getRushBestScore(),
        settingsRepository.getRushMaxComboRecord()
    ) { userName, language, dailyGoal, rushBest, rushCombo ->
        UiState(
            userName = userName,
            selectedLanguage = language,
            wordsLearned = 0, // TODO: Get from progress repository
            currentStreak = 0, // TODO: Get from progress repository
            dailyProgress = 0, // TODO: Calculate based on daily goal
            rushBestScore = rushBest,
            rushMaxCombo = rushCombo
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UiState()
    )
}
