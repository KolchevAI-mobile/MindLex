package com.example.mindlex.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.R
import com.example.mindlex.ui.components.BookOpenDecorLayer
import com.example.mindlex.ui.components.MechanicSessionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onOpenCustomDataset: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    SettingsPermissionEffects(viewModel = viewModel, snackbarHost = snackbarHost)

    val requestNotificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.onNotificationsEnabledByUser()
        else viewModel.onNotificationPermissionDenied()
    }

    LaunchedEffect(state.saveMessage) {
        state.saveMessage?.let { message ->
            snackbarHost.showSnackbar(message)
            viewModel.clearSaveMessage()
        }
    }

    Scaffold(
        topBar = {
            MechanicSessionHeader(
                title = stringResource(R.string.settings_title),
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BookOpenDecorLayer()
            SettingsContent(
                state = state,
                onUserNameChange = viewModel::onUserNameChange,
                onLanguageSelected = viewModel::onLanguageSelected,
                onCategorySelected = viewModel::onCategorySelected,
                onDailyGoalChanged = viewModel::onDailyGoalChanged,
                onPreferredStudyTimeChanged = viewModel::onPreferredStudyTimeChanged,
                recommendedSessionTimes = viewModel::recommendedSessionTimes,
                onNotificationsToggle = { enabled ->
                    handleNotificationToggle(
                        wantEnabled = enabled,
                        context = context,
                        viewModel = viewModel,
                        permissionLauncher = requestNotificationPermission
                    )
                },
                onOpenCustomDataset = onOpenCustomDataset,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
                            )
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}
