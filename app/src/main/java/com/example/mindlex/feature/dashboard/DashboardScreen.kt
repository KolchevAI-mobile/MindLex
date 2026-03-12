package com.example.mindlex.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    onOpenSettings: () -> Unit = {},
    onStartLearning: () -> Unit = {},
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
                onClick = onStartLearning,
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