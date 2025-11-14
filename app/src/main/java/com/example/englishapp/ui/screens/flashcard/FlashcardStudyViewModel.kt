package com.example.englishapp.ui.screens.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishapp.data.repository.FlashcardRepository
import com.example.englishapp.domain.model.Flashcard
import com.example.englishapp.domain.model.Rating
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FlashcardStudyViewModel(
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    private val _studyState = MutableStateFlow<StudyState>(StudyState.Loading)
    val studyState: StateFlow<StudyState> = _studyState.asStateFlow()

    private val _sessionStats = MutableStateFlow(SessionStats())
    val sessionStats: StateFlow<SessionStats> = _sessionStats.asStateFlow()

    private var allCards: List<Flashcard> = emptyList()
    private var currentCardIndex = 0
    private val startTime = System.currentTimeMillis()

    fun loadDueCards(deckId: Long) {
        viewModelScope.launch {
            try {
                allCards = flashcardRepository.getDueCards(deckId)

                if (allCards.isEmpty()) {
                    _studyState.value = StudyState.NoCards
                } else {
                    currentCardIndex = 0
                    _studyState.value = StudyState.Studying(
                        card = allCards[currentCardIndex],
                        currentIndex = currentCardIndex,
                        totalCards = allCards.size
                    )
                }
            } catch (e: Exception) {
                _studyState.value = StudyState.Error(e.message ?: "Failed to load cards")
            }
        }
    }

    fun submitRating(rating: Rating) {
        viewModelScope.launch {
            val currentCard = (studyState.value as? StudyState.Studying)?.card ?: return@launch

            // Update statistics
            val stats = _sessionStats.value
            _sessionStats.value = when (rating) {
                Rating.AGAIN -> stats.copy(
                    again = stats.again + 1,
                    cardsStudied = stats.cardsStudied + 1
                )
                Rating.HARD -> stats.copy(
                    hard = stats.hard + 1,
                    cardsStudied = stats.cardsStudied + 1
                )
                Rating.GOOD -> stats.copy(
                    good = stats.good + 1,
                    cardsStudied = stats.cardsStudied + 1
                )
                Rating.EASY -> stats.copy(
                    easy = stats.easy + 1,
                    cardsStudied = stats.cardsStudied + 1
                )
            }

            // Submit rating to repository (updates spaced repetition)
            flashcardRepository.submitRating(currentCard.id, rating)

            // Move to next card
            moveToNextCard()
        }
    }

    private fun moveToNextCard() {
        currentCardIndex++

        if (currentCardIndex >= allCards.size) {
            // Session finished
            val duration = System.currentTimeMillis() - startTime
            _studyState.value = StudyState.Finished(
                stats = _sessionStats.value,
                duration = duration
            )
        } else {
            // Show next card
            _studyState.value = StudyState.Studying(
                card = allCards[currentCardIndex],
                currentIndex = currentCardIndex,
                totalCards = allCards.size
            )
        }
    }

    fun resetSession() {
        _studyState.value = StudyState.Loading
        _sessionStats.value = SessionStats()
        currentCardIndex = 0
    }
}

sealed class StudyState {
    object Loading : StudyState()
    object NoCards : StudyState()
    data class Studying(
        val card: Flashcard,
        val currentIndex: Int,
        val totalCards: Int
    ) : StudyState()
    data class Finished(
        val stats: SessionStats,
        val duration: Long
    ) : StudyState()
    data class Error(val message: String) : StudyState()
}

data class SessionStats(
    val cardsStudied: Int = 0,
    val again: Int = 0,
    val hard: Int = 0,
    val good: Int = 0,
    val easy: Int = 0
) {
    val totalReviews: Int = again + hard + good + easy
    val accuracy: Float = if (totalReviews > 0) {
        ((good + easy).toFloat() / totalReviews) * 100
    } else 0f
}