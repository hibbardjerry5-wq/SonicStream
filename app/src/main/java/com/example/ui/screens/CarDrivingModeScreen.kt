package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PublicSafetyFeeds
import com.example.model.formatMs
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicPlayerViewModel
import com.example.ui.viewmodel.PlayerUiState

@Composable
fun CarDrivingModeScreen(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    val track = uiState.currentTrack

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A0F))
            .statusBarsPadding()
            .padding(18.dp)
            .testTag("car_driving_mode_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header: Car Stereo Mode & Exit Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("CAR STEREO HEAD UNIT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                    Text("4-Speaker Acoustic Matrix Active", fontSize = 12.sp, color = NeonCyan)
                }
            }

            Button(
                onClick = { viewModel.toggleCarMode() },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("exit_car_mode_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Exit Car Mode", fontSize = 12.sp, color = Color.White)
            }
        }

        // Emergency & Police Scanner Live Status Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleScanner(!uiState.isScannerActive) }
                .testTag("car_scanner_status_bar"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isScannerTransmitting) Color(0xFF3B1219)
                else if (uiState.isScannerActive) Color(0xFF0F1E2E)
                else SurfaceCard
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    if (uiState.isScannerTransmitting) listOf(CrimsonWave, AmberGlow)
                    else listOf(NeonCyan.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.5f))
                )
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isScannerTransmitting) "POLICE DISPATCH LIVE CALL"
                            else if (uiState.isScannerActive) "MONITORING: ${uiState.selectedScannerChannel.name}"
                            else "POLICE / 911 SCANNER OFF (TAP TO ACTIVATE)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isScannerTransmitting) CrimsonWave else if (uiState.isScannerActive) NeonCyan else TextMuted
                        )
                    }

                    if (uiState.isScannerActive) {
                        Text(
                            text = if (uiState.isScannerTransmitting) "MUSIC DUCKED -${uiState.duckingPercent}%" else uiState.selectedScannerChannel.frequencyMhz,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isScannerTransmitting) AmberGlow else TextSecondary
                        )
                    }
                }

                if (uiState.isScannerTransmitting && uiState.activeTransmission != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${uiState.activeTransmission.unitCallSign}: \"${uiState.activeTransmission.message}\"",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

        // Center Track Display (Oversized for Road Visibility)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = track.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${track.artist} • ${track.album}",
                fontSize = 18.sp,
                color = NeonCyan,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Oversized Progress Bar
            val progress = if (uiState.durationMs > 0) (uiState.currentPositionMs.toFloat() / uiState.durationMs).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = NeonCyan,
                trackColor = SurfaceBorder
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatMs(uiState.currentPositionMs), color = TextMuted, fontSize = 14.sp)
                Text(formatMs(uiState.durationMs), color = TextMuted, fontSize = 14.sp)
            }
        }

        // Oversized Tactile Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.playPrevious() },
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard)
                    .testTag("car_prev_button")
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(46.dp))
            }

            // Giant Play/Pause Button
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(NeonCyan, ElectricViolet)))
                    .clickable { viewModel.togglePlayPause() }
                    .testTag("car_play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(56.dp)
                )
            }

            IconButton(
                onClick = { viewModel.playNext() },
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard)
                    .testTag("car_next_button")
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(46.dp))
            }
        }

        // Car Stereo Quick Tuning & Speaker Sweet Spot Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SPEAKER ACOUSTIC FOCUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = if (uiState.isLoudnessEnabled) NeonCyan else TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isLoudnessEnabled) "LOUDNESS ON" else "LOUDNESS OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isLoudnessEnabled) NeonCyan else TextMuted,
                            modifier = Modifier.clickable { viewModel.toggleLoudness() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                Spacer(modifier = Modifier.height(10.dp))

                // AI Road Acoustics Auto-Tuner Quick Button
                Button(
                    onClick = { viewModel.runAiAcousticCalibration() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (uiState.isAiAcousticTuningActive) ElectricViolet else Color(0xFF131C2E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("car_ai_tune_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isCalibratingNow) "AI CALIBRATING ROAD ACOUSTICS..."
                        else if (uiState.isAiAcousticTuningActive) "AI TUNED: ${uiState.ambientSplDb.toInt()} dB (${uiState.ambientEnvironmentType})"
                        else "AI ROAD NOISE ACOUSTIC AUTO-TUNE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subwoofer Gain Quick Adjustment & Manual Dispatch Simulation Trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speaker, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subwoofer: +${uiState.subwooferLevelDb} dB", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.setSubwooferLevel((uiState.subwooferLevelDb - 2).coerceAtLeast(0)) },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceBorder)
                        ) {
                            Text("-", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.setSubwooferLevel((uiState.subwooferLevelDb + 2).coerceAtMost(12)) },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceBorder)
                        ) {
                            Text("+", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.triggerDispatchTransmission() },
                            modifier = Modifier.height(36.dp).testTag("car_test_dispatch_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Test Dispatch", fontSize = 11.sp, color = CrimsonWave)
                        }
                    }
                }
            }
        }
    }
}
