package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeuralDirectiveDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MusicPlayerViewModel
import com.example.ui.viewmodel.PlayerUiState

@Composable
fun MainAppScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNeuralFloatingDialog by remember { mutableStateOf(false) }

    if (showNeuralFloatingDialog) {
        NeuralDirectiveDialog(
            viewModel = viewModel,
            uiState = uiState,
            onDismiss = { showNeuralFloatingDialog = false }
        )
    }

    if (!uiState.isBootCompleted) {
        AnimatedBootScreen(
            onBootComplete = { viewModel.completeBoot() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    // Handle back button
    BackHandler(enabled = uiState.isFullscreenVisualizer || uiState.isCarModeActive || uiState.currentTab != AppTab.NOW_PLAYING) {
        if (uiState.isFullscreenVisualizer) {
            viewModel.toggleFullscreenVisualizer()
        } else if (uiState.isCarModeActive) {
            viewModel.toggleCarMode()
        } else if (uiState.currentTab != AppTab.NOW_PLAYING) {
            viewModel.selectTab(AppTab.NOW_PLAYING)
        }
    }

    if (uiState.isFullscreenVisualizer) {
        VisualizerScreen(
            viewModel = viewModel,
            uiState = uiState,
            modifier = Modifier.fillMaxSize()
        )
    } else if (uiState.isCarModeActive) {
        CarDrivingModeScreen(
            viewModel = viewModel,
            uiState = uiState,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color(0xFF05070D),
            bottomBar = {
                Column {
                    // Mini Player Bar (shown on sub-tabs when not in Now Playing)
                    if (uiState.currentTab != AppTab.NOW_PLAYING) {
                        MiniPlayerBar(
                            uiState = uiState,
                            onBarClick = { viewModel.selectTab(AppTab.NOW_PLAYING) },
                            onPlayPause = { viewModel.togglePlayPause() },
                            onNext = { viewModel.playNext() }
                        )
                    }

                    // Cybernetic Fluid Glass Navigation Bar
                    Surface(
                        color = Color(0xF20B101C),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(SurfaceBorder, NeonCyan.copy(alpha = 0.35f), SurfaceBorder)
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("main_navigation_bar")
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            contentColor = NeonCyan,
                            tonalElevation = 0.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            NavigationBarItem(
                                selected = uiState.currentTab == AppTab.NOW_PLAYING,
                                onClick = { viewModel.selectTab(AppTab.NOW_PLAYING) },
                                icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Now Playing") },
                                label = { Text("Cyber", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan.copy(alpha = 0.18f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_player")
                            )

                            NavigationBarItem(
                                selected = uiState.currentTab == AppTab.LIBRARY,
                                onClick = { viewModel.selectTab(AppTab.LIBRARY) },
                                icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                                label = { Text("Vault", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan.copy(alpha = 0.18f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_library")
                            )

                            NavigationBarItem(
                                selected = uiState.currentTab == AppTab.STREAMS,
                                onClick = { viewModel.selectTab(AppTab.STREAMS) },
                                icon = { Icon(Icons.Default.Radio, contentDescription = "Radio & Streams") },
                                label = { Text("Streams", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan.copy(alpha = 0.18f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_streams")
                            )

                            NavigationBarItem(
                                selected = uiState.currentTab == AppTab.SCANNER,
                                onClick = { viewModel.selectTab(AppTab.SCANNER) },
                                icon = { Icon(Icons.Default.Security, contentDescription = "911 & Police Scanner") },
                                label = { Text("Scanner", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonAmber,
                                    selectedTextColor = NeonAmber,
                                    indicatorColor = NeonAmber.copy(alpha = 0.18f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_scanner")
                            )

                            NavigationBarItem(
                                selected = uiState.currentTab == AppTab.EQUALIZER,
                                onClick = { viewModel.selectTab(AppTab.EQUALIZER) },
                                icon = { Icon(Icons.Default.Tune, contentDescription = "Sound Lab") },
                                label = { Text("Neural", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan.copy(alpha = 0.18f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_equalizer")
                            )

                            NavigationBarItem(
                                selected = uiState.currentTab == AppTab.VISUALIZER,
                                onClick = { viewModel.selectTab(AppTab.VISUALIZER) },
                                icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Visualizer") },
                                label = { Text("Holo", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan.copy(alpha = 0.18f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_visualizer")
                            )

                            NavigationBarItem(
                                selected = uiState.currentTab == AppTab.CLOUD_SYNC,
                                onClick = { viewModel.selectTab(AppTab.CLOUD_SYNC) },
                                icon = { Icon(Icons.Default.CloudSync, contentDescription = "Cloud Sync") },
                                label = { Text("Sync", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan.copy(alpha = 0.18f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_cloud_sync")
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Fluid Screen Switching Transition
                AnimatedContent(
                    targetState = uiState.currentTab,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) + slideInHorizontally { width -> (width * 0.08f).toInt() })
                            .togetherWith(fadeOut(animationSpec = tween(180)))
                    },
                    label = "TabContent"
                ) { targetTab ->
                    when (targetTab) {
                        AppTab.NOW_PLAYING -> NowPlayingScreen(viewModel = viewModel, uiState = uiState)
                        AppTab.LIBRARY -> LibraryPlaylistsScreen(viewModel = viewModel, uiState = uiState)
                        AppTab.STREAMS -> RadioSpotifyScreen(viewModel = viewModel, uiState = uiState)
                        AppTab.SCANNER -> ScannerRadioScreen(viewModel = viewModel, uiState = uiState)
                        AppTab.EQUALIZER -> EqualizerScreen(viewModel = viewModel, uiState = uiState)
                        AppTab.VISUALIZER -> VisualizerScreen(viewModel = viewModel, uiState = uiState)
                        AppTab.CLOUD_SYNC -> CloudSyncScreen(viewModel = viewModel, uiState = uiState)
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    uiState: PlayerUiState,
    onBarClick: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    val track = uiState.currentTrack
    val progress = if (uiState.durationMs > 0) {
        (uiState.currentPositionMs.toFloat() / uiState.durationMs).coerceIn(0f, 1f)
    } else 0f

    Surface(
        color = Color(0xF2111827),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(NeonCyan.copy(alpha = 0.4f), ElectricViolet.copy(alpha = 0.4f))
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBarClick() }
            .testTag("mini_player_bar")
    ) {
        Column {
            // Linear Progress Line
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = NeonCyan,
                trackColor = SurfaceBorder
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(android.graphics.Color.parseColor(track.colorHex))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${track.artist} • ${if (uiState.isAiAcousticTuningActive) "NEURAL DSP" else "STANDARD"}",
                        fontSize = 10.sp,
                        color = if (uiState.isAiAcousticTuningActive) NeonCyan else TextSecondary,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.testTag("mini_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = NeonCyan
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.testTag("mini_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
