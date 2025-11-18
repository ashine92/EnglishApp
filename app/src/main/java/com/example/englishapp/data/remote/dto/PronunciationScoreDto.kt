package com.example.englishapp.data.remote.dto

/**
 * Request for pronunciation scoring
 */
data class PronunciationScoreRequest(
    val expectedText: String,  // Standard sentence for pronunciation
    val userText: String        // Text recognized from user's speech
)

/**
 * Response from pronunciation scoring API
 */
data class PronunciationScoreResponse(
    val score: Int,                    // Score from 0-100
    val similarity: String,            // Similarity level description
    val mistakes: List<String>,        // List of pronunciation errors
    val feedback: String               // Improvement suggestions
)
