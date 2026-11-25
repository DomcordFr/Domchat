package me.lucid.tts.model

import kotlinx.serialization.Serializable

@Serializable
data class TTSModelInfo(
    val id: String,
    val displayName: String = id,
)
