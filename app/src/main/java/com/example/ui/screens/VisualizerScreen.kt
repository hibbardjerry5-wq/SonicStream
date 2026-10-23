package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VisualizerFrame
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicPlayerViewModel
import com.example.ui.viewmodel.PlayerUiState
import com.example.ui.viewmodel.VisualizerMode
import kotlin.math.*

/**
 * Ultra-Luxury 3D Animated Visualizer Suite
 * Designed with bespoke automotive / Swiss horology / high-fidelity aesthetics.
 */
enum class LuxuryPalette(
    val label: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val highlight: Color,
    val darkGlow: Color
) {
    CHAMPAGNE_GOLD(
        label = "Imperial Gold",
        primary = Color(0xFFFFDF7A),
        secondary = Color(0xFFD4AF37),
        accent = Color(0xFFF59E0B),
        highlight = Color(0xFFFFF9E6),
        darkGlow = Color(0x44D4AF37)
    ),
    DIAMOND_ICE(
        label = "Diamond Ice",
        primary = Color(0xFF00E5FF),
        secondary = Color(0xFF38BDF8),
        accent = Color(0xFF818CF8),
        highlight = Color(0xFFE0F7FF),
        darkGlow = Color(0x4400E5FF)
    ),
    ROYAL_AMETHYST(
        label = "Royal Amethyst",
        primary = Color(0xFFC084FC),
        secondary = Color(0xFFA855F7),
        accent = Color(0xFFE879F9),
        highlight = Color(0xFFFAF5FF),
        darkGlow = Color(0x44A855F7)
    ),
    EMERALD_PRESTIGE(
        label = "Emerald Royale",
        primary = Color(0xFF34D399),
        secondary = Color(0xFF10B981),
        accent = Color(0xFF059669),
        highlight = Color(0xFFECFDF5),
        darkGlow = Color(0x4410B981)
    ),
    MONACO_CRIMSON(
        label = "Monaco GT",
        primary = Color(0xFFFB7185),
        secondary = Color(0xFFF43F5E),
        accent = Color(0xFFE11D48),
        highlight = Color(0xFFFFF1F2),
        darkGlow = Color(0x44F43F5E)
    )
}

// 3D Engine Math Primitives
private data class Point3D(val x: Float, val y: Float, val z: Float)
private data class ProjectedPoint(val x: Float, val y: Float, val scale: Float, val depth: Float)

