package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.kantser.elephantmusic.generated.resources.Res
import ru.kantser.elephantmusic.generated.resources.elephant

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Музыкальный проигрыватель", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Image(
            painter = painterResource(Res.drawable.elephant),
            contentDescription = "Elephant",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(300.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text("Версия 1.0 (не определена)", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text("Разработано с использованием Compose Multiplatform")
        Spacer(Modifier.height(4.dp))
        Text("© 2025 Kantser Andrey")
    }
}
