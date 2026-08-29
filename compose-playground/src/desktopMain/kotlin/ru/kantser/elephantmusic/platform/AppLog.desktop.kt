package ru.kantser.elephantmusic.platform

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

/**
 * java.util.logging.
 *  - файл: %USERPROFILE%\.ElephantPlayer\logs\app.log
 *  - консоль IDEA (окно Run) — на уровне FINE, чтобы debug-сообщения тоже были видны.
 * Формат каждой строки включает вызывающий класс/метод/строку (автоматически по стеку).
 *
 * Уровень консоли можно менять системным свойством
 * `-Delephant.log.console=INFO` (поднимется до INFO, спрячет debug).
 */
private class JvmAppLog : AppLog {

    private val log: Logger = createLogger()

    override fun d(tag: String, message: String) = log.log(Level.FINE, "[$tag] $message")
    override fun i(tag: String, message: String) = log.log(Level.INFO, "[$tag] $message")
    override fun w(tag: String, message: String) = log.log(Level.WARNING, "[$tag] $message")
    override fun e(tag: String, message: String) = log.log(Level.SEVERE, "[$tag] $message")

    companion object {
        // JUL-логгеры кешируются по имени, а createAppLog() вызывается в нескольких местах,
        // поэтому хендлеры настраиваем ровно один раз, иначе консоль/файл дублировались бы.
        private val logger: Logger by lazy {
            configure()
        }

        private fun createLogger(): Logger = logger

        private fun configure(): Logger {
            val log = Logger.getLogger("ElephantMusic")
            // Запрещаем наследовать root-хендлеры, иначе сообщение печаталось бы дважды
            // и с чужим форматтером.
            log.useParentHandlers = false

            val consoleLevel = System.getProperty("elephant.log.console")
                ?.let { runCatching { Level.parse(it) }.getOrNull() }
                ?: Level.FINE

            try {
                val logsDir = File(settingsFolderPath(), "logs").apply { mkdirs() }
                val fileHandler = FileHandler(
                    File(logsDir, "app.log").absolutePath,
                    1024 * 1024,
                    1,
                    true,
                ).apply {
                    level = Level.ALL
                    formatter = RichFormatter()
                }
                log.addHandler(fileHandler)
            } catch (e: Exception) {
                // Если логи нельзя писать в файл — пишем хотя бы в консоль.
                System.err.println("AppLog: не удалось инициализировать файловый лог: $e")
            }

            val consoleHandler = ConsoleHandler().apply {
                level = consoleLevel
                formatter = RichFormatter()
            }
            log.addHandler(consoleHandler)
            return log
        }
    }
}

/**
 * Формат строки (в консоль и в файл одинаковый):
 * `HH:mm:ss.SSS LEVEL [ru.kantser.elephantmusic.Class:method:line] message`
 *
 * Класс/метод/строка берутся из верхнего кадра стека, не принадлежащего платформе логирования.
 */
private class RichFormatter : Formatter() {
    private val fmt = SimpleDateFormat("HH:mm:ss.SSS")

    override fun format(record: LogRecord): String {
        val time = fmt.format(Date(record.millis))
        val caller = findCaller(record)
        val msg = formatMessage(record)
        return String.format("%s %-6s %-45s %s%n", time, record.level.name, caller, msg)
    }

    /** Ищем реального вызывающего: пропускаем java.util.logging и саму обёртку JvmAppLog. */
    private fun findCaller(record: LogRecord): String {
        val skip = { cls: String ->
            cls.startsWith("java.util.logging") ||
                cls == JvmAppLog::class.java.name ||
                cls == this::class.java.name
        }
        val stack = record.thrown?.stackTrace ?: Throwable().stackTrace
        for (el in stack) {
            if (skip(el.className)) continue
            return "${el.className.substringAfterLast('.')}:${el.methodName}:${el.lineNumber}"
        }
        return "?"
    }
}

actual fun createAppLog(): AppLog = JvmAppLog()
