package com.example.mindlex.feature.active_recall

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.R
import com.example.mindlex.feature.mechanics.common.MechanicScreenShell

@Composable
fun ActiveRecallScreen(
    onBackClick: () -> Unit,
    viewModel: ActiveRecallViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showTutorial by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showTutorial = viewModel.shouldShowTutorial()
    }

    MechanicScreenShell(
        title = stringResource(R.string.active_recall_title),
        onBackClick = onBackClick
    ) {
        ActiveRecallSessionContent(
            state = state,
            onUserInputChange = viewModel::onUserInputChanged,
            onShowHint = viewModel::showHint,
            onCheckAnswer = viewModel::checkAnswer,
            onNextWord = viewModel::nextWord,
            onRetry = viewModel::retrySession,
            onBack = onBackClick,
            modifier = Modifier.fillMaxSize()
        )
    }

    if (showTutorial) {
        ActiveRecallTutorialDialog(
            onDismiss = {
                showTutorial = false
                viewModel.markTutorialShown()
            }
        )
    }
}
