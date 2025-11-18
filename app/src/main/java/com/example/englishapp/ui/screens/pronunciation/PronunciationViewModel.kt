package com.example.englishapp.ui.screens.pronunciation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishapp.data.repository.PronunciationRepository
import com.example.englishapp.data.repository.VocabRepository
import com.example.englishapp.domain.model.PronunciationResult
import com.example.englishapp.domain.model.Vocabulary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for pronunciation practice screen
 */
class PronunciationViewModel(
    private val pronunciationRepository: PronunciationRepository,
    private val vocabRepository: VocabRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PronunciationUiState>(PronunciationUiState.Idle)
    val uiState: StateFlow<PronunciationUiState> = _uiState.asStateFlow()

    private val _currentWord = MutableStateFlow<Vocabulary?>(null)
    val currentWord: StateFlow<Vocabulary?> = _currentWord.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _microphoneState = MutableStateFlow(MicrophoneState.IDLE)
    val microphoneState: StateFlow<MicrophoneState> = _microphoneState.asStateFlow()

    // For word selection screen
    private val _vocabularyList = MutableStateFlow<List<Vocabulary>>(emptyList())
    val vocabularyList: StateFlow<List<Vocabulary>> = _vocabularyList.asStateFlow()

    private val _pronunciationStats = MutableStateFlow<Map<Long, VocabularyPronunciationStats>>(emptyMap())
    val pronunciationStats: StateFlow<Map<Long, VocabularyPronunciationStats>> = _pronunciationStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Load all vocabulary for word selection
     */
    fun loadVocabularyList() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                vocabRepository.getAllVocabs().collect { vocabs ->
                    _vocabularyList.value = vocabs
                    
                    // Load pronunciation statistics for each vocabulary
                    val stats = mutableMapOf<Long, VocabularyPronunciationStats>()
                    vocabs.forEach { vocab ->
                        val practiceCount = pronunciationRepository.getPracticeCount(vocab.id)
                        val averageScore = pronunciationRepository.getAverageScore(vocab.id)
                        stats[vocab.id] = VocabularyPronunciationStats(
                            practiceCount = practiceCount,
                            averageScore = averageScore
                        )
                    }
                    _pronunciationStats.value = stats
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _uiState.value = PronunciationUiState.Error("Failed to load vocabulary: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    /**
     * Load a specific word by ID for pronunciation practice
     */
    fun loadWordById(vocabId: Long) {
        viewModelScope.launch {
            try {
                // Đảm bảo danh sách từ vựng đã được load
                if (_vocabularyList.value.isEmpty()) {
                    // Load vocabulary list first if empty
                    vocabRepository.getAllVocabs().collect { vocabs ->
                        _vocabularyList.value = vocabs

                        // Now find the word
                        val word = vocabs.find { it.id == vocabId }
                        if (word != null) {
                            _currentWord.value = word
                            _uiState.value = PronunciationUiState.Idle
                        } else {
                            _uiState.value = PronunciationUiState.Error("Từ vựng không tồn tại")
                        }
                    }
                } else {
                    // Find word in existing list
                    val word = _vocabularyList.value.find { it.id == vocabId }
                    if (word != null) {
                        _currentWord.value = word
                        _uiState.value = PronunciationUiState.Idle
                    } else {
                        _uiState.value = PronunciationUiState.Error("Từ vựng không tồn tại")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PronunciationUiState.Error("Lỗi tải từ vựng: ${e.message}")
            }
        }
    }

    /**
     * Set recognized text from speech recognizer
     */
    fun setRecognizedText(text: String) {
        _recognizedText.value = text
    }

    /**
     * Update microphone state
     */
    fun setMicrophoneState(state: MicrophoneState) {
        _microphoneState.value = state
    }

    /**
     * Score the pronunciation
     */
    fun scorePronunciation() {
        val word = _currentWord.value
        if (word == null) {
            _uiState.value = PronunciationUiState.Error("No word selected")
            return
        }
        
        if (_recognizedText.value.isBlank()) {
            _uiState.value = PronunciationUiState.Error("Please speak first before scoring")
            return
        }

        viewModelScope.launch {
            _uiState.value = PronunciationUiState.Scoring

            val result = pronunciationRepository.scorePronunciation(
                expectedText = word.word,
                userText = _recognizedText.value
            )

            _uiState.value = result.fold(
                onSuccess = { pronunciationResult ->
                    // Save pronunciation progress
                    pronunciationRepository.savePronunciationProgress(
                        vocabId = word.id,
                        word = word.word,
                        userText = _recognizedText.value,
                        score = pronunciationResult.score,
                        similarity = pronunciationResult.similarity
                    )
                    
                    PronunciationUiState.Success(pronunciationResult)
                },
                onFailure = { error ->
                    PronunciationUiState.Error(error.message ?: "Failed to score pronunciation")
                }
            )
        }
    }

    /**
     * Reset current attempt (keep same word)
     */
    fun reset() {
        _recognizedText.value = ""
        _uiState.value = PronunciationUiState.Idle
        _microphoneState.value = MicrophoneState.IDLE
    }

    /**
     * Clear current word (for navigating back to selection)
     */
    fun clearCurrentWord() {
        _currentWord.value = null
        _recognizedText.value = ""
        _uiState.value = PronunciationUiState.Idle
        _microphoneState.value = MicrophoneState.IDLE
    }
}

/**
 * UI state for pronunciation screen
 */
sealed class PronunciationUiState {
    object Idle : PronunciationUiState()
    object Scoring : PronunciationUiState()
    data class Success(val result: PronunciationResult) : PronunciationUiState()
    data class Error(val message: String) : PronunciationUiState()
}

/**
 * Microphone button states
 */
enum class MicrophoneState {
    IDLE,       // Ready to record
    LISTENING,  // Currently recording
    PROCESSING  // Processing speech
}
