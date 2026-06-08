package com.example.mindlex.feature.cloze

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.R
import com.example.mindlex.feature.mechanics.common.MechanicScreenShell

@Composable
fun ClozeScreen(
    onBackClick: () -> Unit,
    viewModel: ClozeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    MechanicScreenShell(
        title = stringResource(R.string.cloze_title),
        onBackClick = onBackClick,
        subtitle = if (state.showTimerInHeader) {
            {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.cloze_timer, state.timerLabel),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state.timerUrgent) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        } else {
            null
        }
    ) {
        ClozeSessionContent(
            state = state,
            onUserInputChange = viewModel::onUserInputChanged,
            onCheckAnswer = viewModel::checkAnswer,
            onNextExercise = viewModel::nextExercise,
            onRetryLoad = viewModel::retryLoad,
            onRetrySession = viewModel::retrySession,
            onBack = onBackClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}
