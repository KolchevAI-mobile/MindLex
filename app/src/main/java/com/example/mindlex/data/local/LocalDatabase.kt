package com.example.mindlex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mindlex.data.local.dao.ProgressDao
import com.example.mindlex.data.local.dao.VocabularyDao
import com.example.mindlex.data.local.dao.WordDao
import com.example.mindlex.data.local.dao.WordProgressDao
import com.example.mindlex.data.local.dao.AppNotificationDao
import com.example.mindlex.data.local.entity.AppNotificationEntity
import com.example.mindlex.data.local.entity.ProgressEntity
import com.example.mindlex.data.local.entity.VocabularyEntity
import com.example.mindlex.data.local.entity.WordEntity
import com.example.mindlex.data.local.entity.WordProgressEntity

@Database(
    entities = [
        WordEntity::class,
        ProgressEntity::class,
        VocabularyEntity::class,
        WordProgressEntity::class,
        AppNotificationEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(LocalTypeConverters::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    abstract fun progressDao(): ProgressDao

    abstract fun vocabularyDao(): VocabularyDao

    abstract fun wordProgressDao(): WordProgressDao

    abstract fun appNotificationDao(): AppNotificationDao
}
