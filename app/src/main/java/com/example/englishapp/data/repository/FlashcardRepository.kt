package com.example.englishapp.data.repository

import com.example.englishapp.data.local.dao.FlashcardDao
import com.example.englishapp.data.local.entity.*
import com.example.englishapp.domain.model.*
import com.example.englishapp.util.SpacedRepetitionAlgorithm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlashcardRepository(private val flashcardDao: FlashcardDao) {

    /**
     * Get all flashcard decks
     */
    fun getAllDecks(): Flow<List<FlashcardDeck>> {
        return flashcardDao.getAllDecks().map { entities ->
            entities.map { entity ->
                FlashcardDeck(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    createdDate = entity.createdDate,
                    cardCount = entity.cardCount
                )
            }
        }
    }

    /**
     * Get deck with statistics (new, learning, review cards)
     */
    suspend fun getDeckWithStats(deckId: Long): FlashcardDeck? {
        val deck = flashcardDao.getDeckById(deckId) ?: return null
        val today = System.currentTimeMillis()

        return FlashcardDeck(
            id = deck.id,
            name = deck.name,
            description = deck.description,
            createdDate = deck.createdDate,
            cardCount = deck.cardCount,
            newCards = flashcardDao.getCardCountByStatus(deckId, "NEW"),
            learningCards = flashcardDao.getCardCountByStatus(deckId, "LEARNING"),
            reviewCards = flashcardDao.getCardCountByStatus(deckId, "REVIEW"),
            dueToday = flashcardDao.getDueCardCount(deckId, today)
        )
    }

    /**
     * Create a new deck
     */
    suspend fun createDeck(name: String, description: String?): Long {
        val deck = FlashcardDeckEntity(
            name = name,
            description = description
        )
        return flashcardDao.insertDeck(deck)
    }

    /**
     * Update deck
     */
    suspend fun updateDeck(deck: FlashcardDeck) {
        flashcardDao.updateDeck(
            FlashcardDeckEntity(
                id = deck.id,
                name = deck.name,
                description = deck.description,
                createdDate = deck.createdDate,
                cardCount = deck.cardCount
            )
        )
    }

    /**
     * Delete deck (will cascade delete all cards)
     */
    suspend fun deleteDeck(deckId: Long) {
        val deck = flashcardDao.getDeckById(deckId) ?: return
        flashcardDao.deleteDeck(deck)
    }

    /**
     * Get all cards in a deck
     */
    fun getCardsByDeck(deckId: Long): Flow<List<Flashcard>> {
        return flashcardDao.getCardsByDeck(deckId).map { entities ->
            entities.map { entity ->
                val progress = flashcardDao.getProgress(entity.id)
                Flashcard(
                    id = entity.id,
                    deckId = entity.deckId,
                    vocabId = entity.vocabId,
                    frontContent = entity.frontContent,
                    backContent = entity.backContent,
                    example = entity.example,
                    phonetic = entity.phonetic,
                    createdDate = entity.createdDate,
                    progress = progress?.toDomain()
                )
            }
        }
    }

    /**
     * Add a new card to deck
     */
    suspend fun addCardToDeck(
        deckId: Long,
        vocabId: Long? = null,
        frontContent: String,
        backContent: String,
        example: String? = null,
        phonetic: String? = null
    ): Long {
        val card = FlashcardEntity(
            deckId = deckId,
            vocabId = vocabId,
            frontContent = frontContent,
            backContent = backContent,
            example = example,
            phonetic = phonetic
        )
        val cardId = flashcardDao.insertCard(card)

        // Initialize progress for new card
        val progress = SpacedRepetitionAlgorithm.initializeProgress(cardId)
        flashcardDao.insertProgress(progress.toEntity())

        // Update deck card count
        val count = flashcardDao.getCardCount(deckId)
        flashcardDao.updateCardCount(deckId, count)

        return cardId
    }

    /**
     * Update existing card
     */
    suspend fun updateCard(card: Flashcard) {
        flashcardDao.updateCard(
            FlashcardEntity(
                id = card.id,
                deckId = card.deckId,
                vocabId = card.vocabId,
                frontContent = card.frontContent,
                backContent = card.backContent,
                example = card.example,
                phonetic = card.phonetic,
                createdDate = card.createdDate
            )
        )
    }

    /**
     * Delete card
     */
    suspend fun deleteCard(cardId: Long) {
        val card = flashcardDao.getCardById(cardId) ?: return
        val deckId = card.deckId

        flashcardDao.deleteCard(card)

        // Update deck card count
        val count = flashcardDao.getCardCount(deckId)
        flashcardDao.updateCardCount(deckId, count)
    }

    /**
     * Get cards that are due for review today
     */
    suspend fun getDueCards(deckId: Long): List<Flashcard> {
        val today = System.currentTimeMillis()
        val cardEntities = flashcardDao.getDueCards(deckId, today)

        return cardEntities.map { card ->
            val progress = flashcardDao.getProgress(card.id)
            Flashcard(
                id = card.id,
                deckId = card.deckId,
                vocabId = card.vocabId,
                frontContent = card.frontContent,
                backContent = card.backContent,
                example = card.example,
                phonetic = card.phonetic,
                createdDate = card.createdDate,
                progress = progress?.toDomain()
            )
        }
    }

    /**
     * Submit user rating for a card and update its review schedule
     */
    suspend fun submitRating(cardId: Long, rating: Rating) {
        val currentProgress = flashcardDao.getProgress(cardId)?.toDomain()
            ?: SpacedRepetitionAlgorithm.initializeProgress(cardId)

        val newProgress = SpacedRepetitionAlgorithm.calculateNextReview(currentProgress, rating)
        flashcardDao.updateProgress(newProgress.toEntity())
    }

    /**
     * Get statistics for a deck
     */
    suspend fun getDeckStatistics(deckId: Long): DeckStatistics {
        val today = System.currentTimeMillis()

        return DeckStatistics(
            totalCards = flashcardDao.getCardCount(deckId),
            newCards = flashcardDao.getCardCountByStatus(deckId, "NEW"),
            learningCards = flashcardDao.getCardCountByStatus(deckId, "LEARNING"),
            reviewCards = flashcardDao.getCardCountByStatus(deckId, "REVIEW"),
            dueToday = flashcardDao.getDueCardCount(deckId, today)
        )
    }

    // Mapping functions
    private fun FlashcardProgressEntity.toDomain() = FlashcardProgress(
        cardId = cardId,
        easinessFactor = easinessFactor,
        interval = interval,
        repetitions = repetitions,
        nextReviewDate = nextReviewDate,
        lastReviewDate = lastReviewDate,
        cardStatus = CardStatus.valueOf(cardStatus)
    )

    private fun FlashcardProgress.toEntity() = FlashcardProgressEntity(
        cardId = cardId,
        easinessFactor = easinessFactor,
        interval = interval,
        repetitions = repetitions,
        nextReviewDate = nextReviewDate,
        lastReviewDate = lastReviewDate,
        cardStatus = cardStatus.name
    )
}

data class DeckStatistics(
    val totalCards: Int,
    val newCards: Int,
    val learningCards: Int,
    val reviewCards: Int,
    val dueToday: Int
)