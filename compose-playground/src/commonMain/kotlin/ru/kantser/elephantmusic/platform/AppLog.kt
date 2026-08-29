package ru.kantser.elephantmusic.platform

/**
 * Кросс-платформенный логгер-фасад для отладки.
 * Desktop: java.util.logging -> файл %USERPROFILE%\.ElephantPlayer\logs\app.log (+ консоль).
 * Android: android.util.Log.
 */
interface AppLog {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String)
}

/** Фабрика платформенной реализации. */
expect fun createAppLog(): AppLog
