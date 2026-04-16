package com.example.mindlex.feature.rush

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.ui.components.MechanicSessionHeader

private fun formatMmSs(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

/** Множитель очков для отображения (дублирует пороги [CalculateRushScore]). */
private fun comboMultiplierForStreak(streak: Int): Double = when {
    streak >= 20 -> 3.0
    streak >= 10 -> 2.0
    streak >= 5 -> 1.5
    else -> 1.0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RushScreen(
    viewModel: RushViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val mult = comboMultiplierForStreak(uiState.comboStreak)
    val comboScale by animateFloatAsState(
        targetValue = if (uiState.milestonePulse > 0) 1.15f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "comboPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        MechanicSessionHeader(
            title = "Спринт на скорость",
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
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f)
                        )
                    )
                ),
        ) {
            AnimatedContent(
                targetState = Triple(uiState.isLoading, uiState.sessionFinished, uiState.loadError != null),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "rushStateChange"
            ) {
            when {
                uiState.loadError != null && !uiState.sessionFinished -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.loadError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retryLoad() }) {
                            Text("Повторить")
                        }
                    }
                }

                uiState.isLoading && !uiState.sessionFinished -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(
                            "Загрузка...",
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                uiState.sessionFinished -> {
                    RushResultsContent(
                        uiState = uiState,
                        onPlayAgain = { viewModel.playAgain() },
                        onBack = onBackClick
                    )
                }

                else -> {
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
                            Text(
                                text = "⏱ ${formatMmSs(uiState.timerSecondsRemaining)} / ${formatMmSs(uiState.timerTotalSeconds)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LinearProgressIndicator(
                                progress = {
                                    if (uiState.timerTotalSeconds > 0) {
                                        uiState.timerSecondsRemaining.toFloat() / uiState.timerTotalSeconds
                                    } else 0f
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            val multLabel =
                                if (mult % 1.0 == 0.0) mult.toInt().toString() else "%.1f".format(mult)
                            Text(
                                text = "🔥 Комбо: x$multLabel (${uiState.comboStreak} подряд)",
                                modifier = Modifier.scale(comboScale),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "📊 Счёт: ${uiState.score} очков",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            uiState.currentWord?.let { word ->
                                Card(
                                    modifier = Modifier
                                        .widthIn(max = 440.dp)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = word.wordNative,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = uiState.userInput,
                                onValueChange = viewModel::onUserInputChanged,
                                label = { Text("Перевод") },
                                modifier = Modifier
                                    .widthIn(max = 440.dp)
                                    .fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { viewModel.submitAnswer() }
                                )
                            )
                            Button(
                                onClick = { viewModel.submitAnswer() },
                                modifier = Modifier
                                    .widthIn(max = 440.dp)
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = uiState.userInput.isNotBlank()
                            ) {
                                Text("Ответить")
                            }
                            OutlinedButton(
                                onClick = { viewModel.skipWord() },
                                modifier = Modifier
                                    .widthIn(max = 440.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Пропустить")
                            }
                            Text(
                                text = "Правильно: ${uiState.correctCount}   ·   Ошибки: ${uiState.incorrectCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun RushResultsContent(
    uiState: RushViewModel.UiState,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Спринт завершён!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
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
                Text("📊 Статистика", fontWeight = FontWeight.SemiBold)
                Text("✅ Правильно: ${uiState.correctCount}")
                Text("❌ Ошибки: ${uiState.incorrectCount}")
                Text("⏭ Пропуски: ${uiState.skipCount}")
                Text("🔥 Макс. комбо: x${uiState.sessionMaxCombo}")
                Text("⏱ Слов/мин: ${uiState.wordsPerMinute}")
                Text(
                    text = "🏆 Итоговый счёт: ${uiState.score}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Рекорды: ${uiState.recordBestScore} очков · комбо ${uiState.recordMaxCombo}",
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
            Text("Попробовать снова")
        }
        TextButton(onClick = onBack) {
            Text("Вернуться к механикам")
        }
    }
}
