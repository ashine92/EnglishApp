package com.example.englishapp.data.local.dao

import androidx.room.*
import com.example.englishapp.data.local.entity.FlashcardDeckEntity
import com.example.englishapp.data.local.entity.FlashcardEntity
import com.example.englishapp.data.local.entity.FlashcardProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    // Deck operations
    @Query("SELECT * FROM flashcard_decks ORDER BY createdDate DESC")
    fun getAllDecks(): Flow<List<FlashcardDeckEntity>>

    @Query("SELECT * FROM flashcard_decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Long): FlashcardDeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeckEntity): Long

    @Update
    suspend fun updateDeck(deck: FlashcardDeckEntity)

    @Delete
    suspend fun deleteDeck(deck: FlashcardDeckEntity)

    @Query("UPDATE flashcard_decks SET cardCount = :count WHERE id = :deckId")
    suspend fun updateCardCount(deckId: Long, count: Int)

    // Card operations
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY createdDate DESC")
    fun getCardsByDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE id = :cardId")
    suspend fun getCardById(cardId: Long): FlashcardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardEntity): Long

    @Update
    suspend fun updateCard(card: FlashcardEntity)

    @Delete
    suspend fun deleteCard(card: FlashcardEntity)

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId")
    suspend fun getCardCount(deckId: Long): Int

    // Progress operations
    @Query("SELECT * FROM flashcard_progress WHERE cardId = :cardId")
    suspend fun getProgress(cardId: Long): FlashcardProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: FlashcardProgressEntity)

    @Update
    suspend fun updateProgress(progress: FlashcardProgressEntity)

    // Study queries
    @Query("""
        SELECT f.* FROM flashcards f
        JOIN flashcard_progress p ON f.id = p.cardId
        WHERE f.deckId = :deckId AND p.nextReviewDate <= :today
        ORDER BY p.nextReviewDate ASC
    """)
    suspend fun getDueCards(deckId: Long, today: Long): List<FlashcardEntity>

    @Query("""
        SELECT COUNT(*) FROM flashcards f
        JOIN flashcard_progress p ON f.id = p.cardId
        WHERE f.deckId = :deckId AND p.cardStatus = :status
    """)
    suspend fun getCardCountByStatus(deckId: Long, status: String): Int

    @Query("""
        SELECT COUNT(*) FROM flashcards f
        JOIN flashcard_progress p ON f.id = p.cardId
        WHERE f.deckId = :deckId AND p.nextReviewDate <= :today
    """)
    suspend fun getDueCardCount(deckId: Long, today: Long): Int
}