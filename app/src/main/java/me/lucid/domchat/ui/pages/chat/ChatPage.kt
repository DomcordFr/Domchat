package me.lucid.domchat.ui.pages.chat

import android.content.Context
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.lucid.domchat.DomChatApp
import me.lucid.domchat.data.ai.ContextBudgetCalculator
import me.lucid.domchat.ui.context.LocalChatAnimationsEnabled
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.theme.LocalDarkMode
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.activity.compose.BackHandler
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.draw.blur
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.ui.components.message.ActiveChatMessageAction
import me.lucid.domchat.ui.components.message.ChatMessageActionsSheet
import me.lucid.domchat.ui.components.message.ChatMessageCopySheet
import me.lucid.domchat.ui.components.message.LocalActiveChatMessageAction
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import me.lucid.domchat.ui.components.chat.ChatInfoBottomSheet
import me.lucid.domchat.ui.components.chat.CharacterNotesBottomSheet
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import me.lucid.domchat.ui.components.dialog.CharacterExportDialog
import me.lucid.domchat.ui.components.dialog.ChatDestructiveConfirmDialog
import me.lucid.domchat.ui.components.dialog.SettingsDestructiveConfirmDialog
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material.icons.rounded.Image

import me.lucid.domchat.data.datastore.getEffectiveDisplaySetting
import me.lucid.domchat.ui.components.chat.NewChatContent
import me.lucid.domchat.ui.components.nav.DomChatMenuButton

import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.components.ui.UpdateDialog
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.components.ui.Tooltip
import me.lucid.domchat.ui.component.backdrop.LocalAppBackdrop
import me.lucid.domchat.ui.component.backdrop.effects.blur
import me.lucid.domchat.ui.component.backdrop.isRuntimeShaderSupported
import me.lucid.domchat.ui.component.backdrop.backdrops.LayerBackdrop
import me.lucid.domchat.ui.component.backdrop.backdrops.layerBackdrop
import me.lucid.domchat.ui.component.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.lucid.ai.core.MessageRole
import me.lucid.ai.context.ContextCountConfidence
import me.lucid.ai.context.ContextTokenEstimator
import me.lucid.ai.context.ContextUsageBreakdown
import me.lucid.ai.context.calculateMinSafeFloorTokens
import me.lucid.ai.context.effectiveHistoryForContext
import me.lucid.ai.context.probableTemporaryTokenReserve
import me.lucid.ai.context.smartFitContext
import me.lucid.ai.context.smartInputBudget
import me.lucid.ai.provider.Model
import me.lucid.ai.provider.ModelAbility
import me.lucid.ai.provider.baseCapacityTokens
import me.lucid.ai.provider.contextCapacityTokens
import me.lucid.ai.ui.MessageNode
import me.lucid.ai.ui.UIMessage
import me.lucid.ai.ui.UIMessagePart
import kotlin.math.roundToInt
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.layout.onGloballyPositioned
import me.lucid.domchat.BuildConfig
import me.lucid.domchat.R
import me.lucid.domchat.Screen
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.TtsAutoplayMode
import me.lucid.domchat.data.datastore.findProvider
import me.lucid.domchat.data.datastore.getCurrentAssistant
import me.lucid.domchat.data.datastore.getEffectiveTTSProvider
import me.lucid.domchat.data.datastore.getEffectiveTtsAutoplayMode
import me.lucid.domchat.data.ai.contextUsageSourceKey
import me.lucid.domchat.data.ai.buildTimeAwarenessBlock
import me.lucid.domchat.data.ai.resolveActiveSkillIds
import me.lucid.domchat.data.ai.tools.toDefinitionPreviews
import me.lucid.domchat.data.ai.selectSmartMemoryContext
import me.lucid.domchat.data.ai.prompts.DEFAULT_LEARNING_MODE_PROMPT
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.AssistantMemory
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.data.model.LorebookActivationType
import me.lucid.domchat.data.model.ModeAttachmentType
import me.lucid.domchat.navigation.CHAT_ROUTE_TARGET_KEY
import me.lucid.domchat.navigation.ChatRouteTarget
import me.lucid.domchat.data.repository.ChatAttachmentManager
import me.lucid.domchat.ui.components.ai.MinimalChatInput
import me.lucid.domchat.ui.components.ui.TTSController
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.context.LocalTTSState
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.ChatInputState
import me.lucid.domchat.ui.hooks.EditStateContent
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberChatInputState
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.useEditState
import me.lucid.domchat.ui.pages.assistant.AssistantVM
import me.lucid.domchat.service.ChatPersistenceMode
import me.lucid.domchat.service.ContextManagementActivity
import me.lucid.domchat.ui.theme.AssistantChatTheme
import me.lucid.domchat.utils.base64Decode
import me.lucid.domchat.utils.getFileNameFromUri
import me.lucid.domchat.utils.getFileMimeType
import me.lucid.domchat.utils.navigateToChatPage
import me.lucid.domchat.utils.toLocalInferenceUserMessage
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.ui.draw.clipToBounds
import me.lucid.domchat.ui.modifier.DomChatBlur
import me.lucid.domchat.ui.modifier.LocalDomChatBlur
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.modifier.domChatFloatingSurfaceColor
import me.lucid.domchat.ui.modifier.domChatSoftEdgeBorder
import me.lucid.domchat.ui.modifier.blurredContainerColor
import me.lucid.domchat.ui.modifier.domChatIconPillContainerColor
import me.lucid.domchat.ui.modifier.domChatBlurSource
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.AppSize
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.motion.LocalMotionPolicy
import me.lucid.domchat.ui.motion.EntranceDirection
import me.lucid.domchat.ui.motion.entrance
import androidx.compose.ui.draw.clip

internal fun hasConversationMessages(conversation: Conversation): Boolean {
    return conversation.messageNodes.isNotEmpty()
}

internal fun hasConversationPresetMessages(conversation: Conversation, assistant: Assistant): Boolean {
    if (assistant.presetMessages.isEmpty() && assistant.alternateGreetings.isEmpty()) return false
    val presetIds = assistant.presetMessages.map { it.id }.toSet()
    if (conversation.currentMessages.any { it.id in presetIds }) return true
    if (assistant.alternateGreetings.isNotEmpty()) {
        val alternatesSet = assistant.alternateGreetings.toSet()
        if (conversation.currentMessages.any { msg -> msg.role == me.lucid.ai.core.MessageRole.ASSISTANT && msg.toContentText() in alternatesSet }) {
            return true
        }
    }
    return false
}

internal fun shouldOfferMessageRegenerate(
    role: me.lucid.ai.core.MessageRole,
    isLastTurn: Boolean,
    previousGroup: me.lucid.domchat.ui.components.chat.MessageTurnGroup?,
): Boolean {
    if (role == me.lucid.ai.core.MessageRole.ASSISTANT && previousGroup == null) return false
    return when (role) {
        me.lucid.ai.core.MessageRole.USER -> true
        else -> isLastTurn
    }
}

internal fun isCharacterIntroMessage(
    messages: List<me.lucid.ai.ui.UIMessage>,
    message: me.lucid.ai.ui.UIMessage,
): Boolean {
    if (message.role != me.lucid.ai.core.MessageRole.ASSISTANT) return false
    val index = messages.indexOfFirst { it.id == message.id }
    if (index < 0) return false
    return messages.take(index).none { it.role == me.lucid.ai.core.MessageRole.USER }
}

internal fun completedGenerationIdsAfterDone(
    currentCompleted: Set<Uuid>,
    finishedConversationId: Uuid,
    viewingConversationId: Uuid,
): Set<Uuid> {
    return if (finishedConversationId == viewingConversationId) {
        currentCompleted - viewingConversationId
    } else {
        currentCompleted + finishedConversationId
    }
}

private const val PROGRESSIVE_EDGE_BLUR_AGSL = """
    uniform shader content;
    uniform float2 direction;
    uniform float maxRadius;
    uniform float topHeight;
    uniform float bottomHeight;
    uniform float height;
    uniform float width;

    half4 main(float2 coord) {
        float t = 0.0;
        if (topHeight > 0.5 && coord.y < topHeight) {
            float u = clamp(1.0 - coord.y / topHeight, 0.0, 1.0);
            t = max(t, u * u * (3.0 - 2.0 * u));
        }
        if (bottomHeight > 0.5 && coord.y > height - bottomHeight) {
            float u = clamp((coord.y - (height - bottomHeight)) / bottomHeight, 0.0, 1.0);
            t = max(t, u * u * (3.0 - 2.0 * u));
        }
        float radius = maxRadius * t;
        if (radius < 0.35) {
            return content.eval(coord);
        }

        float stepPx = radius / 8.0;
        float2 minC = float2(0.5, 0.5);
        float2 maxC = float2(max(width - 0.5, 0.5), max(height - 0.5, 0.5));

        half4 acc = content.eval(coord) * 0.12564;
        acc += (content.eval(clamp(coord + direction * (stepPx * 1.0), minC, maxC)) +
                content.eval(clamp(coord - direction * (stepPx * 1.0), minC, maxC))) * 0.11964;
        acc += (content.eval(clamp(coord + direction * (stepPx * 2.0), minC, maxC)) +
                content.eval(clamp(coord - direction * (stepPx * 2.0), minC, maxC))) * 0.10334;
        acc += (content.eval(clamp(coord + direction * (stepPx * 3.0), minC, maxC)) +
                content.eval(clamp(coord - direction * (stepPx * 3.0), minC, maxC))) * 0.08096;
        acc += (content.eval(clamp(coord + direction * (stepPx * 4.0), minC, maxC)) +
                content.eval(clamp(coord - direction * (stepPx * 4.0), minC, maxC))) * 0.05752;
        acc += (content.eval(clamp(coord + direction * (stepPx * 5.0), minC, maxC)) +
                content.eval(clamp(coord - direction * (stepPx * 5.0), minC, maxC))) * 0.03706;
        acc += (content.eval(clamp(coord + direction * (stepPx * 6.0), minC, maxC)) +
                content.eval(clamp(coord - direction * (stepPx * 6.0), minC, maxC))) * 0.02166;
        acc += (content.eval(clamp(coord + direction * (stepPx * 7.0), minC, maxC)) +
                content.eval(clamp(coord - direction * (stepPx * 7.0), minC, maxC))) * 0.01148;
        acc += (content.eval(clamp(coord + direction * (stepPx * 8.0), minC, maxC)) +
                content.eval(clamp(coord - direction * (stepPx * 8.0), minC, maxC))) * 0.00552;
        return acc;
    }
"""

/**
 * Direct 2-pass separable AGSL progressive edge blur (Android 13+ / API 33+).
 * Scales the actual Gaussian blur radius smoothly from maxRadius at the top/bottom
 * screen edges down to 0px into the chat area, with an instant 1-fetch pass-through
 * in the middle of the screen. Zero alpha fade-out, zero Offscreen/DstIn artifacts,
 * and zero backdrop self-sampling.
 */
@Composable
private fun Modifier.progressiveEdgeBlur(
    blurDp: Float,
    topHeightDp: Dp,
    bottomHeightDp: Dp,
): Modifier {
    if (blurDp <= 0.1f || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
        return this
    }
    if (topHeightDp <= 0.dp && bottomHeightDp <= 0.dp) return this

    val density = LocalDensity.current
    val maxRadiusPx = with(density) { blurDp.coerceIn(0f, 60f).dp.toPx() * 1.4f }
    val topPx = with(density) { topHeightDp.coerceAtLeast(0.dp).toPx() }
    val bottomPx = with(density) { bottomHeightDp.coerceAtLeast(0.dp).toPx() }
    var containerWidthPx by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var containerHeightPx by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    val effect = remember(maxRadiusPx, topPx, bottomPx, containerWidthPx, containerHeightPx) {
        val w = containerWidthPx.toFloat()
        val h = containerHeightPx.toFloat()
        if (w <= 1f || h <= 1f || maxRadiusPx <= 0.5f) {
            null
        } else {
            val horizShader = android.graphics.RuntimeShader(PROGRESSIVE_EDGE_BLUR_AGSL).apply {
                setFloatUniform("direction", 1f, 0f)
                setFloatUniform("maxRadius", maxRadiusPx)
                setFloatUniform("topHeight", topPx)
                setFloatUniform("bottomHeight", bottomPx)
                setFloatUniform("height", h)
                setFloatUniform("width", w)
            }
            val vertShader = android.graphics.RuntimeShader(PROGRESSIVE_EDGE_BLUR_AGSL).apply {
                setFloatUniform("direction", 0f, 1f)
                setFloatUniform("maxRadius", maxRadiusPx)
                setFloatUniform("topHeight", topPx)
                setFloatUniform("bottomHeight", bottomPx)
                setFloatUniform("height", h)
                setFloatUniform("width", w)
            }
            val horizEffect = android.graphics.RenderEffect.createRuntimeShaderEffect(horizShader, "content")
            val vertEffect = android.graphics.RenderEffect.createRuntimeShaderEffect(vertShader, "content")
            android.graphics.RenderEffect.createChainEffect(vertEffect, horizEffect).asComposeRenderEffect()
        }
    }

    return this
        .onSizeChanged { size ->
            if (size.width != containerWidthPx) {
                containerWidthPx = size.width
            }
            if (size.height != containerHeightPx) {
                containerHeightPx = size.height
            }
        }
        .graphicsLayer {
            renderEffect = effect
        }
}

@Composable
private fun ChatTopFadeOverlay(
    fadeHeight: Dp,
    settings: Settings,
    modifier: Modifier = Modifier,
) {
    val density = settings.displaySetting.barGradientDensity.coerceIn(0f, 1f)
    if (density <= 0.001f) return

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(statusBarHeight + fadeHeight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeight)
                    .background(Color.Black.copy(alpha = 0.98f * density))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fadeHeight)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.98f * density),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun ChatWidePanelEdgeFadeOverlay(
    width: Dp,
    placement: ChatToolbarPlacement,
    showBottomFade: Boolean,
    settings: Settings,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val density = settings.displaySetting.barGradientDensity.coerceIn(0f, 1f)
    val topFadeHeight = if (placement == ChatToolbarPlacement.Top) {
        chatTopToolbarFadeHeight
    } else {
        chatBottomToolbarTopFadeHeight
    }
    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeight)
                    .background(backgroundColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topFadeHeight)
            ) {
                if (density > 0.001f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.98f * density),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = showBottomFade,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (density > 0.001f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = if (placement == ChatToolbarPlacement.Bottom) {
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.72f * density),
                                                Color.Black.copy(alpha = 0.97f * density)
                                            )
                                        )
                                    } else {
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.92f * density)
                                            )
                                        )
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

internal data class AssistantSwitchNavigation(
    val initText: String?,
    val initFiles: List<String>,
    val persistenceMode: String?,
)

internal fun buildAssistantSwitchNavigation(
    persistenceMode: ChatPersistenceMode,
): AssistantSwitchNavigation {
    return AssistantSwitchNavigation(
        initText = null,
        initFiles = emptyList(),
        persistenceMode = persistenceMode.takeIf { it != ChatPersistenceMode.NORMAL }?.routeValue,
    )
}

internal fun decodeChatRouteText(text: String?): String {
    return text?.let { encoded ->
        runCatching { encoded.base64Decode() }.getOrDefault("")
    }.orEmpty()
}

@Serializable
internal data class ChatInputDraft(
    val text: String = "",
    val messageContent: List<UIMessagePart> = emptyList(),
    val editingMessage: Uuid? = null,
) {
    val isEmpty: Boolean
        get() = text.isEmpty() && messageContent.isEmpty() && editingMessage == null

    fun forAssistantSwitch(): ChatInputDraft {
        return copy(editingMessage = null)
    }
}

internal object ChatSwitchSession {
    var coldStartConsumed: Boolean = false
    var coldStartTargetId: String? = null
}

internal object ChatSessionDraftStore {
    private const val MAX_PERSISTED_DRAFTS = 20
    private val drafts = java.util.concurrent.ConcurrentHashMap<Uuid, ChatInputDraft>()
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs by lazy {
        runCatching {
            DomChatApp.instance.getSharedPreferences("chat_session_drafts", Context.MODE_PRIVATE)
        }.getOrNull()
    }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val ioDispatcher = kotlinx.coroutines.Dispatchers.IO.limitedParallelism(1)
    private val ioScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + ioDispatcher)

    fun get(conversationId: Uuid): ChatInputDraft? {
        drafts[conversationId]?.let { return it }
        val raw = prefs?.getString(conversationId.toString(), null) ?: return null
        return runCatching {
            json.decodeFromString<ChatInputDraft>(raw)
        }.getOrNull()?.let { restored ->
            drafts.putIfAbsent(conversationId, restored) ?: restored
        }
    }

    fun putInMemory(conversationId: Uuid, draft: ChatInputDraft) {
        if (draft.isEmpty) {
            drafts.remove(conversationId)
        } else {
            drafts[conversationId] = draft
        }
    }

    private fun persistDraftSync(conversationId: Uuid, draft: ChatInputDraft) {
        val p = prefs ?: return
        if (draft.isEmpty) {
            p.edit()
                .remove(conversationId.toString())
                .remove("ts_$conversationId")
                .apply()
        } else {
            runCatching {
                val encoded = json.encodeToString(draft)
                val all = p.all
                val editor = p.edit()
                val now = System.currentTimeMillis()
                editor.putString(conversationId.toString(), encoded)
                editor.putLong("ts_$conversationId", now)

                val draftKeys = all.keys.filter { !it.startsWith("ts_") && it != conversationId.toString() }
                if (draftKeys.size >= MAX_PERSISTED_DRAFTS) {
                    val excess = draftKeys.size - MAX_PERSISTED_DRAFTS + 1
                    val oldestKeys = draftKeys.sortedBy { key ->
                        (all["ts_$key"] as? Long) ?: 0L
                    }.take(excess)
                    oldestKeys.forEach { key ->
                        editor.remove(key)
                        editor.remove("ts_$key")
                    }
                }
                editor.apply()
            }
        }
    }

    suspend fun persistDraft(conversationId: Uuid, draft: ChatInputDraft) {
        kotlinx.coroutines.withContext(ioDispatcher) {
            persistDraftSync(conversationId, draft)
        }
    }

    fun put(conversationId: Uuid, draft: ChatInputDraft, isTemporary: Boolean = false) {
        putInMemory(conversationId, draft)
        if (!isTemporary) {
            ioScope.launch {
                persistDraftSync(conversationId, draft)
            }
        }
    }

    fun moveDraft(fromConversationId: Uuid, toConversationId: Uuid, draft: ChatInputDraft, isTemporary: Boolean = false) {
        drafts.remove(fromConversationId)
        val switchedDraft = draft.forAssistantSwitch()
        putInMemory(toConversationId, switchedDraft)
        if (!isTemporary) {
            ioScope.launch {
                prefs?.edit()
                    ?.remove(fromConversationId.toString())
                    ?.remove("ts_$fromConversationId")
                    ?.apply()
                persistDraftSync(toConversationId, switchedDraft)
            }
        }
    }

    fun clear() {
        drafts.clear()
        ioScope.launch {
            prefs?.edit()?.clear()?.apply()
        }
    }
}

