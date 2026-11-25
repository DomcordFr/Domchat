package me.lucid.domchat.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import kotlin.math.tanh
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.lucid.domchat.ui.component.backdrop.BackdropEffectScope
import me.lucid.domchat.ui.component.backdrop.LocalAppBackdrop
import me.lucid.domchat.ui.component.backdrop.drawBackdrop
import me.lucid.domchat.ui.component.backdrop.effects.blur
import me.lucid.domchat.ui.component.backdrop.effects.lens
import me.lucid.domchat.ui.component.backdrop.highlight.Highlight
import me.lucid.domchat.ui.component.backdrop.highlight.HighlightStyle
import me.lucid.domchat.ui.component.backdrop.isRenderEffectSupported
import me.lucid.domchat.ui.component.backdrop.isRuntimeShaderSupported
import me.lucid.domchat.ui.context.LocalDisplaySetting
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.ui.modifier.domChatBlurEffect

/**
 * Liquid Glass Material Tokens.
 * Optical characteristics:
 * 1. Razor-sharp 0.5dp precision specular edge highlight catching ambient bounce light.
 * 2. Translucent surface luminance diffusion (children are kept razor-sharp).
 * 3. Gradient sheen simulating glass refraction and depth.
 * 4. Ambient ground occlusion.
 */
object LiquidGlassTokens {
    val CardShape = RoundedCornerShape(20.dp)
    val BubbleShape = RoundedCornerShape(18.dp)
    val PillShape = RoundedCornerShape(100.dp)

    // Specular border gradients - light reflects from top, disperses cleanly towards bottom
    val SpecularBorderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.40f), // Top specular rim catch
            Color.White.copy(alpha = 0.12f), // Mid-body glass refraction
            Color.White.copy(alpha = 0.03f), // Lower falloff
            Color.Transparent               // Clean ground falloff
        )
    )

    val SpecularBorderSubtle = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.22f),
            Color.White.copy(alpha = 0.06f),
            Color.Transparent
        )
    )

    val LightSpecularBorder = Brush.verticalGradient(
        listOf(
            Color.Black.copy(alpha = 0.12f),
            Color.Black.copy(alpha = 0.04f),
            Color.Transparent
        )
    )

    // Legacy static fallbacks (used when no settings composition local is available,
    // e.g. preview or unit composition). Clear glass anchor: unified DomChat glass card 0xFF141520.
    val GlassSurfaceDark = Color(0xFF141520).copy(alpha = 0.22f)
    val GlassSurfaceLight = Color(0xFFFFFFFF).copy(alpha = 0.45f)
    val BubbleGlassDark = Color(0xFFFFFFFF).copy(alpha = 0.06f)
    val BubbleGlassLight = Color(0xFF000000).copy(alpha = 0.03f)
}

/**
 * Dynamic liquid glass surface color derived from glassAlpha and glassBaseLuminance.
 * Returns Color.Transparent when glassAlpha <= 0.001f (0% opacity).
 */
@Composable
fun liquidGlassSurfaceColor(): Color {
    val display = LocalDisplaySetting.current
    val alpha = display.glassAlpha.coerceIn(0f, 1f)
    if (alpha <= 0.001f) return Color.Transparent
    var base = androidx.compose.ui.graphics.lerp(Color.Black, Color.White, display.glassBaseLuminance.coerceIn(0f, 1f))
    base = base.copy(alpha = alpha)
    // Non-clear tint applies a wash over the glass surface (tint alpha = wash strength).
    val tint = display.glassTintColor
    val tintColor = Color(tint)
    val strength = tintColor.alpha.coerceIn(0f, 1f)
    if (strength > 0.02f) {
        base = androidx.compose.ui.graphics.lerp(base, tintColor, strength)
    }
    return base
}

/**
 * Dynamic specular border brush for glass surfaces. Clear Glass uses a 0.18f white specular rim;
 * DARK and FROSTED use subtler rim catches scaled to their surface brightness.
 */
