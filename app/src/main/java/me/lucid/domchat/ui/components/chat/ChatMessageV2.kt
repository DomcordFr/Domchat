package me.lucid.domchat.ui.components.chat

import me.lucid.domchat.ui.context.LocalChatAnimationsEnabled
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.theme.frostedGlassBubbleSurface
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassBubbleSurface
import me.lucid.domchat.ui.theme.liquidInteractiveGlass
import me.lucid.domchat.utils.safeDrawableResId
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.core.net.toUri
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import me.lucid.ai.core.MessageRole
import me.lucid.ai.core.TokenUsage
import me.lucid.ai.provider.Model
import me.lucid.ai.ui.ToolApprovalState
import me.lucid.ai.ui.UIMessage
import me.lucid.ai.ui.UIMessageAnnotation
import me.lucid.ai.ui.UIMessagePart
import me.lucid.domchat.R
import me.lucid.domchat.Screen
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.datastore.InterfaceStyle
import me.lucid.domchat.data.model.AssistantAffectScope
import me.lucid.domchat.data.model.ChatAttachmentState
import me.lucid.ai.ui.MessageNode
import me.lucid.domchat.data.model.chatAttachmentDisplayName
import me.lucid.domchat.data.model.chatAttachmentMimeHint
import me.lucid.domchat.data.model.chatAttachmentState
import me.lucid.domchat.data.model.replacePersonaPlaceholders
import me.lucid.domchat.data.model.replaceRegexes
import me.lucid.ai.ui.versionSelectionIndices
import me.lucid.ai.ui.versionSelectionPosition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import me.lucid.domchat.data.ai.tools.parseJsonElementWithRecovery
import me.lucid.domchat.ui.components.message.ChatMessageActionButtons
import me.lucid.domchat.ui.components.message.ChatMessageActionsSheet
import me.lucid.domchat.ui.components.message.ChatMessageCopySheet
import me.lucid.domchat.ui.components.message.ActiveChatMessageAction
import me.lucid.domchat.ui.components.message.LocalActiveChatMessageAction
import me.lucid.domchat.ui.components.richtext.buildMarkdownPreviewHtml
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.components.richtext.ZoomableAsyncImage
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.ui.context.LocalDisplaySetting
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.utils.base64Encode
import me.lucid.domchat.utils.copyMessageToClipboard
import me.lucid.domchat.utils.formatNumber
import me.lucid.domchat.utils.getFileMimeType
import me.lucid.domchat.utils.getFileNameFromUri
import me.lucid.domchat.utils.jsonPrimitiveOrNull
import me.lucid.domchat.utils.openAttachmentUri
import me.lucid.domchat.data.datastore.withAssistantOverrides
import me.lucid.domchat.data.datastore.getEffectiveTTSProvider
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.ai.core.MessageRole as AIMessageRole

private val rpFormattedTextCache = object : android.util.LruCache<String, String>(2 * 1024 * 1024) {
    override fun sizeOf(key: String, value: String): Int =
        ((key.length + value.length) * 2).coerceAtLeast(256)
}

internal fun clearRpFormattedTextCache() {
    rpFormattedTextCache.evictAll()
}

/**
 * Smoothly animates vertical height changes using a LayoutModifierNode without
 * hard clipping via clipToBounds(). Snaps immediately on the first measurement
 * and resets cleanly when recycled by LazyColumn, avoiding layout-phase state
 * mutations or snapshotFlow frame lag on line wraps.
 */
private fun Modifier.smoothExpandingHeight(enabled: Boolean = true): Modifier =
    if (enabled) this.then(SmoothExpandingHeightElement) else this

private object SmoothExpandingHeightElement : ModifierNodeElement<SmoothExpandingHeightNode>() {
    override fun create(): SmoothExpandingHeightNode = SmoothExpandingHeightNode()
    override fun update(node: SmoothExpandingHeightNode) = Unit
    override fun InspectorInfo.inspectableProperties() {
        name = "smoothExpandingHeight"
    }
    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = System.identityHashCode(this)
}

private class SmoothExpandingHeightNode : Modifier.Node(), LayoutModifierNode {
    private data class AnimData(
        val anim: Animatable<Float, AnimationVector1D>,
        var targetHeight: Int,
    )

    private var animData: AnimData? = null

    override fun onReset() {
        super.onReset()
        animData = null
    }

    override fun onDetach() {
        super.onDetach()
        animData = null
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        val targetHeight = placeable.height
        if (isLookingAhead) {
            return layout(placeable.width, targetHeight) {
                placeable.placeRelative(0, 0)
            }
        }
        val data = animData
        if (data == null) {
            animData = AnimData(Animatable(targetHeight.toFloat()), targetHeight)
            return layout(placeable.width, targetHeight) {
                placeable.placeRelative(0, 0)
            }
        }
        if (targetHeight != data.targetHeight) {
            data.targetHeight = targetHeight
            coroutineScope.launch {
                data.anim.animateTo(
                    targetValue = targetHeight.toFloat(),
                    animationSpec = spring(
                        dampingRatio = 0.76f,
                        stiffness = if (targetHeight > data.anim.value) 260f else 300f,
                    ),
                )
            }
        }
        val displayHeight = data.anim.value.roundToInt().coerceAtLeast(0)
        return layout(placeable.width, displayHeight) {
            placeable.placeRelative(0, 0)
        }
    }
}

@Composable
private fun rememberFormattedMessageText(
    rawText: String,
    assistant: me.lucid.domchat.data.model.Assistant?,
    userNickname: String,
    scope: me.lucid.domchat.data.model.AssistantAffectScope,
    isStreaming: Boolean
): String {
    if (!isStreaming) {
        val assistantKey = remember(assistant, scope) {
            if (assistant == null) {
                "null"
            } else {
                val regexSig = assistant.regexes.hashCode()
                "${assistant.id}:${assistant.effectiveChatName}:$regexSig"
            }
        }
        return remember(rawText, assistantKey, userNickname, scope) {
            val cacheKey = "${scope.ordinal}:${userNickname.hashCode()}:${assistantKey.hashCode()}:${rawText.length}:${rawText.hashCode()}:$rawText"
            rpFormattedTextCache.get(cacheKey) ?: formatRoleplayTextWithCdnImages(
                cleanHtmlImgToMarkdown(
                    rawText.trimStart()
                        .replacePersonaPlaceholders(
                            assistant = assistant,
                            userNickname = userNickname,
                        )
                        .replaceRegexes(
                            assistant = assistant,
                            scope = scope,
                            visual = true,
                        )
                )
            ).also { formatted ->
                if (cacheKey.length <= 16_384) {
                    rpFormattedTextCache.put(cacheKey, formatted)
                }
            }
        }
    }

    val needsFormatting = remember(assistant, rawText) {
        !assistant?.regexes.isNullOrEmpty() ||
            rawText.contains("<img", ignoreCase = true) ||
            rawText.contains("{{") ||
            rawText.contains("<char>", ignoreCase = true) ||
            rawText.contains("<user>", ignoreCase = true) ||
            rawText.contains("http", ignoreCase = true)
    }
    if (!needsFormatting) {
        return rawText.trimStart()
    }

    val initial = remember { rawText.replacePersonaPlaceholders(assistant, userNickname) }

    return produceState(
        initial,
        rawText,
        assistant,
        userNickname,
        scope,
    ) {
        val formatted = withContext(Dispatchers.Default) {
            formatRoleplayTextWithCdnImages(
                cleanHtmlImgToMarkdown(
                    rawText.trimStart()
                        .replacePersonaPlaceholders(
                            assistant = assistant,
                            userNickname = userNickname,
                        )
                        .replaceRegexes(
                            assistant = assistant,
                            scope = scope,
                            visual = true,
                        )
                )
            )
        }
        value = formatted
    }.value
}

/**
 * Represents a group of consecutive messages from the same role.
 * For assistant messages, this groups all consecutive assistant nodes together.
 */
@Immutable
data class MessageTurnGroup(
    val nodes: List<MessageNode>,
    val role: MessageRole
) {
    val firstNode get() = nodes.first()
    val lastNode get() = nodes.last()
    
    /** Node with the most distinct message versions - used for version switching controls */
    val nodeWithMostVersions by lazy { nodes.maxByOrNull { it.versionSelectionIndices().size } ?: lastNode }
    
    /** The active versionTag from the first node's current message */
    val activeVersionTag: String? by lazy { firstNode.currentMessage.versionTag }
    
    /** 
     * Nodes filtered to only include those with a message matching the active versionTag.
     * For backwards compatibility, if no versionTag exists on the active version, 
     * we show nodes that have at least one null-tagged message (pre-versioning nodes).
     * 
     * IMPORTANT: This returns nodes with their selectIndex adjusted to point to the 
     * message matching the active versionTag, not the original currentMessage.
     */
    val filteredNodes: List<MessageNode> by lazy {
        val tag = activeVersionTag
        val selectedNodes = nodes.mapNotNull { node ->
            val currentIndexMatchesTag = node.messages
                .getOrNull(node.selectIndex)
                ?.versionTag == tag
            val matchingIndex = if (currentIndexMatchesTag) {
                // Keep the node's active selection when it already matches this turn's tag.
                node.selectIndex
            } else {
                // Fall back to the newest matching message (important for same-tag edits).
                node.messages.indexOfLast { it.versionTag == tag }
            }

            if (matchingIndex != -1) {
                node.copy(selectIndex = matchingIndex)
            } else {
                null
            }
        }
        val activeToolCallIds = selectedNodes
            .flatMap { node -> node.currentMessage.getToolCalls() }
            .map { it.toolCallId }
            .toSet()
        if (activeToolCallIds.isEmpty()) {
            selectedNodes
        } else {
            val selectedNodeIds = selectedNodes.map { it.id }.toSet()
            val matchingToolResultNodes = nodes.mapNotNull { node ->
                if (node.id in selectedNodeIds || node.role != MessageRole.TOOL) {
                    null
                } else {
                    val matchingIndex = node.messages.indexOfLast { message ->
                        message.getToolResults().any { result -> result.toolCallId in activeToolCallIds }
                    }
                    if (matchingIndex >= 0) {
                        node.copy(selectIndex = matchingIndex)
                    } else {
                        null
                    }
                }
            }

            (selectedNodes + matchingToolResultNodes).sortedBy { node ->
                nodes.indexOfFirst { it.id == node.id }.takeIf { it >= 0 } ?: Int.MAX_VALUE
            }
        }
    }

    fun filteredNodesForVersion(versionSelectIndex: Int): List<MessageNode> {
        val anchorNode = nodeWithMostVersions
        val safeIndex = versionSelectIndex.coerceIn(0, anchorNode.messages.lastIndex.coerceAtLeast(0))
        val targetMsg = anchorNode.messages.getOrNull(safeIndex) ?: anchorNode.currentMessage
        val tag = targetMsg.versionTag
        val selectedNodes = nodes.mapNotNull { node ->
            if (node.id == anchorNode.id) {
                node.copy(selectIndex = safeIndex)
            } else {
                val currentIndexMatchesTag = node.messages
                    .getOrNull(node.selectIndex)
                    ?.versionTag == tag
                val matchingIndex = if (currentIndexMatchesTag) {
                    node.selectIndex
                } else {
                    node.messages.indexOfLast { it.versionTag == tag }
                }
                if (matchingIndex != -1) {
                    node.copy(selectIndex = matchingIndex)
                } else {
                    null
                }
            }
        }
        val activeToolCallIds = selectedNodes
            .flatMap { node -> node.currentMessage.getToolCalls() }
            .map { it.toolCallId }
            .toSet()
        if (activeToolCallIds.isEmpty()) {
            return selectedNodes
        }
        val selectedNodeIds = selectedNodes.map { it.id }.toSet()
        val matchingToolResultNodes = nodes.mapNotNull { node ->
            if (node.id in selectedNodeIds || node.role != MessageRole.TOOL) {
                null
            } else {
                val matchingIndex = node.messages.indexOfLast { message ->
                    message.getToolResults().any { result -> result.toolCallId in activeToolCallIds }
                }
                if (matchingIndex >= 0) {
                    node.copy(selectIndex = matchingIndex)
                } else {
                    null
                }
            }
        }
        return (selectedNodes + matchingToolResultNodes).sortedBy { node ->
            nodes.indexOfFirst { it.id == node.id }.takeIf { it >= 0 } ?: Int.MAX_VALUE
        }
    }
    
    /** All message parts from filtered nodes in the group */
    val allParts: List<UIMessagePart> by lazy { filteredNodes.flatMap { it.currentMessage.parts } }

    /** All annotations from filtered nodes in the group */
    val allAnnotations: List<UIMessageAnnotation> by lazy { filteredNodes.flatMap { it.currentMessage.annotations } }
    
    /** Combined token usage for filtered messages in the group */
    val combinedUsage: TokenUsage? by lazy {
        val usages = filteredNodes.mapNotNull { it.currentMessage.usage }
        if (usages.isEmpty()) {
            null
        } else {
            TokenUsage(
                promptTokens = usages.sumOf { it.promptTokens },
                completionTokens = usages.sumOf { it.completionTokens },
                totalTokens = usages.sumOf { it.totalTokens },
                cachedTokens = usages.sumOf { it.cachedTokens }
            )
        }
    }
    
    /** Combined generation duration for filtered assistant messages in the group */
    val combinedGenerationDurationMs: Long? by lazy {
        val durations = filteredNodes.mapNotNull { it.currentMessage.generationDurationMs }
        if (durations.isNotEmpty()) durations.sum() else null
    }
}

