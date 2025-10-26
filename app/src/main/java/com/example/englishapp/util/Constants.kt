package com.example.englishapp.util

object Constants {
    const val DATABASE_NAME = "vocab_database"
    const val DICTIONARY_API_BASE_URL = "https://api.dictionaryapi.dev/api/v2/"

    // Test settings
    const val DEFAULT_QUESTION_COUNT = 10
    const val MIN_QUESTION_COUNT = 5
    const val MAX_QUESTION_COUNT = 30

    // Learning thresholds
    const val MASTERY_THRESHOLD = 5 // correct answers to mark as mastered
    const val REVIEW_INTERVAL_DAYS = 7
}