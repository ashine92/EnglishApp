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
                    newVocabs = vocabs.count { it.learningStatus == LearningStatus.NEW },
                    learningVocabs = vocabs.count { it.learningStatus == LearningStatus.LEARNING },
                    masteredVocabs = vocabs.count { it.learningStatus == LearningStatus.MASTERED },
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
    val newVocabs: Int = 0,
    val learningVocabs: Int = 0,
    val masteredVocabs: Int = 0,
    val averageScore: Float = 0f
)