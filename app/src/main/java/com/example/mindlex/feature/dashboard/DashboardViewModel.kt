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

    private data class DashboardBaseState(
        val userName: String,
        val selectedLanguage: String,
        val dailyGoal: Int,
        val rushBestScore: Int,
        val rushMaxCombo: Int,
        val synonymChainsCompleted: Int
    )

    data class UiState(
        val userName: String = "Ученик",
        val selectedLanguage: String = "en",
        val wordsLearned: Int = 0,
        val currentStreak: Int = 0,
        val dailyProgress: Int = 0,
        /** Рекорд спринта (очки и серия) для блока на главной. */
        val rushBestScore: Int = 0,
        val rushMaxCombo: Int = 0,
        val synonymChainsCompleted: Int = 0,
        val synonymChainAvgLength: Double = 0.0
    )

    private val dashboardBaseFlow = combine(
        combine(settingsRepository.getUserName(), settingsRepository.getSelectedLanguage()) { userName, language ->
            userName to language
        },
        combine(settingsRepository.getDailyGoal(), settingsRepository.getRushBestScore()) { dailyGoal, rushBest ->
            dailyGoal to rushBest
        },
        combine(settingsRepository.getRushMaxComboRecord(), settingsRepository.getSynonymChainsCompleted()) { rushCombo, chainsCompleted ->
            rushCombo to chainsCompleted
        }
    ) { userLang, goalRush, comboChains ->
        DashboardBaseState(
            userName = userLang.first,
            selectedLanguage = userLang.second,
            dailyGoal = goalRush.first,
            rushBestScore = goalRush.second,
            rushMaxCombo = comboChains.first,
            synonymChainsCompleted = comboChains.second
        )
    }

    val uiState: StateFlow<UiState> = combine(
        dashboardBaseFlow,
        settingsRepository.getSynonymChainAvgLength()
    ) { base, avgChainLength ->
        UiState(
            userName = base.userName,
            selectedLanguage = base.selectedLanguage,
            wordsLearned = 0, // TODO: Get from progress repository
            currentStreak = 0, // TODO: Get from progress repository
            dailyProgress = 0, // TODO: Calculate based on daily goal
            rushBestScore = base.rushBestScore,
            rushMaxCombo = base.rushMaxCombo,
            synonymChainsCompleted = base.synonymChainsCompleted,
            synonymChainAvgLength = avgChainLength
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UiState()
    )
}
