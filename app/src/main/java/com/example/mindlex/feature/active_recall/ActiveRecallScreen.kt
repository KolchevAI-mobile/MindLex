package com.example.mindlex.feature.active_recall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.ui.components.MechanicSessionHeader
import com.example.mindlex.feature.active_recall.components.FeedbackCard
import com.example.mindlex.feature.active_recall.components.HintUsedFeedbackCard
import com.example.mindlex.feature.active_recall.components.SessionCompleteScreen

/**
 * Экран активного вспоминания.
 *
 * @param viewModel ViewModel для управления состоянием
 * @param onBackClick Callback для возврата назад
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRecallScreen(
    viewModel: ActiveRecallViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTutorial by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showTutorial = viewModel.shouldShowTutorial()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        MechanicSessionHeader(
            title = "Активное вспоминание",
            onBackClick = onBackClick
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                        )
                    )
                )
        ) {
            AnimatedContent(
                targetState = Triple(uiState.isLoading, uiState.sessionComplete, uiState.feedback != null),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "activeRecallStateChange"
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
            if (uiState.isLoading) {
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
            } else if (uiState.sessionComplete) {
                // Экран завершения сессии
                SessionCompleteScreen(
                    correctCount = uiState.correctCount,
                    incorrectCount = uiState.incorrectCount,
                    hintUsedCount = uiState.hintUsedCount,
                    totalWords = uiState.totalWords,
                    onRetry = { viewModel.retrySession() },
                    onBack = onBackClick
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = {
                                if (uiState.totalWords > 0) {
                                    uiState.currentWordIndex.toFloat() / uiState.totalWords
                                } else 0f
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Слово ${uiState.currentWordIndex} из ${uiState.totalWords}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        uiState.currentWord?.let { word ->
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
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(
                                            8.dp,
                                            Alignment.CenterHorizontally
                                        )
                                    ) {
                                        Text(
                                            text = word.wordNative,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        IconButton(
                                            onClick = { viewModel.showHint() },
                                            enabled = !uiState.hintShown && uiState.feedback == null,
                                            modifier = Modifier.padding(start = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = "Показать подсказку",
                                                tint = if (uiState.hintShown) {
                                                    MaterialTheme.colorScheme.outline
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                }
                                            )
                                        }
                                    }
                                    if (uiState.hintShown) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = word.wordForeign,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    if (uiState.feedback != null) {
                                        word.example?.let { example ->
                                            if (example.isNotBlank()) {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                            alpha = 0.5f
                                                        )
                                                    )
                                                ) {
                                                    Text(
                                                        text = example,
                                                        modifier = Modifier.padding(
                                                            horizontal = 12.dp,
                                                            vertical = 8.dp
                                                        ),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                        word.phonetic?.let { phonetic ->
                                            if (phonetic.isNotBlank()) {
                                                Text(
                                                    text = phonetic,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (!uiState.hintShown && uiState.feedback == null) {
                            OutlinedTextField(
                                value = uiState.userInput,
                                onValueChange = viewModel::onUserInputChanged,
                                label = {
                                    Text(
                                        "Перевод",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                placeholder = { Text("Перевод") },
                                modifier = Modifier
                                    .widthIn(max = 400.dp)
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
                                ),
                                enabled = uiState.feedback == null
                            )
                            Button(
                                onClick = { viewModel.checkAnswer() },
                                modifier = Modifier
                                    .widthIn(max = 400.dp)
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = uiState.userInput.isNotBlank() && uiState.feedback == null
                            ) {
                                Text(
                                    "Проверить",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                        uiState.feedback?.let { feedback ->
                            if (feedback.usedHint) {
                                HintUsedFeedbackCard(
                                    correctAnswer = uiState.currentWord?.wordForeign,
                                    onContinue = { viewModel.nextWord() }
                                )
                            } else {
                                FeedbackCard(
                                    isCorrect = feedback.isCorrect,
                                    correctAnswer = uiState.currentWord?.wordForeign,
                                    quality = feedback.quality,
                                    onContinue = { viewModel.nextWord() }
                                )
                            }
                        }
                    }
                }
                }
            }
            }
        }

        // Диалог туториала при первом запуске
        if (showTutorial) {
            TutorialDialog(
                onDismiss = {
                    showTutorial = false
                    viewModel.markTutorialShown()
                }
            )
        }
    }
}

/**
 * Диалог туториала для первого запуска Active Recall.
 */
@Composable
private fun TutorialDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Как это работает?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Вам покажется перевод слова на русском")
                Text("2. Введите оригинал на иностранном языке")
                Text("3. Нажмите «Проверить»")
                Text("4. Система оценит ответ и запланирует повторение")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Понятно")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Пропустить")
            }
        }
    )
}
