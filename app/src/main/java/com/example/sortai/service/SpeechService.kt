package com.example.sortai.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.sortai.model.AppLanguage
import java.util.Locale

class SpeechService(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setSpeechRate(0.9f) // Slightly slower for clear instruction
        } else {
            Log.e("SpeechService", "Failed to initialize TextToSpeech: $status")
        }
    }

    fun speak(text: String, language: AppLanguage) {
        if (!isInitialized || tts == null) return

        val locale = when (language) {
            AppLanguage.HINDI -> java.util.Locale.Builder().setLanguage("hi").setRegion("IN").build()
            AppLanguage.ENGLISH -> Locale.US
        }

        try {
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English if Hindi TTS data is missing on the device
                tts?.setLanguage(Locale.US)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SortAiUtterance_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e("SpeechService", "Error during TTS playback", e)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
