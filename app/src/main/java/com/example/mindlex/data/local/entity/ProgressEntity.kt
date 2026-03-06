package com.example.mindlex.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "progress",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["wordId"], unique = true)
    ]
)
data class ProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val wordId: String,

    val level: Int,

    val nextReviewAt: Instant,

    val lastReviewedAt: Instant? = null,

    val correctCount: Int = 0,

    val incorrectCount: Int = 0
)
