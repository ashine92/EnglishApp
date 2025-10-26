package com.example.englishapp.ui.screens.vocabulary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.domain.model.LearningStatus
import com.example.englishapp.domain.model.Vocabulary
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabListScreen(
    viewModel: VocabViewModel = koinViewModel(),
    onVocabClick: (Vocabulary) -> Unit
) {
    val vocabList by viewModel.vocabList.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search and filter bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        if (query.isBlank()) {
                            viewModel.loadVocabulary()
                        } else {
                            viewModel.searchVocabs(query)
                        }
                    },
                    label = { Text("Tìm kiếm từ vựng") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterChip(
                        selected = filterStatus != null,
                        onClick = { showFilterDialog = true },
                        label = {
                            Text(
                                filterStatus?.name ?: "Tất cả trạng thái"
                            )
                        }
                    )

                    if (filterStatus != null) {
                        TextButton(
                            onClick = { viewModel.filterByStatus(null) }
                        ) {
                            Text("Xóa bộ lọc")
                        }
                    }
                }
            }
        }

        // Vocab count
        Text(
            text = "Tổng: ${vocabList.size} từ",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Vocab list
        if (vocabList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có từ vựng nào",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = vocabList,
                    key = { vocab -> vocab.id }
                ) { vocab ->
                    VocabListItem(
                        vocabulary = vocab,
                        onClick = { onVocabClick(vocab) },
                        onDelete = { viewModel.deleteVocabulary(vocab) },
                        onStatusChange = { newStatus ->
                            viewModel.updateVocabularyStatus(vocab, newStatus)
                        }
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        StatusFilterDialog(
            currentStatus = filterStatus,
            onDismiss = { showFilterDialog = false },
            onStatusSelected = { status ->
                viewModel.filterByStatus(status)
                showFilterDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabListItem(
    vocabulary: Vocabulary,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (LearningStatus) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vocabulary.word,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    vocabulary.phonetic?.let { phonetic ->
                        Text(
                            text = phonetic,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = vocabulary.meaning,
                        fontSize = 14.sp,
                        maxLines = 2
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Text("⋮", fontSize = 24.sp)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mới học") },
                            onClick = {
                                onStatusChange(LearningStatus.NEW)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Đang học") },
                            onClick = {
                                onStatusChange(LearningStatus.LEARNING)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Đã thuộc") },
                            onClick = {
                                onStatusChange(LearningStatus.MASTERED)
                                showMenu = false
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Xóa", color = Color.Red) },
                            onClick = {
                                showDeleteDialog = true
                                showMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(vocabulary.learningStatus)

                vocabulary.category?.let { category ->
                    CategoryBadge(category)
                }

                if (vocabulary.correctCount > 0 || vocabulary.wrongCount > 0) {
                    AccuracyBadge(
                        correct = vocabulary.correctCount,
                        wrong = vocabulary.wrongCount
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa từ vựng") },
            text = { Text("Bạn có chắc muốn xóa từ \"${vocabulary.word}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun StatusBadge(status: LearningStatus) {
    val (color, text) = when (status) {
        LearningStatus.NEW -> Color(0xFF2196F3) to "Mới"
        LearningStatus.LEARNING -> Color(0xFFFFC107) to "Đang học"
        LearningStatus.MASTERED -> Color(0xFF4CAF50) to "Đã thuộc"
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = color
        )
    }
}

@Composable
fun CategoryBadge(category: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun AccuracyBadge(correct: Int, wrong: Int) {
    val total = correct + wrong
    val accuracy = if (total > 0) (correct.toFloat() / total * 100).toInt() else 0

    Surface(
        color = Color(0xFFE0E0E0),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "✓$correct ✗$wrong ($accuracy%)",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp
        )
    }
}

@Composable
fun StatusFilterDialog(
    currentStatus: LearningStatus?,
    onDismiss: () -> Unit,
    onStatusSelected: (LearningStatus?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lọc theo trạng thái") },
        text = {
            Column {
                TextButton(
                    onClick = { onStatusSelected(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tất cả")
                }

                LearningStatus.values().forEach { status ->
                    TextButton(
                        onClick = { onStatusSelected(status) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentStatus == status,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (status) {
                                    LearningStatus.NEW -> "Mới học"
                                    LearningStatus.LEARNING -> "Đang học"
                                    LearningStatus.MASTERED -> "Đã thuộc"
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}