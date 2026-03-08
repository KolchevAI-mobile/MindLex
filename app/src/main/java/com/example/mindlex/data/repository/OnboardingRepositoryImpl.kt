package com.example.mindlex.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.mindlex.core.storage.PreferencesKeys
import com.example.mindlex.domain.model.UserSettings
import com.example.mindlex.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OnboardingRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : OnboardingRepository {

    override suspend fun completeOnboarding(userName: String, language: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_NAME] = userName
            prefs[PreferencesKeys.SELECTED_LANGUAGE] = language
            prefs[PreferencesKeys.ONBOARDING_COMPLETED] = true
        }
    }

    override fun isOnboardingCompleted(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }
    }

    override fun getUserSettings(): Flow<UserSettings> {
        return dataStore.data.map { prefs ->
            UserSettings(
                userName = prefs[PreferencesKeys.USER_NAME] ?: "",
                selectedLanguage = prefs[PreferencesKeys.SELECTED_LANGUAGE] ?: "en"
            )
        }
    }
}
