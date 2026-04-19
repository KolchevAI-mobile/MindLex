package com.example.mindlex.feature.settings.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.LocalTime

@Composable
internal fun PreferredStudyTimePicker(
    time: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    onTimeSelected(LocalTime(hour, minute, 0))
                },
                time.hour,
                time.minute,
                true
            ).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Основное время занятия: ${"%02d:%02d".format(time.hour, time.minute)}")
    }
}

@Composable
internal fun StudyScheduleRecommendation(
    preferredTime: LocalTime,
    recommendedTimes: List<LocalTime>
) {
    val algorithmExtraSlots = recommendedTimes.filterNot {
        it.hour == preferredTime.hour && it.minute == preferredTime.minute
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "💡 Рекомендуемое время",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        if (algorithmExtraSlots.isEmpty()) {
            Text(
                text = "При вашей цели достаточно одной сессии в выбранное основное время.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val label = algorithmExtraSlots.joinToString(" / ") { "%02d:%02d".format(it.hour, it.minute) }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "(расписание по вашей цели: ±4 ч от основного времени)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
