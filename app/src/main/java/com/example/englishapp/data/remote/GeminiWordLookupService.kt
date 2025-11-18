package com.example.englishapp.data.remote

import com.example.englishapp.data.remote.dto.GeminiWordResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for looking up words using Google Gemini API
 */
class GeminiWordLookupService(
    private val apiKey: String
) {
    private val model = GenerativeModel(
        modelName = "gemini-1.5-pro",
        apiKey = apiKey
    )
    
    private val gson = Gson()

    /**
     * Look up a word/phrase using Gemini API
     * Returns standardized word information including meaning, synonyms, examples, and pronunciation
     */
    suspend fun lookupWord(word: String): Result<GeminiWordResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPrompt(word)
            val response = model.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response from Gemini API")
            
            // Parse JSON response from Gemini
            val wordResponse = parseGeminiResponse(responseText)
            Result.success(wordResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Look up word with specific English level context (A1-C2)
     */
    suspend fun lookupWordWithLevel(word: String, level: String): Result<GeminiWordResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPromptWithLevel(word, level)
            val response = model.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response from Gemini API")
            
            val wordResponse = parseGeminiResponse(responseText)
            Result.success(wordResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(word: String): String {
        return """
            Look up the English word or phrase: "$word"
            
            Provide the following information in JSON format:
            {
              "word": "the word or phrase",
              "meaning": "clear definition in English",
              "example": "example sentence using the word",
              "synonyms": ["synonym1", "synonym2", "synonym3"],
              "phonetic": "IPA pronunciation notation"
            }
            
            Important:
            - Return ONLY valid JSON, no additional text
            - Provide at least 3 synonyms if available
            - Use IPA (International Phonetic Alphabet) for pronunciation
            - Make the example sentence natural and clear
            - If the word has multiple meanings, focus on the most common one
        """.trimIndent()
    }
    
    private fun buildPromptWithLevel(word: String, level: String): String {
        return """
            Look up the English word or phrase: "$word" for CEFR level $level learners
            
            Provide the following information in JSON format:
            {
              "word": "the word or phrase",
              "meaning": "clear definition appropriate for $level level",
              "example": "example sentence suitable for $level level learners",
              "synonyms": ["synonym1", "synonym2", "synonym3"],
              "phonetic": "IPA pronunciation notation"
            }
            
            Important:
            - Return ONLY valid JSON, no additional text
            - Adjust explanation complexity for $level level (A1=beginner, C2=advanced)
            - Use example sentences with appropriate difficulty for $level learners
            - Provide at least 3 synonyms if available
            - Use IPA (International Phonetic Alphabet) for pronunciation
        """.trimIndent()
    }
    
    private fun parseGeminiResponse(responseText: String): GeminiWordResponse {
        // Extract JSON from response (Gemini sometimes adds markdown code blocks)
        val jsonText = responseText
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        
        return gson.fromJson(jsonText, GeminiWordResponse::class.java)
    }
}
