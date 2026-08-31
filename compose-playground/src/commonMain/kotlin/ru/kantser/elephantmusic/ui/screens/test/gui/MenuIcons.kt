package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import kotlin.math.cos
import kotlin.math.sin

/**
 * Репозиторий иконок боковой панели меню. Каждая иконка рисуется примитивами DrawScope
 * (drawPath/drawCircle/drawRect/drawOval) в системе координат 200×200 и цветом #b9e8ff.
 * Раскладка соответствует <symbol> из исходного набора SVG Ritmix (viewBox 0 0 200 200).
 *
 * Пиктограмма для пункта меню выбирается по индексу ([drawMenuIcon]); индекс совпадает
 * с позицией пункта в HOME_ITEMS. Масштабируется вызывающей стороной (см. drawMenuPanelIcon).
 */
object MenuIcons {
    val IconColor = Color(0xFFb9e8ff)

    /** Отрисовка иконки пункта меню по его индексу (см. HOME_ITEMS). Раскладка — 200×200. */
    fun drawMenuIcon(scope: DrawScope, index: Int) {
        with(scope) {
            when (index) {
                0 -> drawMusicIcon()
                1 -> drawVideoIcon()
                2 -> drawPhotoIcon()
                3 -> drawTextIcon()
                4 -> drawRecordIcon()
                5 -> drawMiscIcon()
                6 -> drawFilesIcon()
                else -> drawSettingsIcon()
            }
        }
    }

    // ===== Музыка: двойная нота с балкой =====
    private fun DrawScope.drawMusicIcon() {
        val c = IconColor
        drawPath(p("M 64 62 L 163 48 L 163 68 L 64 82 Z"), c)
        drawRect(c, Offset(64f, 72f), Size(9f, 74f))
        drawRect(c, Offset(154f, 58f), Size(9f, 74f))
        drawOval(c, topLeft = Offset(52f - 21f, 146f - 15f), size = Size(42f, 30f))
        drawOval(c, topLeft = Offset(142f - 21f, 132f - 15f), size = Size(42f, 30f))
    }

    // ===== Видео: круглая кнопка play =====
    private fun DrawScope.drawVideoIcon() {
        val c = IconColor
        drawCircle(c, radius = 70f, center = Offset(100f, 100f), style = Stroke(12f))
        drawPath(p("M 80 64 L 146 100 L 80 136 Z"), c)
    }

    // ===== Фото: рамка с пейзажем (солнце + горы) =====
    private fun DrawScope.drawPhotoIcon() {
        val c = IconColor
        drawRect(
            c,
            topLeft = Offset(32f, 42f),
            size = Size(136f, 116f),
            style = Stroke(10f),
        )
        drawCircle(c, radius = 13f, center = Offset(70f, 78f))
        drawPath(p("M 40 152 L 90 100 L 116 128 L 134 110 L 162 152 Z"), c)
    }

    // ===== Текст: блокнот с пружиной и строками =====
    private fun DrawScope.drawTextIcon() {
        val c = IconColor
        drawRoundRect(
            c,
            topLeft = Offset(48f, 46f),
            size = Size(104f, 118f),
            cornerRadius = CornerRadius(6f),
            style = Stroke(10f),
        )
        for (x in floatArrayOf(68f, 96f, 124f)) {
            drawRoundRect(c, Offset(x, 30f), Size(8f, 26f), CornerRadius(4f))
        }
        drawRoundRect(c, Offset(68f, 82f), Size(64f, 7f), CornerRadius(3.5f))
        drawRoundRect(c, Offset(68f, 102f), Size(64f, 7f), CornerRadius(3.5f))
        drawRoundRect(c, Offset(68f, 122f), Size(42f, 7f), CornerRadius(3.5f))
    }

    // ===== Запись: микрофон =====
    private fun DrawScope.drawRecordIcon() {
        val c = IconColor
        drawPath(
            p("M 55 84 A 45 45 0 0 0 145 84"),
            c,
            style = Stroke(10f, cap = StrokeCap.Round),
        )
        drawRoundRect(c, Offset(80f, 28f), Size(40f, 78f), CornerRadius(20f))
        drawRect(c, Offset(96f, 130f), Size(8f, 24f))
        drawRoundRect(c, Offset(70f, 154f), Size(60f, 10f), CornerRadius(5f))
    }

    // ===== Другие функции: кубик Рубика 3×3 в изометрии =====
    private fun DrawScope.drawMiscIcon() {
        val c = IconColor
        val s = Stroke(8f)
        drawPath(p("M 100 30 L 150 55 L 100 80 L 50 55 Z"), c, style = s)
        drawPath(p("M 50 55 L 100 80 L 100 160 L 50 135 Z"), c, style = s)
        drawPath(p("M 100 80 L 150 55 L 150 135 L 100 160 Z"), c, style = s)
        drawPath(
            p(
                "M 83.3 38.3 L 133.3 63.3  M 66.7 46.7 L 116.7 71.7" +
                    "  M 116.7 38.3 L 66.7 63.3  M 133.3 46.7 L 100 80" +
                    "  M 66.7 63.3 V 143.3       M 83.3 71.7 V 151.7" +
                    "  M 50 81.7 L 100 106.7     M 50 108.3 L 100 133.3" +
                    "  M 133.3 63.3 V 143.3      M 116.7 71.7 V 151.7" +
                    "  M 150 81.7 L 100 106.7    M 150 108.3 L 100 133.3",
            ),
            c,
            style = s,
        )
    }

    // ===== Файлы: папка с лупой =====
    private fun DrawScope.drawFilesIcon() {
        val c = IconColor
        drawPath(p("M 30 50 L 75 50 L 85 65 L 170 65 L 170 150 L 30 150 Z"), c)
        drawPath(p("M 159 69 L 184 94 L 174 104 L 149 79 Z"), c)
        drawCircle(c, radius = 18f, center = Offset(145f, 55f), style = Stroke(10f))
        val white = Color.White.copy(alpha = 0.55f)
        drawRoundRect(white, Offset(40f, 118f), Size(30f, 5f), CornerRadius(2f))
        drawRoundRect(white, Offset(40f, 128f), Size(20f, 5f), CornerRadius(2f))
    }

    // ===== Настройки: шестерня с отверстием (перерисовано, чистая геометрия) =====
    private fun DrawScope.drawSettingsIcon() {
        drawPath(gearPath(center = Offset(100f, 100f)), IconColor)
    }

    /** Шестерня: восьмизубый контур с отверстием (even-odd). */
    private fun gearPath(center: Offset, outer: Float = 80f, toothH: Float = 18f, teeth: Int = 8, holeR: Float = 20f): Path {
        val n = teeth * 2
        val path = Path().apply {
            fillType = PathFillType.EvenOdd
            for (i in 0 until n) {
                val a = (Math.PI * 2 * i / n).toFloat()
                val r = if (i % 2 == 0) outer else outer - toothH
                val x = center.x + r * cos(a)
                val y = center.y + r * sin(a)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
            addOval(
                Rect(
                    center.x - holeR, center.y - holeR,
                    center.x + holeR, center.y + holeR,
                ),
            )
        }
        return path
    }

    /** Парсинг SVG path-данных (d) в Compose Path. */
    private fun p(d: String): Path = PathParser().parsePathString(d).toPath()
}
