package com.example.mindlex.di

import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import com.example.mindlex.domain.usecase.GetLearningWords
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Модуль DI для предоставления use case-ов Domain слоя.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetLearningWords(
        vocabularyRepository: VocabularyRepository,
        settingsRepository: SettingsRepository
    ): GetLearningWords {
        return GetLearningWords(
            repository = vocabularyRepository,
            settingsRepository = settingsRepository
        )
    }
}

