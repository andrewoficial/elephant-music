package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.DpSize

/**
 * Общее отладочное состояние тестовой вкладки (Koin single), доступное и полю,
 * и панели отладки. Сюда пишут: Main (размер окна), DeviceScale (область поля),
 * панель читает и показывает. Используются Compose-состояния — реакции в UI живая.
 */
class GuiDebugState {
    /** Текущий размер окна приложения в dp (пишет desktop Main.kt). */
    var windowSize by mutableStateOf<DpSize?>(null)

    /** Размер области, реально отведённой под поле (в px; пересчёт в dp — при выводе). */
    var fieldPx by mutableStateOf(Size.Zero)

    /** Итоговый масштаб, который фактически применён к полю (пишет DeviceScale). */
    var appliedScale by mutableStateOf(1f)

    /** Показывать чертёж корпуса (прямоугольники) на реальном экране плеера. */
    var showPlayerBody by mutableStateOf(false)
}
