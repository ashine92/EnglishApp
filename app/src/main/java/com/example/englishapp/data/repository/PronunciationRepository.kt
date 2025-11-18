package com.example.englishapp.data.repository

import com.example.englishapp.data.remote.PronunciationScoringService
import com.example.englishapp.domain.model.PronunciationResult

/**
 * Repository for pronunciation practice functionality
 */
class PronunciationRepository(
    private val pronunciationService: PronunciationScoringService
) {
    /**
     * Score user's pronunciation
     */
    suspend fun scorePronunciation(
        expectedText: String,
        userText: String
    ): Result<PronunciationResult> {
        return try {
            val result = pronunciationService.scorePronunciation(expectedText, userText)
            result.map { response ->
                PronunciationResult(
                    score = response.score,
                    similarity = response.similarity,
                    mistakes = response.mistakes,
                    feedback = response.feedback,
                    expectedText = expectedText,
                    userText = userText
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
