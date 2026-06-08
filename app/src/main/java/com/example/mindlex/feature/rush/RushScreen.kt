package com.example.mindlex.feature.rush

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
fun RushScreen(
    onBackClick: () -> Unit,
    viewModel: RushViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    MechanicScreenShell(
        title = stringResource(R.string.rush_title),
        onBackClick = onBackClick
    ) {
        RushSessionContent(
            state = state,
            onUserInputChange = viewModel::onUserInputChanged,
            onSubmit = viewModel::submitAnswer,
            onSkip = viewModel::skipWord,
            onPlayAgain = viewModel::playAgain,
            onBack = onBackClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}
