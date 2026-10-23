package com.example.service

import android.app.PendingIntent
import android.content.Intent
import android.media.MediaDescription
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.util.Log
import com.example.MainActivity
import com.example.data.local.OfflineMusicCatalog
import com.example.model.CuratedRadioStations
import com.example.model.PublicSafetyFeeds

/**
 * Android Auto and Automotive OS MediaBrowserService.
 * Exposes the music vault, MyRadio stations, Spotify, Apple Music, and live public safety/police feeds to the car head unit display.
 */
class AutoMediaBrowserService : MediaBrowserService() {

    private val tag = "AutoMediaService"
    private lateinit var mediaSession: MediaSession
    private var currentPlayingIndex = 0
    private var isPlaying = false

    companion object {
        const val ROOT_ID = "root_resonance"
        const val NODE_MUSIC = "node_music"
        const val NODE_SCANNERS = "node_scanners"
        const val NODE_RADIO = "node_radio"
        const val NODE_SPOTIFY = "node_spotify"
        const val NODE_APPLE_MUSIC = "node_apple_music"
        const val NODE_FAVORITES = "node_favorites"

        const val ACTION_TOGGLE_SCANNER = "com.example.action.TOGGLE_SCANNER"
        const val ACTION_AI_ACOUSTIC_TUNE = "com.example.action.AI_ACOUSTIC_TUNE"
    }

    override fun onCreate() {
        super.onCreate()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession(this, "ResonanceAutoMediaSession").apply {
            setSessionActivity(pendingIntent)
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)

            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    isPlaying = true
                    updatePlaybackState(PlaybackState.STATE_PLAYING)
                }

                override fun onPause() {
                    isPlaying = false
                    updatePlaybackState(PlaybackState.STATE_PAUSED)
                }

                override fun onSkipToNext() {
                    val tracks = OfflineMusicCatalog.sampleTracks
                    if (tracks.isNotEmpty()) {
                        currentPlayingIndex = (currentPlayingIndex + 1) % tracks.size
                        playTrackByIndex(currentPlayingIndex)
                    }
                }

                override fun onSkipToPrevious() {
                    val tracks = OfflineMusicCatalog.sampleTracks
                    if (tracks.isNotEmpty()) {
                        currentPlayingIndex = if (currentPlayingIndex > 0) currentPlayingIndex - 1 else tracks.lastIndex
                        playTrackByIndex(currentPlayingIndex)
                    }
                }