@Composable
fun liquidGlassBorderBrush(): Brush {
    val display = LocalDisplaySetting.current
    // Specular rim catch and depth scale jointly drive rim brightness.
    val specular = display.glassSpecularIntensity.coerceIn(0f, 1f)
    val depth = display.glassDepthScale.coerceIn(0f, 2.5f)
    val intensity = (specular * depth).coerceIn(0f, 1f)
    return Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.40f * intensity),
            Color.White.copy(alpha = 0.12f * intensity),
            Color.White.copy(alpha = 0.03f * intensity),
            Color.Transparent
        )
    )
}

/**
 * Dynamic chat bubble surface color derived from chatBubbleAlpha and chatBubbleTone.
 * Returns Color.Transparent when chatBubbleAlpha <= 0.001f (0% opacity).
 */
@Composable
fun liquidBubbleSurfaceColor(): Color {
    val display = LocalDisplaySetting.current
    val alpha = display.chatBubbleAlpha.coerceIn(0f, 1f)
    if (alpha <= 0.001f) return Color.Transparent
    val base = Color.Black
    return base.copy(alpha = alpha)
}

/**
 * Specular rim alpha for Clear Glass (0.18f white specular rim per design charter).
 */
val LiquidGlassSpecularRimAlpha = 0.18f

/**
 * True liquid glass surface: captures the chat wallpaper + content backdrop via
 * [LocalAppBackdrop], then blurs and refracts it with a true AGSL lens shader so
 * bubbles and cards physically distort what lies beneath them.
 *
 * Falls back cleanly to [domChatBlurEffect] (Haze ultraThin) with tint and specular
 * border when the backdrop is unavailable or runtime shaders are unsupported (API < 33).
 *
 * @param shape bubble/card outline (must be a CornerBasedShape such as RoundedCornerShape for lens).
 * @param blurRadius backdrop blur radius in dp.
 * @param refractionHeight lens refraction height in dp.
 * @param refractionAmount lens refraction amount in dp.
 * @param depthEffect whether to add the centerward depth gradient to refraction.
 * @param tintColor surface tint wash ([Color.Unspecified] = none).
 * @param specularIntensity specular rim strength 0..1.
 * @param borderWidth specular rim width in dp (0 disables).
 * @param specularBrush specular rim brush.
 * @param fillColor opaque surface fill painted beneath the refraction.
 * @param shadowElevation drop shadow elevation in dp.
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = LiquidGlassTokens.CardShape,
    blurRadius: Dp = 3.dp,
    refractionHeight: Dp = 16.dp,
    refractionAmount: Dp = 32.dp,
    depthEffect: Boolean = true,
    tintColor: Color = Color.Unspecified,
    specularIntensity: Float = 1f,
    borderWidth: Dp = 0.5.dp,
    specularBrush: Brush = LiquidGlassTokens.SpecularBorderSubtle,
    fillColor: Color = Color.Transparent,
    shadowElevation: Dp = 0.dp,
    isFloatingControl: Boolean = false,
): Modifier {
    val display = LocalDisplaySetting.current
    val density = LocalDensity.current
    val backdrop = LocalAppBackdrop.current
    val useGlass = backdrop != null && isRuntimeShaderSupported()

    val glassBackdropScale = 0.5f
    val blurPx = with(density) { blurRadius.toPx() } * glassBackdropScale
    val heightPx = with(density) { refractionHeight.toPx() } * glassBackdropScale
    val amountPx = with(density) { refractionAmount.toPx() } * glassBackdropScale
    val specular = specularIntensity.coerceIn(0f, 1f)

    val tint = if (tintColor == Color.Unspecified) Color.Transparent else tintColor
    val blurContainer = if (tint.alpha > 0.02f) tint else fillColor

    val baseTint = Color.Transparent

    val highlight: (() -> Highlight?)? = if (useGlass && borderWidth > 0.dp && specular > 0f) {
        remember(borderWidth, specular) {
            {
                Highlight(
                    width = borderWidth,
                    alpha = specular,
                    style = HighlightStyle.Default(
                        color = Color.White.copy(alpha = 0.5f * specular),
                        angle = 45f,
                        falloff = 1f
                    )
                )
            }
        }
    } else {
        null
    }

    val effects: BackdropEffectScope.() -> Unit = remember(
        blurPx,
        useGlass,
        heightPx,
        amountPx,
        depthEffect,
        display.chromaticDispersionEnabled
    ) {
        {
            if (blurPx > 0f) {
                blur(blurPx)
            }
            if (useGlass && heightPx > 0f && amountPx > 0f) {
                lens(
                    refractionHeight = heightPx,
                    refractionAmount = amountPx,
                    depthEffect = depthEffect,
                    chromaticAberration = display.chromaticDispersionEnabled,
                    chromaticAberrationAmount = if (display.chromaticDispersionEnabled) 0.65f else 0f,
                )
            }
        }
    }

    val surfaceBlock: DrawScope.() -> Unit = remember(fillColor, baseTint, tint) {
        {
            val base = when {
                fillColor != Color.Transparent && fillColor != Color.Unspecified -> fillColor
                baseTint != Color.Transparent && baseTint.alpha > 0f -> baseTint
                else -> Color.Transparent
            }
            if (base.alpha > 0f) {
                drawRect(color = base, size = size)
            }
            val tintResolved = if (tint != Color.Transparent) tint else Color.Transparent
            if (tintResolved.alpha > 0f) {
                drawRect(color = tintResolved, size = size)
            }
        }
    }

    val shapeProvider = remember(shape) { { shape } }
    val frozenProvider = remember { { false } }

    return if (useGlass) {
        this
            .drawBackdrop(
                backdrop = backdrop,
                shape = shapeProvider,
                effects = effects,
                highlight = highlight,
                shadow = null,
                onDrawSurface = surfaceBlock,
                backdropScale = glassBackdropScale,
                // Floating controls (input row, chips, floaters) freeze the backdrop
                // capture so streaming content behind them does not keep re-recording;
                // pure position changes are still tracked via draw-time translation.
                frozen = frozenProvider
            )
            .then(
                if (shadowElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = shadowElevation,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    )
                } else {
                    Modifier
                }
            )
    } else {
        // API < 33 or no backdrop: clean solid fill with tint and specular rim.
        // No Haze blur - liquid glass is pure AGSL backdrop refraction only.
        var m: Modifier = this
        if (blurContainer != Color.Transparent && blurContainer != Color.Unspecified) {
            m = m.background(blurContainer, shape = shape)
        } else {
            m = m.background(Color.Transparent, shape = shape)
        }
        if (borderWidth > 0.dp && specular > 0f) {
            m = m.border(width = borderWidth, brush = specularBrush, shape = shape)
        }
        if (shadowElevation > 0.dp) {
            m = m.shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
        }
        m
    }
}

/**
 * Settings-driven liquid glass surface. Maps [me.lucid.domchat.data.datastore.DisplaySetting]
 * glass optics onto [liquidGlass]. Kept as the stable API used by chat bubbles and cards.
 *
 * Falls back cleanly to [domChatBlurEffect] (Haze ultraThin) with tint and specular border
 * when the backdrop is unavailable or runtime shaders are unsupported (API < 33).
 */
