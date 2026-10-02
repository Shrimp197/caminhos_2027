package com.caminhos2027.v1.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import com.caminhos2027.v1.core.model.AudioMode
import java.util.Locale
import java.util.UUID

/** Real Android speech output for the V1 walking status, with no invented navigation instructions. */
internal class AndroidWalkingAudioFeedback(context: Context) {
    private lateinit var engine: TextToSpeech
    private var ready = false

    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = engine.setLanguage(Locale("pt", "PT"))
                ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                if (ready) engine.setSpeechRate(0.96f)
            }
        }
    }

    fun speak(message: String, mode: AudioMode) {
        if (!ready || mode == AudioMode.SILENT || message.isBlank()) return
        val queueMode = if (mode == AudioMode.IMMERSIVE) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH
        engine.speak(message, queueMode, null, "caminhos-" + UUID.randomUUID().toString())
    }

    fun release() {
        engine.stop()
        engine.shutdown()
    }
}
