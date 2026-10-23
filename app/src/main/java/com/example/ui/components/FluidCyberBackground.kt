package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fluid Cybernetic Living Background
 * Generates continuous liquid plasma caustics, audio-reactive ripples,
 * and holographic telemetry grids for a 2040+ super-intelligent futuristic OS.
 */
@Composable
fun FluidCyberBackground(
    modifier: Modifier = Modifier,
    beatEnergy: Float = 0f,
    isBeat: Boolean = false,
    accentColor: Color = NeonCyan,
    secondaryColor: Color = ElectricViolet,
    enabled: Boolean = true
) {
    if (!enabled) return

    val infiniteTransition = rememberInfiniteTransition(label = "FluidTime")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FluidPhase"
    )

    val beatPulse by animateFloatAsState(
        targetValue = if (isBeat) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "BeatPulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        // 1. Deep Void Obsidian Base
        drawRect(color = Color(0xFF05070D))

        // 2. Fluid Plasma Blobs (Liquid Light Fields)
        val blob1X = width * (0.35f + 0.2f * sin(phase))
        val blob1Y = height * (0.28f + 0.15f * cos(phase * 0.8f))
        val blob1Radius = (width * 0.55f) * beatPulse

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(blob1X, blob1Y),
                radius = blob1Radius
            ),
            radius = blob1Radius,
            center = Offset(blob1X, blob1Y)
        )

        val blob2X = width * (0.65f - 0.22f * cos(phase * 0.9f))
        val blob2Y = height * (0.72f - 0.18f * sin(phase * 1.1f))
        val blob2Radius = (width * 0.6f) * (1f + beatEnergy * 0.3f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryColor.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(blob2X, blob2Y),
                radius = blob2Radius
            ),
            radius = blob2Radius,
            center = Offset(blob2X, blob2Y)
        )

        // 3. Fluid Wave Caustics (Undulating Harmonic Liquid Ribbons)
        val wavePath1 = Path()
        val wavePath2 = Path()
        val segments = 32
        val stepX = width / segments

        for (i in 0..segments) {
            val x = i * stepX
            val normX = i / segments.toFloat()
            val y1 = height * 0.42f + sin(normX * 6f + phase * 2f) * (40f * beatPulse) + cos(normX * 3f) * 20f
            val y2 = height * 0.58f + cos(normX * 5f - phase * 1.5f) * (35f * beatPulse)

            if (i == 0) {
                wavePath1.moveTo(x, y1)
                wavePath2.moveTo(x, y2)
            } else {
                wavePath1.lineTo(x, y1)
                wavePath2.lineTo(x, y2)
            }
        }

        drawPath(
            path = wavePath1,
            brush = Brush.horizontalGradient(
                listOf(accentColor.copy(alpha = 0.05f), accentColor.copy(alpha = 0.22f), Color.Transparent)
            ),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        drawPath(
            path = wavePath2,
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, secondaryColor.copy(alpha = 0.20f), secondaryColor.copy(alpha = 0.05f))
            ),
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )

        // 4. Subtle Cybernetic Grid Mesh & Telemetry Marks
        val gridStep = 64f
        val startY = (phase * 15f) % gridStep
        var y = startY
        while (y < height) {
            drawLine(
                color = Color.White.copy(alpha = 0.025f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += gridStep
        }

        // Corner Cybernetic Tech Brackets
        val bracketLen = 24f
        val bracketMargin = 16f
        // Top Left
        drawLine(accentColor.copy(alpha = 0.4f), Offset(bracketMargin, bracketMargin), Offset(bracketMargin + bracketLen, bracketMargin), 1.5f)
        drawLine(accentColor.copy(alpha = 0.4f), Offset(bracketMargin, bracketMargin), Offset(bracketMargin, bracketMargin + bracketLen), 1.5f)
        // Top Right
        drawLine(accentColor.copy(alpha = 0.4f), Offset(width - bracketMargin, bracketMargin), Offset(width - bracketMargin - bracketLen, bracketMargin), 1.5f)
        drawLine(accentColor.copy(alpha = 0.4f), Offset(width - bracketMargin, bracketMargin), Offset(width - bracketMargin, bracketMargin + bracketLen), 1.5f)
        // Bottom Left
        drawLine(accentColor.copy(alpha = 0.4f), Offset(bracketMargin, height - bracketMargin), Offset(bracketMargin + bracketLen, height - bracketMargin), 1.5f)
        drawLine(accentColor.copy(alpha = 0.4f), Offset(bracketMargin, height - bracketMargin), Offset(bracketMargin, height - bracketMargin - bracketLen), 1.5f)
        // Bottom Right
        drawLine(accentColor.copy(alpha = 0.4f), Offset(width - bracketMargin, height - bracketMargin), Offset(width - bracketMargin - bracketLen, height - bracketMargin), 1.5f)
        drawLine(accentColor.copy(alpha = 0.4f), Offset(width - bracketMargin, height - bracketMargin), Offset(width - bracketMargin, height - bracketMargin - bracketLen), 1.5f)
    }
}