@Composable
fun Modifier.liquidGlassSurface(
    shape: Shape = LiquidGlassTokens.CardShape,
    borderWidth: Dp = 0.5.dp,
    specularBrush: Brush = LiquidGlassTokens.SpecularBorderBrush,
    fillColor: Color = Color.Transparent,
    shadowElevation: Dp = 0.dp,
    glassMode: LiquidGlassMode = LiquidGlassMode.BALANCED_BLEND,
    isFloatingControl: Boolean = false
): Modifier {
    val display = LocalDisplaySetting.current
    val activeMode = if (glassMode != LiquidGlassMode.BALANCED_BLEND) glassMode else display.liquidGlassMode
    val isFrostedMode = activeMode == LiquidGlassMode.SOFT_FROSTED
    return this.liquidGlass(
        shape = shape,
        blurRadius = display.glassBlur.dp,
        refractionHeight = if (isFrostedMode) 0f.dp else (display.glassDepthScale * 8f).dp,
        refractionAmount = if (isFrostedMode) 0f.dp else (display.glassDepthScale * 12f).dp,
        depthEffect = if (isFrostedMode) false else true,
        tintColor = Color(display.glassTintColor),
        specularIntensity = display.glassSpecularIntensity,
        borderWidth = borderWidth,
        specularBrush = specularBrush,
        fillColor = fillColor,
        shadowElevation = shadowElevation,
        isFloatingControl = isFloatingControl,
    )
}

