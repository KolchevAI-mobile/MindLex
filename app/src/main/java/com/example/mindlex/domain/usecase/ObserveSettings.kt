package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.SettingsSnapshot
import com.example.mindlex.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class ObserveSettings @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(): Flow<SettingsSnapshot> = combine(
        profile(),
        preferences()
    ) { profile, preferences ->
        SettingsSnapshot(
            userName = profile.userName,
            selectedLanguage = profile.languageCode,
            selectedCategory = profile.categoryCode,
            dailyGoal = preferences.dailyGoal,
            notificationsEnabled = preferences.notificationsEnabled,
            preferredStudyTime = preferences.preferredStudyTime
        )
    }

    private fun profile(): Flow<Profile> = combine(
        settingsRepository.getUserName(),
        settingsRepository.getSelectedLanguage(),
        settingsRepository.getSelectedCategory()
    ) { name, language, category ->
        Profile(userName = name, languageCode = language, categoryCode = category)
    }

    private fun preferences(): Flow<Preferences> = combine(
        settingsRepository.getDailyGoal(),
        settingsRepository.isNotificationsEnabled(),
        settingsRepository.getPreferredStudyTime()
    ) { goal, notifications, preferredTime ->
        Preferences(
            dailyGoal = goal,
            notificationsEnabled = notifications,
            preferredStudyTime = preferredTime
        )
    }

    private data class Profile(
        val userName: String,
        val languageCode: String,
        val categoryCode: String
    )

    private data class Preferences(
        val dailyGoal: Int,
        val notificationsEnabled: Boolean,
        val preferredStudyTime: kotlinx.datetime.LocalTime
    )
}
