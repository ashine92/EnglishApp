package com.example.englishapp.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishapp.data.repository.VocabRepository
import com.example.englishapp.domain.model.Vocabulary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val vocabRepository: VocabRepository
) : ViewModel() {

    private val _searchResult = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchResult: StateFlow<SearchState> = _searchResult.asStateFlow()

    fun searchWord(word: String) {
        if (word.isBlank()) {
            _searchResult.value = SearchState.Idle
            return
        }

        viewModelScope.launch {
            _searchResult.value = SearchState.Loading

            val result = vocabRepository.searchWordOnline(word.trim())

            _searchResult.value = result.fold(
                onSuccess = { vocab -> SearchState.Success(vocab) },
                onFailure = { error -> SearchState.Error(error.message ?: "Không tìm thấy từ") }
            )
        }
    }

    fun saveVocabulary(vocab: Vocabulary, category: String?) {
        viewModelScope.launch {
            val updatedVocab = vocab.copy(category = category)
            vocabRepository.insertVocab(updatedVocab)
            _searchResult.value = SearchState.Saved
        }
    }

    fun resetSearch() {
        _searchResult.value = SearchState.Idle
    }
}

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val vocabulary: Vocabulary) : SearchState()
    object Saved : SearchState()
    data class Error(val message: String) : SearchState()
}