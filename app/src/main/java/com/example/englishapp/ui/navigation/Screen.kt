package com.example.englishapp.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object VocabList : Screen("vocab_list")
    object VocabDetail : Screen("vocab_detail/{vocabId}") {
        fun createRoute(vocabId: Long) = "vocab_detail/$vocabId"
    }
    object Test : Screen("test")
}