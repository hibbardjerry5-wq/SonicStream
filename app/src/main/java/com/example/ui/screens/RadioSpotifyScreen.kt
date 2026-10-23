package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.model.SpotifyTrackItem
import com.example.model.AppleMusicTrackItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicPlayerViewModel
import com.example.ui.viewmodel.PlayerUiState

enum class StreamHubSubTab(val title: String) {
    INTERNET_RADIO("MyRadio FM"),
    SPOTIFY("Spotify"),
    APPLE_MUSIC("Apple Music")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioSpotifyScreen(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSubTab by remember { mutableStateOf(StreamHubSubTab.INTERNET_RADIO) }
    var radioSearchQuery by remember { mutableStateOf("") }
    var spotifySearchQuery by remember { mutableStateOf("") }
    var appleMusicSearchQuery by remember { mutableStateOf("") }
    var selectedRadioGenre by remember { mutableStateOf("ALL") }

    val radioGenres = listOf("ALL", "CHILLOUT", "SYNTHWAVE", "POP", "ROCK", "ELECTRONIC", "JAZZ", "FAVORITES")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF04060C))
            .statusBarsPadding()
            .testTag("radio_spotify_screen")
    ) {
        // Top Header
        Surface(
            color = Color(0xF20B101C),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    when (currentSubTab) {
                        StreamHubSubTab.INTERNET_RADIO -> listOf(NeonCyan.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.5f))
                        StreamHubSubTab.SPOTIFY -> listOf(Color(0xFF1DB954).copy(alpha = 0.5f), NeonCyan.copy(alpha = 0.5f))
                        StreamHubSubTab.APPLE_MUSIC -> listOf(Color(0xFFFC3C44).copy(alpha = 0.5f), Color(0xFFFF2D55).copy(alpha = 0.5f))
                    }
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
                        Icon(
                            imageVector = when (currentSubTab) {
                                StreamHubSubTab.INTERNET_RADIO -> Icons.Default.Radio
                                StreamHubSubTab.SPOTIFY -> Icons.Default.GraphicEq
                                StreamHubSubTab.APPLE_MUSIC -> Icons.Default.MusicNote
                            },
                            contentDescription = null,
                            tint = when (currentSubTab) {
                                StreamHubSubTab.INTERNET_RADIO -> NeonCyan
                                StreamHubSubTab.SPOTIFY -> Color(0xFF1DB954)
                                StreamHubSubTab.APPLE_MUSIC -> Color(0xFFFC3C44)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentSubTab) {
                                StreamHubSubTab.INTERNET_RADIO -> "MYRADIO GLOBAL BROADCASTS"
                                StreamHubSubTab.SPOTIFY -> "SPOTIFY STREAM NETWORK"
                                StreamHubSubTab.APPLE_MUSIC -> "APPLE MUSIC SPATIAL & LOSSLESS"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = when (currentSubTab) {
                                StreamHubSubTab.INTERNET_RADIO -> NeonCyan
                                StreamHubSubTab.SPOTIFY -> Color(0xFF1DB954)
                                StreamHubSubTab.APPLE_MUSIC -> Color(0xFFFC3C44)
                            },
                            letterSpacing = 1.1.sp
                        )
                    }

                    // Android Auto Badge
                    Surface(
                        color = Color(0x2200E5FF),
                        shape = RoundedCornerShape(6.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF1DB954))))
                    ) {
                        Text(
                            "AUTO READY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Segmented Toggle Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF070B14))
                        .padding(3.dp)
                ) {
                    StreamHubSubTab.values().forEach { tab ->
                        val isSelected = currentSubTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        when (tab) {
                                            StreamHubSubTab.INTERNET_RADIO -> NeonCyan.copy(alpha = 0.25f)
                                            StreamHubSubTab.SPOTIFY -> Color(0xFF1DB954).copy(alpha = 0.25f)
                                            StreamHubSubTab.APPLE_MUSIC -> Color(0xFFFC3C44).copy(alpha = 0.25f)
                                        }
                                    } else Color.Transparent
                                )
                                .clickable { currentSubTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    when (tab) {
                                        StreamHubSubTab.INTERNET_RADIO -> NeonCyan
                                        StreamHubSubTab.SPOTIFY -> Color(0xFF1DB954)
                                        StreamHubSubTab.APPLE_MUSIC -> Color(0xFFFC3C44)
                                    }
                                } else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // SubTab Content: MyRadio vs Spotify vs Apple Music
        when (currentSubTab) {
            StreamHubSubTab.INTERNET_RADIO -> {
                InternetRadioSection(
                    viewModel = viewModel,
                    uiState = uiState,
                    searchQuery = radioSearchQuery,
                    onSearchQueryChange = {
                        radioSearchQuery = it
                        viewModel.searchRadioStations(it)
                    },
                    selectedGenre = selectedRadioGenre,
                    onGenreChange = {
                        selectedRadioGenre = it
                        viewModel.filterRadioGenre(it)
                    },
                    genres = radioGenres
                )
            }
            StreamHubSubTab.SPOTIFY -> {
                SpotifySection(
                    viewModel = viewModel,
                    uiState = uiState,
                    searchQuery = spotifySearchQuery,
                    onSearchQueryChange = {
                        spotifySearchQuery = it
                        viewModel.searchSpotifyTracks(it)
                    },
                    onOpenSpotifyUri = { uri ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to web player
                            val webUrl = "https://open.spotify.com/search/" + Uri.encode(spotifySearchQuery)
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                            context.startActivity(webIntent)
                        }
                    }
                )
            }
            StreamHubSubTab.APPLE_MUSIC -> {
                AppleMusicSection(
                    viewModel = viewModel,
                    uiState = uiState,
                    searchQuery = appleMusicSearchQuery,
                    onSearchQueryChange = {
                        appleMusicSearchQuery = it
                        viewModel.searchAppleMusicTracks(it)
                    },
                    onOpenAppleMusicUrl = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.apple.com"))
                            context.startActivity(fallbackIntent)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InternetRadioSection(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedGenre: String,
    onGenreChange: (String) -> Unit,
    genres: List<String>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Genre Bar
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search MyRadio stations (e.g. SomaFM, BBC, KEXP)...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
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
                    .testTag("radio_search_input")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Genre Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(genres) { g ->
                    val isSelected = selectedGenre == g
                    FilterChip(
                        selected = isSelected,
                        onClick = { onGenreChange(g) },
                        label = { Text(g, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
                        modifier = Modifier.testTag("radio_genre_$g")
                    )
                }
            }
        }

        // Stations List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(uiState.radioStations, key = { it.stationUuid }) { station ->
                val isPlayingStation = uiState.activeRadioStation?.stationUuid == station.stationUuid && uiState.isRadioPlaying

                RadioStationCard(
                    station = station,
                    isPlaying = isPlayingStation,
                    onPlay = { viewModel.playRadioStation(station) },
                    onToggleFavorite = { viewModel.toggleRadioFavorite(station.stationUuid) }
                )
            }
        }
    }
}

