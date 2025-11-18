package com.example.englishapp.domain.model

/**
 * Domain model for pronunciation practice result
 */
data class PronunciationResult(
    val score: Int,                    // Score from 0-100
    val similarity: String,            // Similarity level (High/Medium/Low)
    val mistakes: List<String>,        // List of pronunciation errors
    val feedback: String,              // Improvement suggestions
    val expectedText: String,          // The sentence that was supposed to be pronounced
    val userText: String,              // What the user actually said (from speech recognition)
    val timestamp: Long = System.currentTimeMillis()
)
