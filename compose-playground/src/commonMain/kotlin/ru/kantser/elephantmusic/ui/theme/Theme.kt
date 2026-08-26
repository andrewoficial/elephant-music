package ru.kantser.elephantmusic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Цвета из JavaFX-версии (src/main/resources/.../view/styles/main.css)
val AppOrange = Color(0xFFFFAA44)      // активная кнопка / selected / toggle
val AppOrangeLight = Color(0xFFFFB761) // focus
val AppSidebar = Color(0xFFF0F0F0)     // фон сайдбара
val AppButton = Color(0xFFE0E0E0)      // фон кнопки
val AppButtonHover = Color(0xFFD0D0D0)
val AppButtonPressed = Color(0xFFC0C0C0)
val AppText = Color(0xFF333333)
val LastFmGreen = Color(0xFF1DB954)
val LastFmRed = Color(0xFFFF6B6B)

private val LightColors = lightColorScheme(
    primary = AppOrange,
    onPrimary = Color.White,
    secondary = AppOrangeLight,
    background = Color.White,
    onBackground = AppText,
    surface = Color.White,
    onSurface = AppText,
    surfaceVariant = AppButton,
    onSurfaceVariant = AppText,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
