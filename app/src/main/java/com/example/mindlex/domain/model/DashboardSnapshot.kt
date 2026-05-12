package com.example.mindlex.domain.model

data class DashboardSnapshot(
    val userName: String = "Ученик",
    val selectedLanguage: String = "en",
    val wordsLearned: Int = 0,
    val currentStreak: Int = 0,
    val dailyProgress: Int = 0,
    val rushBestScore: Int = 0,
    val rushMaxCombo: Int = 0,
    val synonymChainsCompleted: Int = 0,
    val synonymChainAvgLength: Double = 0.0,
    val unreadNotificationsCount: Int = 0,
    val hasImportedCustomDataset: Boolean = false,
    val isOfflineCustomDatasetMode: Boolean = false
)
