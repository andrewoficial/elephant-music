package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.kantser.elephantmusic.domain.model.Track

enum class ScreenMode { HOME, LIST, NOW, VIDEO, PHOTO, TEXT, RECORD, OTHER, SETTINGS, EMULATOR }

/** Сколько строк списка видно на экране (высота светлой зоны 168 / высота строки 20). */
const val VISIBLE_LIST_ROWS = 8
const val LIST_ROW_HEIGHT = 20

/** Модель состояния экрана прибора. Поля — Compose-состояния, чтобы смена реагировала в UI. */
class ScreenState(
    homeItems: List<String> = HOME_ITEMS,
) {
    val homeItems = homeItems
    var mode by mutableStateOf(ScreenMode.HOME)
    var menuIndex by mutableIntStateOf(0)
    var trackSel by mutableIntStateOf(0)

    /** Сдвиг видимой части списка (прокрутка внутри экрана, а не вылезание за границу). */
    var listScroll by mutableIntStateOf(0)

    var tracks by mutableStateOf<List<Track>>(emptyList())

    /** Держит trackSel в пределах видимой части списка — список "дышит"/прокручивается. */
    fun fitSelection(listSize: Int) {
        if (listSize <= VISIBLE_LIST_ROWS) {
            listScroll = 0
            return
        }
        if (trackSel < listScroll) listScroll = trackSel
        val visibleEnd = listScroll + VISIBLE_LIST_ROWS - 1
        if (trackSel > visibleEnd) listScroll = trackSel - VISIBLE_LIST_ROWS + 1
    }
}

val HOME_ITEMS = listOf(
    "Музыка", "Видео", "Фото", "Текст", "Запись", "Другие функции", "Файлы", "Настройки", "Отладка эмулятора",
)

/** Какая функция открывается при выборе пункта главного меню. */
fun homeModeFor(index: Int): ScreenMode = when (index) {
    0 -> ScreenMode.NOW       // Музыка: экран «сейчас играет»
    1 -> ScreenMode.VIDEO
    2 -> ScreenMode.PHOTO
    3 -> ScreenMode.TEXT
    4 -> ScreenMode.RECORD
    5 -> ScreenMode.OTHER     // Другие функции
    6 -> ScreenMode.LIST      // Файлы (фонотека/плей-лист)
    7 -> ScreenMode.SETTINGS  // Настройки
    else -> ScreenMode.HOME
}
