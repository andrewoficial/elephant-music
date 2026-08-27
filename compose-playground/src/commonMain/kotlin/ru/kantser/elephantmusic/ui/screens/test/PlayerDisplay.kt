package ru.kantser.elephantmusic.ui.screens.test

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import ru.kantser.elephantmusic.domain.model.Track

private val LcdBackground = Color(0xFF07140D)
private val LcdBackground2 = Color(0xFF123324)
private val LcdGlow = Color(0xFF7CFFA0)
private val LcdDim = Color(0xFF3E6B4E)

@Composable
fun PlayerDisplay(currentTrack: Track?, onOpenPlaylist: () -> Unit, modifier: Modifier = Modifier) {
    // Ховер по блоку с песней — только тут появляется меню «Плейлист».
    val songInteraction = remember { MutableInteractionSource() }
    val songHovered by songInteraction.collectIsHoveredAsState()

    // Позиция курсора в координатах экрана (обновляется на каждое движение мыши).
    var pointerPosition by remember { mutableStateOf(Offset.Zero) }

    // Эквалайзер: открывается ПО КЛИКУ, а не по ховеру.
    val eqInteraction = remember { MutableInteractionSource() }
    val eqHovered by eqInteraction.collectIsHoveredAsState()
    var eqExpanded by remember { mutableStateOf(false) }
    var preset by remember { mutableStateOf("NORMAL") }
    val presets = remember { listOf("NORMAL", "BASS", "JAZZ", "VOICE") }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(LcdBackground, LcdBackground2)))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.firstOrNull()?.let { pointerPosition = it.position }
                    }
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            var y = 0f
            val step = 4.dp.toPx()
            while (y < size.height) {
                drawLine(Color.Black.copy(alpha = 0.16f), Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
        }

        Row(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("\u25B6 LOOP", color = LcdGlow, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("78%", color = LcdGlow, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(Modifier.width(8.dp))
                        // Иконка эквалайзера: клик открывает/закрывает панель.
                        Box(
                            Modifier
                                .hoverable(eqInteraction)
                                .clickable(interactionSource = eqInteraction, indication = null) {
                                    eqExpanded = !eqExpanded
                                }
                                .padding(4.dp),
                        ) {
                            EqualizerIcon(active = eqExpanded || eqHovered)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Блок с песней — зона ховера, по которой появляется меню «Плейлист».
                Box(Modifier.hoverable(songInteraction)) {
                    Column {
                        Text("СЕЙЧАС ИГРАЕТ", color = LcdDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            currentTrack?.title ?: "—",
                            color = LcdGlow,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            currentTrack?.artist ?: "",
                            color = LcdDim,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.4f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(LcdGlow),
                    )
                }
            }

            // Панель эквалайзера. «Раздвижение» по клику (eqExpanded).
            AnimatedVisibility(
                visible = eqExpanded,
                enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut(),
                modifier = Modifier.fillMaxHeight(),
            ) {
                EqPanel(
                    presets = presets,
                    selected = preset,
                    onSelect = { preset = it },
                )
            }
        }

        // Меню «Плейлист» — появляется у курсора (не по центру), при ховере на блок песни.
        AnimatedVisibility(
            visible = songHovered,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier.offset {
                IntOffset(pointerPosition.x.roundToInt(), pointerPosition.y.roundToInt())
            },
        ) {
            DisplayMenu(onOpenPlaylist = onOpenPlaylist)
        }
    }
}

@Composable
private fun EqualizerIcon(active: Boolean) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(6, 12, 9).forEach { h ->
            Box(
                Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (active) LcdGlow else LcdDim),
            )
        }
    }
}

@Composable
private fun EqPanel(presets: List<String>, selected: String, onSelect: (String) -> Unit) {
    // verticalScroll — прокрутка колёсиком мыши (на desktop) и пальцем (на мобильных).
    Column(
        Modifier
            .width(112.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            "EQ",
            color = LcdGlow,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
        presets.forEach { preset ->
            EqPresetRow(preset, preset == selected, onClick = { onSelect(preset) })
        }
    }
}

@Composable
private fun EqPresetRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    Text(
        label,
        color = if (selected || hovered) LcdGlow else LcdDim,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) Color(0xFF1C4A2E) else Color.Transparent)
            .hoverable(interaction)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
