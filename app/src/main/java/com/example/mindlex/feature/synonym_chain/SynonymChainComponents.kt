package com.example.mindlex.feature.synonym_chain

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mindlex.R
import com.example.mindlex.feature.mechanics.common.MechanicAnswerField
import com.example.mindlex.feature.mechanics.common.MechanicErrorState
import com.example.mindlex.feature.mechanics.common.MechanicLoadingState
import com.example.mindlex.feature.mechanics.common.SessionProgressBar

@Composable
fun SynonymChainSessionContent(
    state: SynonymChainUiState,
    onUserInputChange: (String) -> Unit,
    onShowHint: () -> Unit,
    onSkip: () -> Unit,
    onCheckAnswer: () -> Unit,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading && state.chainSession == null -> {
            MechanicLoadingState(
                message = stringResource(R.string.synonym_loading_chain),
                modifier = modifier
            )
        }

        state.loadError != null && state.chainSession == null -> {
            MechanicErrorState(
                message = state.loadError,
                onRetry = onContinue,
                modifier = modifier
            )
        }

        state.chainCompleted -> {
            ChainCompletedPanel(
                state = state,
                onContinue = onContinue,
                onFinish = onFinish,
                modifier = modifier
            )
        }

        state.chainSession != null -> {
            val session = state.chainSession
            val scroll = rememberScrollState()
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(top = 4.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SessionProgressBar(
                    progress = state.progressFraction,
                    label = stringResource(
                        R.string.synonym_step,
                        state.progressInChain,
                        state.targetChainLength
                    )
                )

                Card(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.synonym_chain_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                        ChainStepsRow(
                            words = session.collectedWords,
                            pendingSlot = true
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.synonym_for_word, session.targetWord),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier.widthIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MechanicAnswerField(
                        value = state.userInput,
                        onValueChange = onUserInputChange,
                        label = stringResource(R.string.synonym_field),
                        submitLabel = stringResource(R.string.common_check),
                        onSubmit = onCheckAnswer,
                        enabled = state.canAnswer
                    )

                    state.incorrectMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onShowHint,
                            modifier = Modifier.weight(1f),
                            enabled = state.canAnswer && !state.hintVisible
                        ) {
                            Text(stringResource(R.string.synonym_btn_hint))
                        }
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier.weight(1f),
                            enabled = state.canAnswer
                        ) {
                            Text(stringResource(R.string.synonym_btn_skip))
                        }
                    }

                    state.hintsLabel?.let { hints ->
                        Text(
                            text = stringResource(R.string.synonym_options, hints),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChainCompletedPanel(
    state: SynonymChainUiState,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val words = state.chainSession?.collectedWords.orEmpty()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.synonym_done_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.synonym_done_result_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
                ChainStepsRow(words = words, pendingSlot = false)
            }
        }
        Text(
            text = stringResource(R.string.synonym_done_chains, state.chainsCompletedSession),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(
                R.string.synonym_done_hints_skips,
                state.hintsUsedSession,
                state.skipCountSession
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onContinue,
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.synonym_done_continue))
        }
        TextButton(onClick = onFinish) {
            Text(stringResource(R.string.synonym_done_finish))
        }
    }
}

@Composable
fun ChainStepsRow(
    words: List<String>,
    pendingSlot: Boolean,
    modifier: Modifier = Modifier
) {
    val chipShape = RoundedCornerShape(999.dp)
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (words.isEmpty() && pendingSlot) {
            PendingChip(chipShape)
        }
        words.forEachIndexed { index, word ->
            if (index > 0) {
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Surface(
                shape = chipShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
            ) {
                Text(
                    text = word,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        if (pendingSlot && words.isNotEmpty()) {
            Text(
                text = "→",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 6.dp)
            )
            PendingChip(chipShape)
        }
    }
}

@Composable
private fun PendingChip(shape: RoundedCornerShape) {
    Surface(
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Text(
            text = "?",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
