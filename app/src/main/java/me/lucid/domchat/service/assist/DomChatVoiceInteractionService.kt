package me.lucid.domchat.service.assist

import android.service.voice.VoiceInteractionService

/**
 * Root voice-interaction service. Registering this (plus the session service below)
 * makes DomChat selectable as the device "Default digital assistant app".
 *
 * The system binds this service when the user picks DomChat as their assistant; the
 * actual per-invocation UI lives in [AetherVoiceInteractionSessionService].
 */
typealias AetherVoiceInteractionService = DomChatVoiceInteractionService

class DomChatVoiceInteractionService : VoiceInteractionService()
