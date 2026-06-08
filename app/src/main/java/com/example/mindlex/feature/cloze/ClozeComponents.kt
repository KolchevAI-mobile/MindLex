package com.example.mindlex.feature.cloze

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mindlex.R
import com.example.mindlex.domain.model.ClozeExercise
import com.example.mindlex.feature.active_recall.components.SessionCompleteScreen
import com.example.mindlex.feature.mechanics.common.MechanicAnswerField
import com.example.mindlex.feature.mechanics.common.MechanicErrorState
import com.example.mindlex.feature.mechanics.common.MechanicLoadingState
import com.example.mindlex.feature.mechanics.common.SessionProgressBar

@Composable
fun ClozeSessionContent(
    state: ClozeUiState,
    onUserInputChange: (String) -> Unit,
    onCheckAnswer: () -> Unit,
    onNextExercise: () -> Unit,
    onRetryLoad: () -> Unit,
    onRetrySession: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.loadError != null && state.exercise == null -> {
            MechanicErrorState(
                message = state.loadError,
                onRetry = onRetryLoad,
                modifier = modifier
            )
        }

        state.isLoading && state.exercise == null -> {
            MechanicLoadingState(
                message = stringResource(R.string.common_loading),
                modifier = modifier
            )
        }

        state.sessionComplete -> {
            SessionCompleteScreen(
                correctCount = state.correctCount,
                incorrectCount = state.incorrectCount,
                hintUsedCount = state.hintUsedCount,
                totalWords = state.totalExercises,
                onRetry = onRetrySession,
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
                        R.string.cloze_exercise_progress,
                        state.currentIndex,
                        state.totalExercises
                    )
                )

                state.exercise?.let { exercise ->
                    ClozeExerciseCard(exercise = exercise)
                }

                if (state.awaitingAnswer) {
                    Column(
                        modifier = Modifier.widthIn(max = 480.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MechanicAnswerField(
                            value = state.userInput,
                            onValueChange = onUserInputChange,
                            label = stringResource(R.string.cloze_field_word),
                            submitLabel = stringResource(R.string.common_check),
                            onSubmit = onCheckAnswer
                        )
                    }
                }

                state.feedback?.let { feedback ->
                    ClozeFeedbackCard(
                        feedback = feedback,
                        correctWord = state.evaluationWord?.wordForeign.orEmpty(),
                        phonetic = state.evaluationWord?.phonetic,
                        onContinue = onNextExercise,
                        modifier = Modifier.widthIn(max = 480.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClozeExerciseCard(exercise: ClozeExercise) {
    Card(
        modifier = Modifier
            .widthIn(max = 480.dp)
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.cloze_blank_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
            Text(
                text = exercise.sentenceWithBlank,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.cloze_hint, exercise.hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun ClozeFeedbackCard(
    feedback: ClozeFeedback,
    correctWord: String,
    phonetic: String?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ok = feedback.isCorrect && !feedback.timedOut
    val containerColor = when {
        ok -> MaterialTheme.colorScheme.tertiaryContainer
        feedback.timedOut -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when {
        ok -> MaterialTheme.colorScheme.onTertiaryContainer
        feedback.timedOut -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (ok) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = when {
                    ok -> MaterialTheme.colorScheme.primary
                    feedback.timedOut -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.error
                }
            )
            Text(
                text = feedback.message,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
            if (correctWord.isNotBlank()) {
                val phon = phonetic?.takeIf { it.isNotBlank() }
                Text(
                    text = if (phon != null) "$correctWord $phon" else correctWord,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor
                )
            }
            Text(
                text = "\"${feedback.fullSentence}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.cloze_continue))
            }
        }
    }
}
