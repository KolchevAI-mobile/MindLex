package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.repository.OnboardingRepository

class CompleteOnboarding(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(userName: String, language: String): Result<Unit> {
        if (userName.isBlank()) {
            return Result.failure(IllegalArgumentException("Имя не может быть пустым"))
        }
        return try {
            repository.completeOnboarding(userName.trim(), language)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
