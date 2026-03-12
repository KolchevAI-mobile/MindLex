package com.example.mindlex.feature.learning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.domain.model.Vocabulary

/**
 * Простой экран для отладки логики загрузки слов через Supabase + Room.
 */
@Composable
fun LearningWordsScreen(
    viewModel: LearningWordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Учить слова (Supabase)") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.loadWords() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Обновить слова")
            }

            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {
                    LearningWordsList(words = uiState.words)
                }
            }
        }
    }
}

@Composable
private fun LearningWordsList(
    words: List<Vocabulary>
) {
    if (words.isEmpty()) {
        Text(
            text = "Слова не найдены. Попробуйте обновить или изменить язык/категорию.",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(words) { word ->
            VocabularyItem(word = word)
        }
    }
}

@Composable
private fun VocabularyItem(
    word: Vocabulary
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = word.word,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = word.translation,
            style = MaterialTheme.typography.bodyMedium
        )

        word.phonetic?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall
            )
        }

        word.example?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

