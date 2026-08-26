package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var auddToken by remember { mutableStateOf("") }
    var acrAccessKey by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Настройки", style = MaterialTheme.typography.titleLarge)
        Text("Ключи сервисов распознавания треков", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = auddToken,
            onValueChange = { auddToken = it },
            label = { Text("AudD API-ключ") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = acrAccessKey,
            onValueChange = { acrAccessKey = it },
            label = { Text("ACRCloud access_key") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(onClick = {}) { Text("Сохранить") }
    }
}