/**
 * Group consecutive messages by role into MessageTurnGroups.
 * TOOL messages are treated as part of the ASSISTANT turn (they're tool results).
 */
fun List<MessageNode>.groupIntoTurns(
    previousGroups: List<MessageTurnGroup>? = null
): List<MessageTurnGroup> {
    if (isEmpty()) return emptyList()
    
    val groups = mutableListOf<MessageTurnGroup>()
    var currentGroup = mutableListOf<MessageNode>()
    var currentGroupRole: MessageRole? = null  // The "logical" role for grouping
    
    // Helper to determine the logical role for grouping:
    // TOOL messages belong to ASSISTANT turn, others are their own role
    fun getGroupingRole(role: MessageRole): MessageRole = when (role) {
        MessageRole.TOOL -> MessageRole.ASSISTANT
        else -> role
    }

    fun finishCurrentGroup() {
        if (currentGroup.isNotEmpty() && currentGroupRole != null) {
            val role = currentGroupRole!!
            val groupIdx = groups.size
            val prev = previousGroups?.getOrNull(groupIdx)
            if (prev != null &&
                prev.role == role &&
                prev.nodes.size == currentGroup.size &&
                prev.nodes.indices.all { i -> prev.nodes[i] === currentGroup[i] }
            ) {
                groups.add(prev)
            } else {
                groups.add(MessageTurnGroup(currentGroup.toList(), role))
            }
            currentGroup = mutableListOf()
        }
    }
    
    forEach { node ->
        val nodeRole = node.currentMessage.role
        val logicalRole = getGroupingRole(nodeRole)
        
        // Start a new group if logical role changes
        if (currentGroup.isNotEmpty() && (logicalRole != currentGroupRole || node.forceTurnBreakBefore)) {
            finishCurrentGroup()
        }
        currentGroup.add(node)
        currentGroupRole = logicalRole
    }
    
    finishCurrentGroup()
    
    return groups
}

private const val SIGNATURE_OFFSET = -3750763034362895579L
private const val SIGNATURE_PRIME = 1099511628211L

private fun Long.mix(value: Int): Long {
    return (this xor value.toLong()) * SIGNATURE_PRIME
}

private fun Long.mix(value: Long): Long {
    return (this xor value) * SIGNATURE_PRIME
}

private fun sampledStringHash(value: String?): Int {
    if (value.isNullOrEmpty()) return 0
    if (value.length <= 256) return value.hashCode()

    var hash = value.length
    hash = 31 * hash + value.substring(0, 64).hashCode()
    val middleStart = (value.length / 2 - 32).coerceIn(0, value.length - 64)
    hash = 31 * hash + value.substring(middleStart, middleStart + 64).hashCode()
    hash = 31 * hash + value.substring(value.length - 64, value.length).hashCode()
    return hash
}

private fun JsonElement.lightSignature(depth: Int = 0): Int {
    return when (this) {
        is JsonObject -> {
            var hash = size
            entries.forEach { (key, value) ->
                hash = 31 * hash + sampledStringHash(key)
                hash = 31 * hash + if (depth < 1) value.lightSignature(depth + 1) else value::class.hashCode()
            }
            hash
        }

        is JsonArray -> {
            var hash = size
            firstOrNull()?.let { hash = 31 * hash + it.lightSignature(depth + 1) }
            lastOrNull()?.let { hash = 31 * hash + it.lightSignature(depth + 1) }
            hash
        }

        else -> sampledStringHash(jsonPrimitiveOrNull?.contentOrNull)
    }
}

internal fun MessageTurnGroup.activityStateSignature(
    loading: Boolean,
    includeReasoningText: Boolean = false,
): Long {
    var hash = SIGNATURE_OFFSET
        .mix(role.hashCode())
        .mix(activeVersionTag.hashCode())
        .mix(if (loading) 1 else 0)

    filteredNodes.forEach { node ->
        val message = node.currentMessage
        hash = hash
            .mix(node.id.hashCode())
            .mix(node.selectIndex)
            .mix(message.id.hashCode())
            .mix(message.parts.size)
            .mix(message.annotations.size)

        message.annotations.forEach { annotation ->
            hash = when (annotation) {
                is UIMessageAnnotation.OcrActivity -> hash
                    .mix(annotation.source.hashCode())
                    .mix(sampledStringHash(annotation.fileName))
                    .mix(annotation.pageNumbers.size)

                else -> hash.mix(annotation::class.hashCode())
            }
        }

        message.parts.forEach { part ->
            hash = when (part) {
                is UIMessagePart.Text -> hash
                    .mix(1)
                    .mix(if (part.text.isBlank()) 1 else 0)

                is UIMessagePart.Reasoning -> {
                    hash = hash
                        .mix(2)
                        .mix(part.createdAt.toEpochMilliseconds())
                        .mix(part.finishedAt?.toEpochMilliseconds() ?: -1L)
                        .mix(sampledStringHash(part.title))
                    if (includeReasoningText) {
                        hash = hash
                            .mix(part.reasoning.length)
                            .mix(sampledStringHash(part.reasoning))
                    } else {
                        hash = hash.mix(if (part.reasoning.isBlank()) 1 else 0)
                    }
                    hash
                }

                is UIMessagePart.ToolCall -> hash
                    .mix(3)
                    .mix(sampledStringHash(part.toolCallId))
                    .mix(sampledStringHash(part.toolName))
                    .mix(part.arguments.length)
                    .mix(sampledStringHash(part.arguments))
                    .mix(part.approvalState.hashCode())

                is UIMessagePart.ToolResult -> hash
                    .mix(4)
                    .mix(sampledStringHash(part.toolCallId))
                    .mix(sampledStringHash(part.toolName))

                else -> hash.mix(part::class.hashCode())
            }
        }
    }

    return hash
}

internal fun MessageTurnGroup.timelineEntriesSignature(loading: Boolean): Long {
    var hash = activityStateSignature(loading, includeReasoningText = true)

    filteredNodes.forEach { node ->
        node.currentMessage.parts.forEach { part ->
            if (part is UIMessagePart.ToolResult) {
                hash = hash
                    .mix(part.arguments.lightSignature())
                    .mix(part.content.lightSignature())
            }
        }
    }

    return hash
}

private fun MessageTurnGroup.attachmentsSignature(): Long {
    var hash = SIGNATURE_OFFSET
        .mix(activeVersionTag.hashCode())
        .mix(filteredNodes.size)

    filteredNodes.forEach { node ->
        hash = hash
            .mix(node.id.hashCode())
            .mix(node.selectIndex)
            .mix(node.currentMessage.id.hashCode())

        node.currentMessage.parts.forEach { part ->
            hash = when (part) {
                is UIMessagePart.Image -> hash
                    .mix(1)
                    .mix(sampledStringHash(part.url))
                    .mix(part.chatAttachmentState().hashCode())
                    .mix(sampledStringHash(part.chatAttachmentDisplayName()))
                    .mix(sampledStringHash(part.chatAttachmentMimeHint()))

                is UIMessagePart.Document -> hash
                    .mix(2)
                    .mix(sampledStringHash(part.url))
                    .mix(sampledStringHash(part.fileName))
                    .mix(sampledStringHash(part.mime))
                    .mix(part.chatAttachmentState().hashCode())
                    .mix(sampledStringHash(part.chatAttachmentDisplayName()))
                    .mix(sampledStringHash(part.chatAttachmentMimeHint()))

                is UIMessagePart.Video -> hash
                    .mix(3)
                    .mix(sampledStringHash(part.url))
                    .mix(part.chatAttachmentState().hashCode())
                    .mix(sampledStringHash(part.chatAttachmentDisplayName()))
                    .mix(sampledStringHash(part.chatAttachmentMimeHint()))

                is UIMessagePart.Audio -> hash
                    .mix(4)
                    .mix(sampledStringHash(part.url))
                    .mix(part.chatAttachmentState().hashCode())
                    .mix(sampledStringHash(part.chatAttachmentDisplayName()))
                    .mix(sampledStringHash(part.chatAttachmentMimeHint()))

                else -> hash
            }
        }
    }

    return hash
}

private sealed interface RenderableAttachment {
    data class Image(
        val url: String,
        val archived: Boolean,
        val label: String,
    ) : RenderableAttachment

    data class File(
        val url: String,
        val fileName: String,
        val mimeType: String?,
        val archived: Boolean,
    ) : RenderableAttachment

    data class Placeholder(
        val fileName: String,
        val mimeType: String?,
    ) : RenderableAttachment
}

private fun collectRenderableAttachments(
    context: Context,
    parts: List<UIMessagePart>,
    fallbackVideoLabel: String,
    fallbackAudioLabel: String,
): List<RenderableAttachment> {
    return buildList {
        parts.forEach { part ->
            val attachmentState = part.chatAttachmentState()
            when (part) {
                is UIMessagePart.Image -> {
                    if (part.url.isNotBlank()) {
                        add(
                            RenderableAttachment.Image(
                                url = part.url,
                                archived = attachmentState != ChatAttachmentState.ACTIVE,
                                label = part.chatAttachmentDisplayName().orEmpty().ifBlank {
                                    resolveAttachmentDisplayName(
                                        context = context,
                                        url = part.url,
                                        fallbackLabel = context.getString(R.string.attachment_image_fallback),
                                    )
                                },
                            )
                        )
                    } else if (attachmentState != ChatAttachmentState.ACTIVE) {
                        add(
                            RenderableAttachment.Placeholder(
                                fileName = buildString {
                                    append(part.chatAttachmentDisplayName().orEmpty().ifBlank { "Image" })
                                    append(if (attachmentState == ChatAttachmentState.ARCHIVED) " (archived)" else " (deleted)")
                                },
                                mimeType = part.chatAttachmentMimeHint() ?: "image/*",
                            )
                        )
                    }
                }

                is UIMessagePart.Document -> {
                    if (part.url.isNotBlank() || attachmentState != ChatAttachmentState.ACTIVE) {
                        add(
                            RenderableAttachment.File(
                                url = part.url,
                                fileName = part.chatAttachmentDisplayName().orEmpty().ifBlank {
                                    part.fileName.ifBlank {
                                        resolveAttachmentDisplayName(
                                            context = context,
                                            url = part.url,
                                            fallbackLabel = context.getString(R.string.attachment_file_fallback),
                                        )
                                    }
                                },
                                mimeType = part.chatAttachmentMimeHint() ?: part.mime,
                                archived = attachmentState != ChatAttachmentState.ACTIVE,
                            )
                        )
                    }
                }

                is UIMessagePart.Video -> {
                    if (part.url.isNotBlank() || attachmentState != ChatAttachmentState.ACTIVE) {
                        add(
                            RenderableAttachment.File(
                                url = part.url,
                                fileName = part.chatAttachmentDisplayName().orEmpty().ifBlank {
                                    resolveAttachmentDisplayName(
                                        context = context,
                                        url = part.url,
                                        fallbackLabel = fallbackVideoLabel,
                                    )
                                },
                                mimeType = part.chatAttachmentMimeHint()
                                    ?: context.getFileMimeType(part.url.toUri())
                                    ?: "video/*",
                                archived = attachmentState != ChatAttachmentState.ACTIVE,
                            )
                        )
                    }
                }

                is UIMessagePart.Audio -> {
                    if (part.url.isNotBlank() || attachmentState != ChatAttachmentState.ACTIVE) {
                        add(
                            RenderableAttachment.File(
                                url = part.url,
                                fileName = part.chatAttachmentDisplayName().orEmpty().ifBlank {
                                    resolveAttachmentDisplayName(
                                        context = context,
                                        url = part.url,
                                        fallbackLabel = fallbackAudioLabel,
                                    )
                                },
                                mimeType = part.chatAttachmentMimeHint()
                                    ?: context.getFileMimeType(part.url.toUri())
                                    ?: "audio/*",
                                archived = attachmentState != ChatAttachmentState.ACTIVE,
                            )
                        )
                    }
                }

                else -> Unit
            }
        }
    }
}

