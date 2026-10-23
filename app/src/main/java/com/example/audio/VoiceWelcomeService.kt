package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Text-To-Speech Neural Voice Welcome Service.
 * Delivers an intelligent cybernetic vocal greeting when the user opens and boots up the app.
 */
class VoiceWelcomeService(context: Context) : TextToSpeech.OnInitListener {

    private val tag = "VoiceWelcomeService"
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingGreeting: String? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize TextToSpeech: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setPitch(1.08f) // Futuristic cyber pitch
                    engine.setSpeechRate(1.02f)
                    isInitialized = true
                    Log.d(tag, "TextToSpeech initialized successfully.")

                    pendingGreeting?.let { phrase ->
                        speakGreeting(phrase)
                        pendingGreeting = null
                    }
                }
            }
        } else {
            Log.w(tag, "TextToSpeech init returned status: $status")
        }
    }

    fun speakWelcomeGreeting() {
        val greeting = "Welcome to Resonance Neural Audio OS. Acoustic copilot engaged. Multi-band public safety, Spotify, Apple Music, and Internet Radio streams calibrated and online."
        speakGreeting(greeting)
    }

    fun speakGreeting(message: String) {
        if (isInitialized && tts != null) {
            try {
                tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "welcome_utterance_${System.currentTimeMillis()}")
            } catch (e: Exception) {
                Log.w(tag, "Error speaking greeting: ${e.message}")
            }
        } else {
            pendingGreeting = message
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.w(tag, "Error stopping TTS: ${e.message}")
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.w(tag, "Error shutting down TTS: ${e.message}")
        }
    }
}
