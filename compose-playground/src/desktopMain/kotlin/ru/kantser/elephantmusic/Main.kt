package ru.kantser.elephantmusic

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import ru.kantser.elephantmusic.di.appModule
import ru.kantser.elephantmusic.ui.App

fun main() {
    startKoin { modules(appModule) }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ElephantMusic",
        ) {
            App()
        }
    }
}
