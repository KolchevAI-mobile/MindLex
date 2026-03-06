package com.example.mindlex.data.local.mapper

import com.example.mindlex.data.local.entity.ProgressEntity
import com.example.mindlex.domain.model.UserProgress

object ProgressLocalMapper {

    fun toDomain(entity: ProgressEntity): UserProgress {
        return UserProgress(
            wordId = entity.wordId,
            level = entity.level,
            nextReviewAt = entity.nextReviewAt,
            lastReviewedAt = entity.lastReviewedAt,
            correctCount = entity.correctCount,
            incorrectCount = entity.incorrectCount
        )
    }

    fun fromDomain(progress: UserProgress): ProgressEntity {
        return ProgressEntity(
            wordId = progress.wordId,
            level = progress.level,
            nextReviewAt = progress.nextReviewAt,
            lastReviewedAt = progress.lastReviewedAt,
            correctCount = progress.correctCount,
            incorrectCount = progress.incorrectCount
        )
    }
}
