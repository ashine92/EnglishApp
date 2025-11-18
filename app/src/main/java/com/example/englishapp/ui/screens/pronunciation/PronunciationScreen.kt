package com.example.englishapp.ui.screens.pronunciation

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PronunciationScreen(
    onNavigateBack: () -> Unit,
    viewModel: PronunciationViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentWord by viewModel.currentWord.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val microphoneState by viewModel.microphoneState.collectAsState()

    // Text-to-Speech
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    
    // Initialize TTS
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    // Cleanup TTS
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Speech Recognizer
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening(context, viewModel, speechRecognizer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Luyện Phát Âm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current Word Section
            if (currentWord != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Từ vựng cần luyện",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = currentWord!!.word,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        currentWord!!.phonetic?.let { phonetic ->
                            Text(
                                text = phonetic,
                                fontSize = 18.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        // Only show Vietnamese meaning in practice card, not in results
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = currentWord!!.meaning,
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // TTS Speaker Button
                        IconButton(
                            onClick = {
                                tts?.speak(currentWord!!.word, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Play pronunciation",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            } else if (uiState is PronunciationUiState.Error) {
                // Show error if no vocabulary available
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (uiState as PronunciationUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Microphone Button
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = when (microphoneState) {
                            MicrophoneState.IDLE -> MaterialTheme.colorScheme.primary
                            MicrophoneState.LISTENING -> Color(0xFFE53935) // Red
                            MicrophoneState.PROCESSING -> MaterialTheme.colorScheme.secondary
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        when (microphoneState) {
                            MicrophoneState.IDLE -> {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                            MicrophoneState.LISTENING -> {
                                stopListening(speechRecognizer, viewModel)
                            }
                            MicrophoneState.PROCESSING -> {
                                // Do nothing while processing
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = when (microphoneState) {
                            MicrophoneState.IDLE -> Icons.Default.Mic
                            MicrophoneState.LISTENING -> Icons.Default.Stop
                            MicrophoneState.PROCESSING -> Icons.Default.Refresh
                        },
                        contentDescription = "Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Text(
                text = when (microphoneState) {
                    MicrophoneState.IDLE -> "Tap to record"
                    MicrophoneState.LISTENING -> "Listening..."
                    MicrophoneState.PROCESSING -> "Processing..."
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            // User Speech Result
            Spacer(modifier = Modifier.height(24.dp))
            
            if (recognizedText.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "You said:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = recognizedText,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Button
            if (recognizedText.isNotBlank() && uiState is PronunciationUiState.Idle) {
                Button(
                    onClick = { viewModel.scorePronunciation() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Score My Pronunciation")
                }
            }

            // Results Display
            when (val state = uiState) {
                is PronunciationUiState.Scoring -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is PronunciationUiState.Success -> {
                    PronunciationResultCard(state.result, viewModel)
                }
                is PronunciationUiState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {}
            }
        }
    }

    // Initialize Speech Recognizer
    DisposableEffect(Unit) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(createRecognitionListener(viewModel))
        
        onDispose {
            speechRecognizer?.destroy()
        }
    }
}

@Composable
fun PronunciationResultCard(
    result: com.example.englishapp.domain.model.PronunciationResult,
    viewModel: PronunciationViewModel
) {
    val currentWord = viewModel.currentWord.collectAsState().value
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Target Word - English only
            currentWord?.let { vocab ->
                Text(
                    text = "Target Word",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vocab.word,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                vocab.phonetic?.let { phonetic ->
                    Text(
                        text = phonetic,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Score
            Text(
                text = "Score",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${result.score}/100",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    result.score >= 90 -> Color(0xFF4CAF50) // Green
                    result.score >= 75 -> Color(0xFF8BC34A) // Light Green
                    result.score >= 60 -> Color(0xFFFFC107) // Amber
                    else -> Color(0xFFF44336) // Red
                }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Similarity
            Text(
                text = "Similarity: ${result.similarity}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mistakes
            if (result.mistakes.isNotEmpty()) {
                Text(
                    text = "Mistakes:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                result.mistakes.forEach { mistake ->
                    Text(
                        text = "• $mistake",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feedback
            Text(
                text = "Feedback:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = result.feedback,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Selection Button
            Button(
                onClick = { viewModel.clearCurrentWord(); onNavigateBack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chọn từ khác")
            }
        }
    }
}

private fun startListening(
    context: android.content.Context,
    viewModel: PronunciationViewModel,
    speechRecognizer: SpeechRecognizer?
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }
    
    viewModel.setMicrophoneState(MicrophoneState.LISTENING)
    speechRecognizer?.startListening(intent)
}

private fun stopListening(
    speechRecognizer: SpeechRecognizer?,
    viewModel: PronunciationViewModel
) {
    speechRecognizer?.stopListening()
    viewModel.setMicrophoneState(MicrophoneState.PROCESSING)
}

private fun createRecognitionListener(viewModel: PronunciationViewModel) = object : RecognitionListener {
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        viewModel.setMicrophoneState(MicrophoneState.PROCESSING)
    }

    override fun onError(error: Int) {
        viewModel.setMicrophoneState(MicrophoneState.IDLE)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            viewModel.setRecognizedText(matches[0])
        }
        viewModel.setMicrophoneState(MicrophoneState.IDLE)
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
