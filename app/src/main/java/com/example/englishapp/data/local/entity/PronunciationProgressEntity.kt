package com.example.englishapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pronunciation_progress")
data class PronunciationProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vocabId: Long,
    val word: String,
    val userText: String,
    val score: Int,
    val similarity: String,
    val timestamp: Long = System.currentTimeMillis()
)
