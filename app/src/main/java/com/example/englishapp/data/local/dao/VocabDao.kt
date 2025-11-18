package com.example.englishapp.data.local.dao

import androidx.room.*
import com.example.englishapp.data.local.entity.VocabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabDao {
    @Query("SELECT * FROM vocabulary ORDER BY createdDate DESC")
    fun getAllVocabs(): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    suspend fun getVocabById(id: Long): VocabEntity?

    @Query("SELECT * FROM vocabulary WHERE category = :category")
    fun getVocabsByCategory(category: String): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocabulary WHERE learningStatus = :status")
    fun getVocabsByStatus(status: String): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocabulary WHERE word LIKE '%' || :query || '%'")
    fun searchVocabs(query: String): Flow<List<VocabEntity>>

    @Query("SELECT * FROM vocabulary ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomVocabs(limit: Int): List<VocabEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocab(vocab: VocabEntity): Long

    @Update
    suspend fun updateVocab(vocab: VocabEntity)

    @Delete
    suspend fun deleteVocab(vocab: VocabEntity)

    @Query("UPDATE vocabulary SET correctCount = correctCount + 1, lastReviewDate = :date WHERE id = :id")
    suspend fun incrementCorrectCount(id: Long, date: Long)

    @Query("UPDATE vocabulary SET wrongCount = wrongCount + 1, lastReviewDate = :date WHERE id = :id")
    suspend fun incrementWrongCount(id: Long, date: Long)

    @Query("UPDATE vocabulary SET learningStatus = :status WHERE id = :id")
    suspend fun updateLearningStatus(id: Long, status: String)
}