private fun resolveAttachmentDisplayName(
    context: Context,
    url: String,
    fallbackLabel: String,
): String {
    val uri = runCatching { url.toUri() }.getOrNull() ?: return fallbackLabel
    val candidate = context.getFileNameFromUri(uri)
        ?.takeIf { it.isNotBlank() }
        ?.takeUnless(::looksLikeGeneratedUploadName)
    return candidate ?: fallbackLabel
}

private fun looksLikeGeneratedUploadName(fileName: String): Boolean {
    val baseName = fileName.substringBeforeLast('.', fileName)
    return Regex(
        pattern = "^[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$"
    ).matches(baseName)
}

private fun formatMessageTimestamp(dateTime: kotlinx.datetime.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val isToday = dateTime.year == now.year && dateTime.monthNumber == now.monthValue && dateTime.dayOfMonth == now.dayOfMonth
    val hour = dateTime.hour
    val minute = dateTime.minute.toString().padStart(2, '0')
    val amPm = if (hour >= 12) "PM" else "AM"
    val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val timeStr = "$hour12:$minute $amPm"
    return if (isToday) {
        timeStr
    } else {
        val month = dateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        "$month ${dateTime.dayOfMonth}, $timeStr"
    }
}

@Composable
private fun AttachmentRow(
    attachments: List<RenderableAttachment>,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    if (attachments.isEmpty()) return

    val context = LocalContext.current
    val haptics = rememberPremiumHaptics()
    val saturationMatrix = remember {
        android.graphics.ColorMatrix().apply { setSaturation(0f) }
    }
    val colorFilter = remember(saturationMatrix) {
        android.graphics.ColorMatrixColorFilter(saturationMatrix)
    }
    val grayscalePaint = remember(colorFilter) {
        android.graphics.Paint().apply {
            this.colorFilter = colorFilter
        }
    }

    DomChatMessageAttachmentRow(
        alignEnd = alignEnd,
        modifier = modifier,
    ) {
        itemsIndexed(
            items = attachments,
            key = { index, attachment ->
                when (attachment) {
                    is RenderableAttachment.Image -> "${index}_${attachment.url}"
                    is RenderableAttachment.File -> "${index}_${attachment.fileName}_${attachment.url}"
                    is RenderableAttachment.Placeholder -> "${index}_${attachment.fileName}"
                }
            }
        ) { _, attachment ->
            when (attachment) {
                is RenderableAttachment.Image -> {
                    val archivedModifier = if (attachment.archived) {
                        Modifier
                            .graphicsLayer { alpha = 0.99f }
                            .drawWithContent {
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.saveLayer(null, grayscalePaint)
                                    drawContent()
                                    canvas.nativeCanvas.restore()
                                }
                            }
                            .graphicsLayer(alpha = 0.72f)
                    } else {
                        Modifier
                    }
                    ZoomableAsyncImage(
                        model = attachment.url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .size(72.dp)
                            .then(archivedModifier)
                    )
                }

                is RenderableAttachment.File -> {
                    DomChatDocumentAttachmentTile(
                        fileName = if (attachment.archived && attachment.url.isBlank()) {
                            "${attachment.fileName} (archived)"
                        } else {
                            attachment.fileName
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer(alpha = if (attachment.archived) 0.72f else 1f),
                        onClick = {
                            if (attachment.url.isNotBlank()) {
                                haptics.perform(HapticPattern.Pop)
                                context.openAttachmentUri(
                                    uri = attachment.url.toUri(),
                                    mimeType = attachment.mimeType,
                                )
                            }
                        }
                    )
                }

                is RenderableAttachment.Placeholder -> {
                    DomChatDocumentAttachmentTile(
                        fileName = attachment.fileName,
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer(alpha = 0.72f),
                    )
                }
            }
        }
    }
}

/**
 * Build timeline entries from message parts.
 */
internal fun buildTimelineEntries(
    parts: List<UIMessagePart>,
    annotations: List<UIMessageAnnotation> = emptyList(),
    loading: Boolean = false,
): List<TimelineEntry> {
    val entries = mutableListOf<TimelineEntry>()
    val memoryTools = setOf("create_memory", "edit_memory", "delete_memory")
    val ocrAnnotations = annotations.filterIsInstance<UIMessageAnnotation.OcrActivity>()
    val usedEntryIds = mutableSetOf<String>()

    fun reserveEntryId(base: String): String {
        if (usedEntryIds.add(base)) return base
        var suffix = 2
        while (!usedEntryIds.add("${base}_$suffix")) {
            suffix++
        }
        return "${base}_$suffix"
    }

    ocrAnnotations.forEachIndexed { index, annotation ->
        entries.add(
            TimelineEntry.Ocr(
                id = reserveEntryId("ocr_$index"),
                source = annotation.source,
                fileName = annotation.fileName,
                pageNumbers = annotation.pageNumbers,
                isInProgress = loading,
            )
        )
    }

    val toolCallMatches = matchToolCallsToResults(parts).iterator()

    parts.forEachIndexed { partIndex, part ->
        when (part) {
            is UIMessagePart.Reasoning -> {
                val durationMs = if (part.finishedAt != null) {
                    (part.finishedAt!! - part.createdAt).inWholeMilliseconds
                } else if (!loading) {
                    (kotlin.time.Clock.System.now() - part.createdAt).inWholeMilliseconds.coerceAtLeast(0L)
                } else 0L
                
                entries.add(TimelineEntry.Reasoning(
                    id = reserveEntryId("reasoning_${entries.size}"),
                    content = part.reasoning,
                    durationMs = durationMs,
                    title = part.title,
                    isInProgress = loading && part.finishedAt == null
                ))
            }
            is UIMessagePart.ToolCall -> {
                val result = toolCallMatches.next().result
                val resolvedToolName = resolveActivityToolName(part.toolName, part.arguments)
                if (resolvedToolName in memoryTools) {
                    val entry = buildMemoryTimelineEntry(part, result)
                    entries.add(entry.copy(id = reserveEntryId(entry.id)))
                } else {
                    val argumentsJson = result?.arguments ?: parseJsonObjectOrNull(part.arguments)
                    val resultJson = result?.content
                    val rawId = part.toolCallId.takeIf { it.isNotBlank() } ?: partIndex.toString()
                    entries.add(TimelineEntry.ToolCall(
                        id = reserveEntryId("tool_$rawId"),
                        toolName = resolvedToolName,
                        displayName = getToolDisplayName(resolvedToolName),
                        argumentsText = part.arguments,
                        resultText = result?.content?.toString(),
                        argumentsJson = argumentsJson,
                        resultJson = resultJson,
                        isLoading = result == null && part.approvalState !is ToolApprovalState.Pending
                    ))
                }
            }
            else -> {}
        }
    }

    return entries
}

private fun buildMemoryTimelineEntry(
    call: UIMessagePart.ToolCall,
    result: UIMessagePart.ToolResult?
): TimelineEntry.MemoryAction {
    val toolName = resolveActivityToolName(call.toolName, call.arguments)
    val operation = when (toolName) {
        "create_memory" -> MemoryOperation.CREATE
        "edit_memory" -> MemoryOperation.EDIT
        "delete_memory" -> MemoryOperation.DELETE
        else -> MemoryOperation.CREATE
    }
    val resultObj = result?.content as? JsonObject
    val argsObj = (result?.arguments as? JsonObject) ?: parseJsonObjectOrNull(call.arguments)

    val memoryId = resultObj?.get("id")?.jsonPrimitiveOrNull?.intOrNull
        ?: argsObj?.get("id")?.jsonPrimitiveOrNull?.intOrNull
    val content = resultObj?.get("content")?.jsonPrimitiveOrNull?.contentOrNull
        ?: argsObj?.get("content")?.jsonPrimitiveOrNull?.contentOrNull
    val previousContent = resultObj?.get("before_content")?.jsonPrimitiveOrNull?.contentOrNull
    val memoryType = resultObj?.get("type")?.jsonPrimitiveOrNull?.intOrNull
    val timestamp = resultObj?.get("timestamp")?.jsonPrimitiveOrNull?.longOrNull

    return TimelineEntry.MemoryAction(
        id = "memory_${call.toolCallId.takeIf { it.isNotBlank() } ?: toolName}",
        toolName = toolName,
        operation = operation,
        memoryId = memoryId,
        content = content,
        previousContent = previousContent,
        memoryType = memoryType,
        timestamp = timestamp,
        isLoading = result == null
    )
}

private fun parseJsonObjectOrNull(raw: String): JsonObject? {
    return parseJsonElementWithRecovery(raw) as? JsonObject
}

private data class ToolCallMatch(
    val call: UIMessagePart.ToolCall,
    val result: UIMessagePart.ToolResult?
)

private fun matchToolCallsToResults(parts: List<UIMessagePart>): List<ToolCallMatch> {
    val resultQueues = parts
        .filterIsInstance<UIMessagePart.ToolResult>()
        .groupBy { toolResultMatchKey(it.toolCallId, it.toolName) }
        .mapValues { (_, results) -> ArrayDeque(results) }

    return parts.filterIsInstance<UIMessagePart.ToolCall>().map { call ->
        val key = toolResultMatchKey(
            toolCallId = call.toolCallId,
            toolName = resolveActivityToolName(call.toolName, call.arguments)
        )
        ToolCallMatch(
            call = call,
            result = resultQueues[key]?.removeFirstOrNull()
        )
    }
}

private fun toolResultMatchKey(toolCallId: String, toolName: String): String {
    return toolCallId.takeIf { it.isNotBlank() } ?: "blank:${toolName.ifBlank { "unknown" }}"
}

/**
 * Get display name for a tool.
 */
