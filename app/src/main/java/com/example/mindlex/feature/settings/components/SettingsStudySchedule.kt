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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindlex.R
import kotlinx.datetime.LocalTime

@Composable
internal fun PreferredStudyTimePicker(
    timeLabel: String,
    time: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute -> onTimeSelected(LocalTime(hour, minute, 0)) },
                time.hour,
                time.minute,
                true
            ).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.settings_study_time, timeLabel))
    }
}

@Composable
internal fun StudyScheduleRecommendation(
    preferredTime: LocalTime,
    recommendedTimes: List<LocalTime>
) {
    val extraSlots = recommendedTimes.filterNot {
        it.hour == preferredTime.hour && it.minute == preferredTime.minute
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.settings_schedule_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        if (extraSlots.isEmpty()) {
            Text(
                text = stringResource(R.string.settings_schedule_single),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val slotsLabel = extraSlots.joinToString(" / ") { slot ->
                "%02d:%02d".format(slot.hour, slot.minute)
            }
            Text(
                text = slotsLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.settings_schedule_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
