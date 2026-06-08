package com.example.mindlex.feature.synonym_chain

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.R
import com.example.mindlex.feature.mechanics.common.MechanicScreenShell

@Composable
fun SynonymChainScreen(
    onBackClick: () -> Unit,
    viewModel: SynonymChainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    MechanicScreenShell(
        title = stringResource(R.string.synonym_title),
        onBackClick = onBackClick
    ) {
        SynonymChainSessionContent(
            state = state,
            onUserInputChange = viewModel::onUserInputChanged,
            onShowHint = viewModel::showHint,
            onSkip = viewModel::skipCurrentWord,
            onCheckAnswer = viewModel::checkAnswer,
            onContinue = viewModel::continueWithNextChain,
            onFinish = onBackClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}
