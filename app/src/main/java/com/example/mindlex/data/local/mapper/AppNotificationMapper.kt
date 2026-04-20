package com.example.mindlex.data.local.mapper

import com.example.mindlex.data.local.entity.AppNotificationEntity
import com.example.mindlex.domain.model.AppNotification

fun AppNotificationEntity.toDomain(): AppNotification {
    return AppNotification(
        id = id,
        title = title,
        message = message,
        createdAtEpochMs = createdAtEpochMs,
        isRead = isRead,
        type = type
    )
}
