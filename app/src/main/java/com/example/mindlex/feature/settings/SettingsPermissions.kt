package com.example.mindlex.feature.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mindlex.R

/** Следим за разрешением на уведомления и показываем snackbar при отказе. */
@Composable
fun SettingsPermissionEffects(
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

fun handleNotificationToggle(
    wantEnabled: Boolean,
    context: Context,
    viewModel: SettingsViewModel,
    permissionLauncher: ActivityResultLauncher<String>
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
