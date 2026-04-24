package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.CustomDatasetMeta
import com.example.mindlex.domain.model.DatasetImportPayload
import com.example.mindlex.domain.repository.CustomDatasetRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ManageCustomDataset @Inject constructor(
    private val customDatasetRepository: CustomDatasetRepository
) {
    fun observeCurrentMeta(): Flow<CustomDatasetMeta?> = customDatasetRepository.observeCurrentDatasetMeta()

    fun observeHistory(): Flow<List<CustomDatasetMeta>> = customDatasetRepository.observeDatasetHistory()

    suspend fun importDataset(payload: DatasetImportPayload): Result<CustomDatasetMeta> =
        customDatasetRepository.importDataset(payload)

    suspend fun refreshDataset(datasetId: String): Result<CustomDatasetMeta> =
        customDatasetRepository.refreshDataset(datasetId)

    suspend fun deleteDataset(datasetId: String): Result<Unit> =
        customDatasetRepository.deleteDataset(datasetId)
}
