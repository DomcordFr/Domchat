package me.lucid.domchat.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.lucid.domchat.data.model.LiquidGlassMode

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

    // Translucent glass fills with optical depth - luminous and crystal transparent in dark mode
    val GlassSurfaceDark = Color(0xFF141520).copy(alpha = 0.22f)
    val GlassSurfaceLight = Color(0xFFFFFFFF).copy(alpha = 0.45f)
    val BubbleGlassDark = Color(0xFFFFFFFF).copy(alpha = 0.06f)
    val BubbleGlassLight = Color(0xFF000000).copy(alpha = 0.03f)
}

/**
 * Modifier that applies clean liquid glass surface optics without blurring foreground children.
 */
fun Modifier.liquidGlassSurface(
    shape: Shape = LiquidGlassTokens.CardShape,
    borderWidth: Dp = 0.5.dp,
    specularBrush: Brush = LiquidGlassTokens.SpecularBorderBrush,
    fillColor: Color = Color.Transparent,
    shadowElevation: Dp = 0.dp,
    glassMode: LiquidGlassMode = LiquidGlassMode.BALANCED_BLEND,
    isFloatingControl: Boolean = false
): Modifier {
    val withShadow = if (shadowElevation > 0.dp) {
        this.shadow(
            elevation = shadowElevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.25f),
            spotColor = Color.Black.copy(alpha = 0.35f)
        )
    } else {
        this
    }
    val withBg = if (fillColor != Color.Transparent && fillColor != Color.Unspecified) {
        withShadow.background(
            color = fillColor,
            shape = shape
        )
    } else {
        withShadow
    }
    return withBg
        .border(
            width = borderWidth,
            brush = specularBrush,
            shape = shape
        )
        .clip(shape)
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
    fillColor: Color = me.lucid.domchat.ui.modifier.domChatCardContainerColor(),
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
