package me.lucid.domchat.data.model

import kotlinx.serialization.Serializable

/**
 * Visual styling modes for chat message bubbles.
 */
@Serializable
enum class ChatMessageStyle(val displayName: String, val description: String) {
    BUBBLES(
        displayName = "Liquid Glass Bubbles",
        description = "Rounded glass bubbles with a soft shine on the edge."
    ),
    MESSENGER(
        displayName = "Messenger Compact",
        description = "Tight chat bubbles with tails, like a texting app."
    ),
    TRANSPARENT_BOTTOM_BLUR(
        displayName = "Immersive Roleplay",
        description = "No card. Text floats over a soft blur at the bottom."
    )
}
