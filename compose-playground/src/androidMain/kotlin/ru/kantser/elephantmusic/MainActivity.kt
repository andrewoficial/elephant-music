package ru.kantser.elephantmusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import ru.kantser.elephantmusic.platform.FilePickerHolder
import ru.kantser.elephantmusic.ui.App

class MainActivity : ComponentActivity() {
    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FilePickerHolder.init(this)
        setContent {
            App()
        }
        requestStoragePermissionIfNeeded()
    }

    private fun requestStoragePermissionIfNeeded() {
        val prefs = getSharedPreferences("elephant_player", MODE_PRIVATE)
        if (prefs.getBoolean("permission_requested", false)) return
        prefs.edit().putBoolean("permission_requested", true).apply()

        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestIfMissing(permission)
        requestIfMissing(Manifest.permission.RECORD_AUDIO)
    }

    private fun requestIfMissing(permission: String) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionRequest.launch(permission)
        }
    }
}