                override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                    mediaId?.let { id ->
                        when {
                            id.startsWith("track_") -> {
                                val tracks = OfflineMusicCatalog.sampleTracks
                                val idx = tracks.indexOfFirst { it.id == id }
                                if (idx != -1) {
                                    currentPlayingIndex = idx
                                    playTrackByIndex(idx)
                                }
                            }
                            id.startsWith("scanner_") || id.startsWith("ch_") -> {
                                val channel = PublicSafetyFeeds.channels.find { it.id == id || "scanner_${it.id}" == id }
                                channel?.let { ch ->
                                    playBroadcastOnAuto(ch.name, ch.department, ch.frequencyMhz)
                                }
                            }
                            id.startsWith("radio_") || id.startsWith("rad_") -> {
                                val station = CuratedRadioStations.stations.find { it.stationUuid == id || "radio_${it.stationUuid}" == id }
                                station?.let { rad ->
                                    playBroadcastOnAuto(rad.name, rad.genre, "${rad.bitrateKbps} kbps • MyRadio")
                                }
                            }
                            id.startsWith("sp_") -> {
                                val track = CuratedRadioStations.sampleSpotifyTracks.find { it.id == id }
                                track?.let { sp ->
                                    playBroadcastOnAuto(sp.name, sp.artist, "${sp.album} • Spotify")
                                }
                            }
                            id.startsWith("am_") -> {
                                val track = CuratedRadioStations.sampleAppleMusicTracks.find { it.id == id }
                                track?.let { am ->
                                    playBroadcastOnAuto(am.name, am.artist, "${am.album} • Apple Music")
                                }
                            }
                        }
                    }
                }

                override fun onCustomAction(action: String, extras: Bundle?) {
                    when (action) {
                        ACTION_TOGGLE_SCANNER -> {
                            Log.d(tag, "Custom Action: Toggle Scanner on Android Auto")
                        }
                        ACTION_AI_ACOUSTIC_TUNE -> {
                            Log.d(tag, "Custom Action: AI Acoustic Tune triggered from Steering Wheel / Console")
                        }
                    }
                }
            })
        }

        sessionToken = mediaSession.sessionToken
        updatePlaybackState(PlaybackState.STATE_PAUSED)
    }

    private fun playTrackByIndex(index: Int) {
        val tracks = OfflineMusicCatalog.sampleTracks
        if (index in tracks.indices) {
            val track = tracks[index]
            val metadata = MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, track.id)
                .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, track.album)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, track.durationMs)
                .build()

            mediaSession.setMetadata(metadata)
            isPlaying = true
            updatePlaybackState(PlaybackState.STATE_PLAYING)
        }
    }

    private fun playBroadcastOnAuto(title: String, artist: String, albumOrInfo: String) {
        val metadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "stream_live")
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, albumOrInfo)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, -1L) // Live broadcast
            .build()

        mediaSession.setMetadata(metadata)
        isPlaying = true
        updatePlaybackState(PlaybackState.STATE_PLAYING)
    }

    private fun updatePlaybackState(state: Int) {
        val customActionScanner = PlaybackState.CustomAction.Builder(
            ACTION_TOGGLE_SCANNER,
            "911 Scanner",
            android.R.drawable.ic_dialog_alert
        ).build()

        val customActionAi = PlaybackState.CustomAction.Builder(
            ACTION_AI_ACOUSTIC_TUNE,
            "AI Auto-Tune",
            android.R.drawable.ic_menu_compass
        ).build()

        val playbackState = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackState.ACTION_PLAY_FROM_MEDIA_ID or
                        PlaybackState.ACTION_PLAY_PAUSE
            )
            .addCustomAction(customActionScanner)
            .addCustomAction(customActionAi)
            .setState(state, 0L, 1.0f)
            .build()

        mediaSession.setPlaybackState(playbackState)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot {
        Log.d(tag, "Android Auto client connected: $clientPackageName (uid=$clientUid)")
        return BrowserRoot(ROOT_ID, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowser.MediaItem>>) {
        val mediaItems = mutableListOf<MediaBrowser.MediaItem>()

        when (parentId) {
            ROOT_ID -> {
                // Top-Level Tabs shown on Android Auto Car Display
                mediaItems.add(
                    createBrowsableItem(
                        NODE_MUSIC,
                        "Music Vault",
                        "High-Fidelity Offline Audio Tracks"
                    )
                )
                mediaItems.add(
                    createBrowsableItem(
                        NODE_RADIO,
                        "MyRadio FM & Global",
                        "Live Internet Radio Broadcasts"
                    )
                )
                mediaItems.add(
                    createBrowsableItem(
                        NODE_SPOTIFY,
                        "Spotify Streams",
                        "Spotify Hits & Preview Streams"
                    )
                )
                mediaItems.add(
                    createBrowsableItem(
                        NODE_APPLE_MUSIC,
                        "Apple Music Lossless",
                        "Spatial Audio & Apple Lossless Streams"
                    )
                )
                mediaItems.add(
                    createBrowsableItem(
                        NODE_SCANNERS,
                        "Public Safety & Police Feeds",
                        "Live 911 / Police / Fire / EMS Feeds"
                    )
                )
                mediaItems.add(
                    createBrowsableItem(
                        NODE_FAVORITES,
                        "Quick Favorites",
                        "Pinned Tracks and Dispatches"
                    )
                )
            }
            NODE_MUSIC -> {
                OfflineMusicCatalog.sampleTracks.forEach { track ->
                    val desc = MediaDescription.Builder()
                        .setMediaId(track.id)
                        .setTitle(track.title)
                        .setSubtitle(track.artist)
                        .setDescription("${track.album} • ${track.genre}")
                        .build()
                    mediaItems.add(MediaBrowser.MediaItem(desc, MediaBrowser.MediaItem.FLAG_PLAYABLE))
                }
            }
            NODE_RADIO -> {
                CuratedRadioStations.stations.forEach { station ->
                    val desc = MediaDescription.Builder()
                        .setMediaId("radio_${station.stationUuid}")
                        .setTitle(station.name)
                        .setSubtitle("${station.genre} • ${station.country}")
                        .setDescription("${station.bitrateKbps} kbps MP3 • MyRadio")
                        .build()
                    mediaItems.add(MediaBrowser.MediaItem(desc, MediaBrowser.MediaItem.FLAG_PLAYABLE))
                }
            }
            NODE_SPOTIFY -> {
                CuratedRadioStations.sampleSpotifyTracks.forEach { track ->
                    val desc = MediaDescription.Builder()
                        .setMediaId(track.id)
                        .setTitle(track.name)
                        .setSubtitle(track.artist)
                        .setDescription("${track.album} • Spotify")
                        .build()
                    mediaItems.add(MediaBrowser.MediaItem(desc, MediaBrowser.MediaItem.FLAG_PLAYABLE))
                }
            }
            NODE_APPLE_MUSIC -> {
                CuratedRadioStations.sampleAppleMusicTracks.forEach { track ->
                    val desc = MediaDescription.Builder()
                        .setMediaId(track.id)
                        .setTitle(track.name)
                        .setSubtitle(track.artist)
                        .setDescription("${track.album} • Spatial Audio")
                        .build()
                    mediaItems.add(MediaBrowser.MediaItem(desc, MediaBrowser.MediaItem.FLAG_PLAYABLE))
                }
            }
            NODE_SCANNERS -> {
                PublicSafetyFeeds.channels.forEach { channel ->
                    val desc = MediaDescription.Builder()
                        .setMediaId("scanner_${channel.id}")
                        .setTitle(channel.name)
                        .setSubtitle("${channel.frequencyMhz} • ${channel.department}")
                        .setDescription("${channel.location} • ${channel.listenerCount} listeners")
                        .build()
                    mediaItems.add(MediaBrowser.MediaItem(desc, MediaBrowser.MediaItem.FLAG_PLAYABLE))
                }
            }
            NODE_FAVORITES -> {
                OfflineMusicCatalog.sampleTracks.take(3).forEach { track ->
                    val desc = MediaDescription.Builder()
                        .setMediaId(track.id)
                        .setTitle("★ ${track.title}")
                        .setSubtitle(track.artist)
                        .setDescription(track.album)
                        .build()
                    mediaItems.add(MediaBrowser.MediaItem(desc, MediaBrowser.MediaItem.FLAG_PLAYABLE))
                }
            }
        }

        result.sendResult(mediaItems)
    }

    private fun createBrowsableItem(id: String, title: String, subtitle: String): MediaBrowser.MediaItem {
        val description = MediaDescription.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(subtitle)
            .build()
        return MediaBrowser.MediaItem(description, MediaBrowser.MediaItem.FLAG_BROWSABLE)
    }

    override fun onDestroy() {
        mediaSession.release()
        super.onDestroy()
    }
}
