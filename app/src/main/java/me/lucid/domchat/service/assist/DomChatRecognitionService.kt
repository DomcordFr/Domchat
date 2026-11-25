package me.lucid.domchat.service.assist

import android.content.Intent
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/**
 * Minimal [RecognitionService] stub.
 *
 * A `VoiceInteractionService` manifest entry requires an associated recognition
 * service via `android:recognitionService`. DomChat performs its own STT inside the
 * overlay (see ChatMultimodalASRController / rememberCustomSttState), so this stub only
 * exists to satisfy that requirement and reports "not available" if invoked directly.
 */
typealias AetherRecognitionService = DomChatRecognitionService

class DomChatRecognitionService : RecognitionService() {
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        runCatching { listener?.error(SpeechRecognizer.ERROR_RECOGNIZER_BUSY) }
    }

    override fun onCancel(listener: Callback?) {}

    override fun onStopListening(listener: Callback?) {}
}
