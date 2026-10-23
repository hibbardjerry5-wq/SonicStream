package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MusicTrack
import com.example.model.PlaylistModel
import com.example.model.formatMs
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicPlayerViewModel
import com.example.ui.viewmodel.PlayerUiState

enum class LibraryTab { ALL_TRACKS, PLAYLISTS, FAVORITES }

@Composable
fun LibraryPlaylistsScreen(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.ALL_TRACKS) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPlaylistForDetail by remember { mutableStateOf<PlaylistModel?>(null) }
    var trackToAddTarget by remember { mutableStateOf<MusicTrack?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .testTag("library_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Music Library",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${uiState.tracks.size} offline tracks • ${uiState.playlists.size} playlists",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            if (selectedTab == LibraryTab.PLAYLISTS) {
                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(NeonCyan)
                        .testTag("create_playlist_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = Color.Black)
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search songs, artists, genres...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("library_search_field"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = SurfaceBorder,
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Selector Row
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = BackgroundDark,
            contentColor = NeonCyan,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == LibraryTab.ALL_TRACKS,
                onClick = { selectedTab = LibraryTab.ALL_TRACKS },
                text = { Text("All Songs", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("tab_all_songs")
            )
            Tab(
                selected = selectedTab == LibraryTab.PLAYLISTS,
                onClick = { selectedTab = LibraryTab.PLAYLISTS },
                text = { Text("Playlists", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("tab_playlists")
            )
            Tab(
                selected = selectedTab == LibraryTab.FAVORITES,
                onClick = { selectedTab = LibraryTab.FAVORITES },
                text = { Text("Favorites", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("tab_favorites")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Lists
        when (selectedTab) {
            LibraryTab.ALL_TRACKS -> {
                val filtered = uiState.tracks.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.artist.contains(searchQuery, ignoreCase = true) ||
                            it.genre.contains(searchQuery, ignoreCase = true)
                }
                TrackListContent(
                    tracks = filtered,
                    currentTrack = uiState.currentTrack,
                    isPlaying = uiState.isPlaying,
                    onTrackClick = { viewModel.playTrack(it) },
                    onFavoriteClick = { viewModel.toggleFavorite(it.id) },
                    onAddToPlaylistClick = { trackToAddTarget = it }
                )
            }
            LibraryTab.PLAYLISTS -> {
                if (selectedPlaylistForDetail != null) {
                    // Show Playlist Detail View
                    PlaylistDetailView(
                        playlist = selectedPlaylistForDetail!!,
                        allTracks = uiState.tracks,
                        currentTrack = uiState.currentTrack,
                        onBack = { selectedPlaylistForDetail = null },
                        onTrackClick = { viewModel.playTrack(it) },
                        onRemoveTrack = { trId ->
                            viewModel.removeTrackFromPlaylist(selectedPlaylistForDetail!!.id, trId)
                            selectedPlaylistForDetail = selectedPlaylistForDetail!!.copy(
                                trackIds = selectedPlaylistForDetail!!.trackIds - trId
                            )
                        },
                        onDeletePlaylist = {
                            viewModel.deletePlaylist(selectedPlaylistForDetail!!.id)
                            selectedPlaylistForDetail = null
                        }
                    )
                } else {
                    // Playlists Grid/List
                    PlaylistsListContent(
                        playlists = uiState.playlists,
                        tracks = uiState.tracks,
                        onPlaylistClick = { selectedPlaylistForDetail = it }
                    )
                }
            }
            LibraryTab.FAVORITES -> {
                val favs = uiState.tracks.filter { it.isFavorite }
                if (favs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No favorite songs yet", color = TextSecondary, fontSize = 16.sp)
                            Text("Tap the heart icon on any song to add it here", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                } else {
                    TrackListContent(
                        tracks = favs,
                        currentTrack = uiState.currentTrack,
                        isPlaying = uiState.isPlaying,
                        onTrackClick = { viewModel.playTrack(it) },
                        onFavoriteClick = { viewModel.toggleFavorite(it.id) },
                        onAddToPlaylistClick = { trackToAddTarget = it }
                    )
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, color ->
                viewModel.createPlaylist(name, desc, color)
                showCreateDialog = false
            }
        )
    }

    // Add To Playlist Dialog
    if (trackToAddTarget != null) {
        AddToPlaylistDialog(
            track = trackToAddTarget!!,
            playlists = uiState.playlists,
            onDismiss = { trackToAddTarget = null },
            onSelectPlaylist = { plId ->
                viewModel.addTrackToPlaylist(plId, trackToAddTarget!!.id)
                trackToAddTarget = null
            }
        )
    }
}

@Composable
fun TrackListContent(
    tracks: List<MusicTrack>,
    currentTrack: MusicTrack,
    isPlaying: Boolean,
    onTrackClick: (MusicTrack) -> Unit,
    onFavoriteClick: (MusicTrack) -> Unit,
    onAddToPlaylistClick: (MusicTrack) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        items(tracks, key = { it.id }) { track ->
            val isCurrent = track.id == currentTrack.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrackClick(track) }
                    .testTag("track_item_${track.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (isCurrent) SurfaceBorder else SurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(android.graphics.Color.parseColor(track.colorHex))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCurrent && isPlaying) Icons.Default.Equalizer else Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCurrent) NeonCyan else TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "${track.artist} • ${formatMs(track.durationMs)}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    IconButton(onClick = { onFavoriteClick(track) }) {
                        Icon(
                            imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (track.isFavorite) CrimsonWave else TextMuted
                        )
                    }

                    IconButton(onClick = { onAddToPlaylistClick(track) }) {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = "Add to playlist", tint = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistsListContent(
    playlists: List<PlaylistModel>,
    tracks: List<MusicTrack>,
    onPlaylistClick: (PlaylistModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        items(playlists, key = { it.id }) { playlist ->
            val color = try {
                Color(android.graphics.Color.parseColor(playlist.colorHex ?: "#00E5FF"))
            } catch (e: Exception) {
                NeonCyan
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaylistClick(playlist) }
                    .testTag("playlist_item_${playlist.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QueueMusic, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${playlist.trackIds.size} songs • ${playlist.description ?: "Custom offline playlist"}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                }
            }
        }
    }
}

@Composable
fun PlaylistDetailView(
    playlist: PlaylistModel,
    allTracks: List<MusicTrack>,
    currentTrack: MusicTrack,
    onBack: () -> Unit,
    onTrackClick: (MusicTrack) -> Unit,
    onRemoveTrack: (String) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    val playlistTracks = allTracks.filter { it.id in playlist.trackIds }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = playlist.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = onDeletePlaylist) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Playlist", tint = CrimsonWave)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (playlistTracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("This playlist has no songs yet", color = TextSecondary)
                    Text("Add songs from the 'All Songs' tab", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(playlistTracks, key = { it.id }) { track ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onTrackClick(track) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("${track.artist} • ${formatMs(track.durationMs)}", fontSize = 12.sp, color = TextSecondary)
                            }
                            IconButton(onClick = { onRemoveTrack(track.id) }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#00E5FF") }
    val colors = listOf("#00E5FF", "#A855F7", "#F59E0B", "#10B981", "#F43F5E")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Offline Playlist", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("playlist_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("playlist_desc_input")
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("Theme Color", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colors.forEach { hex ->
                        val isSel = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { selectedColor = hex }
                                .then(if (isSel) Modifier.clip(CircleShape) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, desc, selectedColor) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.testTag("dialog_create_playlist_button")
            ) {
                Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = SurfaceCard
    )
}

@Composable
fun AddToPlaylistDialog(
    track: MusicTrack,
    playlists: List<PlaylistModel>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column {
                Text("Select a playlist for \"${track.title}\":", fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                playlists.forEach { pl ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectPlaylist(pl.id) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceBorder)
                    ) {
                        Text(
                            text = pl.name,
                            modifier = Modifier.padding(12.dp),
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = SurfaceCard
    )
}
