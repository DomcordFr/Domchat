package me.lucid.domchat.ui.modifier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import me.lucid.domchat.ui.motion.LocalMotionPolicy

@Composable
fun Modifier.shimmer(
    isLoading: Boolean,
    shimmerColor: Color = LocalContentColor.current.copy(alpha = 0.3f),
    backgroundColor: Color = LocalContentColor.current.copy(alpha = 0.9f),
    durationMillis: Int = 1200,
    angle: Float = 20f,
    gradientWidthRatio: Float = 0.5f
): Modifier {
    if (!isLoading) return this

    val motionPolicy = LocalMotionPolicy.current
    val progressProvider: () -> Float = if (motionPolicy.reduceMotion) {
        { 0.35f }
    } else {
        val transition = rememberInfiniteTransition(label = "ShimmerTransition")
        val progress = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ShimmerProgress"
        )
        val provider: () -> Float = { progress.value }
        provider
    }

    return this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .then(
            ShimmerElement(
                progressProvider = progressProvider,
                shimmerColor = shimmerColor,
                backgroundColor = backgroundColor,
                angle = angle,
                gradientWidthRatio = gradientWidthRatio
            )
        )
}

private data class ShimmerElement(
    val progressProvider: () -> Float,
    val shimmerColor: Color,
    val backgroundColor: Color,
    val angle: Float,
    val gradientWidthRatio: Float
) : ModifierNodeElement<ShimmerNode>() {

    override fun create(): ShimmerNode {
        return ShimmerNode(
            progressProvider = progressProvider,
            shimmerColor = shimmerColor,
            backgroundColor = backgroundColor,
            angle = angle,
            gradientWidthRatio = gradientWidthRatio
        )
    }

    override fun update(node: ShimmerNode) {
        node.progressProvider = progressProvider
        node.shimmerColor = shimmerColor
        node.backgroundColor = backgroundColor
        node.angle = angle
        node.gradientWidthRatio = gradientWidthRatio
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "shimmer"
        properties["shimmerColor"] = shimmerColor
        properties["backgroundColor"] = backgroundColor
        properties["angle"] = angle
        properties["gradientWidthRatio"] = gradientWidthRatio
    }
}

private class ShimmerNode(
    var progressProvider: () -> Float,
    var shimmerColor: Color,
    var backgroundColor: Color,
    var angle: Float,
    var gradientWidthRatio: Float
) : Modifier.Node(), DrawModifierNode {

    private var cachedWidth: Float = -1f
    private var cachedHeight: Float = -1f
    private var cachedShimmerColor: Color = Color.Unspecified
    private var cachedBackgroundColor: Color = Color.Unspecified
    private var cachedAngle: Float = -1f
    private var cachedGradientWidthRatio: Float = -1f

    private var shader: android.graphics.LinearGradient? = null
    private var shaderBrush: ShaderBrush? = null
    private val matrix = android.graphics.Matrix()

    private var angleRad: Float = 0f
    private var cosAngle: Float = 1f
    private var sinAngle: Float = 0f
    private var gradientWidth: Float = 0f
    private var totalDistance: Float = 0f

    override fun ContentDrawScope.draw() {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) {
            drawContent()
            return
        }

        if (width != cachedWidth ||
            height != cachedHeight ||
            shimmerColor != cachedShimmerColor ||
            backgroundColor != cachedBackgroundColor ||
            angle != cachedAngle ||
            gradientWidthRatio != cachedGradientWidthRatio
        ) {
            cachedWidth = width
            cachedHeight = height
            cachedShimmerColor = shimmerColor
            cachedBackgroundColor = backgroundColor
            cachedAngle = angle
            cachedGradientWidthRatio = gradientWidthRatio

            angleRad = Math.toRadians(angle.toDouble()).toFloat()
            cosAngle = kotlin.math.cos(angleRad)
            sinAngle = kotlin.math.sin(angleRad)

            val diagonal = kotlin.math.sqrt(width * width + height * height)
            gradientWidth = diagonal * gradientWidthRatio
            totalDistance = diagonal + gradientWidth

            val colors = intArrayOf(
                backgroundColor.toArgb(),
                shimmerColor.toArgb(),
                backgroundColor.toArgb()
            )
            val positions = floatArrayOf(0f, 0.5f, 1f)

            val newShader = android.graphics.LinearGradient(
                0f,
                0f,
                gradientWidth * cosAngle,
                gradientWidth * sinAngle,
                colors,
                positions,
                android.graphics.Shader.TileMode.CLAMP
            )
            shader = newShader
            shaderBrush = ShaderBrush(newShader)
        }

        val brush = shaderBrush
        val localShader = shader
        if (brush != null && localShader != null) {
            val progress = progressProvider()
            val currentOffset = progress * totalDistance - gradientWidth
            val dx = currentOffset * cosAngle
            val dy = currentOffset * sinAngle

            matrix.setTranslate(dx, dy)
            localShader.setLocalMatrix(matrix)

            drawContent()
            drawRect(
                brush = brush,
                blendMode = BlendMode.DstIn
            )
        } else {
            drawContent()
        }
    }
}
