package com.example.mindlex.feature.synonym_chain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.ui.components.MechanicSessionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SynonymChainScreen(
    viewModel: SynonymChainViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        MechanicSessionHeader(
            title = "Цепочка синонимов",
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
                targetState = Triple(uiState.isLoading, uiState.chainCompleted, uiState.loadError != null),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "synonymStateChange"
            ) {
            when {
                uiState.isLoading && uiState.chainSession == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Загрузка цепочки...",
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                uiState.loadError != null && uiState.chainSession == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.loadError ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.continueWithNextChain() }) {
                            Text("Повторить")
                        }
                    }
                }

                uiState.chainCompleted -> {
                    ChainCompletedContent(
                        uiState = uiState,
                        onContinue = viewModel::continueWithNextChain,
                        onFinish = onBackClick
                    )
                }

                else -> {
                    val session = uiState.chainSession
                    if (session == null) {
                        Box(modifier = Modifier.fillMaxSize())
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
                                Text(
                                    text = "🔗 Цепочка: ${uiState.progressInChain}/${uiState.targetChainLength}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
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
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Text(
                                            text = session.collectedWords.joinToString(" → ") + " → _____",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Введите синоним для \"${session.targetWord}\":",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                OutlinedTextField(
                                    value = uiState.userInput,
                                    onValueChange = viewModel::onUserInputChanged,
                                    label = { Text("Синоним") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .widthIn(max = 480.dp)
                                        .fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { viewModel.checkAnswer() })
                                )
                                uiState.incorrectMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .widthIn(max = 480.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.showHint() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("💡 Подсказка")
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.skipCurrentWord() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("⏭ Пропустить")
                                    }
                                }
                                if (uiState.hintVisible && uiState.shownHints.isNotEmpty()) {
                                    Text(
                                        text = "Варианты: ${uiState.shownHints.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { viewModel.checkAnswer() },
                                    enabled = uiState.userInput.isNotBlank(),
                                    modifier = Modifier
                                        .widthIn(max = 480.dp)
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Проверить")
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun ChainCompletedContent(
    uiState: SynonymChainViewModel.UiState,
    onContinue: () -> Unit,
    onFinish: () -> Unit
) {
    val chain = uiState.chainSession?.collectedWords.orEmpty().joinToString(" → ")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Цепочка собрана!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = chain,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Собрано цепочек: ${uiState.chainsCompletedSession}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Подсказок: ${uiState.hintsUsedSession}  •  Пропусков: ${uiState.skipCountSession}",
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
            Text("Продолжить (ещё 3)")
        }
        TextButton(onClick = onFinish) {
            Text("Завершить")
        }
    }
}
