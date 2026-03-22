package com.example.mindlex.feature.mechanics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mindlex.feature.mechanics.components.MechanicCard
import kotlinx.coroutines.launch

/**
 * Типы механик обучения.
 */
enum class MechanicType {
    ACTIVE_RECALL,
    CLOZE,
    RUSH,
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Режимы обучения") },
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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Выберите режим обучения",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // 1. Карточка "Активное вспоминание" (доступна)
            MechanicCard(
                title = "Active Recall",
                description = "Вводите слово по переводу. Эффективно для запоминания.",
                icon = Icons.Default.Memory,
                status = MechanicStatus.AVAILABLE,
                onClick = { onMechanicSelected(MechanicType.ACTIVE_RECALL) }
            )

            // 2. Карточка "Synonym Chain" (скоро будет)
            MechanicCard(
                title = "Synonym Chain",
                description = "Цепочка синонимов",
                icon = Icons.Default.Memory,
                status = MechanicStatus.COMING_SOON,
                onClick = { 
                    scope.launch {
                        snackbarHostState.showSnackbar("Скоро будет доступно")
                    }
                }
            )

            // 3. Спринт на скорость (Timed Translation Rush)
            MechanicCard(
                title = "Спринт на скорость",
                description = "Переведи максимум слов за отведённое время",
                icon = Icons.Default.Timer,
                status = MechanicStatus.AVAILABLE,
                onClick = { onMechanicSelected(MechanicType.RUSH) }
            )

            // 4. Карточка «Контекстный пропуск» (contextual cloze)
            MechanicCard(
                title = "Контекстный пропуск",
                description = "Заполните пропуск в предложении по смыслу",
                icon = Icons.Default.Memory,
                status = MechanicStatus.AVAILABLE,
                onClick = { onMechanicSelected(MechanicType.CLOZE) }
            )
        }
    }
}
