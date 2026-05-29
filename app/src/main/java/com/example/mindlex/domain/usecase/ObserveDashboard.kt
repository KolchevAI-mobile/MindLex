package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.DashboardSnapshot
import com.example.mindlex.domain.model.VocabularySource
import com.example.mindlex.domain.repository.AppNotificationRepository
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
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

    operator fun invoke(): Flow<DashboardSnapshot> = combine(
        profileAndGoals(),
        learningStats(),
        todayActivity(),
        vocabularyFlags()
    ) { profile, learning, today, vocabulary ->
        val progressPercent = dailyProgressPercent(today.reviewedWords, profile.dailyGoal)
        val streak = resolveVisibleDashboardStreak(
            storedStreak = today.storedStreak,
            lastStudyDateRaw = today.lastStudyDateRaw,
            today = today.calendarDate
        )

        DashboardSnapshot(
            userName = profile.userName,
            selectedLanguage = profile.languageCode,
            wordsLearned = learning.totalCorrectAnswers,
            currentStreak = streak,
            wordsReviewedToday = today.reviewedWords,
            dailyGoal = profile.dailyGoal,
            dailyProgress = progressPercent,
            rushBestScore = profile.rushBestScore,
            rushMaxCombo = profile.rushMaxCombo,
            synonymChainsCompleted = profile.synonymChainsCompleted,
            synonymChainAvgLength = learning.avgChainLength,
            unreadNotificationsCount = today.unreadNotifications,
            hasImportedCustomDataset = vocabulary.hasCustomDataset,
            isOfflineCustomDatasetMode = vocabulary.usesCustomDataset
        )
    }

    private fun profileAndGoals(): Flow<ProfileAndGoals> = combine(
        combine(
            settingsRepository.getUserName(),
            settingsRepository.getSelectedLanguage(),
            settingsRepository.getDailyGoal()
        ) { name, language, goal -> Triple(name, language, goal) },
        combine(
            settingsRepository.getRushBestScore(),
            settingsRepository.getRushMaxComboRecord(),
            settingsRepository.getSynonymChainsCompleted()
        ) { rushScore, rushCombo, chains -> Triple(rushScore, rushCombo, chains) }
    ) { profile, rush ->
        ProfileAndGoals(
            userName = profile.first,
            languageCode = profile.second,
            dailyGoal = profile.third,
            rushBestScore = rush.first,
            rushMaxCombo = rush.second,
            synonymChainsCompleted = rush.third
        )
    }

    private fun learningStats(): Flow<LearningStats> = combine(
        settingsRepository.getSynonymChainAvgLength(),
        progressRepository.observeTotalCorrectCount()
    ) { avgChainLength, totalCorrect ->
        LearningStats(avgChainLength = avgChainLength, totalCorrectAnswers = totalCorrect)
    }

    private fun todayActivity(): Flow<TodayActivity> {
        val bounds = currentDayBounds()
        return combine(
            progressRepository.observeReviewedWordsCountBetween(bounds.start, bounds.end),
            settingsRepository.getCurrentStreak(),
            settingsRepository.getLastStudyDate(),
            appNotificationRepository.observeUnreadCountBetween(
                startEpochMs = bounds.start.toEpochMillis(),
                endEpochMs = bounds.end.toEpochMillis()
            )
        ) { reviewedToday, streak, lastStudyDate, unread ->
            TodayActivity(
                reviewedWords = reviewedToday,
                storedStreak = streak,
                lastStudyDateRaw = lastStudyDate,
                unreadNotifications = unread,
                calendarDate = bounds.date
            )
        }
    }

    private fun vocabularyFlags(): Flow<VocabularyFlags> = combine(
        settingsRepository.getCustomDatasetHistory(),
        settingsRepository.getVocabularySource()
    ) { history, source ->
        VocabularyFlags(
            hasCustomDataset = history.isNotEmpty(),
            usesCustomDataset = source == VocabularySource.CUSTOM
        )
    }

    private fun dailyProgressPercent(reviewedToday: Int, dailyGoal: Int): Int {
        val goal = dailyGoal.coerceAtLeast(1)
        return ((reviewedToday.toDouble() / goal) * 100).toInt().coerceIn(0, 100)
    }

    private fun currentDayBounds(): DayBounds {
        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone).date
        return DayBounds(
            date = today,
            start = today.atStartOfDayIn(zone),
            end = today.plus(DatePeriod(days = 1)).atStartOfDayIn(zone)
        )
    }

    private data class ProfileAndGoals(
        val userName: String,
        val languageCode: String,
        val dailyGoal: Int,
        val rushBestScore: Int,
        val rushMaxCombo: Int,
        val synonymChainsCompleted: Int
    )

    private data class LearningStats(
        val avgChainLength: Double,
        val totalCorrectAnswers: Int
    )

    private data class TodayActivity(
        val reviewedWords: Int,
        val storedStreak: Int,
        val lastStudyDateRaw: String?,
        val unreadNotifications: Int,
        val calendarDate: LocalDate
    )

    private data class VocabularyFlags(
        val hasCustomDataset: Boolean,
        val usesCustomDataset: Boolean
    )

    private data class DayBounds(
        val date: LocalDate,
        val start: Instant,
        val end: Instant
    )
}

private fun Instant.toEpochMillis(): Long = epochSeconds * 1000L
