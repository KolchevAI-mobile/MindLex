package com.example.mindlex.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.mindlex.core.storage.PreferencesKeys
import com.example.mindlex.domain.model.CustomDatasetMeta
import com.example.mindlex.domain.model.VocabularySource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class SettingsLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val json = Json { ignoreUnknownKeys = true }
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

    fun getLastRemoteCategory(): Flow<String> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.LAST_REMOTE_CATEGORY] ?: "general"
        }
    }

    suspend fun setLastRemoteCategory(category: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_REMOTE_CATEGORY] = category
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

    fun isPostNotificationsPermissionGranted(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.POST_NOTIFICATIONS_GRANTED] ?: false
        }
    }

    suspend fun setPostNotificationsPermissionGranted(granted: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.POST_NOTIFICATIONS_GRANTED] = granted
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

    fun getVocabularySource(): Flow<VocabularySource> {
        return dataStore.data.map { prefs ->
            val raw = prefs[PreferencesKeys.VOCABULARY_SOURCE]
            if (raw == VocabularySource.CUSTOM.name) VocabularySource.CUSTOM else VocabularySource.REMOTE
        }
    }

    suspend fun setVocabularySource(source: VocabularySource) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.VOCABULARY_SOURCE] = source.name
        }
    }

    fun getCustomDatasetMeta(): Flow<CustomDatasetMeta?> {
        return dataStore.data.map { prefs ->
            val id = prefs[PreferencesKeys.CUSTOM_DATASET_ID] ?: return@map null
            val name = prefs[PreferencesKeys.CUSTOM_DATASET_NAME] ?: return@map null
            val format = prefs[PreferencesKeys.CUSTOM_DATASET_FORMAT] ?: return@map null
            val recordsCount = prefs[PreferencesKeys.CUSTOM_DATASET_RECORDS_COUNT] ?: return@map null
            val importedAt = prefs[PreferencesKeys.CUSTOM_DATASET_IMPORTED_AT]?.toLongOrNull()
                ?: return@map null
            val sourceUri = prefs[PreferencesKeys.CUSTOM_DATASET_URI] ?: return@map null

            CustomDatasetMeta(
                id = id,
                displayName = name,
                format = format,
                recordsCount = recordsCount,
                importedAtEpochMillis = importedAt,
                sourceUri = sourceUri
            )
        }
    }

    suspend fun setCustomDatasetMeta(meta: CustomDatasetMeta?) {
        dataStore.edit { prefs ->
            if (meta == null) {
                prefs.remove(PreferencesKeys.CUSTOM_DATASET_ID)
                prefs.remove(PreferencesKeys.CUSTOM_DATASET_NAME)
                prefs.remove(PreferencesKeys.CUSTOM_DATASET_FORMAT)
                prefs.remove(PreferencesKeys.CUSTOM_DATASET_RECORDS_COUNT)
                prefs.remove(PreferencesKeys.CUSTOM_DATASET_IMPORTED_AT)
                prefs.remove(PreferencesKeys.CUSTOM_DATASET_URI)
            } else {
                prefs[PreferencesKeys.CUSTOM_DATASET_ID] = meta.id
                prefs[PreferencesKeys.CUSTOM_DATASET_NAME] = meta.displayName
                prefs[PreferencesKeys.CUSTOM_DATASET_FORMAT] = meta.format
                prefs[PreferencesKeys.CUSTOM_DATASET_RECORDS_COUNT] = meta.recordsCount
                prefs[PreferencesKeys.CUSTOM_DATASET_IMPORTED_AT] = meta.importedAtEpochMillis.toString()
                prefs[PreferencesKeys.CUSTOM_DATASET_URI] = meta.sourceUri
            }
        }
    }

    fun getCustomDatasetHistory(): Flow<List<CustomDatasetMeta>> {
        return dataStore.data.map { prefs ->
            val raw = prefs[PreferencesKeys.CUSTOM_DATASET_HISTORY] ?: return@map emptyList()
            runCatching {
                json.decodeFromString(ListSerializer(CustomDatasetMeta.serializer()), raw)
            }.getOrElse { emptyList() }
        }
    }

    suspend fun setCustomDatasetHistory(history: List<CustomDatasetMeta>) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CUSTOM_DATASET_HISTORY] =
                json.encodeToString(ListSerializer(CustomDatasetMeta.serializer()), history)
        }
    }
}
