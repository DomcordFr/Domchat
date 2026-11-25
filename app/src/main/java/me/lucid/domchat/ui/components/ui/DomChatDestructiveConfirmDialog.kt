package me.lucid.domchat.ui.components.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.background
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.draw.clip
import me.lucid.domchat.R
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.component.backdrop.LocalAppBackdrop
import me.lucid.domchat.ui.component.backdrop.isRuntimeShaderSupported
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.modifier.domChatDialogContainerColor
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassSurface

private const val DELETE_RED = 0xFFE53935
private val DIALOG_HEIGHT = 48.dp
private val BUTTON_CORNER = 16.dp

/**
 * Single irreversible-confirm pattern: floating rounded dialog, clear title,
 * one-line consequence, Cancel + solid red confirm (Delete by default).
 *
 * The dialog is gated by [visible] and animates in/out with scale + fade so the
 * popup never appears or disappears abruptly.
 */
@Composable
fun DomChatDestructiveConfirmDialog(
    visible: Boolean,
    title: String,
    consequence: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(R.string.delete),
    cancelLabel: String = stringResource(R.string.cancel),
) {
    if (!visible) return

    val dialogShape = RoundedCornerShape(20.dp)
    val containerColor = domChatDialogContainerColor()
    val outerBackdrop = LocalAppBackdrop.current
    val settings = LocalSettings.current
    val isFrosted = settings.displaySetting.liquidGlassMode == LiquidGlassMode.SOFT_FROSTED
    val useGlass = outerBackdrop != null && isRuntimeShaderSupported()

    val surfaceModifier: Modifier = if (useGlass) {
        if (isFrosted) {
            Modifier.frostedGlassSurface(
                shape = dialogShape,
                borderWidth = 0.5.dp,
                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                fillColor = Color.Transparent,
            )
        } else {
            Modifier.liquidGlassSurface(
                shape = dialogShape,
                borderWidth = 0.5.dp,
                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                fillColor = Color.Transparent,
                glassMode = LiquidGlassMode.BALANCED_BLEND,
            )
        }
    } else {
        Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, dialogShape)
    }

    var isClosing by remember { mutableStateOf(false) }
    var confirmedAction by remember { mutableStateOf(false) }
    val animState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        animState.targetState = true
    }

    LaunchedEffect(animState.isIdle, animState.currentState, isClosing) {
        if (isClosing && animState.isIdle && !animState.currentState) {
            if (confirmedAction) {
                onConfirm()
            }
            onDismiss()
        }
    }

    val triggerDismiss: () -> Unit = {
        if (!isClosing) {
            isClosing = true
            animState.targetState = false
        }
    }

    val triggerConfirm: () -> Unit = {
        if (!isClosing) {
            confirmedAction = true
            isClosing = true
            animState.targetState = false
        }
    }

    Dialog(
        onDismissRequest = triggerDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        CompositionLocalProvider(LocalAppBackdrop provides outerBackdrop) {
            ApplyDialogWindowBlur()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visibleState = animState,
                    enter = fadeIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)) + scaleIn(
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                        initialScale = 0.9f
                    ),
                    exit = fadeOut(animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)) + scaleOut(
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                        targetScale = 0.9f
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 280.dp, max = 380.dp)
                            .then(surfaceModifier)
                            .clip(dialogShape)
                            .background(if (useGlass) Color.Transparent else containerColor),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Title
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )

                            // Body message
                            Text(
                                text = consequence,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 4,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                // Cancel button: translucent dark surface, subtle white
                                // border, centered white text.
                                Surface(
                                    onClick = triggerDismiss,
                                    shape = RoundedCornerShape(BUTTON_CORNER),
                                    color = Color(0xFF000000).copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.weight(1f).height(DIALOG_HEIGHT),
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = cancelLabel,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp,
                                            ),
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                        )
                                    }
                                }

                                // Confirm button: solid red fill with delete icon.
                                Surface(
                                    onClick = triggerConfirm,
                                    shape = RoundedCornerShape(BUTTON_CORNER),
                                    color = Color(DELETE_RED),
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.weight(1f).height(DIALOG_HEIGHT),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = confirmLabel,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp,
                                            ),
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

