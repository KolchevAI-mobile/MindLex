package com.example.mindlex.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey
    val id: String,

    val word: String,

    val translation: String? = null,

    val phonetic: String? = null,

    val partOfSpeech: String? = null,

    val definitions: String,

    val createdAt: Instant,

    val updatedAt: Instant
)