internal fun ChatInputState.toDraft(): ChatInputDraft {
    return ChatInputDraft(
        text = textContent.text.toString(),
        messageContent = messageContent,
        editingMessage = editingMessage,
    )
}

internal fun ChatInputState.applyDraft(draft: ChatInputDraft) {
    clearInput()
    if (draft.text.isNotEmpty()) {
        setMessageText(draft.text)
    }
    if (draft.messageContent.isNotEmpty()) {
        messageContent = draft.messageContent
    }
    editingMessage = draft.editingMessage
}

internal fun shouldShowNewChatContent(
    isTemporaryChat: Boolean,
    hasConversationMessages: Boolean,
    hasAnyPresetMessages: Boolean,
    showNewChatContent: Boolean,
    hasTextInput: Boolean,
    isKeyboardOpen: Boolean,
): Boolean {
    return !isTemporaryChat &&
        !hasConversationMessages &&
        !hasAnyPresetMessages &&
        showNewChatContent &&
        !hasTextInput &&
        !isKeyboardOpen
}

internal fun chatTopBarPlacement(settings: Settings): ChatToolbarPlacement {
    return if (settings.displaySetting.chatToolbarAtBottom) {
        ChatToolbarPlacement.Bottom
    } else {
        ChatToolbarPlacement.Top
    }
}

internal fun chatListTopPadding(
    placement: ChatToolbarPlacement,
    statusBarPadding: Dp = 0.dp,
): Dp {
    val fadeHeight = if (placement == ChatToolbarPlacement.Top) {
        chatTopToolbarFadeHeight
    } else {
        chatBottomToolbarTopFadeHeight
    }
    return statusBarPadding + fadeHeight
}

internal fun chatListBottomPadding(placement: ChatToolbarPlacement): androidx.compose.ui.unit.Dp {
    return if (placement == ChatToolbarPlacement.Bottom) 204.dp else 140.dp
}

internal fun chatToolbarPopupTopPadding(placement: ChatToolbarPlacement): androidx.compose.ui.unit.Dp {
    return if (placement == ChatToolbarPlacement.Top) 53.dp else 0.dp
}

internal fun chatToolbarPopupBottomPadding(placement: ChatToolbarPlacement): androidx.compose.ui.unit.Dp {
    return if (placement == ChatToolbarPlacement.Bottom) 72.dp else 0.dp
}

private fun chatToolbarPopupTransformOrigin(placement: ChatToolbarPlacement): TransformOrigin {
    return if (placement == ChatToolbarPlacement.Bottom) {
        TransformOrigin(0.5f, 1f)
    } else {
        TransformOrigin(0.5f, 0f)
    }
}

private val chatTopToolbarFadeHeight = 86.dp
private val chatBottomToolbarTopFadeHeight = 36.dp

// Startup: pause after the splash clears so the user can view the wallpaper,
// then the chrome reveals in staggered groups. Each group keeps its own
// EntrancePlayed key so back navigation never re-triggers the enter animation.
private val startupWallpaperPauseMs = 350
private val startupStaggerStepMs = 120

private fun latestAssistantSpeechMessage(conversation: Conversation): UIMessage? {
    return conversation.messageNodes.asReversed().asSequence()
        .map { it.currentMessage }
        .firstOrNull { message ->
            message.role == MessageRole.ASSISTANT && message.toContentText().isNotBlank()
        }
}

private fun speakablePrefixLength(text: String, final: Boolean): Int {
    val trimmedEnd = text.indexOfLast { !it.isWhitespace() }
    if (trimmedEnd < 0) return 0

    val paragraphBreak = text.indexOf("\n\n")
    if (paragraphBreak >= 0) return paragraphBreak + 2

    for (i in text.indices) {
        val c = text[i]
        if (c == '。' || c == '！' || c == '？' || c == '…') {
            return i + 1
        }
        if (c == '!' || c == '?') {
            return i + 1
        }
        if (c == '.') {
            val prevIsDigit = i > 0 && text[i - 1].isDigit()
            val nextIsDigit = i + 1 < text.length && text[i + 1].isDigit()
            if (!prevIsDigit || !nextIsDigit) {
                val nextIsBoundary = i + 1 == text.length || text[i + 1].isWhitespace() || text[i + 1] == '\n' || text[i + 1] == '"' || text[i + 1] == '\''
                if (nextIsBoundary) {
                    return i + 1
                }
            }
        }
    }

    val lineBreak = text.indexOf('\n')
    if (lineBreak >= 0) return lineBreak + 1

    return if (final) trimmedEnd + 1 else 0
}

@Composable
private fun ChatTtsAutoplayEffect(
    settings: Settings,
    assistant: Assistant,
    conversation: Conversation,
    loadingJob: Job?,
    isGenerationCancelled: () -> Boolean = { false },
) {
    val tts = LocalTTSState.current
    val mode = settings.getEffectiveTtsAutoplayMode(assistant)
    val provider = remember(
        settings.ttsProviders,
        settings.selectedTTSVoiceId,
        settings.selectedTTSProviderId,
        assistant.ttsVoiceId,
    ) {
        settings.getEffectiveTTSProvider(assistant)
    }
    var completedMessageId by remember(conversation.id) { mutableStateOf<Uuid?>(null) }
    var streamingMessageId by remember(conversation.id) { mutableStateOf<Uuid?>(null) }
    var generationBaselineMessageId by remember(conversation.id) { mutableStateOf<Uuid?>(null) }
    var generationBaselineText by remember(conversation.id) { mutableStateOf("") }
    var spokenLength by remember(conversation.id) { mutableStateOf(0) }
    var wasGenerating by remember(conversation.id) { mutableStateOf(false) }

    // Avoid passing the full conversation.currentMessages list to LaunchedEffect,
    // which triggers a deep O(N) List.equals comparison across 200k tokens on every chunk.
    val ttsEnabled = mode != TtsAutoplayMode.OFF && provider != null
    val speechTriggerKey = if (!ttsEnabled) {
        null
    } else if (mode == TtsAutoplayMode.AFTER_GENERATION) {
        loadingJob == null
    } else {
        latestAssistantSpeechMessage(conversation)?.let { it.id to it.toContentText().length }
    }

    LaunchedEffect(conversation.id, speechTriggerKey, mode, provider) {
        if (!ttsEnabled) return@LaunchedEffect
        if (loadingJob == null) {
            if (!wasGenerating) return@LaunchedEffect
            wasGenerating = false
            if (isGenerationCancelled()) {
                tts.stop()
                return@LaunchedEffect
            }
            val latestMessage = latestAssistantSpeechMessage(conversation) ?: return@LaunchedEffect
            val text = latestMessage.toContentText()
            if (latestMessage.id == generationBaselineMessageId && text == generationBaselineText) {
                return@LaunchedEffect
            }
            if (completedMessageId == latestMessage.id && spokenLength >= text.length) {
                return@LaunchedEffect
            }
            if (streamingMessageId != latestMessage.id || spokenLength > text.length) {
                streamingMessageId = latestMessage.id
                spokenLength = 0
            }
            val remaining = text.drop(spokenLength)
            val length = speakablePrefixLength(remaining, final = true)
            val segment = remaining.take(length).trim()
            if (segment.isNotBlank()) {
                tts.speak(
                    text = segment,
                    flushCalled = spokenLength == 0,
                    overrideSetting = provider,
                )
                spokenLength += length
            }
            completedMessageId = latestMessage.id
            return@LaunchedEffect
        }

        if (mode != TtsAutoplayMode.WHILE_GENERATING) return@LaunchedEffect

        if (!wasGenerating) {
            val baselineMessage = latestAssistantSpeechMessage(conversation)
            generationBaselineMessageId = baselineMessage?.id
            generationBaselineText = baselineMessage?.toContentText().orEmpty()
            wasGenerating = true
        }
        val latestMessage = latestAssistantSpeechMessage(conversation) ?: return@LaunchedEffect
        if (latestMessage.id == generationBaselineMessageId && latestMessage.toContentText() == generationBaselineText) {
            return@LaunchedEffect
        }
        val text = latestMessage.toContentText()
        if (streamingMessageId != latestMessage.id) {
            streamingMessageId = latestMessage.id
            spokenLength = 0
        }
        if (spokenLength > text.length) {
            spokenLength = 0
        }
        val remaining = text.drop(spokenLength)
        val length = speakablePrefixLength(remaining, final = false)
        val segment = remaining.take(length).trim()
        if (segment.isNotBlank()) {
            spokenLength += length
            tts.speak(segment, flushCalled = false, overrideSetting = provider)
        }
    }
}

internal fun shouldUseWideChatLayout(
    windowWidth: Dp,
    windowHeight: Dp,
): Boolean {
    return windowWidth >= 840.dp && windowHeight >= 600.dp
}

internal enum class ChatToolbarPlacement {
    Top,
    Bottom
}