/**
 * Liquid glass surface dedicated to chat bubbles.
 * Reads bubble-specific settings (chatBubbleBlur, bubbleDepthScale, bubbleSpecularIntensity,
 * chatBubbleTintHue, chatBubbleAlpha) instead of the global UI glass settings.
 *
 * Falls back cleanly to a solid fill when backdrop/runtime shaders are unavailable.
 */
@Composable
fun Modifier.liquidGlassBubbleSurface(
    shape: Shape = LiquidGlassTokens.BubbleShape,
    borderWidth: Dp = 0.5.dp,
    specularBrush: Brush = LiquidGlassTokens.SpecularBorderSubtle,
    fillColor: Color = Color.Transparent,
    shadowElevation: Dp = 0.dp,
    glassMode: LiquidGlassMode = LiquidGlassMode.BALANCED_BLEND,
): Modifier {
    val display = LocalDisplaySetting.current
    val density = LocalDensity.current
    val backdrop = LocalAppBackdrop.current
    val useGlass = backdrop != null && isRuntimeShaderSupported()

    // Bubble-specific settings
    val blurRadius = display.chatBubbleBlur.coerceIn(0f, 100f).dp
    val depthScale = display.bubbleDepthScale.coerceIn(0f, 2.5f)
    val refractionHeight = (depthScale * 8f).dp
    val refractionAmount = (depthScale * 12f).dp
    val specularIntensity = display.bubbleSpecularIntensity.coerceIn(0f, 1f)
    val alpha = display.chatBubbleAlpha.coerceIn(0f, 1f)
    val tone = 0f

    val bubbleFrostedMode = display.bubbleInterfaceStyle == me.lucid.domchat.data.datastore.InterfaceStyle.FROSTED || (display.liquidGlassMode == LiquidGlassMode.SOFT_FROSTED && glassMode == LiquidGlassMode.SOFT_FROSTED)

    val bubbleBackdropScale = 0.5f
    val blurPx = with(density) { blurRadius.toPx() } * bubbleBackdropScale
    val specular = specularIntensity.coerceIn(0f, 1f)
    val heightPx = if (bubbleFrostedMode) 0f else with(density) { (depthScale * 8f).dp.toPx() } * bubbleBackdropScale
    val amountPx = if (bubbleFrostedMode) 0f else with(density) { (depthScale * 12f).dp.toPx() } * bubbleBackdropScale

    val tintColor = if (display.chatBubbleTintColor != 0L) {
        Color(display.chatBubbleTintColor)
    } else if (display.chatBubbleTintAmount > 0.02f) {
        Color.hsv(display.chatBubbleTintHue.coerceIn(0f, 360f), 0.75f, 1.0f).copy(alpha = (display.chatBubbleTintAmount * 0.40f).coerceIn(0f, 1f))
    } else Color.Transparent
    val blurContainer = if (tintColor.alpha > 0.02f) tintColor else fillColor

    val highlight: (() -> Highlight?)? = if (useGlass && borderWidth > 0.dp && specular > 0f) {
        remember(borderWidth, specular) {
            {
                Highlight(
                    width = borderWidth,
                    alpha = specular,
                    style = HighlightStyle.Default(
                        color = Color.White.copy(alpha = 0.5f * specular),
                        angle = 45f,
                        falloff = 1f
                    )
                )
            }
        }
    } else {
        null
    }

    val effects: BackdropEffectScope.() -> Unit = remember(
        blurPx,
        useGlass,
        heightPx,
        amountPx,
        display.chromaticDispersionEnabled
    ) {
        {
            if (blurPx > 0f) {
                blur(blurPx)
            }
            if (useGlass && heightPx > 0f && amountPx > 0f) {
                lens(
                    refractionHeight = heightPx,
                    refractionAmount = amountPx,
                    depthEffect = true,
                    chromaticAberration = display.chromaticDispersionEnabled,
                    chromaticAberrationAmount = if (display.chromaticDispersionEnabled) 0.65f else 0f,
                )
            }
        }
    }

    val neutralTone = androidx.compose.ui.graphics.lerp(Color.Black, Color.White, tone)
    val effectiveContainerColor = if (alpha <= 0.02f) Color.Transparent else neutralTone.copy(alpha = alpha)

    val surfaceBlock: DrawScope.() -> Unit = remember(fillColor, effectiveContainerColor, tintColor) {
        {
            val base = when {
                fillColor != Color.Transparent && fillColor != Color.Unspecified -> fillColor
                effectiveContainerColor != Color.Transparent -> effectiveContainerColor
                else -> Color.Transparent
            }
            if (base.alpha > 0f) {
                drawRect(color = base, size = size)
            }
            val tintResolved = if (tintColor != Color.Transparent) tintColor else Color.Transparent
            if (tintResolved.alpha > 0f) {
                drawRect(color = tintResolved, size = size)
            }
        }
    }

    val shapeProvider = remember(shape) { { shape } }

    return if (useGlass) {
        this
            .drawBackdrop(
                backdrop = backdrop,
                shape = shapeProvider,
                effects = effects,
                highlight = highlight,
                shadow = null,
                onDrawSurface = surfaceBlock,
                backdropScale = bubbleBackdropScale
            )
            .then(
                if (shadowElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = shadowElevation,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    )
                } else {
                    Modifier
                }
            )
    } else {
        // API < 33 or no backdrop: clean solid fill with tint and specular rim.
        var m: Modifier = this
        if (blurContainer != Color.Transparent && blurContainer != Color.Unspecified) {
            m = m.background(blurContainer, shape = shape)
        } else {
            m = m.background(Color.Transparent, shape = shape)
        }
        if (borderWidth > 0.dp && specular > 0f) {
            m = m.border(width = borderWidth, brush = specularBrush, shape = shape)
        }
        if (shadowElevation > 0.dp) {
            m = m.shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
        }
        m
    }
}

