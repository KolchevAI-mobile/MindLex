package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.DashboardSnapshot
import com.example.mindlex.domain.repository.AppNotificationRepository
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Singleton
class ObserveDashboard @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val progressRepository: WordProgressRepository,
    private val appNotificationRepository: AppNotificationRepository
) {

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

    private val dashboardBaseFlow = combine(
        combine(settingsRepository.getUserName(), settingsRepository.getSelectedLanguage()) { userName, language ->
            userName to language
        },
        combine(settingsRepository.getDailyGoal(), settingsRepository.getRushBestScore()) { dailyGoal, rushBest ->
            dailyGoal to rushBest
        },
        combine(
            settingsRepository.getRushMaxComboRecord(),
            settingsRepository.getSynonymChainsCompleted()
        ) { rushCombo, chainsCompleted ->
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

    operator fun invoke(): Flow<DashboardSnapshot> = combine(
        combine(
            dashboardBaseFlow,
            settingsRepository.getSynonymChainAvgLength(),
            progressRepository.observeTotalCorrectCount()
        ) { base, avgChainLength, totalCorrectAnswers ->
            Triple(base, avgChainLength, totalCorrectAnswers)
        },
        combine(
            progressRepository.observeReviewedWordsCountBetween(dayStart, dayEnd),
            settingsRepository.getCurrentStreak(),
            settingsRepository.getLastStudyDate(),
            appNotificationRepository.observeUnreadCountBetween(
                startEpochMs = dayStart.epochSeconds * 1000L,
                endEpochMs = dayEnd.epochSeconds * 1000L
            )
        ) { reviewedToday, storedStreak, lastStudyDateRaw, unreadNotificationsCount ->
            Quadruple(reviewedToday, storedStreak, lastStudyDateRaw, unreadNotificationsCount)
        }
    ) { primary, secondary ->
        val base = primary.first
        val avgChainLength = primary.second
        val wordsLearnedCount = primary.third
        val reviewedToday = secondary.first
        val storedStreak = secondary.second
        val lastStudyDateRaw = secondary.third
        val unreadNotificationsCount = secondary.fourth
        val dailyProgress = ((reviewedToday.toDouble() / base.dailyGoal.coerceAtLeast(1)) * 100)
            .toInt()
            .coerceIn(0, 100)
        val nowDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val currentStreak = resolveVisibleDashboardStreak(storedStreak, lastStudyDateRaw, nowDate)

        DashboardSnapshot(
            userName = base.userName,
            selectedLanguage = base.selectedLanguage,
            wordsLearned = wordsLearnedCount,
            currentStreak = currentStreak,
            dailyProgress = dailyProgress,
            rushBestScore = base.rushBestScore,
            rushMaxCombo = base.rushMaxCombo,
            synonymChainsCompleted = base.synonymChainsCompleted,
            synonymChainAvgLength = avgChainLength,
            unreadNotificationsCount = unreadNotificationsCount
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
