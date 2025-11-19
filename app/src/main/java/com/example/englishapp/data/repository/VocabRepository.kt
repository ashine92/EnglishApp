package com.example.englishapp.data.repository

import com.example.englishapp.data.local.dao.VocabDao
import com.example.englishapp.data.local.entity.VocabEntity
import com.example.englishapp.data.remote.GeminiWordLookupService
import com.example.englishapp.domain.model.LearningStatus
import com.example.englishapp.domain.model.Vocabulary
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class VocabRepository(
    private val vocabDao: VocabDao,
    private val geminiService: GeminiWordLookupService
) {
    // Firebase Database reference
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val unlearnedWordsRef = firebaseDatabase.getReference("unlearnedWords")

    fun getAllVocabs(): Flow<List<Vocabulary>> {
        return vocabDao.getAllVocabs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Lấy danh sách từ chưa học (NOT_LEARNED)
     */
    fun getUnlearnedVocabs(): Flow<List<Vocabulary>> {
        return vocabDao.getUnlearnedVocabs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getVocabsByCategory(category: String): Flow<List<Vocabulary>> {
        return vocabDao.getVocabsByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getVocabsByStatus(status: LearningStatus): Flow<List<Vocabulary>> {
        return vocabDao.getVocabsByStatus(status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun searchVocabs(query: String): Flow<List<Vocabulary>> {
        return vocabDao.searchVocabs(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getRandomVocabs(limit: Int): List<Vocabulary> {
        return vocabDao.getRandomVocabs(limit).map { it.toDomain() }
    }

    /**
     * Search word online using Gemini API
     */
    suspend fun searchWordOnline(word: String): Result<Vocabulary> {
        return try {
            val result = geminiService.lookupWord(word)
            result.map { response ->
                Vocabulary(
                    word = response.word,
                    phonetic = response.phonetic,
                    meaning = response.meaning,
                    example = response.example,
                    partOfSpeech = null, // Can be enhanced with Gemini to detect part of speech
                    category = null
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search word with specific English level (A1-C2)
     */
    suspend fun searchWordWithLevel(word: String, level: String): Result<Vocabulary> {
        return try {
            val result = geminiService.lookupWordWithLevel(word, level)
            result.map { response ->
                Vocabulary(
                    word = response.word,
                    phonetic = response.phonetic,
                    meaning = response.meaning,
                    example = response.example,
                    partOfSpeech = null,
                    category = null
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertVocab(vocab: Vocabulary): Long {
        return vocabDao.insertVocab(vocab.toEntity())
    }

    suspend fun updateVocab(vocab: Vocabulary) {
        vocabDao.updateVocab(vocab.toEntity())
    }

    suspend fun deleteVocab(vocab: Vocabulary) {
        vocabDao.deleteVocab(vocab.toEntity())
    }

    suspend fun updateVocabReview(vocabId: Long, isCorrect: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (isCorrect) {
            vocabDao.incrementCorrectCount(vocabId, currentTime)
            // Mark as learned when answered correctly
            vocabDao.updateLearningStatus(vocabId, LearningStatus.LEARNED.name)
        } else {
            vocabDao.incrementWrongCount(vocabId, currentTime)
            // Keep as not learned when answered incorrectly
            vocabDao.updateLearningStatus(vocabId, LearningStatus.NOT_LEARNED.name)
        }
    }

    /**
     * Đồng bộ tất cả từ chưa học lên Firebase Realtime Database
     * ESP32 sẽ đọc dữ liệu từ Firebase và hiển thị trên LCD
     */
    suspend fun syncUnlearnedVocabsToFirebase(): Result<Int> {
        return try {
            // Lấy tất cả từ chưa học từ database
            val unlearnedVocabs = vocabDao.getUnlearnedVocabs().first()
            
            if (unlearnedVocabs.isEmpty()) {
                return Result.success(0)
            }

            // Tạo map để gửi lên Firebase
            val vocabsMap = unlearnedVocabs.mapIndexed { index, vocab ->
                index.toString() to mapOf(
                    "word" to vocab.word,
                    "phonetic" to (vocab.phonetic ?: ""),
                    "meaning" to vocab.meaning,
                    "example" to (vocab.example ?: ""),
                    "category" to (vocab.category ?: ""),
                    "createdDate" to vocab.createdDate
                )
            }.toMap()

            // Gửi lên Firebase (ghi đè toàn bộ danh sách)
            unlearnedWordsRef.setValue(vocabsMap).await()
            
            Result.success(unlearnedVocabs.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa tất cả từ vựng trên Firebase (dùng khi cần reset)
     */
    suspend fun clearFirebaseVocabs(): Result<Unit> {
        return try {
            unlearnedWordsRef.removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun VocabEntity.toDomain() = Vocabulary(
        id = id,
        word = word,
        phonetic = phonetic,
        meaning = meaning,
        example = example,
        partOfSpeech = partOfSpeech,
        category = category,
        difficulty = difficulty,
        learningStatus = LearningStatus.valueOf(learningStatus),
        correctCount = correctCount,
        wrongCount = wrongCount,
        lastReviewDate = lastReviewDate,
        createdDate = createdDate
    )

    private fun Vocabulary.toEntity() = VocabEntity(
        id = id,
        word = word,
        phonetic = phonetic,
        meaning = meaning,
        example = example,
        partOfSpeech = partOfSpeech,
        category = category,
        difficulty = difficulty,
        learningStatus = learningStatus.name,
        correctCount = correctCount,
        wrongCount = wrongCount,
        lastReviewDate = lastReviewDate,
        createdDate = createdDate
    )
}