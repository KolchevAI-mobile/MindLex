package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class IsOnboardingCompleted(
    private val repository: OnboardingRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isOnboardingCompleted()
}
