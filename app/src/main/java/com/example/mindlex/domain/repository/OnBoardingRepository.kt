package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {

    suspend fun completeOnboarding(userName: String, language: String)

    fun isOnboardingCompleted(): Flow<Boolean>

    fun getUserSettings(): Flow<UserSettings>
}
