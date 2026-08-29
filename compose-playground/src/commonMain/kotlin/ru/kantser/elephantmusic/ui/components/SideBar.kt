package ru.kantser.elephantmusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.kantser.elephantmusic.ui.navigation.Screen
import ru.kantser.elephantmusic.ui.theme.AppButton
import ru.kantser.elephantmusic.ui.theme.AppOrange
import ru.kantser.elephantmusic.ui.theme.AppSidebar
import ru.kantser.elephantmusic.ui.theme.AppText

@Composable
fun SideBar(selected: Screen, onSelect: (Screen) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(AppSidebar)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "ElephantMusic",
            style = MaterialTheme.typography.titleMedium,
            color = AppOrange,
        )
        Spacer(Modifier.height(12.dp))

        Screen.entries.forEach { screen ->
            val isSelected = screen == selected
            Text(
                text = screen.title,
                color = if (isSelected) Color.White else AppText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) AppOrange else AppButton)
                    .clickable { onSelect(screen) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}
