package me.lucid.domchat.data.model

import kotlinx.serialization.Serializable

/**
 * Optical realism level for Liquid Glass styling.
 */
@Serializable
enum class LiquidGlassMode {
    /**
     * Soft Frosted:
     * Super fast, light on battery, smooth 120 FPS frosted look.
     */
    SOFT_FROSTED,

    /**
     * Balanced Blend (Default):
     * The best balance. Soft frosted cards with real water-like refraction and shine on floating bars and buttons.
     */
    BALANCED_BLEND,

    /**
     * Full Water Lens:
     * Full optical effect. Everything bends and shines like real liquid glass.
     */
    FULL_WATER_LENS
}
