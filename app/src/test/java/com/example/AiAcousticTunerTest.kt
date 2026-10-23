package com.example

import com.example.audio.AcousticEnvironmentSample
import com.example.audio.AiAcousticTunerService
import org.junit.Assert.*
import org.junit.Test

class AiAcousticTunerTest {

    private val tunerService = AiAcousticTunerService()

    @Test
    fun testHighwayRoadNoiseCompensation() {
        val highwaySample = AcousticEnvironmentSample(
            splDb = 74f,
            lowRumbleRatio = 0.65f,
            lowMidRatio = 0.45f,
            midVocalRatio = 0.20f,
            highMidRatio = 0.15f,
            trebleRatio = 0.10f,
            estimatedEnvironment = "Highway Cabin Rumble"
        )

        val result = tunerService.solveOfflineAcoustics(
            sample = highwaySample,
            genre = "Electronic",
            fallbackNote = null
        )

        // Must boost low frequencies (60Hz & 230Hz) to counteract road rumble
        assertTrue("60Hz band should be boosted", result.equalizerBands[0] > 0)
        assertTrue("230Hz band should be boosted", result.equalizerBands[1] > 0)
        assertTrue("Loudness must be enabled in noisy cabin", result.loudnessEnabled)
        assertTrue("Subwoofer gain should be boosted", result.subwooferDb >= 4)
        assertNotNull(result.explanation)
    }

    @Test
    fun testQuietStudioReferenceCurve() {
        val studioSample = AcousticEnvironmentSample(
            splDb = 36f,
            lowRumbleRatio = 0.15f,
            lowMidRatio = 0.15f,
            midVocalRatio = 0.15f,
            highMidRatio = 0.15f,
            trebleRatio = 0.10f,
            estimatedEnvironment = "Quiet Night Studio"
        )

        val result = tunerService.solveOfflineAcoustics(
            sample = studioSample,
            genre = "Acoustic",
            fallbackNote = null
        )

        // In ultra quiet studio, loudness is false and virtualizer is subtle
        assertFalse("Loudness should not be needed in quiet room", result.loudnessEnabled)
        assertTrue("Virtualizer should be gentle", result.virtualizer <= 300)
    }
}
