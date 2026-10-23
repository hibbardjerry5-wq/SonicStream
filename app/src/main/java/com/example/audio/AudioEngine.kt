package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.model.MusicTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

data class VisualizerFrame(
    val frequencyBands: FloatArray = FloatArray(32),
    val waveform: FloatArray = FloatArray(64),
    val beatEnergy: Float = 0f,
    val isBeat: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VisualizerFrame
        return frequencyBands.contentEquals(other.frequencyBands) &&
                waveform.contentEquals(other.waveform) &&
                beatEnergy == other.beatEnergy &&
                isBeat == other.isBeat
    }

    override fun hashCode(): Int {
        var result = frequencyBands.contentHashCode()
        result = 31 * result + waveform.contentHashCode()
        result = 31 * result + beatEnergy.hashCode()
        result = 31 * result + isBeat.hashCode()
        return result
    }
}

class AudioEngine(private val scope: CoroutineScope) {

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(sampleRate / 4)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _visualizerFrame = MutableStateFlow(VisualizerFrame())
    val visualizerFrame: StateFlow<VisualizerFrame> = _visualizerFrame.asStateFlow()

    // 5-band Equalizer gains (-10 to +10 dB)
    @Volatile var eqBand60Hz: Float = 0f
    @Volatile var eqBand230Hz: Float = 0f
    @Volatile var eqBand910Hz: Float = 0f
    @Volatile var eqBand3600Hz: Float = 0f
    @Volatile var eqBand14000Hz: Float = 0f

    @Volatile var bassBoostStrength: Float = 0f // 0f .. 1f
    @Volatile var virtualizerStrength: Float = 0f // 0f .. 1f
    @Volatile var reverbPresetName: String = "None"
    @Volatile var volumeNormalization: Boolean = false
    @Volatile var playbackSpeed: Float = 1.0f

    // Car Stereo Speaker Controls
    @Volatile var carBalanceX: Float = 0f // -1f (Left) to +1f (Right)
    @Volatile var carFaderY: Float = 0f // -1f (Rear) to +1f (Front)
    @Volatile var carSubwooferDb: Float = 4f // 0 to 12 dB
    @Volatile var carSubwooferCrossover: Int = 80 // 50, 80, 120 Hz
    @Volatile var carLoudnessEnabled: Boolean = true // Road noise compensation

    // Police / 911 Public Safety Scanner Dual-Audio & Auto-Ducking
    @Volatile var isScannerEnabled: Boolean = false
    @Volatile private var isScannerTransmitting: Boolean = false
    @Volatile var scannerVolume: Float = 0.85f // 0f .. 1f
    @Volatile var duckingReductionPercent: Int = 75 // Ducks music by 75% during transmissions
    @Volatile var musicMasterVolume: Float = 1.0f // 0f .. 1f independent mixer volume

    private var currentTrack: MusicTrack? = null
    private var trackDurationMs: Long = 180000L

