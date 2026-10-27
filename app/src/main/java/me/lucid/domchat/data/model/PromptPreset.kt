package me.lucid.domchat.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class PresetInjectionPosition {
    @SerialName("system")
    SYSTEM_PROMPT,
    @SerialName("depth")
    AT_DEPTH,
    @SerialName("before_last_user")
    BEFORE_LAST_USER
}

@Serializable
data class PromptPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val content: String,
    val description: String = "",
    val isSystem: Boolean = false,
    val injectionPosition: PresetInjectionPosition = PresetInjectionPosition.SYSTEM_PROMPT,
    val depth: Int = 0
)
