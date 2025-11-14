package com.example.englishapp.ui.screens.flashcard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeckDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit
) {
    var deckName by remember { mutableStateOf("") }
    var deckDescription by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Deck") },
        text = {
            Column {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = {
                        deckName = it
                        showError = false
                    },
                    label = { Text("Deck Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError
                )

                if (showError) {
                    Text(
                        text = "Deck name is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = deckDescription,
                    onValueChange = { deckDescription = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (deckName.isBlank()) {
                        showError = true
                    } else {
                        onCreate(
                            deckName.trim(),
                            deckDescription.trim().ifBlank { null }
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}