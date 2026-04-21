package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.CustomDatasetMeta
import com.example.mindlex.domain.model.DatasetImportPayload
import kotlinx.coroutines.flow.Flow

interface CustomDatasetRepository {
    fun observeDatasetMeta(): Flow<CustomDatasetMeta?>
    suspend fun importDataset(payload: DatasetImportPayload): Result<CustomDatasetMeta>
    suspend fun deleteDataset(): Result<Unit>
}

