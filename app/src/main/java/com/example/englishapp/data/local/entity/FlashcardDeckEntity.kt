package com.example.englishapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcard_decks")
data class FlashcardDeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String?,
    val createdDate: Long = System.currentTimeMillis(),
    val cardCount: Int = 0
)