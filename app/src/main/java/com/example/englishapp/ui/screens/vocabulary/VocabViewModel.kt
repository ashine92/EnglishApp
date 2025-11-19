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
import android.util.Log

class VocabViewModel(
    private val vocabRepository: VocabRepository
) : ViewModel() {

    private val _vocabList = MutableStateFlow<List<Vocabulary>>(emptyList())
    val vocabList: StateFlow<List<Vocabulary>> = _vocabList.asStateFlow()

    private val _filterStatus = MutableStateFlow<LearningStatus?>(null)
    val filterStatus: StateFlow<LearningStatus?> = _filterStatus.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()

    // State cho Firebase sync
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _unlearnedVocabCount = MutableStateFlow(0)
    val unlearnedVocabCount: StateFlow<Int> = _unlearnedVocabCount.asStateFlow()

    init {
        loadVocabulary()
        loadUnlearnedCount()
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
            loadUnlearnedCount() // Cập nhật số lượng từ chưa học
        }
    }

    /**
     * Tải số lượng từ chưa học
     */
    private fun loadUnlearnedCount() {
        viewModelScope.launch {
            vocabRepository.getUnlearnedVocabs().collect { vocabs ->
                _unlearnedVocabCount.value = vocabs.size
            }
        }
    }

    /**
     * Đồng bộ từ chưa học lên Firebase
     * Gọi hàm này khi người dùng nhấn nút "Sync to Firebase"
     */
    fun syncToFirebase() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Loading
            
            val result = vocabRepository.syncUnlearnedVocabsToFirebase()
            
            result.onSuccess { count ->
                _syncStatus.value = SyncStatus.Success(count)
                Log.d("VocabViewModel", "Synced $count words to Firebase")
            }.onFailure { error ->
                _syncStatus.value = SyncStatus.Error(error.message ?: "Unknown error")
                Log.e("VocabViewModel", "Failed to sync to Firebase", error)
            }
        }
    }

    /**
     * Reset trạng thái sync
     */
    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus.Idle
    }

    /**
     * Xóa tất cả từ vựng trên Firebase
     */
    fun clearFirebaseVocabs() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Loading
            
            val result = vocabRepository.clearFirebaseVocabs()
            
            result.onSuccess {
                _syncStatus.value = SyncStatus.Success(0)
                Log.d("VocabViewModel", "Cleared Firebase vocabs")
            }.onFailure { error ->
                _syncStatus.value = SyncStatus.Error(error.message ?: "Unknown error")
                Log.e("VocabViewModel", "Failed to clear Firebase", error)
            }
        }
    }
}

/**
 * Trạng thái đồng bộ Firebase
 */
sealed class SyncStatus {
    object Idle : SyncStatus()
    object Loading : SyncStatus()
    data class Success(val count: Int) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}