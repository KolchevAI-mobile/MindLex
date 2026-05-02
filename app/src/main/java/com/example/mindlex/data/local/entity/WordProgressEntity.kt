package com.example.mindlex.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import java.util.UUID

@Entity(
    tableName = "word_progress",
    indices = [Index(value = ["wordId"], unique = true)]
)
data class WordProgressEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val wordId: String,
    val status: String,
    val level: Int,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val nextReviewAt: Instant,
    val lastReviewedAt: Instant? = null,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant
)
