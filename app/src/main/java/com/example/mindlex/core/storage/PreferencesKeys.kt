package com.example.mindlex.core.storage

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {

    val USER_ID = stringPreferencesKey("user_id")
    val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    val SELECTED_CATEGORY = stringPreferencesKey("selected_category")
    val DAILY_GOAL = intPreferencesKey("daily_goal")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val USER_NAME = stringPreferencesKey("user_name")
}
