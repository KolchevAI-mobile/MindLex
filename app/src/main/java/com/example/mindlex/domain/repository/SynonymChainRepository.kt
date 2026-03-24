package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.SynonymChain

/** Репозиторий механики «Цепочка синонимов». */
interface SynonymChainRepository {
    suspend fun getRandomChainStartStep(
        language: String,
        category: String
    ): Result<SynonymChain>

    suspend fun getChainStep(
        chainId: String,
        stepNumber: Int,
        language: String
    ): Result<SynonymChain?>
}
