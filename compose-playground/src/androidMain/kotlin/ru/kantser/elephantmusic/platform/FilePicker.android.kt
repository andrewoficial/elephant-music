package ru.kantser.elephantmusic.platform

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CompletableDeferred
import ru.kantser.elephantmusic.AppContextHolder

private val AUDIO_MIME_TYPES = arrayOf(
    "audio/*",
    "audio/mpeg",
    "audio/x-wav",
    "audio/wav",
    "audio/flac",
    "application/ogg",
    "audio/ogg",
    "audio/x-m4a",
    "audio/mp4",
    "audio/aac",
)

object FilePickerHolder {
    private lateinit var launcher: ActivityResultLauncher<Array<String>>
    private var current: CompletableDeferred<List<String>>? = null

    fun init(activity: ComponentActivity) {
        launcher = activity.registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris ->
            val files = uris.mapNotNull { uri ->
                try {
                    AppContextHolder.context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                } catch (_: Exception) {
                }
                uri.toString()
            }
            current?.complete(files)
            current = null
        }
    }

    suspend fun pickAudioFiles(): List<String> {
        val deferred = CompletableDeferred<List<String>>()
        current = deferred
        launcher.launch(AUDIO_MIME_TYPES)
        return deferred.await()
    }
}

actual suspend fun pickAudioFiles(): List<String> = FilePickerHolder.pickAudioFiles()
