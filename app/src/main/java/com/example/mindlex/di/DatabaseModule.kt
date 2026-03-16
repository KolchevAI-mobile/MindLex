package com.example.mindlex.di

import android.content.Context
import androidx.room.Room
import com.example.mindlex.data.local.LocalDatabase
import com.example.mindlex.data.local.dao.ProgressDao
import com.example.mindlex.data.local.dao.VocabularyDao
import com.example.mindlex.data.local.dao.WordDao
import com.example.mindlex.data.local.dao.WordProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLocalDatabase(
        @ApplicationContext context: Context
    ): LocalDatabase {
        return Room.databaseBuilder(
            context,
            LocalDatabase::class.java,
            "mindlex.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideWordDao(database: LocalDatabase): WordDao = database.wordDao()

    @Provides
    fun provideProgressDao(database: LocalDatabase): ProgressDao = database.progressDao()

    @Provides
    fun provideVocabularyDao(database: LocalDatabase): VocabularyDao = database.vocabularyDao()

    @Provides
    fun provideWordProgressDao(database: LocalDatabase): WordProgressDao = database.wordProgressDao()
}
