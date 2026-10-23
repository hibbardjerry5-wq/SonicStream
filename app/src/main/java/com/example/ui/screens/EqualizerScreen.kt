package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.model.PublicSafetyFeeds
import com.example.model.ScannerChannel
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicPlayerViewModel
import com.example.ui.viewmodel.PlayerUiState
import kotlin.math.abs
import kotlin.math.roundToInt

enum class SoundRackTab(val title: String) {
    AI_AUTO_TUNER("AI Auto-Tuner"),
    EQUALIZER("5-Band EQ"),
    CAR_STEREO("Car Stereo"),
    JETAUDIO("jetAudio"),
    SCANNER_911("911 Scanner")
}

@Composable
fun EqualizerScreen(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    var selectedRack by remember { mutableStateOf(SoundRackTab.AI_AUTO_TUNER) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .testTag("equalizer_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sound Studio",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = ElectricViolet.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AI POWERED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricViolet,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Acoustic Tuning & Environment Engine",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Car Mode button
                IconButton(
                    onClick = { viewModel.toggleCarMode() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isCarModeActive) NeonCyan else SurfaceCard)
                        .testTag("eq_car_mode_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Car Mode",
                        tint = if (uiState.isCarModeActive) Color.Black else NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.triggerCloudSync() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .testTag("eq_save_cloud_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Sync EQ to Cloud",
                        tint = if (uiState.isCloudSignedIn) NeonCyan else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Sub-Navigation Tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedRack.ordinal,
            containerColor = SurfaceDark,
            contentColor = NeonCyan,
            edgePadding = 16.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedRack.ordinal]),
                    color = NeonCyan,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth().testTag("eq_sub_tabs")
        ) {
            SoundRackTab.values().forEach { tab ->
                val isSelected = selectedRack == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedRack = tab },
                    text = {
                        Text(
                            text = tab.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NeonCyan else TextMuted
                        )
                    },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedRack) {
                SoundRackTab.AI_AUTO_TUNER -> AiAutoTunerTab(viewModel = viewModel, uiState = uiState)
                SoundRackTab.EQUALIZER -> EqGraphicTab(viewModel = viewModel, uiState = uiState)
                SoundRackTab.CAR_STEREO -> CarStereoTab(viewModel = viewModel, uiState = uiState)
                SoundRackTab.JETAUDIO -> JetAudioRackTab(viewModel = viewModel, uiState = uiState)
                SoundRackTab.SCANNER_911 -> Scanner911Tab(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}

// ---------------------------------------------------------
// TAB 0: AI Acoustic Environment Auto-Tuner & Compensator
// ---------------------------------------------------------
@Composable
private fun AiAutoTunerTab(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState
) {
    val scrollState = rememberScrollState()

    // Microphone permission launcher for real environmental acoustic analysis
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleAiListening(true)
        }
    }

    // Animated listening pulse
    val infiniteTransition = rememberInfiniteTransition(label = "AiPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AiPulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("ai_auto_tuner_tab")
    ) {
        // Hero Ambient SPL & Environment Acoustic Meter
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    if (uiState.isCalibratingNow) listOf(CrimsonWave, AmberGlow)
                    else listOf(ElectricViolet, NeonCyan)
                )
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isAiListeningMic) NeonCyan.copy(alpha = 0.2f)
                                    else ElectricViolet.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isAiListeningMic) Icons.Default.Mic else Icons.Default.AutoAwesome,
                                contentDescription = "AI Acoustic",
                                tint = if (uiState.isAiListeningMic) NeonCyan else ElectricViolet,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ambient Acoustic Analyzer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isAiListeningMic) NeonCyan else AmberGlow)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isAiListeningMic) "Live Mic Sensor Active" else "Acoustic Simulation Active",
                                    fontSize = 11.sp,
                                    color = if (uiState.isAiListeningMic) NeonCyan else AmberGlow
                                )
                            }
                        }
                    }

                    // Mic Permission / Toggle Button
                    OutlinedButton(
                        onClick = {
                            if (!uiState.isAiListeningMic) {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.toggleAiListening(false)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("toggle_mic_btn")
                    ) {
                        Text(
                            text = if (uiState.isAiListeningMic) "Mute Mic" else "Enable Mic",
                            fontSize = 11.sp,
                            color = if (uiState.isAiListeningMic) TextMuted else NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live SPL Decibel Meter & Environment Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "NOISE FLOOR LEVEL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${uiState.ambientSplDb.roundToInt()}",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    uiState.ambientSplDb > 75f -> CrimsonWave
                                    uiState.ambientSplDb > 60f -> AmberGlow
                                    else -> NeonCyan
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "dB SPL",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = SurfaceDark,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = uiState.ambientEnvironmentType,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Status: ${uiState.aiCalibrationState}",
                            fontSize = 11.sp,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Linear Noise Level Bar
                val splProgress = ((uiState.ambientSplDb - 30f) / 75f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { splProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        uiState.ambientSplDb > 75f -> CrimsonWave
                        uiState.ambientSplDb > 60f -> AmberGlow
                        else -> NeonCyan
                    },
                    trackColor = SurfaceDark
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Super-Intelligent Neural Directive Console
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEURAL PROMPT SYNTHESIZER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Command Gemini 3.5 with natural language acoustic directives.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                var promptText by remember { mutableStateOf(uiState.neuralPromptInput) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = {
                            promptText = it
                            viewModel.setNeuralPromptInput(it)
                        },
                        modifier = Modifier.weight(1f).testTag("tab_neural_prompt_input"),
                        placeholder = { Text("e.g., Deep cyber sub-bass in rain...", fontSize = 11.sp, color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.executeNeuralSonicDirective(promptText) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                            .testTag("tab_execute_neural_btn")
                    ) {
                        if (uiState.isNeuralProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Execute", tint = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Quantum Directive Chips
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Deep Cyber Sub-Warp",
                        "Hyper-Spatial 360°",
                        "Neural Room Inversion",
                        "Crystal Laser Vocals",
                        "Cyberpunk Highway"
                    ).forEach { chip ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.06f),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SurfaceBorder, NeonCyan.copy(alpha = 0.3f)))),
                            modifier = Modifier.clickable {
                                promptText = chip
                                viewModel.setNeuralPromptInput(chip)
                                viewModel.executeNeuralSonicDirective(chip)
                            }
                        ) {
                            Text(
                                text = chip,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Big Tactile Action: Calibrate Room Acoustics Now
        Button(
            onClick = { viewModel.runAiAcousticCalibration() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("ai_calibrate_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (uiState.isCalibratingNow) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.Black,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AI CALIBRATING ROOM ACOUSTICS...",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ONE-TAP AI ACOUSTIC CALIBRATION",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Continuous Adaptive Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Continuous Live Adaptive EQ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Constantly monitors ambient noise (e.g. entering tunnel, road speed) and smoothly shifts EQ gains",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = uiState.isLiveAdaptiveAiEnabled,
                    onCheckedChange = { viewModel.toggleLiveAdaptiveAi(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("live_adaptive_ai_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // AI Acoustic Master Rationale & Insights Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SurfaceBorder, ElectricViolet.copy(alpha = 0.3f))))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Sound Engineer Analysis",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        color = ElectricViolet.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = uiState.aiLastTunedSource,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricViolet,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI Explanation Quote
                Surface(
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${uiState.aiExplanation}\"",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown of AI Adjusted Gains
                Text(
                    text = "AI COMPENSATED DSP PARAMETERS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labels = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")
                    labels.forEachIndexed { i, label ->
                        val gain = uiState.equalizerBands.getOrElse(i) { 0 }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = "${if (gain > 0) "+" else ""}$gain",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (gain > 0) NeonCyan else if (gain < 0) CrimsonWave else TextSecondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bass", fontSize = 10.sp, color = TextMuted)
                        Text("${uiState.bassBoost / 10}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricViolet)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Loudness", fontSize = 10.sp, color = TextMuted)
                        Text(if (uiState.isLoudnessEnabled) "ON" else "OFF", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Instant Acoustic Simulation Presets
        Text(
            text = "TEST IN DIFFERENT ACOUSTIC SCENARIOS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "Highway Cabin Rumble" to "74 dB • Sub-bass tire drone",
            "Crowded Cafe Chatter" to "68 dB • Vocal frequency clash",
            "Subway Train Commute" to "82 dB • Heavy transit roar",
            "Echoey Tile Room" to "58 dB • Harsh high reflections",
            "Quiet Night Studio" to "36 dB • Reference studio curve"
        ).forEach { (preset, sub) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.setSimulatedAcousticEnvironment(preset) }
                    .testTag("ai_sim_${preset.lowercase().take(6)}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(preset, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(sub, fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Revert to Flat Button
        OutlinedButton(
            onClick = { viewModel.revertAiToFlat() },
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("revert_flat_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Revert to Flat Reference (0 dB)", fontSize = 12.sp, color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ---------------------------------------------------------
// TAB 1: 5-Band Graphic Equalizer & Frequency Curve
// ---------------------------------------------------------
@Composable
private fun EqGraphicTab(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState
) {
    val scrollState = rememberScrollState()
    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val presets = listOf("Flat", "Bass Boost", "Vocal Booster", "Electronic", "Rock", "Acoustic", "Jazz", "Hip-Hop")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Live Frequency Response Curve Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SurfaceBorder, NeonCyan.copy(alpha = 0.3f))))
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f
                    val bands = uiState.equalizerBands

                    // Grid reference lines
                    drawLine(color = SurfaceBorder, start = Offset(0f, centerY), end = Offset(width, centerY), strokeWidth = 1.5f)
                    drawLine(color = SurfaceBorder.copy(alpha = 0.4f), start = Offset(0f, height * 0.2f), end = Offset(width, height * 0.2f), strokeWidth = 1f)
                    drawLine(color = SurfaceBorder.copy(alpha = 0.4f), start = Offset(0f, height * 0.8f), end = Offset(width, height * 0.8f), strokeWidth = 1f)

                    // Draw Smooth Response Curve
                    val path = Path()
                    val points = bands.mapIndexed { idx, gain ->
                        val x = (idx.toFloat() / (bands.size - 1)) * width
                        val norm = -gain / 10f
                        val y = centerY + (norm * (height * 0.38f))
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        path.moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlX = (p0.x + p1.x) / 2f
                            path.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        }

                        // Fill under curve
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(NeonCyan.copy(alpha = 0.35f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Draw Curve Stroke
                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet)),
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                        )

                        // Point Handles
                        points.forEach { pt ->
                            drawCircle(color = Color.White, radius = 5f, center = pt)
                            drawCircle(color = NeonCyan, radius = 2.5f, center = pt)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    bandLabels.forEach { label ->
                        Text(label, color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Preset Chips Row
        Text(
            text = "AUDIO PRESETS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = uiState.activePreset == preset
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.applyEqPreset(preset) },
                    label = { Text(preset, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = SurfaceCard,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("eq_preset_${preset.lowercase().replace(" ", "_")}")
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5-Band Sliders Card
        Text(
            text = "FREQUENCY GAINS (-10 dB to +10 dB)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.equalizerBands.forEachIndexed { index, gain ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${if (gain > 0) "+" else ""}$gain",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gain != 0) NeonCyan else TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Slider(
                            value = gain.toFloat(),
                            onValueChange = { newVal -> viewModel.setEqualizerBand(index, newVal.toInt()) },
                            valueRange = -10f..10f,
                            steps = 19,
                            modifier = Modifier
                                .height(140.dp)
                                .testTag("eq_slider_band_$index"),
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = SurfaceBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bandLabels.getOrElse(index) { "B$index" },
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Reverb Presets
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Reverb Acoustic Space", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("None", "Small Room", "Medium Hall", "Plate Reverb").forEach { reverb ->
                        val isSelected = uiState.reverbPreset == reverb
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setReverbPreset(reverb) },
                            label = { Text(reverb, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricViolet,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("reverb_${reverb.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------
// TAB 2: Car Stereo Speakers, 4-Corner Matrix & Subwoofer
// ---------------------------------------------------------
@Composable
private fun CarStereoTab(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState
) {
    val scrollState = rememberScrollState()

    // Calculate simulated 4-speaker percentage outputs based on Balance and Fader
    val bal = uiState.carBalanceX
    val fad = uiState.carFaderY
    val flPower = (((1f - bal) * 0.5f) * ((1f + fad) * 0.5f) * 400f).roundToInt().coerceIn(0, 100)
    val frPower = (((1f + bal) * 0.5f) * ((1f + fad) * 0.5f) * 400f).roundToInt().coerceIn(0, 100)
    val rlPower = (((1f - bal) * 0.5f) * ((1f - fad) * 0.5f) * 400f).roundToInt().coerceIn(0, 100)
    val rrPower = (((1f + bal) * 0.5f) * ((1f - fad) * 0.5f) * 400f).roundToInt().coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("car_stereo_tab")
    ) {
        // Car Mode Header Card with Launch Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.5f)))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Car Driving Head-Unit", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("High-visibility road controls", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Button(
                    onClick = { viewModel.toggleCarMode() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("launch_driving_mode_btn")
                ) {
                    Text(
                        text = if (uiState.isCarModeActive) "Active" else "Launch",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2D Car Cabin XY Matrix (Interactive Touchpad)
        Text(
            text = "4-SPEAKER BALANCE & FADER (CABIN TOUCHPAD)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive 2D Cabin Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF090D14))
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val xNorm = ((offset.x / size.width) * 2f - 1f).coerceIn(-1f, 1f)
                                val yNorm = (1f - (offset.y / size.height) * 2f).coerceIn(-1f, 1f)
                                viewModel.setCarFaderBalance(xNorm, yNorm)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val xNorm = ((change.position.x / size.width) * 2f - 1f).coerceIn(-1f, 1f)
                                val yNorm = (1f - (change.position.y / size.height) * 2f).coerceIn(-1f, 1f)
                                viewModel.setCarFaderBalance(xNorm, yNorm)
                            }
                        }
                        .testTag("car_cabin_xy_pad"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f

                        // Car Chassis outline
                        drawRoundRect(
                            color = SurfaceBorder,
                            topLeft = Offset(w * 0.15f, h * 0.08f),
                            size = Size(w * 0.7f, h * 0.84f),
                            cornerRadius = CornerRadius(24f, 24f),
                            style = Stroke(width = 2f)
                        )

                        // Center Grid crosshair
                        drawLine(color = SurfaceBorder.copy(alpha = 0.5f), start = Offset(cx, h * 0.08f), end = Offset(cx, h * 0.92f), strokeWidth = 1f)
                        drawLine(color = SurfaceBorder.copy(alpha = 0.5f), start = Offset(w * 0.15f, cy), end = Offset(w * 0.85f, cy), strokeWidth = 1f)

                        // 4 Speaker Positions
                        val flPos = Offset(w * 0.22f, h * 0.18f)
                        val frPos = Offset(w * 0.78f, h * 0.18f)
                        val rlPos = Offset(w * 0.22f, h * 0.82f)
                        val rrPos = Offset(w * 0.78f, h * 0.82f)

                        // Draw Speaker nodes with power intensity
                        drawCircle(color = NeonCyan.copy(alpha = (flPower / 100f).coerceIn(0.2f, 1f)), radius = 10f, center = flPos)
                        drawCircle(color = NeonCyan.copy(alpha = (frPower / 100f).coerceIn(0.2f, 1f)), radius = 10f, center = frPos)
                        drawCircle(color = NeonCyan.copy(alpha = (rlPower / 100f).coerceIn(0.2f, 1f)), radius = 10f, center = rlPos)
                        drawCircle(color = NeonCyan.copy(alpha = (rrPower / 100f).coerceIn(0.2f, 1f)), radius = 10f, center = rrPos)

                        // Current Acoustic Sweet Spot Puck
                        val puckX = cx + (bal * (w * 0.28f))
                        val puckY = cy - (fad * (h * 0.35f))

                        // Radiating sound field from puck
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonCyan.copy(alpha = 0.5f), Color.Transparent),
                                center = Offset(puckX, puckY),
                                radius = 45f
                            ),
                            radius = 45f,
                            center = Offset(puckX, puckY)
                        )
                        drawCircle(color = Color.White, radius = 8f, center = Offset(puckX, puckY))
                        drawCircle(color = NeonCyan, radius = 4f, center = Offset(puckX, puckY))
                    }

                    // Labels for 4 Speakers
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FRONT-L: $flPower%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                            Text("FRONT-R: $frPower%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("REAR-L: $rlPower%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("REAR-R: $rrPower%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Presets for listening positions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Driver Seat", "Front Seats", "All Cabin", "Rear Seats").forEach { pos ->
                        val isSel = uiState.listeningPositionPreset == pos
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setListeningPosition(pos) },
                            label = { Text(pos, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Subwoofer Gain & Crossover Control
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Speaker, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Dedicated Subwoofer Channel", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Low-pass crossover filter & level", fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    Text("+${uiState.subwooferLevelDb} dB", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ElectricViolet)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = uiState.subwooferLevelDb.toFloat(),
                    onValueChange = { viewModel.setSubwooferLevel(it.roundToInt()) },
                    valueRange = 0f..12f,
                    steps = 11,
                    colors = SliderDefaults.colors(thumbColor = ElectricViolet, activeTrackColor = ElectricViolet),
                    modifier = Modifier.testTag("subwoofer_level_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Crossover Frequency Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Crossover LPF:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(50, 80, 120).forEach { hz ->
                            val isSel = uiState.subwooferCrossoverHz == hz
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.setSubwooferCrossover(hz) },
                                label = { Text("$hz Hz", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricViolet,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Road Loudness Compensation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Road Rumble Loudness", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Boosts sub-bass & high-frequencies to overcome tire and highway cabin noise", fontSize = 12.sp, color = TextSecondary)
                }
                Switch(
                    checked = uiState.isLoudnessEnabled,
                    onCheckedChange = { viewModel.toggleLoudness() },
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("loudness_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------
// TAB 3: jetAudio Sound Rack (X-Bass, X-Surround, Crystalizer)
// ---------------------------------------------------------
@Composable
private fun JetAudioRackTab(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("jetaudio_rack_tab")
    ) {
        // jetAudio Dual Analog Needle VU Meters Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SurfaceBorder, NeonCyan.copy(alpha = 0.3f))))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VuMeterDial(label = "CH-L (dB)", level = uiState.vuMeterLeft, accentColor = NeonCyan, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("jetAudio", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = NeonCyan)
                    Text("VU METER", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                VuMeterDial(label = "CH-R (dB)", level = uiState.vuMeterRight, accentColor = ElectricViolet, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // X-Bass (BBE style bass enhancement)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("X-Bass Enhancement", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Subharmonic low-end synthesizer", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text("${uiState.xBass}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = uiState.xBass.toFloat(),
                    onValueChange = { viewModel.setXBass(it.roundToInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                    modifier = Modifier.testTag("xbass_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // X-Surround (Wide 3D Soundstage)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("X-Surround Field", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Acoustic matrix width & depth expansion", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text("${uiState.xSurround}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ElectricViolet)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = uiState.xSurround.toFloat(),
                    onValueChange = { viewModel.setXSurround(it.roundToInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = ElectricViolet, activeTrackColor = ElectricViolet),
                    modifier = Modifier.testTag("xsurround_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Crystalizer (High Harmonics Exciter)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Crystalizer Harmonic Exciter", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Crisp high-frequency sparkle & vocals", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text("${uiState.crystalizer}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AmberGlow)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = uiState.crystalizer.toFloat(),
                    onValueChange = { viewModel.setCrystalizer(it.roundToInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = AmberGlow, activeTrackColor = AmberGlow),
                    modifier = Modifier.testTag("crystalizer_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Playback Pitch & Speed
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pitch & Speed Control", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${String.format("%.2f", uiState.playbackSpeed)}x", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = uiState.playbackSpeed,
                    onValueChange = { viewModel.setPitchSpeed(it, uiState.playbackPitchSemi) },
                    valueRange = 0.75f..1.5f,
                    steps = 14,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.85f, 1.0f, 1.15f, 1.25f).forEach { spd ->
                        val isSel = abs(uiState.playbackSpeed - spd) < 0.02f
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setPitchSpeed(spd, uiState.playbackPitchSemi) },
                            label = { Text("${spd}x", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------
// TAB 4: 911 & Police Dispatch Live Audio Scanner
// ---------------------------------------------------------
@Composable
private fun Scanner911Tab(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("scanner_911_tab")
    ) {
        // Policy Disclaimer Banner (Non-Government Broadcast Feed)
        Surface(
            color = Color(0xFF1E2430),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = AmberGlow, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = PublicSafetyFeeds.DISCLAIMER,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Master Scanner Toggle & Live Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isScannerTransmitting) Color(0xFF381015)
                else if (uiState.isScannerActive) Color(0xFF0F2233)
                else SurfaceCard
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    if (uiState.isScannerTransmitting) listOf(CrimsonWave, AmberGlow)
                    else listOf(NeonCyan.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.5f))
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isScannerTransmitting) CrimsonWave else if (uiState.isScannerActive) NeonCyan else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (uiState.isScannerTransmitting) "LIVE DISPATCH TRANSMISSION"
                                else if (uiState.isScannerActive) "SCANNER ONLINE (MONITORING)"
                                else "PUBLIC SAFETY RADIO OFF",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isScannerTransmitting) CrimsonWave else if (uiState.isScannerActive) NeonCyan else TextMuted
                            )
                            Text(
                                text = if (uiState.isScannerActive) "Channel: ${uiState.selectedScannerChannel.name}" else "Tap switch to monitor public feeds",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isScannerActive,
                        onCheckedChange = { viewModel.toggleScanner(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("scanner_master_switch")
                    )
                }

                if (uiState.isScannerTransmitting && uiState.activeTransmission != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "UNIT: ${uiState.activeTransmission.unitCallSign} • CALL ACTIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberGlow
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${uiState.activeTransmission.message}\"",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Scanner Channel List
        Text(
            text = "AVAILABLE PUBLIC SAFETY BROADCASTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        uiState.scannerChannels.forEach { channel ->
            val isSelected = uiState.selectedScannerChannel.id == channel.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        viewModel.selectScannerChannel(channel)
                        if (!uiState.isScannerActive) viewModel.toggleScanner(true)
                    }
                    .testTag("scanner_channel_${channel.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) SurfaceDark else SurfaceCard),
                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else SurfaceBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = channel.name,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NeonCyan else TextPrimary
                            )
                            Text(
                                text = "${channel.category} • ${channel.department}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(channel.frequencyMhz, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("${channel.listenerCount} listeners", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Dual-Audio Smart Ducking & Volume Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dual-Audio Mixer & Auto-Ducking", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Music volume automatically lowers when police or emergency radio speaks", fontSize = 12.sp, color = TextSecondary)

                Spacer(modifier = Modifier.height(14.dp))

                // Music Ducking Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Music Ducking Reduction", fontSize = 13.sp, color = TextSecondary)
                    Text("-${uiState.duckingPercent}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrimsonWave)
                }
                Slider(
                    value = uiState.duckingPercent.toFloat(),
                    onValueChange = { viewModel.setDuckingPercent(it.roundToInt()) },
                    valueRange = 20f..100f,
                    colors = SliderDefaults.colors(thumbColor = CrimsonWave, activeTrackColor = CrimsonWave),
                    modifier = Modifier.testTag("ducking_percent_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scanner Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Radio Feed Volume", fontSize = 13.sp, color = TextSecondary)
                    Text("${(uiState.scannerVolume * 100).roundToInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                }
                Slider(
                    value = uiState.scannerVolume,
                    onValueChange = { viewModel.setScannerVolume(it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                    modifier = Modifier.testTag("scanner_volume_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Test Trigger Call Button
                OutlinedButton(
                    onClick = { viewModel.triggerDispatchTransmission() },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("trigger_dispatch_test_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CrimsonWave, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simulate Live Dispatch Call", fontSize = 12.sp, color = CrimsonWave, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
