package ru.kantser.elephantmusic.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.kantser.elephantmusic.ui.components.SideBar
import ru.kantser.elephantmusic.ui.navigation.Screen
import ru.kantser.elephantmusic.ui.screens.AboutScreen
import ru.kantser.elephantmusic.ui.screens.LastFmScreen
import ru.kantser.elephantmusic.ui.screens.PlayerScreen
import ru.kantser.elephantmusic.ui.screens.PlaylistScreen
import ru.kantser.elephantmusic.ui.screens.SettingsScreen
import ru.kantser.elephantmusic.ui.screens.UpdateScreen
import ru.kantser.elephantmusic.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        Surface(Modifier.fillMaxSize()) {
            var selected by remember { mutableStateOf(Screen.PLAYER) }

            Row(Modifier.fillMaxSize()) {
                SideBar(
                    selected = selected,
                    onSelect = { selected = it },
                    modifier = Modifier.width(180.dp).fillMaxHeight(),
                )

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(20.dp),
                ) {
                    when (selected) {
                        Screen.PLAYER -> PlayerScreen()
                        Screen.PLAYLIST -> PlaylistScreen()
                        Screen.LASTFM -> LastFmScreen()
                        Screen.ABOUT -> AboutScreen()
                        Screen.UPDATE -> UpdateScreen()
                        Screen.SETTINGS -> SettingsScreen()
                    }
                }
            }
        }
    }
}
