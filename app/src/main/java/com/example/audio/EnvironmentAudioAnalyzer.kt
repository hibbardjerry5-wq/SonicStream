package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

data class AcousticEnvironmentSample(
    val splDb: Float = 45f,
    val lowRumbleRatio: Float = 0.2f,     // 30 - 200 Hz
    val lowMidRatio: Float = 0.2f,        // 200 - 800 Hz
    val midVocalRatio: Float = 0.3f,       // 800 - 2500 Hz
    val highMidRatio: Float = 0.2f,       // 2500 - 6000 Hz
    val trebleRatio: Float = 0.1f,        // 6000 - 16000 Hz
    val estimatedEnvironment: String = "Quiet Room",
    val isMicrophoneActive: Boolean = false
)

class EnvironmentAudioAnalyzer(private val context: Context) {

    private val _acousticSample = MutableStateFlow(AcousticEnvironmentSample())
    val acousticSample: StateFlow<AcousticEnvironmentSample> = _acousticSample.asStateFlow()

    private var recordJob: Job? = null
    private var isAnalyzing = false

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startListening(coroutineScope: CoroutineScope) {
        if (isAnalyzing) return
        isAnalyzing = true

        if (hasRecordPermission()) {
            recordJob = coroutineScope.launch(Dispatchers.Default) {
                val sampleRate = 16000
                val channelConfig = AudioFormat.CHANNEL_IN_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                val bufferSize = max(minBufferSize, 2048)

                var audioRecord: AudioRecord? = null
                try {
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        bufferSize
                    )

                    if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                        audioRecord.startRecording()
                        val audioBuffer = ShortArray(bufferSize)

                        while (isActive && isAnalyzing) {
                            val readBytes = audioRecord.read(audioBuffer, 0, audioBuffer.size)
                            if (readBytes > 0) {
                                // Calculate RMS amplitude
                                var sumSquares = 0.0
                                for (i in 0 until readBytes) {
                                    val sample = audioBuffer[i]
                                    sumSquares += sample * sample
                                }
                                val rms = sqrt(sumSquares / readBytes)
                                // Convert to approximate sound pressure level (dB SPL)
                                val db = if (rms > 0) {
                                    (20.0 * log10(rms / 32767.0) + 95.0).toFloat().coerceIn(30f, 105f)
                                } else 35f

                                // Estimate energy bands from zero crossings and spectral peaks
                                var zeroCrossings = 0
                                for (i in 1 until readBytes) {
                                    if ((audioBuffer[i - 1] >= 0 && audioBuffer[i] < 0) ||
                                        (audioBuffer[i - 1] < 0 && audioBuffer[i] >= 0)) {
                                        zeroCrossings++
                                    }
                                }
                                val zcr = zeroCrossings.toFloat() / readBytes

                                // Distribute acoustic energy based on ZCR and noise amplitude
                                val lowRumble = if (zcr < 0.12f) 0.55f else 0.25f
                                val midVocal = if (zcr in 0.12f..0.35f) 0.45f else 0.25f
                                val highTreble = if (zcr > 0.35f) 0.40f else 0.15f
                                val lowMid = (1f - (lowRumble + midVocal + highTreble) * 0.5f).coerceIn(0.1f, 0.4f)
                                val highMid = (1f - (lowRumble + lowMid + midVocal + highTreble)).coerceIn(0.1f, 0.4f)

                                val envName = when {
                                    db > 75f && lowRumble > 0.4f -> "Vehicle / Highway Noise"
                                    db > 68f && midVocal > 0.35f -> "Crowded Public Space"
                                    db > 78f -> "Loud City Transit"
                                    db < 45f -> "Quiet Home Studio"
                                    else -> "Normal Room Acoustics"
                                }

                                _acousticSample.value = AcousticEnvironmentSample(
                                    splDb = (_acousticSample.value.splDb * 0.7f + db * 0.3f),
                                    lowRumbleRatio = lowRumble,
                                    lowMidRatio = lowMid,
                                    midVocalRatio = midVocal,
                                    highMidRatio = highMid,
                                    trebleRatio = highTreble,
                                    estimatedEnvironment = envName,
                                    isMicrophoneActive = true
                                )
                            }
                            delay(120L)
                        }
                    } else {
                        // Fallback to simulated ambient monitoring
                        runSimulatedAmbientLoop()
                    }
                } catch (e: Exception) {
                    runSimulatedAmbientLoop()
                } finally {
                    try {
                        audioRecord?.stop()
                        audioRecord?.release()
                    } catch (_: Exception) {}
                }
            }
        } else {
            // Permission not yet granted, start simulated ambient telemetry
            recordJob = coroutineScope.launch {
                runSimulatedAmbientLoop()
            }
        }
    }

    private suspend fun runSimulatedAmbientLoop() {
        var angle = 0f
        while (isAnalyzing) {
            angle += 0.15f
            val baseDb = _acousticSample.value.splDb
            val fluctuatedDb = (baseDb + sin(angle) * 2.5f).coerceIn(35f, 95f)

            _acousticSample.value = _acousticSample.value.copy(
                splDb = fluctuatedDb,
                isMicrophoneActive = false
            )
            delay(150L)
        }
    }

    fun stopListening() {
        isAnalyzing = false
        recordJob?.cancel()
        recordJob = null
        _acousticSample.value = _acousticSample.value.copy(isMicrophoneActive = false)
    }

    fun setSimulatedPreset(presetName: String) {
        val vals = when (presetName) {
            "Highway Cabin Rumble" -> floatArrayOf(74f, 0.65f, 0.45f, 0.20f, 0.15f, 0.10f)
            "Crowded Cafe Chatter" -> floatArrayOf(68f, 0.25f, 0.30f, 0.60f, 0.35f, 0.20f)
            "Subway Train Commute" -> floatArrayOf(82f, 0.70f, 0.50f, 0.40f, 0.30f, 0.25f)
            "Echoey Tile Room" -> floatArrayOf(58f, 0.20f, 0.35f, 0.45f, 0.65f, 0.50f)
            "Quiet Night Studio" -> floatArrayOf(36f, 0.15f, 0.15f, 0.15f, 0.15f, 0.10f)
            else -> floatArrayOf(50f, 0.25f, 0.25f, 0.25f, 0.25f, 0.20f)
        }

        _acousticSample.value = AcousticEnvironmentSample(
            splDb = vals[0],
            lowRumbleRatio = vals[1],
            lowMidRatio = vals[2],
            midVocalRatio = vals[3],
            highMidRatio = vals[4],
            trebleRatio = vals[5],
            estimatedEnvironment = presetName,
            isMicrophoneActive = false
        )
    }
}
