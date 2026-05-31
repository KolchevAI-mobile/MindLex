package com.example.mindlex.domain.model

import kotlinx.datetime.LocalTime

/** Текущие настройки пользователя — без UI-форматирования. */
data class SettingsSnapshot(
    val userName: String = "",
    val selectedLanguage: String = "en",
    val selectedCategory: String = "general",
    val dailyGoal: Int = 10,
    val notificationsEnabled: Boolean = true,
    val preferredStudyTime: LocalTime = LocalTime(15, 0, 0)
)
