package com.example.englishapp.util

object Constants {
    const val DATABASE_NAME = "vocab_database"
    const val DICTIONARY_API_BASE_URL = "https://api.dictionaryapi.dev/api/v2/"
    
    // Gemini API - API key should be provided by user or stored securely
    // For development, you can set this in local.properties or environment variable
    const val GEMINI_API_KEY = "AIzaSyBcYRHwip-CmFIBWjf-lQB4Lt55LMgs81Q" // Replace with actual key or use BuildConfig

    // Test settings
    const val DEFAULT_QUESTION_COUNT = 10
    const val MIN_QUESTION_COUNT = 5
    const val MAX_QUESTION_COUNT = 30

    // Learning thresholds
    const val MASTERY_THRESHOLD = 5 // correct answers to mark as mastered
    const val REVIEW_INTERVAL_DAYS = 7
}