package ru.kantser.elephantmusic.ui.screens.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.domain.controller.PlayerController
import ru.kantser.elephantmusic.platform.AppLog
import ru.kantser.elephantmusic.platform.javaVersionLabel
import ru.kantser.elephantmusic.platform.platformRuntimeLabel
import ru.kantser.elephantmusic.ui.screens.player.DeviceButton
import ru.kantser.elephantmusic.ui.screens.player.ScreenMode
import ru.kantser.elephantmusic.ui.screens.player.ScreenState
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as PlayerGeo
import ru.kantser.elephantmusic.ui.screens.player.homeModeFor
import ru.kantser.elephantmusic.ui.screens.test.gui.DebugPanel
import ru.kantser.elephantmusic.ui.screens.test.gui.DebugUi
import ru.kantser.elephantmusic.ui.screens.test.gui.DebugViewMode
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiDebugState
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiDpiService
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiResolutionService
import ru.kantser.elephantmusic.ui.screens.test.gui.MenuScroll
import ru.kantser.elephantmusic.ui.screens.test.gui.PlaybackUi
import ru.kantser.elephantmusic.ui.screens.test.gui.TestDeviceView
import ru.kantser.elephantmusic.ui.screens.test.gui.formatCornersP

/** Набор действий четырёх кнопок прибора (⏮, M, ▶, ⏭) для текущего режима. */
private data class TestActions(
    val rewind: () -> Unit,
    val fwd: () -> Unit,
    val playPause: () -> Unit,
    val menu: () -> Unit,
)

/**
 * Тестовая вкладка «Отладка SVG»: упрощённый аналог Ritmix-плеера, но с настоящим
 * воспроизведением — использует тот же PlayerController (реальный источник звука), что
 * и вкладка RITMIX. Машина состояний экрана повторяет RitmixScreen: в главном меню ▶
 * входит в выбранный раздел (Музыка → «сейчас играет», Файлы → список, остальные — заглушки),
 * M возвращает в меню.
 */
