package com.example.mindlex.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.mindlex.data.repository.OnboardingRepositoryImpl
import com.example.mindlex.domain.repository.OnboardingRepository
import com.example.mindlex.domain.usecase.CompleteOnboarding
import com.example.mindlex.domain.usecase.IsOnboardingCompleted
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OnboardingModule {

    @Provides
    @Singleton
    fun provideOnboardingRepository(
        dataStore: DataStore<Preferences>
    ): OnboardingRepository =
        OnboardingRepositoryImpl(dataStore)

    @Provides
    @Singleton
    fun provideCompleteOnboardingUseCase(
        repository: OnboardingRepository
    ): CompleteOnboarding = CompleteOnboarding(repository)

    @Provides
    @Singleton
    fun provideIsOnboardingCompletedUseCase(
        repository: OnboardingRepository
    ): IsOnboardingCompleted = IsOnboardingCompleted(repository)
}
