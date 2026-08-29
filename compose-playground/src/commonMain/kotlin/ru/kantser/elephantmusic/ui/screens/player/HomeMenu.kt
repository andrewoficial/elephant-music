package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as Geo
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G

/**
 * Главное меню экрана — обычный Compose UI по данным (state.homeItems).
 * Позиционируется в светлой зоне экрана; пункты живые (hover) и кликабельные.
 */
@Composable
internal fun HomeMenu(
    state: ScreenState,
    onSelect: (Int) -> Unit,
) {
    val s = Geo.Screen
    val contentH = s.LightH - s.TopBarH
    Box(
        Modifier
            .offset(x = s.LightX.dp, y = (s.LightY + s.TopBarH).dp)
            .width(s.LightW.dp)
            .height(contentH.dp)
            .clipToBounds()
            .padding(horizontal = 10.dp),
    ) {
        Column {
            state.homeItems.forEachIndexed { index, item ->
                val selected = index == state.menuIndex
                MenuRow(
                    label = shortLabel(item),
                    selected = selected,
                    onClick = { onSelect(index) },
                )
            }
        }
    }
}

private fun shortLabel(item: String): String = item

@Composable
private fun MenuRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    Box(modifier = Modifier.fillMaxWidth().height(20.dp)) {
        val bg = when {
            selected -> G.SelectedFill
            hovered -> Color.White.copy(alpha = 0.18f)
            else -> Color.Transparent
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(bg)
                .hoverable(interaction)
                .clickable(interactionSource = interaction, indication = null) { onClick() }
                .padding(horizontal = 8.dp),
        ) {
            Text(
                label,
                color = if (selected) Color(0xFF0c2a3f) else G.MenuText,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
