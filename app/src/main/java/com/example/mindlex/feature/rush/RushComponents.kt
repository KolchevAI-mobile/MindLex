package com.example.mindlex.feature.rush

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mindlex.R
import com.example.mindlex.domain.model.Word
import com.example.mindlex.feature.mechanics.common.MechanicAnswerField
import com.example.mindlex.feature.mechanics.common.MechanicErrorState
import com.example.mindlex.feature.mechanics.common.MechanicLoadingState

@Composable
fun RushSessionContent(
    state: RushUiState,
    onUserInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.loadError != null && !state.sessionFinished -> {
            MechanicErrorState(
                message = state.loadError,
                onRetry = onPlayAgain,
                modifier = modifier
            )
        }

        state.isLoading && !state.sessionFinished -> {
            MechanicLoadingState(
                message = stringResource(R.string.common_loading),
                modifier = modifier
            )
        }

        state.sessionFinished -> {
            RushResultsPanel(
                state = state,
                onPlayAgain = onPlayAgain,
                onBack = onBack,
                modifier = modifier
            )
        }

        else -> {
            val comboScale by animateFloatAsState(
                targetValue = if (state.milestonePulse > 0) 1.15f else 1f,
                animationSpec = tween(durationMillis = 220),
                label = "comboPulse"
            )
            val scroll = rememberScrollState()

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(top = 4.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.rush_timer,
                        state.timerLabel,
                        state.timerTotalLabel
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (state.timerUrgent) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                LinearProgressIndicator(
                    progress = { state.timerProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(
                        R.string.rush_combo,
                        state.comboMultiplierLabel,
                        state.comboStreak
                    ),
                    modifier = Modifier.scale(comboScale),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.rush_score, state.score),
                    style = MaterialTheme.typography.titleMedium
                )

                state.currentWord?.let { word ->
                    RushWordCard(word = word)
                }

                Column(
                    modifier = Modifier.widthIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MechanicAnswerField(
                        value = state.userInput,
                        onValueChange = onUserInputChange,
                        label = stringResource(R.string.active_recall_field_translation),
                        submitLabel = stringResource(R.string.common_answer),
                        onSubmit = onSubmit,
                        enabled = state.canAnswer
                    )
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = state.canAnswer
                    ) {
                        Text(stringResource(R.string.common_skip))
                    }
                }

                Text(
                    text = stringResource(
                        R.string.rush_correct_errors,
                        state.correctCount,
                        state.incorrectCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RushWordCard(word: Word) {
    Card(
        modifier = Modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.rush_translate_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = word.wordNative,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun RushResultsPanel(
    state: RushUiState,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.rush_finished_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.rush_stats), fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.rush_stat_correct, state.correctCount))
                Text(stringResource(R.string.rush_stat_wrong, state.incorrectCount))
                Text(stringResource(R.string.rush_stat_skips, state.skipCount))
                Text(stringResource(R.string.rush_stat_max_combo, state.sessionMaxCombo))
                Text(stringResource(R.string.rush_stat_wpm, state.wordsPerMinute))
                Text(
                    text = stringResource(R.string.rush_stat_score, state.score),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.rush_stat_records,
                        state.recordBestScore,
                        state.recordMaxCombo
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.rush_play_again))
        }
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.rush_back_mechanics))
        }
    }
}