@Composable
fun TestScreen() {
    var showCorners by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf(DebugViewMode.BODY) }
    var scrollIndex by remember { mutableStateOf(0) }
    val log: AppLog = koinInject()
    val controller: PlayerController = koinInject()
    val debug: GuiDebugState = koinInject()
    val dpi: GuiDpiService = koinInject()
    val resolution: GuiResolutionService = koinInject()
    val density = LocalDensity.current

    val st = remember { ScreenState() }
    val s = controller.state
    val playlist = s.playlists.firstOrNull { it.name == s.currentPlaylistName }
    st.tracks = playlist?.tracks ?: emptyList()

    // ===== Данные раздела «Отладка эмулятора» (текущая нижняя панель → экран плеера) =====
    val emuUi = remember(debug.windowSize, debug.fieldPx, debug.appliedScale) {
        val win = debug.windowSize?.let { "окно ${it.width.value.toInt()}x${it.height.value.toInt()} dp" }
            ?: "окно: нет данных"
        val field = "область поля ${debug.fieldPx.width.toInt()}x${debug.fieldPx.height.toInt()} dp"
        val screen = resolution.activeDisplay()?.let {
            "экран ${it.widthPx}x${it.heightPx}px dpi=${it.densityDpi.toInt()} ос-масштаб=${it.scaleFactor}"
        } ?: "экран: нет данных"
        val densityLine = "compose density = ${dpi.densityFactor(density)}"
        DebugUi(
            lines = listOf(
                "$win    $field",
                "$screen $densityLine",
                "Shell  ${formatCornersP(PlayerGeo.BodyShell)}",
                "Edge   ${formatCornersP(PlayerGeo.BodyEdge)}",
                "Face   ${formatCornersP(PlayerGeo.BodyFace)}",
                "Frame  ${formatCornersP(PlayerGeo.DisplayFrame)}",
                "Среда выполнения: ${platformRuntimeLabel()}",
                "Версия JVM: ${javaVersionLabel()}",
            ),
        )
    }

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
        // «Отладка эмулятора» — последний пункт меню (индекс за реальными разделами).
        if (st.menuIndex == st.homeItems.size - 1) {
            st.mode = ScreenMode.EMULATOR
            return
        }
        val m = homeModeFor(st.menuIndex)
        when {
            m == ScreenMode.NOW -> {
                nowSource = ScreenMode.HOME
                st.mode = ScreenMode.NOW
            }
            m == ScreenMode.LIST -> {
                st.trackSel = 0
                st.listScroll = 0
                st.mode = m
            }
            else -> st.mode = m
        }
    }

    fun backToHome() {
        st.mode = ScreenMode.HOME
        st.trackSel = 0
        st.listScroll = 0
    }

    // ===== Список: ⏮/⏭ — навигация, ▶ — воспроизведение =====
    fun moveSel(delta: Int) {
        if (st.tracks.isEmpty()) return
        st.trackSel = (st.trackSel + delta).mod(st.tracks.size)
        st.fitSelection(st.tracks.size)
    }

    fun playSelected() {
        if (st.tracks.isEmpty()) return
        val idx = st.trackSel.coerceIn(0, st.tracks.size - 1)
        controller.playTrack(st.tracks[idx])
        nowSource = ScreenMode.LIST
        st.mode = ScreenMode.NOW
    }

    /** ▶ в списке: если ничего не играет (или выбран другой трек) — играем выбранный, иначе пауза. */
    fun playOrToggle() {
        val active = s.currentTrack
        val selected = st.tracks.getOrNull(st.trackSel.coerceIn(0, st.tracks.size - 1))
        when {
            selected == null -> Unit
            active == null || active.filePath != selected.filePath -> controller.playTrack(selected)
            else -> controller.playPause()
        }
    }

    fun moveMenu(delta: Int) {
        st.menuIndex = (st.menuIndex + delta).mod(st.homeItems.size)
        scrollIndex = MenuScroll.fitScroll(st.homeItems.size, scrollIndex, st.menuIndex)
    }

    // ===== «Сейчас играет»: ⏮/⏭ — предыдущий/следующий трек, ▶ — пауза, M — назад =====
    val actions = when (st.mode) {
        ScreenMode.HOME -> TestActions(
            rewind = { moveMenu(-1) },
            fwd = { moveMenu(+1) },
            playPause = ::enterHomeItem,
            menu = {},
        )
        ScreenMode.LIST -> TestActions(
            rewind = { moveSel(-1) },
            fwd = { moveSel(+1) },
            playPause = ::playOrToggle,
            menu = ::backToHome,
        )
        ScreenMode.NOW -> TestActions(
            rewind = { controller.previous() },
            fwd = { controller.next() },
            playPause = { controller.playPause() },
            menu = { st.mode = nowSource },
        )
        // Заглушки: работает только M (вернуться в меню).
        else -> TestActions(
            rewind = {},
            fwd = {},
            playPause = {},
            menu = ::backToHome,
        )
    }

    val playback = PlaybackUi(
        title = s.currentTrack?.title ?: "",
        artist = s.currentTrack?.artist ?: "",
        isPlaying = s.isPlaying,
        position = s.positionSeconds,
        duration = s.durationSeconds,
        levels = s.levels,
    )

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            TestDeviceView(
                mode = mode,
                showCorners = showCorners,
                onButton = { btn ->
                    when (btn) {
                        DeviceButton.REWIND -> actions.rewind()
                        DeviceButton.FWD -> actions.fwd()
                        DeviceButton.PLAY_PAUSE -> actions.playPause()
                        DeviceButton.MENU -> actions.menu()
                    }
                },
                st = st,
                scrollIndex = scrollIndex,
                playback = playback,
                debug = emuUi,
            )
        }
        DebugPanel(
            mode = mode,
            onModeChange = { mode = it },
            showCorners = showCorners,
            onToggleCorners = { showCorners = !showCorners },
        )
    }
}
