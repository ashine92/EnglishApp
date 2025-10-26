package com.example.englishapp.domain.model

data class TestResult(
    val id: Long = 0,
    val testType: TestType,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val score: Float,
    val duration: Long,
    val vocabIds: List<Long>,
    val testDate: Long = System.currentTimeMillis()
)

enum class TestType {
    MULTIPLE_CHOICE, FILL_BLANK, MATCHING
}