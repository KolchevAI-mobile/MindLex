package com.example.mindlex.core.storage

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {

    val USER_ID = stringPreferencesKey("user_id")
    val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    val SELECTED_CATEGORY = stringPreferencesKey("selected_category")
    val LAST_REMOTE_CATEGORY = stringPreferencesKey("last_remote_category")
    val DAILY_GOAL = intPreferencesKey("daily_goal")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    
    val POST_NOTIFICATIONS_GRANTED = booleanPreferencesKey("post_notifications_granted")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val USER_NAME = stringPreferencesKey("user_name")
    val ACTIVE_RECALL_TUTORIAL_SHOWN = booleanPreferencesKey("active_recall_tutorial_shown")
    
    val CLOZE_TIMER_SECONDS = intPreferencesKey("cloze_timer_seconds")
    
    val RUSH_SESSION_SECONDS = intPreferencesKey("rush_session_seconds")
    val RUSH_BEST_SCORE = intPreferencesKey("rush_best_score")
    val RUSH_MAX_COMBO_RECORD = intPreferencesKey("rush_max_combo_record")
    val SYNONYM_CHAINS_COMPLETED = intPreferencesKey("synonym_chains_completed")
    
    val SYNONYM_CHAIN_AVG_LENGTH_X100 = intPreferencesKey("synonym_chain_avg_length_x100")
    val CURRENT_STREAK = intPreferencesKey("current_streak")
    val LAST_STUDY_DATE = stringPreferencesKey("last_study_date")
    val PREFERRED_STUDY_TIME = stringPreferencesKey("preferred_study_time")
    val VOCABULARY_SOURCE = stringPreferencesKey("vocabulary_source")
    val CUSTOM_DATASET_NAME = stringPreferencesKey("custom_dataset_name")
    val CUSTOM_DATASET_ID = stringPreferencesKey("custom_dataset_id")
    val CUSTOM_DATASET_FORMAT = stringPreferencesKey("custom_dataset_format")
    val CUSTOM_DATASET_RECORDS_COUNT = intPreferencesKey("custom_dataset_records_count")
    val CUSTOM_DATASET_IMPORTED_AT = stringPreferencesKey("custom_dataset_imported_at")
    val CUSTOM_DATASET_URI = stringPreferencesKey("custom_dataset_uri")
    val CUSTOM_DATASET_HISTORY = stringPreferencesKey("custom_dataset_history")
}
