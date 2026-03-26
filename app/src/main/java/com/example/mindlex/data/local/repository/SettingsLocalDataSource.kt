package com.example.mindlex.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.mindlex.core.storage.PreferencesKeys
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime

class SettingsLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    fun getSelectedLanguage(): Flow<String> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.SELECTED_LANGUAGE] ?: "en"
        }
    }

    suspend fun setSelectedLanguage(language: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_LANGUAGE] = language
        }
    }

    fun getSelectedCategory(): Flow<String> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.SELECTED_CATEGORY] ?: "general"
        }
    }

    suspend fun setSelectedCategory(category: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_CATEGORY] = category
        }
    }

    fun getDailyGoal(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.DAILY_GOAL] ?: 10
        }
    }

    suspend fun setDailyGoal(goal: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DAILY_GOAL] = goal
        }
    }

    fun isNotificationsEnabled(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    fun getUserName(): Flow<String> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.USER_NAME] ?: ""
        }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_NAME] = name
        }
    }

    fun isActiveRecallTutorialShown(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.ACTIVE_RECALL_TUTORIAL_SHOWN] ?: false
        }
    }

    suspend fun setActiveRecallTutorialShown(shown: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.ACTIVE_RECALL_TUTORIAL_SHOWN] = shown
        }
    }

    fun getClozeTimerSeconds(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.CLOZE_TIMER_SECONDS] ?: 35
        }
    }

    suspend fun setClozeTimerSeconds(seconds: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CLOZE_TIMER_SECONDS] = seconds
        }
    }

    fun getRushSessionSeconds(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.RUSH_SESSION_SECONDS] ?: 90
        }
    }

    suspend fun setRushSessionSeconds(seconds: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.RUSH_SESSION_SECONDS] = seconds
        }
    }

    fun getRushBestScore(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.RUSH_BEST_SCORE] ?: 0
        }
    }

    suspend fun setRushBestScore(score: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.RUSH_BEST_SCORE] = score
        }
    }

    fun getRushMaxComboRecord(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.RUSH_MAX_COMBO_RECORD] ?: 0
        }
    }

    suspend fun setRushMaxComboRecord(combo: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.RUSH_MAX_COMBO_RECORD] = combo
        }
    }

    fun getSynonymChainsCompleted(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.SYNONYM_CHAINS_COMPLETED] ?: 0
        }
    }

    suspend fun setSynonymChainsCompleted(value: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SYNONYM_CHAINS_COMPLETED] = value
        }
    }

    fun getSynonymChainAvgLength(): Flow<Double> {
        return dataStore.data.map { prefs ->
            val encoded = prefs[PreferencesKeys.SYNONYM_CHAIN_AVG_LENGTH_X100] ?: 0
            encoded / 100.0
        }
    }

    suspend fun setSynonymChainAvgLength(value: Double) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SYNONYM_CHAIN_AVG_LENGTH_X100] = (value * 100).toInt()
        }
    }

    fun getCurrentStreak(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.CURRENT_STREAK] ?: 0
        }
    }

    suspend fun setCurrentStreak(value: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CURRENT_STREAK] = value
        }
    }

    fun getLastStudyDate(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.LAST_STUDY_DATE]
        }
    }

    suspend fun setLastStudyDate(value: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_STUDY_DATE] = value
        }
    }

    fun getPreferredStudyTime(): Flow<LocalTime> {
        return dataStore.data.map { prefs ->
            val raw = prefs[PreferencesKeys.PREFERRED_STUDY_TIME] ?: "15:00:00"
            runCatching { LocalTime.parse(raw) }.getOrElse { LocalTime(15, 0, 0) }
        }
    }

    suspend fun setPreferredStudyTime(value: LocalTime) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.PREFERRED_STUDY_TIME] = value.toString()
        }
    }
}