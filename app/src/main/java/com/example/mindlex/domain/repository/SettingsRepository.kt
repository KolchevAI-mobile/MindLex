package com.example.mindlex.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime
import com.example.mindlex.domain.model.CustomDatasetMeta
import com.example.mindlex.domain.model.VocabularySource

interface SettingsRepository {

    fun getSelectedLanguage(): Flow<String>

    suspend fun setSelectedLanguage(language: String)

    fun getSelectedCategory(): Flow<String>

    suspend fun setSelectedCategory(category: String)

    fun getDailyGoal(): Flow<Int>

    suspend fun setDailyGoal(goal: Int)

    fun isNotificationsEnabled(): Flow<Boolean>

    suspend fun setNotificationsEnabled(enabled: Boolean)

    fun isPostNotificationsPermissionGranted(): Flow<Boolean>

    suspend fun setPostNotificationsPermissionGranted(granted: Boolean)

    fun getUserName(): Flow<String>

    suspend fun setUserName(name: String)

    fun isActiveRecallTutorialShown(): Flow<Boolean>

    suspend fun setActiveRecallTutorialShown(shown: Boolean)

    fun getClozeTimerSeconds(): Flow<Int>

    suspend fun setClozeTimerSeconds(seconds: Int)

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

    fun getVocabularySource(): Flow<VocabularySource>

    suspend fun setVocabularySource(source: VocabularySource)

    fun getCustomDatasetMeta(): Flow<CustomDatasetMeta?>

    suspend fun setCustomDatasetMeta(meta: CustomDatasetMeta?)

    fun getCustomDatasetHistory(): Flow<List<CustomDatasetMeta>>

    suspend fun setCustomDatasetHistory(history: List<CustomDatasetMeta>)
}
