package com.example.mindlex.feature.active_recall.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Карточка обратной связи после ответа пользователя.
 *
 * @param isCorrect Правильный ли ответ
 * @param correctAnswer Правильный ответ для отображения
 * @param quality Качество ответа (1-5)
 * @param onContinue Callback для перехода к следующему слову
 */
@Composable
fun FeedbackCard(
    isCorrect: Boolean,
    correctAnswer: String?,
    quality: Int,
    onContinue: () -> Unit
) {
    val containerColor = if (isCorrect) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = if (isCorrect) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = if (isCorrect) "Правильно" else "Неправильно",
                tint = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )

            Text(
                text = if (isCorrect) "Правильно!" else "Неправильно",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )

            if (!isCorrect && correctAnswer != null) {
                Text(
                    text = "Правильный ответ: $correctAnswer",
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
            }

            Text(
                text = "Качество: $quality/5",
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f)
            )

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Далее")
            }
        }
    }
}

/**
 * Нейтральная карточка обратной связи при использовании подсказки.
 * Не показывает success/fail — просто правильный ответ и кнопку продолжения.
 *
 * @param correctAnswer Правильный ответ для отображения
 * @param onContinue Callback для перехода к следующему слову
 */
@Composable
fun HintUsedFeedbackCard(
    correctAnswer: String?,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Слово запомнено. Повторим позже.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (correctAnswer != null) {
                Text(
                    text = "Правильный ответ: $correctAnswer",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Продолжить")
            }
        }
    }
}
