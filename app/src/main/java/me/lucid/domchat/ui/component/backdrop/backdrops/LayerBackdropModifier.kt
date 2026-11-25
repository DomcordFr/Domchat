/*
 * Originally vendored from Kyant0/backdrop v2.0.0 (io.github.kyant0:backdrop)
 * https://github.com/Kyant0/backdrop — Copyright 2025 Kyant0, Apache License 2.0
 *
 */
package me.lucid.domchat.ui.component.backdrop.backdrops

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import me.lucid.domchat.ui.component.backdrop.internal.recordLayer

/**
 * @param frozen no longer read by the source; kept so callers need not change.
 *   Samplers still take their own `frozen`.
 */
fun Modifier.layerBackdrop(
    backdrop: LayerBackdrop,
    frozen: () -> Boolean = { false },
    enabled: Boolean = true,
): Modifier = this then LayerBackdropElement(backdrop, frozen, enabled)

private class LayerBackdropElement(
    val backdrop: LayerBackdrop,
    val frozen: () -> Boolean,
    val enabled: Boolean,
) : ModifierNodeElement<LayerBackdropNode>() {

    override fun create(): LayerBackdropNode {
        return LayerBackdropNode(backdrop, frozen, enabled)
    }

    override fun update(node: LayerBackdropNode) {
        val backdropChanged = node.backdrop !== backdrop
        val frozenChanged = node.frozen !== frozen
        val enabledChanged = node.enabled != enabled
        if (backdropChanged) {
            node.backdrop.sourceInvalidator = null
            node.backdrop.layerCoordinates = null
            node.backdrop = backdrop
            backdrop.sourceInvalidator = { node.invalidateDraw() }
            node.invalidateDraw()
        }
        node.frozen = frozen
        node.enabled = enabled
        if (frozenChanged || (enabledChanged && enabled)) {
            node.invalidateDraw()
        }
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "layerBackdrop"
        properties["backdrop"] = backdrop
        properties["enabled"] = enabled
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LayerBackdropElement) return false

        if (backdrop != other.backdrop) return false
        if (frozen != other.frozen) return false
        if (enabled != other.enabled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 31 * backdrop.hashCode() + frozen.hashCode()
        result = 31 * result + enabled.hashCode()
        return result
    }
}

private class LayerBackdropNode(
    var backdrop: LayerBackdrop,
    var frozen: () -> Boolean,
    var enabled: Boolean,
) : Modifier.Node(), DrawModifierNode, GlobalPositionAwareModifierNode {

    override fun onAttach() {
        backdrop.sourceInvalidator = { invalidateDraw() }
    }

    override fun ContentDrawScope.draw() {
        // When disabled and already primed with a recording, skip offscreen recording
        // so normal chat scrolling/typing pays zero RenderNode copy overhead, while
        // keeping the node attached and ready the instant the drawer or sheet opens.
        if (!enabled && backdrop.hasRecording) {
            drawContent()
            return
        }
        // Record once and composite to the screen; glass samplers reuse this layer.
        recordLayer(this@LayerBackdropNode, backdrop.graphicsLayer) { backdrop.onDraw(this@draw) }
        drawLayer(backdrop.graphicsLayer)
        // Notify sampling glass surfaces that the source pixels changed, so they
        // re-record their own layers. When the source is static they skip it.
        backdrop.contentRecorded()
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) {
            if (backdrop.layerCoordinates !== coordinates) backdrop.geometryChanged()
            backdrop.layerCoordinates = coordinates
        }
    }

    override fun onDetach() {
        if (backdrop.sourceInvalidator != null) {
            backdrop.sourceInvalidator = null
        }
        // Retain last known coordinates across transient detachments (keyboard animation,
        // scroll recycling) so active glass samplers do not drop to null coordinates.
    }
}
