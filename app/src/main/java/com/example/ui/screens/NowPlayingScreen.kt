package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VisualizerFrame
import com.example.model.formatMs
import com.example.ui.components.FluidCyberBackground
import com.example.ui.components.NeuralDirectiveDialog
import com.example.ui.components.NeuralLiquidOrb
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MusicPlayerViewModel
import com.example.ui.viewmodel.PlayerUiState
import com.example.ui.viewmodel.RepeatMode
import kotlin.math.*

@Composable
fun NowPlayingScreen(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    val track = uiState.currentTrack
    val visualizerFrame by viewModel.visualizerFrame.collectAsState()
    var showNeuralDialog by remember { mutableStateOf(false) }
    var showHoloDisc by remember { mutableStateOf(false) }

    val progressFraction = if (uiState.durationMs > 0) {
        (uiState.currentPositionMs.toFloat() / uiState.durationMs).coerceIn(0f, 1f)
    } else 0f

    // Vinyl continuous rotation
    val infiniteTransition = rememberInfiniteTransition(label = "VinylRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "RotationAngle"
    )
    val animatedRotation = if (uiState.isPlaying) rotation else 0f

    if (showNeuralDialog) {
        NeuralDirectiveDialog(
            viewModel = viewModel,
            uiState = uiState,
            onDismiss = { showNeuralDialog = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF05070D))
    ) {
        // Fluid Living Cybernetic Background
        FluidCyberBackground(
            beatEnergy = visualizerFrame.beatEnergy,
            isBeat = visualizerFrame.isBeat,
            accentColor = NeonCyan,
            secondaryColor = ElectricViolet,
            enabled = uiState.isFluidAuraActive
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header: Car Mode, Visualizer, Sound Lab Shortcuts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Car Stereo Driving Mode Button
                IconButton(
                    onClick = { viewModel.toggleCarMode() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), CircleShape)
                        .testTag("now_playing_car_mode_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Car Stereo Mode",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Sci-Fi Branding & Telemetry
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RESONANCE CYBER-OS",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DSP // ${uiState.neuralDspLatencyMs}ms • 32-BIT 96kHz",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Equalizer Sound Lab shortcut
                IconButton(
                    onClick = { viewModel.selectTab(AppTab.EQUALIZER) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, ElectricViolet.copy(alpha = 0.35f), CircleShape)
                        .testTag("now_playing_eq_shortcut")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Equalizer",
                        tint = ElectricViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Super-Intelligent Neural Directive HUD Pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.Black.copy(alpha = 0.65f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        if (uiState.isCalibratingNow || uiState.isNeuralProcessing) listOf(AmberGlow, CrimsonWave)
                        else listOf(NeonCyan, ElectricViolet)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNeuralDialog = true }
                    .testTag("ai_acoustic_quick_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isAiAcousticTuningActive) NeonCyan else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (uiState.isCalibratingNow) "NEURAL SENSING ROOM ACOUSTICS..."
                                else if (uiState.isNeuralProcessing) "NEURAL DSP SYNTHESIZING..."
                                else if (uiState.isAiAcousticTuningActive) "NEURAL LOCK: ${uiState.lastNeuralCommand}"
                                else "TAP TO OPEN NEURAL COPILOT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isAiAcousticTuningActive) NeonCyan else TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                            Text(
                                text = "Acoustics: ${uiState.ambientSplDb.toInt()}dB (${uiState.ambientEnvironmentType}) • ${uiState.aiConfidencePercent}% Match",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Emergency & Police Scanner Broadcast Ticker
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.isScannerTransmitting) Color(0xFF3B1219) else Color(0xCC111724),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        if (uiState.isScannerTransmitting) listOf(CrimsonWave, AmberGlow)
                        else listOf(SurfaceBorder, NeonCyan.copy(alpha = 0.25f))
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleScanner(!uiState.isScannerActive) }
                    .testTag("scanner_mini_ticker")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isScannerTransmitting) CrimsonWave else if (uiState.isScannerActive) NeonCyan else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isScannerTransmitting) "POLICE CALL ACTIVE: Ducking -${uiState.duckingPercent}%"
                            else if (uiState.isScannerActive) "SCANNER: ${uiState.selectedScannerChannel.name}"
                            else "911 / Police Radio: Tap to Monitor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isScannerTransmitting) CrimsonWave else if (uiState.isScannerActive) NeonCyan else TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (uiState.isScannerActive) {
                        Text(uiState.selectedScannerChannel.frequencyMhz, fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Centerpiece: Super-Intelligent Liquid Neural Orb or Holographic Vinyl
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!showHoloDisc) {
                    // Futuristic Living Liquid Neural Orb
                    NeuralLiquidOrb(
                        isCalibrating = uiState.isCalibratingNow,
                        isProcessing = uiState.isNeuralProcessing,
                        isBeat = visualizerFrame.isBeat,
                        beatEnergy = visualizerFrame.beatEnergy,
                        confidencePercent = uiState.aiConfidencePercent,
                        accentColor = NeonCyan,
                        secondaryColor = ElectricViolet,
                        onClick = { showNeuralDialog = true }
                    )
                } else {
                    // Poweramp Cyber Vinyl Disc
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(3.dp, Brush.sweepGradient(listOf(NeonCyan, ElectricViolet, AmberGlow, NeonCyan)), CircleShape)
                            .clickable { showHoloDisc = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().rotate(animatedRotation)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxR = size.minDimension / 2f

                            for (r in listOf(maxR * 0.9f, maxR * 0.8f, maxR * 0.7f, maxR * 0.6f)) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.08f),
                                    radius = r,
                                    center = center,
                                    style = Stroke(width = 1.2f)
                                )
                            }

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(NeonCyan, ElectricViolet, Color(0xFF0F172A)),
                                    center = center,
                                    radius = maxR * 0.42f
                                ),
                                radius = maxR * 0.42f,
                                center = center
                            )
                            drawCircle(color = Color(0xFF090D14), radius = maxR * 0.1f, center = center)
                        }

                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            // Disc / Orb Toggle Chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickable { showHoloDisc = !showHoloDisc }
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (showHoloDisc) Icons.Default.Psychology else Icons.Default.Album,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showHoloDisc) "Switch to Neural Orb" else "Switch to Vinyl Disc",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            // Track Information
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.title,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.toggleFavorite(track.id) },
                        modifier = Modifier.size(32.dp).testTag("track_fav_button")
                    ) {
                        Icon(
                            imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (track.isFavorite) CrimsonWave else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "${track.artist} • ${track.album}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            // Poweramp Fluid Waveform Scrubber
            FluidWaveformScrubber(
                progressFraction = progressFraction,
                isPlaying = uiState.isPlaying,
                currentMs = uiState.currentPositionMs,
                durationMs = uiState.durationMs,
                onSeekFraction = { fraction ->
                    viewModel.seekTo((fraction * uiState.durationMs).toLong())
                }
            )

            // Fluid Transport Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.testTag("shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (uiState.isShuffle) NeonCyan else TextMuted
                    )
                }

                IconButton(
                    onClick = { viewModel.playPrevious() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                        .testTag("prev_track_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Big Tactile Fluid Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NeonCyan, ElectricViolet)))
                        .border(
                            width = if (visualizerFrame.isBeat) 2.5.dp else 1.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable { viewModel.togglePlayPause() }
                        .testTag("main_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        tint = Color(0xFF05070D),
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.playNext() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                        .testTag("next_track_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.cycleRepeatMode() },
                    modifier = Modifier.testTag("repeat_button")
                ) {
                    Icon(
                        imageVector = when (uiState.repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (uiState.repeatMode != RepeatMode.OFF) NeonCyan else TextMuted
                    )
                }
            }

            // Bottom Fullscreen Visualizer Launcher
            Button(
                onClick = { viewModel.selectTab(AppTab.VISUALIZER) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("open_visualizer_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        listOf(NeonCyan.copy(alpha = 0.6f), ElectricViolet.copy(alpha = 0.6f))
                    )
                )
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ENTER 3D HOLOGRAPHIC VISUALIZER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// Poweramp Fluid Waveform Scrubber
@Composable
fun FluidWaveformScrubber(
    progressFraction: Float,
    isPlaying: Boolean,
    currentMs: Long,
    durationMs: Long,
    onSeekFraction: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val effectiveFraction = if (isDragging) dragFraction else progressFraction

    // 40 representative heights for waveform bars
    val waveHeights = remember {
        floatArrayOf(
            0.25f, 0.45f, 0.70f, 0.35f, 0.85f, 0.60f, 0.95f, 0.40f,
            0.75f, 0.90f, 0.55f, 0.65f, 0.80f, 0.30f, 0.90f, 0.70f,
            0.50f, 0.85f, 0.40f, 0.75f, 0.95f, 0.60f, 0.80f, 0.35f,
            0.70f, 0.90f, 0.50f, 0.65f, 0.85f, 0.40f, 0.95f, 0.75f,
            0.60f, 0.80f, 0.45f, 0.70f, 0.85f, 0.50f, 0.65f, 0.30f
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeekFraction(fraction)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeekFraction(dragFraction)
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                }
                .testTag("poweramp_waveform_scrubber")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalWidth = size.width
                val totalHeight = size.height
                val barCount = waveHeights.size
                val barWidth = (totalWidth / barCount) * 0.65f
                val spacing = (totalWidth / barCount) * 0.35f

                for (i in 0 until barCount) {
                    val x = i * (barWidth + spacing)
                    val barFraction = i / (barCount.toFloat() - 1f)
                    val isPast = barFraction <= effectiveFraction

                    val h = totalHeight * waveHeights[i]
                    val y = (totalHeight - h) / 2f

                    val color = if (isPast) NeonCyan else Color.White.copy(alpha = 0.2f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }

                // Fluid Glowing Playhead Needle
                val playheadX = effectiveFraction * totalWidth
                drawLine(
                    color = Color.White,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, totalHeight),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = NeonCyan,
                    radius = 5.dp.toPx(),
                    center = Offset(playheadX, totalHeight / 2f)
                )
            }
        }

        // Time indicators
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayMs = if (isDragging) (dragFraction * durationMs).toLong() else currentMs
            Text(
                text = formatMs(displayMs),
                fontSize = 11.sp,
                color = if (isDragging) NeonCyan else TextMuted,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = formatMs(durationMs),
                fontSize = 11.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// jetAudio Analog Needle VU Meter Component
@Composable
fun VuMeterDial(
    label: String,
    level: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
            val width = size.width
            val height = size.height
            val pivot = Offset(width / 2f, height + 8f)
            val needleLength = height * 1.1f

            // Arc scale background
            drawArc(
                color = SurfaceBorder,
                startAngle = 210f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(width * 0.15f, height * 0.1f),
                size = Size(width * 0.7f, height * 1.6f),
                style = Stroke(width = 3f)
            )

            // Tick marks
            for (i in 0..6) {
                val angle = (210f + (i / 6f) * 120f) * (PI / 180.0)
                val tickColor = if (i >= 5) CrimsonWave else if (i >= 3) AmberGlow else TextMuted
                val r1 = height * 0.85f
                val r2 = height * 1.05f
                val start = Offset(pivot.x + (r1 * cos(angle)).toFloat(), pivot.y + (r1 * sin(angle)).toFloat())
                val end = Offset(pivot.x + (r2 * cos(angle)).toFloat(), pivot.y + (r2 * sin(angle)).toFloat())
                drawLine(color = tickColor, start = start, end = end, strokeWidth = 2f)
            }

            // Needle
            val needleAngle = (210f + level.coerceIn(0f, 1f) * 120f) * (PI / 180.0)
            val needleEnd = Offset(
                pivot.x + (needleLength * cos(needleAngle)).toFloat(),
                pivot.y + (needleLength * sin(needleAngle)).toFloat()
            )
            drawLine(
                color = accentColor,
                start = pivot,
                end = needleEnd,
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            drawCircle(color = accentColor, radius = 4f, center = pivot)
        }
        Text(text = label, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
    }
}

