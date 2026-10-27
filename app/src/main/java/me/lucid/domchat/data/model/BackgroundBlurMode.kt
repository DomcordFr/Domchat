package me.lucid.domchat.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class BackgroundBlurMode(val displayName: String, val description: String) {
    FULL("Full Blur", "Entire wallpaper blurred evenly across chat"),
    BOTTOM_FADE("Bottom Fade Blur", "Atmospheric blur concentrated at the bottom, fading upward")
}
