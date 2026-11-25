package me.lucid.domchat.ui.components.chat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import me.lucid.domchat.R
import me.lucid.domchat.ui.context.LocalDisplaySetting
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassBubbleSurface
import me.lucid.domchat.ui.theme.frostedGlassBubbleSurface
import me.lucid.domchat.data.datastore.InterfaceStyle
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import androidx.compose.ui.graphics.luminance
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.components.richtext.updatePreviewAutoFollowPaused
import me.lucid.domchat.ui.modifier.fadeEdges
import me.lucid.domchat.ui.modifier.shimmer
sealed interface ActivityState {
    /** Waiting for first token - shows typing dots */
    data object Waiting : ActivityState
    
    /** OCR is preprocessing attachments before generation starts */
    data object Ocr : ActivityState

    /** Model is reasoning/thinking - shows timer */
    data class Reasoning(
        val startTimeMs: Long = System.currentTimeMillis(),
        val title: String? = null,
        val reasoningText: String = ""
    ) : ActivityState

    /** Model is using a tool */
    data class ToolUse(
        val toolName: String,
        val displayName: String,
        val startTimeMs: Long = System.currentTimeMillis()
    ) : ActivityState
    
    /** Local model is being loaded into memory */
    data class LoadingModel(
        val modelName: String? = null
    ) : ActivityState
    
    /** Model is generating text reply */
    data object Replying : ActivityState
    
    /** No activities happened - hide the pill */
    data object Hidden : ActivityState
    
    /** Single activity completed - show expanded pill */
    data class CompletedSingle(
        val type: ActivityType,
        val durationMs: Long? = null,
        val toolName: String? = null,
        val displayName: String? = null,
        val count: Int = 1  // Number of times this activity occurred
    ) : ActivityState
    
    /** Multiple activities completed - show compact pills */
    data class CompletedMultiple(
        val reasoningDurationMs: Long? = null,
        val activityTypes: List<ActivityType> = emptyList()
    ) : ActivityState
}

/**
 * Convert ActivityState to a key for AnimatedContent.
 * Same key = no transition animation.
 * 
 * For ToolUse, we group by tool category (e.g., all web searches share the same key)
 * so consecutive searches don't trigger transitions.
 */
private fun stateToKey(state: ActivityState): Any = when (state) {
    is ActivityState.Waiting -> "waiting"
    is ActivityState.Ocr -> "ocr"
    is ActivityState.Reasoning -> "reasoning"
    is ActivityState.ToolUse -> "tool_${categorizeToolName(state.toolName)}"
    is ActivityState.LoadingModel -> "loading_model"
    is ActivityState.Replying -> "replying"
    is ActivityState.Hidden -> "hidden"
    is ActivityState.CompletedSingle -> "completed_single_${state.type}"
    is ActivityState.CompletedMultiple -> "completed_multi"
}

/**
 * Represents a single activity that happened during the turn.
 */
data class ActivityItem(
    val type: ActivityType,
    val durationMs: Long? = null,  // For reasoning
    val count: Int = 1,            // How many times this happened
    val displayName: String? = null // For tools
)

enum class ActivityType {
    REASONING,
    OCR,
    SEARCH,
    MEMORY_RECALL,
    PYTHON,
    WORKSPACE,
    SKILL,
    MCP,
    LOADING_MODEL,
    TOOL_OTHER
}

private fun ActivityType.toTestTag(): String = when (this) {
    ActivityType.REASONING -> "activity_pill_reasoning"
    ActivityType.OCR -> "activity_pill_ocr"
    ActivityType.SEARCH -> "activity_pill_search"
    ActivityType.MEMORY_RECALL -> "activity_pill_memory_recall"
    ActivityType.PYTHON -> "activity_pill_python"
    ActivityType.WORKSPACE -> "activity_pill_workspace"
    ActivityType.SKILL -> "activity_pill_skill"
    ActivityType.MCP -> "activity_pill_mcp"
    ActivityType.LOADING_MODEL -> "activity_pill_loading_model"
    ActivityType.TOOL_OTHER -> "activity_pill_tool_other"
}

/**
 * Get the icon for an activity type.
 */
private fun ActivityType.getIcon(): ImageVector = when (this) {
    ActivityType.REASONING -> Icons.Rounded.Lightbulb
    ActivityType.OCR -> Icons.Rounded.Image
    ActivityType.SEARCH -> Icons.Rounded.Public
    ActivityType.MEMORY_RECALL -> Icons.Rounded.Memory
    ActivityType.PYTHON -> Icons.Rounded.Terminal
    ActivityType.WORKSPACE -> Icons.Rounded.Computer
    ActivityType.SKILL -> Icons.Rounded.Category
    ActivityType.MCP -> Icons.Rounded.Memory
    ActivityType.LOADING_MODEL -> Icons.Rounded.Memory
    ActivityType.TOOL_OTHER -> Icons.Rounded.Build
}

/**
 * Get display text for an activity type (for expanded single pill).
 */
private fun ActivityType.getDisplayText(): String = when (this) {
    ActivityType.REASONING -> "Reasoned"
    ActivityType.OCR -> "OCR"
    ActivityType.SEARCH -> "Searched"
    ActivityType.MEMORY_RECALL -> "Recalled"
    ActivityType.PYTHON -> "Ran Python"
    ActivityType.WORKSPACE -> "Used workspace"
    ActivityType.SKILL -> "Skills"
    ActivityType.MCP -> "MCP"
    ActivityType.LOADING_MODEL -> "Loaded model"
    ActivityType.TOOL_OTHER -> "Used tools"
}

/**
 * Categorize a tool name into activity type.
 */
internal fun categorizeToolName(toolName: String): ActivityType = when (toolName) {
    "search_web", "scrape_web" -> ActivityType.SEARCH
    "search_memory" -> ActivityType.MEMORY_RECALL
    "eval_python", "pip_install", "write_sandbox_file", 
    "read_sandbox_file", "list_sandbox_files", "delete_sandbox_file" -> ActivityType.PYTHON
    "workspace_read_file", "workspace_write_file", "workspace_edit_file", "workspace_shell", "workspace_view_image" -> ActivityType.WORKSPACE
    "manage_skills" -> ActivityType.SKILL
    else -> if (toolName.startsWith("mcp_")) ActivityType.MCP else ActivityType.TOOL_OTHER
}

private val pythonToolNames = setOf(
    "eval_python",
    "pip_install",
    "write_sandbox_file",
    "read_sandbox_file",
    "list_sandbox_files",
    "delete_sandbox_file"
)

private val workspaceToolNames = setOf(
    "workspace_read_file",
    "workspace_write_file",
    "workspace_edit_file",
    "workspace_shell",
    "workspace_view_image",
)

