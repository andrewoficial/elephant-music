package ru.kantser.elephantmusic.ui.screens.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kantser.elephantmusic.domain.model.Playlist

private val Scrim = Color(0x66000000)
private val CardBg = Color(0xFFF5F6F8)
private val CardText = Color(0xFF2A2E34)
private val CardMuted = Color(0xFF6B7280)
private val Highlight = Color(0xFFE2E5EA)

@Composable
fun PlaylistOverlay(
    playlist: Playlist,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBg,
            shadowElevation = 16.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .width(300.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {},
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Плейлист", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CardText)
                    Text(
                        "\u2715",
                        color = CardMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onDismiss)
                            .padding(4.dp),
                    )
                }

                Spacer(Modifier.height(10.dp))

                playlist.tracks.forEachIndexed { index, track ->
                    val isCurrent = index == currentIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) Highlight else Color.Transparent)
                            .clickable { onSelect(index) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Column {
                            Text(
                                track.title,
                                fontSize = 14.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = CardText,
                            )
                            Text(track.artist, fontSize = 12.sp, color = CardMuted)
                        }
                    }
                }
            }
        }
    }
}
