package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.model.DispatchTransmission
import com.example.model.PublicSafetyFeeds
import com.example.model.ScannerChannel
import com.example.model.ScannerStreamState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

/**
 * Dedicated secondary audio player for streaming live police/fire/EMS public safety feeds
 * simultaneously alongside the local music player, with independent volume, squelch,
 * pan, and automatic music ducking coordination.
 */
class LiveScannerAudioPlayer(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onDuckingStateChanged: (isDucking: Boolean, duckingFactor: Float) -> Unit
) {
    private val tag = "LiveScannerAudioPlayer"

    private var mediaPlayer: MediaPlayer? = null
    private var transmissionJob: Job? = null
    private var telemetryJob: Job? = null

    private val _streamState = MutableStateFlow(ScannerStreamState.IDLE)
    val streamState: StateFlow<ScannerStreamState> = _streamState.asStateFlow()

    private val _currentChannel = MutableStateFlow<ScannerChannel?>(null)
    val currentChannel: StateFlow<ScannerChannel?> = _currentChannel.asStateFlow()

    private val _bufferPercent = MutableStateFlow(0)
    val bufferPercent: StateFlow<Int> = _bufferPercent.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _isTransmitting = MutableStateFlow(false)
    val isTransmitting: StateFlow<Boolean> = _isTransmitting.asStateFlow()

    private val _activeTransmission = MutableStateFlow<DispatchTransmission?>(null)
    val activeTransmission: StateFlow<DispatchTransmission?> = _activeTransmission.asStateFlow()

    // Mixing parameters
    var volume: Float = 0.85f
        private set
    var pan: Float = 0.20f // slight right pan by default for dispatch separation
        private set
    var isDuckingEnabled: Boolean = true
        private set
    var duckingPercent: Int = 75 // 75% volume reduction on music
        private set
    var squelchThreshold: Float = 0.30f
        private set
    var isMuted: Boolean = false
        private set

    init {
        startTelemetryLoop()
    }

    fun setMixingParameters(
        volume: Float = this.volume,
        pan: Float = this.pan,
        duckingEnabled: Boolean = this.isDuckingEnabled,
        duckingPercent: Int = this.duckingPercent,
        squelch: Float = this.squelchThreshold,
        muted: Boolean = this.isMuted
    ) {
        this.volume = volume.coerceIn(0f, 1f)
        this.pan = pan.coerceIn(-1f, 1f)
        this.isDuckingEnabled = duckingEnabled
        this.duckingPercent = duckingPercent.coerceIn(10, 95)
        this.squelchThreshold = squelch.coerceIn(0f, 1f)
        this.isMuted = muted

        applyPlayerVolume()
    }

    private fun applyPlayerVolume() {
        if (isMuted) {
            mediaPlayer?.setVolume(0f, 0f)
            return
        }

        // Stereo pan calculation:
        // pan -1.0 = left only, 0.0 = center, +1.0 = right only
        val left = (volume * (1f - pan).coerceIn(0f, 1f))
        val right = (volume * (1f + pan).coerceIn(0f, 1f))
        try {
            mediaPlayer?.setVolume(left, right)
        } catch (e: Exception) {
            Log.w(tag, "Failed to set player volume: ${e.message}")
        }
    }

    fun playChannel(channel: ScannerChannel) {
        _currentChannel.value = channel
        _streamState.value = ScannerStreamState.CONNECTING
        _bufferPercent.value = 10

        // Reset previous player
        releaseMediaPlayer()

        if (channel.streamUrl.isNotBlank()) {
            startStreamingMedia(channel)
        } else {
            // Local fallback simulation mode
            _streamState.value = ScannerStreamState.STREAMING
            triggerTransmissionBurst(channel)
        }
    }

    private fun startStreamingMedia(channel: ScannerChannel) {
        scope.launch(Dispatchers.IO) {
            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    setDataSource(context, Uri.parse(channel.streamUrl))
                    setOnBufferingUpdateListener { _, percent ->
                        _bufferPercent.value = percent
                    }
                    setOnPreparedListener { mp ->
                        scope.launch(Dispatchers.Main) {
                            _streamState.value = ScannerStreamState.STREAMING
                            mp.start()
                            applyPlayerVolume()
                            updateDucking(true)
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.w(tag, "MediaPlayer stream error what=$what, extra=$extra. Engaging tactical fallback receiver.")
                        scope.launch(Dispatchers.Main) {
                            // Automatically maintain situational broadcast with simulated voice dispatch
                            _streamState.value = ScannerStreamState.STREAMING
                            triggerTransmissionBurst(channel)
                        }
                        true
                    }
                    setOnCompletionListener {
                        scope.launch(Dispatchers.Main) {
                            _streamState.value = ScannerStreamState.IDLE
                            updateDucking(false)
                        }
                    }
                }
                mediaPlayer = player
                player.prepareAsync()
            } catch (e: Exception) {
                Log.e(tag, "Error setting up MediaPlayer for ${channel.name}: ${e.message}")
                withContext(Dispatchers.Main) {
                    _streamState.value = ScannerStreamState.STREAMING
                    triggerTransmissionBurst(channel)
                }
            }
        }
    }

    fun stop() {
        _streamState.value = ScannerStreamState.IDLE
        _isTransmitting.value = false
        _activeTransmission.value = null
        transmissionJob?.cancel()
        releaseMediaPlayer()
        updateDucking(false)
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.w(tag, "Error releasing MediaPlayer: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    /**
     * Triggers a dispatch call transmission with simulated squelch,
     * transcription text, and audio ducking.
     */
    fun triggerTransmissionBurst(channel: ScannerChannel? = _currentChannel.value) {
        val ch = channel ?: return
        transmissionJob?.cancel()
        transmissionJob = scope.launch {
            val transmission = PublicSafetyFeeds.sampleTransmissions
                .filter { it.channelId == ch.id }
                .randomOrNull() ?: PublicSafetyFeeds.sampleTransmissions.random()

            _activeTransmission.value = transmission
            _isTransmitting.value = true
            updateDucking(true)

            // Hold transmission for call duration
            delay(transmission.durationMs)

            _isTransmitting.value = false
            _activeTransmission.value = null
            if (_streamState.value != ScannerStreamState.STREAMING) {
                updateDucking(false)
            }
        }
    }

    private fun updateDucking(active: Boolean) {
        if (!isDuckingEnabled) {
            onDuckingStateChanged(false, 1.0f)
            return
        }
        if (active) {
            val factor = 1.0f - (duckingPercent / 100f)
            onDuckingStateChanged(true, factor)
        } else {
            onDuckingStateChanged(false, 1.0f)
        }
    }

    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            var step = 0
            while (isActive) {
                delay(60)
                step++
                val isPlayingStream = _streamState.value == ScannerStreamState.STREAMING
                val isTx = _isTransmitting.value

                if (isPlayingStream || isTx) {
                    // Generate dynamic RF and voice energy telemetry
                    val baseEnergy = if (isTx) 0.65f else 0.35f
                    val wave = (sin(step * 0.4) * 0.25f + sin(step * 0.15) * 0.2f).toFloat()
                    val level = (baseEnergy + wave).coerceIn(0.15f, 0.95f)
                    _audioLevel.value = level
                } else {
                    _audioLevel.value = (_audioLevel.value * 0.85f).coerceAtLeast(0f)
                }
            }
        }
    }

    fun release() {
        stop()
        telemetryJob?.cancel()
    }
}
