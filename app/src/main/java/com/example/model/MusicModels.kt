package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

data class MusicTrack(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val genre: String = "Electronic",
    val durationMs: Long = 180000L,
    val colorHex: String = "#00E5FF",
    val bassFrequency: Float = 120f,
    val tempoBpm: Int = 120,
    val isFavorite: Boolean = false,
    val playCount: Long = 0L,
    val offlineAvailable: Boolean = true
)

@IgnoreExtraProperties
data class PlaylistModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String? = null,
    val colorHex: String? = "#00E5FF",
    val trackIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserSettingsModel(
    @DocumentId val id: String = "audio_settings",
    val userId: String = "",
    val equalizerBands: List<Int> = listOf(0, 0, 0, 0, 0),
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val reverbPreset: String = "None",
    val activePreset: String = "Flat",
    val themeAccent: String = "Cyber Cyan",
    val visualizerMode: String = "Spectrum Bars",
    val volumeNormalization: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TrackItemModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val artist: String = "",
    val album: String? = null,
    val duration: Long = 0L,
    val isFavorite: Boolean = false,
    val playCount: Long = 0L,
    @ServerTimestamp val lastPlayedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
