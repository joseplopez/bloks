package com.centelles.bloks.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = AccentColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    onPrimary = TextPrimary,
    onSecondary = BackgroundColor,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun BlockBloomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
