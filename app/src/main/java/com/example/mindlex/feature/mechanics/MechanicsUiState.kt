package com.example.mindlex.feature.mechanics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mindlex.R

data class MechanicEntry(
    val type: MechanicType,
    val titleRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val status: MechanicStatus = MechanicStatus.AVAILABLE
)

/** Карточки режимов — порядок и метаданные в одном месте. */
object MechanicsUiState {
    val entries = listOf(
        MechanicEntry(
            type = MechanicType.ACTIVE_RECALL,
            titleRes = R.string.mechanic_active_recall_title,
            descriptionRes = R.string.mechanic_active_recall_desc,
            icon = Icons.Default.Memory
        ),
        MechanicEntry(
            type = MechanicType.SYNONYM_CHAIN,
            titleRes = R.string.mechanic_synonym_title,
            descriptionRes = R.string.mechanic_synonym_desc,
            icon = Icons.Default.Psychology
        ),
        MechanicEntry(
            type = MechanicType.RUSH,
            titleRes = R.string.mechanic_rush_title,
            descriptionRes = R.string.mechanic_rush_desc,
            icon = Icons.Default.Bolt
        ),
        MechanicEntry(
            type = MechanicType.CLOZE,
            titleRes = R.string.mechanic_cloze_title,
            descriptionRes = R.string.mechanic_cloze_desc,
            icon = Icons.Default.Timer
        )
    )
}
