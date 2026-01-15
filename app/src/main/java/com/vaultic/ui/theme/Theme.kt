package com.vaultic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = VaulticBlue,
    secondary = VaulticTeal,
    tertiary = VaulticGold,
    background = VaulticBackground,
    surface = VaulticSurface,
    onPrimary = VaulticOnPrimary,
    onSecondary = VaulticOnSecondary,
    onTertiary = VaulticOnPrimary,
    onBackground = VaulticOnBackground,
    onSurface = VaulticOnBackground
)

@Composable
fun VaulticTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content
    )
}
