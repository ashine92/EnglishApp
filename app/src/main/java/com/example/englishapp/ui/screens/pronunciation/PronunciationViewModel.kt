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

    private var availableVocabs = listOf<Vocabulary>()

    init {
        loadVocabulary()
    }

    /**
     * Load vocabulary from database
     */
    private fun loadVocabulary() {
        viewModelScope.launch {
            try {
                // Get random vocabularies from database
                val vocabs = vocabRepository.getRandomVocabs(100)
                availableVocabs = vocabs
                
                if (vocabs.isNotEmpty()) {
                    _currentWord.value = vocabs.random()
                } else {
                    _uiState.value = PronunciationUiState.Error("No vocabulary available. Please add some words first.")
                }
            } catch (e: Exception) {
                _uiState.value = PronunciationUiState.Error("Failed to load vocabulary: ${e.message}")
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
                    PronunciationUiState.Success(pronunciationResult)
                },
                onFailure = { error ->
                    PronunciationUiState.Error(error.message ?: "Failed to score pronunciation")
                }
            )
        }
    }

    /**
     * Reset to try again with new word
     */
    fun tryAgain() {
        if (availableVocabs.isNotEmpty()) {
            _currentWord.value = availableVocabs.random()
        }
        _recognizedText.value = ""
        _uiState.value = PronunciationUiState.Idle
        _microphoneState.value = MicrophoneState.IDLE
    }

    /**
     * Reset current attempt
     */
    fun reset() {
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