@Composable
fun ChatPage(
    target: ChatRouteTarget,
) {
    val id = target.uuid
    val text = target.text
    val files = target.fileUris
    val searchQuery = target.searchQuery
    val persistenceMode = target.persistenceMode
    val focusLatestMessageKey = target.focusLatestMessageKey
    val vm: ChatVM = koinViewModel(
        parameters = {
            parametersOf(id.toString())
        }
    )
    val navController = LocalNavController.current
    val toaster = LocalToaster.current
    val context = LocalContext.current
    val tts = LocalTTSState.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val genericErrorMessage = context.getString(R.string.common_error)

    DisposableEffect(id) {
        vm.setConversationId(id)
        onDispose {
            tts.stop()
            vm.releaseReference()
        }
    }

    // Handle Error (scoped to this conversation + recover errors missed while backgrounded)
    LaunchedEffect(vm, id) {
        vm.scopedErrorFlow.collect { (errConvId, error) ->
            if (errConvId == null || errConvId == id) {
                toaster.show(error.toLocalInferenceUserMessage(context) ?: genericErrorMessage, type = ToastType.Error)
            }
        }
    }
    LaunchedEffect(id) {
        vm.consumePendingError(id)?.let { error ->
            toaster.show(error.toLocalInferenceUserMessage(context) ?: genericErrorMessage, type = ToastType.Error)
        }
    }
    DisposableEffect(id, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.consumePendingError(id)?.let { error ->
                    toaster.show(error.toLocalInferenceUserMessage(context) ?: genericErrorMessage, type = ToastType.Error)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val setting by vm.settings.collectAsStateWithLifecycle()
    val settings = setting
    val conversation by vm.conversation.collectAsStateWithLifecycle()
    val conversationTitle by vm.conversationTitle.collectAsStateWithLifecycle()
    val hasMessages by vm.hasMessages.collectAsStateWithLifecycle()
    val messageCount by vm.messageCount.collectAsStateWithLifecycle()
    val conversationInitialized by vm.conversationInitialized.collectAsStateWithLifecycle()
    val conversationAssistant by vm.conversationAssistant.collectAsStateWithLifecycle()
    val onNewChat = {
        // Always start a fresh generic chat. Route through the shared helper so the
        // existing chat destination is updated in place (launchSingleTop) instead of
        // pushing a raw Screen.Chat route directly.
        navigateToChatPage(navController)
    }
    val loadingJob by vm.conversationJob.collectAsStateWithLifecycle()
    val conversationPersistenceMode by vm.conversationPersistenceMode.collectAsStateWithLifecycle()
    val currentChatModel by vm.currentChatModel.collectAsStateWithLifecycle()
    val enableWebSearch by vm.enableWebSearch.collectAsStateWithLifecycle()
    val currentSearchMode by vm.currentSearchMode.collectAsStateWithLifecycle()
    var manualTemporaryChat by remember(id) { mutableStateOf(false) }
    val activePersistenceMode = when {
        manualTemporaryChat || conversationPersistenceMode == ChatPersistenceMode.TEMPORARY -> ChatPersistenceMode.TEMPORARY
        conversationPersistenceMode == ChatPersistenceMode.PERSIST_ON_REPLY -> ChatPersistenceMode.PERSIST_ON_REPLY
        else -> ChatPersistenceMode.NORMAL
    }
    val isTemporaryChat = activePersistenceMode == ChatPersistenceMode.TEMPORARY
    ChatTtsAutoplayEffect(
        settings = setting,
        assistant = conversationAssistant,
        conversation = conversation,
        loadingJob = loadingJob,
        isGenerationCancelled = { vm.wasGenerationCancelled(conversation.id) },
    )

    LaunchedEffect(conversation.id) {
        manualTemporaryChat = false
    }

    LaunchedEffect(id, persistenceMode) {
        vm.applyRoutePersistenceMode(ChatPersistenceMode.fromRouteValue(persistenceMode))
    }

    LaunchedEffect(conversationPersistenceMode) {
        if (conversationPersistenceMode == ChatPersistenceMode.NORMAL && target.persistenceMode != null) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set(CHAT_ROUTE_TARGET_KEY, target.copy(persistenceMode = null))
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val chatBackdrop = rememberLayerBackdrop()
    val pageBackdrop = rememberLayerBackdrop()
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    // Handle back press when drawer is open
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }


    // Hide keyboard when drawer is open
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            softwareKeyboardController?.hide()
        }
    }

    val windowSize = currentWindowDpSize()
    val useWideLayout = shouldUseWideChatLayout(windowSize.width, windowSize.height)
    val wideDrawerExpandedWidth = 336.dp
    val chatContentMaxWidth = when {
        useWideLayout && windowSize.width >= 1440.dp -> 980.dp
        useWideLayout -> 900.dp
        else -> Dp.Unspecified
    }
    val inputMaxWidth = when {
        useWideLayout && windowSize.width >= 1440.dp -> 920.dp
        useWideLayout -> 840.dp
        else -> Dp.Unspecified
    }
    var isWidePanelCollapsed by rememberSaveable { mutableStateOf(false) }
    var showWideRailAssistantPicker by remember { mutableStateOf(false) }
    var showModalAssistantPicker by remember { mutableStateOf(false) }

    val inputState = rememberChatInputState()
    var inputRestored by remember(id) { mutableStateOf(false) }
    LaunchedEffect(id, text, files) {
        inputRestored = false
        inputState.clearInput()
        val decodedText = decodeChatRouteText(text)
        val importedParts = if (files.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                buildList {
                    files.forEach { sourceFile ->
                        val mimeType = context.getFileMimeType(sourceFile)
                        val fileName = context.getFileNameFromUri(sourceFile) ?: "file"
                        val localFile = ChatAttachmentManager.importChatFile(
                            uri = sourceFile,
                            fileNameHint = fileName,
                            mimeHint = mimeType,
                        )?.uri ?: return@forEach
                        when {
                            mimeType?.startsWith("image/") == true -> add(UIMessagePart.Image(url = localFile.toString()))
                            mimeType?.startsWith("video/") == true -> add(UIMessagePart.Video(url = localFile.toString()))
                            mimeType?.startsWith("audio/") == true -> add(UIMessagePart.Audio(url = localFile.toString()))
                            else -> add(
                                UIMessagePart.Document(
                                    url = localFile.toString(),
                                    fileName = fileName,
                                    mime = mimeType ?: "application/octet-stream"
                                )
                            )
                        }
                    }
                }
            }
        } else {
            emptyList()
        }
        val routeDraft = ChatInputDraft(
            text = decodedText,
            messageContent = importedParts,
        )
        val draft = routeDraft.takeUnless { it.isEmpty }
            ?: withContext(Dispatchers.IO) { ChatSessionDraftStore.get(id) }
            ?: ChatInputDraft()
        inputState.applyDraft(draft)
        inputRestored = true
    }

    LaunchedEffect(id, inputState, isTemporaryChat) {
        snapshotFlow {
            if (inputRestored) {
                inputState.toDraft()
            } else {
                null
            }
        }
            .distinctUntilChanged()
            .onEach { draft ->
                if (draft != null) {
                    ChatSessionDraftStore.putInMemory(id, draft)
                }
            }
            .debounce(300)
            .collect { draft ->
                if (draft != null && !isTemporaryChat) {
                    ChatSessionDraftStore.persistDraft(id, draft)
                }
            }
    }

    val initialChatListScrollPosition = vm.chatListScrollPosition
    val chatListState = remember(conversation.id) {
        LazyListState(
            firstVisibleItemIndex = initialChatListScrollPosition?.firstVisibleItemIndex ?: 0,
            firstVisibleItemScrollOffset = initialChatListScrollPosition?.firstVisibleItemScrollOffset ?: 0,
        )
    }
    val isColdStart = remember(target.id) {
        if (!ChatSwitchSession.coldStartConsumed) {
            ChatSwitchSession.coldStartConsumed = true
            ChatSwitchSession.coldStartTargetId = target.id
            true
        } else if (ChatSwitchSession.coldStartTargetId == target.id) {
            true
        } else {
            ChatSwitchSession.coldStartTargetId = null
            false
        }
    }
    var chatListReady by remember(conversation.id) { mutableStateOf(false) }
    var isSwitchingConversations by remember(target.id) { mutableStateOf(!isColdStart) }
    LaunchedEffect(target.id) {
        if (!isColdStart) {
            isSwitchingConversations = true
            delay(1200L)
            isSwitchingConversations = false
        } else {
            isSwitchingConversations = false
        }
    }
    val isChatSwitching = !isColdStart && settings.displaySetting.enableChatSwitchAnimation && (isSwitchingConversations || !chatListReady || !conversationInitialized || (target.id != conversation.id.toString()))

    // Safely handle back navigation when switching / animating into a chat
    BackHandler(enabled = isChatSwitching) {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            runCatching {
                navController.navigate(Screen.ConversationList) {
                    popUpTo<Screen.Chat> { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Safely navigate back or to conversation list when backing out of chat with the drawer closed
    BackHandler(enabled = !drawerState.isOpen && !isChatSwitching) {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            runCatching {
                navController.navigate(Screen.ConversationList) {
                    launchSingleTop = true
                }
            }
        }
    }

    var chatAnimationsEnabled by remember(conversation.id) { mutableStateOf(false) }
    LaunchedEffect(conversation.id) {
        delay(startupWallpaperPauseMs.toLong())
        chatAnimationsEnabled = true
    }
    var consumedFocusLatestMessageKey by remember(conversation.id) { mutableStateOf<String?>(null) }
    LaunchedEffect(
        conversation.id,
        conversationInitialized,
        messageCount,
        chatListState,
    ) {
        if (!conversationInitialized) {
            return@LaunchedEffect
        }
        if (chatListReady) {
            return@LaunchedEffect
        }
        val savedPosition = vm.chatListScrollPosition
        when {
            conversation.messageNodes.isEmpty() -> {
                chatListReady = true
            }

            savedPosition != null -> {
                chatListState.scrollToItem(
                    index = savedPosition.firstVisibleItemIndex,
                    scrollOffset = savedPosition.firstVisibleItemScrollOffset,
                )
                vm.chatListInitialized = true
                chatListReady = true
            }

            !vm.chatListInitialized -> {
                // Scroll to the last item. The LazyColumn shows turn-groups (not raw nodes),
                // so we cannot use messageNodes.lastIndex as the item index — in a tool-heavy
                // chat there are far fewer groups than nodes. Int.MAX_VALUE is clamped by
                // Compose to the true last item index.
                chatListState.scrollToItem(Int.MAX_VALUE)
                vm.chatListInitialized = true
                chatListReady = true
            }

            else -> {
                chatListReady = true
            }
        }
    }

    LaunchedEffect(focusLatestMessageKey, conversation.id, hasMessages) {
        if (
            focusLatestMessageKey != null &&
            consumedFocusLatestMessageKey != focusLatestMessageKey &&
            hasMessages
        ) {
            consumedFocusLatestMessageKey = focusLatestMessageKey
            // Same reasoning: use Int.MAX_VALUE instead of messageNodes.lastIndex because
            // the LazyColumn items are turn-groups, not individual nodes.
            chatListState.animateScrollToItem(Int.MAX_VALUE)
        }
    }

    LaunchedEffect(conversation.id, hasMessages, chatListReady, chatListState) {
        if (!hasMessages || !chatListReady) {
            return@LaunchedEffect
        }
        snapshotFlow {
            ChatListScrollPosition(
                firstVisibleItemIndex = chatListState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = chatListState.firstVisibleItemScrollOffset,
            )
        }
            .distinctUntilChanged()
            .collect { position ->
                vm.updateChatListScrollPosition(
                    firstVisibleItemIndex = position.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = position.firstVisibleItemScrollOffset,
                )
            }
    }

    fun navigateToAssistantConversation(selectedAssistant: Assistant) {
        scope.launch {
            val targetAssistant = if (selectedAssistant.isDeleted) {
                settings.getCurrentAssistant()
            } else {
                selectedAssistant
            }
            val draft = inputState.toDraft()
            val newConversation = vm.createConversationForAssistant(targetAssistant.id)
            ChatSessionDraftStore.moveDraft(
                fromConversationId = conversation.id,
                toConversationId = newConversation.id,
                draft = draft,
                isTemporary = isTemporaryChat,
            )
            val draftNavigation = buildAssistantSwitchNavigation(
                persistenceMode = activePersistenceMode,
            )
            navigateToChatPage(
                navController = navController,
                chatId = newConversation.id,
                initText = draftNavigation.initText,
                initFiles = draftNavigation.initFiles.map(String::toUri),
                persistenceMode = draftNavigation.persistenceMode,
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            useWideLayout -> {
            AssistantChatTheme(assistant = conversationAssistant) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val wideHazeState = rememberHazeState()
                    val wideBlur = remember(settings.displaySetting.enableBlurEffect, wideHazeState) {
                        DomChatBlur(
                            enabled = settings.displaySetting.enableBlurEffect,
                            hazeState = wideHazeState,
                        )
                    }
                    val widePanelWidth by androidx.compose.animation.core.animateDpAsState(
                        targetValue = if (isWidePanelCollapsed) 80.dp else wideDrawerExpandedWidth,
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 260,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        label = "chat_wide_panel_width"
                    )
                    val widePanelHaptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
                    var widePanelDragX by remember { mutableStateOf(0f) }
                    val wideEffectiveDisplaySetting = settings.getEffectiveDisplaySetting(conversationAssistant)
                    val wideShowsNewChatContent =
                        wideEffectiveDisplaySetting.newChatHeaderStyle != me.lucid.domchat.data.datastore.NewChatHeaderStyle.NONE ||
                            wideEffectiveDisplaySetting.newChatContentStyle != me.lucid.domchat.data.datastore.NewChatContentStyle.NONE
                    val showWideBottomFade =
                        hasConversationMessages(conversation) ||
                            conversationAssistant.presetMessages.isNotEmpty() ||
                            activePersistenceMode == ChatPersistenceMode.TEMPORARY ||
                            !wideShowsNewChatContent
                    CompositionLocalProvider(LocalDomChatBlur provides wideBlur) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(pageBackdrop)
                    ) {
                        AssistantBackground(
                            assistant = conversationAssistant,
                            conversationId = conversation.id.toString(),
                            modifier = Modifier
                                .fillMaxSize()
                                .domChatBlurSource()
                        )

                        ChatWidePanelEdgeFadeOverlay(
                            width = widePanelWidth,
                            placement = chatTopBarPlacement(setting),
                            showBottomFade = showWideBottomFade,
                            settings = setting,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                        Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(widePanelWidth)
                                    .fillMaxHeight()
                                    .clipToBounds()
                                    .pointerInput(isWidePanelCollapsed) {
                                        detectHorizontalDragGestures(
                                            onDragStart = {
                                                widePanelDragX = 0f
                                            },
                                            onHorizontalDrag = { change, dragAmount ->
                                                widePanelDragX += dragAmount
                                                change.consume()
                                            },
                                            onDragEnd = {
                                                val threshold = 36.dp.toPx()
                                                val shouldCollapse = !isWidePanelCollapsed && widePanelDragX < -threshold
                                                val shouldExpand = isWidePanelCollapsed && widePanelDragX > threshold
                                                when {
                                                    shouldCollapse -> {
                                                        widePanelHaptics.perform(HapticPattern.Pop)
                                                        isWidePanelCollapsed = true
                                                    }

                                                    shouldExpand -> {
                                                        widePanelHaptics.perform(HapticPattern.Pop)
                                                        isWidePanelCollapsed = false
                                                    }
                                                }
                                                widePanelDragX = 0f
                                            },
                                            onDragCancel = {
                                                widePanelDragX = 0f
                                            }
                                        )
                                    }
                            ) {
                                AnimatedContent(
                                    targetState = isWidePanelCollapsed,
                                    transitionSpec = {
                                        (fadeIn(
                                            animationSpec = androidx.compose.animation.core.spring(
                                                dampingRatio = 0.7f,
                                                stiffness = 360f
                                            )
                                        ) + scaleIn(
                                            initialScale = 0.96f,
                                            animationSpec = androidx.compose.animation.core.spring(
                                                dampingRatio = 0.7f,
                                                stiffness = 360f
                                            )
                                        )) togetherWith (fadeOut(
                                            animationSpec = androidx.compose.animation.core.spring(
                                                dampingRatio = 0.85f,
                                                stiffness = 420f
                                            )
                                        ) + scaleOut(
                                            targetScale = 0.96f,
                                            animationSpec = androidx.compose.animation.core.spring(
                                                dampingRatio = 0.85f,
                                                stiffness = 420f
                                            )
                                        )) using SizeTransform(clip = true)
                                    },
                                    label = "chat_side_panel",
                                    modifier = Modifier.fillMaxSize()
                                ) { collapsed ->
                                    if (collapsed) {
                                        CollapsedChatSideRail(
                                            current = conversation,
                                            settings = setting,
                                            onExpand = { isWidePanelCollapsed = false },
                                            onOpenStatistics = { navController.navigate(Screen.Menu) },
                                            onOpenSettings = { navController.navigate(Screen.Setting) },
                                            onOpenAssistant = {
                                                showWideRailAssistantPicker = true
                                            },
                                            vm = vm
                                        )
                                    } else {
                                        CompositionLocalProvider(LocalAppBackdrop provides (pageBackdrop ?: chatBackdrop)) {
                                            ChatDrawerContent(
                                                navController = navController,
                                                current = conversation,
                                                vm = vm,
                                                settings = setting,
                                                inputState = inputState,
                                                activePersistenceMode = activePersistenceMode,
                                                drawerState = null,
                                                presentation = ChatDrawerPresentation.PermanentPane,
                                                collapsedWidth = wideDrawerExpandedWidth,
                                                expandedWidth = wideDrawerExpandedWidth,
                                                onCollapseRequest = { isWidePanelCollapsed = true },
                                                onOpenAssistantPicker = { showWideRailAssistantPicker = true },
                                            )
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                ChatPageContent(
                                    inputState = inputState,
                                    loadingJob = loadingJob,
                                    setting = setting,
                                    currentAssistant = conversationAssistant,
                                    conversationInitialized = conversationInitialized,
                                    conversation = conversation,
                                    drawerState = drawerState,
                                    navController = navController,
                                    vm = vm,
                                    chatListState = chatListState,
                                    chatListReady = chatListReady,
                                    isSwitchingConversations = isSwitchingConversations,
                                    chatAnimationsEnabled = chatAnimationsEnabled,
                                    enableWebSearch = enableWebSearch,
                                    currentSearchMode = currentSearchMode,
                                    currentChatModel = currentChatModel,
                                    conversationPersistenceMode = conversationPersistenceMode,
                                    manualTemporaryChat = manualTemporaryChat,
                                    onManualTemporaryChatChange = { manualTemporaryChat = it },
                                    bigScreen = true,
                                    contentMaxWidth = chatContentMaxWidth,
                                    inputMaxWidth = inputMaxWidth,
                                    initialSearchQuery = searchQuery,
                                    renderBackground = false,
                                    chatBackdrop = chatBackdrop,
                                    inheritedBlur = wideBlur,
                                )
                            }
                        }
                        if (showWideRailAssistantPicker) {
                            CompositionLocalProvider(LocalAppBackdrop provides pageBackdrop) {
                                me.lucid.domchat.ui.components.ai.AssistantPickerSheet(
                                    settings = setting,
                                    currentAssistant = conversationAssistant,
                                    onAssistantSelected = { assistant ->
                                        vm.setSelectedAssistant(assistant.id)
                                    },
                                onNavigate = { assistant ->
                                    showWideRailAssistantPicker = false
                                    navigateToAssistantConversation(assistant)
                                },
                                onDismiss = {
                                    showWideRailAssistantPicker = false
                                }
                            )
                            }
                        }
                    }
                    }
                }
            }
        }

        else -> {
            val modalHazeState = rememberHazeState()
            val modalBlur = remember(settings.displaySetting.enableBlurEffect, modalHazeState) {
                DomChatBlur(
                    enabled = settings.displaySetting.enableBlurEffect,
                    hazeState = modalHazeState,
                )
            }
            CompositionLocalProvider(LocalDomChatBlur provides modalBlur) {
                CompositionLocalProvider(LocalAppBackdrop provides chatBackdrop) {
                    ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        CompositionLocalProvider(LocalAppBackdrop provides pageBackdrop) {
                            ChatDrawerContent(
                                navController = navController,
                                current = conversation,
                                vm = vm,
                                settings = setting,
                                inputState = inputState,
                                activePersistenceMode = activePersistenceMode,
                                drawerState = drawerState,
                                presentation = ChatDrawerPresentation.Modal,
                                onOpenAssistantPicker = { showModalAssistantPicker = true },
                            )
                        }
                    }
                ) {
                    var closedDrawerOffset by remember { androidx.compose.runtime.mutableFloatStateOf(Float.NaN) }
                    val isDrawerVisibleOrDragging by remember(drawerState) {
                        derivedStateOf {
                            val offset = drawerState.currentOffset
                            if (drawerState.isClosed && !drawerState.isAnimationRunning && !offset.isNaN()) {
                                if (closedDrawerOffset.isNaN() || offset < closedDrawerOffset) {
                                    closedDrawerOffset = offset
                                }
                            }
                            val draggedFromClosed = !offset.isNaN() &&
                                !closedDrawerOffset.isNaN() &&
                                offset > closedDrawerOffset + 0.5f
                            drawerState.isOpen ||
                                drawerState.targetValue == DrawerValue.Open ||
                                drawerState.isAnimationRunning ||
                                draggedFromClosed
                        }
                    }
                    val recordPageBackdrop = isDrawerVisibleOrDragging || showModalAssistantPicker
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(pageBackdrop, enabled = recordPageBackdrop)
                    ) {
                        ChatPageContent(
                            inheritedBlur = modalBlur,
                            chatBackdrop = chatBackdrop,
                            inputState = inputState,
                            loadingJob = loadingJob,
                            setting = setting,
                            currentAssistant = conversationAssistant,
                            conversationInitialized = conversationInitialized,
                            conversation = conversation,
                            drawerState = drawerState,
                            navController = navController,
                            vm = vm,
                            chatListState = chatListState,
                            chatListReady = chatListReady,
                            isSwitchingConversations = isSwitchingConversations,
                            chatAnimationsEnabled = chatAnimationsEnabled,
                            enableWebSearch = enableWebSearch,
                            currentSearchMode = currentSearchMode,
                            currentChatModel = currentChatModel,
                            conversationPersistenceMode = conversationPersistenceMode,
                            manualTemporaryChat = manualTemporaryChat,
                            onManualTemporaryChatChange = { manualTemporaryChat = it },
                            bigScreen = false,
                            contentMaxWidth = Dp.Unspecified,
                            inputMaxWidth = Dp.Unspecified,
                            initialSearchQuery = searchQuery,
                        )
                    }
                }
                }
            }

            if (showModalAssistantPicker) {
                CompositionLocalProvider(LocalAppBackdrop provides pageBackdrop) {
                    me.lucid.domchat.ui.components.ai.AssistantPickerSheet(
                        settings = setting,
                        currentAssistant = conversationAssistant,
                        onAssistantSelected = { assistant ->
                            vm.setSelectedAssistant(assistant.id)
                        },
                        onNavigate = { assistant ->
                            showModalAssistantPicker = false
                            navigateToAssistantConversation(assistant)
                        },
                        onDismiss = {
                            showModalAssistantPicker = false
                        }
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isChatSwitching,
        enter = EnterTransition.None,
        exit = fadeOut(animationSpec = tween(800, easing = FastOutLinearInEasing)),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(9999f)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        ChatSwitchLoadingIndicator(
            assistant = conversationAssistant,
            conversationTitle = conversationTitle,
            modifier = Modifier.fillMaxSize()
        )
    }
    }
}

/**
 * Startup chrome reveal: chat chrome (topbar, input, new-chat content)
 * slides in once per conversation open, then never again.
 */
@Composable
private fun StartupChromeReveal(
    key: String,
    delayMs: Int,
    motionPolicy: me.lucid.domchat.ui.motion.MotionPolicy,
    from: EntranceDirection,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.entrance(
            key = key,
            motionPolicy = motionPolicy,
            from = from,
            delayMs = delayMs,
        ),
    ) {
        content()
    }
}

@Composable
private fun ChatPageContent(
    inputState: ChatInputState,
    loadingJob: Job?,
    setting: Settings,
    currentAssistant: Assistant,
    conversationInitialized: Boolean,
    bigScreen: Boolean,
    conversation: Conversation,
    drawerState: DrawerState,
    navController: NavHostController,
    vm: ChatVM,
    chatListState: LazyListState,
    chatListReady: Boolean,
    isSwitchingConversations: Boolean = false,
    chatAnimationsEnabled: Boolean,
    enableWebSearch: Boolean,
    currentSearchMode: me.lucid.domchat.data.model.AssistantSearchMode,
    currentChatModel: Model?,
    conversationPersistenceMode: ChatPersistenceMode,
    manualTemporaryChat: Boolean,
    onManualTemporaryChatChange: (Boolean) -> Unit,
    contentMaxWidth: Dp = Dp.Unspecified,
    inputMaxWidth: Dp = Dp.Unspecified,
    initialSearchQuery: String? = null,
    renderBackground: Boolean = true,
    inheritedBlur: DomChatBlur? = null,
    chatBackdrop: LayerBackdrop,
) {
    val settings = setting
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    // Nodes already in the conversation when it opens: they never animate.
    // Captured once as soon as the conversation is initialized / messages load,
    // so existing turns are recognized as history and never re-animate on chat open.
    val preexistingNodeIdsState = remember(conversation.id) {
        mutableStateOf<Set<String>?>(null)
    }
    if (preexistingNodeIdsState.value == null && (conversationInitialized || conversation.messageNodes.isNotEmpty())) {
        preexistingNodeIdsState.value = conversation.messageNodes.map { it.id.toString() }.toSet()
    }
    val preexistingNodeIds = preexistingNodeIdsState.value ?: emptySet()
    val context = LocalContext.current
    val modelRequiredMessage = context.getString(R.string.chat_model_required)
    var previewMode by rememberSaveable { mutableStateOf(false) }
    val activePersistenceMode = when {
        manualTemporaryChat || conversationPersistenceMode == ChatPersistenceMode.TEMPORARY -> ChatPersistenceMode.TEMPORARY
        conversationPersistenceMode == ChatPersistenceMode.PERSIST_ON_REPLY -> ChatPersistenceMode.PERSIST_ON_REPLY
        else -> ChatPersistenceMode.NORMAL
    }
    val isTemporaryChat = activePersistenceMode == ChatPersistenceMode.TEMPORARY

    // State for user message regeneration confirmation dialog
    var showUserRegenerateConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var pendingUserRegenerateMessage by remember { mutableStateOf<me.lucid.ai.ui.UIMessage?>(null) }
    var pendingDeleteMessage by remember { mutableStateOf<me.lucid.ai.ui.UIMessage?>(null) }
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val onNewChat = {
        if (conversationInitialized) {
            // Route through the shared helper so the existing chat destination is
            // updated in place instead of pushing a raw Screen.Chat route.
            navigateToChatPage(navController)
        }
    }
    var showToolbarOverflowMenu by remember { mutableStateOf(false) }
    var showContextUsagePopup by remember { mutableStateOf(false) }
    var showChatInfoSheet by remember { mutableStateOf(false) }
    var showCreatorNotesSheet by remember { mutableStateOf(false) }
    var activeChatMessageAction by remember { mutableStateOf<ActiveChatMessageAction?>(null) }
    var activeCopyMessage by remember { mutableStateOf<me.lucid.ai.ui.UIMessage?>(null) }
    var isChatShareSelecting by rememberSaveable { mutableStateOf(false) }
    var selectedChatShareItems by remember(conversation.id) { mutableStateOf<Set<Uuid>>(emptySet()) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showCustomGreetingDialog by remember { mutableStateOf(false) }
    var chatSearchQuery by remember(conversation.id) { mutableStateOf(initialSearchQuery.orEmpty()) }
    var consumedInitialSearchQuery by remember(conversation.id) { mutableStateOf<String?>(null) }
    val toolbarPlacement = chatTopBarPlacement(setting)
    val motionPolicy = LocalMotionPolicy.current
    val reduceMotion = LocalMotionPolicy.current.reduceMotion
    val isGenerating = loadingJob != null
    val density = LocalDensity.current
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val hazeState = rememberHazeState()
    val blurActive = settings.displaySetting.enableBlurEffect
    val localBlur = remember(blurActive, hazeState) {
        DomChatBlur(
            enabled = blurActive,
            hazeState = hazeState,
        )
    }
    val blur = inheritedBlur ?: localBlur
    val requestContextUsage by vm.contextUsage.collectAsStateWithLifecycle()
    val contextManagementActivity by vm.contextManagementActivity.collectAsStateWithLifecycle()
    val assistantMemories by vm.assistantMemories.collectAsStateWithLifecycle()
    val debouncedPendingParts by produceState<List<UIMessagePart>>(
        initialValue = emptyList(),
        key1 = inputState
    ) {
        snapshotFlow {
            inputState.textContent.text.toString() to inputState.pendingAttachments
        }
            .debounce(400)
            .flowOn(Dispatchers.Default)
            .collect { (text, attachments) ->
                value = if (text.isBlank() && attachments.isEmpty()) {
                    emptyList()
                } else {
                    listOf(UIMessagePart.Text(text)) + attachments.map { it.part }
                }
            }
    }
    val showContextSummary = settings.displaySetting.showContextTokenSummary
    val contextMeterUsage by produceState<ContextUsageBreakdown?>(
        initialValue = requestContextUsage,
        requestContextUsage,
        showContextSummary,
        currentChatModel?.id,
        if (isGenerating) 0L else conversation.updateAt.toEpochMilli(),
        currentAssistant.id,
        debouncedPendingParts,
        isGenerating,
        assistantMemories,
        activePersistenceMode,
        setting.displaySetting.contextBudgetConfig,
    ) {
        if (!showContextSummary) {
            value = null
            return@produceState
        }
        if (isGenerating) {
            kotlinx.coroutines.delay(350L)
        }
        withContext(Dispatchers.Default) {
            value = ContextBudgetCalculator.calculate(
                model = currentChatModel,
                conversation = conversation,
                assistant = currentAssistant,
                settings = setting,
                memoryCandidates = assistantMemories,
                persistenceMode = activePersistenceMode,
                pendingParts = debouncedPendingParts,
                requestUsage = requestContextUsage,
                isGenerating = isGenerating,
            )
        }
    }

    LaunchedEffect(contextMeterUsage) {
        if (contextMeterUsage == null) showContextUsagePopup = false
    }

    LaunchedEffect(conversation.id) {
        previewMode = false
        showToolbarOverflowMenu = false
        showUserRegenerateConfirmDialog = false
        pendingUserRegenerateMessage = null
        if (isChatShareSelecting) {
            isChatShareSelecting = false
            selectedChatShareItems = emptySet()
        }
    }
    
    // Auto-scroll to first matching message when opened from search
    LaunchedEffect(initialSearchQuery, conversation.id, conversation.messageNodes.isNotEmpty()) {
        if (
            !initialSearchQuery.isNullOrBlank() &&
            consumedInitialSearchQuery != initialSearchQuery &&
            conversation.messageNodes.isNotEmpty()
        ) {
            consumedInitialSearchQuery = initialSearchQuery
            // Find the first message containing the search query
            val matchIndex = conversation.messageNodes.indexOfFirst { node ->
                node.currentMessage.toText().contains(initialSearchQuery, ignoreCase = true)
            }
            if (matchIndex >= 0) {
                // Small delay to let the UI settle
                delay(100)
                chatListState.animateScrollToItem(matchIndex)
            }
        }
    }
    
    // Track the last selected search provider index so we can restore it when toggling on
    var lastProviderIndex by rememberSaveable { mutableStateOf(0) }
    
    // Update lastProviderIndex whenever currentSearchMode is Provider
    LaunchedEffect(currentSearchMode) {
        if (currentSearchMode is me.lucid.domchat.data.model.AssistantSearchMode.Provider) {
            lastProviderIndex = currentSearchMode.index
        }
    }


    fun navigateToAssistantConversation(selectedAssistant: Assistant) {
        scope.launch {
            val targetAssistant = if (selectedAssistant.isDeleted) {
                settings.getCurrentAssistant()
            } else {
                selectedAssistant
            }
            val draft = inputState.toDraft()
            val newConversation = vm.createConversationForAssistant(targetAssistant.id)
            ChatSessionDraftStore.moveDraft(
                fromConversationId = conversation.id,
                toConversationId = newConversation.id,
                draft = draft,
                isTemporary = isTemporaryChat,
            )
            val draftNavigation = buildAssistantSwitchNavigation(
                persistenceMode = activePersistenceMode,
            )
            navigateToChatPage(
                navController = navController,
                chatId = newConversation.id,
                initText = draftNavigation.initText,
                initFiles = draftNavigation.initFiles.map(String::toUri),
                persistenceMode = draftNavigation.persistenceMode,
            )
        }
    }

    LaunchedEffect(conversation.id, initialSearchQuery) {
        chatSearchQuery = initialSearchQuery.orEmpty()
    }

    fun startChatShareSelection() {
        showToolbarOverflowMenu = false
        previewMode = false
        selectedChatShareItems = conversation.messageNodes.map { it.id }.toSet()
        isChatShareSelecting = true
    }

    fun cancelChatShareSelection() {
        isChatShareSelecting = false
        selectedChatShareItems = emptySet()
    }

    fun confirmChatShareSelection() {
        isChatShareSelecting = false
        if (selectedChatShareItems.isNotEmpty()) {
            showExportSheet = true
        }
    }


    var isAssistantTurnActive by remember(conversation.id) { mutableStateOf(false) }

    LaunchedEffect(loadingJob, isAssistantTurnActive) {
        inputState.loading = loadingJob != null || isAssistantTurnActive
    }

    AssistantChatTheme(assistant = currentAssistant) {
        // Pure wallpaper layer — the ONLY backdrop chat bubbles, the chat list and
        // new-chat surfaces may sample. Chat content is never recorded into it, so
        // bubbles can never mirror the messages or the composer onto themselves.
        val wallpaperBackdrop = rememberLayerBackdrop()
        // Records the chat list content. Served ONLY to overlay floaters (top bar,
        // composer, picker, menus, sheets) and only while it is opaque and ready.
        val contentBackdrop = rememberLayerBackdrop(
            onDraw = {
                // Synchronously subscribe to scroll changes during the draw pass.
                // In Compose, snapshot reads inside DrawModifierNode.draw() automatically
                // register snapshot observation on this node. Whenever LazyColumn scrolls on ANY frame,
                // this source layer is immediately marked dirty before the draw traversal begins,
                // re-recording synchronously on the very same frame tick at 120Hz!
                chatListState.firstVisibleItemScrollOffset
                chatListState.firstVisibleItemIndex
                // Draw the recorded wallpaper backdrop layer behind content so overlay glass
                // samples both the wallpaper and any messages without running a duplicate
                // AssistantBackground / AsyncImage / RenderEffect hierarchy!
                drawLayer(wallpaperBackdrop.graphicsLayer)
                drawContent()
            }
        )
        // Empty chat: bind the wallpaper backdrop immediately so liquid glass
        // paints at launch with 0ms delay instead of waiting for the first frame.
        val emptyChat = conversation.messageNodes.isEmpty()
        // Overlay floaters sample the chat content once it is opaque and ready;
        // everything else (bubbles, chat list, new-chat content) samples wallpaper.
        // `chatListReady` and `contentBackdrop.hasRecording` are reactive Compose state.
        val isScrollingWhileStreaming = isGenerating && chatListState.isScrollInProgress
        val overlayBackdrop = if (emptyChat || !chatListReady || !contentBackdrop.hasRecording || isScrollingWhileStreaming) {
            wallpaperBackdrop
        } else {
            contentBackdrop
        }

        LaunchedEffect(conversation.id) {
            contentBackdrop.reset()
            wallpaperBackdrop.invalidateSource()
            chatBackdrop.invalidateSource()
            delay(50)
            wallpaperBackdrop.invalidateSource()
            chatBackdrop.invalidateSource()
            wallpaperBackdrop.geometryChanged()
            chatBackdrop.geometryChanged()
            delay(startupWallpaperPauseMs.toLong().coerceAtLeast(100L))
            wallpaperBackdrop.invalidateSource()
            chatBackdrop.invalidateSource()
            contentBackdrop.invalidateSource()
            wallpaperBackdrop.geometryChanged()
            chatBackdrop.geometryChanged()
            contentBackdrop.geometryChanged()
        }
        LaunchedEffect(chatListReady) {
            if (chatListReady) {
                contentBackdrop.invalidateSource()
                contentBackdrop.geometryChanged()
                delay(30)
                wallpaperBackdrop.invalidateSource()
                chatBackdrop.invalidateSource()
                contentBackdrop.invalidateSource()
                wallpaperBackdrop.geometryChanged()
                chatBackdrop.geometryChanged()
                contentBackdrop.geometryChanged()
            }
        }

        CompositionLocalProvider(LocalDomChatBlur provides blur) {
            Surface(
                color = if (renderBackground) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.background
                },
                contentColor = if (LocalDarkMode.current) Color.White else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val needsHazeBlurSource = !isRuntimeShaderSupported() ||
                        setting.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED
                    val barBlurDp = setting.displaySetting.barGradientBlur
                    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    val topBarBlurHeight = statusBarTopPadding + if (toolbarPlacement == ChatToolbarPlacement.Top) {
                        chatTopToolbarFadeHeight
                    } else {
                        chatBottomToolbarTopFadeHeight
                    }
                    val showBottomBarBlur = hasConversationMessages(conversation) ||
                        hasConversationPresetMessages(conversation, currentAssistant) ||
                        isTemporaryChat
                    val bottomBarBlurHeight = if (showBottomBarBlur) 200.dp else 0.dp
                    if (renderBackground) {
                        AssistantBackground(
                            assistant = currentAssistant,
                            conversationId = conversation.id.toString(),
                            modifier = Modifier
                                .progressiveEdgeBlur(
                                    blurDp = barBlurDp,
                                    topHeightDp = topBarBlurHeight,
                                    bottomHeightDp = bottomBarBlurHeight,
                                )
                                .then(if (needsHazeBlurSource) Modifier.domChatBlurSource() else Modifier)
                                .layerBackdrop(wallpaperBackdrop),
                            // Wallpaper bitmap decoded: invalidate all backdrops so all
                            // liquid glass samplers re-record against the real background.
                            onWallpaperLoaded = {
                                wallpaperBackdrop.invalidateSource()
                                chatBackdrop.invalidateSource()
                                contentBackdrop.invalidateSource()
                                wallpaperBackdrop.geometryChanged()
                                chatBackdrop.geometryChanged()
                                contentBackdrop.geometryChanged()
                            },
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .progressiveEdgeBlur(
                                    blurDp = barBlurDp,
                                    topHeightDp = topBarBlurHeight,
                                    bottomHeightDp = bottomBarBlurHeight,
                                )
                                .background(MaterialTheme.colorScheme.background)
                                .then(if (needsHazeBlurSource) Modifier.domChatBlurSource() else Modifier)
                                .layerBackdrop(wallpaperBackdrop)
                        )
                    }

                    Scaffold(
                        topBar = {
                            CompositionLocalProvider(
                                LocalAppBackdrop provides overlayBackdrop
                            ) {
                                if (toolbarPlacement == ChatToolbarPlacement.Top) {
                                    StartupChromeReveal(
                                        key = "${conversation.id}:topbar",
                                        delayMs = startupWallpaperPauseMs,
                                        motionPolicy = motionPolicy,
                                        from = EntranceDirection.DOWN,
                                    ) {
                                        ChatToolbar(
                                            placement = ChatToolbarPlacement.Top,
                                            settings = setting,
                                            currentAssistant = currentAssistant,
                                            conversationInitialized = conversationInitialized,
                                            conversation = conversation,
                                            bigScreen = bigScreen,
                                            drawerState = drawerState,
                                            previewMode = previewMode,
                                            isTemporaryChat = isTemporaryChat,
                                            currentChatModel = currentChatModel,
                                            isGenerating = isGenerating,
                                            showCloseAction = previewMode || isChatShareSelecting,
                                            showTopFade = true,
                                            vm = vm,
                                            contextUsage = contextMeterUsage,
                                            onContextMeterClick = {
                                                showToolbarOverflowMenu = false
                                                showContextUsagePopup = !showContextUsagePopup
                                            },
                                            onOpenOverflowMenu = {
                                                showContextUsagePopup = false
                                                showToolbarOverflowMenu = !showToolbarOverflowMenu
                                            },
                                            onCloseAction = {
                                                showToolbarOverflowMenu = false
                                                if (previewMode) {
                                                    previewMode = false
                                                }
                                                if (isChatShareSelecting) {
                                                    cancelChatShareSelection()
                                                }
                                            },
                                            onUpdateSettings = { newSettings ->
                                                vm.updateSettings(newSettings)
                                            },
                                            onSwitchAssistant = { assistant ->
                                                navigateToAssistantConversation(assistant)
                                            },
                                            onToggleTemporaryChat = {
                                                onManualTemporaryChatChange(!manualTemporaryChat)
                                            },
                                            onNewChat = onNewChat
                                        )
                                    }
                                } else {
                                    ChatTopFadeOverlay(
                                        fadeHeight = chatBottomToolbarTopFadeHeight,
                                        settings = setting,
                                    )
                                }
                            }
                        },
                        // Input is rendered manually at the bottom of the screen
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0.dp)
                    ) { _ ->
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                    val recentlyRestoredNodeIds = vm.recentlyRestoredNodeIds.collectAsStateWithLifecycle().value
                    val conversationSnapshots = remember { mutableMapOf<Uuid, Conversation>() }
                    val chatListStateSnapshots = remember { mutableMapOf<Uuid, LazyListState>() }
                    val searchQuerySnapshots = remember { mutableMapOf<Uuid, String?>() }
                    SideEffect {
                        conversationSnapshots[conversation.id] = conversation
                        chatListStateSnapshots[conversation.id] = chatListState
                        searchQuerySnapshots[conversation.id] = initialSearchQuery
                        while (conversationSnapshots.size > 3) {
                            val oldestKey = conversationSnapshots.keys.firstOrNull { it != conversation.id } ?: break
                            conversationSnapshots.remove(oldestKey)
                            chatListStateSnapshots.remove(oldestKey)
                            searchQuerySnapshots.remove(oldestKey)
                        }
                    }
                    AnimatedContent(
                        targetState = conversation.id,
                        transitionSpec = {
                            if (initialState == targetState) {
                                fadeIn(animationSpec = tween(0)) togetherWith fadeOut(animationSpec = tween(0))
                            } else {
                                (fadeIn(animationSpec = tween(400, easing = LinearOutSlowInEasing)) +
                                    slideInHorizontally(
                                        animationSpec = spring(dampingRatio = 0.85f, stiffness = 180f),
                                        initialOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() }
                                    )) togetherWith
                                (fadeOut(animationSpec = tween(350, easing = FastOutLinearInEasing)) +
                                    slideOutHorizontally(
                                        animationSpec = tween(350, easing = FastOutLinearInEasing),
                                        targetOffsetX = { fullWidth -> -(fullWidth * 0.08f).toInt() }
                                    ))
                            }
                        },
                        label = "chat_conversation_content",
                        modifier = Modifier.fillMaxSize(),
                    ) { targetConversationId ->
                        val fallbackListState = remember(targetConversationId) { LazyListState() }
                        val frameListState = if (targetConversationId == conversation.id) {
                            chatListState
                        } else {
                            chatListStateSnapshots[targetConversationId] ?: fallbackListState
                        }
                        val frozenDuringScrollHolder = remember(targetConversationId) {
                            object {
                                var snapshot: Conversation? = null
                            }
                        }
                        val liveFrameConversation = if (targetConversationId == conversation.id) {
                            conversation
                        } else {
                            conversationSnapshots[targetConversationId] ?: Conversation.ofId(targetConversationId)
                        }
                        val frameConversation = if (isGenerating && frameListState.isScrollInProgress && targetConversationId == conversation.id) {
                            frozenDuringScrollHolder.snapshot ?: liveFrameConversation.also { frozenDuringScrollHolder.snapshot = it }
                        } else {
                            frozenDuringScrollHolder.snapshot = null
                            liveFrameConversation
                        }
                        val frameInitialSearchQuery = if (targetConversationId == conversation.id) {
                            initialSearchQuery
                        } else {
                            searchQuerySnapshots[targetConversationId]
                        }
                        val contentAlpha by animateFloatAsState(
                            targetValue = if (targetConversationId != conversation.id || chatListReady) 1f else 0f,
                            animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
                            label = "chatContentAlpha"
                        )
                        CompositionLocalProvider(
                            LocalChatAnimationsEnabled provides chatAnimationsEnabled,
                            LocalActiveChatMessageAction provides { activeChatMessageAction = it },
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .progressiveEdgeBlur(
                                        blurDp = barBlurDp,
                                        topHeightDp = topBarBlurHeight,
                                        bottomHeightDp = bottomBarBlurHeight,
                                    )
                                    .layerBackdrop(contentBackdrop)
                                    .graphicsLayer {
                                        alpha = contentAlpha
                                    }
                            ) {
                                CompositionLocalProvider(
                                    me.lucid.domchat.ui.motion.LocalPreexistingMessageNodes provides preexistingNodeIds
                                ) {
                                    // Compaction badge: shows when older turns have been summarized into contextSummary
                                    conversation.contextSummary?.let { summary ->
                                        if (summary.isNotBlank() && settings.displaySetting.contextBudgetConfig.enableCompaction) {
                                            CompactionBadge(
                                                summary = summary,
                                                summarizedUpToIndex = conversation.contextSummaryUpToIndex,
                                            )
                                        }
                                    }
                                    val latestFrameConversation by rememberUpdatedState(frameConversation)
                                    val onUpdateMessage = remember(targetConversationId, vm) {
                                        { newNode: MessageNode ->
                                            if (vm.conversation.value.id == targetConversationId) {
                                                val currentFrame = latestFrameConversation
                                                val oldNode = currentFrame.messageNodes.find { it.id == newNode.id }
                                                if (oldNode != null) {
                                                    if (oldNode.messages.size != newNode.messages.size) {
                                                        vm.updateConversation(
                                                            currentFrame.copy(
                                                                messageNodes = currentFrame.messageNodes.map { node ->
                                                                    if (node.id == newNode.id) newNode else node
                                                                },
                                                                updateAt = java.time.Instant.now()
                                                            )
                                                        )
                                                    } else if (oldNode.selectIndex != newNode.selectIndex) {
                                                        vm.selectMessageNode(newNode.id, newNode.selectIndex)
                                                    } else {
                                                        vm.updateConversation(
                                                            currentFrame.copy(
                                                                messageNodes = currentFrame.messageNodes.map { node ->
                                                                    if (node.id == newNode.id) newNode else node
                                                                },
                                                                updateAt = java.time.Instant.now()
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    val onJumpToMessageCallback = remember(targetConversationId, vm, frameListState, scope) {
                                        { index: Int ->
                                            if (vm.conversation.value.id == targetConversationId) {
                                                previewMode = false
                                                scope.launch {
                                                    delay(350)
                                                    frameListState.animateScrollToItem(index)
                                                }
                                            }
                                        }
                                    }
                                    val onRegenerateCallback = remember(targetConversationId, vm) {
                                        { message: UIMessage ->
                                            if (vm.conversation.value.id == targetConversationId) {
                                                vm.regenerateAtMessage(message)
                                            }
                                        }
                                    }
                                    val onEditCallback = remember(targetConversationId, vm, inputState) {
                                        { message: UIMessage ->
                                            if (vm.conversation.value.id == targetConversationId) {
                                                inputState.editingMessage = message.id
                                                inputState.setContents(message.parts)
                                            }
                                        }
                                    }
                                    val onDeleteCallback = remember(targetConversationId, vm) {
                                        { message: UIMessage ->
                                            if (vm.conversation.value.id == targetConversationId) {
                                                pendingDeleteMessage = message
                                            }
                                        }
                                    }
                                    val onDeleteSwipeCallback = remember(targetConversationId, vm, scope) {
                                        { message: UIMessage ->
                                            if (vm.conversation.value.id == targetConversationId) {
                                                scope.launch {
                                                    vm.deleteMessage(message)
                                                }
                                            }
                                        }
                                    }
                                    val onForkCallback = remember(targetConversationId, vm, scope, navController) {
                                        { message: UIMessage ->
                                            if (vm.conversation.value.id == targetConversationId) {
                                                scope.launch {
                                                    val forkConversation = vm.forkMessage(message)
                                                    if (forkConversation != null) {
                                                        navigateToChatPage(navController, forkConversation.id)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    val onSearchQueryChangeCallback = remember(targetConversationId, vm) {
                                        { q: String ->
                                            if (vm.conversation.value.id == targetConversationId) {
                                                chatSearchQuery = q
                                            }
                                        }
                                    }
                                    val onSelectedShareItemsChangeCallback = remember(targetConversationId, vm) {
                                        { items: Set<Uuid> ->
                                            if (vm.conversation.value.id == targetConversationId) {
                                                selectedChatShareItems = items
                                            }
                                        }
                                    }
                                    val onAssistantTurnActiveChangedCallback = remember(targetConversationId, vm) {
                                        { active: Boolean ->
                                            if (vm.conversation.value.id == targetConversationId) {
                                                isAssistantTurnActive = active
                                            }
                                        }
                                    }
                                    val onAddCustomGreetingCallback = remember(targetConversationId, vm) {
                                        {
                                            if (vm.conversation.value.id == targetConversationId) {
                                                showCustomGreetingDialog = true
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) {
                                        CompositionLocalProvider(LocalAppBackdrop provides wallpaperBackdrop) {
                                            ChatList(
                                                innerPadding = PaddingValues(
                                                    top = chatListTopPadding(toolbarPlacement, statusBarTopPadding),
                                                    bottom = chatListBottomPadding(toolbarPlacement)
                                                ),
                                                wallpaperBackdrop = wallpaperBackdrop,
                                                conversation = frameConversation,
                                                state = frameListState,
                                                loading = targetConversationId == conversation.id && loadingJob != null,
                                                previewMode = previewMode,
                                                settings = setting,
                                                recentlyRestoredNodeIds = recentlyRestoredNodeIds,
                                                initialSearchQuery = frameInitialSearchQuery,
                                                searchQuery = chatSearchQuery,
                                                onSearchQueryChange = onSearchQueryChangeCallback,
                                                shareSelecting = isChatShareSelecting,
                                                selectedShareItems = selectedChatShareItems,
                                                onSelectedShareItemsChange = onSelectedShareItemsChangeCallback,
                                                contentMaxWidth = contentMaxWidth,
                                                onJumpToMessage = onJumpToMessageCallback,
                                                onRegenerate = onRegenerateCallback,
                                                onEdit = onEditCallback,
                                                onDelete = onDeleteCallback,
                                                onDeleteSwipe = onDeleteSwipeCallback,
                                                onUpdateMessage = onUpdateMessage,
                                                onAssistantTurnActiveChanged = onAssistantTurnActiveChangedCallback,
                                                onForkMessage = onForkCallback,
                                                onAddCustomGreeting = onAddCustomGreetingCallback,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                ChatExportSheet(
                    visible = showExportSheet,
                    onDismissRequest = {
                        showExportSheet = false
                        selectedChatShareItems = emptySet()
                    },
                    conversation = conversation,
                    selectedMessages = conversation.messageNodes
                        .filter { it.id in selectedChatShareItems }
                        .map { it.currentMessage }
                )

                val hasConversationContent = hasConversationMessages(conversation)
                val hasAnyPresetMessages = hasConversationPresetMessages(conversation, currentAssistant)
                val effectiveDisplaySetting = settings.getEffectiveDisplaySetting(currentAssistant)
                val scrollToBottomRevealThresholdPx = with(density) { 72.dp.roundToPx() }
                val showScrollToBottomButton by remember(
                    chatListState,
                    previewMode,
                    hasConversationContent,
                    scrollToBottomRevealThresholdPx,
                ) {
                    derivedStateOf {
                        if (previewMode || !hasConversationContent) {
                            false
                        } else {
                            val layoutInfo = chatListState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            if (totalItems <= 0) {
                                false
                            } else {
                                val lastContentIndex = (totalItems - 2).coerceAtLeast(0)
                                val lastContentItem = layoutInfo.visibleItemsInfo
                                    .firstOrNull { it.index == lastContentIndex }
                                val lastContentBottom = lastContentItem?.let { it.offset + it.size }
                                val isNearBottom = lastContentBottom != null &&
                                    lastContentBottom <= layoutInfo.viewportEndOffset + scrollToBottomRevealThresholdPx
                                chatListState.canScrollForward && !isNearBottom
                            }
                        }
                    }
                }
                
                // Temporary chat overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = isTemporaryChat && !hasConversationContent && !hasAnyPresetMessages,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .entrance(
                            key = "${conversation.id}:tempchat",
                            motionPolicy = motionPolicy,
                            from = EntranceDirection.UP,
                            delayMs = startupWallpaperPauseMs + startupStaggerStepMs,
                            distance = 30.dp,
                        ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.HistoryToggleOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(R.string.temporary_chat_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                
                val headerStyle = effectiveDisplaySetting.newChatHeaderStyle
                val contentStyle = effectiveDisplaySetting.newChatContentStyle
                val showNewChatContent = headerStyle != me.lucid.domchat.data.datastore.NewChatHeaderStyle.NONE || contentStyle != me.lucid.domchat.data.datastore.NewChatContentStyle.NONE
                val showModeComposer = previewMode || isChatShareSelecting
                
                // Only observe WindowInsets.isImeVisible when the chat is empty and NewChatContent could show;
                // reading isImeVisible unconditionally at top-level forces full ChatPage + ChatList recomposition on IME open.
                val canPotentiallyShowNewChat = !hasConversationContent && showNewChatContent && !showModeComposer
                val isImeVisibleNow = if (canPotentiallyShowNewChat) WindowInsets.isImeVisible else false
                val shouldShowNewChatContent by remember(
                    canPotentiallyShowNewChat,
                    isTemporaryChat,
                    hasConversationContent,
                    hasAnyPresetMessages,
                    showNewChatContent,
                    isImeVisibleNow,
                ) {
                    derivedStateOf {
                        if (!canPotentiallyShowNewChat) {
                            false
                        } else {
                            val hasTextInput = inputState.textContent.text.isNotEmpty() || inputState.messageContent.isNotEmpty()
                            shouldShowNewChatContent(
                                isTemporaryChat = isTemporaryChat,
                                hasConversationMessages = hasConversationContent,
                                hasAnyPresetMessages = hasAnyPresetMessages,
                                showNewChatContent = showNewChatContent,
                                hasTextInput = hasTextInput,
                                isKeyboardOpen = isImeVisibleNow,
                            )
                        }
                    }
                }
                
                // State for assistant picker triggered from header avatar
                var showHeaderAssistantPicker by remember { mutableStateOf(false) }
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = shouldShowNewChatContent,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 28.dp)
                        .entrance(
                            key = "${conversation.id}:newchat",
                            motionPolicy = motionPolicy,
                            from = EntranceDirection.UP,
                            delayMs = startupWallpaperPauseMs + startupStaggerStepMs,
                            distance = 30.dp,
                        ),
                ) {
                        CompositionLocalProvider(LocalAppBackdrop provides wallpaperBackdrop) {
                            NewChatContent(
                                assistant = currentAssistant,
                                headerStyle = headerStyle,
                                contentStyle = contentStyle,
                                showAvatarInHeader = effectiveDisplaySetting.newChatShowAvatar,
                                hasBackgroundImage = currentAssistant.background != null,
                                onTemplateClick = { prompt ->
                                    // Set text and focus the input field to show keyboard
                                    inputState.setMessageTextAndFocus(prompt, scope)
                                },
                                onAvatarClick = {
                                    showHeaderAssistantPicker = true
                                }
                            )
                        }
                    }
                
                // Assistant picker sheet triggered from header avatar. On an empty
                // chat the content backdrop has no recording yet, so fall back to the
                // wallpaper backdrop so liquid glass works immediately on frame 0.
                if (showHeaderAssistantPicker) {
                    CompositionLocalProvider(
                        LocalAppBackdrop provides overlayBackdrop
                    ) {
                        me.lucid.domchat.ui.components.ai.AssistantPickerSheet(
                            settings = setting,
                            currentAssistant = currentAssistant,
                            onAssistantSelected = { selectedAssistant ->
                                vm.setSelectedAssistant(selectedAssistant.id)
                            },
                            onNavigate = { selectedAssistant ->
                                showHeaderAssistantPicker = false
                                navigateToAssistantConversation(selectedAssistant)
                            },
                            onDismiss = { showHeaderAssistantPicker = false }
                        )
                    }
                }

                // User message regeneration confirmation dialog
                if (showUserRegenerateConfirmDialog && pendingUserRegenerateMessage != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showUserRegenerateConfirmDialog = false
                            pendingUserRegenerateMessage = null
                        },
                        title = { Text(stringResource(R.string.chat_regenerate_user_message_title)) },
                        text = {
                            Text(stringResource(R.string.chat_regenerate_user_message_warning))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    pendingUserRegenerateMessage?.let { message ->
                                        vm.regenerateAtMessage(message)
                                    }
                                    showUserRegenerateConfirmDialog = false
                                    pendingUserRegenerateMessage = null
                                }
                            ) {
                                Text(stringResource(R.string.regenerate))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showUserRegenerateConfirmDialog = false
                                    pendingUserRegenerateMessage = null
                                }
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }

                // Message deletion confirmation dialog (liquid glass).
                // Shared by the message action row delete button and the message actions popover delete button.
                pendingDeleteMessage?.let { messageToDelete ->
                    CompositionLocalProvider(LocalAppBackdrop provides overlayBackdrop) {
                        ChatDestructiveConfirmDialog(
                            visible = true,
                            title = stringResource(R.string.chat_message_delete_title),
                            message = stringResource(R.string.chat_message_delete_confirm),
                            onConfirm = {
                                haptics.perform(HapticPattern.Error)
                                pendingDeleteMessage = null
                                scope.launch {
                                    val backup = conversation
                                    val removedIds = vm.deleteMessage(messageToDelete)
                                    toaster.show(
                                        message = context.getString(R.string.message_deleted),
                                        action = me.lucid.domchat.ui.components.ui.ToastAction(
                                            label = context.getString(R.string.undo),
                                            onClick = {
                                                vm.updateConversation(backup)
                                                vm.markNodesAsRestored(removedIds)
                                            }
                                        )
                                    )
                                }
                            },
                            onDismiss = { pendingDeleteMessage = null },
                        )
                    }
                }

                // Gradient behind floating toolbar - hidden when showing new chat content or in transparent immersive mode
                val isTransparentStyle = settings.displaySetting.chatMessageStyle == me.lucid.domchat.data.model.ChatMessageStyle.TRANSPARENT_BOTTOM_BLUR
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isTransparentStyle && (hasConversationContent || hasAnyPresetMessages || isTemporaryChat || !showNewChatContent),
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    val barDensity = settings.displaySetting.barGradientDensity.coerceIn(0f, 1f)
                    if (barDensity > 0.001f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    brush = if (toolbarPlacement == ChatToolbarPlacement.Bottom) {
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.72f * barDensity),
                                                Color.Black.copy(alpha = 0.97f * barDensity)
                                            )
                                        )
                                    } else {
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.92f * barDensity)
                                            )
                                        )
                                    }
                                )
                        )
                    }
                }

                val bottomToolbarContent: (@Composable () -> Unit)? = if (toolbarPlacement == ChatToolbarPlacement.Bottom) {
                    {
                        ChatToolbar(
                            placement = ChatToolbarPlacement.Bottom,
                            settings = setting,
                            currentAssistant = currentAssistant,
                            conversationInitialized = conversationInitialized,
                            conversation = conversation,
                            bigScreen = bigScreen,
                            drawerState = drawerState,
                            previewMode = previewMode,
                            isTemporaryChat = isTemporaryChat,
                            currentChatModel = currentChatModel,
                            isGenerating = isGenerating,
                            showCloseAction = previewMode || isChatShareSelecting,
                            vm = vm,
                            contextUsage = contextMeterUsage,
                            onContextMeterClick = {
                                showToolbarOverflowMenu = false
                                showContextUsagePopup = !showContextUsagePopup
                            },
                            onOpenOverflowMenu = {
                                showContextUsagePopup = false
                                showToolbarOverflowMenu = !showToolbarOverflowMenu
                            },
                            onCloseAction = {
                                showToolbarOverflowMenu = false
                                if (previewMode) {
                                    previewMode = false
                                }
                                if (isChatShareSelecting) {
                                    cancelChatShareSelection()
                                }
                            },
                            onUpdateSettings = { newSettings ->
                                vm.updateSettings(newSettings)
                            },
                            onSwitchAssistant = { assistant ->
                                navigateToAssistantConversation(assistant)
                            },
                            onToggleTemporaryChat = {
                                onManualTemporaryChatChange(!manualTemporaryChat)
                            },
                            onNewChat = onNewChat
                        )
                    }
                } else {
                    null
                }

                if (showModeComposer) {
                    ChatModeBottomControls(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .then(
                                if (inputMaxWidth != Dp.Unspecified) {
                                    Modifier.widthIn(max = inputMaxWidth)
                                } else {
                                    Modifier
                                }
                            )
                            .entrance(
                                key = "${conversation.id}:input",
                                motionPolicy = motionPolicy,
                                from = EntranceDirection.UP,
                                delayMs = startupWallpaperPauseMs + (2 * startupStaggerStepMs),
                                distance = 30.dp,
                            ),
                        showSearch = previewMode,
                        searchQuery = chatSearchQuery,
                        onSearchQueryChange = { chatSearchQuery = it },
                        showShareSelection = isChatShareSelecting,
                        selectedCount = selectedChatShareItems.size,
                        allSelected = selectedChatShareItems.isNotEmpty() &&
                            selectedChatShareItems.size == conversation.messageNodes.size,
                        onCancelShareSelection = { cancelChatShareSelection() },
                        onToggleSelectAll = {
                            selectedChatShareItems = if (selectedChatShareItems.isNotEmpty()) {
                                emptySet()
                            } else {
                                conversation.messageNodes.map { it.id }.toSet()
                            }
                        },
                        onConfirmShareSelection = { confirmChatShareSelection() },
                        bottomAccessory = bottomToolbarContent,
                        bottomPadding = if (toolbarPlacement == ChatToolbarPlacement.Bottom) 12.dp else 24.dp,
                    )
                } else {
                    CompositionLocalProvider(
                        LocalAppBackdrop provides overlayBackdrop
                    ) {
                    MinimalChatInput(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .then(
                            if (inputMaxWidth != Dp.Unspecified) {
                                Modifier.widthIn(max = inputMaxWidth)
                            } else {
                                Modifier
                            }
                        )
                        .entrance(
                            key = "${conversation.id}:input",
                            motionPolicy = motionPolicy,
                            from = EntranceDirection.UP,
                            delayMs = startupWallpaperPauseMs + (2 * startupStaggerStepMs),
                            distance = 30.dp,
                        ),
                    state = inputState,
                    settings = setting,
                    conversation = conversation,
                    mcpManager = vm.mcpManager,
                    onShowChatInfo = { showChatInfoSheet = true },
                    onCancelClick = {
                        vm.stopGeneration()
                        loadingJob?.cancel()
                        isAssistantTurnActive = false
                    },
                    enableSearch = enableWebSearch,
                    onToggleSearch = {
                        if (enableWebSearch) {
                            vm.updateAssistantSearchMode(me.lucid.domchat.data.model.AssistantSearchMode.Off)
                        } else {
                            if (settings.searchServices.isNotEmpty()) {
                                val validIndex = lastProviderIndex.coerceIn(0, settings.searchServices.lastIndex)
                                vm.updateAssistantSearchMode(me.lucid.domchat.data.model.AssistantSearchMode.Provider(validIndex))
                            }
                        }
                    },
                    onSendClick = {
                        val editingId = inputState.editingMessage
                        if (inputState.isEditing() && editingId != null) {
                            vm.handleMessageEdit(
                                parts = inputState.getContents(),
                                messageId = editingId,
                            )
                        } else {
                            if (loadingJob != null) return@MinimalChatInput
                            if (currentChatModel == null) {
                                toaster.show(modelRequiredMessage, type = ToastType.Error)
                                return@MinimalChatInput
                            }
                            vm.handleMessageSend(
                                inputState.getContents(),
                                persistenceMode = activePersistenceMode
                            )
                        }
                        inputState.clearInput()
                    },
                    onLongSendClick = {
                        val editingId = inputState.editingMessage
                        if (inputState.isEditing() && editingId != null) {
                            vm.handleMessageEdit(
                                parts = inputState.getContents(),
                                messageId = editingId,
                            )
                        } else {
                            if (loadingJob != null) return@MinimalChatInput
                            if (currentChatModel == null) {
                                toaster.show(modelRequiredMessage, type = ToastType.Error)
                                return@MinimalChatInput
                            }
                            vm.handleMessageSend(
                                content = inputState.getContents(),
                                answer = false,
                                persistenceMode = activePersistenceMode
                            )
                        }
                        inputState.clearInput()
                    },
                    onUpdateChatModel = {
                        vm.setChatModel(it)
                    },
                    onUpdateAssistant = {
                        vm.updateConversationAssistant(it)
                    },
                    onUpdateSearchService = { index ->
                        vm.updateAssistantSearchMode(me.lucid.domchat.data.model.AssistantSearchMode.Provider(index))
                    },
                    onClearContext = {
                        vm.handleMessageTruncate()
                    },
                    onUpdateConversation = { updatedConversation ->
                        vm.updateConversation(updatedConversation)
                    },
                    onToolApproval = { toolCallId, approved, reason, answer ->
                        vm.handleToolApproval(
                            toolCallId = toolCallId,
                            approved = approved,
                            reason = reason,
                            answer = answer,
                        )
                    },
                    onNavigateToLorebook = { lorebookId ->
                        navController.navigate(Screen.SettingLorebookDetail(lorebookId))
                    },
                    onRefreshContext = { vm.refreshContext() },
                    onDeleteFile = { vm.deleteFile(it) },
                    bottomAccessory = bottomToolbarContent,
                    showScrollToBottomButton = showScrollToBottomButton,
                    onScrollToBottomClick = {
                        scope.launch {
                            val targetIndex = (chatListState.layoutInfo.totalItemsCount - 1)
                                .coerceAtLeast(0)
                            chatListState.animateScrollToItem(targetIndex)
                        }
                    },
                    bottomPadding = if (toolbarPlacement == ChatToolbarPlacement.Bottom) 12.dp else 24.dp,
                )
                }

                }
            }
            }

            // Popups are siblings placed after Scaffold so its top fade can never draw over them.
            androidx.compose.animation.AnimatedVisibility(
                visible = showContextUsagePopup && contextMeterUsage != null,
                enter = if (reduceMotion) {
                    fadeIn(tween(90))
                } else fadeIn(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.75f,
                        stiffness = 360f,
                    )
                ),
                exit = if (reduceMotion) {
                    fadeOut(tween(80))
                } else fadeOut(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.85f,
                        stiffness = 420f,
                    )
                ),
                modifier = Modifier.fillMaxSize(),
            ) {
                val popupScale by transition.animateFloat(
                    transitionSpec = {
                        if (targetState == EnterExitState.Visible) {
                            androidx.compose.animation.core.spring(
                                dampingRatio = 0.75f,
                                stiffness = 360f,
                            )
                        } else {
                            androidx.compose.animation.core.spring(
                                dampingRatio = 0.85f,
                                stiffness = 420f,
                            )
                        }
                    },
                    label = "chat_context_usage_popup_scale",
                ) { state ->
                    if (reduceMotion || state == EnterExitState.Visible) 1f else 0.96f
                }
                contextMeterUsage?.let { usage ->
                    CompositionLocalProvider(LocalAppBackdrop provides overlayBackdrop) {
                        ContextUsageOverlay(
                            usage = usage,
                            activity = contextManagementActivity,
                            placement = toolbarPlacement,
                            popupScale = popupScale,
                            model = currentChatModel,
                            onUpdateModelLimit = { newLimit ->
                                currentChatModel?.let { model ->
                                    vm.updateModelContextLimit(model.id, newLimit)
                                }
                            },
                            onDismissRequest = { showContextUsagePopup = false },
                        )
                    }
                }
            }

            if (showChatInfoSheet) {
                CompositionLocalProvider(LocalAppBackdrop provides overlayBackdrop) {
                    ChatInfoBottomSheet(
                        settings = setting,
                        conversation = conversation,
                        assistant = currentAssistant,
                        model = currentChatModel,
                        onDismissRequest = { showChatInfoSheet = false }
                    )
                }
            }

            activeChatMessageAction?.let { action ->
                CompositionLocalProvider(LocalAppBackdrop provides overlayBackdrop) {
                    ChatMessageActionsSheet(
                        message = action.message,
                        model = action.model,
                        onEdit = {
                            activeChatMessageAction = null
                            action.onEdit()
                        },
                        onDelete = {
                            activeChatMessageAction = null
                            action.onDelete()
                        },
                        onFork = {
                            activeChatMessageAction = null
                            action.onFork()
                        },
                        onSelectAndCopy = {
                            val msg = action.message
                            activeChatMessageAction = null
                            activeCopyMessage = msg
                        },
                        onWebViewPreview = {
                            activeChatMessageAction = null
                            action.onWebViewPreview()
                        },
                        onDismissRequest = { activeChatMessageAction = null }
                    )
                }
            }

            activeCopyMessage?.let { copyMsg ->
                CompositionLocalProvider(LocalAppBackdrop provides overlayBackdrop) {
                    ChatMessageCopySheet(
                        message = copyMsg,
                        onDismissRequest = { activeCopyMessage = null }
                    )
                }
            }


            // Custom initial message creator dialog (swipe-reroll on intro message)
            if (showCustomGreetingDialog) {
                CompositionLocalProvider(LocalAppBackdrop provides overlayBackdrop) {
                    CustomGreetingDialog(
                        visible = true,
                        onDismissRequest = { showCustomGreetingDialog = false },
                        onSave = { newGreeting ->
                            val trimmed = newGreeting.trim()
                            if (trimmed.isNotBlank()) {
                                val updatedAssistant = currentAssistant.copy(
                                    alternateGreetings = currentAssistant.alternateGreetings + trimmed,
                                )
                                vm.updateConversationAssistant(updatedAssistant)
                                val introNode = conversation.messageNodes.firstOrNull { node ->
                                    node.currentMessage.role == MessageRole.ASSISTANT &&
                                        isCharacterIntroMessage(
                                            messages = conversation.messageNodes.map { it.currentMessage },
                                            message = node.currentMessage,
                                        )
                                }
                                introNode?.let { node ->
                                    val newMessage = UIMessage(
                                        role = MessageRole.ASSISTANT,
                                        parts = listOf(UIMessagePart.Text(text = trimmed)),
                                    )
                                    val updatedNode = node.copy(
                                        messages = node.messages + newMessage,
                                        selectIndex = node.messages.size,
                                    )
                                    val updatedConversation = conversation.copy(
                                        messageNodes = conversation.messageNodes.map { n ->
                                            if (n.id == updatedNode.id) updatedNode else n
                                        },
                                    )
                                    vm.updateConversation(updatedConversation)
                                }
                            }
                            showCustomGreetingDialog = false
                        },
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showToolbarOverflowMenu,
                enter = fadeIn(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.75f,
                        stiffness = 360f,
                    )
                ),
                exit = fadeOut(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.85f,
                        stiffness = 420f,
                    )
                ),
                modifier = Modifier.fillMaxSize(),
            ) {
                val menuScale by transition.animateFloat(
                    transitionSpec = {
                        if (targetState == EnterExitState.Visible) {
                            androidx.compose.animation.core.spring(
                                dampingRatio = 0.75f,
                                stiffness = 360f,
                            )
                        } else {
                            androidx.compose.animation.core.spring(
                                dampingRatio = 0.85f,
                                stiffness = 420f,
                            )
                        }
                    },
                    label = "chat_toolbar_overflow_menu_scale",
                ) { state ->
                    if (state == EnterExitState.Visible) 1f else 0.96f
                }
                CompositionLocalProvider(LocalAppBackdrop provides overlayBackdrop) {
                    ChatToolbarOverflowMenu(
                        placement = toolbarPlacement,
                        menuScale = menuScale,
                        previewMode = previewMode,
                        hasConversationContent = conversation.messageNodes.isNotEmpty(),
                        chatListState = chatListState,
                        onDismissRequest = { showToolbarOverflowMenu = false },
                        onCreatorNotesClick = {
                            showToolbarOverflowMenu = false
                            showCreatorNotesSheet = true
                        },
                        onPromptPresetsClick = {
                            showToolbarOverflowMenu = false
                            navController.navigate(Screen.SettingPresets)
                        },
                        onAuthorsNotesClick = {
                            showToolbarOverflowMenu = false
                            navController.navigate(Screen.AssistantDetail(currentAssistant.id.toString(), startRoute = "personality"))
                        },
                        onWallpaperClick = {
                            showToolbarOverflowMenu = false
                            navController.navigate(Screen.SettingUICustomization)
                        },
                        onTextStylingClick = {
                            showToolbarOverflowMenu = false
                            navController.navigate(Screen.SettingFonts)
                        },
                    )
                }
            }

            if (showCreatorNotesSheet) {
                CharacterNotesBottomSheet(
                    assistant = currentAssistant,
                    onDismissRequest = { showCreatorNotesSheet = false }
                )
            }

            CompositionLocalProvider(LocalAppBackdrop provides overlayBackdrop) {
                TTSController()
            }
        }
        }
    }

}

}

@Composable
private fun CustomGreetingDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit,
) {
    if (!visible) return
    val haptics = rememberPremiumHaptics()
    var greetingText by remember(visible) { mutableStateOf("") }
    val outerBackdrop = LocalAppBackdrop.current
    val settings = LocalSettings.current
    val isFrosted = settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED
    val useGlass = outerBackdrop != null && me.lucid.domchat.ui.component.backdrop.isRuntimeShaderSupported()

    val dialogShape = RoundedCornerShape(20.dp)
    val inputShape = RoundedCornerShape(14.dp)
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
                glassMode = me.lucid.domchat.data.model.LiquidGlassMode.BALANCED_BLEND,
            )
        }
    } else {
        Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, dialogShape)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .widthIn(min = 280.dp, max = 400.dp)
                    .then(surfaceModifier)
                    .clip(dialogShape)
                    .background(if (useGlass) Color.Transparent else Color(0xFF000000)),
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.chat_custom_greeting_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    OutlinedTextField(
                        value = greetingText,
                        onValueChange = { greetingText = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.chat_custom_greeting_placeholder),
                                color = Color.White.copy(alpha = 0.5f),
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .liquidGlassSurface(
                                shape = inputShape,
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                fillColor = Color.Transparent,
                            ),
                        maxLines = Int.MAX_VALUE,
                        minLines = 6,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White,
                        ),
                        shape = inputShape,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DomChatGlassPillButton(
                            text = stringResource(R.string.cancel),
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                onDismissRequest()
                            },
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        DomChatGlassPillButton(
                            text = stringResource(R.string.save),
                            accent = true,
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                onSave(greetingText)
                            },
                            enabled = greetingText.trim().isNotBlank(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    enabled: Boolean = true,
    onClick: () -> Unit,
    isInsidePill: Boolean = false,
) {
    val haptics = rememberPremiumHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "chat_toolbar_icon_scale"
    )
    val isDark = LocalDarkMode.current
    val settings = LocalSettings.current
    val iconColor = if (isDark) Color.White else Color.Black

    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (!isInsidePill) {
                    Modifier.liquidGlassSurface(shape = CircleShape, borderWidth = 0.5.dp, isFloatingControl = true)
                } else Modifier
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = iconColor)
    }
}

