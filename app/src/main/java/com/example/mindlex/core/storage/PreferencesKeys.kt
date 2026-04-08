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
    /** Последний известный результат runtime POST_NOTIFICATIONS (Android 13+). */
    val POST_NOTIFICATIONS_GRANTED = booleanPreferencesKey("post_notifications_granted")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val USER_NAME = stringPreferencesKey("user_name")
    val ACTIVE_RECALL_TUTORIAL_SHOWN = booleanPreferencesKey("active_recall_tutorial_shown")
    /** Длительность таймера contextual cloze, сек (рекомендуется 30–45). */
    val CLOZE_TIMER_SECONDS = intPreferencesKey("cloze_timer_seconds")
    /** Длительность спринта «перевод на скорость», сек (60–90). */
    val RUSH_SESSION_SECONDS = intPreferencesKey("rush_session_seconds")
    val RUSH_BEST_SCORE = intPreferencesKey("rush_best_score")
    val RUSH_MAX_COMBO_RECORD = intPreferencesKey("rush_max_combo_record")
    val SYNONYM_CHAINS_COMPLETED = intPreferencesKey("synonym_chains_completed")
    /** Средняя длина цепочки в сотых (например, 2.67 -> 267). */
    val SYNONYM_CHAIN_AVG_LENGTH_X100 = intPreferencesKey("synonym_chain_avg_length_x100")
    val CURRENT_STREAK = intPreferencesKey("current_streak")
    val LAST_STUDY_DATE = stringPreferencesKey("last_study_date")
    val PREFERRED_STUDY_TIME = stringPreferencesKey("preferred_study_time")
}
