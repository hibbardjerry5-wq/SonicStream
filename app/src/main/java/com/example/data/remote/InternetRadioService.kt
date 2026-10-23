package com.example.data.remote

import android.util.Log
import com.example.model.CuratedRadioStations
import com.example.model.RadioStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

/**
 * Internet Radio service connecting to the global Radio Browser directory API
 * (similar to the MyRadio app architecture) with multi-genre search and station streaming.
 */
class InternetRadioService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val userCustomStations = mutableListOf<RadioStation>()

    suspend fun getStations(
        genreFilter: String = "ALL",
        query: String = ""
    ): List<RadioStation> = withContext(Dispatchers.IO) {
        val baseList = (userCustomStations + CuratedRadioStations.stations).distinctBy { it.stationUuid }

        if (query.isNotBlank() && query.length >= 3) {
            // Attempt live online search via Radio Browser API
            val remoteResults = searchRadioBrowserApi(query)
            if (remoteResults.isNotEmpty()) {
                return@withContext remoteResults
            }
        }

        baseList.filter { station ->
            val matchGenre = when (genreFilter.uppercase()) {
                "ALL" -> true
                "CHILLOUT", "AMBIENT" -> station.genre.contains("Chill", ignoreCase = true) || station.genre.contains("Ambient", ignoreCase = true)
                "POP", "TOP 40" -> station.genre.contains("Pop", ignoreCase = true) || station.genre.contains("Top 40", ignoreCase = true)
                "ROCK", "INDIE" -> station.genre.contains("Rock", ignoreCase = true) || station.genre.contains("Indie", ignoreCase = true)
                "ELECTRONIC", "DANCE", "SYNTHWAVE" -> station.genre.contains("Electronic", ignoreCase = true) || station.genre.contains("Synth", ignoreCase = true) || station.genre.contains("House", ignoreCase = true)
                "JAZZ" -> station.genre.contains("Jazz", ignoreCase = true)
                "FAVORITES" -> station.isFavorite
                else -> true
            }

            val matchQuery = if (query.isBlank()) true else {
                val q = query.trim().lowercase()
                station.name.lowercase().contains(q) ||
                        station.genre.lowercase().contains(q) ||
                        station.country.lowercase().contains(q) ||
                        station.tags.any { it.contains(q) }
            }

            matchGenre && matchQuery
        }
    }

    private fun searchRadioBrowserApi(query: String): List<RadioStation> {
        return try {
            val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://de1.api.radio-browser.info/json/stations/byname/$encoded?limit=15"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Resonance-MyRadio-App/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val body = response.body?.string() ?: return emptyList()
            val array = JSONArray(body)
            val stations = mutableListOf<RadioStation>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val streamUrl = obj.optString("url_resolved").ifBlank { obj.optString("url") }
                if (streamUrl.isNotBlank() && (streamUrl.startsWith("http://") || streamUrl.startsWith("https://"))) {
                    stations.add(
                        RadioStation(
                            stationUuid = obj.optString("stationuuid", "rb_$i"),
                            name = obj.optString("name", "Internet Radio"),
                            streamUrl = streamUrl,
                            genre = obj.optString("tags", "General").split(",").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Music",
                            country = obj.optString("country", "Global"),
                            bitrateKbps = obj.optInt("bitrate", 128),
                            codec = obj.optString("codec", "MP3"),
                            faviconUrl = obj.optString("favicon", ""),
                            votes = obj.optInt("votes", 100),
                            tags = obj.optString("tags").split(",").map { it.trim() }
                        )
                    )
                }
            }
            stations
        } catch (e: Exception) {
            Log.w("InternetRadioService", "Radio Browser API search fallback: ${e.message}")
            emptyList()
        }
    }

    fun addCustomStation(name: String, streamUrl: String, genre: String, country: String): RadioStation {
        val station = RadioStation(
            stationUuid = "custom_${System.currentTimeMillis()}",
            name = name.ifBlank { "Custom Web Radio" },
            streamUrl = streamUrl.trim(),
            genre = genre.ifBlank { "Custom" },
            country = country.ifBlank { "User Feed" },
            bitrateKbps = 128
        )
        userCustomStations.add(0, station)
        return station
    }
}
