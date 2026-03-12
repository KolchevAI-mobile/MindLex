package com.example.mindlex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mindlex.data.local.dao.ProgressDao
import com.example.mindlex.data.local.dao.VocabularyDao
import com.example.mindlex.data.local.dao.WordDao
import com.example.mindlex.data.local.entity.ProgressEntity
import com.example.mindlex.data.local.entity.VocabularyEntity
import com.example.mindlex.data.local.entity.WordEntity

@Database(
    entities = [
        WordEntity::class,
        ProgressEntity::class,
        VocabularyEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(LocalTypeConverters::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    abstract fun progressDao(): ProgressDao

    abstract fun vocabularyDao(): VocabularyDao
}
