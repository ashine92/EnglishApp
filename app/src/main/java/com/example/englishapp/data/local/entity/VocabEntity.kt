package com.example.englishapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val phonetic: String?,
    val meaning: String,
    val example: String?,
    val partOfSpeech: String?, // noun, verb, adjective...
    val category: String?, // business, technology, daily...
    val difficulty: Int = 1, // 1-5
    val learningStatus: String = "NOT_LEARNED", // NOT_LEARNED, LEARNED
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewDate: Long = 0,
    val createdDate: Long = System.currentTimeMillis()
)