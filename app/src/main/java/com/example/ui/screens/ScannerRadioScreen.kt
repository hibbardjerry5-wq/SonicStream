package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DispatchTransmission
import com.example.model.PublicSafetyFeeds
import com.example.model.ScannerChannel
import com.example.model.ScannerStreamState
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicPlayerViewModel
import com.example.ui.viewmodel.PlayerUiState
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerRadioScreen(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf(uiState.scannerSearchQuery) }
    var selectedCategory by remember { mutableStateOf(uiState.scannerCategoryFilter) }
    var showMixerPanel by remember { mutableStateOf(true) }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    val categories = listOf("ALL", "POLICE", "FIRE/EMS", "HIGHWAY", "WEATHER", "AVIATION", "FAVORITES", "CUSTOM")

    val filteredChannels = remember(searchQuery, selectedCategory, uiState.scannerChannels) {
        uiState.scannerChannels.filter { ch ->
            val matchCat = when (selectedCategory) {
                "ALL" -> true
                "POLICE" -> ch.category.equals("Police", ignoreCase = true)
                "FIRE/EMS" -> ch.category.contains("Fire", ignoreCase = true) || ch.category.contains("EMS", ignoreCase = true)
                "HIGHWAY" -> ch.category.contains("Highway", ignoreCase = true)
                "WEATHER" -> ch.category.contains("Weather", ignoreCase = true)
                "AVIATION" -> ch.category.contains("Aviation", ignoreCase = true)
                "FAVORITES" -> ch.isFavorite
                "CUSTOM" -> ch.isCustom
                else -> true
            }
            val matchQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.lowercase()
                ch.name.lowercase().contains(q) ||
                        ch.department.lowercase().contains(q) ||
                        ch.location.lowercase().contains(q) ||
                        ch.frequencyMhz.lowercase().contains(q)
            }
            matchCat && matchQuery
        }
    }

    if (showAddCustomDialog) {
        AddCustomFeedDialog(
            onDismiss = { showAddCustomDialog = false },
            onAddFeed = { name, url, dept, freq, cat, loc ->
                viewModel.addCustomScannerFeed(name, url, dept, freq, cat, loc)
                showAddCustomDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF04060C))
            .statusBarsPadding()
            .testTag("scanner_radio_screen")
    ) {
        // Top Tactical Radio HUD Header
        Surface(
            color = Color(0xF2080D18),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(PoliceBlue.copy(alpha = 0.5f), NeonAmber.copy(alpha = 0.6f), PoliceBlue.copy(alpha = 0.5f))
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
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
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isScannerActive) {
                                        if (uiState.isScannerTransmitting) NeonAmber else Color(0xFF00FF66)
                                    } else TextMuted
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TACTICAL PUBLIC SAFETY SCANNER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonAmber,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Android Auto Badge
                    Surface(
                        color = Color(0x3300E5FF),
                        shape = RoundedCornerShape(6.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet)))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "AUTO COMPATIBLE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = NeonCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active Channel Tuner Card
                val activeCh = uiState.selectedScannerChannel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeCh.frequencyMhz,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = activeCh.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activeCh.department} • ${activeCh.location}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    // Master Scanner Play/Stop Toggle
                    IconButton(
                        onClick = { viewModel.toggleScannerPlayback() },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isScannerActive) NeonAmber else Color(0x332A3B58))
                            .border(1.dp, NeonAmber, CircleShape)
                            .testTag("scanner_stream_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isScannerActive) Icons.Default.Stop else Icons.Default.Radio,
                            contentDescription = "Toggle Scanner",
                            tint = if (uiState.isScannerActive) Color.Black else NeonAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Live Squelch / Spectrum Waveform
                Spacer(modifier = Modifier.height(10.dp))
                ScannerTelemetryWaveform(
                    isActive = uiState.isScannerActive,
                    isTransmitting = uiState.isScannerTransmitting,
                    energyLevel = uiState.scannerAudioLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF030508))
                )

                // Active Transmission Banner
                AnimatedVisibility(
                    visible = uiState.activeTransmission != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.activeTransmission?.let { tx ->
                        Surface(
                            color = Color(0x33FFB300),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonAmber, Color(0xFFFF5252)))),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Podcasts,
                                    contentDescription = null,
                                    tint = NeonAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${tx.unitCallSign} [${tx.code10}]",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = NeonAmber
                                    )
                                    Text(
                                        text = "\"${tx.message}\"",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mixer Panel Accordion
        Surface(
            color = Color(0xCC090E1A),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x2200E5FF), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMixerPanel = !showMixerPanel },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE AUDIO MIXER & SMART DUCKING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                    }
                    Icon(
                        imageVector = if (showMixerPanel) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Mixer",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = showMixerPanel) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        // Volume Faders: Music vs Scanner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Local Music Channel Fader
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Local Music", fontSize = 11.sp, color = NeonCyan)
                                    Text("${(uiState.musicVolume * 100).toInt()}%", fontSize = 11.sp, color = TextPrimary)
                                }
                                Slider(
                                    value = uiState.musicVolume,
                                    onValueChange = { viewModel.setMusicVolume(it) },
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonCyan,
                                        activeTrackColor = NeonCyan,
                                        inactiveTrackColor = Color(0x3300E5FF)
                                    ),
                                    modifier = Modifier.height(28.dp).testTag("music_volume_slider")
                                )
                            }

                            // Public Safety Scanner Channel Fader
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Scanner Radio", fontSize = 11.sp, color = NeonAmber)
                                    Text("${(uiState.scannerVolume * 100).toInt()}%", fontSize = 11.sp, color = TextPrimary)
                                }
                                Slider(
                                    value = uiState.scannerVolume,
                                    onValueChange = { viewModel.setScannerVolume(it) },
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonAmber,
                                        activeTrackColor = NeonAmber,
                                        inactiveTrackColor = Color(0x33FFB300)
                                    ),
                                    modifier = Modifier.height(28.dp).testTag("scanner_volume_slider")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Smart Music Ducking Control Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Auto-Duck Music on Transmission",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    "Ducks music by -${uiState.duckingPercent}% when speech/signals break squelch",
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                            Switch(
                                checked = uiState.isDuckingEnabled,
                                onCheckedChange = { viewModel.toggleDucking(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonCyan,
                                    checkedTrackColor = NeonCyan.copy(alpha = 0.4f),
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0x22FFFFFF)
                                ),
                                modifier = Modifier.testTag("ducking_switch")
                            )
                        }

                        // Spatial Pan & Test Transmission Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.triggerManualDispatchTransmission() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonAmber),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(NeonAmber, Color(0xFFFF5252)))),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("test_dispatch_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Dispatch Call", fontSize = 10.sp)
                            }

                            OutlinedButton(
                                onClick = { showAddCustomDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet))),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("add_custom_feed_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Custom Stream", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Search Bar & Category Chips
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.setScannerSearchQuery(it)
                },
                placeholder = { Text("Search city, agency, or frequency...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.setScannerSearchQuery("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonCyan
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("scanner_search_input")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = cat
                            viewModel.setScannerCategory(cat)
                        },
                        label = { Text(cat, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan,
                            containerColor = Color(0x22131C2E),
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NeonCyan else Color(0x332A3B58)
                        ),
                        modifier = Modifier.testTag("filter_chip_$cat")
                    )
                }
            }
        }

        // Live Channels List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredChannels, key = { it.id }) { channel ->
                val isSelected = uiState.selectedScannerChannel.id == channel.id
                val isStreamingThis = isSelected && uiState.isScannerActive

                ScannerChannelCard(
                    channel = channel,
                    isSelected = isSelected,
                    isStreaming = isStreamingThis,
                    onSelect = { viewModel.playScannerChannel(channel) },
                    onToggleFavorite = { viewModel.toggleScannerFavorite(channel.id) }
                )
            }

            if (filteredChannels.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No public safety channels match your search filter.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScannerChannelCard(
    channel: ScannerChannel,
    isSelected: Boolean,
    isStreaming: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFF0F1A2E) else Color(0xFF080C16),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isSelected) listOf(NeonCyan, NeonAmber) else listOf(Color(0x221E2C44), Color(0x221E2C44))
            )
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("channel_card_${channel.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Signal & Category Indicator
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (channel.category) {
                            "Police" -> Color(0x331E88E5)
                            "Fire/EMS" -> Color(0x33E53935)
                            "Highway" -> Color(0x33FB8C00)
                            "Weather" -> Color(0x3343A047)
                            else -> Color(0x338E24AA)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (channel.category) {
                        "Police" -> Icons.Default.Security
                        "Fire/EMS" -> Icons.Default.LocalFireDepartment
                        "Highway" -> Icons.Default.Navigation
                        "Weather" -> Icons.Default.Cloud
                        "Aviation" -> Icons.Default.Flight
                        else -> Icons.Default.Radio
                    },
                    contentDescription = null,
                    tint = when (channel.category) {
                        "Police" -> Color(0xFF64B5F6)
                        "Fire/EMS" -> Color(0xFFEF5350)
                        "Highway" -> Color(0xFFFFB74D)
                        "Weather" -> Color(0xFF81C784)
                        else -> NeonCyan
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${channel.department} • ${channel.location}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = channel.frequencyMhz,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• ${channel.listenerCount} listeners",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    if (channel.isLive) {
                        Text(
                            text = "LIVE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00E676),
                            modifier = Modifier
                                .background(Color(0x2200E676), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (channel.isFavorite) NeonAmber else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Stream Action Button
            IconButton(
                onClick = onSelect,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isStreaming) NeonAmber else Color(0x2200E5FF))
            ) {
                Icon(
                    imageVector = if (isStreaming) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                    contentDescription = "Stream",
                    tint = if (isStreaming) Color.Black else NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ScannerTelemetryWaveform(
    isActive: Boolean,
    isTransmitting: Boolean,
    energyLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Center line
        drawLine(
            color = Color(0x2200E5FF),
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = 1f
        )

        if (isActive) {
            val path = Path()
            val points = 60
            val step = width / points
            val amp = if (isTransmitting) (height * 0.40f * energyLevel) else (height * 0.15f * energyLevel.coerceAtLeast(0.1f))

            path.moveTo(0f, midY)
            for (i in 0..points) {
                val x = i * step
                val sinVal = sin(phase + (i * 0.35f))
                val noise = if (isTransmitting) sin((i * 1.5f) + phase * 2f) * 0.25f else 0f
                val y = midY + ((sinVal + noise).toFloat() * amp)
                path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = if (isTransmitting) NeonAmber else NeonCyan,
                style = Stroke(width = 2.5f)
            )
        }
    }
}

@Composable
fun AddCustomFeedDialog(
    onDismiss: () -> Unit,
    onAddFeed: (name: String, url: String, dept: String, freq: String, category: String, loc: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var streamUrl by remember { mutableStateOf("") }
    var dept by remember { mutableStateOf("") }
    var freq by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Police") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Podcasts, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom Public Safety Stream", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Enter an external Icecast, Broadcastify relay, or direct MP3/AAC public safety audio stream URL.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Feed Name (e.g. City Police Dispatch)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("Stream URL (http:// or https://)", fontSize = 11.sp) },
                    placeholder = { Text("https://broadcastify.cdnstream1.com/...", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = freq,
                        onValueChange = { freq = it },
                        label = { Text("Frequency", fontSize = 11.sp) },
                        placeholder = { Text("460.125 MHz", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = loc,
                        onValueChange = { loc = it },
                        label = { Text("City, State", fontSize = 11.sp) },
                        placeholder = { Text("Austin, TX", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = dept,
                    onValueChange = { dept = it },
                    label = { Text("Agency / Department", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() || streamUrl.isNotBlank()) {
                        onAddFeed(name, streamUrl, dept, freq, category, loc)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
            ) {
                Text("Add & Connect", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = Color(0xFF0C1322)
    )
}
