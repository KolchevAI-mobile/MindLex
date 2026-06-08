package com.example.mindlex.feature.active_recall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mindlex.R
import com.example.mindlex.domain.model.Word
import com.example.mindlex.feature.active_recall.components.FeedbackCard
import com.example.mindlex.feature.active_recall.components.HintUsedFeedbackCard
import com.example.mindlex.feature.active_recall.components.SessionCompleteScreen
import com.example.mindlex.feature.mechanics.common.MechanicAnswerField
import com.example.mindlex.feature.mechanics.common.MechanicLoadingState
import com.example.mindlex.feature.mechanics.common.SessionProgressBar

@Composable
fun ActiveRecallSessionContent(
    state: ActiveRecallUiState,
    onUserInputChange: (String) -> Unit,
    onShowHint: () -> Unit,
    onCheckAnswer: () -> Unit,
    onNextWord: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> {
            MechanicLoadingState(
                message = stringResource(R.string.active_recall_loading),
                modifier = modifier
            )
        }

        state.sessionComplete -> {
            SessionCompleteScreen(
                correctCount = state.correctCount,
                incorrectCount = state.incorrectCount,
                hintUsedCount = state.hintUsedCount,
                totalWords = state.totalWords,
                onRetry = onRetry,
                onBack = onBack,
                modifier = modifier
            )
        }

        else -> {
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
                        R.string.active_recall_word_progress,
                        state.currentWordIndex,
                        state.totalWords
                    )
                )

                state.currentWord?.let { word ->
                    RecallWordCard(
                        word = word,
                        hintShown = state.hintShown,
                        feedback = state.feedback,
                        canShowHint = state.canShowHint,
                        onShowHint = onShowHint
                    )
                }

                if (state.awaitingAnswer) {
                    Column(
                        modifier = Modifier.widthIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MechanicAnswerField(
                            value = state.userInput,
                            onValueChange = onUserInputChange,
                            label = stringResource(R.string.active_recall_field_translation),
                            submitLabel = stringResource(R.string.common_check),
                            onSubmit = onCheckAnswer
                        )
                    }
                }

                state.feedback?.let { feedback ->
                    Column(modifier = Modifier.widthIn(max = 400.dp)) {
                        if (feedback.usedHint) {
                            HintUsedFeedbackCard(
                                correctAnswer = state.currentWord?.wordForeign,
                                onContinue = onNextWord
                            )
                        } else {
                            FeedbackCard(
                                isCorrect = feedback.isCorrect,
                                correctAnswer = state.currentWord?.wordForeign,
                                quality = feedback.quality,
                                onContinue = onNextWord
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecallWordCard(
    word: Word,
    hintShown: Boolean,
    feedback: ActiveRecallFeedback?,
    canShowHint: Boolean,
    onShowHint: () -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.active_recall_label_cue),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = word.wordNative,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onShowHint,
                    enabled = canShowHint
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = stringResource(R.string.cd_hint),
                        tint = if (canShowHint) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                }
            }

            if (hintShown) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.active_recall_label_original),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = word.wordForeign,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (feedback != null) {
                word.example?.takeIf { it.isNotBlank() }?.let { example ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = example,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                word.phonetic?.takeIf { it.isNotBlank() }?.let { phonetic ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = phonetic,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveRecallTutorialDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.active_recall_tutorial_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.active_recall_tutorial_step1))
                Text(stringResource(R.string.active_recall_tutorial_step2))
                Text(stringResource(R.string.active_recall_tutorial_step3))
                Text(stringResource(R.string.active_recall_tutorial_step4))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.active_recall_tutorial_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.active_recall_tutorial_skip))
            }
        }
    )
}
