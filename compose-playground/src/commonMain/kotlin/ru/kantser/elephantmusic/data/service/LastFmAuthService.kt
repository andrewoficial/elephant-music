package ru.kantser.elephantmusic.data.service

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.kantser.elephantmusic.domain.repository.SettingsRepository
import java.security.MessageDigest

private val lastFmJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class LastFmResponse(
    val token: String? = null,
    val session: LastFmSession? = null,
    val error: Int? = null,
    val message: String? = null,
)

@Serializable
private data class LastFmSession(
    val key: String? = null,
    val name: String? = null,
)

class LastFmAuthService(
    private val client: HttpClient,
    private val settingsRepository: SettingsRepository,
) {
    private val apiKey = "c16c73bb3c2f5f792df77fa4f0740d8d"
    private val apiSecret = "7a7c8515b163a720f71443123ec1c421"
    private val baseUrl = "http://ws.audioscrobbler.com/2.0/"

    companion object {
        const val AUTH_URL = "https://www.last.fm/api/auth/"

        fun authUrl(key: String, token: String): String =
            "$AUTH_URL?api_key=$key&token=$token"
    }

    fun authPageUrl(token: String): String = authUrl(apiKey, token)

    suspend fun getAuthToken(): String? {
        val params = mapOf(
            "method" to "auth.getToken",
            "api_key" to apiKey,
            "format" to "json",
        )
        val signed = params + ("api_sig" to signature(params))
        val body = post(signed) ?: return null
        return try {
            lastFmJson.decodeFromString<LastFmResponse>(body).token
        } catch (e: Exception) {
            null
        }
    }

    suspend fun authenticate(token: String): Boolean {
        val params = mapOf(
            "method" to "auth.getSession",
            "api_key" to apiKey,
            "token" to token,
            "format" to "json",
        )
        val signed = params + ("api_sig" to signature(params))
        val body = post(signed) ?: return false
        val parsed = try {
            lastFmJson.decodeFromString<LastFmResponse>(body)
        } catch (e: Exception) {
            return false
        }
        val sessionKey = parsed.session?.key
        val username = parsed.session?.name
        if (sessionKey.isNullOrEmpty()) return false
        val settings = settingsRepository.load()
        settingsRepository.save(
            settings.copy(
                lastFmToken = sessionKey,
                lastFmName = username ?: settings.lastFmName,
            ),
        )
        return true
    }

    suspend fun logout() {
        val settings = settingsRepository.load()
        settingsRepository.save(
            settings.copy(
                lastFmToken = "NULL",
                lastFmName = "Anonim",
                language = "LogOut",
            ),
        )
    }

    suspend fun isAuthenticated(): Boolean =
        settingsRepository.load().lastFmToken.let { it.isNotBlank() && it != "NULL" }

    private fun signature(params: Map<String, String>): String {
        val input = params.entries.sortedBy { it.key }
            .joinToString("") { "${it.key}${it.value}" } + apiSecret
        return md5(input)
    }

    private suspend fun post(params: Map<String, String>): String? = try {
        withContext(Dispatchers.IO) {
            val response = client.submitForm(
                url = baseUrl,
                formParameters = Parameters.build {
                    params.forEach { (k, v) -> append(k, v) }
                },
            )
            response.bodyAsText()
        }
    } catch (e: Exception) {
        null
    }

    private fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
