package com.example.englishapp.data.repository

import com.example.englishapp.data.local.dao.PronunciationProgressDao
import com.example.englishapp.data.local.entity.PronunciationProgressEntity
import com.example.englishapp.data.remote.PronunciationScoringService
import com.example.englishapp.domain.model.PronunciationResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository for pronunciation practice functionality
 */
class PronunciationRepository(
    private val pronunciationService: PronunciationScoringService,
    private val pronunciationProgressDao: PronunciationProgressDao
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

    /**
     * Save pronunciation progress to database
     */
    suspend fun savePronunciationProgress(
        vocabId: Long,
        word: String,
        userText: String,
        score: Int,
        similarity: String
    ) {
        val progress = PronunciationProgressEntity(
            vocabId = vocabId,
            word = word,
            userText = userText,
            score = score,
            similarity = similarity
        )
        pronunciationProgressDao.insertProgress(progress)
    }

    /**
     * Get pronunciation progress for a specific vocabulary
     */
    fun getPronunciationProgress(vocabId: Long): Flow<List<PronunciationProgressEntity>> {
        return pronunciationProgressDao.getProgressByVocabId(vocabId)
    }

    /**
     * Get average score for a vocabulary
     */
    suspend fun getAverageScore(vocabId: Long): Double? {
        return pronunciationProgressDao.getAverageScoreForVocab(vocabId)
    }

    /**
     * Get practice count for a vocabulary
     */
    suspend fun getPracticeCount(vocabId: Long): Int {
        return pronunciationProgressDao.getPracticeCountForVocab(vocabId)
    }

    /**
     * Get all pronunciation progress
     */
    fun getAllPronunciationProgress(): Flow<List<PronunciationProgressEntity>> {
        return pronunciationProgressDao.getAllProgress()
    }
}
