package com.example.mindlex.di

import com.example.mindlex.domain.repository.ClozeRepository
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import com.example.mindlex.domain.usecase.CalculateNextReview
import com.example.mindlex.domain.usecase.EvaluateAnswer
import com.example.mindlex.domain.usecase.CalculateRushScore
import com.example.mindlex.domain.usecase.GetLearningWords
import com.example.mindlex.domain.usecase.GetNextClozeExercise
import com.example.mindlex.domain.usecase.GetNextWordForPractice
import com.example.mindlex.domain.usecase.GetRandomWordForRush
import com.example.mindlex.domain.usecase.UpdateWordProgress
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

    @Provides
    @Singleton
    fun provideGetNextWordForPractice(
        vocabularyRepository: VocabularyRepository,
        progressRepository: WordProgressRepository,
        settingsRepository: SettingsRepository
    ): GetNextWordForPractice {
        return GetNextWordForPractice(
            vocabularyRepository = vocabularyRepository,
            progressRepository = progressRepository,
            settingsRepository = settingsRepository
        )
    }

    @Provides
    @Singleton
    fun provideGetNextClozeExercise(
        clozeRepository: ClozeRepository
    ): GetNextClozeExercise {
        return GetNextClozeExercise(clozeRepository)
    }

    @Provides
    @Singleton
    fun provideGetRandomWordForRush(
        vocabularyRepository: VocabularyRepository,
        settingsRepository: SettingsRepository
    ): GetRandomWordForRush {
        return GetRandomWordForRush(vocabularyRepository, settingsRepository)
    }

    @Provides
    @Singleton
    fun provideCalculateRushScore(): CalculateRushScore {
        return CalculateRushScore()
    }

    @Provides
    @Singleton
    fun provideEvaluateAnswer(): EvaluateAnswer {
        return EvaluateAnswer()
    }

    @Provides
    @Singleton
    fun provideCalculateNextReview(): CalculateNextReview {
        return CalculateNextReview()
    }

    @Provides
    @Singleton
    fun provideUpdateWordProgress(
        progressRepository: WordProgressRepository
    ): UpdateWordProgress {
        return UpdateWordProgress(progressRepository)
    }
}

