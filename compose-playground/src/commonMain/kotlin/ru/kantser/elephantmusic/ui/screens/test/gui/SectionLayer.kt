package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import ru.kantser.elephantmusic.ui.screens.player.ScreenMode
import ru.kantser.elephantmusic.ui.screens.player.ScreenState
import ru.kantser.elephantmusic.ui.screens.player.VISIBLE_LIST_ROWS
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as PlayerGeo

/** Данные «сейчас играет», нужные только для отрисовки (без привязки к PlayerController). */
data class PlaybackUi(
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val position: Double = 0.0,
    val duration: Double = 0.0,
    val levels: List<Float> = emptyList(),
) {
    val progress: Float
        get() = if (duration > 0) (position / duration).toFloat().coerceIn(0f, 1f) else 0f
}

/** Данные раздела «Отладка эмулятора»: заголовок и строки отладочной информации. */
data class DebugUi(
    val title: String = "Отладка SVG",
    val lines: List<String> = emptyList(),
)

/**
 * Контент разделов экрана (всё, кроме главного меню): список треков (Файлы), «сейчас играет»
 * (Музыка) и заглушки видео/фото/текст/запись/другие/настройки. Рисуется примитивами DrawScope
 * в светлой зоне под шапкой — как и меню, в координатах рамки дисплея.
 */
internal class SectionLayer(
    private val measurer: TextMeasurer,
    private val st: ScreenState,
    private val playback: PlaybackUi,
    private val debug: DebugUi = DebugUi(),
    private val marqueeOffset: Float = 0f,
) : DeviceLayer {
    override fun draw(scope: DrawScope) = with(scope) {
        when (st.mode) {
            ScreenMode.LIST -> drawTrackList(measurer, st)
            ScreenMode.NOW -> drawNowPlaying(measurer, playback, marqueeOffset)
            ScreenMode.EMULATOR -> drawEmulator(measurer, debug)
            ScreenMode.VIDEO -> drawVideoStub(measurer)
            ScreenMode.PHOTO -> drawStub(measurer, "Фото", "Просмотр изображений", wrap = false)
            ScreenMode.TEXT -> drawStub(measurer, "Текст", LOREM, wrap = true)
            ScreenMode.RECORD -> drawStub(measurer, "Запись", "Диктофон", wrap = false)
            ScreenMode.OTHER -> drawStub(measurer, "Другие функции", "Раздел в разработке", wrap = false)
            ScreenMode.SETTINGS -> drawStub(measurer, "Настройки", "Раздел в разработке", wrap = false)
            ScreenMode.HOME -> Unit // главное меню рисует MenuListLayer
        }
    }
}

/**
 * Полноэкранная область контента раздела под шапкой (device-координаты):
 * открытый раздел занимает всю ширину экрана (без синей панели).
 */
internal class ContentArea {
    val origin = PlayerGeo.DisplayFrame.topLeft
    val sc = PlayerGeo.Screen
    val left = origin.x + sc.LightX
    val top = origin.y + sc.LightY + sc.TopBarH
    val width = FullContentWidth
    val height = sc.LightH - sc.TopBarH
    val right = left + width
    val bottom = top + height
}

private fun DrawScope.drawTrackList(measurer: TextMeasurer, st: ScreenState) {
    val a = ContentArea()
    val tracks = st.tracks
    val textCol = G.MenuText
    val selBg = G.SelectedDark

    clipRect(left = a.left, top = a.top, right = a.right, bottom = a.bottom) {
        if (tracks.isEmpty()) {
            drawText(
                textLayoutResult = measurer.measure("Список пуст", TextStyle(color = textCol, fontSize = 11.sp)),
                topLeft = Offset(a.left + 10f, a.top + 10f),
            )
            return@clipRect
        }

        val rowH = 20f
        val start = st.listScroll.coerceIn(0, (tracks.size - VISIBLE_LIST_ROWS).coerceAtLeast(0))
        val end = minOf(start + VISIBLE_LIST_ROWS, tracks.size)

        for (i in start until end) {
            val sel = i == st.trackSel
            val y = a.top + (i - start) * rowH
            if (sel) {
                drawRoundRect(selBg, Offset(a.left, y), Size(a.width, rowH), CornerRadius(2f))
            }
            drawText(
                textLayoutResult = measurer.measure(
                    tracks[i].title,
                    TextStyle(color = if (sel) Color(0xFF0c2a3f) else textCol, fontSize = 10.5.sp),
                ),
                topLeft = Offset(a.left + 6f, y),
            )
        }
    }
}

