package com.example.englishapp.data.repository

import com.example.englishapp.data.local.dao.TestResultDao
import com.example.englishapp.data.local.entity.TestResultEntity
import com.example.englishapp.domain.model.TestResult
import com.example.englishapp.domain.model.TestType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TestRepository(
    private val testResultDao: TestResultDao
) {
    fun getAllTestResults(): Flow<List<TestResult>> {
        return testResultDao.getAllTestResults().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getTestResultsByType(type: TestType): Flow<List<TestResult>> {
        return testResultDao.getTestResultsByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertTestResult(result: TestResult): Long {
        return testResultDao.insertTestResult(result.toEntity())
    }

    suspend fun getAverageScore(fromDate: Long): Float {
        return testResultDao.getAverageScore(fromDate) ?: 0f
    }

    private fun TestResultEntity.toDomain() = TestResult(
        id = id,
        testType = TestType.valueOf(testType),
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        wrongAnswers = wrongAnswers,
        score = score,
        duration = duration,
        vocabIds = vocabIds.split(",").mapNotNull { it.toLongOrNull() },
        testDate = testDate
    )

    private fun TestResult.toEntity() = TestResultEntity(
        id = id,
        testType = testType.name,
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        wrongAnswers = wrongAnswers,
        score = score,
        duration = duration,
        vocabIds = vocabIds.joinToString(","),
        testDate = testDate
    )
}