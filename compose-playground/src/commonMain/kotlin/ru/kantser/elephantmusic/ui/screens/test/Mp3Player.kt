package ru.kantser.elephantmusic.ui.screens.test

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kantser.elephantmusic.domain.model.Track

// Хромированные поверхности: полосы светлого/тёмного — имитация зеркального отражения.
private val ChromeStops = listOf(
    Color(0xFFF2F4F7),
    Color(0xFFB7BDC4),
    Color(0xFF7A8188),
    Color(0xFFD8DDE2),
    Color(0xFF8E949B),
)
private val Chrome = Brush.verticalGradient(ChromeStops)
private val ChromeHorizontal = Brush.horizontalGradient(ChromeStops)

private val Bezel = Color(0xFF171A1E)
private val EdgeLine = Color(0xFF5A6066)
private val ButtonGlyph = Color(0xFF2A2E34)

@Composable
fun Mp3Player(currentTrack: Track?, onOpenPlaylist: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(18.dp, RoundedCornerShape(30.dp))
            .size(width = 640.dp, height = 250.dp),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // корпус — хром
            drawRoundRect(
                brush = Brush.verticalGradient(ChromeStops),
                cornerRadius = CornerRadius(30.dp.toPx()),
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                cornerRadius = CornerRadius(30.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx()),
            )
            // углубление под экран
            drawRoundRect(
                color = Bezel,
                topLeft = Offset(22.dp.toPx(), 48.dp.toPx()),
                size = Size(534.dp.toPx(), 154.dp.toPx()),
                cornerRadius = CornerRadius(12.dp.toPx()),
            )
        }

        PlayerDisplay(
            currentTrack = currentTrack,
            onOpenPlaylist = onOpenPlaylist,
            modifier = Modifier
                .offset(x = 28.dp, y = 54.dp)
                .size(width = 522.dp, height = 142.dp),
        )

        // единый блок основных кнопок (нарезан на 4 части), вплотную к экрану
        SideButtons(
            Modifier
                .offset(x = 556.dp, y = 48.dp)
                .size(width = 48.dp, height = 154.dp),
        )

        // кнопки на верхнем торце: громкость +, выключение, громкость -
        TopButtons(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp),
        )
    }
}

@Composable
private fun SideButtons(modifier: Modifier = Modifier) {
    // сверху вниз: назад, пауза, меню, вперёд
    val icons = listOf("\u23EE", "\u23EF", "\u2630", "\u23ED")
    Column(
        modifier
            .border(1.dp, EdgeLine, RectangleShape)
            .background(Chrome),
    ) {
        icons.forEachIndexed { index, icon ->
            SideSegment(icon, Modifier.weight(1f).fillMaxWidth())
            if (index != icons.lastIndex) {
                Box(Modifier.fillMaxWidth().height(2.dp).background(EdgeLine))
            }
        }
    }
}

@Composable
private fun SideSegment(icon: String, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier
            .background(Chrome)
            .clickable(interactionSource = interaction, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Text(icon, color = ButtonGlyph, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        if (pressed) {
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.18f)))
        }
    }
}

@Composable
private fun TopButtons(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        TopButton("\u002B")   // громкость +
        TopButton("\u23FB")   // выключение плеера
        TopButton("\u2212")   // громкость -
    }
}

@Composable
private fun TopButton(label: String) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        Modifier
            .size(width = 32.dp, height = 14.dp)
            .border(1.dp, EdgeLine, RectangleShape)
            .background(ChromeHorizontal)
            .clickable(interactionSource = interaction, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = ButtonGlyph, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        if (pressed) {
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.18f)))
        }
    }
}
