package com.example.englishapp.ui.screens.pronunciation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.domain.model.Vocabulary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PronunciationWordSelectionScreen(
    viewModel: PronunciationViewModel,
    onNavigateBack: () -> Unit,
    onWordSelected: (Long) -> Unit
) {
    val vocabularyList by viewModel.vocabularyList.collectAsState()
    val pronunciationStats by viewModel.pronunciationStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn từ để luyện phát âm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (vocabularyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có từ vựng.\nHãy thêm từ vựng để bắt đầu luyện phát âm!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vocabularyList) { vocab ->
                    val stats = pronunciationStats[vocab.id]
                    VocabularyPronunciationCard(
                        vocabulary = vocab,
                        practiceCount = stats?.practiceCount ?: 0,
                        averageScore = stats?.averageScore,
                        onClick = { onWordSelected(vocab.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun VocabularyPronunciationCard(
    vocabulary: Vocabulary,
    practiceCount: Int,
    averageScore: Double?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vocabulary.word,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                vocabulary.phonetic?.let { phonetic ->
                    Text(
                        text = phonetic,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                Text(
                    text = vocabulary.meaning,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                if (practiceCount > 0) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Đã luyện: $practiceCount lần",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        
                        averageScore?.let { score ->
                            Text(
                                text = "Điểm TB: ${score.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    score >= 90 -> Color(0xFF4CAF50)
                                    score >= 75 -> Color(0xFF8BC34A)
                                    score >= 60 -> Color(0xFFFFC107)
                                    else -> Color(0xFFF44336)
                                }
                            )
                        }
                    }
                }
            }
            
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Practice pronunciation",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

data class VocabularyPronunciationStats(
    val practiceCount: Int,
    val averageScore: Double?
)
