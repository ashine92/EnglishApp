package com.example.englishapp.domain.model

data class Flashcard(
    val id: Long = 0,
    val deckId: Long,
    val vocabId: Long?,
    val frontContent: String,
    val backContent: String,
    val example: String?,
    val phonetic: String?,
    val createdDate: Long,
    val progress: FlashcardProgress? = null
)

data class FlashcardProgress(
    val cardId: Long,
    val easinessFactor: Float = 2.5f,
    val interval: Int = 0,
    val repetitions: Int = 0,
    val nextReviewDate: Long,
    val lastReviewDate: Long? = null,
    val cardStatus: CardStatus = CardStatus.NEW
)

enum class CardStatus {
    NEW, LEARNING, REVIEW
}

enum class Rating {
    AGAIN,  // 0 - Complete blackout, forgot completely
    HARD,   // 2 - Incorrect response, but correct one remembered
    GOOD,   // 3 - Correct response, with some difficulty
    EASY    // 4 - Perfect response
}