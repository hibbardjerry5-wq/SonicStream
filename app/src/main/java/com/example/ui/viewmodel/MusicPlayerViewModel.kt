package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AiAcousticTunerService
import com.example.audio.AcousticEnvironmentSample
import com.example.audio.EnvironmentAudioAnalyzer
import com.example.audio.AudioEngine
import com.example.audio.VisualizerFrame
import com.example.data.local.OfflineMusicCatalog
import com.example.data.remote.MusicSyncRepository
import com.example.model.MusicTrack
import com.example.model.PlaylistModel
import com.example.model.UserSettingsModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class RepeatMode { OFF, ALL, ONE }

enum class AppTab { NOW_PLAYING, LIBRARY, STREAMS, SCANNER, EQUALIZER, VISUALIZER, CLOUD_SYNC }

enum class VisualizerMode(val displayName: String) {
    HYPER_TUNNEL_3D("3D Cyber Tunnel"),
    CHRONO_TORUS_3D("3D Chrono Torus"),
    CHRONOGRAPH_HUD("Luxury Chrono HUD"),
    MONOLITH_BARS_3D("3D Monolith Matrix"),
    QUANTUM_VORTEX("Quantum Starfield"),
    SILK_RIBBON_3D("3D Silk Ribbon"),
    SPECTRUM_BARS("Dual Spectrum Bars"),
    RADIAL_PULSE("Radial Pulse"),
    OSCILLOSCOPE("Oscilloscope Waves"),
    PARTICLE_FIELD("Particle Field"),
    CYBER_GRID("Cyber Horizon Grid")
}

