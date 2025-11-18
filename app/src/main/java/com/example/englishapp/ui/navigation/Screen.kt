package com.example.englishapp.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object VocabList : Screen("vocab_list")
    object VocabDetail : Screen("vocab_detail/{vocabId}") {
        fun createRoute(vocabId: Long) = "vocab_detail/$vocabId"
    }
    object Test : Screen("test")
    object Pronunciation : Screen("pronunciation")

    // Flashcard routes
    object FlashcardDeckList : Screen("flashcard_deck_list")
    object FlashcardDeckDetail : Screen("flashcard_deck_detail/{deckId}") {
        fun createRoute(deckId: Long) = "flashcard_deck_detail/$deckId"
    }
    object AddCard : Screen("add_card/{deckId}") {
        fun createRoute(deckId: Long) = "add_card/$deckId"
    }
    object FlashcardStudy : Screen("flashcard_study/{deckId}") {
        fun createRoute(deckId: Long) = "flashcard_study/$deckId"
    }
}