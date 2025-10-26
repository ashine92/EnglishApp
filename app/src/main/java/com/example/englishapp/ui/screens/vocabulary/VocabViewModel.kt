package com.example.englishapp.ui.screens.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishapp.data.repository.VocabRepository
import com.example.englishapp.domain.model.LearningStatus
import com.example.englishapp.domain.model.Vocabulary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VocabViewModel(
    private val vocabRepository: VocabRepository
) : ViewModel() {

    private val _vocabList = MutableStateFlow<List<Vocabulary>>(emptyList())
    val vocabList: StateFlow<List<Vocabulary>> = _vocabList.asStateFlow()

    private val _filterStatus = MutableStateFlow<LearningStatus?>(null)
    val filterStatus: StateFlow<LearningStatus?> = _filterStatus.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()

    init {
        loadVocabulary()
    }

    fun loadVocabulary() {
        viewModelScope.launch {
            vocabRepository.getAllVocabs().collect { vocabs ->
                _vocabList.value = vocabs
            }
        }
    }

    fun filterByStatus(status: LearningStatus?) {
        _filterStatus.value = status
        viewModelScope.launch {
            if (status == null) {
                vocabRepository.getAllVocabs().collect { vocabs ->
                    _vocabList.value = vocabs
                }
            } else {
                vocabRepository.getVocabsByStatus(status).collect { vocabs ->
                    _vocabList.value = vocabs
                }
            }
        }
    }

    fun filterByCategory(category: String?) {
        _filterCategory.value = category
        viewModelScope.launch {
            if (category == null) {
                vocabRepository.getAllVocabs().collect { vocabs ->
                    _vocabList.value = vocabs
                }
            } else {
                vocabRepository.getVocabsByCategory(category).collect { vocabs ->
                    _vocabList.value = vocabs
                }
            }
        }
    }

    fun searchVocabs(query: String) {
        viewModelScope.launch {
            vocabRepository.searchVocabs(query).collect { vocabs ->
                _vocabList.value = vocabs
            }
        }
    }

    fun deleteVocabulary(vocab: Vocabulary) {
        viewModelScope.launch {
            vocabRepository.deleteVocab(vocab)
        }
    }

    fun updateVocabularyStatus(vocab: Vocabulary, newStatus: LearningStatus) {
        viewModelScope.launch {
            vocabRepository.updateVocab(vocab.copy(learningStatus = newStatus))
        }
    }
}