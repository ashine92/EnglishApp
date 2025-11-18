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
    fun generateFillBlankQuestions_usesVocabsWithoutExampleWhenNeeded() {
        val vocabs = listOf(
            createMockVocab(1, "hello", "xin chào", null),
            createMockVocab(2, "world", "thế giới", ""),
            createMockVocab(3, "apple", "táo", "I like apples.")
        )

        val questions = TestGenerator.generateFillBlankQuestions(vocabs, 3)

        // Should now generate 3 questions (1 with example, 2 with fallback format)
        assertEquals(3, questions.size)
        assertTrue(questions.all { it.sentence.contains("_____") })
        
        // At least one should use fallback format
        assertTrue(questions.any { it.sentence.startsWith("Điền từ phù hợp:") })
    }

    @Test
    fun generateFillBlankQuestions_prioritizesVocabsWithExample() {
        val vocabs = listOf(
            createMockVocab(1, "hello", "xin chào", "Say hello to everyone."),
            createMockVocab(2, "world", "thế giới", "Welcome to the world."),
            createMockVocab(3, "apple", "táo", null),
            createMockVocab(4, "book", "sách", null)
        )

        val questions = TestGenerator.generateFillBlankQuestions(vocabs, 3)

        assertEquals(3, questions.size)
        // First 2 should have examples (not fallback format)
        val withExamples = questions.filter { !it.sentence.startsWith("Điền từ phù hợp:") }
        assertTrue(withExamples.size >= 2)
    }

    @Test
    fun generateMatchingQuestions_createsRequestedNumberOfQuestions() {
        val vocabs = (1..20).map { i ->
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, count = 10)

        // Should create exactly 10 questions as requested
        assertEquals(10, questions.size)
    }

    @Test
    fun generateMatchingQuestions_eachQuestionHasCorrectPairs() {
        val vocabs = (1..25).map { i ->
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, count = 5)

        assertEquals(5, questions.size)
        
        // Each question should have 3 pairs (changed from 5)
        questions.forEach { question ->
            assertEquals(3, question.pairs.size)
        }

        // Verify pairs contain word-meaning combinations
        questions.forEach { question ->
            question.pairs.forEach { (word, meaning) ->
                assertTrue(word.startsWith("word"))
                assertTrue(meaning.startsWith("meaning"))
            }
        }
    }

    @Test
    fun generateMatchingQuestions_recyclesVocabsWhenNotEnough() {
        val vocabs = (1..12).map { i ->  // At least 10 required, use 12
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, count = 5)

        // Should create 5 questions
        assertEquals(5, questions.size)
        
        // Each question should have 3 pairs
        questions.forEach { question ->
            assertEquals(3, question.pairs.size)
        }
    }

    @Test
    fun generateMatchingQuestions_withExactMultiple_createsCorrectCount() {
        val vocabs = (1..25).map { i ->
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, count = 3)

        // Should create exactly 3 questions as requested
        assertEquals(3, questions.size)
        assertEquals(3, questions[0].pairs.size)
        assertEquals(3, questions[1].pairs.size)
        assertEquals(3, questions[2].pairs.size)
    }

    @Test
    fun generateMatchingQuestions_hasUniqueIds() {
        val vocabs = (1..20).map { i ->
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, count = 10)

        val ids = questions.map { it.id }
        assertEquals(ids.size, ids.toSet().size) // All IDs should be unique
        assertEquals("match_0", questions[0].id)
        assertEquals("match_1", questions[1].id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun generateMatchingQuestions_requiresMinimum10Vocabs() {
        val vocabs = (1..9).map { i ->  // Only 9 vocabs, should fail
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        // Should throw IllegalArgumentException
        TestGenerator.generateMatchingQuestions(vocabs, count = 5)
    }

    @Test
    fun generateMatchingQuestions_worksWithExactly10Vocabs() {
        val vocabs = (1..10).map { i ->  // Exactly 10 vocabs, minimum required
            createMockVocab(i.toLong(), "word$i", "meaning$i", "example$i")
        }

        val questions = TestGenerator.generateMatchingQuestions(vocabs, count = 3)

        // Should work with 10 vocabs
        assertEquals(3, questions.size)
        questions.forEach { question ->
            assertEquals(3, question.pairs.size)
        }
    }
}
