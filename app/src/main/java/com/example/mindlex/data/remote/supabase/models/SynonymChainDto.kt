package com.example.mindlex.data.remote.supabase.models

import com.example.mindlex.domain.model.SynonymChain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SynonymChainDto(
    val id: String,
    @SerialName("chain_id")
    val chainId: String,
    @SerialName("step_number")
    val stepNumber: Int,
    val word: String,
    @SerialName("synonyms_en")
    val synonymsEn: List<String>? = null,
    @SerialName("synonyms_de")
    val synonymsDe: List<String>? = null,
    @SerialName("synonyms_fr")
    val synonymsFr: List<String>? = null,
    @SerialName("synonyms_es")
    val synonymsEs: List<String>? = null,
    val difficulty: Int? = null,
    val category: String? = null
) {
    fun toDomain(language: String): SynonymChain {
        val normalizedLang = language.lowercase()
        val synonyms = when (normalizedLang) {
            "de" -> synonymsDe
            "fr" -> synonymsFr
            "es" -> synonymsEs
            else -> synonymsEn
        }.orEmpty().map { it.trim() }.filter { it.isNotBlank() }

        return SynonymChain(
            id = id,
            chainId = chainId,
            stepNumber = stepNumber,
            word = word.trim(),
            validSynonyms = synonyms,
            difficulty = difficulty ?: 1,
            category = category ?: "general"
        )
    }
}
