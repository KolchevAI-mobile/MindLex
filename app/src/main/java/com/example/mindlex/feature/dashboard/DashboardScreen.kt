package com.example.mindlex.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.ui.components.BookOpenDecorLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenSettings: () -> Unit = {},
    onStartLearning: () -> Unit = {},
    onQuickTraining: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenCustomDataset: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            DashboardTopBar(
                notificationBadgeText = state.notificationBadgeText,
                onImportDataset = onOpenCustomDataset,
                onOpenNotifications = onOpenNotifications
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(padding)
        ) {
            BookOpenDecorLayer()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                ) {
                    DashboardGreeting(
                        userName = state.userName,
                        languageLabel = state.languageLabel
                    )
                }

                DashboardProgressCard(
                    state = state,
                    onStartLearning = onStartLearning
                )

                AnimatedVisibility(visible = state.hasImportedCustomDataset) {
                    VocabularyModeCard(
                        checked = state.isOfflineCustomDatasetMode,
                        enabled = !state.isVocabularySwitchBusy,
                        onCheckedChange = viewModel::setOfflineCustomDatasetEnabled
                    )
                }

                DashboardBottomActions(
                    onOpenSettings = onOpenSettings,
                    onQuickTraining = onQuickTraining
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
