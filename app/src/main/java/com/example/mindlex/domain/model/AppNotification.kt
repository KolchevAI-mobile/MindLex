package com.example.mindlex.domain.model

data class AppNotification(
    val id: Long,
    val title: String,
    val message: String,
    val createdAtEpochMs: Long,
    val isRead: Boolean,
    val type: String
)