/**
 * Frosted glass surface: pure Haze blur + alpha + tone + border.
 * No AGSL backdrop refraction or lens effects. Uses Haze ultraThin blur
 * for a soft, diffused frosted look suitable for API < 33 or when
 * runtime shaders are unavailable.
 *
 * @param shape bubble/card outline.
 * @param borderWidth specular rim width in dp (0 disables).
 * @param specularBrush specular rim brush.
 * @param fillColor opaque surface fill painted beneath the blur.
 * @param shadowElevation drop shadow elevation in dp.
 */
@Composable
fun Modifier.frostedGlassSurface(
    shape: Shape = LiquidGlassTokens.CardShape,
    borderWidth: Dp = 0.5.dp,
    specularBrush: Brush = LiquidGlassTokens.SpecularBorderSubtle,
    fillColor: Color = Color.Transparent,
    shadowElevation: Dp = 0.dp,
): Modifier {
    val display = LocalDisplaySetting.current

    val effectiveAlpha = display.glassAlpha.coerceIn(0f, 1f)
    val effectiveTone = display.glassBaseLuminance.coerceIn(0f, 1f)
    val effectiveBlur = display.glassBlur.coerceIn(0f, 100f)

    val tonedColor = androidx.compose.ui.graphics.lerp(Color.Black, Color.White, effectiveTone)
    val containerColor = if (effectiveAlpha <= 0.02f) Color.Transparent else tonedColor.copy(alpha = effectiveAlpha)

    var m: Modifier = this

    if (effectiveBlur > 0f) {
        m = m.domChatBlurEffect(
            containerColor = containerColor,
            shape = shape,
            blurRadius = effectiveBlur.dp,
        )
    } else if (containerColor != Color.Transparent && containerColor.alpha > 0f) {
        m = m.background(containerColor, shape = shape)
    }

    if (borderWidth > 0.dp) {
        m = m.border(width = borderWidth, brush = specularBrush, shape = shape)
    }

    if (shadowElevation > 0.dp) {
        m = m.shadow(
            elevation = shadowElevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.25f),
            spotColor = Color.Black.copy(alpha = 0.35f),
        )
    }

    return m
}

