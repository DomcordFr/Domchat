package me.lucid.domchat.data.model

import kotlinx.serialization.Serializable

/**
 * Visual styling modes for chat message bubbles.
 */
@Serializable
enum class ChatMessageStyle(val displayName: String, val description: String) {
    BUBBLES(
        displayName = "Liquid Glass Bubbles",
        description = "Apple-grade Liquid Glass rounded bubbles with specular caustics and caustics edge borders."
    ),
    MESSENGER(
        displayName = "Messenger Compact",
        description = "Modern compact messaging speech bubbles with directional tail curves and tighter typography."
    ),
    TRANSPARENT_BOTTOM_BLUR(
        displayName = "Immersive Roleplay",
        description = "No background card around text, with atmospheric frosted blur fading upward from the bottom."
    )
}
