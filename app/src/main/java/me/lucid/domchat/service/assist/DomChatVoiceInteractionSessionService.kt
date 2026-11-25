package me.lucid.domchat.service.assist

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService

/**
 * Factory for assist sessions. The framework calls [onNewSession] each time the
 * assist gesture fires while DomChat is the default assistant.
 */
typealias AetherVoiceInteractionSessionService = DomChatVoiceInteractionSessionService

class DomChatVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return DomChatVoiceInteractionSession(this)
    }
}
