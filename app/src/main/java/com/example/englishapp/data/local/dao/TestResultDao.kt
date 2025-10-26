package com.example.englishapp.data.local.dao

import androidx.room.*
import com.example.englishapp.data.local.entity.TestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_results ORDER BY testDate DESC")
    fun getAllTestResults(): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE testType = :type ORDER BY testDate DESC")
    fun getTestResultsByType(type: String): Flow<List<TestResultEntity>>

    @Insert
    suspend fun insertTestResult(result: TestResultEntity): Long

    @Query("SELECT AVG(score) FROM test_results WHERE testDate >= :fromDate")
    suspend fun getAverageScore(fromDate: Long): Float?
}