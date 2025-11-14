package com.example.englishapp.util

import com.example.englishapp.domain.model.Vocabulary
import org.junit.Test
import org.junit.Assert.*

class TestGeneratorTest {

    private fun createMockVocab(
        id: Long,
        word: String,
        meaning: String,
        example: String? = null
    ) = Vocabulary(
        id = id,
        word = word,
        meaning = meaning,
        example = example
    )

    @Test
    fun generateFillBlankQuestions_withWordInExample_replacesWord() {
        val vocabs = listOf(
            createMockVocab(1, "hello", "xin chào", "Say hello to everyone."),
            createMockVocab(2, "world", "thế giới", "Welcome to the world of programming.")
        )

        val questions = TestGenerator.generateFillBlankQuestions(vocabs, 2)

        assertEquals(2, questions.size)
        assertTrue(questions[0].sentence.contains("_____"))
        assertFalse(questions[0].sentence.contains("hello", ignoreCase = true))
        assertTrue(questions[1].sentence.contains("_____"))
        assertFalse(questions[1].sentence.contains("world", ignoreCase = true))
    }

    @Test
    fun generateFillBlankQuestions_withWordNotInExample_usesFallbackFormat() {
        val vocabs = listOf(
            createMockVocab(1, "apple", "táo", "I like oranges.")
        )

        val questions = TestGenerator.generateFillBlankQuestions(vocabs, 1)

        assertEquals(1, questions.size)
        assertEquals("Điền từ phù hợp: _____ (táo)", questions[0].sentence)
    }

    @Test
    fun generateFillBlankQuestions_filtersVocabsWithoutExample() {
        val vocabs = listOf(
            createMockVocab(1, "hello", "xin chào", null),
            createMockVocab(2, "world", "thế giới", ""),
            createMockVocab(3, "apple", "táo", "I like apples.")
        )

        val questions = TestGenerator.generateFillBlankQuestions(vocabs, 10)

        // Only the vocab with non-empty example should be included
        assertEquals(1, questions.size)
        assertTrue(questions[0].sentence.contains("_____"))
    }

    @Test
    fun generateMatchingQuestions_createsMultipleQuestions() {
        val vocabs = (1..10).map { i ->
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, pairsCount = 5)

        // With 10 vocabs and 5 pairs per question, we should get 2 questions
        assertEquals(2, questions.size)
    }

    @Test
    fun generateMatchingQuestions_eachQuestionHasCorrectPairs() {
        val vocabs = (1..10).map { i ->
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, pairsCount = 5)

        // First question should have 5 pairs
        assertEquals(5, questions[0].pairs.size)
        // Second question should have 5 pairs
        assertEquals(5, questions[1].pairs.size)

        // Verify pairs contain word-meaning combinations
        questions.forEach { question ->
            question.pairs.forEach { (word, meaning) ->
                assertTrue(word.startsWith("word"))
                assertTrue(meaning.startsWith("meaning"))
            }
        }
    }

    @Test
    fun generateMatchingQuestions_withFewerVocabsThanPairs_createsOneQuestion() {
        val vocabs = (1..3).map { i ->
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, pairsCount = 5)

        // With only 3 vocabs, should create 1 question with 3 pairs
        assertEquals(1, questions.size)
        assertEquals(3, questions[0].pairs.size)
    }

    @Test
    fun generateMatchingQuestions_withExactMultiple_createsCorrectCount() {
        val vocabs = (1..15).map { i ->
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, pairsCount = 5)

        // With 15 vocabs and 5 pairs per question, we should get 3 questions
        assertEquals(3, questions.size)
        assertEquals(5, questions[0].pairs.size)
        assertEquals(5, questions[1].pairs.size)
        assertEquals(5, questions[2].pairs.size)
    }

    @Test
    fun generateMatchingQuestions_hasUniqueIds() {
        val vocabs = (1..10).map { i ->
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, pairsCount = 5)

        val ids = questions.map { it.id }
        assertEquals(ids.size, ids.toSet().size) // All IDs should be unique
        assertEquals("match_0", questions[0].id)
        assertEquals("match_1", questions[1].id)
    }
}
