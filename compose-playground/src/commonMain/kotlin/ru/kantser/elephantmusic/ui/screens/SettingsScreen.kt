package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import ru.kantser.elephantmusic.platform.openFolder
import ru.kantser.elephantmusic.platform.settingsFolderPath

@Composable
fun SettingsScreen() {
    val settingsRepository: SettingsRepository = koinInject()
    val settings = remember { settingsRepository.load() }

    var language by remember { mutableStateOf(settings.language) }
    var auddToken by remember { mutableStateOf(settings.auddToken ?: "") }
    var acrAccessKey by remember { mutableStateOf(settings.acrAccessKey ?: "") }
    var acrAccessSecret by remember { mutableStateOf(settings.acrAccessSecret ?: "") }
    var acrHost by remember { mutableStateOf(settings.acrHost ?: "") }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    val folderPath = remember { settingsFolderPath() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Настройки", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = language,
            onValueChange = { language = it },
            label = { Text("Язык") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { openFolder(folderPath) }) {
                Text("Открыть папку с настройками")
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Папка: $folderPath",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }

        HorizontalDivider()
        Text(
            "Ключи сервисов распознавания треков",
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = auddToken,
            onValueChange = { auddToken = it },
            label = { Text("AudD API-ключ") },
            placeholder = { Text("api_token с audd.io") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = acrAccessKey,
            onValueChange = { acrAccessKey = it },
            label = { Text("ACRCloud access_key") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = acrAccessSecret,
            onValueChange = { acrAccessSecret = it },
            label = { Text("ACRCloud access_secret") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = acrHost,
            onValueChange = { acrHost = it },
            label = { Text("ACRCloud host") },
            placeholder = { Text("identify-eu-west-1.acrcloud.com") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            "Ключи применяются после нажатия «Сохранить» и используются при определении трека.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )

        Button(
            onClick = {
                settingsRepository.save(
                    settingsRepository.load().copy(
                        language = language,
                        auddToken = auddToken.trim().ifEmpty { null },
                        acrAccessKey = acrAccessKey.trim().ifEmpty { null },
                        acrAccessSecret = acrAccessSecret.trim().ifEmpty { null },
                        acrHost = acrHost.trim().ifEmpty { null },
                    ),
                )
                savedMessage = "Настройки сохранены."
            },
        ) {
            Text("Сохранить")
        }

        Spacer(Modifier.height(4.dp))

        savedMessage?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