    init {
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error initializing AudioTrack", e)
        }
    }

    fun playTrack(track: MusicTrack, startPositionMs: Long = 0L) {
        currentTrack = track
        trackDurationMs = track.durationMs
        _currentPositionMs.value = startPositionMs
        startPlaybackLoop()
    }

    fun resume() {
        if (currentTrack != null) {
            startPlaybackLoop()
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
        } catch (e: Exception) {
            Log.w("AudioEngine", "AudioTrack pause error: ${e.message}")
        }
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs.coerceIn(0L, trackDurationMs)
    }

    fun updateEqualizer(bands: List<Int>) {
        if (bands.size >= 5) {
            eqBand60Hz = bands[0].toFloat()
            eqBand230Hz = bands[1].toFloat()
            eqBand910Hz = bands[2].toFloat()
            eqBand3600Hz = bands[3].toFloat()
            eqBand14000Hz = bands[4].toFloat()
        }
    }

    fun updateDsp(bassBoost: Int, virtualizer: Int, reverb: String, normalize: Boolean) {
        bassBoostStrength = (bassBoost / 1000f).coerceIn(0f, 1f)
        virtualizerStrength = (virtualizer / 1000f).coerceIn(0f, 1f)
        reverbPresetName = reverb
        volumeNormalization = normalize
    }

    fun updateCarStereoSettings(
        balanceX: Float,
        faderY: Float,
        subwooferDb: Float,
        crossoverHz: Int,
        loudness: Boolean
    ) {
        carBalanceX = balanceX.coerceIn(-1f, 1f)
        carFaderY = faderY.coerceIn(-1f, 1f)
        carSubwooferDb = subwooferDb.coerceIn(0f, 12f)
        carSubwooferCrossover = crossoverHz
        carLoudnessEnabled = loudness
    }

    fun setScannerSettings(enabled: Boolean, volume: Float, duckingPercent: Int) {
        isScannerEnabled = enabled
        scannerVolume = volume.coerceIn(0f, 1f)
        duckingReductionPercent = duckingPercent.coerceIn(0, 100)
    }

    fun setMusicMasterVolume(volume: Float) {
        musicMasterVolume = volume.coerceIn(0f, 1f)
    }

    fun setScannerTransmitting(transmitting: Boolean) {
        isScannerTransmitting = transmitting
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        _isPlaying.value = true

        try {
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e("AudioEngine", "AudioTrack play error", e)
        }

        playbackJob = scope.launch(Dispatchers.Default) {
            val shortBufferSize = 1024
            val audioBuffer = ShortArray(shortBufferSize * 2) // Stereo (L/R)
            val baseFreq = currentTrack?.bassFrequency ?: 65f
            val bpm = currentTrack?.tempoBpm ?: 120
            var phaseCounter = (_currentPositionMs.value * sampleRate / 1000.0)

            val bands = FloatArray(32)
            val wave = FloatArray(64)
            var lastBeatTime = System.currentTimeMillis()
            var currentDuckingGain = 1.0f

            while (isActive && _isPlaying.value) {
                val beatIntervalMs = (60000.0 / bpm).toLong()
                val now = System.currentTimeMillis()
                val isBeat = (now - lastBeatTime) >= beatIntervalMs
                if (isBeat) lastBeatTime = now

                // Calculate dB multiplier for EQ bands: 10^(dB/20)
                var gain60 = Math.pow(10.0, (eqBand60Hz / 20.0)).toFloat() * (1f + bassBoostStrength * 0.8f)
                val gain230 = Math.pow(10.0, (eqBand230Hz / 20.0)).toFloat()
                var gain910 = Math.pow(10.0, (eqBand910Hz / 20.0)).toFloat()
                var gain3600 = Math.pow(10.0, (eqBand3600Hz / 20.0)).toFloat()
                var gain14k = Math.pow(10.0, (eqBand14000Hz / 20.0)).toFloat()

                // Car Loudness Road Noise Compensation
                if (carLoudnessEnabled) {
                    gain60 *= 1.30f
                    gain14k *= 1.25f
                }

                // Car Subwoofer gain
                val subGain = Math.pow(10.0, (carSubwooferDb / 20.0)).toFloat()

                // Fader adjustment (Front enhances vocals/treble; Rear enhances spatial reverb)
                val frontBoost = (1f + (carFaderY * 0.25f)).coerceAtLeast(0.7f)
                gain910 *= frontBoost
                gain3600 *= frontBoost

                // Calculate audio ducking target when police scanner is transmitting
                val targetDucking = if (isScannerEnabled && isScannerTransmitting) {
                    ((100 - duckingReductionPercent) / 100f).coerceIn(0.05f, 1f)
                } else {
                    1.0f
                }
                currentDuckingGain = currentDuckingGain * 0.94f + targetDucking * 0.06f

                // Generate rich synthesizer sound chunk
                for (i in 0 until shortBufferSize) {
                    val t = (phaseCounter + i) / sampleRate.toDouble()
                    
                    // Fundamental bass note
                    val bass = sin(2.0 * PI * baseFreq * t) * 0.40 * gain60
                    // Sub harmonic driven by car subwoofer gain
                    val sub = sin(PI * (baseFreq * (carSubwooferCrossover / 80f)) * t) * (0.35 * subGain * (1f + bassBoostStrength))
                    // Mid chord harmonic
                    val chord = (sin(2.0 * PI * (baseFreq * 2.0) * t) +
                            sin(2.0 * PI * (baseFreq * 2.5) * t) +
                            sin(2.0 * PI * (baseFreq * 3.0) * t)) * 0.15 * gain230
                    // Arp melodic high
                    val arpFreq = baseFreq * (3.0 + ((t * 4.0).toInt() % 5))
                    val arp = sin(2.0 * PI * arpFreq * t) * 0.18 * gain910
                    // High shimmer sparkle
                    val shimmer = sin(2.0 * PI * (baseFreq * 12.0) * t) * 0.08 * gain3600
                    // Noise / rhythm snare pulse
                    val rhythmTick = if ((t * (bpm / 60.0) % 1.0) < 0.08) 0.2 * gain14k else 0.0

                    var mixed = (bass + sub + chord + arp + shimmer + rhythmTick).toFloat()

                    // Optional normalization
                    if (volumeNormalization) {
                        mixed = (mixed * 0.85f).coerceIn(-0.95f, 0.95f)
                    }

                    // Apply audio ducking for dispatch transmissions and master mixer volume
                    mixed *= currentDuckingGain * musicMasterVolume

                    // Synthesize police / emergency scanner audio if transmitting
                    var scannerAudio = 0f
                    if (isScannerEnabled && isScannerTransmitting) {
                        val radioVoice = (sin(2.0 * PI * 750.0 * t) * 0.22 +
                                sin(2.0 * PI * 1200.0 * t) * 0.18 +
                                sin(2.0 * PI * 1900.0 * t) * 0.14).toFloat()
                        val radioStatic = (((sin(t * 88321.0) * 43758.5453) % 1.0).toFloat() - 0.5f) * 0.07f
                        scannerAudio = (radioVoice + radioStatic) * scannerVolume
                    }

                    // Virtualizer stereo widening
                    val stereoOffset = (sin(2.0 * PI * (baseFreq * 1.5) * t) * virtualizerStrength * 0.25).toFloat()
                    
                    // Car stereo Balance L/R weighting
                    val leftMultiplier = (1f - (carBalanceX * 0.85f)).coerceIn(0.1f, 1.9f)
                    val rightMultiplier = (1f + (carBalanceX * 0.85f)).coerceIn(0.1f, 1.9f)

                    val sampleLeft = ((((mixed - stereoOffset) * leftMultiplier) + scannerAudio).coerceIn(-1f, 1f) * 26000).toInt().toShort()
                    val sampleRight = ((((mixed + stereoOffset) * rightMultiplier) + scannerAudio).coerceIn(-1f, 1f) * 26000).toInt().toShort()

                    audioBuffer[i * 2] = sampleLeft
                    audioBuffer[i * 2 + 1] = sampleRight

                    if (i < 64) {
                        wave[i] = mixed + scannerAudio
                    }
                }

                // Write to AudioTrack
                audioTrack?.write(audioBuffer, 0, audioBuffer.size)

                // Update position
                phaseCounter += shortBufferSize
                val elapsedMs = (shortBufferSize * 1000L) / sampleRate
                _currentPositionMs.value += elapsedMs
                if (_currentPositionMs.value >= trackDurationMs) {
                    _currentPositionMs.value = 0L
                }

                // Calculate visualizer FFT simulation
                val energy = (0.2f + (gain60 * 0.3f) + (if (isBeat) 0.5f else 0.05f)).coerceIn(0.1f, 1.0f)
                for (b in 0 until 32) {
                    val bandCenter = b / 31.0f
                    val multiplier = when {
                        b < 8 -> gain60 * (1f + (if (isBeat) 0.6f else 0f))
                        b < 16 -> gain230
                        b < 24 -> gain910
                        else -> gain3600 * gain14k * 0.8f
                    }
                    val targetAmp = ((sin(now / 150.0 + b * 0.4).toFloat() * 0.3f + 0.6f) * multiplier * energy).coerceIn(0.05f, 1.0f)
                    // Smooth decay
                    bands[b] = bands[b] * 0.65f + targetAmp * 0.35f
                }

                _visualizerFrame.value = VisualizerFrame(
                    frequencyBands = bands.copyOf(),
                    waveform = wave.copyOf(),
                    beatEnergy = energy,
                    isBeat = isBeat
                )
            }
        }
    }

    fun release() {
        pause()
        try {
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error releasing AudioTrack", e)
        }
    }
}
