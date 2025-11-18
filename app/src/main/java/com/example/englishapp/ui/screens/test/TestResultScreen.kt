package com.example.englishapp.ui.screens.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.domain.model.TestResult
import kotlin.math.roundToInt

@Composable
fun TestResultContent(
    result: TestResult,
    onRetry: () -> Unit,
    onExit: () -> Unit,
    wrongVocabs: List<com.example.englishapp.domain.model.Vocabulary> = emptyList()
) {
    val scrollState = rememberScrollState()
    var showWrongAnswers by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kết Quả",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Score circle
        Card(
            modifier = Modifier.size(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    result.score >= 80 -> Color(0xFF4CAF50)
                    result.score >= 50 -> Color(0xFFFFC107)
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
                        text = "${result.score.roundToInt()}%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = getScoreLabel(result.score),
                        fontSize = 20.sp,
                        color = Color.White
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
                ResultRow("Tổng số câu:", "${result.totalQuestions}")
                Spacer(modifier = Modifier.height(8.dp))
                ResultRow("Trả lời đúng:", "${result.correctAnswers}", Color(0xFF4CAF50))
                Spacer(modifier = Modifier.height(8.dp))
                ResultRow("Trả lời sai:", "${result.wrongAnswers}", Color(0xFFF44336))
                Spacer(modifier = Modifier.height(8.dp))
                ResultRow("Thời gian:", formatDuration(result.duration))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show wrong answers button if there are any
        if (result.wrongAnswers > 0 && wrongVocabs.isNotEmpty()) {
            OutlinedButton(
                onClick = { showWrongAnswers = !showWrongAnswers },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (showWrongAnswers) "Ẩn câu trả lời sai" else "Xem câu trả lời sai (${result.wrongAnswers})"
                )
            }

            if (showWrongAnswers) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Các từ trả lời sai:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        wrongVocabs.forEach { vocab ->
                            WrongAnswerItem(vocab)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action buttons
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Làm Lại", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Về Trang Chủ", fontSize = 18.sp)
        }
    }
}

@Composable
fun ResultRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

fun getScoreLabel(score: Float): String {
    return when {
        score >= 90 -> "Xuất sắc!"
        score >= 80 -> "Giỏi!"
        score >= 70 -> "Khá!"
        score >= 50 -> "Trung bình"
        else -> "Cần cố gắng"
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun WrongAnswerItem(vocab: com.example.englishapp.domain.model.Vocabulary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = vocab.word,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            vocab.phonetic?.let { phonetic ->
                Text(
                    text = phonetic,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = vocab.meaning,
                fontSize = 14.sp,
                color = Color(0xFF424242)
            )
            vocab.example?.let { example ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"$example\"",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}