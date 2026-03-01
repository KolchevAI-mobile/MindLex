package com.example.mindlex.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mindlex.ui.theme.MindLexTheme

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onStartClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Добро пожаловать в MindLex",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onStartClick
        ) {
            Text(text = "Начать")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    MindLexTheme {
        OnboardingScreen(
            onStartClick = {}
        )
    }
}