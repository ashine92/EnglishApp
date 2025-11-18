package com.example.englishapp.data.remote.dto

/**
 * Standardized response format for word lookup using Gemini API
 */
data class GeminiWordResponse(
    val word: String,
    val meaning: String,
    val example: String,
    val synonyms: List<String>,
    val phonetic: String
)
