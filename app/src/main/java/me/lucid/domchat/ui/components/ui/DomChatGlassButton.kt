package me.lucid.domchat.ui.components.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.liquidGlassSurface

private const val DOMCHAT_DESTRUCTIVE = 0xFFE53935.toInt()

/**
 * Dark glassy pill button (enable/disable actions, saves, resets).
 * Replaces stock Material TextButtons/Buttons/Badges that render grey or red.
 */
@Composable
fun DomChatGlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    destructive: Boolean = false,
    accent: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
    containerColor: Color? = null,
    contentColor: Color? = null,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val haptics = rememberPremiumHaptics()
    DomChatGlassPillButtonContent(
        onClick = {
            haptics.perform(HapticPattern.Pop)
            onClick()
        },
        modifier = modifier,
        icon = icon,
        destructive = destructive,
        accent = accent,
        enabled = enabled,
        contentPadding = contentPadding,
        containerColor = containerColor,
        contentColor = contentColor,
        content = content,
        text = text,
    )
}

/**
 * Dark glassy pill button.
 * Default (no containerColor) is a translucent frosted glass surface for chat pages
 * (no solid white, no solid black). Settings pages can pass `containerColor = Color(0xFF000000)`
 * for a black glass + specular border variant.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DomChatGlassPillButtonContent(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    destructive: Boolean = false,
    accent: Boolean = false,
    contentPadding: PaddingValues,
    containerColor: Color? = null,
    contentColor: Color? = null,
    text: String,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val shape = AppShapes.ButtonPill
    val resolvedTextColor = contentColor.takeIf { it != null } ?: Color.White
    val resolvedIconTint = resolvedTextColor
    val borderBrush = LiquidGlassTokens.SpecularBorderSubtle
    val glassFill = containerColor.takeIf { it != null } ?: Color.Transparent
    val surfaceModifier = if (destructive) {
        modifier.border(0.5.dp, borderBrush, shape)
    } else {
        modifier.liquidGlassSurface(
            shape = shape,
            fillColor = if (enabled) glassFill else glassFill.copy(alpha = glassFill.alpha * 0.4f),
            borderWidth = 0.5.dp,
            specularBrush = borderBrush,
        )
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            color = if (destructive) {
                val dColor = Color(DOMCHAT_DESTRUCTIVE)
                if (enabled) dColor else dColor.copy(alpha = 0.5f)
            } else {
                Color.Transparent
            },
            tonalElevation = 0.dp,
            modifier = surfaceModifier,
        ) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = resolvedIconTint
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    ),
                    color = when {
                        !enabled -> resolvedTextColor.copy(alpha = 0.4f)
                        else -> resolvedTextColor
                    },
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
                content?.invoke(this)
            }
        }
    }
}

/** Small dark glassy badge/pill for status text ("Chat Specific", model tags, etc.). */
@Composable
fun DomChatGlassChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    val chipFill = Color.Transparent
    Surface(
        color = Color.Transparent,
        shape = AppShapes.Tag,
        tonalElevation = 0.dp,
        modifier = modifier.liquidGlassSurface(
            shape = AppShapes.Tag,
            borderWidth = 0.5.dp,
            specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
            fillColor = chipFill,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (accent) Color.White else Color.White.copy(alpha = 0.85f),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            maxLines = 1,
            softWrap = false,
        )
    }
}
