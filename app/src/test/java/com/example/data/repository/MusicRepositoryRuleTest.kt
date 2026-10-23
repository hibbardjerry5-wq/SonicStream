package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.remote.MusicSyncRepository
import com.example.model.PlaylistModel
import com.example.model.UserSettingsModel
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class MusicRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun savePlaylist_authenticatedOwner_createsDocument(): Unit = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = MusicSyncRepository(firestore)

        val playlist = PlaylistModel(
            id = "pl_${UUID.randomUUID().toString().take(8)}",
            userId = uid,
            name = "Alice Synth Playlist",
            description = "Neon retro vibes",
            colorHex = "#00E5FF",
            trackIds = listOf("track_1", "track_2")
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.savePlaylist(playlist)
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun observePlaylists_authenticatedOwner_emitsPlaylists(): Unit = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = MusicSyncRepository(firestore)
        val playlistId = "pl_${UUID.randomUUID().toString().take(8)}"

        val playlist = PlaylistModel(
            id = playlistId,
            userId = uid,
            name = "Alice Stream Test",
            trackIds = listOf("track_1")
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.savePlaylist(playlist).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePlaylists().first { list -> list.any { it.id == playlistId } }
        }
        assertTrue(emitted.map { it.id }.contains(playlistId))
    }

    @Test
    fun saveUserSettings_and_observe_succeeds(): Unit = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = MusicSyncRepository(firestore)

        val settings = UserSettingsModel(
            userId = uid,
            equalizerBands = listOf(4, 2, 0, 1, 3),
            bassBoost = 500,
            virtualizer = 300,
            activePreset = "Bass Boost",
            visualizerMode = "Spectrum Bars"
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserSettings(settings)
        }
        assertTrue(saveResult.isSuccess)

        val loaded = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserSettings().first { it != null }
        }
        assertNotNull(loaded)
        assertEquals("Bass Boost", loaded?.activePreset)
        assertEquals(500, loaded?.bassBoost)
    }

    @Test
    fun observePlaylists_unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
        auth.signOut()
        val repository = MusicSyncRepository(firestore)

        var failed = false
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observePlaylists().first()
            }
        } catch (e: Exception) {
            failed = true
            val firestoreException = e.unwrapFirestoreException()
            assertTrue(e is IllegalStateException || firestoreException?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
        }
        assertTrue(failed)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@resonance.test"
        const val BOB_EMAIL = "bob@resonance.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
