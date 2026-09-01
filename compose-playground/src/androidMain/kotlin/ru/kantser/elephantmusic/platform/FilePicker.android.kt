package ru.kantser.elephantmusic.platform

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "flac", "ogg", "m4a", "aac")

object FilePickerHolder {
    private lateinit var filesLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var treeLauncher: ActivityResultLauncher<Uri?>
    private var filesDeferred: CompletableDeferred<List<String>>? = null
    private var treeDeferred: CompletableDeferred<Uri?>? = null

    fun init(activity: ComponentActivity) {
        filesLauncher = activity.registerForActivityResult(
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
            filesDeferred?.complete(files)
            filesDeferred = null
        }

        treeLauncher = activity.registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree(),
        ) { uri ->
            if (uri != null) {
                try {
                    AppContextHolder.context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                } catch (_: Exception) {
                }
            }
            treeDeferred?.complete(uri)
            treeDeferred = null
        }
    }

    suspend fun pickAudioFiles(): List<String> {
        val deferred = CompletableDeferred<List<String>>()
        filesDeferred = deferred
        filesLauncher.launch(AUDIO_MIME_TYPES)
        return deferred.await()
    }

    /** Открывает выбор папки (SAF tree) и возвращает найденные аудиофайлы (content:// URI). */
    suspend fun pickAudioFolder(): List<String> {
        val deferred = CompletableDeferred<Uri?>()
        treeDeferred = deferred
        treeLauncher.launch(null)
        val treeUri = deferred.await() ?: return emptyList()
        return withContext(Dispatchers.IO) {
            val root = DocumentFile.fromTreeUri(AppContextHolder.context, treeUri)
                ?: return@withContext emptyList()
            val out = mutableListOf<String>()
            collectAudio(root, out, 0)
            out
        }
    }

    private fun collectAudio(dir: DocumentFile, out: MutableList<String>, depth: Int) {
        if (depth > 24) return
        dir.listFiles().forEach { f ->
            if (f.isDirectory) {
                collectAudio(f, out, depth + 1)
            } else if (f.isFile) {
                val ext = f.name?.substringAfterLast('.', "")?.lowercase()
                if (ext in AUDIO_EXTENSIONS) out.add(f.uri.toString())
            }
        }
    }
}

actual suspend fun pickAudioFiles(): List<String> = FilePickerHolder.pickAudioFiles()

actual suspend fun pickAudioFolder(): List<String> = FilePickerHolder.pickAudioFolder()