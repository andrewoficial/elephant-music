package ru.kantser.elephantmusic.platform

import android.util.Log

/** android.util.Log (возвращает Int в Android, приводим к Unit). */
private class AndroidAppLog : AppLog {
    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun e(tag: String, message: String) {
        Log.e(tag, message)
    }
}

actual fun createAppLog(): AppLog = AndroidAppLog()