private fun getToolDisplayName(toolName: String): String {
    return when (toolName) {
        "search_web" -> "Searching web"
        "search_memory" -> "Recalling memories"
        "scrape_web" -> "Reading page"
        "eval_python" -> "Running Python"
        "pip_install" -> "Installing packages"
        "write_sandbox_file" -> "Writing file"
        "read_sandbox_file" -> "Reading file"
        "list_sandbox_files" -> "Listing files"
        "delete_sandbox_file" -> "Deleting file"
        "workspace_read_file" -> "Reading workspace file"
        "workspace_write_file" -> "Writing workspace file"
        "workspace_edit_file" -> "Editing workspace file"
        "workspace_shell" -> "Running workspace command"
        "workspace_view_image" -> "Viewing workspace image"
        "create_memory" -> "Creating memory"
        "edit_memory" -> "Editing memory"
        "delete_memory" -> "Deleting memory"
        "ask_user" -> "Asking a question"
        "manage_skills" -> "Managing skills"
        else -> toolName.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}

/**
 * Determine the current activity state from message parts.
 */
internal fun deriveActivityState(
    parts: List<UIMessagePart>,
    annotations: List<UIMessageAnnotation> = emptyList(),
    loading: Boolean
): ActivityState {
    val reasoningParts = parts.filterIsInstance<UIMessagePart.Reasoning>()
    val toolCalls = parts.filterIsInstance<UIMessagePart.ToolCall>()
    val toolCallMatches = matchToolCallsToResults(parts)
    val ocrAnnotations = annotations.filterIsInstance<UIMessageAnnotation.OcrActivity>()
    
    // Only count text AFTER the last tool-related part as "currently replying"
    // This prevents text from before tool calls (e.g. "Let me run that for you") 
    // from causing a "Replying" state during/between tool calls
    val lastToolIndex = parts.indexOfLast { it is UIMessagePart.ToolCall || it is UIMessagePart.ToolResult }
    val hasRecentText = parts.drop(lastToolIndex + 1)
        .filterIsInstance<UIMessagePart.Text>()
        .any { it.text.isNotBlank() }
        
    if (!loading) {
        // Generation complete - determine what to show based on activities
        val totalReasoningMs = reasoningParts.sumOf { r ->
            if (r.finishedAt != null) {
                (r.finishedAt!! - r.createdAt).inWholeMilliseconds
            } else 0L
        }
        
        // Group tools by CATEGORY (Python, Search, etc.) not individual tool names
        val toolCategories = toolCalls
            .map { categorizeToolName(resolveActivityToolName(it.toolName, it.arguments)) }
            .distinct()
        
        val hasReasoning = totalReasoningMs > 0 || reasoningParts.isNotEmpty()
        val hasTools = toolCategories.isNotEmpty()
        val hasOcr = ocrAnnotations.isNotEmpty()
        
        // Count distinct activity categories (not individual tools)
        val activityCount = (if (hasReasoning) 1 else 0) + (if (hasOcr) 1 else 0) + toolCategories.size

        return when {
            activityCount == 0 -> ActivityState.Hidden  // No activities, hide pill
            activityCount == 1 && hasReasoning -> ActivityState.CompletedSingle(
                type = ActivityType.REASONING,
                durationMs = totalReasoningMs
            )
            activityCount == 1 && hasOcr -> ActivityState.CompletedSingle(
                type = ActivityType.OCR,
                count = ocrAnnotations.size,
            )
            activityCount == 1 && hasTools -> ActivityState.CompletedSingle(
                type = toolCategories.first(),
                toolName = resolveActivityToolName(toolCalls.first().toolName, toolCalls.first().arguments),
                displayName = getToolDisplayName(
                    resolveActivityToolName(toolCalls.first().toolName, toolCalls.first().arguments)
                ),
                count = toolCalls.size  // Pass total count of tool calls
            )
            else -> ActivityState.CompletedMultiple(
                reasoningDurationMs = if (hasReasoning) totalReasoningMs else null,
                activityTypes = buildList {
                    if (hasOcr) {
                        add(ActivityType.OCR)
                    }
                    addAll(toolCategories)
                }
            )
        }
    }
    
    // Check for active reasoning (close immediately once response text begins after it)
    val lastReasoningIndex = parts.indexOfLast { it is UIMessagePart.Reasoning }
    val hasTextAfterLastReasoning = lastReasoningIndex >= 0 && parts.drop(lastReasoningIndex + 1)
        .filterIsInstance<UIMessagePart.Text>()
        .any { it.text.isNotBlank() }
    val activeReasoning = reasoningParts.lastOrNull { it.finishedAt == null }
    if (activeReasoning != null && !hasTextAfterLastReasoning) {
        return ActivityState.Reasoning(
            startTimeMs = activeReasoning.createdAt.toEpochMilliseconds(),
            title = activeReasoning.title,
            reasoningText = activeReasoning.reasoning
        )
    }
    
    // Check for active tool calls (tool call without matching result)
    val activeTool = toolCallMatches.lastOrNull { it.result == null }?.call
    if (activeTool != null) {
        val resolvedToolName = resolveActivityToolName(activeTool.toolName, activeTool.arguments)
        return ActivityState.ToolUse(
            toolName = resolvedToolName,
            displayName = getToolDisplayName(resolvedToolName),
            startTimeMs = System.currentTimeMillis()
        )
    }
    
    // Check if we have any text AFTER the last tool activity
    if (hasRecentText) {
        // Text is being generated after all tools completed - show "Replying" state
        return ActivityState.Replying
    }

    if (ocrAnnotations.isNotEmpty()) {
        return ActivityState.Ocr
    }
    
    return ActivityState.Waiting
}

/**
 * Redesigned ChatMessage component for a GROUP of consecutive messages.
 * 
 * For user message turns: Right-aligned bubbles, long-press for menu
 * For assistant message turns: 
 *   - Name + Avatar row at the top
 *   - Activity Pill below name
 *   - Stacked message bubbles with grouped corners
 *   - Token stats and action buttons at bottom
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageTurn(
    group: MessageTurnGroup,
    isLastTurn: Boolean,
    onCitationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    model: Model? = null,
    assistant: Assistant? = null,
    onFork: (MessageNode) -> Unit = {},
    onRegenerate: (MessageNode) -> Unit = {},
    onEdit: (MessageNode) -> Unit = {},
    onDelete: (MessageNode) -> Unit = {},
    onUpdate: (MessageNode) -> Unit = {},
    onDeleteSwipe: ((UIMessage) -> Unit)? = null,
    showRegenerate: Boolean,
    onEditLorebookEntry: ((me.lucid.ai.ui.UsedLorebookEntry) -> Unit)? = null,
    onModeClick: ((me.lucid.ai.ui.UsedMode) -> Unit)? = null,
    onMemoryClick: ((me.lucid.ai.ui.UsedMemory) -> Unit)? = null,
    onExpandedStreamingCodeBlockChanged: (() -> Unit)? = null,
    onStreamingRevealTick: (() -> Unit)? = null,
    onTurnActiveChanged: ((Boolean) -> Unit)? = null,
    isIntroMessage: Boolean = false,
    isIntroLocked: Boolean = false,
    onAddCustomGreeting: () -> Unit = {},
) {
    val settings = LocalSettings.current
    val baseDisplay = LocalDisplaySetting.current
    val context = LocalContext.current
    val navController = LocalNavController.current
    val colorScheme = MaterialTheme.colorScheme
    val effectiveDisplay = remember(baseDisplay, assistant) {
        baseDisplay.withAssistantOverrides(assistant)
    }
    val ttsProviderOverride = remember(
        settings.ttsProviders,
        settings.selectedTTSVoiceId,
        settings.selectedTTSProviderId,
        assistant?.ttsVoiceId,
    ) {
        settings.getEffectiveTTSProvider(assistant)
    }
    val textStyle = LocalTextStyle.current.copy(
        fontSize = LocalTextStyle.current.fontSize * effectiveDisplay.fontSizeRatio,
        lineHeight = LocalTextStyle.current.lineHeight * effectiveDisplay.fontSizeRatio
    )
    val configuration = LocalConfiguration.current
    val maxBubbleWidth = (configuration.screenWidthDp * 0.85f).dp
    
    // State for sheets
    var showActionsSheet by remember { mutableStateOf(false) }
    var showSelectCopySheet by remember { mutableStateOf(false) }
    var timelineOpen by remember { mutableStateOf(false) }
    var timelineOpenRequest by remember { mutableStateOf<TimelineOpenRequest?>(null) }
    var showUserDropdown by remember { mutableStateOf(false) }
    var actionsExpanded by remember { mutableStateOf(false) }
    var showUserToolbar by remember { mutableStateOf(false) }  // User message toolbar visibility
    
    // Activity state from all nodes in the group. Use cheap primitive signatures as
    // remember keys; using MessageTurnGroup or MessageNode keys can structurally
    // compare large JsonElement tool payloads on the UI thread.
    val isTimelineLive = loading && isLastTurn
    val includeReasoningText = timelineOpen || effectiveDisplay.reasoningPreviewEnabled
    val activitySignature = group.activityStateSignature(
        loading = isTimelineLive,
        includeReasoningText = includeReasoningText
    )
    val activityState = remember(activitySignature) {
        deriveActivityState(
            parts = group.allParts,
            annotations = group.allAnnotations,
            loading = isTimelineLive,
        )
    }

    // Timeline entries are deferred until the timeline is actually opened.
    // The compact activity pill uses `activityState` (always computed above), not
    // `timelineEntries`, so returning emptyList() while closed is invisible.
    val timelineSignature = if (timelineOpen) group.timelineEntriesSignature(isTimelineLive) else 0L
    val timelineEntries = remember(timelineOpen, timelineSignature) {
        if (timelineOpen) {
            buildTimelineEntries(
                parts = group.allParts,
                annotations = group.allAnnotations,
                loading = isTimelineLive,
            )
        } else {
            emptyList()
        }
    }

    // Actions should target the visible assistant content node instead of blindly using lastNode,
    // because the last node in a turn can be a tool node.
    val actionTargetNode = group.filteredNodes
        .asReversed()
        .firstOrNull { node ->
            node.currentMessage.parts.any { part ->
                part is UIMessagePart.Text || part is UIMessagePart.Reasoning || part is UIMessagePart.Thinking
            }
        }
        ?: group.filteredNodes.lastOrNull()
        ?: group.lastNode
    
    ProvideTextStyle(textStyle) {
        when (group.role) {
            MessageRole.USER -> {
                val context = androidx.compose.ui.platform.LocalContext.current
                UserMessageTurn(
                    group = group,
                    node = actionTargetNode,
                    assistant = assistant,
                    maxWidth = maxBubbleWidth,
                    showToolbar = showUserToolbar,
                    onToggleToolbar = { showUserToolbar = !showUserToolbar },
                    onEdit = { onEdit(actionTargetNode) },
                    onCopy = { context.copyMessageToClipboard(group.lastNode.currentMessage) },
                    onRegenerate = { onRegenerate(group.lastNode) },
                    onOpenMenu = { showActionsSheet = true },
                    onUpdate = onUpdate,
                    showRegenerate = showRegenerate,
                    modifier = modifier
                )
            }
            
            MessageRole.ASSISTANT -> {
                AssistantMessageTurn(
                    group = group,
                    assistant = assistant,
                    model = model,
                    activityState = activityState,
                    loading = loading && isLastTurn,
                    isLastTurn = isLastTurn,
                    actionsExpanded = actionsExpanded,
                    maxWidth = maxBubbleWidth,
                    showTokenUsage = effectiveDisplay.showTokenUsage,
                    showAssistantBubbles = effectiveDisplay.showAssistantBubbles,
                    timelineEntries = timelineEntries,
                    timelineOpen = timelineOpen,
                    initialTimelineOpenRequest = timelineOpenRequest,
                    onCitationClick = onCitationClick,
                    onActivityPillClick = { type ->
                        if (activityState is ActivityState.Hidden) return@AssistantMessageTurn
                        if (timelineOpen && timelineOpenRequest?.focusType == type) {
                            timelineOpen = false
                        } else {
                            timelineOpenRequest = TimelineOpenRequest(
                                focusType = type,
                                openMode = if (isTimelineLive) {
                                    TimelineOpenMode.FocusCurrent
                                } else {
                                    TimelineOpenMode.Collapsed
                                }
                            )
                            timelineOpen = true
                        }
                    },
                    onTimelineDismiss = { timelineOpen = false },
                    onBubbleClick = {
                        if (timelineOpen) {
                            timelineOpen = false
                        } else {
                            actionsExpanded = !actionsExpanded
                        }
                    },
                    onRegenerate = { onRegenerate(group.lastNode) },
                    onUpdate = onUpdate,
                    onOpenActionSheet = { showActionsSheet = true },
                    showRegenerate = showRegenerate,
                    onEditLorebookEntry = onEditLorebookEntry,
                    onModeClick = onModeClick,
                    onMemoryClick = onMemoryClick,
                    onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                    onStreamingRevealTick = onStreamingRevealTick,
                    onTurnActiveChanged = onTurnActiveChanged,
                    ttsProviderOverride = ttsProviderOverride,
                    isIntroMessage = isIntroMessage,
                    isIntroLocked = isIntroLocked,
                    onAddCustomGreeting = onAddCustomGreeting,
                    onEdit = onEdit,
                    onDeleteSwipe = onDeleteSwipe,
                    modifier = modifier
                )
            }
            
            else -> { /* System messages not rendered */ }
        }
    }
    
    val activeActionController = LocalActiveChatMessageAction.current
    if (showActionsSheet) {
        if (activeActionController != null) {
            LaunchedEffect(Unit) {
                showActionsSheet = false
                activeActionController(
                    ActiveChatMessageAction(
                        message = actionTargetNode.currentMessage,
                        model = model,
                        onEdit = { onEdit(actionTargetNode) },
                        onDelete = { onDelete(actionTargetNode) },
                        onFork = { onFork(actionTargetNode) },
                        onSelectAndCopy = { showSelectCopySheet = true },
                        onWebViewPreview = {
                            val markdown = actionTargetNode.currentMessage.parts
                                .filterIsInstance<UIMessagePart.Text>()
                                .joinToString(separator = "\n\n") { it.text }
                                .trim()
                            if (markdown.isNotEmpty()) {
                                val html = buildMarkdownPreviewHtml(context, markdown, colorScheme)
                                navController.navigate(Screen.WebView(content = html.base64Encode()))
                            }
                        }
                    )
                )
            }
        } else {
            ChatMessageActionsSheet(
                message = actionTargetNode.currentMessage,
                onEdit = { onEdit(actionTargetNode) },
                onDelete = { onDelete(actionTargetNode) },
                onFork = { onFork(actionTargetNode) },
                model = model,
                onSelectAndCopy = { showSelectCopySheet = true },
                onWebViewPreview = {
                    val markdown = actionTargetNode.currentMessage.parts
                        .filterIsInstance<UIMessagePart.Text>()
                        .joinToString(separator = "\n\n") { it.text }
                        .trim()
                    if (markdown.isNotEmpty()) {
                        val html = buildMarkdownPreviewHtml(context, markdown, colorScheme)
                        navController.navigate(Screen.WebView(content = html.base64Encode()))
                    }
                },
                onDismissRequest = { showActionsSheet = false }
            )
        }
    }
    
    if (showSelectCopySheet) {
        ChatMessageCopySheet(
            message = actionTargetNode.currentMessage,
            onDismissRequest = { showSelectCopySheet = false }
        )
    }
}

