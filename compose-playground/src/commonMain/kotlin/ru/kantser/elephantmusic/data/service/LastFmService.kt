package ru.kantser.elephantmusic.data.service

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kantser.elephantmusic.domain.model.Track
import ru.kantser.elephantmusic.domain.repository.SettingsRepository
import java.security.MessageDigest

class LastFmService(
    private val client: HttpClient,
    private val settingsRepository: SettingsRepository,
) {
    private val apiKey = "c16c73bb3c2f5f792df77fa4f0740d8d"
    private val apiSecret = "7a7c8515b163a720f71443123ec1c421"
    private val baseUrl = "http://ws.audioscrobbler.com/2.0/"

    suspend fun isAuthenticated(): Boolean =
        settingsRepository.load().lastFmToken.let { it.isNotBlank() && it != "NULL" }

    suspend fun updateNowPlaying(track: Track) {
        val token = settingsRepository.load().lastFmToken
        if (token.isBlank() || token == "NULL") return
        if (settingsRepository.load().activeScrobbling.not()) return

        val params = mutableMapOf(
            "method" to "track.updateNowPlaying",
            "api_key" to apiKey,
            "sk" to token,
            "artist" to track.artist,
            "track" to track.title,
            "format" to "json",
        )
        sign(params)
        send(params)
    }

    suspend fun scrobble(track: Track, playedSeconds: Double, durationSeconds: Double) {
        val token = settingsRepository.load().lastFmToken
        if (token.isBlank() || token == "NULL") return
        if (settingsRepository.load().activeScrobbling.not()) return

        if (playedSeconds < 240 && (durationSeconds <= 0 || playedSeconds / durationSeconds < 0.5)) return

        val params = mutableMapOf(
            "method" to "track.scrobble",
            "api_key" to apiKey,
            "sk" to token,
            "artist" to track.artist,
            "track" to track.title,
            "timestamp" to (System.currentTimeMillis() / 1000).toString(),
            "format" to "json",
        )
        sign(params)
        send(params)
    }

    private suspend fun sign(params: MutableMap<String, String>) {
        val signatureInput = params.entries.sortedBy { it.key }
            .joinToString("") { "${it.key}${it.value}" } + apiSecret
        params["api_sig"] = md5(signatureInput)
    }

    private suspend fun send(params: Map<String, String>) {
        try {
            withContext(Dispatchers.IO) {
                client.submitForm(
                    url = baseUrl,
                    formParameters = Parameters.build {
                        params.forEach { (k, v) -> append(k, v) }
                    },
                )
            }
        } catch (e: Exception) {
            // сеть недоступна — молча пропускаем
        }
    }

    private fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
