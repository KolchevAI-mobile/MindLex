package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.CustomDatasetMeta
import com.example.mindlex.domain.model.DatasetImportPayload
import com.example.mindlex.domain.model.ManualWordEntry
import kotlinx.coroutines.flow.Flow

interface CustomDatasetRepository {
    fun observeCurrentDatasetMeta(): Flow<CustomDatasetMeta?>
    fun observeDatasetHistory(): Flow<List<CustomDatasetMeta>>
    suspend fun importDataset(payload: DatasetImportPayload): Result<CustomDatasetMeta>
    suspend fun importManualDataset(
        entries: List<ManualWordEntry>,
        displayName: String
    ): Result<CustomDatasetMeta>
    suspend fun refreshDataset(datasetId: String): Result<CustomDatasetMeta>
    suspend fun deleteDataset(datasetId: String): Result<Unit>
}
