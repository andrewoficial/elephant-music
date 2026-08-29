package ru.kantser.elephantmusic.platform

/**
 * Информация об одном физическом/логическом дисплее (разрешение, плотность, масштаб ОС).
 * Используется сервисом WindowMetricsProvider и при отладке позиционирования.
 */
data class DisplayInfo(
    val id: String,
    val name: String,
    val widthPx: Int,
    val heightPx: Int,
    val densityDpi: Float,
    val scaleFactor: Float,
    val isPrimary: Boolean,
) {
    val aspectRatio: Float get() = if (heightPx > 0) widthPx.toFloat() / heightPx else 1f
}

/**
 * Сервис, выдающий текущие сведения о мониторах (разрешение, плотность, масштаб).
 * На него (и на сведения о размере окна) опирается позиционирование прибора.
 */
interface WindowMetricsProvider {
    /** Снимок всех подключённых дисплеев (каждый логируется один раз при первом чтении). */
    fun displays(): List<DisplayInfo>

    /** Основной дисплей или null, если дисплеев нет. */
    fun primary(): DisplayInfo?

    /** Текущий активный дисплей (тот, где находится окно/приложение). */
    fun activeDisplay(): DisplayInfo?
}

/** Фабрика платформенной реализации. */
expect fun createWindowMetricsProvider(): WindowMetricsProvider
