package com.example.englishapp.data.repository

import com.example.englishapp.data.local.dao.VocabDao
import com.example.englishapp.data.local.entity.VocabEntity
import com.example.englishapp.data.remote.GeminiWordLookupService
import com.example.englishapp.domain.model.LearningStatus
import com.example.englishapp.domain.model.Vocabulary
import com.google.firebase.database.FirebaseDatabase  // Code mới: Import Firebase để gửi dữ liệu
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first  // Code mới: Để chuyển Flow thành List
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await  // Code mới: Để chờ Firebase sync

class VocabRepository(
    private val vocabDao: VocabDao,
    private val geminiService: GeminiWordLookupService
) {
    // Code cũ: Các hàm hiện có (không thay đổi)
    fun getAllVocabs(): Flow<List<Vocabulary>> {
        return vocabDao.getAllVocabs().map { entities ->
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

    // Code mới: Hàm lấy tất cả từ chưa thuộc (dựa trên query mới từ VocabDao)
    // Lý do: Để dễ dàng truy cập từ chưa thuộc, chuyển từ entity sang domain model
    fun getUnlearnedVocabs(): Flow<List<Vocabulary>> {
        return vocabDao.getUnlearnedVocabs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // Code mới: Hàm gửi toàn bộ từ chưa thuộc lên Firebase
    // Lý do: Đồng bộ dữ liệu thời gian thực từ app lên đám mây, để ESP32 truy cập
    suspend fun syncUnlearnedVocabsToFirebase() {
        val unlearnedVocabs = getUnlearnedVocabs().first()  // Lấy snapshot hiện tại của Flow
        val database = FirebaseDatabase.getInstance().reference
        val wordsMap = mutableMapOf<String, String>()
        unlearnedVocabs.forEachIndexed { index, vocab ->
            wordsMap["word${index + 1}"] = vocab.word  // Chỉ gửi từ (word), key dạng word1, word2, ...
        }
        database.child("unlearnedWords").setValue(wordsMap).await()  // Gửi lên Firebase và chờ hoàn thành
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