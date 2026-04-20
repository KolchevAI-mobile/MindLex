package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface AppNotificationRepository {
    fun observeNotificationsBetween(startEpochMs: Long, endEpochMs: Long): Flow<List<AppNotification>>

    fun observeUnreadCountBetween(startEpochMs: Long, endEpochMs: Long): Flow<Int>

    suspend fun addNotification(
        title: String,
        message: String,
        createdAtEpochMs: Long,
        type: String
    )

    suspend fun markAsRead(notificationId: Long)

    suspend fun markAllAsReadBetween(startEpochMs: Long, endEpochMs: Long)

    suspend fun deleteNotification(notificationId: Long)
}
