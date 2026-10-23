package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ResonanceMusicTheme(
    accentColor: Color = NeonCyan,
    content: @Composable () -> Unit
) {
    val darkColorScheme = darkColorScheme(
        primary = accentColor,
        onPrimary = Color.Black,
        primaryContainer = accentColor.copy(alpha = 0.2f),
        onPrimaryContainer = Color.White,
        secondary = ElectricViolet,
        onSecondary = Color.White,
        secondaryContainer = ElectricViolet.copy(alpha = 0.2f),
        background = BackgroundDark,
        onBackground = TextPrimary,
        surface = SurfaceDark,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceCard,
        onSurfaceVariant = TextSecondary,
        outline = SurfaceBorder
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        typography = Typography,
        content = content
    )
}
