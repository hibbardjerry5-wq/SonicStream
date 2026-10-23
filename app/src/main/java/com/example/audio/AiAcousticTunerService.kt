package com.example.audio

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class AiAcousticTuningResult(
    val equalizerBands: List<Int>,
    val bassBoost: Int,
    val virtualizer: Int,
    val loudnessEnabled: Boolean,
    val subwooferDb: Int,
    val explanation: String,
    val source: String // "Gemini 3.5 Flash" or "Acoustic DSP Solver"
)

class AiAcousticTunerService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun tuneEqualizerForEnvironment(
        sample: AcousticEnvironmentSample,
        trackTitle: String,
        genre: String
    ): AiAcousticTuningResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        // If API key is present and not a default placeholder, call Gemini 3.5 Flash
        if (!apiKey.isNullOrBlank() && !apiKey.contains("MY_GEMINI_API_KEY")) {
            try {
                return@withContext callGeminiAcousticTuner(apiKey, sample, trackTitle, genre)
            } catch (e: Exception) {
                // Graceful fallback to acoustic DSP solver on network/rate-limit error
                return@withContext solveOfflineAcoustics(sample, genre, "Gemini offline fallback: ${e.localizedMessage ?: "Network error"}")
            }
        }

        // Offline acoustic DSP solver
        return@withContext solveOfflineAcoustics(sample, genre, null)
    }

    suspend fun tuneByNeuralPrompt(
        userPrompt: String,
        sample: AcousticEnvironmentSample,
        trackTitle: String,
        genre: String
    ): AiAcousticTuningResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (!apiKey.isNullOrBlank() && !apiKey.contains("MY_GEMINI_API_KEY")) {
            try {
                return@withContext callGeminiNeuralPromptTuner(apiKey, userPrompt, sample, trackTitle, genre)
            } catch (e: Exception) {
                return@withContext solvePromptOffline(userPrompt, sample, genre, "Gemini fallback: ${e.localizedMessage ?: "Network error"}")
            }
        }

        return@withContext solvePromptOffline(userPrompt, sample, genre, null)
    }

    private fun callGeminiAcousticTuner(
        apiKey: String,
        sample: AcousticEnvironmentSample,
        trackTitle: String,
        genre: String
    ): AiAcousticTuningResult {
        val model = "gemini-3.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val prompt = """
            You are an acoustic sound mastering engineer. Analyze this real-time ambient environment audio measurement and music track:
            - Ambient Noise Floor: ${sample.splDb.roundToInt()} dB SPL
            - Low Frequency Rumble (30-200Hz): ${(sample.lowRumbleRatio * 100).roundToInt()}%
            - Low-Mid Energy (200-800Hz): ${(sample.lowMidRatio * 100).roundToInt()}%
            - Vocal/Mid Clutter (800-2500Hz): ${(sample.midVocalRatio * 100).roundToInt()}%
            - High-Mid Energy (2500-6000Hz): ${(sample.highMidRatio * 100).roundToInt()}%
            - Treble Air (6000-16000Hz): ${(sample.trebleRatio * 100).roundToInt()}%
            - Detected Environment Type: ${sample.estimatedEnvironment}
            - Current Music: "$trackTitle" (Genre: $genre)

            Compute optimal acoustic compensation settings to neutralize environmental noise masking and acoustic absorption:
            - 5 EQ band gains in dB (-10 to +10) for [60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz]
            - Bass Boost (0 to 1000)
            - 3D Virtualizer (0 to 1000)
            - Loudness compensation (true/false)
            - Subwoofer level (0 to 12 dB)
            - A concise 2-sentence rationale of how the EQ counteracts the environment noise.

            Return ONLY a valid JSON object matching this schema:
            {
              "equalizerBands": [int, int, int, int, int],
              "bassBoost": int,
              "virtualizer": int,
              "loudnessEnabled": boolean,
              "subwooferDb": int,
              "explanation": string
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Gemini API error code: ${response.code}")
            }
            val responseText = response.body?.string() ?: throw IllegalStateException("Empty response")
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates") ?: throw IllegalStateException("No candidates returned")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")

            val parsedJson = JSONObject(text.trim())
            val bandsJson = parsedJson.getJSONArray("equalizerBands")
            val bands = mutableListOf<Int>()
            for (i in 0 until minOf(5, bandsJson.length())) {
                bands.add(bandsJson.getInt(i).coerceIn(-10, 10))
            }
            while (bands.size < 5) bands.add(0)

            val bass = parsedJson.optInt("bassBoost", 400).coerceIn(0, 1000)
            val virt = parsedJson.optInt("virtualizer", 350).coerceIn(0, 1000)
            val loud = parsedJson.optBoolean("loudnessEnabled", sample.splDb > 65f)
            val sub = parsedJson.optInt("subwooferDb", 4).coerceIn(0, 12)
            val exp = parsedJson.optString("explanation", "Compensated for ambient noise and room absorption.")

            return AiAcousticTuningResult(
                equalizerBands = bands,
                bassBoost = bass,
                virtualizer = virt,
                loudnessEnabled = loud,
                subwooferDb = sub,
                explanation = exp,
                source = "Gemini 3.5 Flash"
            )
        }
    }

    fun solveOfflineAcoustics(
        sample: AcousticEnvironmentSample,
        genre: String,
        fallbackNote: String?
    ): AiAcousticTuningResult {
        val spl = sample.splDb
        val lowRumble = sample.lowRumbleRatio
        val midVocal = sample.midVocalRatio
        val highTreble = sample.trebleRatio

        // Acoustic Masking Compensation logic:
        // 1. If low rumble is high (e.g. road/engine), boost 60Hz and 230Hz to restore punch
        val b60 = if (lowRumble > 0.4f || spl > 70f) ((spl - 55f) / 4.5f).roundToInt().coerceIn(2, 8) else 1
        val b230 = if (lowRumble > 0.35f) (b60 * 0.65f).roundToInt().coerceIn(1, 6) else 0

        // 2. If vocal chatter is high (busy cafe/office), slightly notch mid-range so vocals in music don't clash, or boost clarity
        val b910 = if (midVocal > 0.4f) -2 else if (spl < 50f) 1 else 0
        val b3600 = if (midVocal > 0.35f) 2 else if (spl > 70f) 3 else 1

        // 3. High air compensation for absorption over distance or glass reflections
        val b14k = if (highTreble > 0.3f) -1 else 4

        val bands = listOf(b60, b230, b910, b3600, b14k)
        val bassBoost = if (spl > 65f) (400 + (spl - 65f) * 15f).roundToInt().coerceIn(300, 850) else 350
        val virtualizer = if (sample.estimatedEnvironment.contains("Room") || sample.estimatedEnvironment.contains("Studio")) 250 else 550
        val loudness = spl > 60f
        val subwoofer = if (lowRumble > 0.4f) 6 else 3

        val explanation = when {
            spl > 72f -> "Boosted 60Hz (+${b60}dB) & 230Hz (+${b230}dB) with Loudness to overcome ${spl.roundToInt()}dB low-frequency road/transit rumble. Lifted 3.6kHz (+${b3600}dB) for vocal intelligibility."
            midVocal > 0.35f -> "Applied acoustic masking filter: cut 910Hz (${b910}dB) to avoid speech clash from room chatter, and brightened highs (+${b14k}dB) for crisp presence."
            spl < 45f -> "Ultra-quiet environment detected (${spl.roundToInt()}dB SPL). Configured balanced reference curve with low virtualizer to preserve pristine studio dynamic range."
            else -> "Ambient noise measured at ${spl.roundToInt()}dB SPL. Balanced 5-band curve applied to preserve tonal warmth in ${sample.estimatedEnvironment}."
        }

        return AiAcousticTuningResult(
            equalizerBands = bands,
            bassBoost = bassBoost,
            virtualizer = virtualizer,
            loudnessEnabled = loudness,
            subwooferDb = subwoofer,
            explanation = if (fallbackNote != null) "$explanation ($fallbackNote)" else explanation,
            source = "Acoustic DSP Engine"
        )
    }

    private fun callGeminiNeuralPromptTuner(
        apiKey: String,
        userPrompt: String,
        sample: AcousticEnvironmentSample,
        trackTitle: String,
        genre: String
    ): AiAcousticTuningResult {
        val model = "gemini-3.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val prompt = """
            You are a futuristic Cybernetic Neural Audio Engine AI. The user has given this sonic tuning command:
            "$userPrompt"

            Current Audio Telemetry & Environment:
            - Track: "$trackTitle" (Genre: $genre)
            - Ambient Room Noise: ${sample.splDb.roundToInt()} dB SPL (${sample.estimatedEnvironment})
            - Low Rumble Ratio: ${(sample.lowRumbleRatio * 100).roundToInt()}%
            - High Treble Ratio: ${(sample.trebleRatio * 100).roundToInt()}%

            Translate the user's creative prompt and physical acoustics into precise master-grade DSP parameters:
            - equalizerBands: 5 integer gains in dB (-10 to +10) for [60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz]
            - bassBoost: integer (0 to 1000)
            - virtualizer: 3D spatializer (0 to 1000)
            - loudnessEnabled: boolean
            - subwooferDb: integer (0 to 12)
            - explanation: A concise, futuristic 2-sentence explanation of how the neural acoustic engine sculpted the soundstage to fulfill their request.

            Return ONLY valid JSON matching this schema:
            {
              "equalizerBands": [int, int, int, int, int],
              "bassBoost": int,
              "virtualizer": int,
              "loudnessEnabled": boolean,
              "subwooferDb": int,
              "explanation": string
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.35)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Gemini API error code: ${response.code}")
            }
            val responseText = response.body?.string() ?: throw IllegalStateException("Empty response")
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates") ?: throw IllegalStateException("No candidates returned")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")

            val parsedJson = JSONObject(text.trim())
            val bandsJson = parsedJson.getJSONArray("equalizerBands")
            val bands = mutableListOf<Int>()
            for (i in 0 until minOf(5, bandsJson.length())) {
                bands.add(bandsJson.getInt(i).coerceIn(-10, 10))
            }
            while (bands.size < 5) bands.add(0)

            val bass = parsedJson.optInt("bassBoost", 500).coerceIn(0, 1000)
            val virt = parsedJson.optInt("virtualizer", 450).coerceIn(0, 1000)
            val loud = parsedJson.optBoolean("loudnessEnabled", true)
            val sub = parsedJson.optInt("subwooferDb", 6).coerceIn(0, 12)
            val exp = parsedJson.optString("explanation", "Neural audio engine calibrated soundstage to prompt '$userPrompt'.")

            return AiAcousticTuningResult(
                equalizerBands = bands,
                bassBoost = bass,
                virtualizer = virt,
                loudnessEnabled = loud,
                subwooferDb = sub,
                explanation = exp,
                source = "Gemini 3.5 Neural Engine"
            )
        }
    }

    private fun solvePromptOffline(
        userPrompt: String,
        sample: AcousticEnvironmentSample,
        genre: String,
        fallbackNote: String?
    ): AiAcousticTuningResult {
        val lower = userPrompt.lowercase()
        val bands = when {
            lower.contains("sub") || lower.contains("bass") || lower.contains("club") || lower.contains("heavy") || lower.contains("808") ->
                listOf(8, 4, -1, 1, 3)
            lower.contains("spatial") || lower.contains("360") || lower.contains("hologram") || lower.contains("space") || lower.contains("surround") ->
                listOf(3, 1, 0, 4, 7)
            lower.contains("vocal") || lower.contains("clarity") || lower.contains("podcast") || lower.contains("speech") ->
                listOf(-2, 0, 2, 6, 2)
            lower.contains("warm") || lower.contains("vinyl") || lower.contains("jazz") || lower.contains("acoustic") ->
                listOf(2, 4, 2, -1, -2)
            lower.contains("cyber") || lower.contains("highway") || lower.contains("gt") || lower.contains("drive") || lower.contains("laser") ->
                listOf(6, 2, -1, 3, 6)
            else -> listOf(4, 2, 0, 2, 4)
        }

        val bass = when {
            lower.contains("bass") || lower.contains("sub") || lower.contains("club") -> 850
            lower.contains("vocal") || lower.contains("acoustic") -> 200
            else -> 550
        }

        val virt = when {
            lower.contains("spatial") || lower.contains("360") || lower.contains("hologram") || lower.contains("space") -> 850
            lower.contains("vocal") -> 250
            else -> 500
        }

        val sub = when {
            lower.contains("sub") || lower.contains("bass") -> 9
            lower.contains("vocal") -> 2
            else -> 6
        }

        val exp = "Neural synthesis applied for \"$userPrompt\": adjusted 5-band curve to [${bands.joinToString { if (it > 0) "+$it" else "$it" }} dB] with ${bass / 10}% sub-boost & ${virt / 10}% 3D spatial field."

        return AiAcousticTuningResult(
            equalizerBands = bands,
            bassBoost = bass,
            virtualizer = virt,
            loudnessEnabled = true,
            subwooferDb = sub,
            explanation = if (fallbackNote != null) "$exp ($fallbackNote)" else exp,
            source = "Neural Acoustic DSP Solver"
        )
    }
}
