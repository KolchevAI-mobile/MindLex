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

    override fun getUserName(): Flow<String> =
        localDataSource.getUserName()

    override suspend fun setUserName(name: String) {
        localDataSource.setUserName(name)
    }

    override fun isActiveRecallTutorialShown(): Flow<Boolean> =
        localDataSource.isActiveRecallTutorialShown()

    override suspend fun setActiveRecallTutorialShown(shown: Boolean) {
        localDataSource.setActiveRecallTutorialShown(shown)
    }

    override fun getClozeTimerSeconds(): Flow<Int> =
        localDataSource.getClozeTimerSeconds()

    override suspend fun setClozeTimerSeconds(seconds: Int) {
        localDataSource.setClozeTimerSeconds(seconds)
    }

    override fun getRushSessionSeconds(): Flow<Int> =
        localDataSource.getRushSessionSeconds()

    override suspend fun setRushSessionSeconds(seconds: Int) {
        localDataSource.setRushSessionSeconds(seconds)
    }

    override fun getRushBestScore(): Flow<Int> =
        localDataSource.getRushBestScore()

    override suspend fun setRushBestScore(score: Int) {
        localDataSource.setRushBestScore(score)
    }

    override fun getRushMaxComboRecord(): Flow<Int> =
        localDataSource.getRushMaxComboRecord()

    override suspend fun setRushMaxComboRecord(combo: Int) {
        localDataSource.setRushMaxComboRecord(combo)
    }
}
