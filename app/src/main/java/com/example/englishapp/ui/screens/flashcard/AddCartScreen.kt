package com.example.englishapp.ui.screens.flashcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.data.repository.FlashcardRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    deckId: Long,
    flashcardRepository: FlashcardRepository,
    onNavigateBack: () -> Unit,
    onCardAdded: () -> Unit
) {
    var frontContent by remember { mutableStateOf("") }
    var backContent by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var phonetic by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Card Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Front content (e.g., word or question)
            OutlinedTextField(
                value = frontContent,
                onValueChange = {
                    frontContent = it
                    showError = false
                },
                label = { Text("Front (Word/Question) *") },
                placeholder = { Text("e.g., Hello") },
                modifier = Modifier.fillMaxWidth(),
                isError = showError && frontContent.isBlank()
            )

            // Back content (e.g., meaning or answer)
            OutlinedTextField(
                value = backContent,
                onValueChange = {
                    backContent = it
                    showError = false
                },
                label = { Text("Back (Meaning/Answer) *") },
                placeholder = { Text("e.g., Xin chào") },
                modifier = Modifier.fillMaxWidth(),
                isError = showError && backContent.isBlank(),
                minLines = 2
            )

            if (showError && (frontContent.isBlank() || backContent.isBlank())) {
                Text(
                    text = "Both front and back are required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Phonetic (optional)
            OutlinedTextField(
                value = phonetic,
                onValueChange = { phonetic = it },
                label = { Text("Phonetic (Optional)") },
                placeholder = { Text("e.g., /həˈloʊ/") },
                modifier = Modifier.fillMaxWidth()
            )

            // Example (optional)
            OutlinedTextField(
                value = example,
                onValueChange = { example = it },
                label = { Text("Example (Optional)") },
                placeholder = { Text("e.g., Hello, how are you?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preview card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preview",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = frontContent.ifBlank { "Front content" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (phonetic.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = phonetic,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = backContent.ifBlank { "Back content" },
                        fontSize = 16.sp
                    )

                    if (example.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Example: $example",
                            fontSize = 14.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (frontContent.isBlank() || backContent.isBlank()) {
                            showError = true
                        } else {
                            scope.launch {
                                flashcardRepository.addCardToDeck(
                                    deckId = deckId,
                                    vocabId = null,
                                    frontContent = frontContent.trim(),
                                    backContent = backContent.trim(),
                                    example = example.trim().ifBlank { null },
                                    phonetic = phonetic.trim().ifBlank { null }
                                )
                                onCardAdded()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Card")
                }
            }
        }
    }
}