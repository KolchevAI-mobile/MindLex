package com.example.mindlex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mindlex.data.local.entity.AppNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppNotificationDao {

    @Query(
        """
        SELECT * FROM app_notifications
        WHERE created_at_epoch_ms >= :startEpochMs AND created_at_epoch_ms < :endEpochMs
        ORDER BY created_at_epoch_ms DESC
        """
    )
    fun observeNotificationsBetween(startEpochMs: Long, endEpochMs: Long): Flow<List<AppNotificationEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM app_notifications
        WHERE created_at_epoch_ms >= :startEpochMs AND created_at_epoch_ms < :endEpochMs
          AND is_read = 0
        """
    )
    fun observeUnreadCountBetween(startEpochMs: Long, endEpochMs: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: AppNotificationEntity)

    @Query("UPDATE app_notifications SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long)

    @Query(
        """
        UPDATE app_notifications
        SET is_read = 1
        WHERE created_at_epoch_ms >= :startEpochMs AND created_at_epoch_ms < :endEpochMs
        """
    )
    suspend fun markAllAsReadBetween(startEpochMs: Long, endEpochMs: Long)

    @Query("DELETE FROM app_notifications WHERE id = :notificationId")
    suspend fun deleteById(notificationId: Long)
}
