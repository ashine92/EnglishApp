package com.example.englishapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testType: String, // MULTIPLE_CHOICE, FILL_BLANK, MATCHING
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val score: Float,
    val duration: Long, // milliseconds
    val vocabIds: String, // comma-separated IDs
    val testDate: Long = System.currentTimeMillis()
)