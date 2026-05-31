package com.example.mindlex.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindlex.R
import com.example.mindlex.feature.settings.SettingsUiState
import kotlinx.datetime.LocalTime

@Composable
internal fun SettingsHomeBody(
    state: SettingsUiState,
    onUserNameChange: (String) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onDailyGoalChanged: (Int) -> Unit,
    onPreferredStudyTimeChanged: (LocalTime) -> Unit,
    recommendedSessionTimes: (LocalTime, Int) -> List<LocalTime>,
    onNotificationsToggle: (Boolean) -> Unit,
    onOpenCustomDataset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSummaryCard(summaryLine = state.summaryLine)

        SettingsCard(
            icon = Icons.Default.Person,
            title = stringResource(R.string.settings_name_title),
            description = stringResource(R.string.settings_name_subtitle)
        ) {
            UserNameField(
                userName = state.userName,
                onUserNameChange = onUserNameChange
            )
        }

        SettingsCard(
            icon = Icons.Default.School,
            title = stringResource(R.string.settings_language_title),
            description = stringResource(R.string.settings_language_subtitle)
        ) {
            LanguageDropdown(
                selectedLanguage = state.selectedLanguage,
                onLanguageSelected = onLanguageSelected
            )
        }

        SettingsCard(
            icon = Icons.AutoMirrored.Filled.Sort,
            title = stringResource(R.string.settings_category_title),
            description = stringResource(R.string.settings_category_subtitle)
        ) {
            CategoryDropdown(
                selectedCategory = state.selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        SettingsCard(
            icon = Icons.Default.Star,
            title = stringResource(R.string.settings_goal_title),
            description = stringResource(R.string.settings_goal_subtitle)
        ) {
            DailyGoalSlider(
                currentGoal = state.dailyGoal,
                onGoalChanged = onDailyGoalChanged
            )
        }

        SettingsCard(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.settings_notifications_title),
            description = stringResource(R.string.settings_notifications_subtitle)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotificationToggle(
                    enabled = state.notificationsEnabled,
                    onToggle = onNotificationsToggle
                )
                if (state.notificationsEnabled) {
                    PreferredStudyTimePicker(
                        timeLabel = state.preferredTimeLabel,
                        time = state.preferredStudyTime,
                        onTimeSelected = onPreferredStudyTimeChanged
                    )
                    StudyScheduleRecommendation(
                        preferredTime = state.preferredStudyTime,
                        recommendedTimes = recommendedSessionTimes(
                            state.preferredStudyTime,
                            state.dailyGoal
                        )
                    )
                }
            }
        }

        SettingsCard(
            icon = Icons.Default.Add,
            title = stringResource(R.string.settings_dataset_title),
            description = stringResource(R.string.settings_dataset_subtitle)
        ) {
            SettingsActionButton(
                text = stringResource(R.string.settings_dataset_action),
                onClick = onOpenCustomDataset
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsSummaryCard(summaryLine: String) {
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
                text = stringResource(R.string.settings_summary_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = summaryLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
