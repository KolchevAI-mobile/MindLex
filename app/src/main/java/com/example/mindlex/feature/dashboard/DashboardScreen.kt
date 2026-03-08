package com.example.mindlex.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    onOpenSettings: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MindLex") },
                actions = {
                    TextButton(onClick = onOpenSettings) {
                        Text("Настройки")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Привет, ${uiState.userName.ifBlank { "ученик" }}!",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Язык обучения: ${uiState.selectedLanguage.uppercase()}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = "Выучено слов",
                    value = uiState.wordsLearned.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Серия дней",
                    value = uiState.currentStreak.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            StatCard(
                title = "Прогресс на сегодня",
                value = "${uiState.dailyProgress}%",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: перейти в режим карточек */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Учить новые слова")
            }

            OutlinedButton(
                onClick = { /* TODO: перейти к повторению */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Повторение слов")
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = value, style = MaterialTheme.typography.titleLarge)
        }
    }
}