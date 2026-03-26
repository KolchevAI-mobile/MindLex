package com.example.mindlex.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    progressRepository: WordProgressRepository
) : ViewModel() {
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val dayStart = today.atStartOfDayIn(TimeZone.currentSystemDefault())
    private val dayEnd = today.plus(DatePeriod(days = 1)).atStartOfDayIn(TimeZone.currentSystemDefault())

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
        combine(
            dashboardBaseFlow,
            settingsRepository.getSynonymChainAvgLength(),
            progressRepository.observeKnownWordsCount()
        ) { base, avgChainLength, knownWordsCount ->
            Triple(base, avgChainLength, knownWordsCount)
        },
        combine(
            progressRepository.observeReviewedWordsCountBetween(dayStart, dayEnd),
            settingsRepository.getCurrentStreak(),
            settingsRepository.getLastStudyDate()
        ) { reviewedToday, storedStreak, lastStudyDateRaw ->
            Triple(reviewedToday, storedStreak, lastStudyDateRaw)
        }
    ) { primary, secondary ->
        val base = primary.first
        val avgChainLength = primary.second
        val knownWordsCount = primary.third
        val reviewedToday = secondary.first
        val storedStreak = secondary.second
        val lastStudyDateRaw = secondary.third
        val dailyProgress = ((reviewedToday.toDouble() / base.dailyGoal.coerceAtLeast(1)) * 100)
            .toInt()
            .coerceIn(0, 100)
        val currentStreak = resolveVisibleStreak(storedStreak, lastStudyDateRaw)

        UiState(
            userName = base.userName,
            selectedLanguage = base.selectedLanguage,
            wordsLearned = knownWordsCount,
            currentStreak = currentStreak,
            dailyProgress = dailyProgress,
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

    private fun resolveVisibleStreak(storedStreak: Int, lastStudyDateRaw: String?): Int {
        val lastStudyDate = lastStudyDateRaw?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return 0
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val yesterday = today.minus(DatePeriod(days = 1))
        return when {
            lastStudyDate == today || lastStudyDate == yesterday -> storedStreak
            else -> 0
        }
    }
}
