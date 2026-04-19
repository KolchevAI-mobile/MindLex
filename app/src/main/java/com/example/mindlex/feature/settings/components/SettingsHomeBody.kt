package com.example.mindlex.feature.settings.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mindlex.feature.settings.Categories
import com.example.mindlex.feature.settings.Languages
import com.example.mindlex.feature.settings.SettingsViewModel

@Composable
internal fun SettingsHomeBody(
    uiState: SettingsViewModel.UiState,
    viewModel: SettingsViewModel,
    notificationPermissionLauncher: ActivityResultLauncher<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Текущие настройки",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${Languages.getDisplayName(uiState.selectedLanguage)} • ${Categories.getDisplayName(uiState.selectedCategory)} • ${uiState.dailyGoal} слов/день",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        SettingsCard(
            icon = Icons.Default.Person,
            title = "Ваше имя",
            description = "Как к вам обращаться"
        ) {
            UserNameField(
                userName = uiState.userName,
                onUserNameChange = viewModel::onUserNameChange
            )
        }

        SettingsCard(
            icon = Icons.Default.School,
            title = "Язык обучения",
            description = "Выберите язык, который хотите изучать"
        ) {
            LanguageDropdown(
                selectedLanguage = uiState.selectedLanguage,
                onLanguageSelected = viewModel::onLanguageSelected
            )
        }

        SettingsCard(
            icon = Icons.AutoMirrored.Filled.Sort,
            title = "Категория слов",
            description = "Выберите тему для изучения"
        ) {
            CategoryDropdown(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )
        }

        SettingsCard(
            icon = Icons.Default.Star,
            title = "Ежедневная цель",
            description = "Сколько слов вы хотите учить каждый день"
        ) {
            DailyGoalSlider(
                currentGoal = uiState.dailyGoal,
                onGoalChanged = viewModel::onDailyGoalChanged
            )
        }

        SettingsCard(
            icon = Icons.Default.Notifications,
            title = "Уведомления",
            description = "Получать напоминания об учёбе"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotificationToggle(
                    enabled = uiState.notificationsEnabled,
                    onToggle = { wantEnabled ->
                        handleNotificationToggle(
                            wantEnabled = wantEnabled,
                            context = context,
                            viewModel = viewModel,
                            notificationPermissionLauncher = notificationPermissionLauncher
                        )
                    }
                )
                PreferredStudyTimePicker(
                    time = uiState.preferredStudyTime,
                    onTimeSelected = viewModel::onPreferredStudyTimeChanged
                )
                StudyScheduleRecommendation(
                    preferredTime = uiState.preferredStudyTime,
                    recommendedTimes = viewModel.getRecommendedSessionTimes(
                        preferred = uiState.preferredStudyTime,
                        dailyGoal = uiState.dailyGoal
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun handleNotificationToggle(
    wantEnabled: Boolean,
    context: Context,
    viewModel: SettingsViewModel,
    notificationPermissionLauncher: ActivityResultLauncher<String>
) {
    if (!wantEnabled) {
        viewModel.onNotificationsDisabledByUser()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.onNotificationsEnabledByUser()
            }
            else -> {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    } else {
        viewModel.onNotificationsEnabledByUser()
    }
}