private fun DrawScope.drawNowPlaying(measurer: TextMeasurer, pb: PlaybackUi, marqueeOffset: Float = 0f) {
    val a = ContentArea()
    val textCol = G.MenuText
    val baseX = a.left + 10f
    var y = a.top + 6f

    val status = if (pb.isPlaying) "PLAY ▶" else "PAUSE ❚❚"
    drawText(
        textLayoutResult = measurer.measure(status, TextStyle(color = Color(0xFF165d3a), fontSize = 11.sp)),
        topLeft = Offset(baseX, y),
    )
    y += 16f

    // Название: если шире области — бегущая строка (marquee), иначе статичный текст.
    val titleText = pb.title.ifEmpty { "—" }
    val titleStyle = TextStyle(color = textCol, fontSize = 13.sp)
    val titleLayout = measurer.measure(titleText, titleStyle)
    val availW = a.right - a.left - 20f
    if (titleLayout.size.width > availW) {
        val gap = titleLayout.size.width * 0.25f
        val total = titleLayout.size.width + gap
        val off = marqueeOffset % total
        clipRect(left = a.left, top = y, right = a.left + availW, bottom = y + titleLayout.size.height) {
            drawText(titleLayout, topLeft = Offset(baseX - off, y))
            drawText(titleLayout, topLeft = Offset(baseX - off + total, y))
        }
    } else {
        drawText(titleLayout, topLeft = Offset(baseX, y))
    }
    y += 17f

    drawText(
        textLayoutResult = measurer.measure(pb.artist, TextStyle(color = textCol, fontSize = 10.sp)),
        topLeft = Offset(baseX, y),
    )
    y += 24f

    // Прогресс воспроизведения.
    val barW = 180f
    val barH = 4f
    val barX = a.left + (a.width - barW) / 2f
    drawRoundRect(Color.Black.copy(alpha = 0.2f), Offset(barX, y), Size(barW, barH), CornerRadius(2f))
    if (pb.progress > 0f) {
        drawRoundRect(Color(0xFF0a3a70), Offset(barX, y), Size(barW * pb.progress, barH), CornerRadius(2f))
    }
    y += 12f

    drawText(
        textLayoutResult = measurer.measure(
            "${fmtTime(pb.position)} / ${fmtTime(pb.duration)}",
            TextStyle(color = textCol.copy(alpha = 0.7f), fontSize = 9.sp),
        ),
        topLeft = Offset(barX, y),
    )
    y += 18f

    // Визуальный ряд управления (реальные команды — на кнопках прибора).
    val transport = "⏮   ${if (pb.isPlaying) "❚❚" else "▶"}   ⏭"
    drawText(
        textLayoutResult = measurer.measure(transport, TextStyle(color = textCol, fontSize = 14.sp)),
        topLeft = Offset(barX, y),
    )
    y += 24f

    // Маленький белый «эквалайзер»: тонкие полоски, амплитуда — из реального аудио (pb.levels).
    val src = pb.levels
    val n = if (src.isEmpty()) 12 else minOf(src.size, 24)
    val slot = 8f
    val barW2 = 3f
    val gap2 = 5f
    val maxH = 12f
    val stripW = n * slot - gap2
    val x0 = a.left + (a.width - stripW) / 2f
    for (i in 0 until n) {
        val lvl = if (src.isEmpty()) 0f else src.getOrElse(i) { 0f }
        val h = (1f + lvl.coerceIn(0f, 1f) * (maxH - 1f)).coerceAtLeast(1f)
        drawRoundRect(
            Color.White.copy(alpha = 0.9f),
            Offset(x0 + i * slot, y + (maxH - h)),
            Size(barW2, h),
            CornerRadius(1f),
        )
    }
}

