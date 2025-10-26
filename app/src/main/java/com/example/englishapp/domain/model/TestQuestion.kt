package com.example.englishapp.domain.model

sealed class TestQuestion {
    abstract val id: String
    abstract val vocab: Vocabulary

    data class MultipleChoice(
        override val id: String,
        override val vocab: Vocabulary,
        val options: List<String>,
        val correctAnswer: String
    ) : TestQuestion()

    data class FillBlank(
        override val id: String,
        override val vocab: Vocabulary,
        val sentence: String,
        val blankPosition: Int
    ) : TestQuestion()

    data class Matching(
        override val id: String,
        override val vocab: Vocabulary,
        val pairs: List<Pair<String, String>>
    ) : TestQuestion()
}