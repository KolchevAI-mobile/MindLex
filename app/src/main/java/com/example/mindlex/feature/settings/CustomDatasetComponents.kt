package com.example.mindlex.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindlex.R
import com.example.mindlex.domain.model.CustomDatasetMeta

private val CardShape = RoundedCornerShape(22.dp)

@Composable
fun CustomDatasetContent(
    state: CustomDatasetUiState,
    onTabSelected: (CustomDatasetTab) -> Unit,
    onBuilderWordChange: (String) -> Unit,
    onBuilderTranslationChange: (String) -> Unit,
    onBuilderNameChange: (String) -> Unit,
    onAddEntry: () -> Unit,
    onRemoveEntry: (Int) -> Unit,
    onSaveBuilder: () -> Unit,
    onImportFile: () -> Unit,
    onRefreshDataset: (String) -> Unit,
    onDeleteDataset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 16.dp
        )
    ) {
        if (state.isCustomModeActive) {
            item {
                ActiveDatasetBanner(meta = state.currentMeta!!)
            }
        }

        item {
            DatasetTabSelector(selected = state.selectedTab, onSelected = onTabSelected)
        }

        when (state.selectedTab) {
            CustomDatasetTab.BUILDER -> {
                item {
                    BuilderSection(
                        state = state,
                        onWordChange = onBuilderWordChange,
                        onTranslationChange = onBuilderTranslationChange,
                        onNameChange = onBuilderNameChange,
                        onAddEntry = onAddEntry,
                        onRemoveEntry = onRemoveEntry,
                        onSave = onSaveBuilder
                    )
                }
            }
            CustomDatasetTab.IMPORT_FILE -> {
                item {
                    ImportSection(
                        isLoading = state.isLoading,
                        onImportFile = onImportFile
                    )
                }
            }
        }

        if (state.history.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.custom_dataset_history_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.history, key = { it.id }) { dataset ->
                DatasetHistoryRow(
                    dataset = dataset,
                    isActive = state.currentMeta?.id == dataset.id,
                    isLoading = state.isLoading,
                    onRefresh = { onRefreshDataset(dataset.id) },
                    onDelete = { onDeleteDataset(dataset.id) }
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.custom_dataset_remote_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun ActiveDatasetBanner(meta: CustomDatasetMeta) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.custom_dataset_active_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = meta.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(
                    R.string.custom_dataset_active_meta,
                    meta.recordsCount,
                    meta.format
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.custom_dataset_offline_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatasetTabSelector(
    selected: CustomDatasetTab,
    onSelected: (CustomDatasetTab) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = selected == CustomDatasetTab.BUILDER,
            onClick = { onSelected(CustomDatasetTab.BUILDER) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            label = { Text(stringResource(R.string.custom_dataset_tab_builder)) }
        )
        SegmentedButton(
            selected = selected == CustomDatasetTab.IMPORT_FILE,
            onClick = { onSelected(CustomDatasetTab.IMPORT_FILE) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            label = { Text(stringResource(R.string.custom_dataset_tab_import)) }
        )
    }
}

@Composable
private fun BuilderSection(
    state: CustomDatasetUiState,
    onWordChange: (String) -> Unit,
    onTranslationChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onAddEntry: () -> Unit,
    onRemoveEntry: (Int) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.custom_dataset_builder_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = state.builderWord,
                onValueChange = onWordChange,
                label = { Text(stringResource(R.string.custom_dataset_field_word)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = state.builderTranslation,
                onValueChange = onTranslationChange,
                label = { Text(stringResource(R.string.custom_dataset_field_translation)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedButton(
                onClick = onAddEntry,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.custom_dataset_add_pair))
            }

            if (state.builderEntries.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.custom_dataset_builder_list, state.builderEntries.size),
                    style = MaterialTheme.typography.labelLarge
                )
                state.builderEntries.forEachIndexed { index, entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = entry.word, fontWeight = FontWeight.Medium)
                            Text(
                                text = entry.translation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onRemoveEntry(index) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.custom_dataset_remove_pair)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = state.builderName,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.custom_dataset_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSaveBuilder
                ) {
                    Text(stringResource(R.string.custom_dataset_save_builder))
                }
            }
        }
    }
}

@Composable
private fun ImportSection(
    isLoading: Boolean,
    onImportFile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.custom_dataset_import_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.custom_dataset_import_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onImportFile,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.custom_dataset_pick_file))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatasetHistoryRow(
    dataset: CustomDatasetMeta,
    isActive: Boolean,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * 0.35f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {}
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isActive) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = dataset.displayName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = stringResource(
                            R.string.custom_dataset_row_meta,
                            dataset.recordsCount,
                            dataset.format
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isActive) {
                        Text(
                            text = stringResource(R.string.custom_dataset_row_active),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (dataset.format != "MANUAL") {
                    IconButton(onClick = onRefresh, enabled = !isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.custom_dataset_refresh))
                    }
                }
            }
        }
    }
}
