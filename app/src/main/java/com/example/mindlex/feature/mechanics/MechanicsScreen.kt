package com.example.mindlex.feature.mechanics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mindlex.ui.components.MechanicSessionHeader
import com.example.mindlex.feature.mechanics.components.MechanicCard

/**
 * Типы механик обучения.
 */
enum class MechanicType {
    ACTIVE_RECALL,
    CLOZE,
    RUSH,
    SYNONYM_CHAIN,
    FLASHCARDS,
    LISTENING
}

/**
 * Статус доступности механики.
 */
enum class MechanicStatus {
    AVAILABLE,
    COMING_SOON
}

/**
 * Экран выбора режима обучения.
 *
 * @param onBackClick Callback для возврата назад
 * @param onMechanicSelected Callback при выборе механики
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicsScreen(
    onBackClick: () -> Unit,
    onMechanicSelected: (MechanicType) -> Unit
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        MechanicSessionHeader(
            title = "Режимы обучения",
            onBackClick = onBackClick
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(16.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Выберите режим обучения",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MechanicCard(
                            title = "Активное вспоминание",
                            description = "Вводите слово по переводу и закрепляйте долговременную память.",
                            icon = Icons.Default.Memory,
                            status = MechanicStatus.AVAILABLE,
                            onClick = { onMechanicSelected(MechanicType.ACTIVE_RECALL) }
                        )

                        MechanicCard(
                            title = "Цепочка синонимов",
                            description = "Собирайте смысловые связи и прокачивайте словарную гибкость.",
                            icon = Icons.Default.Psychology,
                            status = MechanicStatus.AVAILABLE,
                            onClick = { onMechanicSelected(MechanicType.SYNONYM_CHAIN) }
                        )

                        MechanicCard(
                            title = "Спринт на скорость",
                            description = "Переведите максимум слов за ограниченное время.",
                            icon = Icons.Default.Bolt,
                            status = MechanicStatus.AVAILABLE,
                            onClick = { onMechanicSelected(MechanicType.RUSH) }
                        )

                        MechanicCard(
                            title = "Контекстный пропуск",
                            description = "Заполните пропуск в предложении, опираясь на контекст.",
                            icon = Icons.Default.Timer,
                            status = MechanicStatus.AVAILABLE,
                            onClick = { onMechanicSelected(MechanicType.CLOZE) }
                        )
                    }
                }
            }
        }
    }
}
