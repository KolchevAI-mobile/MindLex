package com.example.mindlex.core.constants

object LearningDefaults {
    const val FALLBACK_CATEGORY: String = "general"
    const val DAILY_GOAL_FALLBACK: Int = 10
    const val PROGRESS_CANDIDATE_LIMIT: Int = 10
    const val VOCABULARY_FETCH_LIMIT: Int = 20

    const val REMOTE_FETCH_MULTIPLIER: Int = 25
    const val REMOTE_FETCH_CAP_MIN: Int = 60
    const val REMOTE_FETCH_CAP_MAX: Int = 500

    const val ROOM_POOL_MULTIPLIER: Int = 4
    const val ROOM_POOL_MIN: Int = 40
    const val ROOM_POOL_MAX: Int = 400
}
