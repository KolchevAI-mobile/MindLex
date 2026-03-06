package com.example.mindlex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mindlex.data.local.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: ProgressEntity)

    @Update
    suspend fun updateProgress(progress: ProgressEntity)

    @Query("SELECT * FROM progress WHERE wordId = :wordId")
    fun observeProgressForWord(wordId: String): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE nextReviewAt <= :currentTime ORDER BY nextReviewAt ASC")
    fun observeDueReviews(currentTime: Instant): Flow<List<ProgressEntity>>
}
