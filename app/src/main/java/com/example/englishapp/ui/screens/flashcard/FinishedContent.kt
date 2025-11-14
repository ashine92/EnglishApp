package com.example.englishapp.ui.screens.flashcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun FinishedContent(
    stats: SessionStats,
    duration: Long,
    onFinish: () -> Unit,
    onRestart: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text("🎉", fontSize = 64.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Session Complete!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Great work! Keep it up!",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Accuracy circle
        Card(
            modifier = Modifier.size(180.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    stats.accuracy >= 80 -> Color(0xFF4CAF50)
                    stats.accuracy >= 50 -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${stats.accuracy.roundToInt()}%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Accuracy",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Statistics
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Session Statistics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                StatRow("Total Cards", stats.cardsStudied.toString())
                Spacer(modifier = Modifier.height(8.dp))
                StatRow("Duration", formatDuration(duration))

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Rating Breakdown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                RatingRow("Again", stats.again, Color(0xFFF44336))
                Spacer(modifier = Modifier.height(8.dp))
                RatingRow("Hard", stats.hard, Color(0xFFFF9800))
                Spacer(modifier = Modifier.height(8.dp))
                RatingRow("Good", stats.good, Color(0xFF4CAF50))
                Spacer(modifier = Modifier.height(8.dp))
                RatingRow("Easy", stats.easy, Color(0xFF2196F3))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action buttons
        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Study More", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Back to Deck", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RatingRow(
    label: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(12.dp),
                color = color,
                shape = MaterialTheme.shapes.small
            ) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 16.sp
            )
        }
        Text(
            text = count.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))

    return when {
        hours > 0 -> String.format("%dh %02dm %02ds", hours, minutes, seconds)
        minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
        else -> String.format("%ds", seconds)
    }
}