package com.example.mindlex.feature.synonym_chain

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

object SynonymChainDestinations {
    const val ROOT = "synonym_chain_root"
    const val SCREEN = "synonym_chain_screen"
}

fun NavGraphBuilder.synonymChainGraph(
    onBackClick: () -> Unit
) {
    navigation(
        startDestination = SynonymChainDestinations.SCREEN,
        route = SynonymChainDestinations.ROOT
    ) {
        composable(SynonymChainDestinations.SCREEN) {
            SynonymChainScreen(onBackClick = onBackClick)
        }
    }
}
