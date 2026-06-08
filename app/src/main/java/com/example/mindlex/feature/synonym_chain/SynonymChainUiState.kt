package com.example.mindlex.feature.synonym_chain

data class ChainSession(
    val chainId: String = "",
    val startStep: Int = 1,
    val currentStep: Int = 1,
    val collectedWords: List<String> = emptyList(),
    val targetWord: String = "",
    val validSynonyms: List<String> = emptyList()
)

/** Цепочка синонимов: шаг, подсказки и итог сессии. */
data class SynonymChainUiState(
    val isLoading: Boolean = true,
    val userInput: String = "",
    val chainSession: ChainSession? = null,
    val progressInChain: Int = 1,
    val targetChainLength: Int = 3,
    val shownHints: List<String> = emptyList(),
    val hintVisible: Boolean = false,
    val incorrectMessage: String? = null,
    val chainsCompletedSession: Int = 0,
    val hintsUsedSession: Int = 0,
    val skipCountSession: Int = 0,
    val chainCompleted: Boolean = false,
    val loadError: String? = null
) {
    val progressFraction: Float
        get() = if (targetChainLength > 0) {
            progressInChain.toFloat() / targetChainLength
        } else {
            0f
        }

    val canAnswer: Boolean
        get() = !isLoading && chainSession != null && !chainCompleted

    val hintsLabel: String?
        get() = shownHints.takeIf { it.isNotEmpty() && hintVisible }?.joinToString(", ")
}
