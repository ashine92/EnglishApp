package com.example.englishapp.util

import com.example.englishapp.domain.model.TestQuestion
import com.example.englishapp.domain.model.Vocabulary
import kotlin.random.Random

object TestGenerator {

    fun generateMultipleChoiceQuestions(
        vocabs: List<Vocabulary>,
        count: Int = 10
    ): List<TestQuestion.MultipleChoice> {
        val selectedVocabs = vocabs.shuffled().take(count)

        return selectedVocabs.mapIndexed { index, vocab ->
            val wrongOptions = vocabs
                .filter { it.id != vocab.id }
                .shuffled()
                .take(3)
                .map { it.meaning }

            val allOptions = (wrongOptions + vocab.meaning).shuffled()

            TestQuestion.MultipleChoice(
                id = "mc_$index",
                vocab = vocab,
                options = allOptions,
                correctAnswer = vocab.meaning
            )
        }
    }

    fun generateFillBlankQuestions(
        vocabs: List<Vocabulary>,
        count: Int = 10
    ): List<TestQuestion.FillBlank> {
        val selectedVocabs = vocabs
            .filter { !it.example.isNullOrBlank() }
            .shuffled()
            .take(count)

        return selectedVocabs.mapIndexed { index, vocab ->
            val example = vocab.example ?: ""
            val wordPosition = example.lowercase().indexOf(vocab.word.lowercase())

            val sentence = if (wordPosition >= 0) {
                example.replaceRange(
                    wordPosition,
                    wordPosition + vocab.word.length,
                    "_____"
                )
            } else {
                "Điền từ phù hợp: _____ (${vocab.meaning})"
            }

            TestQuestion.FillBlank(
                id = "fb_$index",
                vocab = vocab,
                sentence = sentence,
                blankPosition = wordPosition
            )
        }
    }

    fun generateMatchingQuestions(
        vocabs: List<Vocabulary>,
        pairsCount: Int = 5
    ): List<TestQuestion.Matching> {
        val questionsCount = (vocabs.size / pairsCount).coerceAtLeast(1)
        val shuffledVocabs = vocabs.shuffled()
        
        return (0 until questionsCount).mapNotNull { questionIndex ->
            val startIndex = questionIndex * pairsCount
            val endIndex = (startIndex + pairsCount).coerceAtMost(shuffledVocabs.size)
            
            if (startIndex >= shuffledVocabs.size) return@mapNotNull null
            
            val selectedVocabs = shuffledVocabs.subList(startIndex, endIndex)
            if (selectedVocabs.isEmpty()) return@mapNotNull null
            
            val pairs = selectedVocabs.map { vocab ->
                vocab.word to vocab.meaning
            }
            
            TestQuestion.Matching(
                id = "match_$questionIndex",
                vocab = selectedVocabs.first(),
                pairs = pairs.shuffled()
            )
        }
    }
}