@Composable
fun RadioStationCard(
    station: RadioStation,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        color = if (isPlaying) Color(0xFF0E1A2E) else Color(0xFF080C16),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isPlaying) listOf(NeonCyan, ElectricViolet) else listOf(Color(0x221E2C44), Color(0x221E2C44))
            )
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("radio_station_${station.stationUuid}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x3300E5FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Radio,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${station.genre} • ${station.country}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${station.bitrateKbps} kbps ${station.codec}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                    Text(
                        text = "★ ${station.votes} votes",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (station.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (station.isFavorite) NeonAmber else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) NeonCyan else Color(0x2200E5FF))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play Station",
                    tint = if (isPlaying) Color.Black else NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SpotifySection(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenSpotifyUri: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Spotify Search Bar
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search Spotify tracks, artists, or albums...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF1DB954), modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1DB954),
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Color(0xFF1DB954)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("spotify_search_input")
            )

            // Info note
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spotify High-Fidelity Audio Preview & Deep Stream Integration",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Text(
                    text = "320 KBPS AAC",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1DB954)
                )
            }
        }

        // Spotify Tracks List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(uiState.spotifyTracks, key = { it.id }) { track ->
                val isPlayingThis = uiState.activeSpotifyTrack?.id == track.id && uiState.isSpotifyPlaying

                SpotifyTrackCard(
                    track = track,
                    isPlaying = isPlayingThis,
                    onPlay = { viewModel.playSpotifyTrack(track) },
                    onOpenSpotify = { onOpenSpotifyUri(track.spotifyUri) }
                )
            }
        }
    }
}

