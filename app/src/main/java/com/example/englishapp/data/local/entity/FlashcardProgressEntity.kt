package com.example.englishapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcard_progress")
data class FlashcardProgressEntity(
    @PrimaryKey val cardId: Long,
    val easinessFactor: Float = 2.5f,
    val interval: Int = 0,
    val repetitions: Int = 0,
    val nextReviewDate: Long,
    val lastReviewDate: Long? = null,
    val cardStatus: String = "NEW" // NEW, LEARNING, REVIEW
)