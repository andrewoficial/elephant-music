package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ru.kantser.elephantmusic.domain.controller.PlayerController
import ru.kantser.elephantmusic.ui.screens.player.PlayerDeviceView
import ru.kantser.elephantmusic.ui.screens.player.ScreenMode
import ru.kantser.elephantmusic.ui.screens.player.ScreenState
import ru.kantser.elephantmusic.ui.screens.player.homeModeFor

/** Набор действий четырёх кнопок прибора (⏮, M, ▶, ⏭) для текущего режима. */
private data class DeviceActions(
    val rewind: () -> Unit,
    val fwd: () -> Unit,
    val playPause: () -> Unit,
    val menu: () -> Unit,
)

/**
 * Экран Ritmix-плеера: машина состояний (меню/список/сейчас играет/заглушки) + живая отрисовка прибора.
 * Логика кнопок зависит от режима; геометрия — в ui.screens.player (стабильный компонент).
 */
@Composable
fun RitmixScreen(controller: PlayerController) {
    val s = controller.state
    val playlist = s.playlists.firstOrNull { it.name == s.currentPlaylistName }
    val tracks = playlist?.tracks ?: emptyList()

    val st = remember { ScreenState() }
    st.tracks = tracks

    /** Откуда пришли в «сейчас играет» — туда возвращаемся по кнопке M. */
    var nowSource by remember { mutableStateOf(ScreenMode.LIST) }

    // ===== Периодически обновляем позицию трека (для прогресс-бара) =====
    LaunchedEffect(st.mode) {
        if (st.mode == ScreenMode.NOW) {
            while (isActive) {
                controller.tick()
                delay(500)
            }
        }
    }

    // ===== Главное меню: ▶ входит в выбранный пункт =====
    fun enterHomeItem() {
        val mode = homeModeFor(st.menuIndex)
        when {
            mode == ScreenMode.NOW -> {
                // Музыка: сразу экран «сейчас играет» (последний открытый трек, даже на паузе).
                nowSource = ScreenMode.HOME
                st.mode = ScreenMode.NOW
            }
            mode == ScreenMode.LIST -> {
                st.trackSel = 0
                st.listScroll = 0
                st.mode = mode
            }
            else -> st.mode = mode
        }
    }

    fun backToHome() {
        st.mode = ScreenMode.HOME
        st.trackSel = 0
        st.listScroll = 0
    }

    // ===== Список: ⏮/⏭ — навигация, ▶ — воспроизведение =====
    fun moveSel(delta: Int) {
        if (tracks.isEmpty()) return
        st.trackSel = (st.trackSel + delta).mod(tracks.size)
        st.fitSelection(tracks.size)
    }

    fun playSelected() {
        if (tracks.isEmpty()) return
        val idx = st.trackSel.coerceIn(0, tracks.size - 1)
        controller.playTrack(tracks[idx])
        nowSource = ScreenMode.LIST
        st.mode = ScreenMode.NOW
    }

    /** ▶ в списке: если ничего не играет (или выбран другой трек) — играем выбранный, иначе пауза. */
    fun playOrToggle() {
        val active = s.currentTrack
        val selected = tracks.getOrNull(st.trackSel.coerceIn(0, tracks.size - 1))
        when {
            selected == null -> Unit
            active == null || active.filePath != selected.filePath -> controller.playTrack(selected)
            else -> controller.playPause()
        }
    }

    fun moveMenu(delta: Int) {
        if (st.homeItems.isEmpty()) return
        st.menuIndex = (st.menuIndex + delta).mod(st.homeItems.size)
    }

    // Кнопки устройства зависят от текущего режима.
    val actions = when (st.mode) {
        ScreenMode.HOME -> DeviceActions(
            rewind = { moveMenu(-1) },
            fwd = { moveMenu(+1) },
            playPause = ::enterHomeItem,
            menu = {},
        )
        ScreenMode.LIST -> DeviceActions(
            rewind = { moveSel(-1) },
            fwd = { moveSel(+1) },
            playPause = ::playOrToggle,
            menu = ::backToHome,
        )
        ScreenMode.NOW -> DeviceActions(
            rewind = { controller.previous() },
            fwd = { controller.next() },
            playPause = { controller.playPause() },
            menu = { st.mode = nowSource },
        )
        // Все заглушки: работает только M (вернуться в меню).
        else -> DeviceActions(
            rewind = {},
            fwd = {},
            playPause = {},
            menu = ::backToHome,
        )
    }

    // Прокрутка по вертикали: на узких/высоких экранах прибор не сжимается до микро-размера
    // и не обрезается — он занимает всю доступную ширину, а при нехватке высоты можно докрутить.
    Box(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        PlayerDeviceView(
            state = st,
            isPlaying = s.isPlaying,
            nowPlaying = s.currentTrack,
            nowPosition = s.positionSeconds,
            nowDuration = s.durationSeconds,
            onRewind = actions.rewind,
            onFwd = actions.fwd,
            onPlayPause = actions.playPause,
            onMenu = actions.menu,
            onSelectHome = { st.menuIndex = it; enterHomeItem() },
            onSelectTrack = { index -> st.trackSel = index; playSelected() },
            onPrevious = { controller.previous() },
            onNext = { controller.next() },
        )
    }
}
