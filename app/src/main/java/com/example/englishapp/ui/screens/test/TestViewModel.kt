package com.example.englishapp.ui.screens.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishapp.data.repository.TestRepository
import com.example.englishapp.data.repository.VocabRepository
import com.example.englishapp.domain.model.TestQuestion
import com.example.englishapp.domain.model.TestResult
import com.example.englishapp.domain.model.TestType
import com.example.englishapp.util.TestGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TestViewModel(
    private val vocabRepository: VocabRepository,
    private val testRepository: TestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TestUiState>(TestUiState.Idle)
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    private var currentQuestions = listOf<TestQuestion>()
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var startTime = 0L

    fun generateTest(testType: TestType, questionCount: Int = 10) {
        viewModelScope.launch {
            _uiState.value = TestUiState.Loading

            try {
                val vocabs = vocabRepository.getRandomVocabs(questionCount * 2)

                if (vocabs.isEmpty()) {
                    _uiState.value = TestUiState.Error("Không có từ vựng nào để tạo bài kiểm tra")
                    return@launch
                }

                currentQuestions = when (testType) {
                    TestType.MULTIPLE_CHOICE -> TestGenerator.generateMultipleChoiceQuestions(
                        vocabs,
                        questionCount
                    )
                    TestType.FILL_BLANK -> TestGenerator.generateFillBlankQuestions(
                        vocabs,
                        questionCount
                    )
                    TestType.MATCHING -> TestGenerator.generateMatchingQuestions(
                        vocabs,
                        questionCount
                    )
                }

                currentQuestionIndex = 0
                correctAnswers = 0
                startTime = System.currentTimeMillis()

                _uiState.value = TestUiState.Testing(
                    question = currentQuestions[0],
                    currentIndex = 0,
                    totalQuestions = currentQuestions.size,
                    testType = testType
                )
            } catch (e: Exception) {
                _uiState.value = TestUiState.Error(e.message ?: "Lỗi tạo bài kiểm tra")
            }
        }
    }

    fun submitAnswer(userAnswer: String) {
        val currentQuestion = currentQuestions[currentQuestionIndex]
        val isCorrect = when (currentQuestion) {
            is TestQuestion.MultipleChoice -> userAnswer == currentQuestion.correctAnswer
            is TestQuestion.FillBlank -> userAnswer.equals(
                currentQuestion.vocab.word,
                ignoreCase = true
            )
            is TestQuestion.Matching -> false // Handle separately
        }

        if (isCorrect) {
            correctAnswers++
        }

        viewModelScope.launch {
            vocabRepository.updateVocabReview(currentQuestion.vocab.id, isCorrect)
        }

        moveToNextQuestion()
    }

    fun submitMatchingAnswers(matches: Map<String, String>) {
        val currentQuestion = currentQuestions[currentQuestionIndex] as TestQuestion.Matching
        var correctMatches = 0

        matches.forEach { (word, meaning) ->
            if (currentQuestion.pairs.any { it.first == word && it.second == meaning }) {
                correctMatches++
            }
        }

        if (correctMatches == currentQuestion.pairs.size) {
            correctAnswers++
        }

        moveToNextQuestion()
    }

    private fun moveToNextQuestion() {
        currentQuestionIndex++

        if (currentQuestionIndex >= currentQuestions.size) {
            finishTest()
        } else {
            val state = _uiState.value as TestUiState.Testing
            _uiState.value = state.copy(
                question = currentQuestions[currentQuestionIndex],
                currentIndex = currentQuestionIndex
            )
        }
    }

    private fun finishTest() {
        viewModelScope.launch {
            val duration = System.currentTimeMillis() - startTime
            val score = (correctAnswers.toFloat() / currentQuestions.size) * 100

            val state = _uiState.value as TestUiState.Testing
            val result = TestResult(
                testType = state.testType,
                totalQuestions = currentQuestions.size,
                correctAnswers = correctAnswers,
                wrongAnswers = currentQuestions.size - correctAnswers,
                score = score,
                duration = duration,
                vocabIds = currentQuestions.map { it.vocab.id }
            )

            testRepository.insertTestResult(result)

            _uiState.value = TestUiState.Finished(result)
        }
    }

    fun resetTest() {
        _uiState.value = TestUiState.Idle
        currentQuestions = emptyList()
        currentQuestionIndex = 0
        correctAnswers = 0
        startTime = 0L
    }
}

sealed class TestUiState {
    object Idle : TestUiState()
    object Loading : TestUiState()
    data class Testing(
        val question: TestQuestion,
        val currentIndex: Int,
        val totalQuestions: Int,
        val testType: TestType
    ) : TestUiState()
    data class Finished(val result: TestResult) : TestUiState()
    data class Error(val message: String) : TestUiState()
}