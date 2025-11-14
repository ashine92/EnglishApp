package com.example.englishapp.ui.screens.flashcard

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.domain.model.Flashcard
import com.example.englishapp.domain.model.Rating
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardStudyScreen(
    deckId: Long,
    viewModel: FlashcardStudyViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val studyState by viewModel.studyState.collectAsState()
    val sessionStats by viewModel.sessionStats.collectAsState()

    LaunchedEffect(deckId) {
        viewModel.loadDueCards(deckId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Session") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = studyState) {
                is StudyState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is StudyState.NoCards -> {
                    FlashcardEmptyContent(
                        emoji = "🎉",
                        title = "No Cards Due!",
                        description = "You've reviewed all cards for today.",
                        buttonText = "Back to Deck",
                        onButtonClick = onNavigateBack,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is StudyState.Studying -> {
                    StudyingContent(
                        card = state.card,
                        currentIndex = state.currentIndex,
                        totalCards = state.totalCards,
                        sessionStats = sessionStats,
                        onRating = { rating ->
                            viewModel.submitRating(rating)
                        }
                    )
                }
                is StudyState.Finished -> {
                    FinishedContent(
                        stats = state.stats,
                        duration = state.duration,
                        onFinish = onNavigateBack,
                        onRestart = {
                            viewModel.resetSession()
                            viewModel.loadDueCards(deckId)
                        }
                    )
                }
                is StudyState.Error -> {
                    FlashcardErrorContent(  // ← Đổi tên
                        message = state.message,
                        onRetry = { viewModel.loadDueCards(deckId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun StudyingContent(
    card: Flashcard,
    currentIndex: Int,
    totalCards: Int,
    sessionStats: SessionStats,
    onRating: (Rating) -> Unit
) {
    var isFlipped by remember(card.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = (currentIndex + 1).toFloat() / totalCards,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Card ${currentIndex + 1} of $totalCards",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "${sessionStats.cardsStudied} studied",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Flip card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FlipCard(
                card = card,
                isFlipped = isFlipped,
                onFlip = { isFlipped = !isFlipped }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Show answer button (when not flipped)
        if (!isFlipped) {
            Button(
                onClick = { isFlipped = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Show Answer", fontSize = 18.sp)
            }
        } else {
            // Rating buttons (when flipped)
            Text(
                text = "Rate your recall:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingButton(
                    text = "Again",
                    subtitle = "<1m",
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f),
                    onClick = { onRating(Rating.AGAIN) }
                )
                RatingButton(
                    text = "Hard",
                    subtitle = "<10m",
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f),
                    onClick = { onRating(Rating.HARD) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingButton(
                    text = "Good",
                    subtitle = "1d",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    onClick = { onRating(Rating.GOOD) }
                )
                RatingButton(
                    text = "Easy",
                    subtitle = "4d",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                    onClick = { onRating(Rating.EASY) }
                )
            }
        }
    }
}

@Composable
fun FlipCard(
    card: Flashcard,
    isFlipped: Boolean,
    onFlip: () -> Unit
) {
    // Animation for card flip
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "cardRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rotation > 90f)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onFlip
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Front side (word)
            if (rotation <= 90f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = card.frontContent,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    card.phonetic?.let { phonetic ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = phonetic,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Tap to flip",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
            // Back side (meaning)
            else {
                Column(
                    modifier = Modifier.graphicsLayer {
                        rotationY = 180f
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = card.backContent,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    card.example?.let { example ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Example:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = example,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Tap to flip back",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun RatingButton(
    text: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}


