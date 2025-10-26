package com.example.englishapp.domain.model

data class Vocabulary(
    val id: Long = 0,
    val word: String,
    val phonetic: String? = null,
    val meaning: String,
    val example: String? = null,
    val partOfSpeech: String? = null,
    val category: String? = null,
    val difficulty: Int = 1,
    val learningStatus: LearningStatus = LearningStatus.NEW,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewDate: Long = 0,
    val createdDate: Long = System.currentTimeMillis()
)

enum class LearningStatus {
    NEW,
    LEARNING,
    MASTERED
}