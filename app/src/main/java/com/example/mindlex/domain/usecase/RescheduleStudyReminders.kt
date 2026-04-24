package com.example.mindlex.domain.usecase

import com.example.mindlex.core.notifications.StudyNotificationScheduler
import com.example.mindlex.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class RescheduleStudyReminders @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scheduler: StudyNotificationScheduler
) {
    suspend operator fun invoke() {
        scheduler.rescheduleDailyNotifications(
            notificationsEnabled = settingsRepository.isNotificationsEnabled().first(),
            preferredStudyTime = settingsRepository.getPreferredStudyTime().first(),
            dailyGoal = settingsRepository.getDailyGoal().first()
        )
    }
}
