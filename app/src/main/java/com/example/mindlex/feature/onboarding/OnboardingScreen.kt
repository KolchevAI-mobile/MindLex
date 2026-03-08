package com.example.mindlex.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.completed) {
        if (uiState.completed) {
            onCompleted()
        }
    }

    val languages = listOf("en", "de", "fr", "es")
    var isLanguageMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Добро пожаловать в MindLex!",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = uiState.userName,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Ваше имя") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.selectedLanguage,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isLanguageMenuExpanded = true },
                    label = { Text("Язык изучения") },
                    enabled = false,
                    readOnly = true
                )

                DropdownMenu(
                    expanded = isLanguageMenuExpanded,
                    onDismissRequest = { isLanguageMenuExpanded = false }
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.uppercase()) },
                            onClick = {
                                viewModel.onLanguageSelected(lang)
                                isLanguageMenuExpanded = false
                            }
                        )
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            Button(
                onClick = { viewModel.onCompleteClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Начать обучение")
            }
        }
    }
}
