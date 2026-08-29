package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Отражает <defs> упрощённого SVG (RITMIX RF-8800).
 * Правка цветов/градиентов здесь = правка в SVG.
 */
object SvgGradients {
    // Корпус: bodyFill (вертикальный глянец)
    val BodyFill: Brush = Brush.verticalGradient(
        0.00f to Color(0xFF4a4d50),
        0.06f to Color(0xFF17181a),
        0.55f to Color(0xFF010102),
        1.00f to Color(0xFF141517),
    )

    // Блик на лицевой панели (faceGloss, упрощённо — белый сверху)
    val FaceGloss: Brush = Brush.verticalGradient(
        0.00f to Color.White.copy(alpha = 0.20f),
        0.45f to Color.White.copy(alpha = 0.04f),
        1.00f to Color.White.copy(alpha = 0f),
    )

    // Серебро логотипа (logoMetal)
    val LogoMetal: Brush = Brush.linearGradient(
        0.00f to Color(0xFFfafbfc),
        0.55f to Color(0xFFdfe4ea),
        1.00f to Color(0xFFbfc6cd),
    )

    // Единый цилиндрический блик кнопочного бруска (barMetal)
    val BarMetal: Brush = Brush.linearGradient(
        0.00f to Color(0xFF54585e),
        0.15f to Color(0xFF90959c),
        0.40f to Color(0xFFdde2e7),
        0.52f to Color(0xFFf0f3f6),
        0.72f to Color(0xFFaeb4bb),
        1.00f to Color(0xFF5c6066),
    )

    // Фаска лица: свет слева, тень справа (faceSide) — objectBoundingBox
    val FaceSide: Brush = Brush.horizontalGradient(
        0.00f to Color.White.copy(alpha = 0.55f),
        0.16f to Color.White.copy(alpha = 0f),
        0.84f to Color.Black.copy(alpha = 0f),
        1.00f to Color.Black.copy(alpha = 0.45f),
    )

    // Фаска по вертикали: верх блестит, низ затенён (faceVert) — objectBoundingBox
    val FaceVert: Brush = Brush.verticalGradient(
        0.00f to Color.White.copy(alpha = 0.40f),
        0.16f to Color.White.copy(alpha = 0f),
        0.84f to Color.Black.copy(alpha = 0f),
        1.00f to Color.Black.copy(alpha = 0.35f),
    )

    /** Единый диагональный зайчик поверх всех кнопок (barGlare). Строится по размеру бруска. */
    fun BarGlare(w: Float, h: Float): Brush = Brush.linearGradient(
        0.00f to Color.White.copy(alpha = 0f),
        0.38f to Color.White.copy(alpha = 0f),
        0.50f to Color.White.copy(alpha = 0.45f),
        0.62f to Color.White.copy(alpha = 0f),
        1.00f to Color.White.copy(alpha = 0f),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(w, h),
    )

    // Экранная область (контент рисуется в Compose; здесь — светлый фон + синяя панель)
    val ScreenBg: Brush = Brush.verticalGradient(
        0.00f to Color(0xFFeef5ec),
        0.50f to Color(0xFFcbe6d2),
        1.00f to Color(0xFF8bcfb2),
    )
    val PanelBlue: Brush = Brush.radialGradient(
        0.00f to Color(0xFF2f7fd4),
        0.55f to Color(0xFF15569f),
        1.00f to Color(0xFF0a3a70),
    )

    // Статичные цвета
    val BodyStroke = Color(0xFF050505)
    val BodyEdgeStroke = Color(0xFF3c3f42).copy(alpha = 0.7f)
    val BodyFaceFill = Color(0xFF050506)
    val FrameFill = Color(0xFF141414)
    val FrameStroke = Color(0xFF000000)
    val ButtonFaceBase = Color(0xFF7b7b7b)
    val ButtonRim = Color(0xFF0b0c0e)
    val IconColor = Color(0xFFeef2f5)

    // Палитра экрана (пункты меню, выделение)
    val MenuText = Color(0xFF133a55)
    val SelectedFill = Color.White.copy(alpha = 0.58f)
    val SelectedDark = Color(0xFF434343)
    val SidePanelText = Color.White
}
