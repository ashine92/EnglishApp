package com.example.englishapp.ui.screens.home

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
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToSearch: () -> Unit,
    onNavigateToVocabList: () -> Unit,
    onNavigateToTest: () -> Unit,
    onNavigateToPronunciation: () -> Unit = {}
) {
    val statistics by viewModel.statistics.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Vocab App",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Học từ vựng thông minh",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick actions
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Hành Động Nhanh",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onNavigateToSearch,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📖 Tra từ mới")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onNavigateToTest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✏️ Làm bài kiểm tra")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onNavigateToPronunciation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎤 Luyện phát âm")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onNavigateToVocabList,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📚 Xem danh sách từ")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Statistics
        Text(
            text = "Thống Kê",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Tổng từ",
                value = statistics.totalVocabs.toString(),
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Đã học",
                value = statistics.learnedVocabs.toString(),
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Chưa học",
                value = statistics.notLearnedVocabs.toString(),
                color = Color(0xFFFFC107),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Điểm TB",
                value = "${statistics.averageScore.toInt()}%",
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Learning progress
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Tiến Độ Học",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (statistics.totalVocabs > 0) {
                    val learnedProgress = statistics.learnedVocabs.toFloat() / statistics.totalVocabs

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Đã học")
                            Text("${(learnedProgress * 100).toInt()}%")
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = learnedProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color(0xFF4CAF50)
                        )
                    }
                } else {
                    Text(
                        text = "Chưa có dữ liệu",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}