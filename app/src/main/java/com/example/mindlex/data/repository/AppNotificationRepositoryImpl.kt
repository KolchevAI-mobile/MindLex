package com.example.mindlex.data.repository

import com.example.mindlex.data.local.dao.AppNotificationDao
import com.example.mindlex.data.local.entity.AppNotificationEntity
import com.example.mindlex.data.local.mapper.toDomain
import com.example.mindlex.domain.model.AppNotification
import com.example.mindlex.domain.repository.AppNotificationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppNotificationRepositoryImpl @Inject constructor(
    private val appNotificationDao: AppNotificationDao
) : AppNotificationRepository {

    override fun observeNotificationsBetween(startEpochMs: Long, endEpochMs: Long): Flow<List<AppNotification>> {
        return appNotificationDao.observeNotificationsBetween(startEpochMs, endEpochMs)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeUnreadCountBetween(startEpochMs: Long, endEpochMs: Long): Flow<Int> {
        return appNotificationDao.observeUnreadCountBetween(startEpochMs, endEpochMs)
    }

    override suspend fun addNotification(
        title: String,
        message: String,
        createdAtEpochMs: Long,
        type: String
    ) {
        appNotificationDao.insert(
            AppNotificationEntity(
                title = title,
                message = message,
                createdAtEpochMs = createdAtEpochMs,
                isRead = false,
                type = type
            )
        )
    }

    override suspend fun markAsRead(notificationId: Long) {
        appNotificationDao.markAsRead(notificationId)
    }

    override suspend fun markAllAsReadBetween(startEpochMs: Long, endEpochMs: Long) {
        appNotificationDao.markAllAsReadBetween(startEpochMs, endEpochMs)
    }

    override suspend fun deleteNotification(notificationId: Long) {
        appNotificationDao.deleteById(notificationId)
    }
}
