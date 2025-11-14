package com.example.englishapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = FlashcardDeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VocabEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocabId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("deckId"), Index("vocabId")]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long,
    val vocabId: Long?,
    val frontContent: String,
    val backContent: String,
    val example: String?,
    val phonetic: String?,
    val createdDate: Long = System.currentTimeMillis()
)