@Composable
private fun ChatModeBottomControls(
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showShareSelection: Boolean,
    selectedCount: Int,
    allSelected: Boolean,
    onCancelShareSelection: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onConfirmShareSelection: () -> Unit,
    bottomAccessory: (@Composable () -> Unit)?,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = bottomPadding, start = 16.dp, end = 16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            AnimatedContent(
                targetState = when {
                    showSearch -> "search"
                    showShareSelection -> "share"
                    else -> "none"
                },
                transitionSpec = {
                    (fadeIn(animationSpec = tween(120)) + scaleIn(
                        initialScale = 0.98f,
                        animationSpec = tween(120)
                    )) togetherWith (fadeOut(animationSpec = tween(90)) + scaleOut(
                        targetScale = 0.98f,
                        animationSpec = tween(90)
                    ))
                },
                label = "chat_mode_composer"
            ) { mode ->
                when (mode) {
                    "search" -> ChatSearchModeBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                    )
                    "share" -> ChatShareSelectionModeBar(
                        selectedCount = selectedCount,
                        allSelected = allSelected,
                        onCancel = onCancelShareSelection,
                        onToggleSelectAll = onToggleSelectAll,
                        onConfirm = onConfirmShareSelection,
                    )
                    else -> Spacer(Modifier.height(56.dp))
                }
            }

            bottomAccessory?.invoke()
        }
    }
}

