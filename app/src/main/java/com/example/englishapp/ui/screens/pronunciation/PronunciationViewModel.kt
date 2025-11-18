package com.example.englishapp.ui.screens.pronunciation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishapp.data.repository.PronunciationRepository
import com.example.englishapp.domain.model.PronunciationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for pronunciation practice screen
 */
class PronunciationViewModel(
    private val pronunciationRepository: PronunciationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PronunciationUiState>(PronunciationUiState.Idle)
    val uiState: StateFlow<PronunciationUiState> = _uiState.asStateFlow()

    private val _sampleSentence = MutableStateFlow(DEFAULT_SENTENCES.random())
    val sampleSentence: StateFlow<String> = _sampleSentence.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _microphoneState = MutableStateFlow(MicrophoneState.IDLE)
    val microphoneState: StateFlow<MicrophoneState> = _microphoneState.asStateFlow()

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
        if (_recognizedText.value.isBlank()) {
            _uiState.value = PronunciationUiState.Error("Please speak first before scoring")
            return
        }

        viewModelScope.launch {
            _uiState.value = PronunciationUiState.Scoring

            val result = pronunciationRepository.scorePronunciation(
                expectedText = _sampleSentence.value,
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
     * Reset to try again with new sentence
     */
    fun tryAgain() {
        _sampleSentence.value = DEFAULT_SENTENCES.random()
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

    companion object {
        private val DEFAULT_SENTENCES = listOf(
            "The quick brown fox jumps over the lazy dog",
            "I enjoy learning English every day",
            "Practice makes perfect",
            "A journey of a thousand miles begins with a single step",
            "The early bird catches the worm",
            "Where there's a will, there's a way",
            "Actions speak louder than words",
            "Knowledge is power",
            "Time flies when you're having fun",
            "Every cloud has a silver lining"
        )
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
