package me.lucid.domchat.ui.components.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import me.lucid.domchat.ui.theme.LiquidGlassTokens

/** Unified near-black glassy fill used by cards in settings and glassy popovers. */
const val DOMCHAT_UNIFIED_CARD_COLOR = 0xFF000000

/**
 * The one true card design (from the "no lorebooks yet" card):
 * near-black translucent fill + razor-thin specular border + 20dp shape.
 * Use everywhere instead of stock Material cards.
 */
@Composable
fun DomChatGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassTokens.CardShape,
    fillAlpha: Float = 0.55f,
    content: @Composable BoxScope.() -> Unit,
) {
    me.lucid.domchat.ui.theme.LiquidGlassCard(
        modifier = modifier,
        shape = shape,
        specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
        fillColor = Color(DOMCHAT_UNIFIED_CARD_COLOR).copy(alpha = fillAlpha.coerceIn(0f, 1f)),
        content = content,
    )
}
