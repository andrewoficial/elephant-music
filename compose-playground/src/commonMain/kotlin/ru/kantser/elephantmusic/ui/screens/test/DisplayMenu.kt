package ru.kantser.elephantmusic.ui.screens.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MenuPanel = Color(0xE6101812)
private val MenuHover = Color(0xFF2E5C40)
private val MenuText = Color(0xFF7CFFA0)

@Composable
fun DisplayMenu(onOpenPlaylist: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MenuPanel,
        shadowElevation = 8.dp,
    ) {
        Column(Modifier.padding(6.dp)) {
            MenuRow("Плейлист") { onOpenPlaylist() }
        }
    }
}

@Composable
private fun MenuRow(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Text(
        text = label,
        color = MenuText,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHovered) MenuHover else Color.Transparent)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