@Composable
private fun ChatSearchModeBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val searchFieldShape = me.lucid.domchat.ui.theme.AppShapes.SearchField
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val settings = LocalSettings.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppSize.ChromeBar)
            .then(
                if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                    Modifier.frostedGlassSurface(shape = searchFieldShape)
                } else {
                    Modifier.liquidGlassSurface(
                        shape = searchFieldShape,
                        glassMode = settings.displaySetting.liquidGlassMode,
                    )
                }
            ),
        shape = searchFieldShape,
        color = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) blurredContainerColor(containerColor) else Color.Transparent,
        border = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) domChatSoftEdgeBorder() else null,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search messages") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear_search),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = searchFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun ChatShareSelectionModeBar(
    selectedCount: Int,
    allSelected: Boolean,
    onCancel: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onConfirm: () -> Unit,
) {
    val haptics = rememberPremiumHaptics()
    val shape = AppShapes.ButtonPill
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val settings = LocalSettings.current
    Surface(
        shape = shape,
        color = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) blurredContainerColor(containerColor) else Color.Transparent,
        border = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) domChatSoftEdgeBorder() else null,
        modifier = Modifier
            .fillMaxWidth()
            .height(AppSize.ChromeBar)
            .then(
                if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                    Modifier.frostedGlassSurface(shape = shape)
                } else {
                    Modifier.liquidGlassSurface(shape = shape, glassMode = settings.displaySetting.liquidGlassMode)
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            Tooltip(tooltip = { Text(stringResource(R.string.chat_clear_selection)) }) {
                IconButton(
                    modifier = Modifier.size(AppSize.ChromePill),
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        onCancel()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Text(
                text = selectedCount.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.weight(1f))

            Tooltip(tooltip = { Text(stringResource(R.string.select_all)) }) {
                IconButton(
                    modifier = Modifier.size(AppSize.ChromePill),
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        onToggleSelectAll()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SelectAll,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (allSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Tooltip(tooltip = { Text(stringResource(R.string.confirm)) }) {
                FilledIconButton(
                    modifier = Modifier.size(AppSize.ChromePill),
                    enabled = selectedCount > 0,
                    onClick = {
                        haptics.perform(HapticPattern.Success)
                        onConfirm()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatToolbarOverflowMenu(
    placement: ChatToolbarPlacement,
    menuScale: Float,
    previewMode: Boolean,
    hasConversationContent: Boolean,
    chatListState: LazyListState,
    onDismissRequest: () -> Unit,
    onCreatorNotesClick: () -> Unit,
    onPromptPresetsClick: () -> Unit,
    onAuthorsNotesClick: () -> Unit,
    onWallpaperClick: () -> Unit,
    onTextStylingClick: () -> Unit,
) {
    BackHandler(onBack = onDismissRequest)

    val scrimInteractionSource = remember { MutableInteractionSource() }
    val menuShape = AppShapes.CardMedium
    val containerColor = domChatSheetContainerColor()
    val border = domChatSoftEdgeBorder()
    val settings = LocalSettings.current
    val menuTopPadding = chatToolbarPopupTopPadding(placement)
    val menuBottomPadding = chatToolbarPopupBottomPadding(placement)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.16f))
            .clickable(
                interactionSource = scrimInteractionSource,
                indication = null,
                onClick = onDismissRequest
            )
    ) {
        Surface(
            shape = menuShape,
            color = Color.Transparent,
            border = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) border else null,
            modifier = Modifier
                .align(
                    if (placement == ChatToolbarPlacement.Top) {
                        Alignment.TopEnd
                    } else {
                        Alignment.BottomEnd
                    }
                )
                .then(
                    if (placement == ChatToolbarPlacement.Top) {
                        Modifier.statusBarsPadding()
                    } else {
                        Modifier.navigationBarsPadding()
                    }
                )
                .padding(top = menuTopPadding, bottom = menuBottomPadding, end = 16.dp)
                .widthIn(min = 196.dp, max = 260.dp)
                .graphicsLayer {
                    scaleX = 1f
                    scaleY = menuScale
                    transformOrigin = chatToolbarPopupTransformOrigin(placement)
                }
                .then(
                    if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                        Modifier.frostedGlassSurface(shape = menuShape)
                    } else {
                        Modifier.liquidGlassSurface(shape = menuShape, glassMode = settings.displaySetting.liquidGlassMode)
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp)
            ) {
                ChatToolbarOverflowMenuItem(
                    label = "Creator Notes",
                    icon = Icons.Rounded.AutoAwesome,
                    onClick = onCreatorNotesClick
                )
                ChatToolbarOverflowMenuItem(
                    label = "Prompt Presets",
                    icon = Icons.Rounded.Tune,
                    onClick = onPromptPresetsClick
                )
                ChatToolbarOverflowMenuItem(
                    label = "Author's Notes",
                    icon = Icons.Rounded.Edit,
                    onClick = onAuthorsNotesClick
                )
                ChatToolbarOverflowMenuItem(
                    label = "Wallpaper",
                    icon = Icons.Rounded.Image,
                    onClick = onWallpaperClick
                )
                ChatToolbarOverflowMenuItem(
                    label = "Text Styling",
                    icon = Icons.Rounded.FormatItalic,
                    onClick = onTextStylingClick
                )
            }
        }
    }
}

@Composable
private fun ChatToolbarOverflowMenuItem(
    label: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val haptics = rememberPremiumHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "chat_toolbar_menu_item_scale"
    )
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    onClick()
                }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = contentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private enum class TopBarActionMode {
    CompactNewChat,
    NewChat,
    TemporaryNewChat,
    InChat,
}

private data class TopBarActionState(
    val mode: TopBarActionMode,
    val assistantId: Uuid?,
    val showCloseAction: Boolean,
)

private val DefaultTopBarActionState = TopBarActionState(
    mode = TopBarActionMode.NewChat,
    assistantId = null,
    showCloseAction = false,
)

@Composable
private fun ChatToolbarActionPill(
    actionState: TopBarActionState,
    currentAssistant: Assistant,
    conversationTitle: String = "",
    topPillSize: Dp,
    buttonShape: RoundedCornerShape,
    containerColor: Color,
    border: BorderStroke,
    onNewChat: () -> Unit,
    onOpenOverflowMenu: () -> Unit,
    onCloseAction: () -> Unit,
    onToggleTemporaryChat: () -> Unit,
    onOpenAssistantPicker: () -> Unit,
) {
    val chatTitle = conversationTitle.takeIf { it.isNotBlank() } ?: currentAssistant.effectiveChatName
    val fullPillWidth = topPillSize * 2f
    val targetWidth = when (actionState.mode) {
        TopBarActionMode.CompactNewChat -> topPillSize
        TopBarActionMode.NewChat, TopBarActionMode.TemporaryNewChat -> fullPillWidth
        TopBarActionMode.InChat -> if (actionState.showCloseAction) fullPillWidth else topPillSize
    }
    val pillWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = targetWidth,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "top_pill_width"
    )
    val compactAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (actionState.mode == TopBarActionMode.CompactNewChat) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "top_pill_compact_alpha"
    )
    val newChatAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (actionState.mode == TopBarActionMode.NewChat) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "top_pill_new_chat_alpha"
    )
    val temporaryAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (actionState.mode == TopBarActionMode.TemporaryNewChat) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "top_pill_temporary_alpha"
    )
    val inChatAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (actionState.mode == TopBarActionMode.InChat) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "top_pill_in_chat_alpha"
    )

    Surface(
        shape = buttonShape,
        color = Color.Transparent,
        border = null,
        modifier = Modifier
            .size(width = pillWidth, height = topPillSize)
            .clip(buttonShape)
            .then(
                Modifier.liquidGlassSurface(shape = buttonShape, fillColor = Color.Transparent, isFloatingControl = true)
            )
    ) {
        Box(Modifier.fillMaxSize()) {
            if (compactAlpha > 0f) {
                ChatToolbarCompactLayer(
                    alpha = compactAlpha,
                    enabled = actionState.mode == TopBarActionMode.CompactNewChat,
                    topPillSize = topPillSize,
                    onToggleTemporaryChat = onToggleTemporaryChat
                )
            }
            if (newChatAlpha > 0f) {
                ChatToolbarNewChatLayer(
                    alpha = newChatAlpha,
                    enabled = actionState.mode == TopBarActionMode.NewChat,
                    topPillSize = topPillSize,
                    fullPillWidth = fullPillWidth,
                    currentAssistant = currentAssistant,
                    temporary = false,
                    onToggleTemporaryChat = onToggleTemporaryChat,
                    onOpenAssistantPicker = onOpenAssistantPicker
                )
            }
            if (temporaryAlpha > 0f) {
                ChatToolbarNewChatLayer(
                    alpha = temporaryAlpha,
                    enabled = actionState.mode == TopBarActionMode.TemporaryNewChat,
                    topPillSize = topPillSize,
                    fullPillWidth = fullPillWidth,
                    currentAssistant = currentAssistant,
                    temporary = true,
                    onToggleTemporaryChat = onToggleTemporaryChat,
                    onOpenAssistantPicker = onOpenAssistantPicker
                )
            }
            if (inChatAlpha > 0f) {
                ChatToolbarInChatLayer(
                    alpha = inChatAlpha,
                    enabled = actionState.mode == TopBarActionMode.InChat,
                    topPillSize = topPillSize,
                    fullPillWidth = fullPillWidth,
                    showCloseAction = actionState.showCloseAction,
                    onNewChat = onNewChat,
                    onOpenOverflowMenu = onOpenOverflowMenu,
                    onCloseAction = onCloseAction
                )
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.ChatToolbarCompactLayer(
    alpha: Float,
    enabled: Boolean,
    topPillSize: Dp,
    onToggleTemporaryChat: () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .size(topPillSize)
            .graphicsLayer {
                this.alpha = alpha
                val layerScale = 0.92f + alpha * 0.08f
                scaleX = layerScale
                scaleY = layerScale
            },
        contentAlignment = Alignment.Center
    ) {
        ChatToolbarIconButton(
            icon = Icons.Rounded.HistoryToggleOff,
            contentDescription = "Temporary Chat",
            size = topPillSize,
            enabled = enabled,
            onClick = onToggleTemporaryChat,
            isInsidePill = true,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.ChatToolbarNewChatLayer(
    alpha: Float,
    enabled: Boolean,
    topPillSize: Dp,
    fullPillWidth: Dp,
    currentAssistant: Assistant,
    temporary: Boolean,
    onToggleTemporaryChat: () -> Unit,
    onOpenAssistantPicker: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .size(width = fullPillWidth, height = topPillSize)
            .graphicsLayer {
                this.alpha = alpha
                val layerScale = 0.92f + alpha * 0.08f
                scaleX = layerScale
                scaleY = layerScale
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChatToolbarIconButton(
            icon = if (temporary) Icons.Rounded.History else Icons.Rounded.HistoryToggleOff,
            contentDescription = if (temporary) "Make Normal Chat" else "Temporary Chat",
            size = topPillSize,
            enabled = enabled,
            onClick = onToggleTemporaryChat,
            isInsidePill = true,
        )
        val haptics = me.lucid.domchat.ui.hooks.rememberPremiumHaptics()
        Box(
            modifier = Modifier
                .size(topPillSize)
                .clip(CircleShape)
                .clickable(
                    enabled = enabled,
                    onClick = {
                        haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
                        onOpenAssistantPicker()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            me.lucid.domchat.ui.components.ui.UIAvatar(
                name = currentAssistant.effectiveChatName,
                value = currentAssistant.avatar,
                modifier = Modifier.size(30.dp),
                onClick = null
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.ChatToolbarInChatLayer(
    alpha: Float,
    enabled: Boolean,
    topPillSize: Dp,
    fullPillWidth: Dp,
    showCloseAction: Boolean,
    onNewChat: () -> Unit,
    onOpenOverflowMenu: () -> Unit,
    onCloseAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .size(width = if (showCloseAction) fullPillWidth else topPillSize, height = topPillSize)
            .graphicsLayer {
                this.alpha = alpha
                val layerScale = 0.92f + alpha * 0.08f
                scaleX = layerScale
                scaleY = layerScale
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChatToolbarIconButton(
            icon = Icons.Rounded.Add,
            contentDescription = "New Message",
            size = topPillSize,
            enabled = enabled,
            onClick = onNewChat,
            isInsidePill = true,
        )
        if (showCloseAction) {
            ChatToolbarIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.banner_dismiss),
                size = topPillSize,
                enabled = enabled,
                onClick = onCloseAction,
                isInsidePill = true,
            )
        }
    }
}


@Composable
fun UpdatePill(
    bigScreen: Boolean,
    height: Dp,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val haptics = me.lucid.domchat.ui.hooks.rememberPremiumHaptics()
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    val pillShape = RoundedCornerShape(999.dp)
    val settings = LocalSettings.current

    Surface(
        onClick = {
            haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
            onClick()
        },
        shape = pillShape,
        color = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) blurredContainerColor(containerColor) else Color.Transparent,
        contentColor = contentColor,
        border = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) domChatSoftEdgeBorder() else null,
        modifier = Modifier
            .height(height)
            .then(
                if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                    Modifier.frostedGlassSurface(shape = pillShape)
                } else {
                    Modifier.liquidGlassSurface(shape = pillShape, glassMode = settings.displaySetting.liquidGlassMode)
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (bigScreen) stringResource(R.string.update_available) else "New Update",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            
            Spacer(Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
                    onDismiss()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
        }
    }
}

@Composable
private fun ChatToolbar(
    placement: ChatToolbarPlacement,
    settings: Settings,
    currentAssistant: Assistant,
    conversationInitialized: Boolean,
    conversation: Conversation,
    drawerState: DrawerState,
    bigScreen: Boolean,
    previewMode: Boolean,
    isTemporaryChat: Boolean,
    currentChatModel: Model? = null,
    isGenerating: Boolean = false,
    showCloseAction: Boolean,
    showTopFade: Boolean = true,
    vm: ChatVM,
    contextUsage: ContextUsageBreakdown?,
    onContextMeterClick: () -> Unit,
    onNewChat: () -> Unit,
    onOpenOverflowMenu: () -> Unit,
    onCloseAction: () -> Unit,
    onUpdateSettings: (Settings) -> Unit,
    onSwitchAssistant: (Assistant) -> Unit,
    onToggleTemporaryChat: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val isDark = LocalDarkMode.current
    val topContainerColor = Color(0xFF000000).copy(alpha = settings.displaySetting.glassAlpha.coerceIn(0.10f, 0.75f))
    val topContainerBorder = BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
    val buttonShape = AppShapes.ButtonPill
    val topPillSize = AppSize.ChromePill
    // State for assistant picker - must be at function level for proper recomposition
    var showAssistantPicker by remember { mutableStateOf(false) }
    var showCharacterNotesSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCreatorNotesSheet by remember { mutableStateOf(false) }
    val toaster = LocalToaster.current
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val isEmpty = !conversation.messageNodes.any { it.role == me.lucid.ai.core.MessageRole.USER }
    val rawActionMode = run {
        val hasPresetMessages = currentAssistant.presetMessages.isNotEmpty() || currentAssistant.alternateGreetings.isNotEmpty()
        val effectiveDisplay = settings.getEffectiveDisplaySetting(currentAssistant)
        val headerShowsAvatar = effectiveDisplay.newChatShowAvatar && (
            effectiveDisplay.newChatHeaderStyle == me.lucid.domchat.data.datastore.NewChatHeaderStyle.BIG_ICON ||
                effectiveDisplay.newChatHeaderStyle == me.lucid.domchat.data.datastore.NewChatHeaderStyle.GREETING
            )
        val shouldUseCompactTemporaryToggle = !hasPresetMessages && headerShowsAvatar
        when {
            isEmpty && !isTemporaryChat && shouldUseCompactTemporaryToggle -> TopBarActionMode.CompactNewChat
            isEmpty && !isTemporaryChat -> TopBarActionMode.NewChat
            isEmpty && isTemporaryChat -> TopBarActionMode.TemporaryNewChat
            else -> TopBarActionMode.InChat
        }
    }
    val rawActionState = TopBarActionState(
        mode = rawActionMode,
        assistantId = currentAssistant.id.takeUnless { rawActionMode == TopBarActionMode.InChat },
        showCloseAction = showCloseAction && rawActionMode == TopBarActionMode.InChat,
    )
    var displayedActionState by remember {
        mutableStateOf(DefaultTopBarActionState)
    }

    LaunchedEffect(rawActionState, conversationInitialized) {
        if (conversationInitialized) {
            if (displayedActionState != rawActionState) {
                withFrameNanos { }
            }
            displayedActionState = rawActionState
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (placement == ChatToolbarPlacement.Top && showTopFade) {
            ChatTopFadeOverlay(
                fadeHeight = chatTopToolbarFadeHeight,
                settings = settings,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        Row(
            modifier = Modifier
                .then(
                    if (placement == ChatToolbarPlacement.Top) {
                        Modifier.statusBarsPadding().offset(y = (-11).dp)
                    } else {
                        Modifier
                    }
                )
                .fillMaxWidth()
                .padding(
                    vertical = if (placement == ChatToolbarPlacement.Top) 4.dp else 0.dp,
                    horizontal = if (placement == ChatToolbarPlacement.Top) 16.dp else 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!bigScreen) {
                DomChatMenuButton(
                    onClick = {
                        scope.launch { drawerState.open() }
                    },
                    contentDescription = "Messages",
                    shape = buttonShape,
                    containerColor = topContainerColor,
                    border = topContainerBorder,
                    modifier = Modifier
                        .size(topPillSize)
                        .zIndex(2f)
                        .liquidGlassSurface(shape = CircleShape, borderWidth = 0.5.dp, isFloatingControl = true),
                )
            }


            // Centered Character Name with click for Creator Notes - Glassy pill tag
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    enabled = !currentAssistant.isDeleted,
                    onClick = {
                        if (!currentAssistant.isDeleted) {
                            haptics.perform(HapticPattern.Pop)
                            showCharacterNotesSheet = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = null,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = conversation.title.takeIf { it.isNotBlank() } ?: currentAssistant.effectiveChatName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            val isForcedCheck by vm.isForcedCheck.collectAsStateWithLifecycle()
            val context = LocalContext.current
            var showUpdateDialog by remember { mutableStateOf(false) }
            val currentVersion = remember { me.lucid.domchat.utils.Version(BuildConfig.VERSION_NAME) }
            val isNewChat = isEmpty
            val shouldObserveUpdates = (settings.displaySetting.checkForUpdates || isForcedCheck) &&
                isNewChat

            // Always collect update state so AnimatedVisibility can fade out gracefully
            // even when shouldObserveUpdates becomes false mid-session.
            val updateState by vm.updateState.collectAsStateWithLifecycle()
            @Suppress("UNCHECKED_CAST")
            val updateInfo = ((updateState as? me.lucid.domchat.utils.UiState.Success<*>)?.data as? me.lucid.domchat.utils.UpdateInfo)
            val latestVersion = remember(updateInfo) {
                updateInfo?.let { me.lucid.domchat.utils.Version(it.version) }
            }
            val isNewer = latestVersion != null && latestVersion > currentVersion
            val isIgnored = remember(updateInfo, isForcedCheck) {
                if (updateInfo != null)
                    vm.updateChecker.isUpdateIgnored(context, updateInfo.version, forceCheck = isForcedCheck)
                else true
            }
            val isDismissed = remember(updateInfo, isForcedCheck) {
                if (updateInfo != null)
                    vm.updateChecker.isUpdateDismissed(context, updateInfo.version, forceCheck = isForcedCheck)
                else true
            }

            LaunchedEffect(isNewer, isDismissed, isIgnored, shouldObserveUpdates) {
                if (isNewer && !isDismissed && !isIgnored && shouldObserveUpdates) {
                    showUpdateDialog = true
                }
            }

            // Top bar update badge removed per user request
            val showUpdatePill = false

            if (showUpdateDialog && updateInfo != null) {
                UpdateDialog(
                    info = updateInfo,
                    updateChecker = vm.updateChecker,
                    onDismiss = {
                        vm.updateChecker.dismissVersion(context, updateInfo.version)
                        vm.updateChecker.clearForcedCheck()
                        showUpdateDialog = false
                    },
                    onLater = {
                        vm.updateChecker.dismissVersion(context, updateInfo.version)
                        vm.updateChecker.clearForcedCheck()
                        showUpdateDialog = false
                    },
                    onIgnore = {
                        vm.updateChecker.ignoreUpdate(context, updateInfo.version)
                        vm.updateChecker.clearForcedCheck()
                        showUpdateDialog = false
                    }
                )
            }

            ChatToolbarActionPill(
                actionState = displayedActionState,
                currentAssistant = currentAssistant,
                topPillSize = topPillSize,
                buttonShape = buttonShape,
                containerColor = topContainerColor,
                border = topContainerBorder,
                onNewChat = onNewChat,
                onOpenOverflowMenu = onOpenOverflowMenu,
                onCloseAction = onCloseAction,
                onToggleTemporaryChat = onToggleTemporaryChat,
                onOpenAssistantPicker = { showAssistantPicker = true }
            )
        }
    }
    
    // Assistant picker sheet - outside TopAppBar for proper state handling
    if (showAssistantPicker) {
        me.lucid.domchat.ui.components.ai.AssistantPickerSheet(
            settings = settings,
            currentAssistant = currentAssistant,
            onAssistantSelected = { selectedAssistant ->
                onUpdateSettings(settings.copy(assistantId = selectedAssistant.id))
            },
            onNavigate = { selectedAssistant ->
                showAssistantPicker = false
                onSwitchAssistant(selectedAssistant)
            },
            onDismiss = { showAssistantPicker = false }
        )
    }

    if (showCharacterNotesSheet) {
        val navController = LocalNavController.current
        me.lucid.domchat.ui.pages.assistant.CharacterInspectorSheet(
            assistant = currentAssistant,
            settings = settings,
            isDark = LocalDarkMode.current,
            ttsProviders = settings.ttsProviders,
            onDismiss = { showCharacterNotesSheet = false },
            onStartChat = { showCharacterNotesSheet = false },
            onEdit = {
                showCharacterNotesSheet = false
                navController.navigate(Screen.AssistantDetail(currentAssistant.id.toString()))
            },
            onViewCreatorNotes = {
                showCharacterNotesSheet = false
                showCreatorNotesSheet = true
            },
            onDuplicate = {
                val cloned = currentAssistant.copy(
                    id = kotlin.uuid.Uuid.random(),
                    name = "${currentAssistant.name} (Copy)"
                )
                vm.addAssistant(cloned)
                showCharacterNotesSheet = false
                toaster?.show("Cloned ${currentAssistant.name}")
            },
            onExport = {
                showCharacterNotesSheet = false
                showExportDialog = true
            },
            onDelete = {
                showCharacterNotesSheet = false
                showDeleteConfirm = true
            },
            onUpdateLorebookIds = { newIds ->
                val updated = currentAssistant.copy(enabledLorebookIds = newIds)
                vm.updateConversationAssistant(updated)
            }
        )
    }

    if (showCreatorNotesSheet) {
        CharacterNotesBottomSheet(
            assistant = currentAssistant,
            onDismissRequest = { showCreatorNotesSheet = false }
        )
    }

    if (showExportDialog) {
        CharacterExportDialog(
            assistant = currentAssistant,
            onDismissRequest = { showExportDialog = false }
        )
    }

    if (showDeleteConfirm) {
        SettingsDestructiveConfirmDialog(
            visible = true,
            title = "Delete ${currentAssistant.name}?",
            message = "This will permanently remove this character from your library. Associated chat sessions will be preserved.",
            onConfirm = {
                val toDelete = currentAssistant
                showDeleteConfirm = false
                vm.removeAssistant(toDelete)
                toaster?.show(
                    message = "Deleted ${toDelete.name}",
                    action = me.lucid.domchat.ui.components.ui.ToastAction(
                        label = "Undo",
                        onClick = { vm.undoRemoveAssistant(toDelete) }
                    )
                )
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}


@Composable
private fun ContextUsageOverlay(
    usage: ContextUsageBreakdown,
    activity: ContextManagementActivity?,
    placement: ChatToolbarPlacement,
    popupScale: Float,
    model: Model? = null,
    onUpdateModelLimit: ((Int?) -> Unit)? = null,
    onDismissRequest: () -> Unit,
) {
    BackHandler(onBack = onDismissRequest)
    val settings = LocalSettings.current
    val interactionSource = remember { MutableInteractionSource() }
    val shape = AppShapes.CardMedium
    val sheetColor = domChatSheetContainerColor()
    val isDark = LocalDarkMode.current
    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.22f else 0.12f),
            Color.White.copy(alpha = if (isDark) 0.05f else 0.03f),
            Color.Transparent
        )
    )
    val border = androidx.compose.foundation.BorderStroke(0.5.dp, borderBrush)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onDismissRequest,
            )
    ) {
        Surface(
            shape = shape,
            color = Color.Transparent,
            border = border,
            modifier = Modifier
                .align(
                    if (placement == ChatToolbarPlacement.Top) Alignment.TopCenter
                    else Alignment.BottomCenter
                )
                .then(
                    if (placement == ChatToolbarPlacement.Top) Modifier.statusBarsPadding()
                    else Modifier.navigationBarsPadding()
                )
                .padding(
                    top = chatToolbarPopupTopPadding(placement),
                    bottom = chatToolbarPopupBottomPadding(placement),
                    start = 16.dp,
                    end = 16.dp,
                )
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = 1f
                    scaleY = popupScale
                    transformOrigin = chatToolbarPopupTransformOrigin(placement)
                }
                .then(
                    if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                        Modifier.frostedGlassSurface(shape = shape)
                    } else {
                        Modifier.liquidGlassSurface(shape = shape, glassMode = settings.displaySetting.liquidGlassMode)
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
        ) {
            ContextUsagePopupContent(
                usage = usage,
                activity = activity,
                model = model,
                onUpdateModelLimit = onUpdateModelLimit,
            )
        }
    }
}

@Composable
private fun ContextMeterAnchor(
    usage: ContextUsageBreakdown?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionPolicy = LocalMotionPolicy.current
    var displayedUsage by remember { mutableStateOf<ContextUsageBreakdown?>(usage) }
    LaunchedEffect(usage) {
        if (usage != null) displayedUsage = usage
    }
    val haptics = rememberPremiumHaptics()
    val enter = if (motionPolicy.reduceMotion) {
        fadeIn(tween(90))
    } else {
        fadeIn(tween(180)) + expandHorizontally(expandFrom = Alignment.Start) +
            slideInHorizontally(animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f)) { -it / 2 }
    }
    val exit = if (motionPolicy.reduceMotion) {
        fadeOut(tween(80))
    } else {
        fadeOut(tween(140)) + shrinkHorizontally(shrinkTowards = Alignment.Start) +
            slideOutHorizontally(animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f)) { -it / 2 }
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = usage != null,
        enter = enter,
        exit = exit,
        modifier = modifier.clipToBounds(),
    ) {
        displayedUsage?.let { animatedUsage ->
            ContextMeterButton(
                usage = animatedUsage,
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    onClick()
                },
            )
        }
    }
}

@Composable
private fun ContextMeterButton(
    usage: ContextUsageBreakdown,
    onClick: () -> Unit,
) {
    val animatedPressure by animateFloatAsState(
        targetValue = usage.fractionUsed,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 240f),
        label = "context_meter_pressure",
    )
    val animatedWindowProgress by animateFloatAsState(
        targetValue = usage.fractionOfWindowUsed,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 240f),
        label = "context_meter_window_progress",
    )
    val animatedReservedProgress by animateFloatAsState(
        targetValue = usage.fractionOfWindowReserved,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 260f),
        label = "context_meter_reserved_progress",
    )
    val targetProgressColor = when {
        animatedPressure >= 0.95f -> MaterialTheme.colorScheme.error
        animatedPressure >= 0.82f -> if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
            Color(0xFFFFC857)
        } else {
            Color(0xFFB46900)
        }
        else -> MaterialTheme.colorScheme.primary
    }
    val progressColor by animateColorAsState(
        targetValue = targetProgressColor,
        animationSpec = tween(220),
        label = "context_meter_color",
    )
    val containerColor = domChatIconPillContainerColor()
    val shape = AppShapes.ButtonPill
    val settings = LocalSettings.current
    Surface(
        modifier = Modifier
            .size(AppSize.ChromePill)
            .then(
                if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                    Modifier.frostedGlassSurface(shape = shape)
                } else {
                    Modifier.liquidGlassSurface(shape = shape, glassMode = settings.displaySetting.liquidGlassMode)
                }
            )
            .clickable(onClick = onClick),
        shape = shape,
        color = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) blurredContainerColor(containerColor) else Color.Transparent,
        border = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) domChatSoftEdgeBorder() else null,
    ) {
        Box(contentAlignment = Alignment.Center) {
            val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            val reserveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            Canvas(modifier = Modifier.size(27.dp)) {
                val strokeWidth = 3.5.dp.toPx()
                val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke,
                )
                if (animatedReservedProgress > 0f) {
                    drawArc(
                        color = reserveColor,
                        startAngle = -90f + (1f - animatedReservedProgress) * 360f,
                        sweepAngle = animatedReservedProgress * 360f,
                        useCenter = false,
                        style = stroke,
                    )
                }
                if (animatedWindowProgress > 0f) {
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = animatedWindowProgress * 360f,
                        useCenter = false,
                        style = stroke,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextUsagePopupContent(
    usage: ContextUsageBreakdown,
    activity: ContextManagementActivity?,
    model: Model? = null,
    onUpdateModelLimit: ((Int?) -> Unit)? = null,
) {
    val tokenAnimation = spring<Int>(dampingRatio = 0.82f, stiffness = 260f)
    val animatedUsed by animateIntAsState(usage.usedTokens, tokenAnimation, label = "context_used_tokens")
    val animatedTotal by animateIntAsState(usage.totalTokens, tokenAnimation, label = "context_total_tokens")
    val animatedReserved by animateIntAsState(usage.reservedTokens, tokenAnimation, label = "context_reserved")
    val animatedConversation by animateIntAsState(usage.conversationTokens, tokenAnimation, label = "context_conversation")
    val animatedSystemPrompt by animateIntAsState(usage.systemPromptTokens, tokenAnimation, label = "context_system_prompt")
    val animatedSummary by animateIntAsState(usage.summaryTokens, tokenAnimation, label = "context_summary")
    val animatedMemory by animateIntAsState(usage.memoryTokens, tokenAnimation, label = "context_memory")
    val animatedSkills by animateIntAsState(usage.skillTokens, tokenAnimation, label = "context_skills")
    val animatedLorebook by animateIntAsState(usage.lorebookTokens, tokenAnimation, label = "context_lorebook")
    val animatedToolDefinitions by animateIntAsState(usage.toolDefinitionTokens, tokenAnimation, label = "context_tool_definitions")
    val animatedToolCalls by animateIntAsState(usage.toolCallTokens, tokenAnimation, label = "context_tool_calls")
    val animatedMedia by animateIntAsState(usage.mediaTokens, tokenAnimation, label = "context_media")
    val animatedImages by animateIntAsState(usage.imageCount, tokenAnimation, label = "context_images")

    val haptics = rememberPremiumHaptics()
    var isEditingLimit by remember { mutableStateOf(false) }
    val maxCapacityTokens = model?.baseCapacityTokens ?: maxOf(usage.totalTokens, model?.contextCapacityTokens ?: 0)
    val isCustomLimitActive = model?.customContextLimitTokens != null ||
        model?.contextLimitSource == me.lucid.ai.provider.ContextLimitSource.MANUAL

    val minSafeFloorTokens = remember(model, usage) {
        if (model != null) {
            calculateMinSafeFloorTokens(
                model = model,
                systemPromptTokens = usage.systemPromptTokens,
                toolDefinitionTokens = usage.toolDefinitionTokens,
            )
        } else {
            1_500
        }
    }
    val safeFloor = minSafeFloorTokens.coerceAtMost(maxCapacityTokens)

    val currentLimit = model?.customContextLimitTokens ?: usage.totalTokens
    var draggedTokens by remember(currentLimit, maxCapacityTokens) {
        mutableStateOf(currentLimit.coerceIn(safeFloor, maxCapacityTokens))
    }
    LaunchedEffect(currentLimit, maxCapacityTokens, isEditingLimit) {
        if (!isEditingLimit) {
            draggedTokens = currentLimit.coerceIn(safeFloor, maxCapacityTokens)
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val categoryColors = if (isDark) {
        listOf(
            Color(0xFF8AB4F8), Color(0xFFFF8A80), Color(0xFFFFD166),
            Color(0xFF7ED99B), Color(0xFFC69AF7), Color(0xFF4DD0E1),
            Color(0xFFFFA45B), Color(0xFFF48FB1), Color(0xFFB6D957),
        )
    } else {
        listOf(
            Color(0xFF2457C5), Color(0xFFC43D3D), Color(0xFF9A6500),
            Color(0xFF187A3B), Color(0xFF7139B6), Color(0xFF087F8C),
            Color(0xFFB84A00), Color(0xFFA92868), Color(0xFF5F7300),
        )
    }
    val segments = listOf(
        Triple(stringResource(R.string.context_meter_conversation), animatedConversation, categoryColors[0]),
        Triple(stringResource(R.string.context_meter_system_prompt), animatedSystemPrompt, categoryColors[1]),
        Triple(stringResource(R.string.context_meter_summary), animatedSummary, categoryColors[2]),
        Triple(stringResource(R.string.context_meter_memory), animatedMemory, categoryColors[3]),
        Triple(stringResource(R.string.context_meter_skills_modes), animatedSkills, categoryColors[4]),
        Triple(stringResource(R.string.context_meter_lorebook), animatedLorebook, categoryColors[5]),
        Triple(stringResource(R.string.context_meter_tool_definitions), animatedToolDefinitions, categoryColors[6]),
        Triple(stringResource(R.string.context_meter_tool_calls), animatedToolCalls, categoryColors[7]),
        Triple(stringResource(R.string.context_meter_media), animatedMedia, categoryColors[8]),
    ).filter { it.second > 0 }
    val reserveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    val activeBudgetCeiling = if (isEditingLimit) draggedTokens else animatedTotal
    val liveBudget = if (model != null) {
        smartInputBudget(model, null, customLimitTokens = activeBudgetCeiling) ?: activeBudgetCeiling
    } else {
        activeBudgetCeiling
    }
    val liveReserve = (activeBudgetCeiling - liveBudget).coerceAtLeast(0)
    val effectiveReserved = if (isEditingLimit) liveReserve else (if (animatedReserved > 0) animatedReserved else liveReserve)
    val animatedEffectiveReserved by animateIntAsState(effectiveReserved, tokenAnimation, label = "context_effective_reserved")
    val effectiveAvailable = (activeBudgetCeiling - animatedUsed - animatedEffectiveReserved).coerceAtLeast(0)
    val remainingPercent = if (activeBudgetCeiling <= 0) 100 else
        ((effectiveAvailable.toFloat() / activeBudgetCeiling) * 100).toInt().coerceIn(0, 100)

    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            DomChatSheetDragHandle()
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(
                targetState = isEditingLimit,
                transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(110)) },
                modifier = Modifier.weight(1f),
                label = "context_meter_header_text",
            ) { editing ->
                if (editing) {
                    Text(
                        text = "Target Limit: ${compactTokenCount(draggedTokens)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.context_meter_used,
                            compactTokenCount(animatedUsed),
                            compactTokenCount(animatedTotal),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            AnimatedContent(
                targetState = isEditingLimit,
                transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(110)) },
                label = "context_meter_sub_text",
            ) { editing ->
                if (editing) {
                    Text(
                        text = "Floor: ${compactTokenCount(safeFloor)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.context_meter_remaining, remainingPercent),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            var barWidthPx by remember { mutableFloatStateOf(1f) }
            val zoomFraction by animateFloatAsState(
                targetValue = if (isEditingLimit && maxCapacityTokens > 0) {
                    (draggedTokens.toFloat() / maxCapacityTokens).coerceIn(0.01f, 1f)
                } else 1f,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = 240f),
                label = "context_limit_zoom",
            )

            val canReset = isCustomLimitActive || (isEditingLimit && draggedTokens != maxCapacityTokens)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .onGloballyPositioned { coordinates ->
                        barWidthPx = coordinates.size.width.toFloat().coerceAtLeast(1f)
                    }
                    .then(
                        if (isEditingLimit) {
                            Modifier.pointerInput(safeFloor, maxCapacityTokens) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Selection)
                                        val fraction = (offset.x / barWidthPx).coerceIn(0f, 1f)
                                        val computed = (fraction * maxCapacityTokens).roundToInt()
                                        draggedTokens = computed.coerceIn(safeFloor, maxCapacityTokens)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val fraction = (change.position.x / barWidthPx).coerceIn(0f, 1f)
                                        val computed = (fraction * maxCapacityTokens).roundToInt()
                                        val clamped = computed.coerceIn(safeFloor, maxCapacityTokens)
                                        if (clamped != draggedTokens) {
                                            haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Tick)
                                            draggedTokens = clamped
                                        }
                                    },
                                    onDragEnd = {
                                        haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
                                    },
                                )
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                // Background Track (18dp height centered in 24dp container)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    // Active content segments
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(zoomFraction)
                            .clip(RoundedCornerShape(999.dp)),
                    ) {
                        segments.forEach { (_, value, color) ->
                            Spacer(
                                Modifier
                                    .weight(value.toFloat().coerceAtLeast(1f))
                                    .fillMaxHeight()
                                    .background(color),
                            )
                        }
                        if (effectiveAvailable > 0) {
                            Spacer(Modifier.weight(effectiveAvailable.toFloat()).fillMaxHeight())
                        }
                        if (animatedEffectiveReserved > 0) {
                            Spacer(
                                Modifier
                                    .weight(animatedEffectiveReserved.toFloat())
                                    .fillMaxHeight()
                                    .background(reserveColor),
                            )
                        }
                    }

                    // Ignored / Darkened Region on the right in Edit Mode
                    if (isEditingLimit && zoomFraction < 0.999f) {
                        val ignoredColor = if (isDark) Color(0xFF070709) else Color(0xFF18181E)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(1f - zoomFraction)
                                .align(Alignment.CenterEnd)
                                .background(ignoredColor)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(topEnd = 999.dp, bottomEnd = 999.dp),
                                ),
                        )
                    }
                }

                // Tactile Draggable thumb indicator (24dp height, 8dp width, centered over the 18dp track)
                if (isEditingLimit) {
                    val density = LocalDensity.current
                    val thumbWidth = 8.dp
                    val thumbWidthPx = with(density) { thumbWidth.toPx() }
                    val thumbOffset = ((zoomFraction * barWidthPx) - (thumbWidthPx / 2f))
                        .coerceIn(0f, (barWidthPx - thumbWidthPx).coerceAtLeast(0f))
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset { androidx.compose.ui.unit.IntOffset(thumbOffset.roundToInt(), 0) }
                            .width(thumbWidth)
                            .height(24.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(999.dp)),
                    )
                }
            }

            // The Pencil / Close button sized to 24dp to optically balance against the bar height, with long-press reset
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        color = when {
                            isEditingLimit -> MaterialTheme.colorScheme.surfaceContainerHighest
                            isCustomLimitActive -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                        },
                        shape = androidx.compose.foundation.shape.CircleShape,
                    )
                    .combinedClickable(
                        onClick = {
                            haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
                            if (isEditingLimit) {
                                val newLimit = if (draggedTokens >= maxCapacityTokens) null else draggedTokens
                                onUpdateModelLimit?.invoke(newLimit)
                                isEditingLimit = false
                            } else {
                                isEditingLimit = true
                            }
                        },
                        onLongClick = if (canReset) {
                            {
                                haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Success)
                                draggedTokens = maxCapacityTokens
                                onUpdateModelLimit?.invoke(null)
                                isEditingLimit = false
                            }
                        } else null,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = isEditingLimit,
                    transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(110)) },
                    label = "context_limit_edit_toggle",
                ) { editing ->
                    Icon(
                        imageVector = if (editing) androidx.compose.material.icons.Icons.Rounded.Close else androidx.compose.material.icons.Icons.Rounded.Edit,
                        contentDescription = if (editing) "Commit" else "Edit context limit",
                        modifier = Modifier.size(13.dp),
                        tint = when {
                            editing -> MaterialTheme.colorScheme.onSurface
                            isCustomLimitActive -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
        FlowRow(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        ) {
            segments.forEach { (label, value, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(color, RoundedCornerShape(999.dp)))
                    Spacer(Modifier.width(5.dp))
                    Text("$label ${compactTokenCount(value)}", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (animatedEffectiveReserved > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(reserveColor, RoundedCornerShape(999.dp)))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "${stringResource(R.string.context_meter_reserved)} ${compactTokenCount(animatedEffectiveReserved)}",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(
                targetState = activity,
                transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(110)) },
                label = "context_management_activity",
            ) { currentActivity ->
                if (currentActivity == ContextManagementActivity.SUMMARIZING) {
                    Text(
                        stringResource(R.string.context_meter_summarizing),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Spacer(Modifier.width(0.dp))
                }
            }
            if (activity != null && usage.maxImages != null) Spacer(Modifier.width(12.dp))
            usage.maxImages?.let { maxImages ->
                Text(
                    stringResource(R.string.context_meter_images, animatedImages, maxImages),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.weight(1f))
            AnimatedContent(
                targetState = usage.confidence,
                transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(110)) },
                label = "context_confidence",
            ) { confidence ->
                Text(
                    text = stringResource(
                        when (confidence) {
                            ContextCountConfidence.EXACT -> R.string.context_meter_exact
                            ContextCountConfidence.PROVIDER_COUNTED -> R.string.context_meter_provider_counted
                            ContextCountConfidence.ESTIMATED -> R.string.context_meter_estimated
                        }
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun compactTokenCount(tokens: Int): String = when {
    tokens >= 1_000_000 -> "%.1fM".format(tokens / 1_000_000f)
    tokens >= 1_000 -> "%.1fK".format(tokens / 1_000f)
    else -> tokens.toString()
}

/**
 * Compaction badge shown above the reasoning badge when older turns have been
 * summarized into [contextSummary]. Expandable to reveal the summary text.
 * Never leaks into roleplay — purely a UI affordance.
 */
@Composable
private fun CompactionBadge(
    summary: String,
    summarizedUpToIndex: Int,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptics = rememberPremiumHaptics()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptics.perform(HapticPattern.Pop)
                    isExpanded = !isExpanded
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isExpanded) "Compacted — turn $summarizedUpToIndex summarized" else "Compacting…",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White.copy(alpha = 0.9f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp),
            )
        }
        
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .background(Color(0xFF000000), RoundedCornerShape(8.dp))
                    .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(8.dp))
                    .padding(10.dp),
            ) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@Composable
private fun ChatSwitchLoadingIndicator(
    assistant: Assistant,
    conversationTitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chat_loading_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chat_loading_glow"
    )
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chat_avatar_pulse"
    )

    val resolvedTitle = conversationTitle?.takeIf { it.isNotBlank() && it != "New message" && it != "Untitled Conversation" }
        ?: assistant.chatName?.takeIf { it.isNotBlank() }
        ?: assistant.displayName?.takeIf { it.isNotBlank() }
        ?: assistant.name.takeIf { it.isNotBlank() }
        ?: "Chat"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                        event.changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Glowing Assistant Avatar
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = scalePulse
                        scaleY = scalePulse
                    }
                    .liquidGlassSurface(
                        shape = CircleShape,
                        borderWidth = 1.dp,
                        fillColor = Color(0xFF000000).copy(alpha = 0.65f),
                        specularBrush = LiquidGlassTokens.SpecularBorderBrush,
                    ),
                contentAlignment = Alignment.Center
            ) {
                me.lucid.domchat.ui.components.ui.UIAvatar(
                    name = resolvedTitle,
                    value = assistant.avatar,
                    modifier = Modifier.size(64.dp),
                    onClick = null,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = resolvedTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White.copy(alpha = 0.85f),
                        trackColor = Color.White.copy(alpha = 0.12f),
                    )
                    Text(
                        text = "Loading conversation...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.70f)
                        )
                    )
                }
            }
        }
    }
}

