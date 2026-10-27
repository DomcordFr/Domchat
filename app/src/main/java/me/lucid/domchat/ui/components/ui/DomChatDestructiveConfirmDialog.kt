package me.lucid.domchat.ui.components.ui

import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.lucid.domchat.R
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatDialogContainerColor
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.AppSurface
import me.lucid.domchat.ui.theme.LocalDarkMode

/**
 * Single irreversible-confirm pattern: floating rounded dialog, clear title,
 * one-line consequence, Cancel + danger confirm (Delete by default).
 */
@Composable
fun DomChatDestructiveConfirmDialog(
    title: String,
    consequence: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(R.string.delete),
    cancelLabel: String = stringResource(R.string.cancel),
) {
    val containerColor = domChatDialogContainerColor()
    val isDark = LocalDarkMode.current
    me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .then(
                if (isDark) {
                    Modifier.border(
                        0.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        AppShapes.Dialog
                    )
                } else Modifier
            ),
        shape = AppShapes.Dialog,
        containerColor = containerColor,
        titleContentColor = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurface,
        textContentColor = if (isDark) Color(0xFFD1D1D6) else MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = AppSurface.TonalElevation,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = consequence,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelLabel)
            }
        },
    )
}

@Composable
fun AetherDestructiveConfirmDialog(
    title: String,
    consequence: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(R.string.delete),
    cancelLabel: String = stringResource(R.string.cancel),
) = DomChatDestructiveConfirmDialog(
    title = title,
    consequence = consequence,
    onConfirm = onConfirm,
    onDismiss = onDismiss,
    confirmLabel = confirmLabel,
    cancelLabel = cancelLabel,
)
