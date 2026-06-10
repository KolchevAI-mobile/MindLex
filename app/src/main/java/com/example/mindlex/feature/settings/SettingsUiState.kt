package com.example.mindlex.feature.settings

import com.example.mindlex.domain.model.SettingsSnapshot
import java.util.Locale
import kotlinx.datetime.LocalTime

data class SettingsSummaryChip(
    val emoji: String,
    val label: String
)

/** Настройки, уже подготовленные для отрисовки. */
data class SettingsUiState(
    val userName: String = "",
    val selectedLanguage: String = "en",
    val selectedCategory: String = "general",
    val dailyGoal: Int = 10,
    val notificationsEnabled: Boolean = true,
    val preferredStudyTime: LocalTime = LocalTime(15, 0, 0),
    val languageLabel: String = "",
    val categoryLabel: String = "",
    val summaryChips: List<SettingsSummaryChip> = emptyList(),
    val preferredTimeLabel: String = "",
    val saveMessage: String? = null
) {
    companion object {
        fun from(snapshot: SettingsSnapshot, saveMessage: String? = null): SettingsUiState {
            val language = Languages.getDisplayName(snapshot.selectedLanguage)
            val category = Categories.getDisplayName(snapshot.selectedCategory)
            return SettingsUiState(
                userName = snapshot.userName,
                selectedLanguage = snapshot.selectedLanguage,
                selectedCategory = snapshot.selectedCategory,
                dailyGoal = snapshot.dailyGoal,
                notificationsEnabled = snapshot.notificationsEnabled,
                preferredStudyTime = snapshot.preferredStudyTime,
                languageLabel = language,
                categoryLabel = category,
                summaryChips = listOf(
                    SettingsSummaryChip("🌐", language),
                    SettingsSummaryChip("📚", category),
                    SettingsSummaryChip("🎯", "${snapshot.dailyGoal} слов/день")
                ),
                preferredTimeLabel = formatTime(snapshot.preferredStudyTime),
                saveMessage = saveMessage
            )
        }

        private fun formatTime(time: LocalTime): String =
            String.format(Locale.getDefault(), "%02d:%02d", time.hour, time.minute)
    }
}
