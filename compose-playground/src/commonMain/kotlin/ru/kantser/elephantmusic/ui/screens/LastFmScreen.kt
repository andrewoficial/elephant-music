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
import androidx.compose.material3.Switch
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
fun LastFmScreen() {
    val settingsRepository: SettingsRepository = koinInject()

    val settings = remember { settingsRepository.load() }
    var scrobbling by remember { mutableStateOf(settings.activeScrobbling) }
    var token by remember { mutableStateOf(settings.lastFmToken) }
    var name by remember { mutableStateOf(settings.lastFmName) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Last.fm интеграция", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = scrobbling,
                onCheckedChange = {
                    scrobbling = it
                    settingsRepository.save(settingsRepository.load().copy(activeScrobbling = it))
                },
            )
            Spacer(Modifier.width(8.dp))
            Text("Передавать данные в Last.fm")
        }

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                settingsRepository.save(settingsRepository.load().copy(lastFmName = it))
            },
            label = { Text("Имя пользователя") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = token,
            onValueChange = {
                token = it
                settingsRepository.save(settingsRepository.load().copy(lastFmToken = it))
            },
            label = { Text("Last.fm session key (sk)") },
            modifier = Modifier.fillMaxWidth(),
        )

        val authenticated = token.isNotBlank() && token != "NULL"
        Text(
            text = if (authenticated) "Авторизован: $name" else "Не авторизован",
            style = MaterialTheme.typography.bodyMedium,
            color = if (authenticated) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}
