package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.model.MusicTrack
import com.example.model.PlaylistModel
import com.example.model.TrackItemModel
import com.example.model.UserSettingsModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MusicSyncRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing cloud sync.")
    }

    val isUserSignedIn: Boolean
        get() = auth.currentUser != null

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // --- PLAYLISTS ---

    fun observePlaylists(): Flow<List<PlaylistModel>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/playlists"
        emitAll(
            db.collection("users").document(uid).collection("playlists")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(PlaylistModel::class.java) }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun savePlaylist(playlist: PlaylistModel): Result<String> {
        return try {
            val uid = requireUserId()
            val collectionRef = db.collection("users").document(uid).collection("playlists")
            val docId = if (playlist.id.isNotBlank()) playlist.id else collectionRef.document().id
            val docRef = collectionRef.document(docId)

            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "name" to playlist.name,
                "trackIds" to playlist.trackIds,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            playlist.description?.let { payload["description"] = it }
            playlist.colorHex?.let { payload["colorHex"] = it }

            if (playlist.createdAt != null) {
                payload["createdAt"] = playlist.createdAt
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }

            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unknown"
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/playlists/${playlist.id}")
            Result.failure(e)
        }
    }

    suspend fun deletePlaylist(playlistId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("playlists").document(playlistId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unknown"
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/playlists/$playlistId")
            Result.failure(e)
        }
    }

    // --- USER SETTINGS & EQ PROFILES ---

    fun observeUserSettings(): Flow<UserSettingsModel?> = flow {
        val uid = requireUserId()
        val path = "users/$uid/user_settings/audio_settings"
        emitAll(
            db.collection("users").document(uid).collection("user_settings").document("audio_settings")
                .snapshots()
                .map { snapshot -> snapshot.toObject(UserSettingsModel::class.java) }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveUserSettings(settings: UserSettingsModel): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("user_settings").document("audio_settings")
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "equalizerBands" to settings.equalizerBands,
                "bassBoost" to settings.bassBoost,
                "virtualizer" to settings.virtualizer,
                "reverbPreset" to settings.reverbPreset,
                "activePreset" to settings.activePreset,
                "themeAccent" to settings.themeAccent,
                "visualizerMode" to settings.visualizerMode,
                "volumeNormalization" to settings.volumeNormalization,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (settings.createdAt != null) {
                payload["createdAt"] = settings.createdAt
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unknown"
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/user_settings/audio_settings")
            Result.failure(e)
        }
    }

    // --- LIBRARY TRACKS STATS ---

    suspend fun syncTrackFavorite(track: MusicTrack, isFav: Boolean): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("library_tracks").document(track.id)
            val payload = mapOf(
                "userId" to uid,
                "title" to track.title,
                "artist" to track.artist,
                "album" to track.album,
                "duration" to track.durationMs,
                "isFavorite" to isFav,
                "playCount" to track.playCount,
                "lastPlayedAt" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unknown"
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/library_tracks/${track.id}")
            Result.failure(e)
        }
    }
}
