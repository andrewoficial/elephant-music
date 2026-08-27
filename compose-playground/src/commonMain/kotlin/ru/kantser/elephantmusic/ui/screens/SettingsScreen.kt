package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.domain.repository.SettingsRepository

@Composable
fun SettingsScreen() {
    val settingsRepository: SettingsRepository = koinInject()
    val settings = remember { settingsRepository.load() }

    var language by remember { mutableStateOf(settings.language) }
    var volume by remember { mutableStateOf(settings.volume) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Настройки", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = language,
            onValueChange = {
                language = it
                settingsRepository.save(settingsRepository.load().copy(language = it))
            },
            label = { Text("Язык") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Громкость")
            Spacer(Modifier.width(12.dp))
            Slider(
                value = volume.toFloat(),
                onValueChange = { v ->
                    volume = v.toDouble()
                    settingsRepository.save(settingsRepository.load().copy(volume = volume))
                },
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            "Заготовка настроек: язык и громкость сохраняются автоматически.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}
