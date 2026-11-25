
package me.lucid.domchat.ui.components.message

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import kotlinx.coroutines.delay
import kotlinx.datetime.toJavaLocalDateTime
import me.lucid.ai.core.MessageRole
import me.lucid.ai.provider.Model
import me.lucid.ai.ui.UIMessage
import me.lucid.ai.ui.UIMessagePart
import me.lucid.ai.ui.UsedLorebookEntry
import me.lucid.domchat.R
import androidx.compose.runtime.staticCompositionLocalOf

import me.lucid.domchat.data.datastore.getEffectiveDisplaySetting
import me.lucid.ai.ui.MessageNode
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.context.LocalTTSState
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.utils.copyMessageToClipboard
import me.lucid.domchat.utils.toLocalString
import me.lucid.tts.provider.TTSProviderSetting

data class ActiveChatMessageAction(
    val message: UIMessage,
    val model: Model?,
    val onEdit: () -> Unit,
    val onDelete: () -> Unit,
    val onFork: () -> Unit,
    val onSelectAndCopy: () -> Unit,
    val onWebViewPreview: () -> Unit,
)

val LocalActiveChatMessageAction = staticCompositionLocalOf<((ActiveChatMessageAction?) -> Unit)?> { null }

@Composable
fun ColumnScope.ChatMessageActionButtons(
    message: UIMessage,
    node: MessageNode,
    onUpdate: (MessageNode) -> Unit,
    onRegenerate: () -> Unit,
    showRegenerate: Boolean = true,
    onOpenActionSheet: () -> Unit,
    onEditLorebookEntry: ((UsedLorebookEntry) -> Unit)? = null,
    onModeClick: ((me.lucid.ai.ui.UsedMode) -> Unit)? = null,
    onMemoryClick: ((me.lucid.ai.ui.UsedMemory) -> Unit)? = null,
    ttsProviderOverride: TTSProviderSetting? = null,
    onEdit: () -> Unit = {},
    allowVersionSwitching: Boolean = true,
    isIntroMessage: Boolean = false,
    onAddCustomGreeting: (() -> Unit)? = null,
    onDeleteSwipe: ((UIMessage) -> Unit)? = null,
) {
    val context = LocalContext.current
    val settings = LocalSettings.current
    val effectiveDisplay = settings.getEffectiveDisplaySetting()
    var isPendingDelete by remember { mutableStateOf(false) }
    var showContextSheet by remember { mutableStateOf(false) }
    
    val usedEntries = message.usedLorebookEntries ?: emptyList()
    val usedModes = message.usedModes ?: emptyList()
    val usedMemories = message.usedMemories ?: emptyList()
    val hasContextSources = usedEntries.isNotEmpty() || usedModes.isNotEmpty() || usedMemories.isNotEmpty()
    val showContextStacks = effectiveDisplay.showContextStacks && hasContextSources

    LaunchedEffect(isPendingDelete) {
        if (isPendingDelete) {
            delay(3000) // 3秒后自动取消
            isPendingDelete = false
        }
    }
    
    // Context sources sheet
    if (showContextSheet && hasContextSources) {
        ContextSourcesSheet(
            modes = usedModes,
            memories = usedMemories,
            entries = usedEntries,
            onModeClick = { mode ->
                showContextSheet = false
                onModeClick?.invoke(mode)
            },
            onMemoryClick = { memory ->
                showContextSheet = false
                onMemoryClick?.invoke(memory)
            },
            onEntryClick = { entry ->
                showContextSheet = false
                onEditLorebookEntry?.invoke(entry)
            },
            onDismissRequest = { showContextSheet = false }
        )
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        // Context stack indicator at the start
        if (showContextStacks) {
            ContextStackIndicator(
                modes = usedModes,
                memories = usedMemories,
                entries = usedEntries,
                onClick = { showContextSheet = true },
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        
        Icon(
            Icons.Rounded.ContentCopy, stringResource(R.string.copy), modifier = Modifier
                .clip(CircleShape)
                .clickable { context.copyMessageToClipboard(message) }
                .padding(8.dp)
                .size(16.dp)
        )

        if (showRegenerate) {
            Icon(
                Icons.Rounded.Refresh, stringResource(R.string.regenerate), modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onRegenerate() }
                    .padding(8.dp)
                    .size(16.dp)
            )
        }

        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = stringResource(R.string.edit),
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onEdit() }
                .padding(8.dp)
                .size(16.dp)
        )

        Icon(
            imageVector = Icons.Rounded.MoreHoriz,
            contentDescription = stringResource(R.string.more_options),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    onClick = {
                        onOpenActionSheet()
                    }
                )
                .padding(8.dp)
                .size(16.dp)
        )

        if (allowVersionSwitching) {
            ChatMessageBranchSelector(
                node = node,
                onUpdate = onUpdate,
                onGenerateAlternate = if (isIntroMessage) onAddCustomGreeting else if (showRegenerate) onRegenerate else null,
                onDeleteSwipe = if (!isIntroMessage) onDeleteSwipe else null,
            )
        }
    }
}