internal fun resolveActivityToolName(toolName: String, arguments: String): String {
    val normalized = toolName.trim()
    if (normalized in pythonToolNames || normalized in workspaceToolNames) {
        return normalized
    }
    if (normalized.length >= 10 && workspaceToolNames.any { it.startsWith(normalized) }) {
        return workspaceToolNames.first { it.startsWith(normalized) }
    }
    if (normalized.length >= 3 && pythonToolNames.any { it.startsWith(normalized) }) {
        return "eval_python"
    }
    if (normalized.isBlank() && arguments.looksLikePythonToolArguments()) {
        return "eval_python"
    }
    return normalized
}

private fun String.looksLikePythonToolArguments(): Boolean {
    if (isBlank()) return false
    return contains("\"code\"") ||
        contains("'code'") ||
        contains("\\\"code\\\"")
}


/**
 * Build activity items from a CompletedMultiple state.
 */
fun buildActivityItemsFromMultiple(state: ActivityState.CompletedMultiple): List<ActivityItem> {
    val items = mutableListOf<ActivityItem>()
    
    // Add reasoning if present
    if (state.reasoningDurationMs != null) {
        items.add(ActivityItem(
            type = ActivityType.REASONING,
            durationMs = state.reasoningDurationMs
        ))
    }
    
    if (state.activityTypes.isNotEmpty()) {
        state.activityTypes.distinct().forEach { type ->
            items.add(ActivityItem(type = type))
        }
    }
    
    return items
}

// Corner radius constants
private val LARGE_RADIUS = 20.dp
private val SMALL_RADIUS = 6.dp
private val PILL_HEIGHT = 36.dp
private val PILL_MORPH_SPEC = spring<IntSize>(dampingRatio = 0.72f, stiffness = 280f)
private val PILL_COLLAPSE_SPEC = spring<IntSize>(dampingRatio = 0.78f, stiffness = 440f)
private val PILL_LIVE_GROWTH_SPEC = spring<IntSize>(dampingRatio = 0.80f, stiffness = 340f)
private val PILL_CORNER_SPEC = spring<Dp>(dampingRatio = 0.72f, stiffness = 280f)

/**
 * Position of a pill in a row of pills.
 */
enum class PillPosition {
    SINGLE,     // Only one pill - fully rounded
    FIRST,      // First in row - rounded left, flat right
    MIDDLE,     // Middle pills - flat both sides
    LAST        // Last in row - flat left, rounded right
}

private sealed interface SinglePillContentState {
    class Compact(val state: ActivityState) : SinglePillContentState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Compact) return false
            val s1 = this.state
            val s2 = other.state
            if (s1 === s2) return true
            if (s1 is ActivityState.Reasoning && s2 is ActivityState.Reasoning) {
                return s1.startTimeMs == s2.startTimeMs && s1.title == s2.title
            }
            return s1 == s2
        }

        override fun hashCode(): Int {
            return if (state is ActivityState.Reasoning) {
                31 * state.startTimeMs.hashCode() + (state.title?.hashCode() ?: 0)
            } else {
                state.hashCode()
            }
        }
    }
    data class ExpandedReasoning(
        val state: ActivityState.Reasoning,
        val durationMs: Long? = null,
        val isLive: Boolean = true
    ) : SinglePillContentState
    data class ExpandedTimeline(
        val entries: List<TimelineEntry>,
        val initialRequest: TimelineOpenRequest?,
        val assistantId: String?,
        val scrollHandoffMode: TimelineScrollHandoffMode,
        val isLive: Boolean,
    ) : SinglePillContentState
}

private fun SinglePillContentState.isUnifiedReasoning(): Boolean = when (this) {
    is SinglePillContentState.ExpandedReasoning -> true
    is SinglePillContentState.Compact -> state is ActivityState.Reasoning ||
        (state is ActivityState.CompletedSingle && state.type == ActivityType.REASONING)
    else -> false
}

/**
 * A row of activity pills with Apple-like smooth animations.
 * 
 * During loading: Shows a single morphing pill (Waiting → Reasoning → Tool → etc.)
 * After completion: If multiple activities, reveals them with staggered fly-out animation
 */
@Composable
internal fun ActivityPillRow(
    state: ActivityState,
    onClick: (ActivityType?) -> Unit,
    modifier: Modifier = Modifier,
    connectsToBubbleBelow: Boolean = true,
    reasoningPreviewEnabled: Boolean = false,
    maxBubbleWidth: Dp = Dp.Infinity,
    timelineOpen: Boolean = false,
    timelineEntries: List<TimelineEntry> = emptyList(),
    initialTimelineOpenRequest: TimelineOpenRequest? = null,
    assistantId: String? = null,
    timelineScrollHandoffMode: TimelineScrollHandoffMode = TimelineScrollHandoffMode.EdgeGatedToParent,
    timelineLive: Boolean = false,
    onTimelineDismiss: () -> Unit = { onClick(null) },
    key: Any? = null,
    displaySetting: me.lucid.domchat.data.datastore.DisplaySetting? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "pill_scale"
    )
    
    // Build activity items for multi-pill state
    val activityItems = remember(state) {
        if (state is ActivityState.CompletedMultiple) buildActivityItemsFromMultiple(state) else emptyList()
    }

    val wasCompletedInitially = remember(key) { state is ActivityState.CompletedMultiple || state is ActivityState.CompletedSingle }
    
    // Animated visibility for the entire pill row
    if (wasCompletedInitially) {
        if (state !is ActivityState.Hidden) {
            Row(
                modifier = modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedSinglePill(
                    state = state,
                    onClick = onClick,
                    connectsToBubbleBelow = connectsToBubbleBelow,
                    reasoningPreviewEnabled = reasoningPreviewEnabled,
                    maxBubbleWidth = maxBubbleWidth,
                    timelineOpen = timelineOpen,
                    timelineEntries = timelineEntries,
                    initialTimelineOpenRequest = initialTimelineOpenRequest,
                    assistantId = assistantId,
                    timelineScrollHandoffMode = timelineScrollHandoffMode,
                    timelineLive = timelineLive,
                    onTimelineDismiss = onTimelineDismiss,
                    wasCompletedInitially = wasCompletedInitially,
                    key = key,
                    displaySetting = displaySetting,
                )
            }
        }
    } else {
        AnimatedVisibility(
            visible = state !is ActivityState.Hidden,
            enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.9f),
            exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.9f)
        ) {
            Row(
                modifier = modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedSinglePill(
                    state = state,
                    onClick = onClick,
                    connectsToBubbleBelow = connectsToBubbleBelow,
                    reasoningPreviewEnabled = reasoningPreviewEnabled,
                    maxBubbleWidth = maxBubbleWidth,
                    timelineOpen = timelineOpen,
                    timelineEntries = timelineEntries,
                    initialTimelineOpenRequest = initialTimelineOpenRequest,
                    assistantId = assistantId,
                    timelineScrollHandoffMode = timelineScrollHandoffMode,
                    timelineLive = timelineLive,
                    onTimelineDismiss = onTimelineDismiss,
                    wasCompletedInitially = wasCompletedInitially,
                    key = key,
                    displaySetting = displaySetting,
                )
            }
        }
    }
}

