package com.example.mindlex.feature.cloze

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.feature.active_recall.components.SessionCompleteScreen

private fun formatTimer(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClozeScreen(
    viewModel: ClozeViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Контекстный пропуск")
                        if (!uiState.sessionComplete && uiState.feedback == null && !uiState.isLoading &&
                            uiState.exercise != null
                        ) {
                            Text(
                                text = "⏱ ${formatTimer(uiState.timerSecondsRemaining)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                uiState.loadError != null && uiState.exercise == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.loadError ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.retryLoad() }) {
                            Text("Повторить")
                        }
                    }
                }

                uiState.isLoading && uiState.exercise == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Загрузка...",
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                uiState.sessionComplete -> {
                    SessionCompleteScreen(
                        correctCount = uiState.correctCount,
                        incorrectCount = uiState.incorrectCount,
                        hintUsedCount = uiState.hintUsedCount,
                        totalWords = uiState.totalExercises,
                        onRetry = { viewModel.retrySession() },
                        onBack = onBackClick
                    )
                }

                else -> {
                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = {
                            if (uiState.totalExercises > 0) {
                                uiState.currentIndex.toFloat() / uiState.totalExercises
                            } else 0f
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Упражнение ${uiState.currentIndex} из ${uiState.totalExercises}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    uiState.exercise?.let { exercise ->
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
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = exercise.sentenceWithBlank,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Start
                                )
                                Text(
                                    text = "💡 ${exercise.hint}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.feedback == null && uiState.exercise != null) {
                        OutlinedTextField(
                            value = uiState.userInput,
                            onValueChange = viewModel::onUserInputChanged,
                            label = { Text("Введите слово") },
                            modifier = Modifier
                                .widthIn(max = 480.dp)
                                .fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.checkAnswer() }
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.checkAnswer() },
                            modifier = Modifier
                                .widthIn(max = 480.dp)
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState.userInput.isNotBlank()
                        ) {
                            Text("Проверить")
                        }
                    }

                    uiState.feedback?.let { fb ->
                        ClozeFeedbackCard(
                            feedback = fb,
                            correctWord = uiState.evaluationWord?.wordForeign ?: "",
                            phonetic = uiState.evaluationWord?.phonetic,
                            onContinue = { viewModel.nextExercise() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClozeFeedbackCard(
    feedback: ClozeViewModel.Feedback,
    correctWord: String,
    phonetic: String?,
    onContinue: () -> Unit
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
        modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth(),
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
                imageVector = when {
                    ok -> Icons.Default.CheckCircle
                    else -> Icons.Default.Close
                },
                contentDescription = null,
                tint = when {
                    ok -> Color(0xFF4CAF50)
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
                Text("Продолжить")
            }
        }
    }
}
