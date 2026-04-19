package com.example.mindlex.feature.mechanics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mindlex.R
import com.example.mindlex.feature.mechanics.components.MechanicCard
import com.example.mindlex.ui.components.MechanicSessionHeader

enum class MechanicType {
    ACTIVE_RECALL,
    CLOZE,
    RUSH,
    SYNONYM_CHAIN
}

enum class MechanicStatus {
    AVAILABLE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicsScreen(
    onBackClick: () -> Unit,
    onMechanicSelected: (MechanicType) -> Unit
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        MechanicSessionHeader(
            title = stringResource(R.string.mechanics_header),
            onBackClick = onBackClick
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(16.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.mechanics_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MechanicCard(
                        title = stringResource(R.string.mechanic_active_recall_title),
                        description = stringResource(R.string.mechanic_active_recall_desc),
                        icon = Icons.Default.Memory,
                        status = MechanicStatus.AVAILABLE,
                        onClick = { onMechanicSelected(MechanicType.ACTIVE_RECALL) }
                    )

                    MechanicCard(
                        title = stringResource(R.string.mechanic_synonym_title),
                        description = stringResource(R.string.mechanic_synonym_desc),
                        icon = Icons.Default.Psychology,
                        status = MechanicStatus.AVAILABLE,
                        onClick = { onMechanicSelected(MechanicType.SYNONYM_CHAIN) }
                    )

                    MechanicCard(
                        title = stringResource(R.string.mechanic_rush_title),
                        description = stringResource(R.string.mechanic_rush_desc),
                        icon = Icons.Default.Bolt,
                        status = MechanicStatus.AVAILABLE,
                        onClick = { onMechanicSelected(MechanicType.RUSH) }
                    )

                    MechanicCard(
                        title = stringResource(R.string.mechanic_cloze_title),
                        description = stringResource(R.string.mechanic_cloze_desc),
                        icon = Icons.Default.Timer,
                        status = MechanicStatus.AVAILABLE,
                        onClick = { onMechanicSelected(MechanicType.CLOZE) }
                    )
                }
            }
        }
    }
}
