package com.example.englishapp.ui.screens.flashcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.data.repository.FlashcardRepository
import com.example.englishapp.domain.model.Flashcard
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardDeckDetailScreen(
    deckId: Long,
    viewModel: FlashcardViewModel = koinViewModel(),
    flashcardRepository: FlashcardRepository,
    onNavigateBack: () -> Unit,
    onStartStudy: (Long) -> Unit,
    onAddCard: () -> Unit
) {
    val selectedDeck by viewModel.selectedDeck.collectAsState()
    val cards = flashcardRepository.getCardsByDeck(deckId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LaunchedEffect(deckId) {
        viewModel.loadDeckWithStats(deckId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedDeck?.name ?: "Deck Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Deck statistics
            selectedDeck?.let { deck ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        deck.description?.let { desc ->
                            Text(
                                text = desc,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatChip("Total", deck.cardCount.toString(), Color(0xFF2196F3))
                            StatChip("New", deck.newCards.toString(), Color(0xFF4CAF50))
                            StatChip("Learning", deck.learningCards.toString(), Color(0xFFFFC107))
                            StatChip("Review", deck.reviewCards.toString(), Color(0xFF9C27B0))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onStartStudy(deckId) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = deck.dueToday > 0
                        ) {
                            Text(
                                if (deck.dueToday > 0)
                                    "Study Now (${deck.dueToday} cards)"
                                else
                                    "No cards due today"
                            )
                        }
                    }
                }
            }

            // Cards list
            if (cards.value.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📝", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No cards yet", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Add your first card to start learning!", color = Color.Gray)
                    }
                }
            } else {
                Text(
                    text = "Cards (${cards.value.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cards.value) { card ->
                        FlashcardItem(
                            card = card,
                            onDelete = {
                                scope.launch {
                                    flashcardRepository.deleteCard(card.id)
                                    viewModel.loadDeckWithStats(deckId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardItem(
    card: Flashcard,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.frontContent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.backContent,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                card.progress?.let { progress ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status: ${progress.cardStatus.name}",
                        fontSize = 12.sp,
                        color = when (progress.cardStatus.name) {
                            "NEW" -> Color(0xFF4CAF50)
                            "LEARNING" -> Color(0xFFFFC107)
                            "REVIEW" -> Color(0xFF9C27B0)
                            else -> Color.Gray
                        }
                    )
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Text("🗑️", fontSize = 18.sp)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Card?") },
            text = { Text("Are you sure you want to delete this card?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}