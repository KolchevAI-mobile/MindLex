package com.example.mindlex.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.mindlex.core.storage.DataStoreFactory
import com.example.mindlex.data.local.repository.SettingsLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return DataStoreFactory.create(context)
    }

    @Provides
    @Singleton
    fun provideSettingsLocalDataSource(
        dataStore: DataStore<Preferences>
    ): SettingsLocalDataSource {
        return SettingsLocalDataSource(dataStore)
    }
}