data class PlayerUiState(
    val currentTab: AppTab = AppTab.NOW_PLAYING,
    val currentTrack: MusicTrack = OfflineMusicCatalog.sampleTracks.first(),
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 210000L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val tracks: List<MusicTrack> = OfflineMusicCatalog.sampleTracks,
    val playlists: List<PlaylistModel> = OfflineMusicCatalog.defaultPlaylists,
    val activePlaylist: PlaylistModel? = null,
    
    // Equalizer & Sound
    val equalizerBands: List<Int> = listOf(3, 1, 0, 2, 4), // 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
    val bassBoost: Int = 450, // 0..1000
    val virtualizer: Int = 350, // 0..1000
    val reverbPreset: String = "Small Room",
    val activePreset: String = "Electronic",
    val volumeNormalization: Boolean = false,
    val stereoBalance: Float = 0f, // -1f (L) to +1f (R)

    // Visualizer & Theme
    val visualizerMode: VisualizerMode = VisualizerMode.SPECTRUM_BARS,
    val isFullscreenVisualizer: Boolean = false,
    val themeAccent: String = "Cyber Cyan",
    val visualizerSensitivity: Float = 1.0f,

    // Cloud Sync
    val isCloudSignedIn: Boolean = false,
    val userEmail: String? = null,
    val userDisplayName: String? = null,
    val isSyncing: Boolean = false,
    val lastSyncMessage: String = "Offline storage ready",
    val syncSuccessCount: Int = 0,

    // Car Stereo Speaker Controls
    val isCarModeActive: Boolean = false,
    val carBalanceX: Float = 0f, // -1f (Left) to +1f (Right)
    val carFaderY: Float = 0.15f, // -1f (Rear) to +1f (Front)
    val subwooferLevelDb: Int = 4, // 0 to 12 dB
    val subwooferCrossoverHz: Int = 80, // 50, 80, 120 Hz
    val isLoudnessEnabled: Boolean = true, // Low volume / road rumble loudness
    val listeningPositionPreset: String = "All Cabin", // "Driver Seat", "Front Seats", "All Cabin", "Rear Seats"
    val carVehiclePreset: String = "Sedan", // "Sedan", "SUV / Truck", "Sports Coupe"

    // jetAudio Audiophile Sound Rack
    val xBass: Int = 50, // 0..100
    val xSurround: Int = 40, // 0..100
    val crystalizer: Int = 60, // 0..100 (high harmonic exciter)
    val playbackSpeed: Float = 1.0f,
    val playbackPitchSemi: Float = 0f,
    val vuMeterLeft: Float = 0.45f,
    val vuMeterRight: Float = 0.50f,

    // Animated Boot Screen & Voice Welcome
    val isBootCompleted: Boolean = false,
    val isVoiceWelcomeEnabled: Boolean = true,

    // 911 & Police Dispatch Scanner Feed
    val isScannerActive: Boolean = false,
    val selectedScannerChannel: com.example.model.ScannerChannel = com.example.model.PublicSafetyFeeds.channels.first(),
    val isScannerTransmitting: Boolean = false,
    val scannerVolume: Float = 0.85f,
    val duckingPercent: Int = 75,
    val activeTransmission: com.example.model.DispatchTransmission? = null,
    val scannerChannels: List<com.example.model.ScannerChannel> = com.example.model.PublicSafetyFeeds.channels,
    val musicVolume: Float = 1.0f,
    val scannerStreamState: com.example.model.ScannerStreamState = com.example.model.ScannerStreamState.IDLE,
    val scannerAudioLevel: Float = 0f,
    val scannerBufferPercent: Int = 0,
    val scannerSearchQuery: String = "",
    val scannerCategoryFilter: String = "ALL",
    val scannerPan: Float = 0.20f,
    val isDuckingEnabled: Boolean = true,
    val squelchThreshold: Float = 0.30f,

    // Internet Radio (MyRadio), Spotify & Apple Music Streams
    val radioStations: List<com.example.model.RadioStation> = com.example.model.CuratedRadioStations.stations,
    val activeRadioStation: com.example.model.RadioStation? = null,
    val isRadioPlaying: Boolean = false,
    val spotifyTracks: List<com.example.model.SpotifyTrackItem> = com.example.model.CuratedRadioStations.sampleSpotifyTracks,
    val activeSpotifyTrack: com.example.model.SpotifyTrackItem? = null,
    val isSpotifyPlaying: Boolean = false,
    val appleMusicTracks: List<com.example.model.AppleMusicTrackItem> = com.example.model.CuratedRadioStations.sampleAppleMusicTracks,
    val activeAppleMusicTrack: com.example.model.AppleMusicTrackItem? = null,
    val isAppleMusicPlaying: Boolean = false,

    // AI Acoustic Environment Auto-Tuning
    val isAiAcousticTuningActive: Boolean = false,
    val isAiListeningMic: Boolean = false,
    val aiCalibrationState: String = "Idle",
    val ambientSplDb: Float = 44f,
    val ambientEnvironmentType: String = "Normal Room Acoustics",
    val ambientLowRumble: Float = 0.25f,
    val ambientLowMid: Float = 0.25f,
    val ambientMidVocal: Float = 0.25f,
    val ambientHighMid: Float = 0.15f,
    val ambientTreble: Float = 0.10f,
    val isLiveAdaptiveAiEnabled: Boolean = false,
    val aiExplanation: String = "Gemini AI Acoustic Auto-Tuner evaluates ambient microphone noise levels, room reverberation, and frequency absorption to dynamically sculpt the 5-band equalizer, bass boost, and road loudness in real time.",
    val aiLastTunedSource: String = "Gemini 3.5 Flash",
    val isCalibratingNow: Boolean = false,

    // Futuristic Cybernetic Neural Audio OS
    val isNeuralProcessing: Boolean = false,
    val neuralPromptInput: String = "",
    val aiConfidencePercent: Int = 98,
    val psychoacousticMaskingPercent: Int = 42,
    val spatial360Degree: Int = 45, // 0..100
    val neuralHarmonicExciter: Int = 60, // 0..100
    val lastNeuralCommand: String = "Autonomous Soundstage Lock",
    val isFluidAuraActive: Boolean = true,
    val cyberneticHudEnabled: Boolean = true,
    val neuralNodesActive: Int = 16,
    val neuralDspLatencyMs: Float = 1.2f
)

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val audioEngine = AudioEngine(viewModelScope)
    private val syncRepository = MusicSyncRepository(application)
    private val environmentAnalyzer = EnvironmentAudioAnalyzer(application)
    private val aiTunerService = AiAcousticTunerService()
    private val voiceWelcomeService = com.example.audio.VoiceWelcomeService(application)
    private val liveScannerPlayer = com.example.audio.LiveScannerAudioPlayer(application, viewModelScope) { isDucking, _ ->
        if (isDucking) {
            audioEngine.setScannerSettings(true, _uiState.value.scannerVolume, _uiState.value.duckingPercent)
            audioEngine.setScannerTransmitting(true)
        } else {
            audioEngine.setScannerTransmitting(false)
        }
    }
    private val scannerRadioService = com.example.data.remote.PublicSafetyRadioService()
    private val internetRadioService = com.example.data.remote.InternetRadioService()
    private val spotifyApiService = com.example.data.remote.SpotifyApiService()
    private val appleMusicApiService = com.example.data.remote.AppleMusicApiService()

    private var streamPlayer: android.media.MediaPlayer? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val visualizerFrame: StateFlow<VisualizerFrame> = audioEngine.visualizerFrame

    private val auth = FirebaseAuth.getInstance()
    private var liveAdaptiveJob: kotlinx.coroutines.Job? = null

    init {
        // Observe audio engine states
        viewModelScope.launch {
            audioEngine.isPlaying.collect { playing ->
                _uiState.value = _uiState.value.copy(isPlaying = playing)
            }
        }
        viewModelScope.launch {
            audioEngine.currentPositionMs.collect { pos ->
                _uiState.value = _uiState.value.copy(currentPositionMs = pos)
            }
        }

        // Observe environmental acoustic samples
        viewModelScope.launch {
            environmentAnalyzer.acousticSample.collect { sample ->
                _uiState.value = _uiState.value.copy(
                    ambientSplDb = sample.splDb,
                    ambientEnvironmentType = sample.estimatedEnvironment,
                    ambientLowRumble = sample.lowRumbleRatio,
                    ambientLowMid = sample.lowMidRatio,
                    ambientMidVocal = sample.midVocalRatio,
                    ambientHighMid = sample.highMidRatio,
                    ambientTreble = sample.trebleRatio,
                    isAiListeningMic = sample.isMicrophoneActive
                )
            }
        }

        // Observe live scanner player telemetry
        viewModelScope.launch {
            liveScannerPlayer.streamState.collect { state ->
                _uiState.value = _uiState.value.copy(
                    scannerStreamState = state,
                    isScannerActive = state == com.example.model.ScannerStreamState.STREAMING || state == com.example.model.ScannerStreamState.CONNECTING
                )
            }
        }
        viewModelScope.launch {
            liveScannerPlayer.audioLevel.collect { lvl ->
                _uiState.value = _uiState.value.copy(scannerAudioLevel = lvl)
            }
        }
        viewModelScope.launch {
            liveScannerPlayer.bufferPercent.collect { buf ->
                _uiState.value = _uiState.value.copy(scannerBufferPercent = buf)
            }
        }
        viewModelScope.launch {
            liveScannerPlayer.isTransmitting.collect { tx ->
                _uiState.value = _uiState.value.copy(isScannerTransmitting = tx)
            }
        }
        viewModelScope.launch {
            liveScannerPlayer.activeTransmission.collect { item ->
                _uiState.value = _uiState.value.copy(activeTransmission = item)
            }
        }

        // Apply initial EQ & DSP
        audioEngine.updateEqualizer(_uiState.value.equalizerBands)
        audioEngine.updateDsp(
            _uiState.value.bassBoost,
            _uiState.value.virtualizer,
            _uiState.value.reverbPreset,
            _uiState.value.volumeNormalization
        )
        audioEngine.updateCarStereoSettings(
            _uiState.value.carBalanceX,
            _uiState.value.carFaderY,
            _uiState.value.subwooferLevelDb.toFloat(),
            _uiState.value.subwooferCrossoverHz,
            _uiState.value.isLoudnessEnabled
        )

        // Observe visualizer frame to compute ballistic jetAudio VU meters
        viewModelScope.launch {
            audioEngine.visualizerFrame.collect { frame ->
                if (_uiState.value.isPlaying) {
                    val energyL = (frame.frequencyBands.take(16).average().toFloat() * (1f - _uiState.value.carBalanceX * 0.4f)).coerceIn(0.1f, 0.95f)
                    val energyR = (frame.frequencyBands.takeLast(16).average().toFloat() * (1f + _uiState.value.carBalanceX * 0.4f)).coerceIn(0.1f, 0.95f)
                    _uiState.value = _uiState.value.copy(
                        vuMeterLeft = _uiState.value.vuMeterLeft * 0.65f + energyL * 0.35f,
                        vuMeterRight = _uiState.value.vuMeterRight * 0.65f + energyR * 0.35f
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        vuMeterLeft = (_uiState.value.vuMeterLeft * 0.8f).coerceAtLeast(0f),
                        vuMeterRight = (_uiState.value.vuMeterRight * 0.8f).coerceAtLeast(0f)
                    )
                }
            }
        }

        // Periodic simulated dispatch transmissions when scanner is active
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(24000L)
                if (_uiState.value.isScannerActive && !_uiState.value.isScannerTransmitting) {
                    val candidate = com.example.model.PublicSafetyFeeds.sampleTransmissions
                        .filter { it.channelId == _uiState.value.selectedScannerChannel.id }
                        .randomOrNull() ?: com.example.model.PublicSafetyFeeds.sampleTransmissions.random()
                    triggerDispatchTransmission(candidate)
                }
            }
        }

        // Check initial auth status
        checkAuthStatus()

        // Listen for Auth changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _uiState.value = _uiState.value.copy(
                isCloudSignedIn = user != null,
                userEmail = user?.email,
                userDisplayName = user?.displayName ?: user?.email?.substringBefore("@")
            )
            if (user != null) {
                observeCloudData()
            }
        }
    }

    fun checkAuthStatus() {
        val user = auth.currentUser
        _uiState.value = _uiState.value.copy(
            isCloudSignedIn = user != null,
            userEmail = user?.email,
            userDisplayName = user?.displayName ?: user?.email?.substringBefore("@")
        )
        if (user != null) {
            observeCloudData()
        }
    }

    private fun observeCloudData() {
        viewModelScope.launch {
            try {
                syncRepository.observePlaylists().collect { cloudPlaylists ->
                    if (cloudPlaylists.isNotEmpty()) {
                        // Merge default + cloud playlists without duplicates
                        val existingIds = cloudPlaylists.map { it.id }.toSet()
                        val merged = cloudPlaylists + OfflineMusicCatalog.defaultPlaylists.filter { it.id !in existingIds }
                        _uiState.value = _uiState.value.copy(
                            playlists = merged,
                            lastSyncMessage = "Synced ${cloudPlaylists.size} playlists from cloud"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(lastSyncMessage = "Cloud playlist sync standby: ${e.message}")
            }
        }

        viewModelScope.launch {
            try {
                syncRepository.observeUserSettings().collect { settings ->
                    settings?.let {
                        val visMode = try {
                            VisualizerMode.values().find { vm -> vm.displayName == it.visualizerMode } ?: VisualizerMode.SPECTRUM_BARS
                        } catch (e: Exception) {
                            VisualizerMode.SPECTRUM_BARS
                        }
                        _uiState.value = _uiState.value.copy(
                            equalizerBands = if (it.equalizerBands.size >= 5) it.equalizerBands else _uiState.value.equalizerBands,
                            bassBoost = it.bassBoost,
                            virtualizer = it.virtualizer,
                            reverbPreset = it.reverbPreset,
                            activePreset = it.activePreset,
                            themeAccent = it.themeAccent,
                            visualizerMode = visMode,
                            volumeNormalization = it.volumeNormalization,
                            lastSyncMessage = "Audio profile synced from cloud"
                        )
                        audioEngine.updateEqualizer(_uiState.value.equalizerBands)
                        audioEngine.updateDsp(
                            it.bassBoost,
                            it.virtualizer,
                            it.reverbPreset,
                            it.volumeNormalization
                        )
                    }
                }
            } catch (e: Exception) {
                // Keep local settings
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            audioEngine.pause()
        } else {
            audioEngine.playTrack(_uiState.value.currentTrack, _uiState.value.currentPositionMs)
        }
    }

    fun playTrack(track: MusicTrack) {
        val updatedTracks = _uiState.value.tracks.map {
            if (it.id == track.id) it.copy(playCount = it.playCount + 1) else it
        }
        val targetTrack = updatedTracks.find { it.id == track.id } ?: track
        _uiState.value = _uiState.value.copy(
            currentTrack = targetTrack,
            durationMs = targetTrack.durationMs,
            tracks = updatedTracks
        )
        audioEngine.playTrack(targetTrack, 0L)
    }

    fun playNext() {
        val list = _uiState.value.tracks
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _uiState.value.currentTrack.id }
        val nextIndex = if (_uiState.value.isShuffle) {
            (list.indices).random()
        } else {
            (currentIndex + 1) % list.size
        }
        playTrack(list[nextIndex])
    }

    fun playPrevious() {
        val list = _uiState.value.tracks
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _uiState.value.currentTrack.id }
        val prevIndex = if (currentIndex <= 0) list.lastIndex else currentIndex - 1
        playTrack(list[prevIndex])
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun toggleShuffle() {
        _uiState.value = _uiState.value.copy(isShuffle = !_uiState.value.isShuffle)
    }

    fun cycleRepeatMode() {
        val nextMode = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _uiState.value = _uiState.value.copy(repeatMode = nextMode)
    }

    fun toggleFavorite(trackId: String) {
        val updated = _uiState.value.tracks.map {
            if (it.id == trackId) it.copy(isFavorite = !it.isFavorite) else it
        }
        val current = if (_uiState.value.currentTrack.id == trackId) {
            _uiState.value.currentTrack.copy(isFavorite = !_uiState.value.currentTrack.isFavorite)
        } else {
            _uiState.value.currentTrack
        }
        _uiState.value = _uiState.value.copy(tracks = updated, currentTrack = current)

        // Sync favorite to cloud if signed in
        if (_uiState.value.isCloudSignedIn) {
            viewModelScope.launch {
                val tr = updated.find { it.id == trackId }
                if (tr != null) {
                    syncRepository.syncTrackFavorite(tr, tr.isFavorite)
                }
            }
        }
    }

    // --- SOUND & EQUALIZER CUSTOMIZATION ---

    fun setEqualizerBand(index: Int, gainDb: Int) {
        val currentBands = _uiState.value.equalizerBands.toMutableList()
        if (index in currentBands.indices) {
            currentBands[index] = gainDb.coerceIn(-10, 10)
            _uiState.value = _uiState.value.copy(
                equalizerBands = currentBands,
                activePreset = "Custom"
            )
            audioEngine.updateEqualizer(currentBands)
        }
    }

    fun applyEqPreset(presetName: String) {
        val bands = when (presetName) {
            "Bass Boost" -> listOf(7, 5, 1, 2, 3)
            "Vocal Booster" -> listOf(-2, 1, 6, 4, 1)
            "Electronic" -> listOf(5, 3, 0, 3, 6)
            "Rock" -> listOf(6, 3, -1, 3, 5)
            "Acoustic" -> listOf(3, 2, 2, 4, 3)
            "Jazz" -> listOf(4, 2, 1, 2, 3)
            "Classical" -> listOf(4, 3, -1, 3, 4)
            "Hip-Hop" -> listOf(8, 5, 0, 2, 4)
            "Flat" -> listOf(0, 0, 0, 0, 0)
            else -> _uiState.value.equalizerBands
        }
        _uiState.value = _uiState.value.copy(
            activePreset = presetName,
            equalizerBands = bands
        )
        audioEngine.updateEqualizer(bands)
    }

    fun setBassBoost(value: Int) {
        val clamped = value.coerceIn(0, 1000)
        _uiState.value = _uiState.value.copy(bassBoost = clamped)
        audioEngine.updateDsp(
            clamped,
            _uiState.value.virtualizer,
            _uiState.value.reverbPreset,
            _uiState.value.volumeNormalization
        )
    }

    fun setVirtualizer(value: Int) {
        val clamped = value.coerceIn(0, 1000)
        _uiState.value = _uiState.value.copy(virtualizer = clamped)
        audioEngine.updateDsp(
            _uiState.value.bassBoost,
            clamped,
            _uiState.value.reverbPreset,
            _uiState.value.volumeNormalization
        )
    }

    fun setReverbPreset(reverb: String) {
        _uiState.value = _uiState.value.copy(reverbPreset = reverb)
        audioEngine.updateDsp(
            _uiState.value.bassBoost,
            _uiState.value.virtualizer,
            reverb,
            _uiState.value.volumeNormalization
        )
    }

    fun toggleVolumeNormalization() {
        val next = !_uiState.value.volumeNormalization
        _uiState.value = _uiState.value.copy(volumeNormalization = next)
        audioEngine.updateDsp(
            _uiState.value.bassBoost,
            _uiState.value.virtualizer,
            _uiState.value.reverbPreset,
            next
        )
    }

    // --- VISUALIZER & THEME CUSTOMIZATION ---

    fun setVisualizerMode(mode: VisualizerMode) {
        _uiState.value = _uiState.value.copy(visualizerMode = mode)
    }

    fun toggleFullscreenVisualizer() {
        _uiState.value = _uiState.value.copy(isFullscreenVisualizer = !_uiState.value.isFullscreenVisualizer)
    }

    fun setThemeAccent(themeName: String) {
        _uiState.value = _uiState.value.copy(themeAccent = themeName)
    }

    fun setVisualizerSensitivity(sensitivity: Float) {
        _uiState.value = _uiState.value.copy(visualizerSensitivity = sensitivity.coerceIn(0.5f, 2.5f))
    }

    // --- OFFLINE PLAYLIST MANAGEMENT ---

    fun createPlaylist(name: String, description: String, colorHex: String) {
        val newPlaylist = PlaylistModel(
            id = "pl_${UUID.randomUUID().toString().take(8)}",
            userId = syncRepository.currentUserId ?: "local_user",
            name = name,
            description = description.ifBlank { null },
            colorHex = colorHex,
            trackIds = emptyList()
        )
        val updated = _uiState.value.playlists + newPlaylist
        _uiState.value = _uiState.value.copy(playlists = updated)

        if (_uiState.value.isCloudSignedIn) {
            viewModelScope.launch {
                syncRepository.savePlaylist(newPlaylist)
            }
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        val updated = _uiState.value.playlists.map { pl ->
            if (pl.id == playlistId && trackId !in pl.trackIds) {
                pl.copy(trackIds = pl.trackIds + trackId)
            } else pl
        }
        _uiState.value = _uiState.value.copy(playlists = updated)

        if (_uiState.value.isCloudSignedIn) {
            viewModelScope.launch {
                updated.find { it.id == playlistId }?.let {
                    syncRepository.savePlaylist(it)
                }
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        val updated = _uiState.value.playlists.map { pl ->
            if (pl.id == playlistId) {
                pl.copy(trackIds = pl.trackIds - trackId)
            } else pl
        }
        _uiState.value = _uiState.value.copy(playlists = updated)

        if (_uiState.value.isCloudSignedIn) {
            viewModelScope.launch {
                updated.find { it.id == playlistId }?.let {
                    syncRepository.savePlaylist(it)
                }
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        val updated = _uiState.value.playlists.filterNot { it.id == playlistId }
        _uiState.value = _uiState.value.copy(playlists = updated)

        if (_uiState.value.isCloudSignedIn) {
            viewModelScope.launch {
                syncRepository.deletePlaylist(playlistId)
            }
        }
    }

    // --- MANUAL CLOUD SYNC TRIGGER ---

    fun triggerCloudSync() {
        if (!_uiState.value.isCloudSignedIn) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, lastSyncMessage = "Synchronizing with Cloud Firestore...")
            try {
                // Save current settings to cloud
                val settings = UserSettingsModel(
                    userId = syncRepository.requireUserId(),
                    equalizerBands = _uiState.value.equalizerBands,
                    bassBoost = _uiState.value.bassBoost,
                    virtualizer = _uiState.value.virtualizer,
                    reverbPreset = _uiState.value.reverbPreset,
                    activePreset = _uiState.value.activePreset,
                    themeAccent = _uiState.value.themeAccent,
                    visualizerMode = _uiState.value.visualizerMode.displayName,
                    volumeNormalization = _uiState.value.volumeNormalization
                )
                syncRepository.saveUserSettings(settings)

                // Sync custom playlists
                _uiState.value.playlists.forEach { pl ->
                    syncRepository.savePlaylist(pl.copy(userId = syncRepository.requireUserId()))
                }

                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    lastSyncMessage = "Library & Equalizer successfully synced with cloud!",
                    syncSuccessCount = _uiState.value.syncSuccessCount + 1
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    lastSyncMessage = "Sync error: ${e.message}"
                )
            }
        }
    }

    // --- CAR STEREO SPEAKER CONTROLS ---

    fun toggleCarMode() {
        val next = !_uiState.value.isCarModeActive
        _uiState.value = _uiState.value.copy(isCarModeActive = next)
    }

    fun setCarFaderBalance(x: Float, y: Float) {
        val clampedX = x.coerceIn(-1f, 1f)
        val clampedY = y.coerceIn(-1f, 1f)
        _uiState.value = _uiState.value.copy(
            carBalanceX = clampedX,
            carFaderY = clampedY,
            listeningPositionPreset = "Custom Focus"
        )
        audioEngine.updateCarStereoSettings(
            clampedX,
            clampedY,
            _uiState.value.subwooferLevelDb.toFloat(),
            _uiState.value.subwooferCrossoverHz,
            _uiState.value.isLoudnessEnabled
        )
    }

    fun setListeningPosition(preset: String) {
        val (x, y) = when (preset) {
            "Driver Seat" -> Pair(-0.45f, 0.45f)
            "Front Seats" -> Pair(0f, 0.50f)
            "Rear Seats" -> Pair(0f, -0.60f)
            else -> Pair(0f, 0f) // All Cabin
        }
        _uiState.value = _uiState.value.copy(
            listeningPositionPreset = preset,
            carBalanceX = x,
            carFaderY = y
        )
        audioEngine.updateCarStereoSettings(
            x,
            y,
            _uiState.value.subwooferLevelDb.toFloat(),
            _uiState.value.subwooferCrossoverHz,
            _uiState.value.isLoudnessEnabled
        )
    }

    fun setSubwooferLevel(db: Int) {
        val clamped = db.coerceIn(0, 12)
        _uiState.value = _uiState.value.copy(subwooferLevelDb = clamped)
        audioEngine.updateCarStereoSettings(
            _uiState.value.carBalanceX,
            _uiState.value.carFaderY,
            clamped.toFloat(),
            _uiState.value.subwooferCrossoverHz,
            _uiState.value.isLoudnessEnabled
        )
    }

    fun setSubwooferCrossover(hz: Int) {
        _uiState.value = _uiState.value.copy(subwooferCrossoverHz = hz)
        audioEngine.updateCarStereoSettings(
            _uiState.value.carBalanceX,
            _uiState.value.carFaderY,
            _uiState.value.subwooferLevelDb.toFloat(),
            hz,
            _uiState.value.isLoudnessEnabled
        )
    }

    fun toggleLoudness() {
        val next = !_uiState.value.isLoudnessEnabled
        _uiState.value = _uiState.value.copy(isLoudnessEnabled = next)
        audioEngine.updateCarStereoSettings(
            _uiState.value.carBalanceX,
            _uiState.value.carFaderY,
            _uiState.value.subwooferLevelDb.toFloat(),
            _uiState.value.subwooferCrossoverHz,
            next
        )
    }

    // --- jetAudio SOUND EFFECTS RACK ---

    fun setXBass(value: Int) {
        val clamped = value.coerceIn(0, 100)
        _uiState.value = _uiState.value.copy(xBass = clamped)
        setBassBoost(clamped * 10)
    }

    fun setXSurround(value: Int) {
        val clamped = value.coerceIn(0, 100)
        _uiState.value = _uiState.value.copy(xSurround = clamped)
        setVirtualizer(clamped * 10)
    }

    fun setCrystalizer(value: Int) {
        val clamped = value.coerceIn(0, 100)
        _uiState.value = _uiState.value.copy(crystalizer = clamped)
        // Adjust higher bands for crystal high harmonics
        val current = _uiState.value.equalizerBands.toMutableList()
        if (current.size >= 5) {
            current[4] = (clamped / 15).coerceIn(0, 8)
            setEqualizerBand(4, current[4])
        }
    }

    fun setPitchSpeed(speed: Float, pitchSemi: Float) {
        _uiState.value = _uiState.value.copy(
            playbackSpeed = speed.coerceIn(0.75f, 1.5f),
            playbackPitchSemi = pitchSemi.coerceIn(-5f, 5f)
        )
    }

    // --- ANIMATED BOOT & VOICE WELCOME ---

    fun completeBoot() {
        _uiState.value = _uiState.value.copy(isBootCompleted = true)
        if (_uiState.value.isVoiceWelcomeEnabled) {
            voiceWelcomeService.speakWelcomeGreeting()
        }
    }

    fun playWelcomeVoice() {
        voiceWelcomeService.speakWelcomeGreeting()
    }

    fun toggleVoiceWelcome(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isVoiceWelcomeEnabled = enabled)
    }

    // --- 911 & POLICE DISPATCH SCANNER (DUAL-AUDIO & DUCKING) ---

    fun toggleScanner(active: Boolean) {
        if (active) {
            playScannerChannel(_uiState.value.selectedScannerChannel)
        } else {
            stopScanner()
        }
    }

    fun toggleScannerPlayback() {
        if (_uiState.value.isScannerActive) {
            stopScanner()
        } else {
            playScannerChannel(_uiState.value.selectedScannerChannel)
        }
    }

    fun playScannerChannel(channel: com.example.model.ScannerChannel) {
        _uiState.value = _uiState.value.copy(
            selectedScannerChannel = channel,
            isScannerActive = true
        )
        audioEngine.setScannerSettings(true, _uiState.value.scannerVolume, _uiState.value.duckingPercent)
        liveScannerPlayer.playChannel(channel)
    }

    fun stopScanner() {
        _uiState.value = _uiState.value.copy(isScannerActive = false)
        audioEngine.setScannerSettings(false, _uiState.value.scannerVolume, _uiState.value.duckingPercent)
        audioEngine.setScannerTransmitting(false)
        liveScannerPlayer.stop()
    }

    fun selectScannerChannel(channel: com.example.model.ScannerChannel) {
        _uiState.value = _uiState.value.copy(selectedScannerChannel = channel)
        if (_uiState.value.isScannerActive) {
            playScannerChannel(channel)
        }
    }

    fun setScannerVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(scannerVolume = clamped)
        liveScannerPlayer.setMixingParameters(volume = clamped)
        audioEngine.setScannerSettings(_uiState.value.isScannerActive, clamped, _uiState.value.duckingPercent)
    }

    fun setMusicVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(musicVolume = clamped)
        audioEngine.setMusicMasterVolume(clamped)
    }

    fun setDuckingPercent(percent: Int) {
        val clamped = percent.coerceIn(10, 100)
        _uiState.value = _uiState.value.copy(duckingPercent = clamped)
        liveScannerPlayer.setMixingParameters(duckingPercent = clamped)
        audioEngine.setScannerSettings(_uiState.value.isScannerActive, _uiState.value.scannerVolume, clamped)
    }

    fun toggleDucking(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDuckingEnabled = enabled)
        liveScannerPlayer.setMixingParameters(duckingEnabled = enabled)
    }

    fun setScannerPan(pan: Float) {
        val clamped = pan.coerceIn(-1f, 1f)
        _uiState.value = _uiState.value.copy(scannerPan = clamped)
        liveScannerPlayer.setMixingParameters(pan = clamped)
    }

    fun setSquelchThreshold(thresh: Float) {
        val clamped = thresh.coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(squelchThreshold = clamped)
        liveScannerPlayer.setMixingParameters(squelch = clamped)
    }

    fun setScannerSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(scannerSearchQuery = query)
    }

    fun setScannerCategory(category: String) {
        _uiState.value = _uiState.value.copy(scannerCategoryFilter = category)
    }

    fun toggleScannerFavorite(channelId: String) {
        val updated = _uiState.value.scannerChannels.map {
            if (it.id == channelId) it.copy(isFavorite = !it.isFavorite) else it
        }
        val updatedSelected = if (_uiState.value.selectedScannerChannel.id == channelId) {
            _uiState.value.selectedScannerChannel.copy(isFavorite = !_uiState.value.selectedScannerChannel.isFavorite)
        } else {
            _uiState.value.selectedScannerChannel
        }
        _uiState.value = _uiState.value.copy(
            scannerChannels = updated,
            selectedScannerChannel = updatedSelected
        )
    }

    fun triggerManualDispatchTransmission() {
        if (!_uiState.value.isScannerActive) {
            _uiState.value = _uiState.value.copy(isScannerActive = true)
            audioEngine.setScannerSettings(true, _uiState.value.scannerVolume, _uiState.value.duckingPercent)
        }
        liveScannerPlayer.triggerTransmissionBurst(_uiState.value.selectedScannerChannel)
    }

    fun triggerDispatchTransmission(sample: com.example.model.DispatchTransmission? = null) {
        triggerManualDispatchTransmission()
    }

    fun addCustomScannerFeed(name: String, url: String, dept: String, freq: String, category: String, loc: String) {
        val newChannel = scannerRadioService.addCustomFeed(name, url, dept, freq, category, loc)
        _uiState.value = _uiState.value.copy(
            scannerChannels = listOf(newChannel) + _uiState.value.scannerChannels,
            selectedScannerChannel = newChannel
        )
        playScannerChannel(newChannel)
    }

    // --- INTERNET RADIO (MYRADIO), SPOTIFY & APPLE MUSIC STREAMING ---

    fun playRadioStation(station: com.example.model.RadioStation) {
        if (_uiState.value.isRadioPlaying && _uiState.value.activeRadioStation?.stationUuid == station.stationUuid) {
            stopExternalStreamPlayer()
            _uiState.value = _uiState.value.copy(isRadioPlaying = false)
            return
        }
        _uiState.value = _uiState.value.copy(
            activeRadioStation = station,
            isRadioPlaying = true,
            isSpotifyPlaying = false,
            isAppleMusicPlaying = false
        )
        playStreamUrl(station.streamUrl)
    }

    fun searchRadioStations(query: String) {
        viewModelScope.launch {
            val results = internetRadioService.getStations(query = query)
            _uiState.value = _uiState.value.copy(radioStations = results)
        }
    }

    fun filterRadioGenre(genre: String) {
        viewModelScope.launch {
            val results = internetRadioService.getStations(genreFilter = genre)
            _uiState.value = _uiState.value.copy(radioStations = results)
        }
    }

    fun toggleRadioFavorite(stationUuid: String) {
        val updated = _uiState.value.radioStations.map {
            if (it.stationUuid == stationUuid) it.copy(isFavorite = !it.isFavorite) else it
        }
        _uiState.value = _uiState.value.copy(radioStations = updated)
    }

    fun playSpotifyTrack(track: com.example.model.SpotifyTrackItem) {
        if (_uiState.value.isSpotifyPlaying && _uiState.value.activeSpotifyTrack?.id == track.id) {
            stopExternalStreamPlayer()
            _uiState.value = _uiState.value.copy(isSpotifyPlaying = false)
            return
        }
        _uiState.value = _uiState.value.copy(
            activeSpotifyTrack = track,
            isSpotifyPlaying = true,
            isRadioPlaying = false,
            isAppleMusicPlaying = false
        )
        val url = track.previewUrl
        if (!url.isNullOrBlank()) {
            playStreamUrl(url)
        }
    }

    fun searchSpotifyTracks(query: String) {
        viewModelScope.launch {
            val results = spotifyApiService.searchTracks(query)
            _uiState.value = _uiState.value.copy(spotifyTracks = results)
        }
    }

    fun playAppleMusicTrack(track: com.example.model.AppleMusicTrackItem) {
        if (_uiState.value.isAppleMusicPlaying && _uiState.value.activeAppleMusicTrack?.id == track.id) {
            stopExternalStreamPlayer()
            _uiState.value = _uiState.value.copy(isAppleMusicPlaying = false)
            return
        }
        _uiState.value = _uiState.value.copy(
            activeAppleMusicTrack = track,
            isAppleMusicPlaying = true,
            isRadioPlaying = false,
            isSpotifyPlaying = false
        )
        val url = track.previewUrl
        if (!url.isNullOrBlank()) {
            playStreamUrl(url)
        }
    }

    fun searchAppleMusicTracks(query: String) {
        viewModelScope.launch {
            val results = appleMusicApiService.searchTracks(query)
            _uiState.value = _uiState.value.copy(appleMusicTracks = results)
        }
    }

    private fun playStreamUrl(url: String) {
        stopExternalStreamPlayer()
        try {
            val player = android.media.MediaPlayer().apply {
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { it.start() }
                prepareAsync()
            }
            streamPlayer = player
        } catch (e: Exception) {
            android.util.Log.w("MusicPlayerViewModel", "Error starting stream player: ${e.message}")
        }
    }

    private fun stopExternalStreamPlayer() {
        try {
            streamPlayer?.stop()
            streamPlayer?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            streamPlayer = null
        }
    }

    // --- AI ACOUSTIC AUTO-TUNER & ENVIRONMENT COMPENSATOR ---

    fun toggleAiListening(enable: Boolean) {
        if (enable) {
            environmentAnalyzer.startListening(viewModelScope)
            _uiState.value = _uiState.value.copy(
                isAiAcousticTuningActive = true,
                aiCalibrationState = "Listening to environment..."
            )
        } else {
            environmentAnalyzer.stopListening()
            _uiState.value = _uiState.value.copy(
                isAiListeningMic = false,
                aiCalibrationState = "Mic Paused"
            )
        }
    }

    fun runAiAcousticCalibration() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCalibratingNow = true,
                isAiAcousticTuningActive = true,
                aiCalibrationState = "Sampling Ambient Acoustics..."
            )

            // Start listening if not already
            environmentAnalyzer.startListening(viewModelScope)
            kotlinx.coroutines.delay(1200L) // Sample ambient profile

            _uiState.value = _uiState.value.copy(aiCalibrationState = "Gemini AI Computing Optimal EQ...")

            val sample = environmentAnalyzer.acousticSample.value
            val result = aiTunerService.tuneEqualizerForEnvironment(
                sample = sample,
                trackTitle = _uiState.value.currentTrack.title,
                genre = _uiState.value.currentTrack.genre
            )

            // Apply AI tuned parameters
            _uiState.value = _uiState.value.copy(
                equalizerBands = result.equalizerBands,
                bassBoost = result.bassBoost,
                virtualizer = result.virtualizer,
                isLoudnessEnabled = result.loudnessEnabled,
                subwooferLevelDb = result.subwooferDb,
                activePreset = "AI ${sample.estimatedEnvironment}",
                aiExplanation = result.explanation,
                aiLastTunedSource = result.source,
                isCalibratingNow = false,
                aiCalibrationState = "Acoustics Calibrated (${result.source})"
            )

            // Update DSP engine
            audioEngine.updateEqualizer(result.equalizerBands)
            audioEngine.updateDsp(
                result.bassBoost,
                result.virtualizer,
                _uiState.value.reverbPreset,
                _uiState.value.volumeNormalization
            )
            audioEngine.updateCarStereoSettings(
                _uiState.value.carBalanceX,
                _uiState.value.carFaderY,
                result.subwooferDb.toFloat(),
                _uiState.value.subwooferCrossoverHz,
                result.loudnessEnabled
            )
        }
    }

    fun toggleLiveAdaptiveAi(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isLiveAdaptiveAiEnabled = enabled)
        if (enabled) {
            environmentAnalyzer.startListening(viewModelScope)
            liveAdaptiveJob?.cancel()
            liveAdaptiveJob = viewModelScope.launch {
                var lastDb = _uiState.value.ambientSplDb
                while (isActive) {
                    kotlinx.coroutines.delay(10000L) // Re-evaluate every 10 seconds
                    val currentSpl = _uiState.value.ambientSplDb
                    // If noise changed by more than 4 dB, re-tune smoothly
                    if (kotlin.math.abs(currentSpl - lastDb) >= 4f) {
                        lastDb = currentSpl
                        val sample = environmentAnalyzer.acousticSample.value
                        val result = aiTunerService.tuneEqualizerForEnvironment(
                            sample = sample,
                            trackTitle = _uiState.value.currentTrack.title,
                            genre = _uiState.value.currentTrack.genre
                        )
                        _uiState.value = _uiState.value.copy(
                            equalizerBands = result.equalizerBands,
                            bassBoost = result.bassBoost,
                            isLoudnessEnabled = result.loudnessEnabled,
                            aiExplanation = result.explanation,
                            aiLastTunedSource = result.source,
                            aiCalibrationState = "Live Adapted to ${currentSpl.toInt()} dB SPL"
                        )
                        audioEngine.updateEqualizer(result.equalizerBands)
                        audioEngine.updateDsp(
                            result.bassBoost,
                            result.virtualizer,
                            _uiState.value.reverbPreset,
                            _uiState.value.volumeNormalization
                        )
                    }
                }
            }
        } else {
            liveAdaptiveJob?.cancel()
            liveAdaptiveJob = null
        }
    }

    fun setSimulatedAcousticEnvironment(presetName: String) {
        environmentAnalyzer.setSimulatedPreset(presetName)
        runAiAcousticCalibration()
    }

    fun revertAiToFlat() {
        val flatBands = listOf(0, 0, 0, 0, 0)
        _uiState.value = _uiState.value.copy(
            equalizerBands = flatBands,
            activePreset = "Flat",
            isAiAcousticTuningActive = false,
            isLiveAdaptiveAiEnabled = false,
            aiCalibrationState = "Reset to Flat Reference"
        )
        liveAdaptiveJob?.cancel()
        liveAdaptiveJob = null
        audioEngine.updateEqualizer(flatBands)
    }

    fun setNeuralPromptInput(text: String) {
        _uiState.value = _uiState.value.copy(neuralPromptInput = text)
    }

    fun toggleFluidAura() {
        _uiState.value = _uiState.value.copy(isFluidAuraActive = !_uiState.value.isFluidAuraActive)
    }

    fun toggleCyberneticHud() {
        _uiState.value = _uiState.value.copy(cyberneticHudEnabled = !_uiState.value.cyberneticHudEnabled)
    }

    fun setSpatial360(level: Int) {
        val clamped = level.coerceIn(0, 100)
        _uiState.value = _uiState.value.copy(spatial360Degree = clamped, virtualizer = clamped * 10)
        audioEngine.updateDsp(
            _uiState.value.bassBoost,
            clamped * 10,
            _uiState.value.reverbPreset,
            _uiState.value.volumeNormalization
        )
    }

    fun setNeuralHarmonicExciter(level: Int) {
        val clamped = level.coerceIn(0, 100)
        _uiState.value = _uiState.value.copy(neuralHarmonicExciter = clamped, crystalizer = clamped)
    }

    fun executeNeuralSonicDirective(promptText: String) {
        val prompt = promptText.ifBlank { _uiState.value.neuralPromptInput.ifBlank { "Deep Cybernetic Spatial Bass" } }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isNeuralProcessing = true,
                isAiAcousticTuningActive = true,
                lastNeuralCommand = prompt,
                aiCalibrationState = "Neural Core Synthesizing: '$prompt'..."
            )
            val sample = environmentAnalyzer.acousticSample.value
            val currentTrack = _uiState.value.currentTrack
            val result = aiTunerService.tuneByNeuralPrompt(
                userPrompt = prompt,
                sample = sample,
                trackTitle = currentTrack.title,
                genre = currentTrack.album
            )

            _uiState.value = _uiState.value.copy(
                isNeuralProcessing = false,
                equalizerBands = result.equalizerBands,
                bassBoost = result.bassBoost,
                virtualizer = result.virtualizer,
                spatial360Degree = (result.virtualizer / 10).coerceIn(0, 100),
                isLoudnessEnabled = result.loudnessEnabled,
                subwooferLevelDb = result.subwooferDb,
                aiExplanation = result.explanation,
                aiLastTunedSource = result.source,
                aiConfidencePercent = (95..99).random(),
                psychoacousticMaskingPercent = (35..52).random(),
                aiCalibrationState = "Neural Lock: Active (${result.source})"
            )

            audioEngine.updateEqualizer(result.equalizerBands)
            audioEngine.updateDsp(
                result.bassBoost,
                result.virtualizer,
                _uiState.value.reverbPreset,
                _uiState.value.volumeNormalization
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        liveAdaptiveJob?.cancel()
        environmentAnalyzer.stopListening()
        voiceWelcomeService.shutdown()
        liveScannerPlayer.release()
        stopExternalStreamPlayer()
        audioEngine.release()
    }
}

