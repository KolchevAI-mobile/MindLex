package com.example.mindlex.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.mindlex.core.storage.PreferencesKeys
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // Текущий язык обучения (target_language) для Supabase и UI
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

    // Текущая выбранная категория слов (используется для фильтрации выборки из Supabase)
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
}
