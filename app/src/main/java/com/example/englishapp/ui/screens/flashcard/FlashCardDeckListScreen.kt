package com.example.englishapp.ui.screens.flashcard

import androidx.compose.foundation.clickable
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
import com.example.englishapp.domain.model.FlashcardDeck
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardDeckListScreen(
    viewModel: FlashcardViewModel = koinViewModel(),
    onDeckClick: (Long) -> Unit,
    onCreateDeck: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<FlashcardDeck?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashcard Decks") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateDeck,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Deck")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is FlashcardUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is FlashcardUiState.Empty -> {
                    FlashcardEmptyContent(
                        emoji = "📚",
                        title = "No Flashcard Decks Yet",
                        description = "Create your first deck to start learning!",
                        buttonText = "Create Deck",
                        onButtonClick = onCreateDeck,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is FlashcardUiState.Success -> {
                    DeckListContent(
                        decks = state.decks,
                        onDeckClick = onDeckClick,
                        onDeleteClick = { deck ->
                            deckToDelete = deck
                            showDeleteDialog = true
                        }
                    )
                }
                is FlashcardUiState.Error -> {
                    FlashcardErrorContent(  // ← Đổi tên
                        message = state.message,
                        onRetry = { viewModel.loadDecks() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog && deckToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Deck?") },
                text = { Text("Are you sure you want to delete '${deckToDelete?.name}'? All cards will be deleted.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deckToDelete?.let { viewModel.deleteDeck(it.id) }
                            showDeleteDialog = false
                            deckToDelete = null
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
}

@Composable
fun DeckListContent(
    decks: List<FlashcardDeck>,
    onDeckClick: (Long) -> Unit,
    onDeleteClick: (FlashcardDeck) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(decks) { deck ->
            DeckCard(
                deck = deck,
                onClick = { onDeckClick(deck.id) },
                onDeleteClick = { onDeleteClick(deck) }
            )
        }
    }
}

@Composable
fun DeckCard(
    deck: FlashcardDeck,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deck.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    deck.description?.let { desc ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(onClick = onDeleteClick) {
                    Text("🗑️", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatChip(
                    label = "Total",
                    value = deck.cardCount.toString(),
                    color = Color(0xFF2196F3)
                )
                StatChip(
                    label = "New",
                    value = deck.newCards.toString(),
                    color = Color(0xFF4CAF50)
                )
                StatChip(
                    label = "Learning",
                    value = deck.learningCards.toString(),
                    color = Color(0xFFFFC107)
                )
                StatChip(
                    label = "Review",
                    value = deck.reviewCards.toString(),
                    color = Color(0xFF9C27B0)
                )
            }

            if (deck.dueToday > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📅 ${deck.dueToday} cards due today",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun StatChip(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

