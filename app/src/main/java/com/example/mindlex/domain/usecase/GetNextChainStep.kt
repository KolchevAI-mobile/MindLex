package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.SynonymChain
import com.example.mindlex.domain.repository.SynonymChainRepository
import javax.inject.Inject

class GetNextChainStep @Inject constructor(
    private val synonymChainRepository: SynonymChainRepository
) {
    suspend fun getRandomChainStartStep(
        language: String,
        category: String
    ): Result<SynonymChain> {
        return synonymChainRepository.getRandomChainStartStep(language, category)
    }

    suspend operator fun invoke(
        chainId: String,
        stepNumber: Int,
        language: String
    ): Result<SynonymChain?> {
        return synonymChainRepository.getChainStep(chainId, stepNumber, language)
    }
}
