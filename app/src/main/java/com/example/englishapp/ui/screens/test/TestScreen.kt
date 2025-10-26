package com.example.englishapp.ui.screens.test

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishapp.domain.model.TestQuestion
import com.example.englishapp.domain.model.TestType
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    viewModel: TestViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bài Kiểm Tra") },
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
            when (val state = uiState) {
                is TestUiState.Idle -> {
                    TestTypeSelection(
                        onTypeSelected = { type, count ->
                            viewModel.generateTest(type, count)
                        }
                    )
                }
                is TestUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TestUiState.Testing -> {
                    TestContent(
                        question = state.question,
                        currentIndex = state.currentIndex,
                        totalQuestions = state.totalQuestions,
                        onAnswerSubmit = { answer ->
                            viewModel.submitAnswer(answer)
                        },
                        onMatchingSubmit = { matches ->
                            viewModel.submitMatchingAnswers(matches)
                        }
                    )
                }
                is TestUiState.Finished -> {
                    TestResultContent(
                        result = state.result,
                        onRetry = { viewModel.resetTest() },
                        onExit = onNavigateBack
                    )
                }
                is TestUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.resetTest() }) {
                            Text("Quay lại")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestTypeSelection(
    onTypeSelected: (TestType, Int) -> Unit
) {
    var selectedType by remember { mutableStateOf<TestType?>(null) }
    var questionCount by remember { mutableStateOf(10) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Chọn Loại Bài Kiểm Tra",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        TestTypeCard(
            title = "Trắc Nghiệm",
            description = "Chọn nghĩa đúng của từ",
            isSelected = selectedType == TestType.MULTIPLE_CHOICE,
            onClick = { selectedType = TestType.MULTIPLE_CHOICE }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TestTypeCard(
            title = "Điền Từ Còn Thiếu",
            description = "Điền từ vào chỗ trống",
            isSelected = selectedType == TestType.FILL_BLANK,
            onClick = { selectedType = TestType.FILL_BLANK }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TestTypeCard(
            title = "Ghép Từ - Nghĩa",
            description = "Nối từ với nghĩa tương ứng",
            isSelected = selectedType == TestType.MATCHING,
            onClick = { selectedType = TestType.MATCHING }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Số câu hỏi: ")
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { if (questionCount > 5) questionCount-- }
            ) {
                Text("-")
            }

            Text(
                text = "$questionCount",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { if (questionCount < 30) questionCount++ }
            ) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                selectedType?.let { type ->
                    onTypeSelected(type, questionCount)
                }
            },
            enabled = selectedType != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Bắt Đầu", fontSize = 18.sp)
        }
    }
}

@Composable
fun TestTypeCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TestContent(
    question: TestQuestion,
    currentIndex: Int,
    totalQuestions: Int,
    onAnswerSubmit: (String) -> Unit,
    onMatchingSubmit: (Map<String, String>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = (currentIndex + 1).toFloat() / totalQuestions,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Câu ${currentIndex + 1}/$totalQuestions",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (question) {
            is TestQuestion.MultipleChoice -> {
                MultipleChoiceQuestion(
                    question = question,
                    onAnswerSubmit = onAnswerSubmit
                )
            }
            is TestQuestion.FillBlank -> {
                FillBlankQuestion(
                    question = question,
                    onAnswerSubmit = onAnswerSubmit
                )
            }
            is TestQuestion.Matching -> {
                MatchingQuestion(
                    question = question,
                    onSubmit = onMatchingSubmit
                )
            }
        }
    }
}

@Composable
fun MultipleChoiceQuestion(
    question: TestQuestion.MultipleChoice,
    onAnswerSubmit: (String) -> Unit
) {
    var selectedAnswer by remember(question.id) { mutableStateOf<String?>(null) }

    Column {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = question.vocab.word,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                question.vocab.phonetic?.let { phonetic ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = phonetic,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                question.vocab.partOfSpeech?.let { pos ->
                    Text(
                        text = pos,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chọn nghĩa đúng:",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        question.options.forEach { option ->
            OptionCard(
                text = option,
                isSelected = selectedAnswer == option,
                onClick = { selectedAnswer = option }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                selectedAnswer?.let { onAnswerSubmit(it) }
            },
            enabled = selectedAnswer != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Xác Nhận", fontSize = 18.sp)
        }
    }
}

@Composable
fun OptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillBlankQuestion(
    question: TestQuestion.FillBlank,
    onAnswerSubmit: (String) -> Unit
) {
    var userAnswer by remember(question.id) { mutableStateOf("") }

    Column {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Điền từ còn thiếu:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = question.sentence,
                    fontSize = 18.sp,
                    lineHeight = 28.sp
                )

                question.vocab.phonetic?.let { phonetic ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Phát âm: $phonetic",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = userAnswer,
            onValueChange = { userAnswer = it },
            label = { Text("Nhập từ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onAnswerSubmit(userAnswer) },
            enabled = userAnswer.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Xác Nhận", fontSize = 18.sp)
        }
    }
}

@Composable
fun MatchingQuestion(
    question: TestQuestion.Matching,
    onSubmit: (Map<String, String>) -> Unit
) {
    var selectedMatches by remember(question.id) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    var selectedWord by remember { mutableStateOf<String?>(null) }

    val words = question.pairs.map { it.first }.shuffled()
    val meanings = question.pairs.map { it.second }.shuffled()

    Column {
        Text(
            text = "Ghép từ với nghĩa tương ứng:",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Words column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                words.forEach { word ->
                    MatchingItem(
                        text = word,
                        isSelected = selectedWord == word,
                        isMatched = selectedMatches.containsKey(word),
                        onClick = {
                            if (!selectedMatches.containsKey(word)) {
                                selectedWord = word
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Meanings column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                meanings.forEach { meaning ->
                    val isMatched = selectedMatches.containsValue(meaning)
                    MatchingItem(
                        text = meaning,
                        isSelected = false,
                        isMatched = isMatched,
                        onClick = {
                            selectedWord?.let { word ->
                                if (!isMatched) {
                                    selectedMatches = selectedMatches + (word to meaning)
                                    selectedWord = null
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (selectedMatches.size == question.pairs.size) {
                    onSubmit(selectedMatches)
                }
            },
            enabled = selectedMatches.size == question.pairs.size,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Hoàn Thành (${selectedMatches.size}/${question.pairs.size})", fontSize = 18.sp)
        }
    }
}

@Composable
fun MatchingItem(
    text: String,
    isSelected: Boolean,
    isMatched: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = !isMatched)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = when {
                    isMatched -> Color.Green
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color.Gray
                },
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isMatched -> Color.Green.copy(alpha = 0.2f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            fontSize = 14.sp
        )
    }
}