package com.example.englishapp.util

import com.example.englishapp.domain.model.CardStatus
import com.example.englishapp.domain.model.FlashcardProgress
import com.example.englishapp.domain.model.Rating
import java.util.concurrent.TimeUnit

/**
 * SuperMemo 2 (SM-2) Spaced Repetition Algorithm
 *
 * This algorithm calculates the optimal time to review flashcards
 * based on user's performance (rating).
 */
object SpacedRepetitionAlgorithm {

    /**
     * Calculate next review schedule based on user rating
     *
     * @param currentProgress Current progress of the card
     * @param rating User's rating (AGAIN, HARD, GOOD, EASY)
     * @return Updated FlashcardProgress with new schedule
     */
    fun calculateNextReview(
        currentProgress: FlashcardProgress,
        rating: Rating
    ): FlashcardProgress {
        // Convert rating to quality (0-5 scale)
        val quality = when (rating) {
            Rating.AGAIN -> 0
            Rating.HARD -> 2
            Rating.GOOD -> 3
            Rating.EASY -> 4
        }

        // Calculate new Easiness Factor (EF)
        // EF determines how quickly intervals increase
        var newEF = currentProgress.easinessFactor +
                (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))

        // EF should not be less than 1.3
        if (newEF < 1.3f) newEF = 1.3f

        // Calculate new interval and repetitions
        val (newInterval, newReps) = when {
            quality < 3 -> {
                // Failed - restart from beginning
                Pair(1, 0)
            }
            currentProgress.repetitions == 0 -> {
                // First successful review - 1 day
                Pair(1, 1)
            }
            currentProgress.repetitions == 1 -> {
                // Second successful review - 6 days
                Pair(6, 2)
            }
            else -> {
                // Subsequent reviews - multiply by EF
                val interval = (currentProgress.interval * newEF).toInt()
                Pair(interval, currentProgress.repetitions + 1)
            }
        }

        // Determine card status
        val newStatus = when {
            newReps == 0 -> CardStatus.LEARNING
            newReps < 3 -> CardStatus.LEARNING
            else -> CardStatus.REVIEW
        }

        // Calculate next review date
        val now = System.currentTimeMillis()
        val nextReview = now + TimeUnit.DAYS.toMillis(newInterval.toLong())

        return FlashcardProgress(
            cardId = currentProgress.cardId,
            easinessFactor = newEF,
            interval = newInterval,
            repetitions = newReps,
            nextReviewDate = nextReview,
            lastReviewDate = now,
            cardStatus = newStatus
        )
    }

    /**
     * Initialize progress for a new card
     */
    fun initializeProgress(cardId: Long): FlashcardProgress {
        return FlashcardProgress(
            cardId = cardId,
            easinessFactor = 2.5f,
            interval = 0,
            repetitions = 0,
            nextReviewDate = System.currentTimeMillis(),
            lastReviewDate = null,
            cardStatus = CardStatus.NEW
        )
    }
}