/**
 * Animated single pill that smoothly morphs between states.
 * Uses AnimatedContent for crossfade and smooth size transitions.
 */
@Composable
private fun AnimatedSinglePill(
    state: ActivityState,
    onClick: (ActivityType?) -> Unit,
    connectsToBubbleBelow: Boolean,
    reasoningPreviewEnabled: Boolean,
    maxBubbleWidth: Dp,
    timelineOpen: Boolean,
    timelineEntries: List<TimelineEntry>,
    initialTimelineOpenRequest: TimelineOpenRequest?,
    assistantId: String?,
    timelineScrollHandoffMode: TimelineScrollHandoffMode,
    timelineLive: Boolean,
    onTimelineDismiss: () -> Unit,
    wasCompletedInitially: Boolean,
    key: Any? = null,
    displaySetting: me.lucid.domchat.data.datastore.DisplaySetting? = null,
) {
    val isExpandedReasoning = reasoningPreviewEnabled && state is ActivityState.Reasoning && !timelineOpen
    val requestedContentState = if (timelineOpen && timelineEntries.isNotEmpty()) {
        if (timelineEntries.size == 1 && timelineEntries.first() is TimelineEntry.Reasoning) {
            val reasoningEntry = timelineEntries.first() as TimelineEntry.Reasoning
            val isLive = timelineLive && reasoningEntry.isInProgress
            SinglePillContentState.ExpandedReasoning(
                state = ActivityState.Reasoning(
                    startTimeMs = (state as? ActivityState.Reasoning)?.startTimeMs
                        ?: (System.currentTimeMillis() - reasoningEntry.durationMs),
                    title = reasoningEntry.title,
                    reasoningText = reasoningEntry.content
                ),
                durationMs = reasoningEntry.durationMs,
                isLive = isLive
            )
        } else {
            SinglePillContentState.ExpandedTimeline(
                entries = timelineEntries,
                initialRequest = initialTimelineOpenRequest,
                assistantId = assistantId,
                scrollHandoffMode = timelineScrollHandoffMode,
                isLive = timelineLive,
            )
        }
    } else if (isExpandedReasoning && state is ActivityState.Reasoning) {
        SinglePillContentState.ExpandedReasoning(
            state = state,
            durationMs = null,
            isLive = timelineLive
        )
    } else {
        SinglePillContentState.Compact(state)
    }
    val surfaceExpanded = requestedContentState !is SinglePillContentState.Compact
    val isExpandedTimeline = requestedContentState is SinglePillContentState.ExpandedTimeline
    // Match the inner ActivityTimelinePanel shape (InputField = 24dp) when expanded,
    // otherwise the outer pill background peeks out around the inner corners.
    val expandedRadius = 24.dp

    // Animate corner radius once (all 4 corners share the same radius)
    val animatedCornerRadius by animateDpAsState(
        targetValue = if (surfaceExpanded) expandedRadius else 18.dp,
        animationSpec = PILL_CORNER_SPEC,
        label = "pill_corner_radius"
    )
    val topStartRadius = animatedCornerRadius
    val topEndRadius = animatedCornerRadius
    val bottomStartRadius = animatedCornerRadius
    val bottomEndRadius = animatedCornerRadius
    
    val isMultipleMinimized = !surfaceExpanded && state is ActivityState.CompletedMultiple

    val display = LocalDisplaySetting.current
    val isTransparentStyle = display.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.TRANSPARENT_BOTTOM_BLUR
    val isGlassy = display.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.BUBBLES
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

    val isGlassyPill = !isMultipleMinimized && !isExpandedTimeline

    val pillContainerColor = if (isTransparentStyle) {
        Color.Transparent
    } else if (isGlassy) {
        if (effectiveAlpha <= 0.02f) Color.Transparent else baseTone.copy(alpha = effectiveAlpha)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val pillSpecularBorder = if (isTransparentStyle || !isGlassyPill || (!isGlassy && bubbleBlur <= 0f)) null else androidx.compose.foundation.BorderStroke(
        if (effectiveAlpha <= 0.05f) 0.75.dp else 0.5.dp,
        androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(
                androidx.compose.ui.graphics.Color.White.copy(alpha = if (effectiveAlpha <= 0.05f) 0.40f else if (isGlassy) 0.35f else 0.25f),
                androidx.compose.ui.graphics.Color.White.copy(alpha = if (effectiveAlpha <= 0.05f) 0.18f else if (isGlassy) 0.12f else 0.06f),
                androidx.compose.ui.graphics.Color.White.copy(alpha = if (effectiveAlpha <= 0.05f) 0.08f else 0f)
            )
        )
    )

    val targetPillColor = if (isMultipleMinimized || isExpandedTimeline) {
        Color.Transparent
    } else {
        pillContainerColor
    }

    val pillColor by animateColorAsState(
        targetValue = targetPillColor,
        animationSpec = tween(150),
        label = "pill_color"
    )
    val pillShape = RoundedCornerShape(
            topStart = topStartRadius,
            topEnd = topEndRadius,
            bottomStart = bottomStartRadius,
            bottomEnd = bottomEndRadius
        )
    val testTag = when (state) {
        is ActivityState.Ocr -> ActivityType.OCR
        is ActivityState.Reasoning -> ActivityType.REASONING
        is ActivityState.ToolUse -> categorizeToolName(state.toolName)
        is ActivityState.CompletedSingle -> state.type
        else -> null
    }?.toTestTag()

    val chatAnimationsEnabled = me.lucid.domchat.ui.context.LocalChatAnimationsEnabled.current

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val resolvedContentColor = if (isGlassyPill && (isGlassy || isTransparentStyle)) {
        when {
            isTransparentStyle -> MaterialTheme.colorScheme.onSurface
            effectiveAlpha <= 0.05f -> if (isDark) Color(0xFFF2F2F7) else Color(0xFF14151C)
            tone >= 0.55f -> Color(0xFF14151C)
            else -> Color(0xFFF2F2F7)
        }
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val pillSurface = @Composable {
        Surface(
            modifier = Modifier
                .widthIn(max = maxBubbleWidth)
                .then(
                    if (isGlassyPill && !isTransparentStyle && (isGlassy || bubbleBlur > 0f)) {
                        val specularBorder = LiquidGlassTokens.SpecularBorderSubtle.takeIf {
                            display.bubbleSpecularIntensity > 0f
                        }
                        val bubbleFillColor = if (effectiveAlpha <= 0.02f) {
                            Color.Transparent
                        } else {
                            baseTone.copy(alpha = effectiveAlpha)
                        }
                        when (display.bubbleInterfaceStyle) {
                            InterfaceStyle.FROSTED -> Modifier.frostedGlassBubbleSurface(
                                shape = pillShape,
                                borderWidth = 0.5.dp,
                                specularBrush = specularBorder ?: LiquidGlassTokens.SpecularBorderSubtle,
                            )
                            InterfaceStyle.GLASS -> Modifier.liquidGlassBubbleSurface(
                                shape = pillShape,
                                borderWidth = 0.5.dp,
                                fillColor = bubbleFillColor,
                                specularBrush = specularBorder ?: LiquidGlassTokens.SpecularBorderSubtle,
                                glassMode = display.liquidGlassMode,
                            )
                        }
                    } else Modifier
                )
                .clip(pillShape)
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
            shape = pillShape,
            color = if (isGlassyPill) Color.Transparent else pillColor,
            border = if (isGlassyPill) null else pillSpecularBorder,
            contentColor = resolvedContentColor,
            onClick = {
                val clickType = when (state) {
                    is ActivityState.Ocr -> ActivityType.OCR
                    is ActivityState.Reasoning -> ActivityType.REASONING
                    is ActivityState.ToolUse -> categorizeToolName(state.toolName)
                    is ActivityState.CompletedSingle -> state.type
                    else -> null
                }
                onClick(clickType)
            }
        ) {
        AnimatedContent(
            targetState = requestedContentState,
            modifier = if (chatAnimationsEnabled) {
                val sizeSpec = when {
                    isExpandedReasoning && state is ActivityState.Reasoning -> PILL_LIVE_GROWTH_SPEC
                    !surfaceExpanded && !timelineOpen && state is ActivityState.Replying -> PILL_COLLAPSE_SPEC
                    else -> PILL_MORPH_SPEC
                }
                Modifier.animateContentSize(
                    animationSpec = sizeSpec,
                    alignment = Alignment.TopStart
                )
            } else Modifier,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) togetherWith
                    fadeOut(animationSpec = tween(140, easing = FastOutLinearInEasing))) using SizeTransform(clip = false) { _, _ ->
                    snap()
                }
            },
            contentAlignment = Alignment.TopStart,
            contentKey = {
                when {
                    it is SinglePillContentState.ExpandedTimeline -> "timeline"
                    it.isUnifiedReasoning() -> "reasoning_unified"
                    else -> "compact"
                }
            },
            label = "pill_morph_content"
        ) { targetContentState ->
            when {
                targetContentState is SinglePillContentState.ExpandedTimeline -> {
                    ActivityTimelinePanel(
                        entries = targetContentState.entries,
                        initialOpenRequest = targetContentState.initialRequest,
                        assistantId = targetContentState.assistantId,
                        scrollHandoffMode = targetContentState.scrollHandoffMode,
                        isLive = targetContentState.isLive,
                        onTimelineClick = onTimelineDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                targetContentState.isUnifiedReasoning() -> {
                    val liveContentState = if (requestedContentState.isUnifiedReasoning()) {
                        requestedContentState
                    } else {
                        targetContentState
                    }
                    UnifiedReasoningPill(
                        contentState = liveContentState,
                        parentState = state,
                        timelineOpen = timelineOpen,
                        onHeaderDismiss = onTimelineDismiss,
                        displaySetting = displaySetting,
                    )
                }
                else -> {
                    val compactTarget = targetContentState as SinglePillContentState.Compact
                    val compactState = if (!surfaceExpanded && !isExpandedReasoning) state else compactTarget.state
                    
                    AnimatedContent(
                        targetState = compactState,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(200, easing = androidx.compose.animation.core.LinearOutSlowInEasing)) +
                                scaleIn(initialScale = 0.98f, animationSpec = tween(200)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(150, easing = androidx.compose.animation.core.FastOutLinearInEasing)) +
                                        scaleOut(targetScale = 0.98f, animationSpec = tween(150))
                                ) using SizeTransform(clip = false) { _, _ ->
                                    PILL_MORPH_SPEC
                                }
                        },
                        label = "pill_content",
                        contentKey = { stateToKey(it) }
                    ) { targetState ->
                        if (targetState is ActivityState.CompletedMultiple) {
                            val items = buildActivityItemsFromMultiple(targetState)
                            var othersCanAppear by remember(key) { mutableStateOf(wasCompletedInitially) }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items.forEachIndexed { index, item ->
                                    val position = when {
                                        items.size == 1 -> PillPosition.SINGLE
                                        index == 0 -> PillPosition.FIRST
                                        index == items.lastIndex -> PillPosition.LAST
                                        else -> PillPosition.MIDDLE
                                    }
                                    
                                    if (index == 0) {
                                        LaunchedEffect(Unit) {
                                            if (!wasCompletedInitially) {
                                                delay(100L)
                                            }
                                            othersCanAppear = true
                                        }
                                        if (items.size == 1) {
                                            ExpandedActivityPill(
                                                item = item,
                                                onClick = { onClick(item.type) },
                                                position = position,
                                                connectsToBubbleBelow = connectsToBubbleBelow
                                            )
                                        } else {
                                            CompactActivityPill(
                                                item = item,
                                                onClick = { onClick(item.type) },
                                                position = position,
                                                connectsToBubbleBelow = connectsToBubbleBelow
                                            )
                                        }
                                    } else {
                                        if (wasCompletedInitially) {
                                            CompactActivityPill(
                                                item = item,
                                                onClick = { onClick(item.type) },
                                                position = position,
                                                connectsToBubbleBelow = connectsToBubbleBelow
                                            )
                                        } else {
                                            var visible by remember(key) { mutableStateOf(false) }
                                            LaunchedEffect(othersCanAppear) {
                                                if (othersCanAppear && !visible) {
                                                    delay(index * 50L)
                                                    visible = true
                                                }
                                            }
                                            
                                            AnimatedVisibility(
                                                visible = visible,
                                                enter = fadeIn(tween(150)) + slideInHorizontally(
                                                    initialOffsetX = { -it / 2 },
                                                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                                                ),
                                                exit = fadeOut(tween(100)) + slideOutHorizontally(targetOffsetX = { -it / 2 })
                                            ) {
                                                CompactActivityPill(
                                                    item = item,
                                                    onClick = { onClick(item.type) },
                                                    position = position,
                                                    connectsToBubbleBelow = connectsToBubbleBelow
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .height(PILL_HEIGHT)
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (targetState) {
                                    is ActivityState.Waiting -> {
                                        TypingIndicator(
                                            dotSize = 7.dp,
                                            color = resolvedContentColor.copy(alpha = 0.8f)
                                        )
                                    }
                                    is ActivityState.Ocr -> {
                                        OcrContent(isLive = true)
                                    }
                                    is ActivityState.Reasoning -> {
                                        val liveState = (state as? ActivityState.Reasoning) ?: targetState
                                        ReasoningContent(
                                            startTimeMs = liveState.startTimeMs,
                                            title = liveState.title,
                                            isLive = true
                                        )
                                    }
                                    is ActivityState.ToolUse -> {
                                        ToolUseContent(
                                            toolName = targetState.toolName,
                                            displayName = targetState.displayName,
                                            isLive = true
                                        )
                                    }
                                    is ActivityState.Replying -> {
                                        Text(
                                            text = stringResource(R.string.activity_pill_replying),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            modifier = Modifier.shimmer(
                                                isLoading = true,
                                                shimmerColor = Color.White.copy(alpha = 0.35f),
                                                backgroundColor = Color.White
                                            )
                                        )
                                    }
                                    is ActivityState.LoadingModel -> {
                                        Icon(
                                            imageVector = Icons.Rounded.Memory,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = resolvedContentColor.copy(alpha = 0.85f)
                                        )
                                        Text(
                                            text = targetState.modelName?.let { "Loading $it..." } ?: "Loading model...",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = resolvedContentColor,
                                            modifier = Modifier.shimmer(isLoading = true)
                                        )
                                    }
                                    is ActivityState.CompletedSingle -> {
                                        val item = ActivityItem(
                                            type = targetState.type,
                                            durationMs = targetState.durationMs,
                                            count = targetState.count,
                                            displayName = targetState.displayName
                                        )
                                        ExpandedActivityContent(item = item)
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        pillSurface()
    }
}

@Composable
private fun LiveElapsedTimerText(
    isLive: Boolean,
    startTimeMs: Long,
    completedDurationMs: Long?,
    isExpanded: Boolean,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    if (isLive) {
        var elapsedMs by remember(startTimeMs) { mutableLongStateOf((System.currentTimeMillis() - startTimeMs).coerceAtLeast(0L)) }
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(startTimeMs, lifecycleOwner) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    val nowElapsed = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(0L)
                    elapsedMs = nowElapsed
                    delay(if (nowElapsed < 10_000L) 100L else 1000L)
                }
            }
        }
        Text(
            text = formatDuration(elapsedMs),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = modifier.shimmer(true)
        )
    } else {
        val staticText = if (isExpanded) {
            completedDurationMs?.let { formatDuration(it) }.orEmpty()
        } else {
            completedDurationMs?.let { "for ${formatDuration(it)}" }.orEmpty()
        }
        if (staticText.isNotBlank()) {
            Text(
                text = staticText,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun UnifiedReasoningPill(
    contentState: SinglePillContentState,
    parentState: ActivityState,
    timelineOpen: Boolean,
    onHeaderDismiss: () -> Unit,
    displaySetting: me.lucid.domchat.data.datastore.DisplaySetting? = null,
) {
    val expandedState = contentState as? SinglePillContentState.ExpandedReasoning
    val isExpanded = expandedState != null
    val compactReasoning = (contentState as? SinglePillContentState.Compact)?.state as? ActivityState.Reasoning
    val compactCompleted = (contentState as? SinglePillContentState.Compact)?.state as? ActivityState.CompletedSingle

    val liveParent = parentState as? ActivityState.Reasoning
    val resolvedExpandedReasoning = expandedState?.state?.let { targetState ->
        if (liveParent != null &&
            liveParent.startTimeMs == targetState.startTimeMs &&
            liveParent.reasoningText.length > targetState.reasoningText.length
        ) {
            liveParent
        } else {
            targetState
        }
    }
    val resolvedCompactReasoning = compactReasoning?.let { targetState -> liveParent ?: targetState }

    val isLive = expandedState?.isLive ?: (compactReasoning != null)
    val startTimeMs = resolvedExpandedReasoning?.startTimeMs ?: resolvedCompactReasoning?.startTimeMs ?: 0L
    val completedDurationMs = expandedState?.durationMs ?: compactCompleted?.durationMs
    val defaultReasoningTitle = stringResource(R.string.activity_timeline_reasoning)
    val headerTitle = when {
        resolvedExpandedReasoning != null -> resolvedExpandedReasoning.title?.takeIf { it.isNotBlank() } ?: defaultReasoningTitle
        resolvedCompactReasoning != null -> resolvedCompactReasoning.title?.takeIf { it.isNotBlank() } ?: defaultReasoningTitle
        else -> "Reasoned"
    }

    val scrollState = rememberScrollState()
    var previewAutoFollowPaused by remember(startTimeMs) { mutableStateOf(false) }
    var programmaticScrollInProgress by remember(startTimeMs) { mutableStateOf(false) }
    var previousScrollValue by remember(startTimeMs) { mutableIntStateOf(0) }

    LaunchedEffect(scrollState, isExpanded && isLive) {
        if (!isExpanded || !isLive) return@LaunchedEffect
        snapshotFlow {
            Triple(
                scrollState.value,
                scrollState.maxValue,
                scrollState.isScrollInProgress
            )
        }.collect { (scrollValue, maxValue, isScrollInProgress) ->
            val isAtBottom = scrollValue >= maxValue
            previewAutoFollowPaused = updatePreviewAutoFollowPaused(
                currentlyPaused = previewAutoFollowPaused,
                isAtBottom = isAtBottom,
                scrollDelta = scrollValue - previousScrollValue,
                userScrollInProgress = isScrollInProgress,
                programmaticScrollInProgress = programmaticScrollInProgress
            )
            previousScrollValue = scrollValue
        }
    }

    LaunchedEffect(scrollState, previewAutoFollowPaused, isExpanded && isLive) {
        if (!isExpanded || !isLive) return@LaunchedEffect
        snapshotFlow { scrollState.maxValue }.collect { maxValue ->
            if (!previewAutoFollowPaused) {
                programmaticScrollInProgress = true
                try {
                    scrollState.scrollTo(maxValue)
                } finally {
                    programmaticScrollInProgress = false
                }
            }
        }
    }

    var streamingLayoutTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(streamingLayoutTick, previewAutoFollowPaused, isExpanded && isLive) {
        if (!isExpanded || !isLive || previewAutoFollowPaused) return@LaunchedEffect
        delay(16)
        programmaticScrollInProgress = true
        try {
            scrollState.scrollTo(scrollState.maxValue)
        } finally {
            programmaticScrollInProgress = false
        }
    }

    val lastPreviewText = remember { mutableStateOf("") }
    val currentPreviewText = resolvedExpandedReasoning?.reasoningText?.takeIf { it.isNotBlank() }
        ?: resolvedCompactReasoning?.reasoningText?.takeIf { it.isNotBlank() }
        ?: if (isExpanded) headerTitle else null
    if (currentPreviewText != null) {
        lastPreviewText.value = currentPreviewText
    }

    val contentColor = LocalContentColor.current

    Column(modifier = if (isExpanded) Modifier.fillMaxWidth() else Modifier) {
        Row(
            modifier = Modifier
                .height(PILL_HEIGHT)
                .then(
                    if (isExpanded) {
                        Modifier
                            .fillMaxWidth()
                            .then(
                                if (timelineOpen) {
                                    Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onHeaderDismiss
                                    )
                                } else Modifier
                            )
                    } else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = if (isExpanded) Modifier.weight(1f, fill = false) else Modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor.copy(alpha = 0.85f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = headerTitle,
                        transitionSpec = {
                            if (targetState == defaultReasoningTitle || targetState != "Reasoned") {
                                (slideInVertically(animationSpec = tween(220)) { it / 2 } + fadeIn(animationSpec = tween(200))) togetherWith
                                    (slideOutVertically(animationSpec = tween(180)) { -it / 2 } + fadeOut(animationSpec = tween(150)))
                            } else {
                                (slideInVertically(animationSpec = tween(220)) { -it / 2 } + fadeIn(animationSpec = tween(200))) togetherWith
                                    (slideOutVertically(animationSpec = tween(180)) { it / 2 } + fadeOut(animationSpec = tween(150)))
                            }
                        },
                        label = "reasoning_header_title"
                    ) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (isLive) Modifier.shimmer(true) else Modifier
                        )
                    }
                    AnimatedVisibility(
                        visible = !isExpanded && !isLive && completedDurationMs != null,
                        enter = fadeIn(tween(220, delayMillis = 260, easing = LinearOutSlowInEasing)),
                        exit = fadeOut(tween(120))
                    ) {
                        if (completedDurationMs != null) {
                            Text(
                                text = "for ${formatDuration(completedDurationMs)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = contentColor
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = isLive && !isExpanded,
                        enter = fadeIn(tween(140, delayMillis = 110)),
                        exit = fadeOut(tween(90))
                    ) {
                        LiveElapsedTimerText(
                            isLive = true,
                            startTimeMs = startTimeMs,
                            completedDurationMs = null,
                            isExpanded = false,
                            contentColor = contentColor
                        )
                    }
                }
            }

            if (isExpanded) {
                var expandedTimerVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(140)
                    expandedTimerVisible = true
                }
                val expandedTimerAlpha by animateFloatAsState(
                    targetValue = if (expandedTimerVisible) 1f else 0f,
                    animationSpec = tween(220, easing = LinearOutSlowInEasing),
                    label = "expanded_timer_alpha"
                )
                Box(modifier = Modifier.graphicsLayer { alpha = expandedTimerAlpha }) {
                    LiveElapsedTimerText(
                        isLive = isLive,
                        startTimeMs = startTimeMs,
                        completedDurationMs = completedDurationMs,
                        isExpanded = true,
                        contentColor = contentColor
                    )
                }
            }
        }

        val canScrollBackward by remember { derivedStateOf { scrollState.canScrollBackward } }
        val canScrollForward by remember { derivedStateOf { scrollState.canScrollForward } }
        val topFadeProgress by animateFloatAsState(
            targetValue = if (canScrollBackward) 1f else 0f,
            animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
            label = "reasoning_top_fade"
        )
        val bottomFadeProgress by animateFloatAsState(
            targetValue = if (canScrollForward) 1f else 0f,
            animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
            label = "reasoning_bottom_fade"
        )

        AnimatedVisibility(
            visible = isExpanded && lastPreviewText.value.isNotBlank(),
            enter = androidx.compose.animation.expandVertically(
                animationSpec = spring(dampingRatio = 0.74f, stiffness = 280f),
                expandFrom = Alignment.Top
            ) + fadeIn(animationSpec = tween(220, delayMillis = 50, easing = LinearOutSlowInEasing)),
            exit = androidx.compose.animation.shrinkVertically(
                animationSpec = spring(dampingRatio = 0.78f, stiffness = 340f),
                shrinkTowards = Alignment.Top
            ) + fadeOut(animationSpec = tween(140, easing = FastOutLinearInEasing))
        ) {
            MarkdownBlock(
                content = lastPreviewText.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .fadeEdges(
                        topProgress = topFadeProgress,
                        bottomProgress = bottomFadeProgress,
                        fadeHeight = if (isLive) 36f else 64f
                    )
                    .heightIn(max = if (isExpanded && isLive && !timelineOpen) 120.dp else 280.dp)
                    .verticalScroll(scrollState)
                    .padding(start = 14.dp, end = 14.dp, bottom = if (isLive) 18.dp else 14.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = contentColor
                ),
                paragraphSpacing = 8.dp,
                streamingTextReveal = isLive,
                displaySetting = displaySetting,
                onExpandedStreamingCodeBlockChanged = {
                    if (isLive) streamingLayoutTick++
                }
            )
        }
    }
}

/**
 * Content for reasoning pill (live timer).
 */
@Composable
private fun ReasoningContent(startTimeMs: Long, title: String? = null, isLive: Boolean) {
    val contentColor = LocalContentColor.current
    Icon(
        imageVector = Icons.Rounded.Lightbulb,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = contentColor.copy(alpha = 0.85f)
    )
    val displayTitle = title?.takeIf { it.isNotBlank() } ?: stringResource(R.string.activity_timeline_reasoning)
    AnimatedContent(
        targetState = displayTitle,
        transitionSpec = {
            fadeIn(tween(200, easing = LinearOutSlowInEasing))
                .togetherWith(fadeOut(tween(160, easing = FastOutLinearInEasing)))
        },
        label = "reasoning_title"
    ) { text ->
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = if (isLive) Modifier.shimmer(true) else Modifier
        )
    }
    LiveElapsedTimerText(
        isLive = isLive,
        startTimeMs = startTimeMs,
        completedDurationMs = null,
        isExpanded = true,
        contentColor = contentColor
    )
}

@Composable
private fun OcrContent(isLive: Boolean) {
    val contentColor = LocalContentColor.current
    Icon(
        imageVector = Icons.Rounded.Image,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = contentColor.copy(alpha = 0.85f)
    )
    Text(
        text = stringResource(R.string.activity_pill_ocr_live),
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
        modifier = if (isLive) Modifier.shimmer(true) else Modifier
    )
}

/**
 * Content for tool use pill.
 */
@Composable
private fun ToolUseContent(toolName: String, displayName: String, isLive: Boolean) {
    val contentColor = LocalContentColor.current
    val type = categorizeToolName(toolName)

    Icon(
        imageVector = type.getIcon(),
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = contentColor.copy(alpha = 0.85f)
    )
    Text(
        text = displayName,
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
        modifier = if (isLive) Modifier.shimmer(true) else Modifier
    )
}

/**
 * Content for expanded single activity (after completion).
 */
@Composable
private fun ExpandedActivityContent(item: ActivityItem) {
    val contentColor = LocalContentColor.current
    Icon(
        imageVector = item.type.getIcon(),
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = contentColor.copy(alpha = 0.85f)
    )

    if (item.type == ActivityType.REASONING) {
        Text(
            text = "Reasoned",
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
        if (item.durationMs != null) {
            Text(
                text = "for ${formatDuration(item.durationMs)}",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    } else {
        val text = when (item.type) {
            ActivityType.REASONING -> "Reasoned"
            ActivityType.OCR -> {
                if (item.count > 1) {
                    stringResource(R.string.activity_pill_ocr_done_count, item.count)
                } else {
                    stringResource(R.string.activity_pill_ocr_done)
                }
            }
            ActivityType.SEARCH -> "Searched the Web"
            ActivityType.MEMORY_RECALL -> stringResource(R.string.activity_pill_memory_recalled)
            ActivityType.PYTHON -> "Ran Python"
            ActivityType.WORKSPACE -> "Used workspace"
            ActivityType.SKILL -> "Managed skills"
            ActivityType.MCP -> "MCP"
            ActivityType.TOOL_OTHER -> "Used tool"
            ActivityType.LOADING_MODEL -> "Loading model"
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

/**
 * Get corner radii based on pill position and whether it connects to bubble below.
 */
private fun getCornerRadii(
    position: PillPosition,
    connectsToBubbleBelow: Boolean
): RoundedCornerShape {
    val bottomLeft = if (connectsToBubbleBelow) SMALL_RADIUS else LARGE_RADIUS
    val bottomRight = if (connectsToBubbleBelow) SMALL_RADIUS else LARGE_RADIUS
    
    return when (position) {
        PillPosition.SINGLE -> RoundedCornerShape(
            topStart = LARGE_RADIUS,
            topEnd = LARGE_RADIUS,
            bottomStart = bottomLeft,
            bottomEnd = bottomRight
        )
        PillPosition.FIRST -> RoundedCornerShape(
            topStart = LARGE_RADIUS,
            topEnd = SMALL_RADIUS,
            bottomStart = bottomLeft,  // Flat to connect to bubble
            bottomEnd = SMALL_RADIUS
        )
        PillPosition.MIDDLE -> RoundedCornerShape(
            topStart = SMALL_RADIUS,
            topEnd = SMALL_RADIUS,
            bottomStart = SMALL_RADIUS,
            bottomEnd = SMALL_RADIUS
        )
        PillPosition.LAST -> RoundedCornerShape(
            topStart = SMALL_RADIUS,
            topEnd = LARGE_RADIUS,
            bottomStart = SMALL_RADIUS,
            bottomEnd = bottomRight  // Flat to connect to bubble
        )
    }
}

/**
 * Base single pill component.
 */
@Composable
private fun SinglePill(
    onClick: () -> Unit,
    position: PillPosition,
    connectsToBubbleBelow: Boolean,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    isLoading: Boolean = false,
    content: @Composable () -> Unit
) {
    val display = LocalDisplaySetting.current
    val isTransparentStyle = display.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.TRANSPARENT_BOTTOM_BLUR
    val isGlassy = display.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.BUBBLES
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

    val pillContainerColor = if (isTransparentStyle) {
        Color.Transparent
    } else if (isGlassy) {
        if (effectiveAlpha <= 0.02f) Color.Transparent else baseTone.copy(alpha = effectiveAlpha)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val pillSpecularBorder = if (isTransparentStyle || (!isGlassy && bubbleBlur <= 0f)) null else androidx.compose.foundation.BorderStroke(
        if (effectiveAlpha <= 0.05f) 0.75.dp else 0.5.dp,
        androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(
                androidx.compose.ui.graphics.Color.White.copy(alpha = if (effectiveAlpha <= 0.05f) 0.40f else if (isGlassy) 0.35f else 0.25f),
                androidx.compose.ui.graphics.Color.White.copy(alpha = if (effectiveAlpha <= 0.05f) 0.18f else if (isGlassy) 0.12f else 0.06f),
                androidx.compose.ui.graphics.Color.White.copy(alpha = if (effectiveAlpha <= 0.05f) 0.08f else 0f)
            )
        )
    )

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val resolvedContentColor = if (isGlassy || isTransparentStyle) {
        when {
            isTransparentStyle -> MaterialTheme.colorScheme.onSurface
            effectiveAlpha <= 0.05f -> if (isDark) Color(0xFFF2F2F7) else Color(0xFF14151C)
            tone >= 0.55f -> Color(0xFF14151C)
            else -> Color(0xFFF2F2F7)
        }
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val pillShape = getCornerRadii(position, connectsToBubbleBelow)
    val chatAnimationsEnabled = me.lucid.domchat.ui.context.LocalChatAnimationsEnabled.current

    val pillSurface = @Composable {
        Surface(
            modifier = modifier
                .height(PILL_HEIGHT)
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
                .then(
                    if (chatAnimationsEnabled) {
                        Modifier.animateContentSize(animationSpec = PILL_MORPH_SPEC)
                    } else Modifier
                )
                .then(
                    if (!isTransparentStyle && (isGlassy || bubbleBlur > 0f)) {
                        Modifier.domChatBlurEffect(
                            containerColor = pillContainerColor,
                            shape = pillShape,
                            blurRadius = bubbleBlur.dp
                        )
                    } else Modifier
                )
                .clip(pillShape),
            shape = pillShape,
            color = pillContainerColor,
            border = pillSpecularBorder,
            contentColor = resolvedContentColor,
            onClick = onClick
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        pillSurface()
    }
}

/**
 * Reasoning pill - expanded with timer.
 */
@Composable
private fun ReasoningPill(
    startTimeMs: Long,
    onClick: () -> Unit,
    position: PillPosition,
    connectsToBubbleBelow: Boolean,
    isLive: Boolean = false
) {
    SinglePill(
        onClick = onClick,
        position = position,
        connectsToBubbleBelow = connectsToBubbleBelow,
        testTag = ActivityType.REASONING.toTestTag(),
        isLoading = isLive
    ) {
        val contentColor = LocalContentColor.current
        Icon(
            imageVector = Icons.Rounded.Lightbulb,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = contentColor.copy(alpha = 0.85f)
        )
        Text(
            text = stringResource(R.string.activity_timeline_reasoning),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = if (isLive) Modifier.shimmer(true) else Modifier
        )
        LiveElapsedTimerText(
            isLive = isLive,
            startTimeMs = startTimeMs,
            completedDurationMs = null,
            isExpanded = true,
            contentColor = contentColor
        )
    }
}

/**
 * Tool use pill - expanded with tool name.
 */
@Composable
private fun ToolUsePill(
    toolName: String,
    displayName: String,
    onClick: () -> Unit,
    position: PillPosition,
    connectsToBubbleBelow: Boolean,
    isLive: Boolean = false
) {
    val type = categorizeToolName(toolName)
    
    SinglePill(
        onClick = onClick,
        position = position,
        connectsToBubbleBelow = connectsToBubbleBelow,
        testTag = type.toTestTag(),
        isLoading = isLive
    ) {
        val contentColor = LocalContentColor.current
        Icon(
            imageVector = type.getIcon(),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = contentColor.copy(alpha = 0.85f)
        )
        Text(
            text = displayName,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = if (isLive) Modifier.shimmer(true) else Modifier
        )
    }
}

/**
 * Expanded activity pill for completed single activity.
 * Shows text like "Reasoned for 3.2s" or "Searched the Web".
 */
@Composable
private fun ExpandedActivityPill(
    item: ActivityItem,
    onClick: () -> Unit,
    position: PillPosition,
    connectsToBubbleBelow: Boolean
) {
    SinglePill(
        onClick = onClick,
        position = position,
        connectsToBubbleBelow = connectsToBubbleBelow,
        testTag = item.type.toTestTag()
    ) {
        val contentColor = LocalContentColor.current
        Icon(
            imageVector = item.type.getIcon(),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = contentColor.copy(alpha = 0.85f)
        )
        
        val text = when (item.type) {
            ActivityType.REASONING -> {
                if (item.durationMs != null) {
                    "Reasoned for ${formatDuration(item.durationMs)}"
                } else {
                    "Reasoned"
                }
            }
            ActivityType.OCR -> {
                if (item.count > 1) {
                    stringResource(R.string.activity_pill_ocr_done_count, item.count)
                } else {
                    stringResource(R.string.activity_pill_ocr_done)
                }
            }
            ActivityType.SEARCH -> {
                if (item.count > 1) "Searched Ã—${item.count}" else "Searched the Web"
            }
            ActivityType.MEMORY_RECALL -> {
                if (item.count > 1) stringResource(R.string.activity_pill_memory_recalled_count, item.count)
                else stringResource(R.string.activity_pill_memory_recalled)
            }
            ActivityType.PYTHON -> {
                if (item.count > 1) "Ran Python Ã—${item.count}" else "Ran Python"
            }
            ActivityType.WORKSPACE -> {
                if (item.count > 1) "Used workspace x${item.count}" else "Used workspace"
            }
            ActivityType.SKILL -> {
                if (item.count > 1) "Managed skills x${item.count}" else "Managed skills"
            }
            ActivityType.MCP -> {
                if (item.count > 1) "MCP calls Ã—${item.count}" else "MCP"
            }
            ActivityType.LOADING_MODEL -> {
                "Loading model..."
            }
            ActivityType.TOOL_OTHER -> {
                if (item.count > 1) "Used tools Ã—${item.count}" else "Used tool"
            }
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

/**
 * Compact activity pill for multiple activities.
 * Shows just icon + optional count.
 */
@Composable
private fun CompactActivityPill(
    item: ActivityItem,
    onClick: () -> Unit,
    position: PillPosition,
    connectsToBubbleBelow: Boolean,
    modifier: Modifier = Modifier
) {
    SinglePill(
        onClick = onClick,
        position = position,
        connectsToBubbleBelow = connectsToBubbleBelow,
        modifier = modifier,
        testTag = item.type.toTestTag()
    ) {
        val contentColor = LocalContentColor.current
        Icon(
            imageVector = item.type.getIcon(),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = contentColor.copy(alpha = 0.85f)
        )
        
        // Show duration for reasoning only (no counts for tools)
        val text = when (item.type) {
            ActivityType.REASONING -> {
                item.durationMs?.let { formatDuration(it) }
            }
            ActivityType.OCR -> null
            ActivityType.MEMORY_RECALL -> null
            else -> null
        }
        
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

/**
 * Format milliseconds to a readable duration string like "2.3s"
 */
private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000.0
    return if (seconds < 10) {
        String.format("%.1fs", seconds)
    } else {
        String.format("%.0fs", seconds)
    }
}

// Keep old ActivityPill for compatibility, but redirect to new implementation
@Composable
fun ActivityPill(
    state: ActivityState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadii: GroupedCornerRadii = GroupedCornerRadii.Default,
) {
    ActivityPillRow(
        state = state,
        onClick = { onClick() },
        modifier = modifier,
        connectsToBubbleBelow = true
    )
}

/**
 * Corner radii for grouped bubbles/pills.
 * Allows different radii on each corner for the "grouped message" look.
 */
data class GroupedCornerRadii(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomStart: Dp,
    val bottomEnd: Dp,
) {
    companion object {
        val Default = GroupedCornerRadii(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        )
        
        /** For first item in a group (small bottom-left corner) */
        fun first(largeRadius: Dp = 20.dp, smallRadius: Dp = 6.dp) = GroupedCornerRadii(
            topStart = largeRadius,
            topEnd = largeRadius,
            bottomStart = smallRadius,
            bottomEnd = largeRadius
        )
        
        /** For middle item in a group (small top-left and bottom-left corners) */
        fun middle(largeRadius: Dp = 20.dp, smallRadius: Dp = 6.dp) = GroupedCornerRadii(
            topStart = smallRadius,
            topEnd = largeRadius,
            bottomStart = smallRadius,
            bottomEnd = largeRadius
        )
        
        /** For last item in a group (small top-left corner) */
        fun last(largeRadius: Dp = 20.dp, smallRadius: Dp = 6.dp) = GroupedCornerRadii(
            topStart = smallRadius,
            topEnd = largeRadius,
            bottomStart = largeRadius,
            bottomEnd = largeRadius
        )
        
        /** For single item (not grouped) */
        fun single(radius: Dp = 20.dp) = GroupedCornerRadii(
            topStart = radius,
            topEnd = radius,
            bottomStart = radius,
            bottomEnd = radius
        )
    }
}
