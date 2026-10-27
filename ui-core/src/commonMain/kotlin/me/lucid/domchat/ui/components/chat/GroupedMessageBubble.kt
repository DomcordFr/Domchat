package me.lucid.domchat.ui.components.chat

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class BubblePosition { SINGLE, FIRST, MIDDLE, LAST }

enum class BubbleRole { USER, ASSISTANT, ACTIVITY }

/** The production DomChat grouped-message container, shared by Android and iOS. */
@Composable
fun GroupedMessageBubble(
    position: BubblePosition,
    role: BubbleRole,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    largeRadius: Dp = 20.dp,
    smallRadius: Dp = 6.dp,
    isTransparent: Boolean = false,
    isGlassy: Boolean = false,
    glassAlpha: Float = 0.55f,
    baseLuminance: Float = 0f,
    tintHue: Float = 0f,
    tintAmount: Float = 0f,
    bubbleTone: Float = 0f,
    bubbleAlpha: Float = 0.55f,
    onClick: (() -> Unit)? = null,
    bubbleModifier: @Composable (Modifier, Shape, Color) -> Modifier = { m, _, _ -> m },
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isTransparent) {
        val resolvedContentColor = contentColor ?: when (role) {
            BubbleRole.USER -> MaterialTheme.colorScheme.onPrimaryContainer
            BubbleRole.ASSISTANT, BubbleRole.ACTIVITY -> MaterialTheme.colorScheme.onSurface
        }
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides resolvedContentColor
        ) {
            Column(
                modifier = modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = onClick != null,
                        onClick = { onClick?.invoke() }
                    )
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                content = content
            )
        }
        return
    }

    val tone = bubbleTone.coerceIn(0f, 1f)
    val effectiveAlpha = bubbleAlpha.coerceIn(0f, 1f)
    val neutralTone = lerp(Color.Black, Color.White, tone)
    val baseTone = if (tintAmount > 0.02f) {
        val hsvTint = Color.hsv(tintHue.coerceIn(0f, 360f), 0.75f, 1.0f)
        lerp(neutralTone, hsvTint, tintAmount.coerceIn(0f, 1f))
    } else {
        neutralTone
    }

    val defaultContainerColor = when (role) {
        BubbleRole.USER -> if (isGlassy) {
            if (effectiveAlpha <= 0.02f) Color.Transparent else baseTone.copy(alpha = effectiveAlpha)
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
        BubbleRole.ASSISTANT -> if (isGlassy) {
            if (effectiveAlpha <= 0.02f) Color.Transparent else baseTone.copy(alpha = effectiveAlpha)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
        BubbleRole.ACTIVITY -> if (isGlassy) {
            if (effectiveAlpha <= 0.02f) Color.Transparent else baseTone.copy(alpha = (effectiveAlpha * 0.8f).coerceIn(0.1f, 0.7f))
        } else {
            Color(0xFF141520).copy(alpha = 0.60f)
        }
    }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val defaultContentColor = when {
        effectiveAlpha <= 0.05f -> if (isDark) Color(0xFFF2F2F7) else Color(0xFF14151C)
        tone >= 0.55f -> Color(0xFF14151C)
        else -> Color(0xFFF2F2F7)
    }
    val isLeftAligned = role != BubbleRole.USER
    val shape = when (position) {
        BubblePosition.SINGLE -> RoundedCornerShape(largeRadius)
        BubblePosition.FIRST -> if (isLeftAligned) {
            RoundedCornerShape(
                topStart = largeRadius, topEnd = largeRadius,
                bottomEnd = largeRadius, bottomStart = smallRadius,
            )
        } else {
            RoundedCornerShape(
                topStart = largeRadius, topEnd = largeRadius,
                bottomEnd = smallRadius, bottomStart = largeRadius,
            )
        }
        BubblePosition.MIDDLE -> if (isLeftAligned) {
            RoundedCornerShape(
                topStart = smallRadius, topEnd = largeRadius,
                bottomEnd = largeRadius, bottomStart = smallRadius,
            )
        } else {
            RoundedCornerShape(
                topStart = largeRadius, topEnd = smallRadius,
                bottomEnd = smallRadius, bottomStart = largeRadius,
            )
        }
        BubblePosition.LAST -> if (isLeftAligned) {
            RoundedCornerShape(
                topStart = smallRadius, topEnd = largeRadius,
                bottomEnd = largeRadius, bottomStart = largeRadius,
            )
        } else {
            RoundedCornerShape(
                topStart = largeRadius, topEnd = smallRadius,
                bottomEnd = largeRadius, bottomStart = largeRadius,
            )
        }
    }
    val bubbleContent: @Composable () -> Unit = {
        // Roomier sides than before: horizontal inset matches the airier top/bottom
        // rhythm (bubble padding + paragraph spacing), so text, images, code blocks
        // and tables all sit evenly inside the bubble.
        Column(modifier = Modifier.padding(horizontal = if (isTransparent) 4.dp else 16.dp, vertical = if (isTransparent) 6.dp else 12.dp), content = content)
    }
    val specularBorder = if (isTransparent) null else androidx.compose.foundation.BorderStroke(
        if (effectiveAlpha <= 0.05f) 0.75.dp else 0.5.dp,
        androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(
                androidx.compose.ui.graphics.Color.White.copy(alpha = if (effectiveAlpha <= 0.05f) 0.40f else if (isGlassy) 0.35f else 0.25f),
                androidx.compose.ui.graphics.Color.White.copy(alpha = if (effectiveAlpha <= 0.05f) 0.18f else if (isGlassy) 0.12f else 0.06f),
                androidx.compose.ui.graphics.Color.White.copy(alpha = if (effectiveAlpha <= 0.05f) 0.08f else 0f)
            )
        )
    )
    val finalContainerColor = if (isTransparent) Color.Transparent else (containerColor ?: defaultContainerColor)
    val appliedModifier = bubbleModifier(modifier, shape, finalContainerColor)
    val surfaceColor = if (isGlassy) Color.Transparent else finalContainerColor

    if (onClick != null) {
        Surface(
            modifier = appliedModifier,
            shape = shape,
            color = surfaceColor,
            contentColor = contentColor ?: defaultContentColor,
            border = specularBorder,
            onClick = onClick,
            content = bubbleContent,
        )
    } else {
        Surface(
            modifier = appliedModifier,
            shape = shape,
            color = surfaceColor,
            contentColor = contentColor ?: defaultContentColor,
            border = specularBorder,
            content = bubbleContent,
        )
    }
}

fun getBubblePosition(index: Int, total: Int): BubblePosition = when {
    total == 1 -> BubblePosition.SINGLE
    index == 0 -> BubblePosition.FIRST
    index == total - 1 -> BubblePosition.LAST
    else -> BubblePosition.MIDDLE
}
