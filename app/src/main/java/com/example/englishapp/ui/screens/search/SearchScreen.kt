package com.example.englishapp.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.domain.model.Vocabulary
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var vocabToSave by remember { mutableStateOf<Vocabulary?>(null) }

    val searchState by viewModel.searchResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tra Từ",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Nhập từ cần tra") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { viewModel.searchWord(searchQuery) }
            ),
            trailingIcon = {
                Button(
                    onClick = { viewModel.searchWord(searchQuery) }
                ) {
                    Text("Tra")
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = searchState) {
            is SearchState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nhập từ để tra cứu",
                        color = Color.Gray
                    )
                }
            }
            is SearchState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SearchState.Success -> {
                VocabularyDetailCard(
                    vocabulary = state.vocabulary,
                    onSave = {
                        vocabToSave = state.vocabulary
                        showCategoryDialog = true
                    }
                )
            }
            is SearchState.Saved -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "✓ Đã lưu từ vựng",
                        fontSize = 20.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.resetSearch() }) {
                        Text("Tra từ khác")
                    }
                }
            }
            is SearchState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.resetSearch() }) {
                        Text("Thử lại")
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        CategorySelectionDialog(
            onDismiss = { showCategoryDialog = false },
            onCategorySelected = { category ->
                vocabToSave?.let {
                    viewModel.saveVocabulary(it, category)
                }
                showCategoryDialog = false
            }
        )
    }
}

@Composable
fun VocabularyDetailCard(
    vocabulary: Vocabulary,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = vocabulary.word,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            vocabulary.phonetic?.let { phonetic ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phonetic,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            vocabulary.partOfSpeech?.let { pos ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pos,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nghĩa:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = vocabulary.meaning,
                fontSize = 16.sp
            )

            vocabulary.example?.let { example ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ví dụ:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = example,
                    fontSize = 16.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lưu Từ Vựng")
            }
        }
    }
}

@Composable
fun CategorySelectionDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var customCategory by remember { mutableStateOf("") }

    val predefinedCategories = listOf(
        "Công việc",
        "Công nghệ",
        "Hàng ngày",
        "Du lịch",
        "Ẩm thực",
        "Sức khỏe",
        "Giáo dục",
        "Giải trí"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn Chủ Đề") },
        text = {
            Column {
                predefinedCategories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category
                                customCategory = ""
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(category)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customCategory,
                    onValueChange = {
                        customCategory = it
                        if (it.isNotBlank()) {
                            selectedCategory = null
                        }
                    },
                    label = { Text("Hoặc nhập chủ đề khác") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val category = if (customCategory.isNotBlank()) {
                        customCategory
                    } else {
                        selectedCategory
                    }
                    onCategorySelected(category)
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}