package com.example.mindlex.di

import com.example.mindlex.data.local.dao.ProgressDao
import com.example.mindlex.data.local.dao.VocabularyDao
import com.example.mindlex.data.local.dao.WordDao
import com.example.mindlex.data.local.dao.WordProgressDao
import com.example.mindlex.data.local.dao.AppNotificationDao
import com.example.mindlex.data.local.repository.SettingsLocalDataSource
import com.example.mindlex.data.local.repository.VocabularyLocalDataSource
import com.example.mindlex.data.local.repository.WordLocalDataSource
import com.example.mindlex.data.repository.AppNotificationRepositoryImpl
import com.example.mindlex.data.repository.CustomDatasetRepositoryImpl
import com.example.mindlex.data.repository.WordProgressRepositoryImpl
import com.example.mindlex.data.remote.supabase.ClozeRemoteDataSource
import com.example.mindlex.data.remote.supabase.SynonymChainRemoteDataSource
import com.example.mindlex.data.remote.supabase.SupabaseVocabularyRemoteDataSource
import com.example.mindlex.data.repository.ClozeRepositoryImpl
import com.example.mindlex.data.repository.SettingsRepositoryImpl
import com.example.mindlex.data.repository.SynonymChainRepositoryImpl
import com.example.mindlex.data.repository.VocabularyRepositoryImpl
import com.example.mindlex.data.repository.WordRepositoryImpl
import com.example.mindlex.domain.repository.ClozeRepository
import com.example.mindlex.domain.repository.AppNotificationRepository
import com.example.mindlex.domain.repository.CustomDatasetRepository
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.SynonymChainRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import com.example.mindlex.domain.repository.WordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWordRepository(
        localDataSource: WordLocalDataSource,
        progressDao: ProgressDao,
        wordDao: WordDao
    ): WordRepository {
        return WordRepositoryImpl(localDataSource, progressDao, wordDao)
    }

    @Provides
    @Singleton
    fun provideVocabularyRepository(
        supabaseClient: io.github.jan.supabase.SupabaseClient,
        vocabularyDao: VocabularyDao,
        settingsRepository: SettingsRepository
    ): VocabularyRepository {
        val remoteDataSource = SupabaseVocabularyRemoteDataSource(supabaseClient)
        val localDataSource = VocabularyLocalDataSource(vocabularyDao)
        return VocabularyRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            settingsRepository = settingsRepository
        )
    }

    @Provides
    @Singleton
    fun provideClozeRepository(
        supabaseClient: io.github.jan.supabase.SupabaseClient,
        settingsRepository: SettingsRepository,
        vocabularyRepository: VocabularyRepository
    ): ClozeRepository {
        val remote = ClozeRemoteDataSource(supabaseClient)
        return ClozeRepositoryImpl(remote, settingsRepository, vocabularyRepository)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        localDataSource: SettingsLocalDataSource
    ): SettingsRepository {
        return SettingsRepositoryImpl(localDataSource)
    }

    @Provides
    @Singleton
    fun provideWordProgressRepository(
        wordProgressDao: WordProgressDao,
        wordRepository: WordRepository
    ): WordProgressRepository {
        return WordProgressRepositoryImpl(wordProgressDao, wordRepository)
    }

    @Provides
    @Singleton
    fun provideSynonymChainRepository(
        supabaseClient: io.github.jan.supabase.SupabaseClient
    ): SynonymChainRepository {
        val remote = SynonymChainRemoteDataSource(supabaseClient)
        return SynonymChainRepositoryImpl(remote)
    }

    @Provides
    @Singleton
    fun provideAppNotificationRepository(
        appNotificationDao: AppNotificationDao
    ): AppNotificationRepository {
        return AppNotificationRepositoryImpl(appNotificationDao)
    }

    @Provides
    @Singleton
    fun provideCustomDatasetRepository(
        localDataSource: VocabularyLocalDataSource,
        settingsRepository: SettingsRepository
    ): CustomDatasetRepository {
        return CustomDatasetRepositoryImpl(
            localDataSource = localDataSource,
            settingsRepository = settingsRepository
        )
    }
}