@Composable
fun SpotifyTrackCard(
    track: SpotifyTrackItem,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onOpenSpotify: () -> Unit
) {
    Surface(
        color = if (isPlaying) Color(0xFF0F2218) else Color(0xFF080C16),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isPlaying) listOf(Color(0xFF1DB954), NeonCyan) else listOf(Color(0x221E2C44), Color(0x221E2C44))
            )
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("spotify_track_${track.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x331DB954)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.album}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Popularity ${track.popularity}% • High Quality Stream",
                    fontSize = 10.sp,
                    color = Color(0xFF1DB954)
                )
            }

            // Open in Spotify App button
            IconButton(
                onClick = onOpenSpotify,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = "Open in Spotify",
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Play stream preview button
            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) Color(0xFF1DB954) else Color(0x221DB954))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = if (isPlaying) Color.Black else Color(0xFF1DB954),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AppleMusicSection(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenAppleMusicUrl: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Apple Music Search Bar
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search Apple Music songs, artists, or albums...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFFC3C44), modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFC3C44),
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Color(0xFFFC3C44)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("apple_music_search_input")
            )

            // Info note
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Apple Music Lossless & Dolby Atmos Spatial Audio Previews",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Text(
                    text = "256 KBPS AAC",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFC3C44)
                )
            }
        }

        // Apple Music Tracks List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(uiState.appleMusicTracks, key = { it.id }) { track ->
                val isPlayingThis = uiState.activeAppleMusicTrack?.id == track.id && uiState.isAppleMusicPlaying

                AppleMusicTrackCard(
                    track = track,
                    isPlaying = isPlayingThis,
                    onPlay = { viewModel.playAppleMusicTrack(track) },
                    onOpenAppleMusic = { onOpenAppleMusicUrl(track.appleMusicUrl) }
                )
            }
        }
    }
}

@Composable
fun AppleMusicTrackCard(
    track: AppleMusicTrackItem,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onOpenAppleMusic: () -> Unit
) {
    Surface(
        color = if (isPlaying) Color(0xFF261014) else Color(0xFF080C16),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isPlaying) listOf(Color(0xFFFC3C44), Color(0xFFFF2D55)) else listOf(Color(0x221E2C44), Color(0x221E2C44))
            )
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("apple_music_track_${track.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33FC3C44)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFFFC3C44),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.album}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (track.isSpatialAudio) {
                        Text(
                            text = "SPATIAL AUDIO",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFC3C44),
                            modifier = Modifier
                                .background(Color(0x22FC3C44), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    if (track.isLossless) {
                        Text(
                            text = "LOSSLESS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan,
                            modifier = Modifier
                                .background(Color(0x2200E5FF), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Open in Apple Music App
            IconButton(
                onClick = onOpenAppleMusic,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = "Open in Apple Music",
                    tint = Color(0xFFFC3C44),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Play stream preview button
            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) Color(0xFFFC3C44) else Color(0x22FC3C44))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = if (isPlaying) Color.Black else Color(0xFFFC3C44),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

