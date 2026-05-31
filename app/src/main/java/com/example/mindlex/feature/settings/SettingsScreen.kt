package com.example.mindlex.feature.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mindlex.R
import com.example.mindlex.feature.settings.components.SettingsHomeBody
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
            SettingsHomeBody(
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
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsPermissionEffects(
    viewModel: SettingsViewModel,
    snackbarHost: SnackbarHostState
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionSnackbar = stringResource(R.string.settings_permission_snackbar)
    val permissionAction = stringResource(R.string.settings_permission_action)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                viewModel.syncRuntimeNotificationPermission(granted)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.permissionEvents.collect { event ->
            if (event is SettingsViewModel.PermissionEvent.OpenSystemSettings) {
                val result = snackbarHost.showSnackbar(
                    message = permissionSnackbar,
                    actionLabel = permissionAction,
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    openAppNotificationSettings(context)
                }
            }
        }
    }
}

private fun handleNotificationToggle(
    wantEnabled: Boolean,
    context: Context,
    viewModel: SettingsViewModel,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    if (!wantEnabled) {
        viewModel.onNotificationsDisabledByUser()
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.onNotificationsEnabledByUser()
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    } else {
        viewModel.onNotificationsEnabledByUser()
    }
}
