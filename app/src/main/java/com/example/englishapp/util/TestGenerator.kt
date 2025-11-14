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
        // Ưu tiên lấy từ có example
        val vocabsWithExample = vocabs.filter { !it.example.isNullOrBlank() }.shuffled()
        val vocabsWithoutExample = vocabs.filter { it.example.isNullOrBlank() }.shuffled()
        
        // Kết hợp: lấy từ có example trước, thiếu thì lấy từ không có example
        val selectedVocabs = (vocabsWithExample + vocabsWithoutExample).take(count)

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
        count: Int = 10
    ): List<TestQuestion.Matching> {
        val pairsPerQuestion = 5  // Số cặp từ mỗi câu hỏi
        val shuffledVocabs = vocabs.shuffled()
        
        return (0 until count).mapNotNull { questionIndex ->
            val startIndex = questionIndex * pairsPerQuestion
            val endIndex = (startIndex + pairsPerQuestion).coerceAtMost(shuffledVocabs.size)
            
            if (startIndex >= shuffledVocabs.size) {
                // Nếu hết từ, lặp lại từ đầu
                val recycledVocabs = shuffledVocabs.shuffled().take(pairsPerQuestion)
                val pairs = recycledVocabs.map { vocab ->
                    vocab.word to vocab.meaning
                }
                
                TestQuestion.Matching(
                    id = "match_$questionIndex",
                    vocab = recycledVocabs.first(),
                    pairs = pairs.shuffled()
                )
            } else {
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
}