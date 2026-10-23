package com.example.data.remote

import android.util.Log
import com.example.model.AppleMusicTrackItem
import com.example.model.CuratedRadioStations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Apple Music Search & Streaming Preview Service.
 * Queries Apple Music catalog via the iTunes Search API for real tracks,
 * high-fidelity AAC preview streams, and deep-linking to the Apple Music app.
 */
class AppleMusicApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun searchTracks(query: String): List<AppleMusicTrackItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext CuratedRadioStations.sampleAppleMusicTracks
        }

        try {
            val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encoded&media=music&entity=song&limit=20"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Resonance-Cyber-Music/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext filterCurated(query)
            }

            val body = response.body?.string() ?: return@withContext filterCurated(query)
            val json = JSONObject(body)
            val resultsArray = json.optJSONArray("results") ?: return@withContext filterCurated(query)
            val tracks = mutableListOf<AppleMusicTrackItem>()

            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.getJSONObject(i)
                val trackId = item.optLong("trackId", System.currentTimeMillis()).toString()
                val trackName = item.optString("trackName", "Apple Music Track")
                val artistName = item.optString("artistName", "Unknown Artist")
                val collectionName = item.optString("collectionName", "Single")
                val previewUrl = item.optString("previewUrl", "")
                val artworkUrl = item.optString("artworkUrl100", "").replace("100x100bb", "600x600bb")
                val trackViewUrl = item.optString("trackViewUrl", "https://music.apple.com")
                val durationMs = item.optLong("trackTimeMillis", 210000L)

                tracks.add(
                    AppleMusicTrackItem(
                        id = "am_$trackId",
                        name = trackName,
                        artist = artistName,
                        album = collectionName,
                        durationMs = durationMs,
                        previewUrl = previewUrl.ifBlank { null },
                        albumArtUrl = artworkUrl.ifBlank { null },
                        appleMusicUrl = trackViewUrl,
                        isSpatialAudio = true,
                        isLossless = true
                    )
                )
            }

            if (tracks.isNotEmpty()) tracks else filterCurated(query)
        } catch (e: Exception) {
            Log.w("AppleMusicApiService", "Apple Music search error: ${e.message}")
            filterCurated(query)
        }
    }

    private fun filterCurated(query: String): List<AppleMusicTrackItem> {
        val q = query.lowercase()
        return CuratedRadioStations.sampleAppleMusicTracks.filter {
            it.name.lowercase().contains(q) || it.artist.lowercase().contains(q) || it.album.lowercase().contains(q)
        }
    }
}
