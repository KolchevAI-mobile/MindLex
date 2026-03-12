package com.example.mindlex.data.repository

import com.example.mindlex.data.local.repository.SettingsLocalDataSource
import com.example.mindlex.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl @Inject constructor(
    private val localDataSource: SettingsLocalDataSource
) : SettingsRepository {

    override fun getSelectedLanguage(): Flow<String> =
        localDataSource.getSelectedLanguage()

    override suspend fun setSelectedLanguage(language: String) {
        localDataSource.setSelectedLanguage(language)
    }

    override fun getSelectedCategory(): Flow<String> =
        localDataSource.getSelectedCategory()

    override suspend fun setSelectedCategory(category: String) {
        localDataSource.setSelectedCategory(category)
    }

    override fun getDailyGoal(): Flow<Int> =
        localDataSource.getDailyGoal()

    override suspend fun setDailyGoal(goal: Int) {
        localDataSource.setDailyGoal(goal)
    }

    override fun isNotificationsEnabled(): Flow<Boolean> =
        localDataSource.isNotificationsEnabled()

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        localDataSource.setNotificationsEnabled(enabled)
    }
}
