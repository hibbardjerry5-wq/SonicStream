package com.example.data.remote

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.CuratedRadioStations
import com.example.model.SpotifyAuthToken
import com.example.model.SpotifyTrackItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Real Spotify Web API Service for searching tracks, fetching preview streams,
 * and integrating Spotify playback with simultaneous police scanner mixing.
 */
class SpotifyApiService {

    private val tag = "SpotifyApiService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var cachedToken: SpotifyAuthToken? = null

    // Check if user has configured Spotify Client credentials in Secrets or BuildConfig
    fun hasConfiguredCredentials(): Boolean {
        return try {
            val clientId = BuildConfig.SPOTIFY_CLIENT_ID
            val clientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET
            clientId.isNotBlank() && clientSecret.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Authenticates with Spotify Accounts service using OAuth2 Client Credentials grant.
     */
    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val current = cachedToken
        if (current != null && !current.isExpired) {
            return@withContext current.accessToken
        }

        val clientId = try { BuildConfig.SPOTIFY_CLIENT_ID } catch (e: Exception) { "" }
        val clientSecret = try { BuildConfig.SPOTIFY_CLIENT_SECRET } catch (e: Exception) { "" }

        if (clientId.isBlank() || clientSecret.isBlank()) {
            Log.d(tag, "Spotify credentials not set in secrets; using curated Spotify streaming tracks.")
            return@withContext null
        }

        try {
            val authHeader = "Basic " + Base64.encodeToString(
                "$clientId:$clientSecret".toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )

            val body = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build()

            val request = Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .header("Authorization", authHeader)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext null
                val json = JSONObject(responseBody)
                val token = json.getString("access_token")
                val expiresIn = json.optInt("expires_in", 3600)
                cachedToken = SpotifyAuthToken(accessToken = token, expiresInSeconds = expiresIn)
                return@withContext token
            } else {
                Log.w(tag, "Spotify token error response code: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching Spotify access token: ${e.message}")
            null
        }
    }

    /**
     * Search Spotify catalog for tracks matching the query.
     */
    suspend fun searchTracks(query: String): List<SpotifyTrackItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext CuratedRadioStations.sampleSpotifyTracks
        }

        val token = getAccessToken()
        if (token == null) {
            // Filter curated library by query
            val q = query.lowercase()
            return@withContext CuratedRadioStations.sampleSpotifyTracks.filter {
                it.name.lowercase().contains(q) || it.artist.lowercase().contains(q) || it.album.lowercase().contains(q)
            }
        }

        try {
            val encodedQuery = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://api.spotify.com/v1/search?q=$encodedQuery&type=track&limit=20"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(tag, "Spotify search failed with code: ${response.code}")
                return@withContext CuratedRadioStations.sampleSpotifyTracks
            }

            val body = response.body?.string() ?: return@withContext CuratedRadioStations.sampleSpotifyTracks
            val json = JSONObject(body)
            val tracksJson = json.getJSONObject("tracks").getJSONArray("items")
            val results = mutableListOf<SpotifyTrackItem>()

            for (i in 0 until tracksJson.length()) {
                val item = tracksJson.getJSONObject(i)
                val id = item.getString("id")
                val name = item.getString("name")
                val artistsArray = item.getJSONArray("artists")
                val artistNames = mutableListOf<String>()
                for (a in 0 until artistsArray.length()) {
                    artistNames.add(artistsArray.getJSONObject(a).getString("name"))
                }
                val artist = artistNames.joinToString(", ")
                val albumObj = item.getJSONObject("album")
                val albumName = albumObj.getString("name")
                val imagesArray = albumObj.optJSONArray("images")
                val albumArt = if (imagesArray != null && imagesArray.length() > 0) {
                    imagesArray.getJSONObject(0).getString("url")
                } else null
                val durationMs = item.optLong("duration_ms", 210000L)
                val previewUrl = item.optString("preview_url", "")
                val uri = item.optString("uri", "spotify:track:$id")
                val pop = item.optInt("popularity", 50)

                results.add(
                    SpotifyTrackItem(
                        id = id,
                        name = name,
                        artist = artist,
                        album = albumName,
                        durationMs = durationMs,
                        previewUrl = previewUrl.ifBlank { null },
                        albumArtUrl = albumArt,
                        spotifyUri = uri,
                        popularity = pop
                    )
                )
            }
            if (results.isNotEmpty()) results else CuratedRadioStations.sampleSpotifyTracks
        } catch (e: Exception) {
            Log.e(tag, "Spotify search error: ${e.message}")
            CuratedRadioStations.sampleSpotifyTracks
        }
    }
}
