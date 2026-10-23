package com.example.data.local

import com.example.model.MusicTrack
import com.example.model.PlaylistModel

object OfflineMusicCatalog {
    val sampleTracks = listOf(
        MusicTrack(
            id = "track_neon_skyline",
            title = "Neon Skyline",
            artist = "Aura Wave",
            album = "Cyber Odyssey",
            genre = "Synthwave",
            durationMs = 210000L,
            colorHex = "#00E5FF",
            bassFrequency = 65f,
            tempoBpm = 124,
            isFavorite = true,
            playCount = 14
        ),
        MusicTrack(
            id = "track_midnight_drive",
            title = "Midnight Drive",
            artist = "Echo Pulse",
            album = "Retro Horizon",
            genre = "Electro Groove",
            durationMs = 195000L,
            colorHex = "#A855F7",
            bassFrequency = 80f,
            tempoBpm = 118,
            isFavorite = false,
            playCount = 9
        ),
        MusicTrack(
            id = "track_celestial_drift",
            title = "Celestial Drift",
            artist = "Starlight Theory",
            album = "Deep Cosmos",
            genre = "Ambient Space",
            durationMs = 240000L,
            colorHex = "#38BDF8",
            bassFrequency = 50f,
            tempoBpm = 95,
            isFavorite = true,
            playCount = 22
        ),
        MusicTrack(
            id = "track_lofi_rain",
            title = "Coffee & Rain",
            artist = "Velvet Chill",
            album = "Sunday Melancholy",
            genre = "Lo-Fi Beats",
            durationMs = 168000L,
            colorHex = "#F59E0B",
            bassFrequency = 90f,
            tempoBpm = 82,
            isFavorite = true,
            playCount = 31
        ),
        MusicTrack(
            id = "track_quantum_surge",
            title = "Quantum Surge",
            artist = "Bass Protocol",
            album = "Subwoofer Warfare",
            genre = "Bass Heavy",
            durationMs = 184000L,
            colorHex = "#F43F5E",
            bassFrequency = 45f,
            tempoBpm = 140,
            isFavorite = false,
            playCount = 17
        ),
        MusicTrack(
            id = "track_aurora_borealis",
            title = "Aurora Nights",
            artist = "Nordic Bloom",
            album = "Glacial Reverie",
            genre = "Acoustic Chill",
            durationMs = 225000L,
            colorHex = "#10B981",
            bassFrequency = 110f,
            tempoBpm = 105,
            isFavorite = false,
            playCount = 6
        ),
        MusicTrack(
            id = "track_solar_flare",
            title = "Solar Flare",
            artist = "Hyperion 9",
            album = "Orbital Decay",
            genre = "Cyberpunk",
            durationMs = 202000L,
            colorHex = "#EC4899",
            bassFrequency = 55f,
            tempoBpm = 132,
            isFavorite = true,
            playCount = 19
        )
    )

    val defaultPlaylists = listOf(
        PlaylistModel(
            id = "playlist_cyber_night",
            name = "Cyber Nightride",
            description = "High octane synthwave and neon pulses for nighttime cruising",
            colorHex = "#00E5FF",
            trackIds = listOf("track_neon_skyline", "track_midnight_drive", "track_solar_flare")
        ),
        PlaylistModel(
            id = "playlist_chill_focus",
            name = "Deep Focus Lo-Fi",
            description = "Relaxing acoustic rhythms, soft pads and ambient frequencies",
            colorHex = "#F59E0B",
            trackIds = listOf("track_lofi_rain", "track_celestial_drift", "track_aurora_borealis")
        ),
        PlaylistModel(
            id = "playlist_bass_beast",
            name = "Bass Sub-Zero",
            description = "Heavy sub-bass kicks and deep low-end frequencies to test your EQ",
            colorHex = "#F43F5E",
            trackIds = listOf("track_quantum_surge", "track_neon_skyline", "track_solar_flare")
        )
    )
}
