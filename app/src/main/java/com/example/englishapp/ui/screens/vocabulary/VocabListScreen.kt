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
    val syncStatus by viewModel.syncStatus.collectAsState()
    val unlearnedCount by viewModel.unlearnedVocabCount.collectAsState()
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSyncDialog by remember { mutableStateOf(false) }

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

        // Vocab count and Firebase sync button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tổng: ${vocabList.size} từ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Chưa học: $unlearnedCount từ",
                    fontSize = 12.sp,
                    color = Color(0xFFFFC107)
                )
            }

            Button(
                onClick = { showSyncDialog = true },
                enabled = unlearnedCount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("🔄 Sync Firebase")
            }
        }

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

    if (showSyncDialog) {
        FirebaseSyncDialog(
            unlearnedCount = unlearnedCount,
            syncStatus = syncStatus,
            onDismiss = {
                showSyncDialog = false
                viewModel.resetSyncStatus()
            },
            onConfirm = {
                viewModel.syncToFirebase()
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
                            text = { Text("Chưa học") },
                            onClick = {
                                onStatusChange(LearningStatus.NOT_LEARNED)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Đã học") },
                            onClick = {
                                onStatusChange(LearningStatus.LEARNED)
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
        LearningStatus.NOT_LEARNED -> Color(0xFFFFC107) to "Chưa học"
        LearningStatus.LEARNED -> Color(0xFF4CAF50) to "Đã học"
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
                                    LearningStatus.NOT_LEARNED -> "Chưa học"
                                    LearningStatus.LEARNED -> "Đã học"
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

@Composable
fun FirebaseSyncDialog(
    unlearnedCount: Int,
    syncStatus: SyncStatus,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🔄 Đồng bộ Firebase") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (syncStatus) {
                    is SyncStatus.Idle -> {
                        Text(
                            text = "Bạn có muốn đồng bộ $unlearnedCount từ chưa học lên Firebase?",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ESP32 sẽ hiển thị các từ này trên màn hình LCD.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    is SyncStatus.Loading -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Đang đồng bộ...")
                    }
                    is SyncStatus.Success -> {
                        Text(
                            text = "✅ Đã đồng bộ ${syncStatus.count} từ thành công!",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ESP32 có thể đọc dữ liệu từ Firebase ngay bây giờ.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    is SyncStatus.Error -> {
                        Text(
                            text = "❌ Lỗi: ${syncStatus.message}",
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vui lòng kiểm tra kết nối Internet và thử lại.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (syncStatus) {
                is SyncStatus.Idle -> {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Đồng bộ")
                    }
                }
                is SyncStatus.Success, is SyncStatus.Error -> {
                    Button(onClick = onDismiss) {
                        Text("Đóng")
                    }
                }
                is SyncStatus.Loading -> {
                    // Không hiển thị nút khi đang loading
                }
            }
        },
        dismissButton = {
            if (syncStatus is SyncStatus.Idle) {
                TextButton(onClick = onDismiss) {
                    Text("Hủy")
                }
            }
        }
    )
}