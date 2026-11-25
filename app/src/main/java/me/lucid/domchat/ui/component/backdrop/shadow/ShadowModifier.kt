/*
 * Originally vendored from Kyant0/backdrop v2.0.0 (io.github.kyant0:backdrop)
 * https://github.com/Kyant0/backdrop — Copyright 2025 Kyant0, Apache License 2.0
 *
 */
package me.lucid.domchat.ui.component.backdrop.shadow

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import me.lucid.domchat.ui.component.backdrop.internal.ShapeProvider
import me.lucid.domchat.ui.component.backdrop.internal.blur
import kotlin.math.ceil

internal class ShadowElement(
    val shapeProvider: ShapeProvider,
    val shadow: () -> Shadow?
) : ModifierNodeElement<ShadowNode>() {

    override fun create(): ShadowNode {
        return ShadowNode(shapeProvider, shadow)
    }

    override fun update(node: ShadowNode) {
        node.shapeProvider = shapeProvider
        node.shadow = shadow
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "shadow"
        properties["shapeProvider"] = shapeProvider
        properties["shadow"] = shadow
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShadowElement) return false

        if (shapeProvider != other.shapeProvider) return false
        if (shadow != other.shadow) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shapeProvider.hashCode()
        result = 31 * result + shadow.hashCode()
        return result
    }
}

internal class ShadowNode(
    var shapeProvider: ShapeProvider,
    var shadow: () -> Shadow?
) : Modifier.Node(), DrawModifierNode {

    override val shouldAutoInvalidate: Boolean = false

    private var shadowLayer: GraphicsLayer? = null

    private val paint = Paint()

    private var recordedShadow: Shadow? = null
    private var recordedSize = IntSize.Zero
    private var recordedOutline: Outline? = null

    override fun ContentDrawScope.draw() {
        val shadow = shadow() ?: return drawContent()

        val shadowLayer = shadowLayer
        if (shadowLayer != null) {
            val size = size
            val density: Density = this
            val layoutDirection = layoutDirection

            val radius = shadow.radius.toPx()
            val offsetX = shadow.offset.x.toPx()
            val offsetY = shadow.offset.y.toPx()
            val shadowSize = IntSize(
                ceil(size.width + radius * 4f + offsetX).toInt(),
                ceil(size.height + radius * 4f + offsetY).toInt()
            )
            val outline = shapeProvider.shape.createOutline(size, layoutDirection, density)

            shadowLayer.alpha = shadow.alpha
            shadowLayer.blendMode = shadow.blendMode
            if (shadow != recordedShadow || shadowSize != recordedSize || outline != recordedOutline) {
                configurePaint(shadow)
                shadowLayer.record(shadowSize) {
                    translate(radius * 2f + offsetX, radius * 2f + offsetY) {
                        val canvas = drawContext.canvas
                        canvas.save()
                        canvas.translate(-offsetX, -offsetY)
                        canvas.clipOutlineOut(outline)
                        canvas.translate(offsetX, offsetY)
                        canvas.drawOutline(outline, paint)
                        canvas.restore()
                    }
                }
                recordedShadow = shadow
                recordedSize = shadowSize
                recordedOutline = outline
            }

            translate(-radius * 2f, -radius * 2f) {
                drawLayer(shadowLayer)
            }
        }

        drawContent()
    }

    override fun onAttach() {
        val graphicsContext = requireGraphicsContext()
        shadowLayer =
            graphicsContext.createGraphicsLayer().apply {
                compositingStrategy = CompositingStrategy.ModulateAlpha
            }
    }

    override fun onDetach() {
        val graphicsContext = requireGraphicsContext()
        shadowLayer?.let { layer ->
            graphicsContext.releaseGraphicsLayer(layer)
            shadowLayer = null
        }
        recordedShadow = null
        recordedSize = IntSize.Zero
        recordedOutline = null
    }

    private fun DrawScope.configurePaint(shadow: Shadow) {
        paint.color = shadow.color
        paint.blur(shadow.radius.toPx())
    }
}

private fun androidx.compose.ui.graphics.Canvas.clipOutlineOut(outline: Outline) {
    when (outline) {
        is Outline.Rectangle -> clipRect(outline.rect, ClipOp.Difference)
        is Outline.Rounded -> clipPath(Path().apply { addRoundRect(outline.roundRect) }, ClipOp.Difference)
        is Outline.Generic -> clipPath(outline.path, ClipOp.Difference)
    }
}
