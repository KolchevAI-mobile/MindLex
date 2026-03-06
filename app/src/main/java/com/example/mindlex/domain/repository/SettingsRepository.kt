package com.example.mindlex.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getSelectedLanguage(): Flow<String>

    suspend fun setSelectedLanguage(language: String)

    fun getDailyGoal(): Flow<Int>

    suspend fun setDailyGoal(goal: Int)

    fun isNotificationsEnabled(): Flow<Boolean>

    suspend fun setNotificationsEnabled(enabled: Boolean)
}