@Composable
fun VisualizerScreen(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    val visualizerFrame by viewModel.visualizerFrame.collectAsState()
    var showControls by remember { mutableStateOf(true) }
    var selectedPaletteIndex by remember { mutableIntStateOf(0) }
    var autoOrbit by remember { mutableStateOf(true) }
    var showTelemetry by remember { mutableStateOf(false) }

    // Interactive 3D Camera Angles
    var userYaw by remember { mutableFloatStateOf(0f) }
    var userPitch by remember { mutableFloatStateOf(0.18f) }

    // Continuous ultra-fluid 60+ FPS animation clock
    val animationTime by produceState(0f) {
        while (true) {
            withFrameMillis { frameMillis ->
                value = frameMillis / 1000f
            }
        }
    }

    val currentPalette = remember(selectedPaletteIndex) {
        val palettes = LuxuryPalette.values()
        palettes[selectedPaletteIndex % palettes.size]
    }

    // Effective camera rotation angles
    val effectiveYaw = if (autoOrbit) userYaw + (animationTime * 0.42f) else userYaw
    val effectivePitch = userPitch

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06080D))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls },
                    onDoubleTap = {
                        userYaw = 0f
                        userPitch = 0.18f
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    userYaw += dragAmount.x * 0.006f
                    userPitch = (userPitch - dragAmount.y * 0.005f).coerceIn(-0.85f, 0.85f)
                }
            }
            .testTag("visualizer_canvas_container")
    ) {
        // Luxury Deep Space Background Vignette & Ambient Radial Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        currentPalette.darkGlow.copy(alpha = 0.22f),
                        Color(0xFF090D14),
                        Color(0xFF030508)
                    ),
                    center = center,
                    radius = size.maxDimension * 0.75f
                )
            )

            // Luxury metallic grid horizon guide in background
            val gridY = size.height * 0.72f
            drawLine(
                color = currentPalette.secondary.copy(alpha = 0.18f),
                start = Offset(0f, gridY),
                end = Offset(size.width, gridY),
                strokeWidth = 1f
            )
        }

        // 3D Canvas Rendering Engine
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sensitivity = uiState.visualizerSensitivity
            when (uiState.visualizerMode) {
                VisualizerMode.HYPER_TUNNEL_3D -> drawHyperTunnel3D(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime,
                    yaw = effectiveYaw,
                    pitch = effectivePitch
                )
                VisualizerMode.CHRONO_TORUS_3D -> drawChronoTorus3D(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime,
                    yaw = effectiveYaw,
                    pitch = effectivePitch
                )
                VisualizerMode.CHRONOGRAPH_HUD -> drawChronographHud(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime,
                    yaw = effectiveYaw,
                    pitch = effectivePitch
                )
                VisualizerMode.MONOLITH_BARS_3D -> drawMonolithBars3D(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime,
                    yaw = effectiveYaw,
                    pitch = effectivePitch
                )
                VisualizerMode.QUANTUM_VORTEX -> drawQuantumVortex(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime,
                    yaw = effectiveYaw,
                    pitch = effectivePitch
                )
                VisualizerMode.SILK_RIBBON_3D -> drawSilkRibbon3D(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime,
                    yaw = effectiveYaw,
                    pitch = effectivePitch
                )
                VisualizerMode.SPECTRUM_BARS -> drawLuxurySpectrumBars(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime
                )
                VisualizerMode.RADIAL_PULSE -> drawLuxuryRadialPulse(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime
                )
                VisualizerMode.OSCILLOSCOPE -> drawLuxuryOscilloscope(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime
                )
                VisualizerMode.PARTICLE_FIELD -> drawLuxuryParticleBurst(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime
                )
                VisualizerMode.CYBER_GRID -> drawCyberHorizonGrid(
                    frame = visualizerFrame,
                    palette = currentPalette,
                    sensitivity = sensitivity,
                    time = animationTime
                )
            }
        }

        // Luxury Floating UI Overlay (Auto-hideable on touch)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header: Track Identity, 3D Camera Status, Palette Selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, currentPalette.secondary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Track Info & Luxury High-Res Badge
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isPlaying) currentPalette.primary else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MASTER AUDIO • 96kHz / 32-BIT DSP",
                                    color = currentPalette.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.currentTrack.title,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${uiState.currentTrack.artist} — ${uiState.visualizerMode.displayName}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        // Top Quick Controls (Palette, Orbit, Telemetry, Fullscreen)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Palette Switcher
                            IconButton(
                                onClick = {
                                    selectedPaletteIndex = (selectedPaletteIndex + 1) % LuxuryPalette.values().size
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(currentPalette.primary.copy(alpha = 0.18f))
                                    .testTag("vis_palette_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Switch Luxury Palette",
                                    tint = currentPalette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Auto Orbit Toggle
                            IconButton(
                                onClick = { autoOrbit = !autoOrbit },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (autoOrbit) currentPalette.secondary.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f))
                                    .testTag("vis_orbit_toggle_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Toggle 3D Auto-Orbit",
                                    tint = if (autoOrbit) currentPalette.highlight else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Telemetry HUD Toggle
                            IconButton(
                                onClick = { showTelemetry = !showTelemetry },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (showTelemetry) currentPalette.accent.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f))
                                    .testTag("vis_telemetry_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Toggle Telemetry HUD",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Fullscreen Toggle
                            IconButton(
                                onClick = { viewModel.toggleFullscreenVisualizer() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .testTag("fullscreen_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isFullscreenVisualizer) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Toggle Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Optional Luxury Telemetry Strip
                    if (showTelemetry) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .border(1.dp, currentPalette.secondary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bassEnergy = visualizerFrame.frequencyBands.take(4).average().toFloat()
                            val midEnergy = visualizerFrame.frequencyBands.drop(4).take(12).average().toFloat()
                            val trebleEnergy = visualizerFrame.frequencyBands.takeLast(12).average().toFloat()
                            val estDb = (-42f + (bassEnergy * 36f)).coerceIn(-48f, 0f)

                            Text(
                                text = "BASS: ${(bassEnergy * 100).toInt()}%",
                                color = currentPalette.primary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "MID: ${(midEnergy * 100).toInt()}%",
                                color = currentPalette.accent,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "TREBLE: ${(trebleEnergy * 100).toInt()}%",
                                color = currentPalette.highlight,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = String.format("%.1f dB", estDb),
                                color = if (visualizerFrame.isBeat) Color.Red else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Center Gesture Hint (faint and unobtrusive)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (autoOrbit) "Drag to tilt 3D camera • Double-tap to reset" else "Manual 3D Orbit • Drag to rotate",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Bottom Floating Control Deck: Mode Selector, Sensitivity & Transport
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xE60D111A))
                        .border(1.dp, currentPalette.secondary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .padding(14.dp)
                ) {
                    // Mode Selector Carousel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3D VISUALIZER SUITE",
                            color = currentPalette.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = currentPalette.label,
                            color = currentPalette.secondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Scrollable Visualizer Modes Bar
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VisualizerMode.values().forEach { mode ->
                            val selected = uiState.visualizerMode == mode
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setVisualizerMode(mode) },
                                label = {
                                    Text(
                                        text = mode.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (mode) {
                                            VisualizerMode.HYPER_TUNNEL_3D -> Icons.Default.FilterTiltShift
                                            VisualizerMode.CHRONO_TORUS_3D -> Icons.Default.Explore
                                            VisualizerMode.CHRONOGRAPH_HUD -> Icons.Default.Watch
                                            VisualizerMode.MONOLITH_BARS_3D -> Icons.Default.BarChart
                                            VisualizerMode.QUANTUM_VORTEX -> Icons.Default.Flare
                                            VisualizerMode.SILK_RIBBON_3D -> Icons.Default.Waves
                                            VisualizerMode.SPECTRUM_BARS -> Icons.Default.GraphicEq
                                            VisualizerMode.RADIAL_PULSE -> Icons.Default.AllInclusive
                                            VisualizerMode.OSCILLOSCOPE -> Icons.Default.Timeline
                                            VisualizerMode.PARTICLE_FIELD -> Icons.Default.Grain
                                            VisualizerMode.CYBER_GRID -> Icons.Default.BlurOn
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = currentPalette.primary,
                                    selectedLabelColor = Color(0xFF090D14),
                                    selectedLeadingIconColor = Color(0xFF090D14),
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = Color.White,
                                    iconColor = Color.White.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("visualizer_mode_${mode.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Playback Transport & Sensitivity Deck
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Track
                        IconButton(
                            onClick = { viewModel.playPrevious() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .testTag("vis_prev_button")
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
                        }

                        // Play / Pause with beat-synced aura
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(currentPalette.primary)
                                .border(
                                    width = if (visualizerFrame.isBeat) 3.dp else 1.dp,
                                    color = currentPalette.highlight,
                                    shape = CircleShape
                                )
                                .testTag("vis_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play Pause",
                                tint = Color(0xFF090D14),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Next Track
                        IconButton(
                            onClick = { viewModel.playNext() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .testTag("vis_next_button")
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                        }

                        // Compact Sensitivity Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .widthIn(min = 120.dp, max = 180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = currentPalette.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Slider(
                                value = uiState.visualizerSensitivity,
                                onValueChange = { viewModel.setVisualizerSensitivity(it) },
                                valueRange = 0.5f..2.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = currentPalette.primary,
                                    activeTrackColor = currentPalette.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("visualizer_sensitivity_slider")
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3D PERSPECTIVE MATH HELPERS
// =========================================================================

private fun rotate3D(point: Point3D, yaw: Float, pitch: Float, roll: Float = 0f): Point3D {
    // Yaw around Y
    val cosY = cos(yaw)
    val sinY = sin(yaw)
    val x1 = point.x * cosY + point.z * sinY
    val z1 = -point.x * sinY + point.z * cosY
    val y1 = point.y

    // Pitch around X
    val cosP = cos(pitch)
    val sinP = sin(pitch)
    val y2 = y1 * cosP - z1 * sinP
    val z2 = y1 * sinP + z1 * cosP
    val x2 = x1

    if (roll == 0f) return Point3D(x2, y2, z2)

    // Roll around Z
    val cosR = cos(roll)
    val sinR = sin(roll)
    val x3 = x2 * cosR - y2 * sinR
    val y3 = x2 * sinR + y2 * cosR
    return Point3D(x3, y3, z2)
}

private fun projectPoint(
    point: Point3D,
    cx: Float,
    cy: Float,
    fov: Float = 750f,
    cameraDist: Float = 700f
): ProjectedPoint? {
    val zEff = point.z + cameraDist
    if (zEff <= 15f) return null
    val scale = fov / zEff
    return ProjectedPoint(
        x = cx + point.x * scale,
        y = cy + point.y * scale,
        scale = scale,
        depth = zEff
    )
}

// =========================================================================
// 1. 3D HYPER-TUNNEL (WORMHOLE)
// =========================================================================
private fun DrawScope.drawHyperTunnel3D(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float,
    yaw: Float,
    pitch: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val bands = frame.frequencyBands
    val ringCount = 18
    val verticesPerRing = 10
    val maxZ = 1600f
    val tunnelSpeed = (180f + (if (frame.isBeat) 320f else 90f) * sensitivity)
    val zOffset = (time * tunnelSpeed) % (maxZ / ringCount)

    val ringsProjected = mutableListOf<List<ProjectedPoint>>()

    for (r in 0 until ringCount) {
        val rawZ = (r * (maxZ / ringCount)) - zOffset
        val z = if (rawZ < 0f) rawZ + maxZ else rawZ
        val ringIndexNorm = r / ringCount.toFloat()
        val twist = (ringIndexNorm * PI * 1.5f).toFloat() + (time * 0.25f)

        val ringVertices = mutableListOf<ProjectedPoint>()
        val baseRadius = 260f * (1f + (bands[r % bands.size] * sensitivity * 0.6f))

        for (v in 0 until verticesPerRing) {
            val angle = (v * (2.0 * PI / verticesPerRing)).toFloat() + twist
            val radius = baseRadius * (1f + 0.15f * sin(v * 3f + time * 2f))
            val x = radius * cos(angle)
            val y = radius * sin(angle)

            val rotated = rotate3D(Point3D(x, y, z - 800f), yaw, pitch)
            val projected = projectPoint(rotated, cx, cy)
            if (projected != null) {
                ringVertices.add(projected)
            }
        }
        if (ringVertices.size == verticesPerRing) {
            ringsProjected.add(ringVertices)
        }
    }

    // Draw connecting longitudinal perspective rails
    for (v in 0 until verticesPerRing) {
        val railPath = Path()
        var started = false
        for (r in ringsProjected.indices) {
            val pt = ringsProjected[r][v]
            if (!started) {
                railPath.moveTo(pt.x, pt.y)
                started = true
            } else {
                railPath.lineTo(pt.x, pt.y)
            }
        }
        drawPath(
            path = railPath,
            color = palette.secondary.copy(alpha = 0.28f),
            style = Stroke(width = 1.5f)
        )
    }

    // Draw geometric polygon rings from back to front
    for (r in ringsProjected.indices.reversed()) {
        val pts = ringsProjected[r]
        val depthFactor = (1f - (r.toFloat() / ringCount)).coerceIn(0.15f, 1f)
        val strokeWidth = (depthFactor * 3.5f).coerceAtLeast(1f)
        val ringColor = lerpColor(palette.secondary, palette.primary, depthFactor)
            .copy(alpha = (depthFactor * 0.85f).coerceIn(0.2f, 1f))

        val ringPath = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) {
                lineTo(pts[i].x, pts[i].y)
            }
            close()
        }

        drawPath(
            path = ringPath,
            color = ringColor,
            style = Stroke(width = strokeWidth)
        )

        // Add shimmering vertex gems
        for (pt in pts) {
            drawCircle(
                color = palette.highlight.copy(alpha = depthFactor),
                radius = 2.5f * pt.scale,
                center = Offset(pt.x, pt.y)
            )
        }
    }
}

// =========================================================================
// 2. 3D CHRONO-TORUS (ASTRONOMICAL GYROSCOPE)
// =========================================================================
private fun DrawScope.drawChronoTorus3D(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float,
    yaw: Float,
    pitch: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val bands = frame.frequencyBands
    val wave = frame.waveform

    // Outer Gyroscope Chrono Ring (60 etched horology ticks)
    val outerRadius = 260f
    val tickCount = 48
    for (i in 0 until tickCount) {
        val angle = (i * 2.0 * PI / tickCount).toFloat()
        val r1 = outerRadius - 10f
        val r2 = outerRadius + (if (i % 4 == 0) 14f else 4f)

        val p1 = rotate3D(Point3D(r1 * cos(angle), r1 * sin(angle), 0f), yaw + time * 0.2f, pitch)
        val p2 = rotate3D(Point3D(r2 * cos(angle), r2 * sin(angle), 0f), yaw + time * 0.2f, pitch)

        val pr1 = projectPoint(p1, cx, cy)
        val pr2 = projectPoint(p2, cx, cy)
        if (pr1 != null && pr2 != null) {
            val isMajor = i % 4 == 0
            drawLine(
                color = if (isMajor) palette.primary else palette.secondary.copy(alpha = 0.5f),
                start = Offset(pr1.x, pr1.y),
                end = Offset(pr2.x, pr2.y),
                strokeWidth = if (isMajor) 2.5f else 1.2f
            )
        }
    }

    // Middle Ring: Waveform Modulated Spiked Crown
    val midRadius = 180f
    val waveStep = wave.size / tickCount
    val midPts = mutableListOf<ProjectedPoint>()
    for (i in 0 until tickCount) {
        val angle = (i * 2.0 * PI / tickCount).toFloat()
        val waveVal = wave[(i * waveStep) % wave.size] * sensitivity
        val r = midRadius + (waveVal * 45f)
        val p = rotate3D(
            Point3D(r * cos(angle), r * sin(angle), sin(i * 0.5f + time * 3f) * 20f),
            yaw - time * 0.35f,
            pitch + 0.3f
        )
        val pr = projectPoint(p, cx, cy)
        if (pr != null) midPts.add(pr)
    }
    if (midPts.size >= 3) {
        val path = Path().apply {
            moveTo(midPts[0].x, midPts[0].y)
            for (j in 1 until midPts.size) lineTo(midPts[j].x, midPts[j].y)
            close()
        }
        drawPath(path, palette.accent, style = Stroke(width = 3f))
    }

    // Inner Core: Floating Pulsing Octahedron
    val beatScale = if (frame.isBeat) 1.35f else 1.0f
    val coreSize = 75f * beatScale * (1f + bands[0] * sensitivity * 0.5f)
    val vertices = listOf(
        Point3D(0f, -coreSize, 0f),
        Point3D(coreSize, 0f, 0f),
        Point3D(0f, 0f, coreSize),
        Point3D(-coreSize, 0f, 0f),
        Point3D(0f, 0f, -coreSize),
        Point3D(0f, coreSize, 0f)
    )
    val edges = listOf(
        0 to 1, 0 to 2, 0 to 3, 0 to 4,
        5 to 1, 5 to 2, 5 to 3, 5 to 4,
        1 to 2, 2 to 3, 3 to 4, 4 to 1
    )
    val projVertices = vertices.map {
        projectPoint(rotate3D(it, yaw + time * 0.8f, pitch + time * 0.5f), cx, cy)
    }

    for ((i1, i2) in edges) {
        val v1 = projVertices[i1]
        val v2 = projVertices[i2]
        if (v1 != null && v2 != null) {
            drawLine(
                color = palette.highlight,
                start = Offset(v1.x, v1.y),
                end = Offset(v2.x, v2.y),
                strokeWidth = 2.5f
            )
        }
    }
}

// =========================================================================
// 3. LUXURY CHRONOGRAPH HUD (SWISS HOROLOGY & TWIN GT COCKPIT)
// =========================================================================
private fun DrawScope.drawChronographHud(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float,
    yaw: Float,
    pitch: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val bands = frame.frequencyBands
    val bassAvg = bands.take(6).average().toFloat() * sensitivity
    val trebleAvg = bands.takeLast(10).average().toFloat() * sensitivity

    val dialRadius = (size.minDimension * 0.22f).coerceIn(90f, 160f)
    val separation = dialRadius * 1.35f

    // Left Dial: Dynamic RMS / Tachometer Gauge
    val leftCenter = Offset(cx - separation, cy)
    drawLuxuryGauge(
        center = leftCenter,
        radius = dialRadius,
        value = bassAvg.coerceIn(0f, 1f),
        label = "BASS / RMS",
        palette = palette,
        unit = "dB"
    )

    // Right Dial: High-Frequency / Turbo Harmonics Gauge
    val rightCenter = Offset(cx + separation, cy)
    drawLuxuryGauge(
        center = rightCenter,
        radius = dialRadius,
        value = trebleAvg.coerceIn(0f, 1f),
        label = "HARMONICS",
        palette = palette,
        unit = "kHz"
    )

    // Center Lissajous Audio Wave Scope
    val wave = frame.waveform
    val lissajousPath = Path()
    val scopeRadius = dialRadius * 0.7f
    val halfWave = wave.size / 2

    for (i in 0 until halfWave) {
        val wX = wave[i] * sensitivity
        val wY = wave[i + halfWave] * sensitivity
        val x = cx + (wX * scopeRadius * 1.4f)
        val y = cy + (wY * scopeRadius * 1.4f)
        if (i == 0) lissajousPath.moveTo(x, y) else lissajousPath.lineTo(x, y)
    }

    // Outer Scope Ring
    drawCircle(
        color = palette.secondary.copy(alpha = 0.35f),
        radius = scopeRadius,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )
    drawPath(
        path = lissajousPath,
        color = palette.highlight,
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawLuxuryGauge(
    center: Offset,
    radius: Float,
    value: Float,
    label: String,
    palette: LuxuryPalette,
    unit: String
) {
    // Outer Bezel Ring
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF151922), Color(0xFF090D14)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
    drawCircle(
        color = palette.secondary,
        radius = radius,
        center = center,
        style = Stroke(width = 3f)
    )

    // Ticks (240 degree arc from 150 deg to 390 deg)
    val startAngle = 140f
    val sweepAngle = 260f
    val tickCount = 26
    for (i in 0..tickCount) {
        val frac = i / tickCount.toFloat()
        val deg = startAngle + frac * sweepAngle
        val rad = Math.toRadians(deg.toDouble()).toFloat()
        val isMajor = i % 5 == 0
        val r1 = radius - (if (isMajor) 16f else 8f)
        val r2 = radius - 4f

        val start = Offset(center.x + r1 * cos(rad), center.y + r1 * sin(rad))
        val end = Offset(center.x + r2 * cos(rad), center.y + r2 * sin(rad))

        val tickColor = if (frac > 0.82f) Color(0xFFF43F5E) else if (isMajor) palette.primary else palette.secondary.copy(alpha = 0.5f)
        drawLine(
            color = tickColor,
            start = start,
            end = end,
            strokeWidth = if (isMajor) 2.5f else 1.2f
        )
    }

    // Active Value Arc
    drawArc(
        color = palette.primary,
        startAngle = startAngle,
        sweepAngle = sweepAngle * value,
        useCenter = false,
        topLeft = Offset(center.x - radius + 10f, center.y - radius + 10f),
        size = Size((radius - 10f) * 2f, (radius - 10f) * 2f),
        style = Stroke(width = 4f, cap = StrokeCap.Round)
    )

    // Swiss Chrono Needle
    val needleAngle = startAngle + sweepAngle * value
    val needleRad = Math.toRadians(needleAngle.toDouble()).toFloat()
    val needleLen = radius * 0.78f
    val needleTip = Offset(center.x + needleLen * cos(needleRad), center.y + needleLen * sin(needleRad))
    val needleCounter = Offset(center.x - 16f * cos(needleRad), center.y - 16f * sin(needleRad))

    drawLine(
        color = palette.highlight,
        start = needleCounter,
        end = needleTip,
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    // Needle Hub / Jewel
    drawCircle(color = palette.secondary, radius = 7f, center = center)
    drawCircle(color = Color.Black, radius = 3.5f, center = center)
}

// =========================================================================
// 4. 3D MONOLITH MATRIX (ISOMETRIC PILLARS)
// =========================================================================
private fun DrawScope.drawMonolithBars3D(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float,
    yaw: Float,
    pitch: Float
) {
    val cx = size.width / 2f
    val cy = size.height * 0.65f
    val bands = frame.frequencyBands
    val rows = 4
    val cols = 8
    val pillarW = 28f
    val pillarD = 28f
    val spacingX = 42f
    val spacingZ = 45f

    // Precalculate and depth-sort pillars
    data class PillarRender(val points: List<ProjectedPoint>, val depth: Float, val bandVal: Float)
    val pillars = mutableListOf<PillarRender>()

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val bandIdx = (r * cols + c) % bands.size
            val bandVal = (bands[bandIdx] * sensitivity).coerceIn(0.05f, 1.2f)
            val h = 18f + (bandVal * 160f)

            val originX = (c - (cols / 2f - 0.5f)) * spacingX
            val originZ = (r - (rows / 2f - 0.5f)) * spacingZ

            // 8 vertices of the 3D box
            val rawVertices = listOf(
                Point3D(originX - pillarW / 2f, 0f, originZ - pillarD / 2f),
                Point3D(originX + pillarW / 2f, 0f, originZ - pillarD / 2f),
                Point3D(originX + pillarW / 2f, 0f, originZ + pillarD / 2f),
                Point3D(originX - pillarW / 2f, 0f, originZ + pillarD / 2f),
                Point3D(originX - pillarW / 2f, -h, originZ - pillarD / 2f),
                Point3D(originX + pillarW / 2f, -h, originZ - pillarD / 2f),
                Point3D(originX + pillarW / 2f, -h, originZ + pillarD / 2f),
                Point3D(originX - pillarW / 2f, -h, originZ + pillarD / 2f)
            )

            val rotated = rawVertices.map { rotate3D(it, yaw, pitch) }
            val avgDepth = rotated.map { it.z }.average().toFloat()
            val projected = rotated.mapNotNull { projectPoint(it, cx, cy) }

            if (projected.size == 8) {
                pillars.add(PillarRender(projected, avgDepth, bandVal))
            }
        }
    }

    // Sort back-to-front by depth
    pillars.sortByDescending { it.depth }

    for (pillar in pillars) {
        val pts = pillar.points
        val fraction = pillar.bandVal
        val topColor = lerpColor(palette.primary, palette.highlight, fraction)
        val sideColor = lerpColor(palette.secondary, palette.accent, fraction).copy(alpha = 0.85f)

        // Draw Front Face (pts 4, 5, 1, 0)
        val frontPath = Path().apply {
            moveTo(pts[4].x, pts[4].y)
            lineTo(pts[5].x, pts[5].y)
            lineTo(pts[1].x, pts[1].y)
            lineTo(pts[0].x, pts[0].y)
            close()
        }
        drawPath(frontPath, sideColor)

        // Draw Right Face (pts 5, 6, 2, 1)
        val rightPath = Path().apply {
            moveTo(pts[5].x, pts[5].y)
            lineTo(pts[6].x, pts[6].y)
            lineTo(pts[2].x, pts[2].y)
            lineTo(pts[1].x, pts[1].y)
            close()
        }
        drawPath(rightPath, sideColor.copy(alpha = 0.7f))

        // Draw Glowing Top Cap (pts 4, 5, 6, 7)
        val topPath = Path().apply {
            moveTo(pts[4].x, pts[4].y)
            lineTo(pts[5].x, pts[5].y)
            lineTo(pts[6].x, pts[6].y)
            lineTo(pts[7].x, pts[7].y)
            close()
        }
        drawPath(topPath, topColor)
        drawPath(topPath, Color.White, style = Stroke(width = 1.2f))
    }
}

// =========================================================================
// 5. QUANTUM STARFIELD VORTEX
// =========================================================================
private fun DrawScope.drawQuantumVortex(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float,
    yaw: Float,
    pitch: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val bands = frame.frequencyBands
    val particleCount = 140
    val beatWarp = if (frame.isBeat) 2.2f else 1.0f

    data class ParticleProj(val pt: ProjectedPoint, val size: Float, val alpha: Float, val color: Color)
    val particles = mutableListOf<ParticleProj>()

    for (i in 0 until particleCount) {
        val bandVal = bands[i % bands.size] * sensitivity
        val spiralAngle = (i * 0.18f) + (time * (0.4f + bandVal * 0.5f))
        val radius = 40f + (i * 3.2f) * beatWarp
        val z = ((i * 14f + time * 320f * beatWarp) % 1500f) - 600f

        val p = Point3D(
            x = radius * cos(spiralAngle),
            y = radius * sin(spiralAngle),
            z = z
        )
        val rotated = rotate3D(p, yaw, pitch)
        val proj = projectPoint(rotated, cx, cy)
        if (proj != null) {
            val depthRatio = (1f - (proj.depth / 1600f)).coerceIn(0.1f, 1f)
            val pColor = if (i % 2 == 0) palette.primary else palette.accent
            particles.add(
                ParticleProj(
                    pt = proj,
                    size = (2f + depthRatio * 7f * (1f + bandVal)),
                    alpha = depthRatio,
                    color = pColor
                )
            )
        }
    }

    particles.sortBy { it.pt.depth }
    for (p in particles) {
        drawCircle(
            color = p.color.copy(alpha = p.alpha),
            radius = p.size,
            center = Offset(p.pt.x, p.pt.y)
        )
        // Luminous core
        drawCircle(
            color = Color.White.copy(alpha = p.alpha * 0.8f),
            radius = p.size * 0.4f,
            center = Offset(p.pt.x, p.pt.y)
        )
    }
}

// =========================================================================
// 6. 3D SILK RIBBON WAVE
// =========================================================================
private fun DrawScope.drawSilkRibbon3D(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float,
    yaw: Float,
    pitch: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val wave = frame.waveform
    val pointsCount = wave.size
    val ribbonWidth = 45f

    val topPts = mutableListOf<ProjectedPoint>()
    val bottomPts = mutableListOf<ProjectedPoint>()

    for (i in 0 until pointsCount) {
        val t = (i / (pointsCount - 1f)) - 0.5f
        val x = t * (size.width * 0.95f)
        val waveAmp = wave[i] * sensitivity * 110f
        val y1 = waveAmp - ribbonWidth / 2f
        val y2 = waveAmp + ribbonWidth / 2f
        val z = sin(t * PI * 2.5f + time * 2.2f).toFloat() * 120f

        val pTop = projectPoint(rotate3D(Point3D(x, y1, z), yaw, pitch), cx, cy)
        val pBot = projectPoint(rotate3D(Point3D(x, y2, z), yaw, pitch), cx, cy)

        if (pTop != null && pBot != null) {
            topPts.add(pTop)
            bottomPts.add(pBot)
        }
    }

    if (topPts.size >= 4) {
        // Continuous silky ribbon mesh
        val ribbonPath = Path().apply {
            moveTo(topPts[0].x, topPts[0].y)
            for (i in 1 until topPts.size) lineTo(topPts[i].x, topPts[i].y)
            for (i in bottomPts.indices.reversed()) lineTo(bottomPts[i].x, bottomPts[i].y)
            close()
        }

        drawPath(
            path = ribbonPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    palette.secondary.copy(alpha = 0.3f),
                    palette.primary.copy(alpha = 0.85f),
                    palette.accent.copy(alpha = 0.9f),
                    palette.highlight.copy(alpha = 0.4f)
                )
            )
        )

        // Crest Highlight line
        val crestPath = Path().apply {
            moveTo(topPts[0].x, topPts[0].y)
            for (i in 1 until topPts.size) lineTo(topPts[i].x, topPts[i].y)
        }
        drawPath(crestPath, palette.highlight, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
    }
}

// =========================================================================
// 7. LUXURY DUAL SPECTRUM BARS (WITH REFLECTION & PEAKS)
// =========================================================================
private fun DrawScope.drawLuxurySpectrumBars(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float
) {
    val bands = frame.frequencyBands
    val bandCount = bands.size
    val totalWidth = size.width
    val centerY = size.height * 0.58f
    val barWidth = (totalWidth / (bandCount * 2f)) * 0.72f
    val spacing = (totalWidth / (bandCount * 2f)) * 0.28f

    for (i in 0 until bandCount) {
        val rawAmp = bands[i] * sensitivity
        val amp = (rawAmp * (size.height * 0.38f)).coerceIn(8f, size.height * 0.45f)
        val reflectionAmp = amp * 0.35f

        val colorFraction = i / (bandCount.toFloat() - 1f)
        val barColor = lerpColor(palette.primary, palette.secondary, colorFraction)

        val xRight = (totalWidth / 2f) + (i * (barWidth + spacing))
        val xLeft = (totalWidth / 2f) - ((i + 1) * (barWidth + spacing))

        listOf(xRight, xLeft).forEach { xPos ->
            // Upper Bar with Luxury Gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.highlight, barColor, barColor.copy(alpha = 0.6f)),
                    startY = centerY - amp,
                    endY = centerY
                ),
                topLeft = Offset(xPos, centerY - amp),
                size = Size(barWidth, amp),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )

            // Glossy Floor Reflection
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(barColor.copy(alpha = 0.45f), Color.Transparent),
                    startY = centerY + 3f,
                    endY = centerY + reflectionAmp
                ),
                topLeft = Offset(xPos, centerY + 3f),
                size = Size(barWidth, reflectionAmp),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )

            // Floating Peak Dot
            drawCircle(
                color = palette.highlight,
                radius = barWidth * 0.45f,
                center = Offset(xPos + barWidth / 2f, centerY - amp - 5f)
            )
        }
    }
}

// =========================================================================
// 8. LUXURY RADIAL PULSE
// =========================================================================
private fun DrawScope.drawLuxuryRadialPulse(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val baseRadius = (size.minDimension * 0.23f) * (1f + (if (frame.isBeat) 0.16f else 0f))
    val bands = frame.frequencyBands
    val bandCount = bands.size

    // Pulsing Outer Aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.primary.copy(alpha = 0.3f), Color.Transparent),
            center = center,
            radius = baseRadius * 1.6f
        ),
        radius = baseRadius * 1.55f,
        center = center
    )

    // Inner Metallic Disc
    drawCircle(color = Color(0xFF10141E), radius = baseRadius, center = center)
    drawCircle(color = palette.secondary, radius = baseRadius, center = center, style = Stroke(width = 3.5f))

    // Radial Rays
    val angleStep = (2.0 * PI / bandCount)
    for (i in 0 until bandCount) {
        val angle = i * angleStep + (time * 0.25f)
        val rayLen = (bands[i] * sensitivity * (size.minDimension * 0.3f)).coerceIn(12f, size.minDimension * 0.36f)
        val startX = center.x + (baseRadius * cos(angle)).toFloat()
        val startY = center.y + (baseRadius * sin(angle)).toFloat()
        val endX = center.x + ((baseRadius + rayLen) * cos(angle)).toFloat()
        val endY = center.y + ((baseRadius + rayLen) * sin(angle)).toFloat()

        val color = lerpColor(palette.primary, palette.accent, i / bandCount.toFloat())
        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 5.5f,
            cap = StrokeCap.Round
        )

        drawCircle(color = palette.highlight, radius = 3.5f, center = Offset(endX, endY))
    }
}

// =========================================================================
// 9. LUXURY OSCILLOSCOPE
// =========================================================================
private fun DrawScope.drawLuxuryOscilloscope(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float
) {
    val centerY = size.height / 2f
    val wave = frame.waveform
    val pointsCount = wave.size
    val stepX = size.width / (pointsCount - 1f)

    // Grid Graticule Lines (Luxury Lab Style)
    for (i in -3..3) {
        val y = centerY + (i * 45f)
        drawLine(
            color = palette.secondary.copy(alpha = 0.14f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
    }

    val path = Path()
    val harmonicPath = Path()

    for (i in 0 until pointsCount) {
        val x = i * stepX
        val y = centerY + (wave[i] * (size.height * 0.22f) * sensitivity)
        val yHarmonic = centerY - (wave[i] * (size.height * 0.16f) * sensitivity * 0.75f)

        if (i == 0) {
            path.moveTo(x, y)
            harmonicPath.moveTo(x, yHarmonic)
        } else {
            path.lineTo(x, y)
            harmonicPath.lineTo(x, yHarmonic)
        }
    }

    // Glow trace
    drawPath(path = path, color = palette.primary.copy(alpha = 0.35f), style = Stroke(width = 10f, cap = StrokeCap.Round))
    drawPath(path = path, color = palette.highlight, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
    drawPath(path = harmonicPath, color = palette.accent.copy(alpha = 0.75f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
}

// =========================================================================
// 10. LUXURY PARTICLE BURST
// =========================================================================
private fun DrawScope.drawLuxuryParticleBurst(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val bands = frame.frequencyBands
    val beatMultiplier = if (frame.isBeat) 1.6f else 1.0f

    for (i in 0 until 84) {
        val angle = (i * 137.5 * PI / 180.0) + (time * 0.3f)
        val bandVal = bands[i % bands.size] * sensitivity
        val distance = ((i * 4.2f + 35f) * (1f + bandVal * 0.85f) * beatMultiplier) % (size.minDimension * 0.48f)
        val x = center.x + (distance * cos(angle)).toFloat()
        val y = center.y + (distance * sin(angle)).toFloat()

        val pRadius = (2.5f + (bandVal * 4.5f)).coerceIn(2.5f, 9.5f)
        val color = if (i % 2 == 0) palette.primary else palette.secondary

        drawCircle(
            color = color.copy(alpha = (0.35f + bandVal * 0.65f).coerceIn(0.2f, 1f)),
            radius = pRadius,
            center = Offset(x, y)
        )
        drawCircle(
            color = palette.highlight,
            radius = pRadius * 0.4f,
            center = Offset(x, y)
        )
    }
}

// =========================================================================
// 11. CYBER HORIZON GRID (3D SYNTHWAVE GT)
// =========================================================================
private fun DrawScope.drawCyberHorizonGrid(
    frame: VisualizerFrame,
    palette: LuxuryPalette,
    sensitivity: Float,
    time: Float
) {
    val horizonY = size.height * 0.48f
    val bassEnergy = frame.frequencyBands.take(4).average().toFloat() * sensitivity

    // Neon Sun on Horizon
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.primary, palette.accent, Color.Transparent),
            center = Offset(size.width / 2f, horizonY),
            radius = size.width * 0.32f
        ),
        radius = size.width * 0.32f,
        center = Offset(size.width / 2f, horizonY)
    )

    // Horizontal perspective grid lines
    val linesCount = 15
    for (i in 1..linesCount) {
        val t = (i.toFloat() / linesCount).pow(2.1f)
        val y = horizonY + t * (size.height - horizonY)
        val waveDisplacement = sin(i + bassEnergy * 4f + time * 3f) * (bassEnergy * 14f)
        drawLine(
            color = palette.primary.copy(alpha = (0.2f + t * 0.65f)),
            start = Offset(0f, y + waveDisplacement),
            end = Offset(size.width, y + waveDisplacement),
            strokeWidth = (1f + t * 3.5f)
        )
    }

    // Vertical converging perspective lines
    val verticalCount = 14
    for (j in 0..verticalCount) {
        val xBottom = (j.toFloat() / verticalCount) * size.width
        val xTop = (size.width / 2f) + ((xBottom - (size.width / 2f)) * 0.12f)
        drawLine(
            color = palette.secondary.copy(alpha = 0.5f),
            start = Offset(xTop, horizonY),
            end = Offset(xBottom, size.height),
            strokeWidth = 2f
        )
    }
}

private fun lerpColor(c1: Color, c2: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = c1.red + (c2.red - c1.red) * f,
        green = c1.green + (c2.green - c1.green) * f,
        blue = c1.blue + (c2.blue - c1.blue) * f,
        alpha = c1.alpha + (c2.alpha - c1.alpha) * f
    )
}