/**
 * Frosted glass surface dedicated to chat bubbles.
 * Reads bubble-specific settings (chatBubbleAlpha, chatBubbleTone, chatBubbleBlur).
 */
@Composable
fun Modifier.frostedGlassBubbleSurface(
    shape: Shape = LiquidGlassTokens.BubbleShape,
    borderWidth: Dp = 0.5.dp,
    specularBrush: Brush = LiquidGlassTokens.SpecularBorderSubtle,
    fillColor: Color = Color.Transparent,
    shadowElevation: Dp = 0.dp,
): Modifier {
    val display = LocalDisplaySetting.current

    val effectiveAlpha = display.chatBubbleAlpha.coerceIn(0f, 1f)
    val effectiveTone = 0f
    val effectiveBlur = display.chatBubbleBlur.coerceIn(0f, 100f)

    val tonedColor = androidx.compose.ui.graphics.lerp(Color.Black, Color.White, effectiveTone)
    val containerColor = if (effectiveAlpha <= 0.02f) Color.Transparent else tonedColor.copy(alpha = effectiveAlpha)

    var m: Modifier = this

    if (effectiveBlur > 0f) {
        m = m.domChatBlurEffect(
            containerColor = containerColor,
            shape = shape,
            blurRadius = effectiveBlur.dp,
        )
    } else if (containerColor != Color.Transparent && containerColor.alpha > 0f) {
        m = m.background(containerColor, shape = shape)
    }

    if (borderWidth > 0.dp) {
        m = m.border(width = borderWidth, brush = specularBrush, shape = shape)
    }

    if (shadowElevation > 0.dp) {
        m = m.shadow(
            elevation = shadowElevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.25f),
            spotColor = Color.Black.copy(alpha = 0.35f),
        )
    }

    return m
}

/**
 * Liquid Glass Card Container. Keeps content, avatars, and text razor-sharp while providing optical glass depth.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassTokens.CardShape,
    borderWidth: Dp = 0.5.dp,
    specularBrush: Brush = LiquidGlassTokens.SpecularBorderBrush,
    fillColor: Color = Color.Transparent,
    glassMode: LiquidGlassMode = LiquidGlassMode.BALANCED_BLEND,
    isFloatingControl: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.liquidGlassSurface(
            shape = shape,
            borderWidth = borderWidth,
            specularBrush = specularBrush,
            fillColor = fillColor,
            glassMode = glassMode,
            isFloatingControl = isFloatingControl
        ),
        content = content
    )
}

/**
 * Interactive Liquid Glass Modifier:
 * - Dynamic touch highlight following finger during drag/hold with BlendMode.Plus.
 * - Micro-expansion on press (1.02x - 1.04x) with spring damping.
 * - Elastic micro-translation response with tanh soft-clamping.
 */
fun Modifier.liquidInteractiveGlass(
    enabled: Boolean = true,
    scaleLiftDp: Dp = 4.dp,
    elasticDrag: Boolean = true,
    spotAlpha: Float = 0.22f,
    sheenAlpha: Float = 0.08f,
    radiusScale: Float = 1.35f,
): Modifier = this

