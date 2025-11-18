package com.example.englishapp.data.local.dao

import androidx.room.*
import com.example.englishapp.data.local.entity.PronunciationProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PronunciationProgressDao {
    @Query("SELECT * FROM pronunciation_progress ORDER BY timestamp DESC")
    fun getAllProgress(): Flow<List<PronunciationProgressEntity>>

    @Query("SELECT * FROM pronunciation_progress WHERE vocabId = :vocabId ORDER BY timestamp DESC")
    fun getProgressByVocabId(vocabId: Long): Flow<List<PronunciationProgressEntity>>

    @Query("SELECT * FROM pronunciation_progress WHERE vocabId = :vocabId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestProgressForVocab(vocabId: Long): PronunciationProgressEntity?

    @Query("SELECT AVG(score) FROM pronunciation_progress WHERE vocabId = :vocabId")
    suspend fun getAverageScoreForVocab(vocabId: Long): Double?

    @Query("SELECT COUNT(*) FROM pronunciation_progress WHERE vocabId = :vocabId")
    suspend fun getPracticeCountForVocab(vocabId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: PronunciationProgressEntity): Long

    @Delete
    suspend fun deleteProgress(progress: PronunciationProgressEntity)

    @Query("DELETE FROM pronunciation_progress WHERE vocabId = :vocabId")
    suspend fun deleteProgressByVocabId(vocabId: Long)
}
