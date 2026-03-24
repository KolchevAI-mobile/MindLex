package com.example.mindlex.data.repository

import com.example.mindlex.data.remote.supabase.SynonymChainRemoteDataSource
import com.example.mindlex.domain.model.SynonymChain
import com.example.mindlex.domain.repository.SynonymChainRepository
import javax.inject.Inject
import timber.log.Timber

class SynonymChainRepositoryImpl @Inject constructor(
    private val remoteDataSource: SynonymChainRemoteDataSource
) : SynonymChainRepository {

    override suspend fun getRandomChainStartStep(
        language: String,
        category: String
    ): Result<SynonymChain> {
        val allSteps = remoteDataSource.safeGetAllSteps().getOrElse { error ->
            return Result.failure(error)
        }

        val mapped = allSteps
            .map { it.toDomain(language) }
            .filter { it.validSynonyms.isNotEmpty() }

        val inCategory = mapped.filter { it.category.equals(category, ignoreCase = true) }
        val pool = if (inCategory.isNotEmpty()) inCategory else mapped

        if (pool.isEmpty()) {
            return Result.failure(NoSuchElementException("Нет шагов synonym_chains с доступными синонимами"))
        }

        val groupedByChain = pool.groupBy { it.chainId }
        val startCandidates = groupedByChain.values.mapNotNull { steps ->
            steps.minByOrNull { it.stepNumber }
        }

        if (startCandidates.isEmpty()) {
            return Result.failure(NoSuchElementException("Не найдены стартовые шаги цепочек"))
        }

        val selected = startCandidates.random()
        Timber.d("[SynonymChainRepo] Выбрана цепочка ${selected.chainId}, шаг ${selected.stepNumber}")
        return Result.success(selected)
    }

    override suspend fun getChainStep(
        chainId: String,
        stepNumber: Int,
        language: String
    ): Result<SynonymChain?> {
        val allSteps = remoteDataSource.safeGetAllSteps().getOrElse { error ->
            return Result.failure(error)
        }

        val step = allSteps
            .map { it.toDomain(language) }
            .firstOrNull { it.chainId == chainId && it.stepNumber == stepNumber }

        return Result.success(step)
    }
}
