package com.example.mindlex.feature.dashboard

import com.example.mindlex.domain.model.DashboardSnapshot
import com.example.mindlex.feature.settings.Languages
import java.util.Locale

/** То, что реально рисуем на экране - уже с подписями и форматированием. */
data class DashboardUiState(
    val userName: String = "Ученик",
    val languageLabel: String = "",
    val wordsLearned: Int = 0,
    val currentStreak: Int = 0,
    val wordsReviewedToday: Int = 0,
    val dailyGoal: Int = 1,
    val dailyProgressPercent: Int = 0,
    val isDailyGoalComplete: Boolean = false,
    val synonymChainsCompleted: Int = 0,
    val synonymChainAvgLabel: String = "0.0",
    val rushBestScore: Int = 0,
    val rushMaxCombo: Int = 0,
    val showRushHighlights: Boolean = false,
    val unreadNotifications: Int = 0,
    val notificationBadgeText: String? = null,
    val isCustomDatasetActive: Boolean = false
) {
    companion object {
        fun from(snapshot: DashboardSnapshot): DashboardUiState {
            val goal = snapshot.dailyGoal.coerceAtLeast(1)
            val unread = snapshot.unreadNotificationsCount
            return DashboardUiState(
                userName = snapshot.userName,
                languageLabel = Languages.getDisplayName(snapshot.selectedLanguage),
                wordsLearned = snapshot.wordsLearned,
                currentStreak = snapshot.currentStreak,
                wordsReviewedToday = snapshot.wordsReviewedToday,
                dailyGoal = goal,
                dailyProgressPercent = snapshot.dailyProgress,
                isDailyGoalComplete = snapshot.wordsReviewedToday >= goal,
                synonymChainsCompleted = snapshot.synonymChainsCompleted,
                synonymChainAvgLabel = String.format(
                    Locale.getDefault(),
                    "%.1f",
                    snapshot.synonymChainAvgLength
                ),
                rushBestScore = snapshot.rushBestScore,
                rushMaxCombo = snapshot.rushMaxCombo,
                showRushHighlights = snapshot.rushBestScore > 0 || snapshot.rushMaxCombo > 1,
                unreadNotifications = unread,
                notificationBadgeText = when {
                    unread <= 0 -> null
                    unread > 9 -> "9+"
                    else -> unread.toString()
                },
                isCustomDatasetActive = snapshot.isOfflineCustomDatasetMode
            )
        }
    }
}
