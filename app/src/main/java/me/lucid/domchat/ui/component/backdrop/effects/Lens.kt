/*
 * Originally vendored from Kyant0/backdrop v2.0.0 (io.github.kyant0:backdrop)
 * https://github.com/Kyant0/backdrop — Copyright 2025 Kyant0, Apache License 2.0
 *
 */
package me.lucid.domchat.ui.component.backdrop.effects

import androidx.annotation.FloatRange
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastCoerceAtMost
import me.lucid.domchat.ui.component.backdrop.BackdropEffectScope
import me.lucid.domchat.ui.component.backdrop.internal.RoundedRectRefractionShaderString
import me.lucid.domchat.ui.component.backdrop.internal.RoundedRectRefractionWithDispersionShaderString
import me.lucid.domchat.ui.component.backdrop.internal.runtimeShaderEffect
import me.lucid.domchat.ui.component.backdrop.isRuntimeShaderSupported

fun BackdropEffectScope.lens(
    @FloatRange(from = 0.0) refractionHeight: Float,
    @FloatRange(from = 0.0) refractionAmount: Float,
    depthEffect: Boolean = false,
    chromaticAberration: Boolean = false,
    fastCircleMap: Boolean = false,
    @FloatRange(from = 0.0, to = 1.0) chromaticAberrationAmount: Float =
        if (chromaticAberration) 1f else 0f,
) {
    if (!isRuntimeShaderSupported()) return
    if (refractionHeight <= 0f || refractionAmount <= 0f) return

    val neededPadding = refractionAmount.coerceAtLeast(refractionHeight)
    val padPx = kotlin.math.ceil(maxOf(padding, neededPadding)).toInt()
    val padF = padPx.toFloat()
    padding = padF

    val cornerRadii = cornerRadii
    val effect =
        if (cornerRadii != null) {
            val dispersion = if (chromaticAberration) chromaticAberrationAmount.coerceIn(0f, 1f) else 0f
            val shader =
                if (dispersion <= 0f) {
                    obtainRuntimeShader(
                        "Refraction",
                        RoundedRectRefractionShaderString
                    )
                } else {
                    obtainRuntimeShader(
                        "RefractionWithDispersion",
                        RoundedRectRefractionWithDispersionShaderString
                    )
                }
            shader.apply {
                val w = size.width
                val h = size.height
                setFloatUniform("halfSize", w * 0.5f, h * 0.5f)
                setFloatUniform("minHalf", minOf(w, h) * 0.5f)
                setFloatUniform("offset", -padF, -padF)
                setFloatUniform("cornerRadii", cornerRadii)
                setFloatUniform("refractionHeight", refractionHeight)
                setFloatUniform("refractionAmount", -refractionAmount)
                setFloatUniform("depthEffect", if (depthEffect) 1f else 0f)
                setIntUniform("circleMapMode", if (fastCircleMap) 1 else 0)
                if (dispersion > 0f) {
                    setFloatUniform("chromaticAberration", dispersion)
                }
            }
            runtimeShaderEffect(shader, "content")
        } else {
            throwUnsupportedSDFException()
        }
    effect(effect)
}

// Vendored change: support for io.github.kyant0:shapes' RoundedRectangularShape was
// dropped so the vendored sources have no external dependency; this app only passes
// CornerBasedShape here.
private val BackdropEffectScope.cornerRadii: FloatArray?
    get() = when (val shape = shape) {
        is AbsoluteRoundedCornerShape -> {
            val size = size
            val maxRadius = size.minDimension / 2f
            val topLeft = shape.topStart.toPx(size, this)
            val topRight = shape.topEnd.toPx(size, this)
            val bottomRight = shape.bottomEnd.toPx(size, this)
            val bottomLeft = shape.bottomStart.toPx(size, this)
            floatArrayOf(
                topLeft.fastCoerceAtMost(maxRadius),
                topRight.fastCoerceAtMost(maxRadius),
                bottomRight.fastCoerceAtMost(maxRadius),
                bottomLeft.fastCoerceAtMost(maxRadius)
            )
        }

        is CornerBasedShape -> {
            val size = size
            val maxRadius = size.minDimension / 2f
            val isLtr = layoutDirection == LayoutDirection.Ltr
            val topLeft =
                if (isLtr) shape.topStart.toPx(size, this)
                else shape.topEnd.toPx(size, this)
            val topRight =
                if (isLtr) shape.topEnd.toPx(size, this)
                else shape.topStart.toPx(size, this)
            val bottomRight =
                if (isLtr) shape.bottomEnd.toPx(size, this)
                else shape.bottomStart.toPx(size, this)
            val bottomLeft =
                if (isLtr) shape.bottomStart.toPx(size, this)
                else shape.bottomEnd.toPx(size, this)
            floatArrayOf(
                topLeft.fastCoerceAtMost(maxRadius),
                topRight.fastCoerceAtMost(maxRadius),
                bottomRight.fastCoerceAtMost(maxRadius),
                bottomLeft.fastCoerceAtMost(maxRadius)
            )
        }

        else -> null
    }

private fun throwUnsupportedSDFException(): Nothing {
    throw UnsupportedOperationException(
        "Only RoundedRectangularShape or CornerBasedShape is supported in lens effects."
    )
}
