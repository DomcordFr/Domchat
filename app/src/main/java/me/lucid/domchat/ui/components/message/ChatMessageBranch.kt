package me.lucid.domchat.ui.components.message

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import me.lucid.domchat.R
import me.lucid.domchat.ui.components.dialog.ChatDestructiveConfirmDialog
import me.lucid.ai.core.MessageRole
import me.lucid.ai.ui.MessageNode
import me.lucid.ai.ui.UIMessage
import me.lucid.ai.ui.versionSelectionIndices
import me.lucid.ai.ui.versionSelectionPosition

@Composable
fun ChatMessageBranchSelector(
    node: MessageNode,
    modifier: Modifier = Modifier,
    onUpdate: (MessageNode) -> Unit,
    onGenerateAlternate: (() -> Unit)? = null,
    onDeleteSwipe: ((UIMessage) -> Unit)? = null,
) {
    val versionIndices = remember(node.messages) {
        node.versionSelectionIndices()
    }
    val currentVersionPosition = remember(node.messages, node.selectIndex) {
        node.versionSelectionPosition()
    }
    var branchDeletePending by remember { mutableStateOf<Boolean>(false) }

    val canGenerateAlternate = onGenerateAlternate != null && node.role == MessageRole.ASSISTANT
    val showControls = versionIndices.isNotEmpty() && (versionIndices.size > 1 || canGenerateAlternate)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showControls) {
            val position = currentVersionPosition.coerceAtLeast(0)
            val canGoPrev = position > 0
            val canGoNext = position < versionIndices.lastIndex

            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = stringResource(R.string.previous),
                modifier = Modifier
                    .clip(CircleShape)
                    .alpha(if (canGoPrev) 1f else 0.5f)
                    .clickable(
                        enabled = canGoPrev,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = {
                            val targetIndex = versionIndices.getOrNull(position - 1)
                                ?: return@clickable
                            onUpdate(node.copy(selectIndex = targetIndex))
                        }
                    )
                    .padding(8.dp)
                    .size(16.dp)
            )

            Text(
                text = "${position + 1}/${versionIndices.size}",
                style = MaterialTheme.typography.bodySmall
            )

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = stringResource(R.string.next),
                modifier = Modifier
                    .clip(CircleShape)
                    .alpha(if (canGenerateAlternate) 1f else (if (canGoNext) 1f else 0.5f))
                    .clickable(
                        enabled = canGoNext || canGenerateAlternate,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = {
                            if (canGoNext) {
                                val targetIndex = versionIndices.getOrNull(position + 1)
                                    ?: return@clickable
                                onUpdate(node.copy(selectIndex = targetIndex))
                            } else {
                                onGenerateAlternate?.invoke()
                            }
                        }
                    )
                    .padding(8.dp)
                    .size(16.dp),
            )

            if (versionIndices.size > 1) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete swipe",
                    tint = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current,
                            onClick = {
                                // Require confirmation before destructively removing a swipe branch.
                                branchDeletePending = true
                            }
                        )
                        .padding(6.dp)
                        .size(15.dp)
                )
            }
        }
    }

    // Branch deletion confirmation — explicit confirm required before removing a swipe.
    if (branchDeletePending) {
        ChatDestructiveConfirmDialog(
            visible = true,
            title = "Delete this swipe?",
            message = "This branch will be permanently removed.",
            onConfirm = {
                branchDeletePending = false
                val activeMsg = node.messages.getOrNull(node.selectIndex)
                if (onDeleteSwipe != null && activeMsg != null && node.messages.size > 1) {
                    onDeleteSwipe(activeMsg)
                } else {
                    val updatedMessages = node.messages.toMutableList()
                    if (node.selectIndex in updatedMessages.indices && updatedMessages.size > 1) {
                        updatedMessages.removeAt(node.selectIndex)
                        val nonEmpty = updatedMessages.filter { it.parts.isNotEmpty() }
                        val finalMessages = if (nonEmpty.isNotEmpty()) nonEmpty else updatedMessages
                        val newSelectIndex = (node.selectIndex - 1).coerceIn(0, finalMessages.lastIndex)
                        onUpdate(node.copy(messages = finalMessages, selectIndex = newSelectIndex))
                    }
                }
            },
            onDismiss = { branchDeletePending = false },
        )
    }
}
