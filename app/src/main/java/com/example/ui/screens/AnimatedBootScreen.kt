package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedBootScreen(
    onBootComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var bootPhaseText by remember { mutableStateOf("INITIALIZING NEURAL DSP CORE...") }

    // Smooth boot progress driver
    LaunchedEffect(Unit) {
        val steps = listOf(
            Pair(0.20f, "INITIALIZING 64-BIT ACOUSTIC DRIVERS & AUDIO ENGINE..."),
            Pair(0.45f, "CALIBRATING CAR STEREO FADER & 5-BAND EQUALIZER..."),
            Pair(0.70f, "LOCKING MULTI-BAND 911 & POLICE DISPATCH RECEIVER..."),
            Pair(0.90f, "SYNCING SPOTIFY, APPLE MUSIC & MYRADIO STREAMS..."),
            Pair(1.00f, "RESONANCE CYBERNETIC NEURAL OS ONLINE.")
        )

        for (step in steps) {
            bootPhaseText = step.second
            val startProg = progress
            val targetProg = step.first
            val subSteps = 10
            for (i in 1..subSteps) {
                delay(35)
                progress = startProg + (targetProg - startProg) * (i / subSteps.toFloat())
            }
            delay(120)
        }
        delay(250)
        onBootComplete()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "BootAnimation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030509))
            .testTag("animated_boot_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Holographic 3D Rotating Gyro Audio Core
            Box(
                modifier = Modifier
                    .size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2.4f

                    // Outer segmented ring
                    drawArc(
                        brush = Brush.sweepGradient(listOf(NeonCyan, ElectricViolet, NeonCyan)),
                        startAngle = rotation,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Counter-rotating inner ring
                    val innerRadius = radius * 0.72f
                    drawArc(
                        brush = Brush.sweepGradient(listOf(ElectricViolet, Color(0xFF00FFCC), ElectricViolet)),
                        startAngle = -rotation * 1.5f,
                        sweepAngle = 200f,
                        useCenter = false,
                        topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                        size = Size(innerRadius * 2, innerRadius * 2),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Core pulsing glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(NeonCyan.copy(alpha = 0.35f * pulse), Color.Transparent),
                            center = center,
                            radius = innerRadius * pulse
                        ),
                        center = center,
                        radius = innerRadius * pulse
                    )
                }

                // Center Audio Core Badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF08101E))
                        .border(1.5.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Identity Brand
            Text(
                text = "RESONANCE",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp,
                color = TextPrimary
            )
            Text(
                text = "NEURAL ACOUSTIC AUDIO OS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                color = NeonCyan
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Diagnostics Progress Bar
            Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BOOTLOADER",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Glowing Linear Progress
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF10192A))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(NeonCyan, ElectricViolet, Color(0xFF00FFCC))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Terminal Status Log
                Text(
                    text = "> $bootPhaseText",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF88A4D0),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Instant Skip / Enter Button
            OutlinedButton(
                onClick = onBootComplete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("skip_boot_button")
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ENTER OS", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}
