package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.data.service.LastFmAuthService
import ru.kantser.elephantmusic.domain.repository.SettingsRepository
import ru.kantser.elephantmusic.platform.openBrowser
import ru.kantser.elephantmusic.ui.theme.LastFmGreen
import ru.kantser.elephantmusic.ui.theme.LastFmRed

private sealed interface AuthStatus {
    data object Loading : AuthStatus
    data class Authed(val name: String) : AuthStatus
    data object Anon : AuthStatus
}

@Composable
fun LastFmScreen() {
    val authService: LastFmAuthService = koinInject()
    val settingsRepository: SettingsRepository = koinInject()
    val scope = rememberCoroutineScope()

    val settings = remember { settingsRepository.load() }
    var scrobbling by remember { mutableStateOf(settings.activeScrobbling) }
    var status by remember { mutableStateOf<AuthStatus>(AuthStatus.Loading) }
    var authToken by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    var initialized by remember { mutableStateOf(false) }
    if (!initialized) {
        initialized = true
        scope.launch {
            status = if (authService.isAuthenticated()) {
                AuthStatus.Authed(settingsRepository.load().lastFmName)
            } else {
                AuthStatus.Anon
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Last.fm интеграция", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = scrobbling,
                enabled = status is AuthStatus.Authed,
                onCheckedChange = { enabled ->
                    scrobbling = enabled
                    settingsRepository.save(
                        settingsRepository.load().copy(activeScrobbling = enabled),
                    )
                },
            )
            Spacer(Modifier.width(8.dp))
            Text("Передавать данные в Last.fm")
        }

        when (val s = status) {
            is AuthStatus.Loading -> Text("Проверка статуса...")
            is AuthStatus.Anon -> {
                Text(
                    "Не авторизован",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Button(
                    onClick = {
                        message = null
                        scope.launch {
                            val token = authService.getAuthToken()
                            authToken = token
                            if (token == null) {
                                message = "Не удалось получить токен авторизации."
                            } else {
                                val url = authService.authPageUrl(token)
                                openBrowser(url)
                                message = "Выполните вход в Last.fm в открывшемся браузере, " +
                                    "затем нажмите «Продолжить»."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LastFmGreen),
                ) {
                    Text("Войти")
                }

                if (authToken != null) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val ok = authService.authenticate(authToken!!)
                                if (ok) {
                                    status = AuthStatus.Authed(
                                        settingsRepository.load().lastFmName,
                                    )
                                    scrobbling = settingsRepository.load().activeScrobbling
                                    message = null
                                } else {
                                    message = "Не удалось завершить авторизацию."
                                }
                            }
                        },
                    ) {
                        Text("Я авторизовал(а). Продолжить")
                    }
                }
            }
            is AuthStatus.Authed -> {
                Text(
                    "Авторизован как: ${s.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LastFmGreen,
                )
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            authService.logout()
                            status = AuthStatus.Anon
                            authToken = null
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LastFmRed,
                    ),
                ) {
                    Text("Выйти")
                }
            }
        }

        message?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}
