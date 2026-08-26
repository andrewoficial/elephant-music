package ru.kantser.elephantmusic

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.kantser.elephantmusic.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ElephantMusic",
    ) {
        App()
    }
}
