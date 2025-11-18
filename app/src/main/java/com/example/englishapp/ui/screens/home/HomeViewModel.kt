package com.example.englishapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishapp.data.repository.TestRepository
import com.example.englishapp.data.repository.VocabRepository
import com.example.englishapp.domain.model.LearningStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val vocabRepository: VocabRepository,
    private val testRepository: TestRepository
) : ViewModel() {

    private val _statistics = MutableStateFlow(Statistics())
    val statistics: StateFlow<Statistics> = _statistics.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            vocabRepository.getAllVocabs().collect { vocabs ->
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.timeInMillis

                val averageScore = testRepository.getAverageScore(weekAgo)

                _statistics.value = Statistics(
                    totalVocabs = vocabs.size,
                    notLearnedVocabs = vocabs.count { it.learningStatus == LearningStatus.NOT_LEARNED },
                    learnedVocabs = vocabs.count { it.learningStatus == LearningStatus.LEARNED },
                    averageScore = averageScore
                )
            }
        }
    }

    fun refreshStatistics() {
        loadStatistics()
    }
}

data class Statistics(
    val totalVocabs: Int = 0,
    val notLearnedVocabs: Int = 0,
    val learnedVocabs: Int = 0,
    val averageScore: Float = 0f
)