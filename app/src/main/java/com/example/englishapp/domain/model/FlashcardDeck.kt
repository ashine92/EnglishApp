package com.example.englishapp.domain.model

data class FlashcardDeck(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val createdDate: Long,
    val cardCount: Int = 0,
    val newCards: Int = 0,
    val learningCards: Int = 0,
    val reviewCards: Int = 0,
    val dueToday: Int = 0
)