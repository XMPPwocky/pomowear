package com.pomowear.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val PomowearColorScheme = ColorScheme(
    primary = WorkPrimary,
    secondary = ShortBreakPrimary,
    tertiary = LongBreakPrimary,
    background = Background,
    surface = Surface,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onPrimary = OnSurface,
    onSecondary = OnSurface,
    onTertiary = OnSurface
)

@Composable
fun PomowearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PomowearColorScheme,
        content = content
    )
}
