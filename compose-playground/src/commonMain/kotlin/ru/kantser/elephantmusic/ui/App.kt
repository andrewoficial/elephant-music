package ru.kantser.elephantmusic.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.domain.controller.PlayerController
import ru.kantser.elephantmusic.ui.components.SideBar
import ru.kantser.elephantmusic.ui.navigation.Screen
import ru.kantser.elephantmusic.ui.screens.AboutScreen
import ru.kantser.elephantmusic.ui.screens.LastFmScreen
import ru.kantser.elephantmusic.ui.screens.PlayerScreen
import ru.kantser.elephantmusic.ui.screens.PlaylistScreen
import ru.kantser.elephantmusic.ui.screens.SettingsScreen
import ru.kantser.elephantmusic.ui.screens.UpdateScreen
import ru.kantser.elephantmusic.ui.screens.test.TestScreen
import ru.kantser.elephantmusic.ui.theme.AppTheme

@Composable
fun App() {
    val playerController: PlayerController = koinInject()

    AppTheme {
        Surface(Modifier.fillMaxSize()) {
            var selected by remember { mutableStateOf(Screen.PLAYER) }
            var sidebarVisible by remember { mutableStateOf(true) }

            Row(Modifier.fillMaxSize()) {
                if (sidebarVisible) {
                    SideBar(
                        selected = selected,
                        onSelect = { selected = it },
                        modifier = Modifier.width(180.dp).fillMaxHeight(),
                    )
                }

                Column(Modifier.weight(1f).fillMaxHeight()) {
                    TopBar(
                        sidebarVisible = sidebarVisible,
                        title = selected.title,
                        onToggleSidebar = { sidebarVisible = !sidebarVisible },
                    )

                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(20.dp),
                    ) {
                        when (selected) {
                            Screen.PLAYER -> PlayerScreen(playerController)
                            Screen.PLAYLIST -> PlaylistScreen(playerController)
                            Screen.LASTFM -> LastFmScreen()
                            Screen.ABOUT -> AboutScreen()
                            Screen.UPDATE -> UpdateScreen()
                            Screen.SETTINGS -> SettingsScreen()
                            Screen.TEST -> TestScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    sidebarVisible: Boolean,
    title: String,
    onToggleSidebar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (sidebarVisible) "◀" else "☰",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .clickable { onToggleSidebar() }
                .padding(6.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
