package me.lucid.domchat.ui.components.dialog

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassSurface

private const val DELETE_RED = 0xFFE53935
private val DIALOG_HEIGHT = 48.dp
private val BUTTON_CORNER = 16.dp

/**
 * Button content shared by every destructive confirmation dialog:
 * - Delete: solid red fill ([DELETE_RED]), rounded corners, delete icon + 15.dp SemiBold white
 *   text, vertically & horizontally centered.
 * - Cancel: translucent dark surface with a subtle white specular border, centered
 *   white text.
 */
@Composable
private fun DestructiveConfirmButtons(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String = "Delete",
    cancelLabel: String = "Cancel",
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Cancel button: translucent glass / dark surface, subtle white border,
        // centered white text.
        Surface(
            onClick = onCancel,
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

        // Delete button: full solid red background, delete icon + white text.
        Surface(
            onClick = onConfirm,
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

/**
 * A reusable, smooth-opening destructive action confirmation dialog.
 *
 * Features:
 * - Centered on screen, capped at [widthIn(max = 380.dp)] so it never clips at the
 *   edges or gets cut off at the bottom on small screens.
 * - Single AMOLED black glass surface ([Color(0xFF000000)]) with a 0.5dp specular
 *   border ([LiquidGlassTokens.SpecularBorderSubtle]) — no nested/dual backgrounds.
 * - Smooth scale + fade entry/exit animations.
 * - Title and body message.
 * - Cancel button: glassy outlined, centered white text.
 * - Delete button: prominent solid red button with delete icon and pure white text.
 *
 * @param visible Whether the dialog is currently shown.
 * @param title The dialog title (e.g. "Delete conversation?").
 * @param message The explanatory body text.
 * @param onConfirm Called when the user confirms the destructive action.
 * @param onDismiss Called when the dialog should be dismissed (cancel or backdrop press).
 * @param confirmLabel Label for the destructive button (default "Delete").
 * @param cancelLabel Label for the cancel button (default "Cancel").
 */
@Composable
fun UniversalDestructiveConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "Delete",
    cancelLabel: String = "Cancel",
) {
    if (!visible) return

    val dialogShape = RoundedCornerShape(20.dp)
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
                Surface(
                    shape = dialogShape,
                    color = Color(0xFF000000),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier.widthIn(min = 280.dp, max = 380.dp),
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
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 4,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )

                        DestructiveConfirmButtons(
                            onCancel = triggerDismiss,
                            onConfirm = triggerConfirm,
                            confirmLabel = confirmLabel,
                            cancelLabel = cancelLabel,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Confirmation dialog for chat pages: translucent liquid-glass surface over the
 * conversation backdrop (frosted glass when [LiquidGlassMode.SOFT_FROSTED]),
 * transparent fill, specular border, centered text, solid red delete button with delete icon.
 */
@Composable
fun ChatDestructiveConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "Delete",
    cancelLabel: String = "Cancel",
) {
    if (!visible) return

    val dialogShape = RoundedCornerShape(20.dp)
    val isFrosted = LocalSettings.current.displaySetting.liquidGlassMode == LiquidGlassMode.SOFT_FROSTED
    val surfaceModifier: Modifier = if (isFrosted) {
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
                        .clip(dialogShape),
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
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 4,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )

                        DestructiveConfirmButtons(
                            onCancel = triggerDismiss,
                            onConfirm = triggerConfirm,
                            confirmLabel = confirmLabel,
                            cancelLabel = cancelLabel,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Confirmation dialog for settings pages: opaque AMOLED black surface,
 * [RoundedCornerShape(20.dp)], specular border, solid red delete button —
 * no greyish background, no grey borders.
 */
@Composable
fun SettingsDestructiveConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "Delete",
    cancelLabel: String = "Cancel",
) {
    if (!visible) return

    val dialogShape = RoundedCornerShape(20.dp)
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
                Surface(
                    shape = dialogShape,
                    color = Color(0xFF000000),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier.widthIn(min = 280.dp, max = 380.dp),
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
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 4,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )

                        DestructiveConfirmButtons(
                            onCancel = triggerDismiss,
                            onConfirm = triggerConfirm,
                            confirmLabel = confirmLabel,
                            cancelLabel = cancelLabel,
                        )
                    }
                }
            }
        }
    }
}

