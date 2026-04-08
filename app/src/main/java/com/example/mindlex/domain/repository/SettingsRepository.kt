package com.example.mindlex.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime

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

    /** Флаг runtime POST_NOTIFICATIONS (обновляется после диалога и при возврате в настройки). */
    fun isPostNotificationsPermissionGranted(): Flow<Boolean>

    suspend fun setPostNotificationsPermissionGranted(granted: Boolean)

    fun getUserName(): Flow<String>

    suspend fun setUserName(name: String)

    /**
     * Проверяет, показывался ли туториал для Active Recall.
     */
    fun isActiveRecallTutorialShown(): Flow<Boolean>

    /**
     * Отмечает, что туториал для Active Recall был показан.
     */
    suspend fun setActiveRecallTutorialShown(shown: Boolean)

    /** Длительность таймера для механики «Контекстный пропуск» (секунды). */
    fun getClozeTimerSeconds(): Flow<Int>

    suspend fun setClozeTimerSeconds(seconds: Int)

    /** Длительность одной сессии Rush (секунды). */
    fun getRushSessionSeconds(): Flow<Int>

    suspend fun setRushSessionSeconds(seconds: Int)

    fun getRushBestScore(): Flow<Int>

    suspend fun setRushBestScore(score: Int)

    fun getRushMaxComboRecord(): Flow<Int>

    suspend fun setRushMaxComboRecord(combo: Int)

    fun getSynonymChainsCompleted(): Flow<Int>

    suspend fun setSynonymChainsCompleted(value: Int)

    fun getSynonymChainAvgLength(): Flow<Double>

    suspend fun setSynonymChainAvgLength(value: Double)

    fun getCurrentStreak(): Flow<Int>

    suspend fun setCurrentStreak(value: Int)

    fun getLastStudyDate(): Flow<String?>

    suspend fun setLastStudyDate(value: String)

    fun getPreferredStudyTime(): Flow<LocalTime>

    suspend fun setPreferredStudyTime(value: LocalTime)
}