@Composable
fun ChatMessageActionsSheet(
    message: UIMessage,
    model: Model?,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
    onFork: () -> Unit,
    onSelectAndCopy: () -> Unit,
    onWebViewPreview: () -> Unit,
    ttsProviderOverride: TTSProviderSetting? = null,
    onDismissRequest: () -> Unit
) {
    val haptics = rememberPremiumHaptics()
    val hasTextContent = message.parts.filterIsInstance<UIMessagePart.Text>()
        .any { it.text.isNotBlank() }

    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MessageActionGlassItem(
                label = stringResource(R.string.select_and_copy),
                icon = { Icon(Icons.Rounded.SelectAll, null, modifier = Modifier.size(20.dp)) },
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    onDismissRequest()
                    onSelectAndCopy()
                },
            )
            if (hasTextContent) {
                MessageActionGlassItem(
                    label = stringResource(R.string.render_with_webview),
                    icon = { Icon(Icons.Rounded.OpenInBrowser, null, modifier = Modifier.size(20.dp)) },
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        onDismissRequest()
                        onWebViewPreview()
                    },
                )
            }
            if (message.role == MessageRole.ASSISTANT) {
                val tts = LocalTTSState.current
                val isSpeaking by tts.isSpeaking.collectAsState()
                MessageActionGlassItem(
                    label = if (isSpeaking) "Stop TTS" else stringResource(R.string.tts),
                    icon = {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Rounded.StopCircle else Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        if (!isSpeaking) {
                            tts.speak(
                                text = message.toContentText(),
                                overrideSetting = ttsProviderOverride,
                            )
                        } else {
                            tts.stop()
                        }
                        onDismissRequest()
                    }
                )
            }
            MessageActionGlassItem(
                label = stringResource(R.string.create_fork),
                icon = { Icon(Icons.AutoMirrored.Rounded.CallSplit, null, modifier = Modifier.size(20.dp)) },
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    onDismissRequest()
                    onFork()
                },
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                color = Color.White.copy(alpha = 0.12f),
                thickness = 0.5.dp,
            )

            MessageActionGlassItem(
                label = stringResource(R.string.delete),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White.copy(alpha = 0.9f),
                    )
                },
                destructive = true,
                onClick = {
                    haptics.perform(HapticPattern.Thud)
                    onDismissRequest()
                    onDelete()
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Message Info
            ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                val ldt = message.createdAt.toJavaLocalDateTime()
                val hour12 = if (ldt.hour % 12 == 0) 12 else ldt.hour % 12
                val amPm = if (ldt.hour < 12) "AM" else "PM"
                val monthName = ldt.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                val formattedTime = "%s %d, %d, %d:%02d %s".format(
                    monthName,
                    ldt.dayOfMonth,
                    ldt.year,
                    hour12,
                    ldt.minute,
                    amPm
                )
                Text(formattedTime)
                if (model != null) {
                    Text(model.displayName)
                }
            }
        }
    }
}

@Composable
private fun MessageActionGlassItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    destructive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberPremiumHaptics()
    val isDark = LocalDarkMode.current
    val itemShape = RoundedCornerShape(16.dp)

    Surface(
        onClick = {
            haptics.perform(if (destructive) HapticPattern.Thud else HapticPattern.Pop)
            onClick()
        },
        shape = itemShape,
        color = if (destructive) Color(0xFFE53935) else Color.Transparent,
        border = if (destructive) null else BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!destructive) {
                    Modifier.liquidGlassSurface(
                        shape = itemShape,
                        borderWidth = 0.5.dp,
                        specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                        fillColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.25f),
                    )
                } else Modifier
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}
