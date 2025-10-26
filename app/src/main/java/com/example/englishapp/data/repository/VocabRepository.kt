package com.example.englishapp.data.repository

import com.example.englishapp.data.local.dao.VocabDao
import com.example.englishapp.data.local.entity.VocabEntity
import com.example.englishapp.data.remote.DictionaryApi
import com.example.englishapp.domain.model.LearningStatus
import com.example.englishapp.domain.model.Vocabulary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VocabRepository(
    private val vocabDao: VocabDao,
    private val dictionaryApi: DictionaryApi
) {
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

    suspend fun searchWordOnline(word: String): Result<Vocabulary> {
        return try {
            val response = dictionaryApi.searchWord(word)
            if (response.isNotEmpty()) {
                val data = response[0]
                val meaning = data.meanings.firstOrNull()
                val definition = meaning?.definitions?.firstOrNull()

                Result.success(
                    Vocabulary(
                        word = data.word,
                        phonetic = data.phonetic ?: data.phonetics?.firstOrNull()?.text,
                        meaning = definition?.definition ?: "No definition found",
                        example = definition?.example,
                        partOfSpeech = meaning?.partOfSpeech,
                        category = null  // ← THÊM DÒNG NÀY
                    )
                )
            } else {
                Result.failure(Exception("Word not found"))
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
        } else {
            vocabDao.incrementWrongCount(vocabId, currentTime)
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