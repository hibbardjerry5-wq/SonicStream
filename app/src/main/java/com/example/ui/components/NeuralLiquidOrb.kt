package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import kotlin.math.*

/**
 * Super-Intelligent Liquid Neural Orb
 * Autonomous Cybernetic Core that calculates room acoustic inverse filters
 * and provides fluid liquid interaction.
 */
@Composable
fun NeuralLiquidOrb(
    isCalibrating: Boolean,
    isProcessing: Boolean,
    isBeat: Boolean,
    beatEnergy: Float,
    confidencePercent: Int,
    accentColor: Color = NeonCyan,
    secondaryColor: Color = ElectricViolet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidOrb")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbTime"
    )

    val corePulse by animateFloatAsState(
        targetValue = if (isBeat) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow),
        label = "OrbPulse"
    )

    Box(
        modifier = modifier
            .size(190.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("neural_liquid_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val baseRadius = (size.minDimension * 0.32f) * corePulse

            // 1. Fluid Ambient Aura Rings
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = if (isProcessing || isCalibrating) 0.5f else 0.25f),
                        secondaryColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = baseRadius * 1.8f
                ),
                radius = baseRadius * 1.75f,
                center = Offset(cx, cy)
            )

            // 2. Multi-Harmonic Liquid Perimeter Path
            val points = 64
            val liquidPath = Path()
            val speedFactor = if (isCalibrating || isProcessing) 3.5f else 1.2f

            for (i in 0 until points) {
                val theta = (i * 2.0 * PI / points).toFloat()
                // Perturbation waves: 3-lobed and 5-lobed sinusoidal liquid displacement
                val wave1 = sin(theta * 3f + time * speedFactor) * (8f + beatEnergy * 14f)
                val wave2 = cos(theta * 5f - time * (speedFactor * 0.8f)) * (5f + beatEnergy * 10f)
                val wave3 = sin(theta * 7f + time * (speedFactor * 1.4f)) * 3f
                val r = baseRadius + wave1 + wave2 + wave3

                val x = cx + r * cos(theta)
                val y = cy + r * sin(theta)

                if (i == 0) liquidPath.moveTo(x, y) else liquidPath.lineTo(x, y)
            }
            liquidPath.close()

            // Draw Liquid Shimmer Fill
            drawPath(
                path = liquidPath,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.85f),
                        secondaryColor.copy(alpha = 0.90f),
                        Color(0xFF38BDF8),
                        accentColor.copy(alpha = 0.85f)
                    ),
                    center = Offset(cx, cy)
                )
            )

            // Outer Liquid Specular Border
            drawPath(
                path = liquidPath,
                color = Color.White.copy(alpha = 0.8f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // 3. Orbiting Neural Synapse Nodes
            val nodeCount = 8
            val orbitRadius = baseRadius * 1.38f
            for (k in 0 until nodeCount) {
                val nodeAngle = (k * 2.0 * PI / nodeCount).toFloat() + (time * 0.6f)
                val nx = cx + orbitRadius * cos(nodeAngle)
                val ny = cy + orbitRadius * sin(nodeAngle)

                // Synapse ray to center
                drawLine(
                    color = accentColor.copy(alpha = 0.25f),
                    start = Offset(cx, cy),
                    end = Offset(nx, ny),
                    strokeWidth = 1f
                )

                // Synapse node dot
                drawCircle(
                    color = if (k % 2 == 0) accentColor else secondaryColor,
                    radius = 3.5f,
                    center = Offset(nx, ny)
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.8f,
                    center = Offset(nx, ny)
                )
            }

            // 4. Concentric Holographic Reticle Ticks
            val reticleRadius = baseRadius * 0.62f
            for (j in 0 until 12) {
                val tickAngle = (j * 2.0 * PI / 12).toFloat() - (time * 0.4f)
                val r1 = reticleRadius - 4f
                val r2 = reticleRadius + 4f
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(cx + r1 * cos(tickAngle), cy + r1 * sin(tickAngle)),
                    end = Offset(cx + r2 * cos(tickAngle), cy + r2 * sin(tickAngle)),
                    strokeWidth = 1.5f
                )
            }
        }

        // Center Cybernetic Telemetry Icon & Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isProcessing || isCalibrating) Icons.Default.AutoAwesome else Icons.Default.Psychology,
                contentDescription = "Neural Core",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isCalibrating) "TUNING..." else if (isProcessing) "COMPUTING..." else "NEURAL DSP",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "$confidencePercent% SYNC",
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
