package com.example.mindlex.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    /**
     * Текущий язык обучения пользователя (target_language),
     * используется для выборки слов из Supabase и отображения в UI.
     */
    fun getSelectedLanguage(): Flow<String>

    suspend fun setSelectedLanguage(language: String)

    /**
     * Текущая выбранная категория словаря (например, general/food/travel).
     * Используется для фильтрации слов в репозитории словаря.
     */
    fun getSelectedCategory(): Flow<String>

    suspend fun setSelectedCategory(category: String)

    fun getDailyGoal(): Flow<Int>

    suspend fun setDailyGoal(goal: Int)

    fun isNotificationsEnabled(): Flow<Boolean>

    suspend fun setNotificationsEnabled(enabled: Boolean)
}