/**
 * User message turn - right-aligned stacked bubbles.
 * Tap to show/hide action toolbar.
 */
@Composable
private fun UserMessageTurn(
    group: MessageTurnGroup,
    node: MessageNode,
    assistant: Assistant?,
    maxWidth: androidx.compose.ui.unit.Dp,
    showToolbar: Boolean,
    onToggleToolbar: () -> Unit,
    onEdit: () -> Unit = {},
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onOpenMenu: () -> Unit,
    onUpdate: (MessageNode) -> Unit,
    showRegenerate: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val baseDisplay = LocalDisplaySetting.current
    val effectiveDisplay = remember(baseDisplay, assistant) {
        baseDisplay.withAssistantOverrides(assistant)
    }
    // History turns never animate; only turns that arrived this session do.
    val isHistoryTurn = group.firstNode.id.toString() in me.lucid.domchat.ui.motion.LocalPreexistingMessageNodes.current
    val defaultVideoLabel = stringResource(R.string.chat_message_attachment_video)
    val defaultAudioLabel = stringResource(R.string.chat_message_attachment_audio)
    val haptics = rememberPremiumHaptics()
    val attachmentSignature = group.attachmentsSignature()
    val attachments = remember(attachmentSignature, defaultVideoLabel, defaultAudioLabel, context) {
        collectRenderableAttachments(
            context = context,
            parts = group.filteredNodes.flatMap { it.currentMessage.parts },
            fallbackVideoLabel = defaultVideoLabel,
            fallbackAudioLabel = defaultAudioLabel,
        )
    }

    val versionIndices: List<Int> = remember(node.messages) { node.versionSelectionIndices() }
    val currentVersionPos: Int = remember(node.messages, node.selectIndex) { node.versionSelectionPosition() }
    val swipeModifier = if (versionIndices.size > 1) {
        Modifier.pointerInput(node.id, currentVersionPos) {
            var totalDrag = 0f
            detectHorizontalDragGestures(
                onHorizontalDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: Float ->
                    change.consume()
                    totalDrag += dragAmount
                },
                onDragEnd = {
                    if (totalDrag > 50f && currentVersionPos > 0) {
                        val targetIndex = versionIndices.getOrNull(currentVersionPos - 1)
                        if (targetIndex != null) {
                            haptics.perform(HapticPattern.Pop)
                            onUpdate(node.copy(selectIndex = targetIndex))
                        }
                    } else if (totalDrag < -50f && currentVersionPos < versionIndices.lastIndex) {
                        val targetIndex = versionIndices.getOrNull(currentVersionPos + 1)
                        if (targetIndex != null) {
                            haptics.perform(HapticPattern.Pop)
                            onUpdate(node.copy(selectIndex = targetIndex))
                        }
                    }
                    totalDrag = 0f
                },
                onDragCancel = { totalDrag = 0f }
            )
        }
    } else {
        Modifier
    }
    
    var previewUserAvatarUrl by remember { mutableStateOf<String?>(null) }
    val activePersona = effectiveDisplay.getActivePersona()
    val personaName = activePersona.name.ifBlank { effectiveDisplay.userNickname.ifBlank { "User" } }
    val personaAvatar = activePersona.avatar
    val messageTimestamp = remember(node.currentMessage.createdAt) {
        formatMessageTimestamp(node.currentMessage.createdAt)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Persona Header: Timestamp + Persona Name + Avatar
        val isDark = LocalDarkMode.current
        val headerNameColor = if (isDark) Color.White else Color(0xFF18181B)
        val headerTimeColor = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF52525B)
        val textShadow = Shadow(
            color = if (isDark) Color.Black.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.45f),
            offset = Offset(0f, 1f),
            blurRadius = 3f
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp, end = 2.dp)
        ) {
            Text(
                text = messageTimestamp,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    shadow = textShadow
                ),
                color = headerTimeColor,
            )
            Text(
                text = personaName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = textShadow
                ),
                color = headerNameColor,
            )
            val onUserAvatarClick: () -> Unit = {
                haptics.perform(HapticPattern.Pop)
                val avatarToUse = personaAvatar.takeIf { it !is Avatar.Dummy } ?: effectiveDisplay.userAvatar
                val avatarUrl = when (avatarToUse) {
                    is Avatar.Image -> avatarToUse.url
                    is Avatar.Resource -> "android.resource://${context.packageName}/${safeDrawableResId(context, avatarToUse.id, me.lucid.domchat.R.drawable.default_generical_pfp)}"
                    else -> null
                }
                if (avatarUrl != null) {
                    previewUserAvatarUrl = avatarUrl
                }
            }

            UIAvatar(
                name = personaName,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                value = personaAvatar,
                onClick = onUserAvatarClick,
            )
        }

        AttachmentRow(
            attachments = attachments,
            alignEnd = true,
        )
        
        // Message bubbles
        group.filteredNodes.forEachIndexed { nodeIndex, bubbleNode ->
            val textParts = bubbleNode.currentMessage.parts.filterIsInstance<UIMessagePart.Text>()
            textParts.forEachIndexed { partIndex, part ->
                // Calculate bubble position based on overall position in group
                val isFirst = nodeIndex == 0 && partIndex == 0
                val isLast = nodeIndex == group.filteredNodes.lastIndex && partIndex == textParts.lastIndex
                val totalBubbles = group.filteredNodes.sumOf { n ->
                    n.currentMessage.parts.filterIsInstance<UIMessagePart.Text>().size 
                }
                val position = when {
                    totalBubbles == 1 -> BubblePosition.SINGLE
                    isFirst -> BubblePosition.FIRST
                    isLast -> BubblePosition.LAST
                    else -> BubblePosition.MIDDLE
                }
                
                val isTransparentStyle = effectiveDisplay.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.TRANSPARENT_BOTTOM_BLUR
                val isMessengerStyle = effectiveDisplay.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.MESSENGER
                val isGlassy = effectiveDisplay.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.BUBBLES
                val bubbleInterfaceStyle = effectiveDisplay.bubbleInterfaceStyle
                val glassAlpha = effectiveDisplay.chatBubbleAlpha
                val bubbleBlur = effectiveDisplay.chatBubbleBlur
                val bubbleTone = 0f
                val baseLuminance = effectiveDisplay.glassBaseLuminance
                val tintHue = effectiveDisplay.chatBubbleTintHue
                val tintAmount = effectiveDisplay.chatBubbleTintAmount

                GroupedMessageBubble(
                    position = position,
                    role = BubbleRole.USER,
                    isTransparent = isTransparentStyle,
                    isGlassy = isGlassy,
                    glassAlpha = glassAlpha,
                    baseLuminance = baseLuminance,
                    tintHue = tintHue,
                    tintAmount = tintAmount,
                    bubbleTone = bubbleTone,
                    bubbleAlpha = glassAlpha,
                    containerColor = if (isMessengerStyle) {
                        Color(0xFF0A84FF).copy(alpha = if (bubbleBlur > 0f) 0.85f else 0.95f)
                    } else null,
                    contentColor = if (isMessengerStyle) Color.White else null,
                    largeRadius = if (isMessengerStyle) 14.dp else 20.dp,
                    smallRadius = if (isMessengerStyle) 4.dp else 6.dp,
                    modifier = Modifier
                        .widthIn(max = maxWidth)
                        .then(swipeModifier),
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        onToggleToolbar()
                    },
                    bubbleModifier = { m: Modifier, shape: Shape, color: Color ->
                        if (isTransparentStyle) {
                            m
                        } else if (!isGlassy) {
                            if (bubbleBlur > 0f) {
                                m.domChatBlurEffect(
                                    containerColor = color,
                                    shape = shape,
                                    blurRadius = bubbleBlur.dp
                                )
                            } else {
                                m
                            }
                        } else when (bubbleInterfaceStyle) {
                            InterfaceStyle.FROSTED -> m.frostedGlassBubbleSurface(
                                shape = shape,
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                            )
                            InterfaceStyle.GLASS -> {
                                val specularBorder = LiquidGlassTokens.SpecularBorderSubtle.takeIf {
                                    effectiveDisplay.bubbleSpecularIntensity > 0f
                                }
                                val bubbleFillColor = if (glassAlpha <= 0.02f) Color.Transparent else androidx.compose.ui.graphics.lerp(Color.Black, Color.White, bubbleTone.coerceIn(0f, 1f)).copy(alpha = glassAlpha.coerceIn(0f, 1f))
                                m.liquidGlassBubbleSurface(
                                    shape = shape,
                                    borderWidth = 0.5.dp,
                                    fillColor = bubbleFillColor,
                                    specularBrush = specularBorder ?: LiquidGlassTokens.SpecularBorderSubtle,
                                    glassMode = effectiveDisplay.liquidGlassMode,
                                )
                            }
                        }
                    }
                ) {
                    val chatAnimationsEnabled = LocalChatAnimationsEnabled.current
                    var hasAnimated by remember(node.id) { mutableStateOf(isHistoryTurn) }
                    var isRevealing by remember(node.id) { mutableStateOf(!isHistoryTurn && !hasAnimated) }
                    LaunchedEffect(node.id) {
                        if (!hasAnimated) {
                            // Safety timeout so state eventually settles even if scrolled away
                            kotlinx.coroutines.delay(8000L)
                            hasAnimated = true
                            isRevealing = false
                        }
                    }
                    val userNickname = effectiveDisplay.getActivePersona().name
                    val renderUserBubbleContent: @Composable (Int) -> Unit = { targetIdx ->
                        val targetMsg = node.messages.getOrNull(targetIdx) ?: bubbleNode.currentMessage
                        val targetText = targetMsg.parts.filterIsInstance<UIMessagePart.Text>().getOrNull(partIndex)?.text ?: part.text
                        val displayContent = rememberFormattedMessageText(
                            rawText = targetText,
                            assistant = assistant,
                            userNickname = userNickname,
                            scope = AssistantAffectScope.USER,
                            isStreaming = isRevealing
                        )
                        MarkdownBlock(
                            workspaceId = assistant?.workspaceId?.toString(),
                            content = displayContent,
                            paragraphSpacing = 12.dp,
                            streamingTextReveal = isRevealing,
                            freshReveal = isRevealing,
                            displaySetting = effectiveDisplay,
                            onRevealComplete = {
                                hasAnimated = true
                                isRevealing = false
                            },
                            onClickCitation = {}
                        )
                    }
                    Box(modifier = Modifier.smoothExpandingHeight(enabled = chatAnimationsEnabled)) {
                        if (node.messages.size > 1 && chatAnimationsEnabled) {
                            AnimatedContent(
                                targetState = node.selectIndex,
                                transitionSpec = {
                                    val forward = targetState >= initialState
                                    (slideInHorizontally(animationSpec = tween(280, easing = LinearOutSlowInEasing)) { width ->
                                        if (forward) (width * 0.35f).toInt() else (-width * 0.35f).toInt()
                                    } + fadeIn(animationSpec = tween(280, easing = LinearOutSlowInEasing))) togetherWith
                                    (slideOutHorizontally(animationSpec = tween(220, easing = FastOutLinearInEasing)) { width ->
                                        if (forward) (-width * 0.35f).toInt() else (width * 0.35f).toInt()
                                    } + fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))) using
                                    SizeTransform(clip = false) { _, _ -> snap() }
                                },
                                label = "UserMessageVersionAnimatedContent"
                            ) { targetIdx ->
                                renderUserBubbleContent(targetIdx)
                            }
                        } else {
                            renderUserBubbleContent(node.selectIndex)
                        }
                    }
                }
            }
        }
        
        // Toolbar - appears on tap or when versions exist
        AnimatedVisibility(
            visible = showToolbar || versionIndices.size > 1,
            enter = expandVertically(
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
            ) + fadeOut()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (versionIndices.size > 1) {
                    me.lucid.domchat.ui.components.message.ChatMessageBranchSelector(
                        node = node,
                        onUpdate = onUpdate,
                    )
                }

                // Edit button
                Box(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .clickable {
                            haptics.perform(HapticPattern.Pop)
                            onEdit()
                        }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.edit),
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }

                // Copy button
                Box(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .clickable {
                            haptics.perform(HapticPattern.Pop)
                            onCopy()
                        }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.ContentCopy,
                        contentDescription = stringResource(R.string.copy),
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
                
                // Regenerate button
                if (showRegenerate) {
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .clickable {
                                haptics.perform(HapticPattern.Pop)
                                onRegenerate()
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.regenerate),
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
                
                // More options button
                Box(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .clickable {
                            haptics.perform(HapticPattern.Pop)
                            onOpenMenu()
                        }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.MoreHoriz,
                        contentDescription = stringResource(R.string.more_options),
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (previewUserAvatarUrl != null) {
        me.lucid.domchat.ui.components.ui.ImagePreviewDialog(
            images = listOf(previewUserAvatarUrl!!),
            onDismissRequest = { previewUserAvatarUrl = null }
        )
    }
}

/**
 * Assistant message turn - name + avatar at top, activity pill, stacked bubbles.
 */
@Composable
private fun AssistantMessageTurn(
    group: MessageTurnGroup,
    assistant: Assistant?,
    model: Model?,
    activityState: ActivityState,
    loading: Boolean,
    isLastTurn: Boolean,
    actionsExpanded: Boolean,
    maxWidth: androidx.compose.ui.unit.Dp,
    showTokenUsage: Boolean,
    showAssistantBubbles: Boolean,
    timelineEntries: List<TimelineEntry>,
    timelineOpen: Boolean,
    initialTimelineOpenRequest: TimelineOpenRequest?,
    onCitationClick: (String) -> Unit,
    onActivityPillClick: (ActivityType?) -> Unit,
    onTimelineDismiss: () -> Unit,
    onBubbleClick: () -> Unit,
    onRegenerate: () -> Unit,
    onUpdate: (MessageNode) -> Unit,
    onOpenActionSheet: () -> Unit,
    showRegenerate: Boolean,
    onEditLorebookEntry: ((me.lucid.ai.ui.UsedLorebookEntry) -> Unit)?,
    onModeClick: ((me.lucid.ai.ui.UsedMode) -> Unit)?,
    onMemoryClick: ((me.lucid.ai.ui.UsedMemory) -> Unit)?,
    onExpandedStreamingCodeBlockChanged: (() -> Unit)?,
    onStreamingRevealTick: (() -> Unit)? = null,
    onTurnActiveChanged: ((Boolean) -> Unit)? = null,
    ttsProviderOverride: me.lucid.tts.provider.TTSProviderSetting?,
    isIntroMessage: Boolean = false,
    isIntroLocked: Boolean = false,
    onAddCustomGreeting: () -> Unit = {},
    onEdit: ((MessageNode) -> Unit)? = null,
    onDeleteSwipe: ((UIMessage) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isTimelineLive = loading && isLastTurn
    val baseDisplay = LocalDisplaySetting.current
    val context = LocalContext.current
    // History turns never animate; only turns that arrived this session do.
    val isHistoryTurn = group.firstNode.id.toString() in me.lucid.domchat.ui.motion.LocalPreexistingMessageNodes.current
    val defaultAssistantName = stringResource(R.string.assistant_page_default_assistant)
    val defaultVideoLabel = stringResource(R.string.chat_message_attachment_video)
    val defaultAudioLabel = stringResource(R.string.chat_message_attachment_audio)
    val effectiveDisplay = remember(baseDisplay, assistant) {
        baseDisplay.withAssistantOverrides(assistant)
    }
    val showIcon = effectiveDisplay.showModelIcon
    val showModelName = effectiveDisplay.showModelName
    val haptics = rememberPremiumHaptics()

    // Bubble size-animation is gated off during active streaming: applying
    // animateContentSize on every new-chunk frame kept re-targeting the
    // spring and made expansion choppy. Animate only when the timeline
    // toggles or when loading finishes.
    var prevLoading by remember { mutableStateOf(false) }
    var isTextAnimating by remember { mutableStateOf(false) }
    val loadingJustFinished = !loading && prevLoading
    LaunchedEffect(loading) {
        if (loading) isTextAnimating = true
        prevLoading = loading
    }
    val isTurnActive = loading || isTextAnimating
    LaunchedEffect(isTurnActive, isLastTurn) {
        if (isLastTurn) {
            onTurnActiveChanged?.invoke(isTurnActive)
        }
    }
    androidx.compose.runtime.DisposableEffect(isLastTurn) {
        onDispose {
            if (isLastTurn) {
                onTurnActiveChanged?.invoke(false)
            }
        }
    }
    val showName = showModelName && (!isLastTurn || !isTurnActive)
    val nameAlpha by animateFloatAsState(
        targetValue = if (showName) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f),
        label = "assistant_name_alpha"
    )
    val handleBubbleClick = {
        haptics.perform(HapticPattern.Pop)
        onBubbleClick()
    }
    
    // Get avatar info
    val avatarName = assistant?.effectiveChatName ?: model?.displayName ?: defaultAssistantName
    val avatarValue = assistant?.avatar ?: Avatar.Dummy
    var previewBotAvatarUrl by remember { mutableStateOf<String?>(null) }
    val onBotAvatarClick: () -> Unit = {
        haptics.perform(HapticPattern.Pop)
        when (avatarValue) {
            is Avatar.Image -> previewBotAvatarUrl = avatarValue.url
            is Avatar.Resource -> previewBotAvatarUrl = "android.resource://${context.packageName}/${safeDrawableResId(context, avatarValue.id, me.lucid.domchat.R.drawable.default_generical_pfp)}"
            else -> Unit
        }
    }
    
    // Check if there's interesting activity (reasoning or tools)
    val hasInterestingActivity = activityState !is ActivityState.Hidden
    
    // Consistent spacing between all elements
    val elementSpacing = 4.dp
    
    val assistantNode = group.nodeWithMostVersions
    val versionIndices = remember(assistantNode.messages) { assistantNode.versionSelectionIndices() }
    val currentVersionPos = remember(assistantNode.messages, assistantNode.selectIndex) { assistantNode.versionSelectionPosition() }
    val density = LocalDensity.current
    val swipeThresholdPx = remember(density) { with(density) { 48.dp.toPx() } }

    val latestAssistantNode by androidx.compose.runtime.rememberUpdatedState(assistantNode)
    val latestVersionIndices by androidx.compose.runtime.rememberUpdatedState(versionIndices)
    val latestCurrentVersionPos by androidx.compose.runtime.rememberUpdatedState(currentVersionPos)
    val latestIsIntroMessage by androidx.compose.runtime.rememberUpdatedState(isIntroMessage)
    val latestOnUpdate by androidx.compose.runtime.rememberUpdatedState(onUpdate)
    val latestOnRegenerate by androidx.compose.runtime.rememberUpdatedState(onRegenerate)
    val latestOnAddCustomGreeting by androidx.compose.runtime.rememberUpdatedState(onAddCustomGreeting)

    val assistantSwipeModifier = if (!loading && !isIntroLocked) {
        Modifier.pointerInput(assistantNode.id) {
            var totalDrag = 0f
            detectHorizontalDragGestures(
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    totalDrag += dragAmount
                },
                onDragEnd = {
                    val pos = latestCurrentVersionPos
                    val indices = latestVersionIndices
                    if (totalDrag > swipeThresholdPx && pos > 0) {
                        val targetIndex = indices.getOrNull(pos - 1)
                        if (targetIndex != null) {
                            haptics.perform(HapticPattern.Pop)
                            latestOnUpdate(latestAssistantNode.copy(selectIndex = targetIndex))
                        }
                    } else if (totalDrag < -swipeThresholdPx && pos < indices.lastIndex) {
                        val targetIndex = indices.getOrNull(pos + 1)
                        if (targetIndex != null) {
                            haptics.perform(HapticPattern.Pop)
                            latestOnUpdate(latestAssistantNode.copy(selectIndex = targetIndex))
                        }
                    } else if (totalDrag < -swipeThresholdPx && pos >= indices.lastIndex) {
                        haptics.perform(HapticPattern.Pop)
                        if (latestIsIntroMessage) {
                            latestOnAddCustomGreeting()
                        } else {
                            latestOnRegenerate()
                        }
                    }
                    totalDrag = 0f
                },
                onDragCancel = { totalDrag = 0f }
            )
        }
    } else {
        Modifier
    }

    val assistantTimestamp = remember(assistantNode.currentMessage.createdAt) {
        formatMessageTimestamp(assistantNode.currentMessage.createdAt)
    }

    val chatAnimationsEnabled = LocalChatAnimationsEnabled.current

    val renderVersionContent: @Composable (
        targetIdx: Int,
        versionNodes: List<MessageNode>,
        versionSwipeModifier: Modifier,
    ) -> Unit = { targetIdx, versionNodes, versionSwipeModifier ->
        val allParts = remember(versionNodes) {
            versionNodes.flatMap { it.currentMessage.parts }
        }
        val pageAttachments = remember(allParts, defaultVideoLabel, defaultAudioLabel, context) {
            collectRenderableAttachments(
                context = context,
                parts = allParts,
                fallbackVideoLabel = defaultVideoLabel,
                fallbackAudioLabel = defaultAudioLabel,
            )
        }

        if (showAssistantBubbles) {
            val isTransparentStyle = effectiveDisplay.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.TRANSPARENT_BOTTOM_BLUR
            val isMessengerStyle = effectiveDisplay.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.MESSENGER
            val isGlassy = effectiveDisplay.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.BUBBLES
            val bubbleInterfaceStyle = effectiveDisplay.bubbleInterfaceStyle
            val glassAlpha = effectiveDisplay.chatBubbleAlpha
            val bubbleBlur = effectiveDisplay.chatBubbleBlur
            val bubbleTone = 0f
            val baseLuminance = effectiveDisplay.glassBaseLuminance
            val tintHue = effectiveDisplay.chatBubbleTintHue
            val tintAmount = effectiveDisplay.chatBubbleTintAmount

            val bubbleModifierLambda: @Composable (Modifier, Shape, Color) -> Modifier = { m: Modifier, shape: Shape, color: Color ->
                if (isTransparentStyle) {
                    m
                } else if (!isGlassy) {
                    if (bubbleBlur > 0f) {
                        m.domChatBlurEffect(
                            containerColor = color,
                            shape = shape,
                            blurRadius = bubbleBlur.dp
                        )
                    } else {
                        m
                    }
                } else when (bubbleInterfaceStyle) {
                    InterfaceStyle.FROSTED -> m.frostedGlassBubbleSurface(
                        shape = shape,
                        borderWidth = 0.5.dp,
                        specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                    )
                    InterfaceStyle.GLASS -> {
                        val specularBorder = LiquidGlassTokens.SpecularBorderSubtle.takeIf {
                            effectiveDisplay.bubbleSpecularIntensity > 0f
                        }
                        val bubbleFillColor = if (glassAlpha <= 0.02f) Color.Transparent else androidx.compose.ui.graphics.lerp(Color.Black, Color.White, bubbleTone.coerceIn(0f, 1f)).copy(alpha = glassAlpha.coerceIn(0f, 1f))
                        m.liquidGlassBubbleSurface(
                            shape = shape,
                            borderWidth = 0.5.dp,
                            fillColor = bubbleFillColor,
                            specularBrush = specularBorder ?: LiquidGlassTokens.SpecularBorderSubtle,
                            glassMode = effectiveDisplay.liquidGlassMode,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(versionSwipeModifier),
                verticalArrangement = Arrangement.spacedBy(elementSpacing)
            ) {
                if (pageAttachments.isNotEmpty()) {
                    AttachmentRow(
                        attachments = pageAttachments,
                        alignEnd = false,
                    )
                }

                val activeTextParts = allParts.filterIsInstance<UIMessagePart.Text>().filter { it.text.isNotBlank() }
                activeTextParts.forEachIndexed { index, part ->
                    val position = when {
                        activeTextParts.size == 1 -> BubblePosition.SINGLE
                        index == 0 -> BubblePosition.FIRST
                        index == activeTextParts.lastIndex -> BubblePosition.LAST
                        else -> BubblePosition.MIDDLE
                    }

                    GroupedMessageBubble(
                        position = position,
                        role = BubbleRole.ASSISTANT,
                        isTransparent = isTransparentStyle,
                        isGlassy = isGlassy,
                        glassAlpha = glassAlpha,
                        baseLuminance = baseLuminance,
                        tintHue = tintHue,
                        tintAmount = tintAmount,
                        bubbleTone = bubbleTone,
                        bubbleAlpha = glassAlpha,
                        largeRadius = if (isMessengerStyle) 14.dp else 20.dp,
                        smallRadius = if (isMessengerStyle) 4.dp else 6.dp,
                        modifier = Modifier.widthIn(max = maxWidth),
                        onClick = handleBubbleClick,
                        bubbleModifier = bubbleModifierLambda,
                    ) {
                        val userNickname = effectiveDisplay.getActivePersona().name
                        val isStreaming = loading && targetIdx == assistantNode.selectIndex && index == activeTextParts.lastIndex
                        val displayContent = rememberFormattedMessageText(
                            rawText = part.text,
                            assistant = assistant,
                            userNickname = userNickname,
                            scope = AssistantAffectScope.ASSISTANT,
                            isStreaming = isStreaming,
                        )
                        Box(
                            modifier = Modifier.smoothExpandingHeight(enabled = chatAnimationsEnabled)
                        ) {
                            MarkdownBlock(
                                workspaceId = assistant?.workspaceId?.toString(),
                                content = displayContent,
                                paragraphSpacing = 12.dp,
                                streamingTextReveal = isStreaming,
                                displaySetting = effectiveDisplay,
                                onRevealComplete = {
                                    if (index == activeTextParts.lastIndex) {
                                        isTextAnimating = false
                                    }
                                },
                                onExpandedStreamingCodeBlockChanged = {
                                    onExpandedStreamingCodeBlockChanged?.invoke()
                                    onStreamingRevealTick?.invoke()
                                },
                                onClickCitation = { id -> onCitationClick(id) }
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(versionSwipeModifier),
                verticalArrangement = Arrangement.spacedBy(elementSpacing)
            ) {
                if (pageAttachments.isNotEmpty()) {
                    AttachmentRow(
                        attachments = pageAttachments,
                        alignEnd = false,
                    )
                }

                val reasoningParts = allParts
                    .filterIsInstance<UIMessagePart.Reasoning>()
                    .filter { it.reasoning.isNotBlank() }

                if (reasoningParts.isNotEmpty()) {
                    val isReasoningActive = (targetIdx == assistantNode.selectIndex) &&
                        (reasoningParts.any { it.finishedAt == null } || (isTurnActive && loading))
                    AssistantThinkingDropdown(
                        reasoningParts = reasoningParts,
                        maxWidth = maxWidth,
                        isStreaming = isReasoningActive,
                    )
                }

                val activeTextParts = allParts.filterIsInstance<UIMessagePart.Text>().filter { it.text.isNotBlank() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(elementSpacing)
                ) {
                    activeTextParts.forEachIndexed { index, part ->
                        val userNickname = effectiveDisplay.getActivePersona().name
                        val isStreaming = loading && targetIdx == assistantNode.selectIndex && index == activeTextParts.lastIndex
                        val displayContent = rememberFormattedMessageText(
                            rawText = part.text,
                            assistant = assistant,
                            userNickname = userNickname,
                            scope = AssistantAffectScope.ASSISTANT,
                            isStreaming = isStreaming,
                        )
                        MarkdownBlock(
                            workspaceId = assistant?.workspaceId?.toString(),
                            content = displayContent,
                            paragraphSpacing = 12.dp,
                            streamingTextReveal = isStreaming,
                            displaySetting = effectiveDisplay,
                            onRevealComplete = {
                                if (index == activeTextParts.lastIndex) {
                                    isTextAnimating = false
                                }
                            },
                            onExpandedStreamingCodeBlockChanged = {
                                onExpandedStreamingCodeBlockChanged?.invoke()
                                onStreamingRevealTick?.invoke()
                            },
                            onClickCitation = { id -> onCitationClick(id) },
                            modifier = Modifier.clickable { handleBubbleClick() }
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(assistantSwipeModifier)
            .then(
                if (timelineOpen) {
                    Modifier.combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTimelineDismiss
                    )
                } else {
                    Modifier
                }
            ),
        verticalArrangement = Arrangement.spacedBy(
            if (showAssistantBubbles) elementSpacing else 3.dp
        )
    ) {
        if (showAssistantBubbles) {
            // Layout varies based on whether there's an activity bar
            if (hasInterestingActivity) {
                // WITH ACTIVITIES:
                // [Name] (if enabled)
                // [Avatar] [Pills row]
                // [Full-width bubble]
                
                // Name above pills (only if enabled)
                if (showModelName) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val isDark = LocalDarkMode.current
                        val headerNameColor = if (isDark) Color.White else Color(0xFF18181B)
                        val headerTimeColor = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF52525B)
                        val textShadow = Shadow(
                            color = if (isDark) Color.Black.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.45f),
                            offset = Offset(0f, 1f),
                            blurRadius = 3f
                        )
                        Text(
                            text = avatarName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = textShadow
                            ),
                            color = headerNameColor,
                            modifier = Modifier.graphicsLayer { alpha = nameAlpha.coerceAtLeast(0.85f) }
                        )
                        Text(
                            text = assistantTimestamp,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                shadow = textShadow
                            ),
                            color = headerTimeColor,
                        )
                    }
                }

                // Avatar + Pills row
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(elementSpacing)
                ) {
                    if (showIcon) {
                        UIAvatar(
                            name = avatarName,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                            value = avatarValue,
                            loading = loading,
                            onClick = onBotAvatarClick,
                        )
                    }

                    ActivityPillRow(
                        state = activityState,
                        onClick = { type ->
                            haptics.perform(HapticPattern.Pop)
                            onActivityPillClick(type)
                        },
                        connectsToBubbleBelow = false,  // Bubbles are separate - fully rounded
                        modifier = Modifier
                            .widthIn(max = maxWidth),
                        reasoningPreviewEnabled = effectiveDisplay.reasoningPreviewEnabled,
                        maxBubbleWidth = maxWidth,
                        timelineOpen = timelineOpen,
                        timelineEntries = timelineEntries,
                        initialTimelineOpenRequest = initialTimelineOpenRequest,
                        assistantId = assistant?.id?.toString(),
                        timelineLive = isTimelineLive,
                        onTimelineDismiss = {
                            haptics.perform(HapticPattern.Pop)
                            onTimelineDismiss()
                        },
                        key = group.firstNode.id,
                        displaySetting = effectiveDisplay,
                    )
                }
            } else {
                // WITHOUT ACTIVITIES:
                // [Avatar] [Name] (side by side)
                // [Full-width bubble]

                if (showIcon || showModelName) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (showIcon) {
                            UIAvatar(
                                name = avatarName,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                                value = avatarValue,
                                loading = loading,
                                onClick = onBotAvatarClick,
                            )
                        }
                        if (showModelName) {
                            val isDark = LocalDarkMode.current
                            val headerNameColor = if (isDark) Color.White else Color(0xFF18181B)
                            val headerTimeColor = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF52525B)
                            val textShadow = Shadow(
                                color = if (isDark) Color.Black.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.45f),
                                offset = Offset(0f, 1f),
                                blurRadius = 3f
                            )
                            Text(
                                text = avatarName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    shadow = textShadow
                                ),
                                color = headerNameColor,
                                modifier = Modifier.graphicsLayer { alpha = nameAlpha.coerceAtLeast(0.85f) }
                            )
                            Text(
                                text = assistantTimestamp,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    shadow = textShadow
                                ),
                                color = headerTimeColor,
                            )
                        }
                    }
                }
            }
        } else {
            // No bubbles for characters:
            // [Avatar] [Name] (side by side)
            // [Activity pills] below avatar row
            // [Full-width text]

            if (showIcon || showModelName) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showIcon) {
                        UIAvatar(
                            name = avatarName,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                            value = avatarValue,
                            loading = loading,
                            onClick = onBotAvatarClick,
                        )
                    }
                    if (showModelName) {
                        Text(
                            text = avatarName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.graphicsLayer { alpha = nameAlpha }
                        )
                    }
                }
            }

            if (activityState !is ActivityState.Hidden) {
                ActivityPillRow(
                    state = activityState,
                    onClick = { type ->
                        haptics.perform(HapticPattern.Pop)
                        onActivityPillClick(type)
                    },
                    connectsToBubbleBelow = false,
                    modifier = Modifier.widthIn(max = maxWidth),
                    reasoningPreviewEnabled = effectiveDisplay.reasoningPreviewEnabled,
                    maxBubbleWidth = maxWidth,
                    timelineOpen = timelineOpen,
                    timelineEntries = timelineEntries,
                    initialTimelineOpenRequest = initialTimelineOpenRequest,
                    assistantId = assistant?.id?.toString(),
                    timelineLive = isTimelineLive,
                    onTimelineDismiss = {
                        haptics.perform(HapticPattern.Pop)
                        onTimelineDismiss()
                    },
                    key = group.firstNode.id,
                    displaySetting = effectiveDisplay,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val currentVersionAllParts = remember(group.filteredNodes) {
            group.filteredNodes.flatMap { it.currentMessage.parts }
        }
        val currentVersionTextParts = remember(currentVersionAllParts) {
            currentVersionAllParts.filterIsInstance<UIMessagePart.Text>().filter { it.text.isNotBlank() }
        }

        if (versionIndices.size > 1 && showAssistantBubbles && currentVersionTextParts.size == 1 && chatAnimationsEnabled) {
            val isTransparentStyle = effectiveDisplay.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.TRANSPARENT_BOTTOM_BLUR
            val isMessengerStyle = effectiveDisplay.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.MESSENGER
            val isGlassy = effectiveDisplay.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.BUBBLES
            val bubbleInterfaceStyle = effectiveDisplay.bubbleInterfaceStyle
            val glassAlpha = effectiveDisplay.chatBubbleAlpha
            val bubbleBlur = effectiveDisplay.chatBubbleBlur
            val bubbleTone = 0f
            val baseLuminance = effectiveDisplay.glassBaseLuminance
            val tintHue = effectiveDisplay.chatBubbleTintHue
            val tintAmount = effectiveDisplay.chatBubbleTintAmount
            val currentPageAttachments = remember(currentVersionAllParts, defaultVideoLabel, defaultAudioLabel, context) {
                collectRenderableAttachments(
                    context = context,
                    parts = currentVersionAllParts,
                    fallbackVideoLabel = defaultVideoLabel,
                    fallbackAudioLabel = defaultAudioLabel,
                )
            }
            val morphBubbleModifierLambda: @Composable (Modifier, Shape, Color) -> Modifier = { m: Modifier, shape: Shape, color: Color ->
                if (isTransparentStyle) {
                    m
                } else if (!isGlassy) {
                    if (bubbleBlur > 0f) {
                        m.domChatBlurEffect(
                            containerColor = color,
                            shape = shape,
                            blurRadius = bubbleBlur.dp
                        )
                    } else {
                        m
                    }
                } else when (bubbleInterfaceStyle) {
                    InterfaceStyle.FROSTED -> m.frostedGlassBubbleSurface(
                        shape = shape,
                        borderWidth = 0.5.dp,
                        specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                    )
                    InterfaceStyle.GLASS -> {
                        val specularBorder = LiquidGlassTokens.SpecularBorderSubtle.takeIf {
                            effectiveDisplay.bubbleSpecularIntensity > 0f
                        }
                        val bubbleFillColor = if (glassAlpha <= 0.02f) Color.Transparent else androidx.compose.ui.graphics.lerp(Color.Black, Color.White, bubbleTone.coerceIn(0f, 1f)).copy(alpha = glassAlpha.coerceIn(0f, 1f))
                        m.liquidGlassBubbleSurface(
                            shape = shape,
                            borderWidth = 0.5.dp,
                            fillColor = bubbleFillColor,
                            specularBrush = specularBorder ?: LiquidGlassTokens.SpecularBorderSubtle,
                            glassMode = effectiveDisplay.liquidGlassMode,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(elementSpacing)
            ) {
                if (currentPageAttachments.isNotEmpty()) {
                    AttachmentRow(
                        attachments = currentPageAttachments,
                        alignEnd = false,
                    )
                }

                GroupedMessageBubble(
                    position = BubblePosition.SINGLE,
                    role = BubbleRole.ASSISTANT,
                    isTransparent = isTransparentStyle,
                    isGlassy = isGlassy,
                    glassAlpha = glassAlpha,
                    baseLuminance = baseLuminance,
                    tintHue = tintHue,
                    tintAmount = tintAmount,
                    bubbleTone = bubbleTone,
                    bubbleAlpha = glassAlpha,
                    largeRadius = if (isMessengerStyle) 14.dp else 20.dp,
                    smallRadius = if (isMessengerStyle) 4.dp else 6.dp,
                    modifier = Modifier.widthIn(max = maxWidth),
                    onClick = handleBubbleClick,
                    bubbleModifier = morphBubbleModifierLambda,
                ) {
                    Box(modifier = Modifier.smoothExpandingHeight(enabled = chatAnimationsEnabled)) {
                        AnimatedContent(
                            targetState = assistantNode.selectIndex,
                            transitionSpec = {
                                val forward = targetState >= initialState
                                (fadeIn(animationSpec = tween(240, delayMillis = 50, easing = LinearOutSlowInEasing)) +
                                    slideInHorizontally(animationSpec = tween(260, easing = LinearOutSlowInEasing)) { width ->
                                        if (forward) (width * 0.14f).toInt() else (-width * 0.14f).toInt()
                                    }) togetherWith
                                (fadeOut(animationSpec = tween(150, easing = FastOutLinearInEasing)) +
                                    slideOutHorizontally(animationSpec = tween(180, easing = FastOutLinearInEasing)) { width ->
                                        if (forward) (-width * 0.14f).toInt() else (width * 0.14f).toInt()
                                    }) using
                                SizeTransform(clip = false) { _, _ -> snap() }
                            },
                            contentAlignment = Alignment.TopStart,
                            label = "assistant_bubble_inner_version_switch"
                        ) { targetIdx ->
                            val versionNodes = remember(group, targetIdx) {
                                group.filteredNodesForVersion(targetIdx)
                            }
                            val versionTextParts = remember(versionNodes) {
                                versionNodes
                                    .flatMap { it.currentMessage.parts }
                                    .filterIsInstance<UIMessagePart.Text>()
                                    .filter { it.text.isNotBlank() }
                            }
                            val userNickname = effectiveDisplay.getActivePersona().name
                            val isStreaming = loading && targetIdx == assistantNode.selectIndex
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                versionTextParts.forEachIndexed { index, part ->
                                    val isPartStreaming = isStreaming && index == versionTextParts.lastIndex
                                    val displayContent = rememberFormattedMessageText(
                                        rawText = part.text,
                                        assistant = assistant,
                                        userNickname = userNickname,
                                        scope = AssistantAffectScope.ASSISTANT,
                                        isStreaming = isPartStreaming,
                                    )
                                    MarkdownBlock(
                                        workspaceId = assistant?.workspaceId?.toString(),
                                        content = displayContent,
                                        paragraphSpacing = 12.dp,
                                        streamingTextReveal = isPartStreaming,
                                        displaySetting = effectiveDisplay,
                                        onRevealComplete = {
                                            if (index == versionTextParts.lastIndex) {
                                                isTextAnimating = false
                                            }
                                        },
                                        onExpandedStreamingCodeBlockChanged = {
                                            onExpandedStreamingCodeBlockChanged?.invoke()
                                            onStreamingRevealTick?.invoke()
                                        },
                                        onClickCitation = { id -> onCitationClick(id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (versionIndices.size > 1 && chatAnimationsEnabled) {
            AnimatedContent(
                targetState = assistantNode.selectIndex,
                transitionSpec = {
                    val forward = targetState >= initialState
                    (fadeIn(animationSpec = tween(240, delayMillis = 50, easing = LinearOutSlowInEasing)) +
                        slideInHorizontally(animationSpec = tween(260, easing = LinearOutSlowInEasing)) { width ->
                            if (forward) (width * 0.14f).toInt() else (-width * 0.14f).toInt()
                        }) togetherWith
                    (fadeOut(animationSpec = tween(150, easing = FastOutLinearInEasing)) +
                        slideOutHorizontally(animationSpec = tween(180, easing = FastOutLinearInEasing)) { width ->
                            if (forward) (-width * 0.14f).toInt() else (width * 0.14f).toInt()
                        }) using
                    SizeTransform(clip = false)
                },
                contentAlignment = Alignment.TopStart,
                label = "assistant_version_content_switch"
            ) { targetIdx ->
                val versionNodes = remember(group, targetIdx) {
                    group.filteredNodesForVersion(targetIdx)
                }
                renderVersionContent(targetIdx, versionNodes, Modifier)
            }
        } else {
            renderVersionContent(
                assistantNode.selectIndex,
                group.filteredNodes,
                Modifier,
            )
        }
        
        // Token statistics - combined for all messages in group
        val usage = group.combinedUsage
        if (showTokenUsage && usage != null && !isTurnActive) {
            TokenStatisticsInline(
                usage = usage,
                generationDurationMs = group.combinedGenerationDurationMs,
                modifier = Modifier
            )
        }
        
        // Action buttons
        val showActions = !isTurnActive && (
            actionsExpanded ||
            (isIntroMessage && !isIntroLocked) ||
            currentVersionTextParts.isEmpty()
        )
        
        AnimatedVisibility(
            visible = showActions,
            enter = expandVertically(spring(dampingRatio = 0.7f, stiffness = 300f)) +
                    slideInVertically(spring(dampingRatio = 0.6f, stiffness = 300f)) { -it } +
                    fadeIn(spring(dampingRatio = 0.8f, stiffness = 400f)),
            exit = shrinkVertically(spring(dampingRatio = 0.8f, stiffness = 400f)) +
                   slideOutVertically(spring(dampingRatio = 0.8f, stiffness = 500f)) { -it } +
                   fadeOut()
        ) {
            Column {
                ChatMessageActionButtons(
                    message = group.lastNode.currentMessage,
                    onRegenerate = onRegenerate,
                    node = group.nodeWithMostVersions,
                    onUpdate = onUpdate,
                    showRegenerate = showRegenerate,
                    onOpenActionSheet = onOpenActionSheet,
                    onEditLorebookEntry = onEditLorebookEntry,
                    onModeClick = onModeClick,
                    onMemoryClick = onMemoryClick,
                    ttsProviderOverride = ttsProviderOverride,
                    onEdit = { onEdit?.invoke(group.lastNode) },
                    allowVersionSwitching = !isIntroLocked,
                    isIntroMessage = isIntroMessage,
                    onAddCustomGreeting = onAddCustomGreeting,
                    onDeleteSwipe = onDeleteSwipe,
                )
            }
        }
    }

    if (previewBotAvatarUrl != null) {
        me.lucid.domchat.ui.components.ui.ImagePreviewDialog(
            images = listOf(previewBotAvatarUrl!!),
            onDismissRequest = { previewBotAvatarUrl = null }
        )
    }
}

/**
 * Inline token statistics display.
 */
@Composable
private fun TokenStatisticsInline(
    usage: TokenUsage,
    generationDurationMs: Long?,
    modifier: Modifier = Modifier
) {
    val grayColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val promptTokensText = stringResource(R.string.chat_message_tokens, usage.promptTokens.formatNumber())
    val cachedTokensText = if (usage.cachedTokens > 0) {
        stringResource(R.string.chat_message_cached, usage.cachedTokens.formatNumber())
    } else {
        null
    }
    val completionTokensText = stringResource(
        R.string.chat_message_tokens,
        usage.completionTokens.formatNumber()
    )
    
    // Calculate tokens per second
    val tokensPerSecond: Float? = generationDurationMs?.let { durationMs ->
        if (durationMs > 0) {
            (usage.completionTokens / (durationMs / 1000.0)).toFloat()
        } else null
    }
    
    Row(
        modifier = modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sent tokens
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowUpward,
                contentDescription = stringResource(R.string.chat_message_sent),
                modifier = Modifier.size(14.dp),
                tint = grayColor
            )
            Text(
                text = buildString {
                    append(promptTokensText)
                    if (cachedTokensText != null) {
                        append(" ")
                        append(cachedTokensText)
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = grayColor
            )
        }
        
        // Received tokens
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowDownward,
                contentDescription = stringResource(R.string.chat_message_received),
                modifier = Modifier.size(14.dp),
                tint = grayColor
            )
            Text(
                text = completionTokensText,
                style = MaterialTheme.typography.labelSmall,
                color = grayColor
            )
        }
        
        // Tokens per second
        if (tokensPerSecond != null && tokensPerSecond > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = stringResource(R.string.chat_message_speed),
                    modifier = Modifier.size(14.dp),
                    tint = grayColor
                )
                Text(
                    text = stringResource(R.string.chat_message_tokens_per_second, tokensPerSecond),
                    style = MaterialTheme.typography.labelSmall,
                    color = grayColor
                )
            }
        }
    }
}

@Composable
private fun AssistantThinkingDropdown(
    reasoningParts: List<UIMessagePart.Reasoning>,
    maxWidth: androidx.compose.ui.unit.Dp,
    isStreaming: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val totalReasoningText = remember(reasoningParts) {
        reasoningParts.joinToString("\n\n") { it.reasoning }
    }
    val totalDurationMs = remember(reasoningParts) {
        reasoningParts.mapNotNull { it.finishedAt?.minus(it.createdAt)?.inWholeMilliseconds }.sum()
    }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(0.82f, 300f),
        label = "thinking_chevron"
    )

    val display = LocalDisplaySetting.current
    val isTransparentStyle = display.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.TRANSPARENT_BOTTOM_BLUR
    val glassAlpha = display.chatBubbleAlpha
    val bubbleBlur = display.chatBubbleBlur
    val bubbleTone = 0f
    val tintHue = display.chatBubbleTintHue
    val tintAmount = display.chatBubbleTintAmount

    val tone = bubbleTone.coerceIn(0f, 1f)
    val effectiveAlpha = glassAlpha.coerceIn(0f, 1f)
    val neutralTone = androidx.compose.ui.graphics.lerp(Color.Black, Color.White, tone)
    val baseTone = if (tintAmount > 0.02f) {
        val hsvTint = Color.hsv(tintHue.coerceIn(0f, 360f), 0.75f, 1.0f)
        androidx.compose.ui.graphics.lerp(neutralTone, hsvTint, tintAmount.coerceIn(0f, 1f))
    } else {
        neutralTone
    }

    val shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val resolvedContentColor = when {
        isTransparentStyle -> MaterialTheme.colorScheme.onSurface
        effectiveAlpha <= 0.05f -> if (isDark) Color(0xFFF2F2F7) else Color(0xFF14151C)
        tone >= 0.55f -> Color(0xFF14151C)
        else -> Color(0xFFF2F2F7)
    }

    val reasoningMotionSpec = remember {
        spring<androidx.compose.ui.unit.IntSize>(
            dampingRatio = 0.72f,
            stiffness = 280f
        )
    }

    Column(
        modifier = modifier
            .widthIn(max = maxWidth)
            .animateContentSize(animationSpec = reasoningMotionSpec)
            .then(
                if (isTransparentStyle) {
                    Modifier
                } else when (display.bubbleInterfaceStyle) {
                    InterfaceStyle.FROSTED -> Modifier.frostedGlassBubbleSurface(
                        shape = shape,
                        borderWidth = 0.5.dp,
                        specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                    )
                    InterfaceStyle.GLASS -> {
                        val specularBorder = LiquidGlassTokens.SpecularBorderSubtle.takeIf {
                            display.bubbleSpecularIntensity > 0f
                        }
                        val bubbleFillColor = if (effectiveAlpha <= 0.02f) Color.Transparent else baseTone.copy(alpha = effectiveAlpha)
                        Modifier.liquidGlassBubbleSurface(
                            shape = shape,
                            borderWidth = 0.5.dp,
                            fillColor = bubbleFillColor,
                            specularBrush = specularBorder ?: LiquidGlassTokens.SpecularBorderSubtle,
                            glassMode = display.liquidGlassMode,
                        )
                    }
                }
            )
            .padding(horizontal = if (isTransparentStyle) 4.dp else 12.dp, vertical = if (isTransparentStyle) 4.dp else 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { expanded = !expanded }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (totalDurationMs > 0) {
                        "Thought for ${"%.1f".format(totalDurationMs / 1000.0)}s"
                    } else {
                        "Thinking process"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = resolvedContentColor.copy(alpha = 0.9f)
                )
            }
            Icon(
                imageVector = Icons.Rounded.ArrowDownward,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = rotation },
                tint = resolvedContentColor.copy(alpha = 0.7f)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 280f),
                expandFrom = Alignment.Top
            ) + fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)),
            exit = shrinkVertically(
                animationSpec = spring(dampingRatio = 0.76f, stiffness = 320f),
                shrinkTowards = Alignment.Top
            ) + fadeOut(animationSpec = tween(180, easing = LinearOutSlowInEasing))
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 6.dp)
            ) {
                MarkdownBlock(
                    content = totalReasoningText,
                    paragraphSpacing = 8.dp,
                    streamingTextReveal = isStreaming,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = resolvedContentColor.copy(alpha = 0.85f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    onClickCitation = {}
                )
            }
        }
    }
}
