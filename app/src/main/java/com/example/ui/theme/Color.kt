package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Obsidian & Navy Dark Surface Colors
val BackgroundDark = Color(0xFF090D14)
val SurfaceDark = Color(0xFF111724)
val SurfaceCard = Color(0xFF161E2E)
val SurfaceBorder = Color(0xFF222F47)

// Text & Highlights
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Accent Colors
val NeonCyan = Color(0xFF00E5FF)
val ElectricViolet = Color(0xFFA855F7)
val AmberGlow = Color(0xFFF59E0B)
val NeonAmber = Color(0xFFFFB300)
val PoliceBlue = Color(0xFF1E88E5)
val EmeraldPulse = Color(0xFF10B981)
val CrimsonWave = Color(0xFFF43F5E)

// Gradients & Accents
val PlayerDiscDark = Color(0xFF0F172A)
val GlowCyan = Color(0x3300E5FF)
val GlowViolet = Color(0x33A855F7)

fun getThemeAccentColor(themeName: String): Color {
    return when (themeName) {
        "Neon Violet" -> ElectricViolet
        "Amber Glow" -> AmberGlow
        "Emerald Pulse" -> EmeraldPulse
        "Crimson Wave" -> CrimsonWave
        else -> NeonCyan
    }
}
