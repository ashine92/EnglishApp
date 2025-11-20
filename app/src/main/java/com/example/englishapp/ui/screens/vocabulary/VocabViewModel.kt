package com.example.englishapp.ui.screens.vocabulary  // CODE CỦ: Giữ nguyên.

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishapp.data.repository.VocabRepository  // CODE CỦ: Giữ nguyên.
import com.example.englishapp.domain.model.LearningStatus  // CODE CỦ: Giữ nguyên.
import com.example.englishapp.domain.model.Vocabulary  // CODE CỦ: Giữ nguyên.
import kotlinx.coroutines.flow.MutableStateFlow  // CODE CỦ: Giữ nguyên.
import kotlinx.coroutines.flow.StateFlow  // CODE CỦ: Giữ nguyên.
import kotlinx.coroutines.flow.asStateFlow  // CODE CỦ: Giữ nguyên.
import kotlinx.coroutines.flow.collectLatest  // CODE MỚI: Thêm import này để lắng nghe Flow từ getUnlearnedVocabs().
import kotlinx.coroutines.launch  // CODE CỦ: Giữ nguyên.

class VocabViewModel(
    private val vocabRepository: VocabRepository  // CODE CỦ: Giữ nguyên.
) : ViewModel() {

    private val _vocabList = MutableStateFlow<List<Vocabulary>>(emptyList())  // CODE CỦ: Giữ nguyên.
    val vocabList: StateFlow<List<Vocabulary>> = _vocabList.asStateFlow()  // CODE CỦ: Giữ nguyên.

    private val _filterStatus = MutableStateFlow<LearningStatus?>(null)  // CODE CỦ: Giữ nguyên.
    val filterStatus: StateFlow<LearningStatus?> = _filterStatus.asStateFlow()  // CODE CỦ: Giữ nguyên.

    private val _filterCategory = MutableStateFlow<String?>(null)  // CODE CỦ: Giữ nguyên.
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()  // CODE CỦ: Giữ nguyên.

    init {
        loadVocabulary()  // CODE CỦ: Giữ nguyên – load tất cả từ vựng.

        // CODE MỚI: Thêm logic auto-sync. Lắng nghe Flow từ getUnlearnedVocabs() (từ VocabRepository). Khi danh sách từ chưa thuộc thay đổi (do cập nhật DB), tự động gọi sync lên Firebase.
        viewModelScope.launch {
            vocabRepository.getUnlearnedVocabs().collectLatest { unlearnedList ->
                // Chỉ sync nếu danh sách không rỗng (tránh sync không cần thiết khi không có từ chưa thuộc).
                if (unlearnedList.isNotEmpty()) {
                    vocabRepository.syncUnlearnedVocabsToFirebase()
                }
            }
        }
    }

    fun loadVocabulary() {  // CODE CỦ: Giữ nguyên toàn bộ hàm.
        viewModelScope.launch {
            vocabRepository.getAllVocabs().collect { vocabs ->
                _vocabList.value = vocabs
            }
        }
    }

    fun filterByStatus(status: LearningStatus?) {  // CODE CỦ: Giữ nguyên toàn bộ hàm.
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

    fun filterByCategory(category: String?) {  // CODE CỦ: Giữ nguyên toàn bộ hàm.
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

    fun searchVocabs(query: String) {  // CODE CỦ: Giữ nguyên toàn bộ hàm.
        viewModelScope.launch {
            vocabRepository.searchVocabs(query).collect { vocabs ->
                _vocabList.value = vocabs
            }
        }
    }

    fun deleteVocabulary(vocab: Vocabulary) {  // CODE CỦ: Giữ nguyên toàn bộ hàm.
        viewModelScope.launch {
            vocabRepository.deleteVocab(vocab)
        }
    }

    fun updateVocabularyStatus(vocab: Vocabulary, newStatus: LearningStatus) {  // CODE CỦ: Giữ nguyên toàn bộ hàm.
        viewModelScope.launch {
            vocabRepository.updateVocab(vocab.copy(learningStatus = newStatus))
        }
    }
}