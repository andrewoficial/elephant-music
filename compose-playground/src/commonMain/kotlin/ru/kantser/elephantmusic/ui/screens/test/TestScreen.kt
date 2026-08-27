package ru.kantser.elephantmusic.ui.screens.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.kantser.elephantmusic.domain.model.Playlist
import ru.kantser.elephantmusic.domain.model.Track

private val demoPlaylist = Playlist(
    name = "Демо",
    tracks = listOf(
        Track("Sapphire", "Ed Sheeran", "/music/01.mp3", 179),
        Track("Love Me Not", "Ravyn Lenae", "/music/02.mp3", 213),
        Track("Azizam", "Ed Sheeran", "/music/12.mp3", 122),
        Track("Still Bad", "Lizzo", "/music/16.mp3", 208),
    ),
)

@Composable
fun TestScreen() {
    val playlist = remember { demoPlaylist }
    var currentIndex by remember { mutableStateOf(0) }
    var isPlaylistOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Mp3Player(
            currentTrack = playlist.tracks.getOrNull(currentIndex),
            onOpenPlaylist = { isPlaylistOpen = true },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
        )

        if (isPlaylistOpen) {
            PlaylistOverlay(
                playlist = playlist,
                currentIndex = currentIndex,
                onSelect = { index -> currentIndex = index; isPlaylistOpen = false },
                onDismiss = { isPlaylistOpen = false },
            )
        }
    }
}