private fun DrawScope.drawVideoStub(measurer: TextMeasurer) {
    val a = ContentArea()
    val textCol = G.MenuText
    val baseX = a.left + 10f
    var y = a.top + 10f

    drawText(
        textLayoutResult = measurer.measure("Видео", TextStyle(color = textCol, fontSize = 11.sp)),
        topLeft = Offset(baseX, y),
    )
    y += 20f

    // Чёрная «рамка» проигрывателя с треугольником play.
    drawRoundRect(Color(0xFF101820), Offset(baseX, y), Size(a.width - 20f, 88f), CornerRadius(4f))
    drawText(
        textLayoutResult = measurer.measure("▶", TextStyle(color = Color.White, fontSize = 26.sp)),
        topLeft = Offset(a.left + a.width / 2f - 8f, y + 26f),
    )
    y += 96f

    drawText(
        textLayoutResult = measurer.measure("Видеоплеер", TextStyle(color = textCol, fontSize = 10.sp)),
        topLeft = Offset(baseX, y),
    )
    y += 13f
    drawText(
        textLayoutResult = measurer.measure("(раздел в разработке)", TextStyle(color = textCol.copy(alpha = 0.6f), fontSize = 9.sp)),
        topLeft = Offset(baseX, y),
    )
}

private fun DrawScope.drawStub(measurer: TextMeasurer, title: String, bodyRaw: String, wrap: Boolean) {
    val a = ContentArea()
    val textCol = G.MenuText
    val baseX = a.left + 10f
    val titleL = measurer.measure(title, TextStyle(color = textCol, fontSize = 11.sp))
    val bodyStyle = TextStyle(color = textCol, fontSize = 10.sp, lineHeight = 14.sp)
    val bodyL = if (wrap) {
        // Перенос по словам в пределах области дисплея (как в меню): текст не выходит за края.
        val maxW = (a.width - 20f).toInt().coerceAtLeast(1)
        measurer.measure(
            bodyRaw,
            bodyStyle,
            overflow = TextOverflow.Clip,
            softWrap = true,
            maxLines = Int.MAX_VALUE,
            constraints = Constraints(maxWidth = maxW),
        )
    } else {
        measurer.measure(bodyRaw, bodyStyle)
    }
    drawText(textLayoutResult = titleL, topLeft = Offset(baseX, a.top + 10f))
    drawText(textLayoutResult = bodyL, topLeft = Offset(baseX, a.top + 10f + titleL.size.height + 8f))
}

/** Отладочная страница эмулятора: заголовок и строки с переносом по словам, клип области. */
private fun DrawScope.drawEmulator(measurer: TextMeasurer, ui: DebugUi) {
    val a = ContentArea()
    val textCol = G.MenuText
    val baseX = a.left + 8f
    val maxW = (a.width - 16f).toInt().coerceAtLeast(1)

    clipRect(left = a.left, top = a.top, right = a.right, bottom = a.bottom) {
        var y = a.top + 6f
        drawText(
            textLayoutResult = measurer.measure(
                ui.title,
                TextStyle(color = Color(0xFF0a3a70), fontSize = 12.sp),
            ),
            topLeft = Offset(baseX, y),
        )
        y += 17f

        val lineStyle = TextStyle(color = textCol, fontSize = 9.5.sp, lineHeight = 13.sp)
        for (line in ui.lines) {
            val r = measurer.measure(
                line,
                lineStyle,
                overflow = TextOverflow.Clip,
                softWrap = true,
                maxLines = Int.MAX_VALUE,
                constraints = Constraints(maxWidth = maxW),
            )
            drawText(textLayoutResult = r, topLeft = Offset(baseX, y))
            y += r.size.height + 3f
        }
    }
}

private fun fmtTime(seconds: Double): String {
    val total = seconds.toInt().coerceAtLeast(0)
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}

private val LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."