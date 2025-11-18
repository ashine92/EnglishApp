package com.example.englishapp.data.remote

import com.example.englishapp.data.remote.dto.PronunciationScoreResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for AI-based pronunciation scoring using Google Gemini
 */
class PronunciationScoringService(
    private val apiKey: String
) {
    private val model = GenerativeModel(
        modelName = "gemini-1.5-pro",
        apiKey = apiKey
    )
    
    private val gson = Gson()

    /**
     * Score pronunciation by comparing expected text with user's spoken text
     */
    suspend fun scorePronunciation(
        expectedText: String,
        userText: String
    ): Result<PronunciationScoreResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildScoringPrompt(expectedText, userText)
            val response = model.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response from Gemini API")
            
            val scoreResponse = parseResponse(responseText)
            Result.success(scoreResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildScoringPrompt(expectedText: String, userText: String): String {
        return """
            You are an English pronunciation assessment expert. Analyze the pronunciation accuracy.
            
            Expected text (correct): "$expectedText"
            User's spoken text (from speech recognition): "$userText"
            
            Compare the two texts and provide a pronunciation assessment in JSON format:
            {
              "score": <number 0-100>,
              "similarity": "<High/Medium/Low>",
              "mistakes": ["list", "of", "errors"],
              "feedback": "specific improvement suggestions"
            }
            
            Scoring criteria:
            - 90-100: Excellent pronunciation, all words correct
            - 75-89: Good pronunciation, minor mistakes
            - 60-74: Fair pronunciation, several mistakes
            - 40-59: Needs improvement, many mistakes
            - 0-39: Poor pronunciation, major issues
            
            For mistakes, identify:
            - Missing words (words in expected but not in user text)
            - Wrong words (different words spoken)
            - Word order issues
            
            For feedback, provide:
            - Specific words to practice
            - General pronunciation tips
            - Encouragement
            
            Important: Return ONLY valid JSON, no additional text.
        """.trimIndent()
    }

    private fun parseResponse(responseText: String): PronunciationScoreResponse {
        // Extract JSON from response
        val jsonText = responseText
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        
        return gson.fromJson(jsonText, PronunciationScoreResponse::class.java)
    }
}
