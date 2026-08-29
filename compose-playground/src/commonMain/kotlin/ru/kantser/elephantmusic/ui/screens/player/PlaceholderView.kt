package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as Geo
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G

/**
 * Заглушки разделов прибора: видео/фото/текст/запись/другие функции/настройки.
 * Работает только кнопка M (вернуться в меню) — настраивается в PlayerScreen.
 * Позиционируется в светлой зоне экрана; обычный Compose UI.
 */
@Composable
internal fun PlaceholderView(mode: ScreenMode) {
    val s = Geo.Screen
    val contentH = s.LightH - s.TopBarH
    Box(
        Modifier
            .offset(x = s.LightX.dp, y = (s.LightY + s.TopBarH).dp)
            .width(s.LightW.dp)
            .height(contentH.dp)
            .clipToBounds()
            .padding(10.dp),
    ) {
        Column {
            when (mode) {
                ScreenMode.VIDEO -> VideoPlaceholder()
                ScreenMode.PHOTO -> StubText("Фото", "🖼  Просмотр изображений\n(раздел в разработке)")
                ScreenMode.TEXT -> StubText("Текст", LOREM)
                ScreenMode.RECORD -> StubText("Запись", "🎙  Диктофон\n(раздел в разработке)")
                ScreenMode.OTHER -> StubText("Другие функции", "Раздел в разработке")
                ScreenMode.SETTINGS -> StubText("Настройки", "Раздел в разработке")
                else -> Unit
            }
        }
    }
}

/** Минималистичный плеер-заглушка. */
@Composable
private fun VideoPlaceholder() {
    StubTitle("Видео")
    Spacer(Modifier.height(8.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF101820)),
        contentAlignment = Alignment.Center,
    ) {
        Text("▶", color = Color.White, fontSize = 26.sp)
    }
    Spacer(Modifier.height(6.dp))
    Text("Видеоплеер", color = G.MenuText, fontSize = 10.sp)
    Text("(раздел в разработке)", color = G.MenuText.copy(alpha = 0.6f), fontSize = 9.sp)
}

@Composable
private fun StubText(title: String, body: String) {
    StubTitle(title)
    Spacer(Modifier.height(6.dp))
    Text(body, color = G.MenuText, fontSize = 10.sp, lineHeight = 14.sp)
}

@Composable
private fun StubTitle(title: String) {
    Text(
        title,
        color = G.MenuText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
    )
}

private val LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
    "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
    "Ut enim ad minim veniam, quis nostrud exercitation."
