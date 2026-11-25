/*
 * Originally vendored from Kyant0/backdrop v2.0.0 (io.github.kyant0:backdrop)
 * https://github.com/Kyant0/backdrop — Copyright 2025 Kyant0, Apache License 2.0
 *
 */
package me.lucid.domchat.ui.component.backdrop

import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density

interface Backdrop {

    val isCoordinatesDependent: Boolean
    val hasRecording: Boolean get() = true

    fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)? = null
    )
}

/** Application-wide backdrop captured from the chat wallpaper + content. */
val LocalAppBackdrop = androidx.compose.runtime.staticCompositionLocalOf<Backdrop?> { null }
