package me.lucid.domchat.ui.components.ai

import me.lucid.domchat.ui.theme.liquidGlassSurface
import androidx.compose.foundation.border
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableFloatStateOf
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.ui.components.ui.SearchProviderIcon
import me.lucid.search.SearchServiceOptions
import coil3.compose.AsyncImage
import me.lucid.ai.ui.UIMessagePart
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.spring
import androidx.compose.ui.zIndex
import androidx.compose.foundation.combinedClickable
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.verticalScroll
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.rounded.Info
import me.lucid.domchat.data.datastore.ConversationWallpaperConfig
import me.lucid.domchat.data.model.BackgroundBlurMode
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatFloatingSurfaceColor
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContentColor
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import me.lucid.ai.provider.Model
import me.lucid.ai.ui.ToolApprovalState
import me.lucid.domchat.R
import me.lucid.domchat.data.ai.models.ModelCatalogService
import me.lucid.domchat.data.ai.mcp.McpManager
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.findProvider
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.domchat.data.datastore.resolveConversationContext
import me.lucid.domchat.ui.components.chat.PersonaPickerBottomSheet
import me.lucid.domchat.ui.components.chat.PromptPresetsBottomSheet
import me.lucid.domchat.ui.components.chat.AetherInWindowBottomSheet
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.canManuallySummarizeConversation
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.data.model.hasManualSkillSelectionOverride
import me.lucid.domchat.data.model.Skill
import me.lucid.domchat.data.model.withoutSkillSelectionOverride
import me.lucid.domchat.service.ChatService
import me.lucid.domchat.ui.components.crop.CropImageScreen
import me.lucid.domchat.ui.components.chat.DomChatComposerAddButton
import me.lucid.domchat.ui.components.chat.DomChatComposerAction
import me.lucid.domchat.ui.components.chat.DomChatComposerActionButton
import me.lucid.domchat.ui.components.chat.DomChatComposerCapsule
import me.lucid.domchat.ui.components.chat.DomChatComposerAttachmentRow
import me.lucid.domchat.ui.components.chat.DomChatComposerAudioIcon
import me.lucid.domchat.ui.components.chat.DomChatDocumentAttachmentTile
import me.lucid.domchat.ui.components.chat.DomChatComposerImageAttachment
import me.lucid.domchat.ui.components.chat.DomChatComposerInputShape
import me.lucid.domchat.ui.components.chat.DomChatComposerMediaAttachment
import me.lucid.domchat.ui.components.chat.DomChatComposerRow
import me.lucid.domchat.ui.components.chat.DomChatComposerVideoIcon
import me.lucid.domchat.ui.components.ui.icons.ModeIcons
import me.lucid.domchat.ui.components.ui.permission.PermissionCamera
import me.lucid.domchat.ui.components.ui.permission.PermissionMicrophone
import me.lucid.domchat.ui.components.ui.permission.PermissionManager
import me.lucid.domchat.ui.components.ui.permission.rememberPermissionState
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.AppSize
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.context.LocalSTTState
import me.lucid.domchat.ui.hooks.ChatInputState
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.mergeCommittedSttTranscript
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.shouldApplyPendingSttTranscript
import me.lucid.domchat.ui.modifier.blurredContainerColor
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.data.ai.tools.LocalToolOption
import me.lucid.domchat.data.ai.tools.AskUserAnswer
import me.lucid.domchat.data.ai.tools.AskUserAnswerPayload
import me.lucid.domchat.data.ai.tools.AskUserOption
import me.lucid.domchat.data.ai.tools.AskUserQuestionnaire
import me.lucid.domchat.data.ai.tools.ASK_USER_TOOL_NAME
import me.lucid.domchat.data.ai.tools.findPendingAskUserToolCall
import me.lucid.domchat.data.ai.tools.toJsonElement
import me.lucid.domchat.data.repository.ChatAttachmentManager
import me.lucid.domchat.data.repository.WorkspaceRepository
import me.lucid.domchat.utils.JsonInstantPretty
import me.lucid.domchat.utils.jsonPrimitiveOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.time.Instant
import kotlin.uuid.Uuid
import org.koin.compose.koinInject

/**
 * Minimal ChatGPT-style input bar with bottom sheet picker.
 * Shows a simple input bar with + button, text field, and send button.
 * The + button opens a bottom sheet with file upload, model picker, and other options.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun MinimalChatInput(
    state: ChatInputState,
    conversation: Conversation,
    settings: Settings,
    mcpManager: McpManager,
    enableSearch: Boolean,
    onToggleSearch: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    chatSuggestions: List<String> = emptyList(),
    onClickSuggestion: (String) -> Unit = {},
    onUpdateChatModel: (Model) -> Unit,
    onUpdateAssistant: (Assistant) -> Unit,
    onUpdateConversation: (Conversation) -> Unit,
    onToolApproval: (toolCallId: String, approved: Boolean, reason: String, answer: String?) -> Unit,
    onUpdateSearchService: (Int) -> Unit,
    onClearContext: () -> Unit,
    onCancelClick: () -> Unit,
    onSendClick: () -> Unit,
    onLongSendClick: () -> Unit,
    onNavigateToLorebook: (String) -> Unit = {},
    onRefreshContext: suspend () -> ChatService.ContextRefreshResult = {
        ChatService.ContextRefreshResult(
            success = false,
            errorResId = R.string.context_refresh_no_summarizer,
        )
    },
    onDeleteFile: (Uri) -> Unit = {},
    bottomAccessory: @Composable (() -> Unit)? = null,
    showScrollToBottomButton: Boolean = false,
    onScrollToBottomClick: () -> Unit = {},
    onShowChatInfo: () -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 24.dp,
) {
    val context = LocalContext.current
    val toaster = LocalToaster.current
    val conversationContext = remember(settings, conversation) {
        settings.resolveConversationContext(conversation)
    }
    val assistant = conversationContext.assistant
    val currentChatModel = conversationContext.chatModel
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val workspaceRepository = koinInject<WorkspaceRepository>()
    val modelCatalog = koinInject<ModelCatalogService>()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val settingsStore: me.lucid.domchat.data.datastore.SettingsStore = koinInject()
    val wallpaperPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val localPath = me.lucid.domchat.utils.ImageUtils.copyImageToAppStorage(
                context = context,
                sourceUri = uri,
                subDir = "wallpapers",
                fileName = "wp_chat_${conversation.id}_${System.currentTimeMillis()}.jpg"
            )
            val updatedUri = localPath ?: uri.toString()
            val convId = conversation.id.toString()
            val existing = settings.displaySetting.conversationWallpapers[convId] ?: me.lucid.domchat.data.datastore.ConversationWallpaperConfig()
            val newMap = settings.displaySetting.conversationWallpapers + (convId to existing.copy(backgroundUri = updatedUri))
            scope.launch {
                settingsStore.update { cur ->
                    cur.copy(displaySetting = cur.displaySetting.copy(conversationWallpapers = newMap))
                }
            }
        }
    }
    val availableSkills = remember(settings.skills) {
        settings.skills
    }
    val availableSkillIds = remember(availableSkills) { availableSkills.map { it.id }.toSet() }
    val assistantAvailableSkillIds = remember(settings.skills, assistant.id) {
        settings.skills.filter { it.isAvailableForAssistant(assistant.id) }.map { it.id }.toSet()
    }
    val pendingQuestionnaire = remember(conversation.messageNodes) {
        conversation.currentMessages.findPendingAskUserToolCall()
    }
    val isQuestionnaireActive = pendingQuestionnaire != null
    val pendingToolApproval = remember(conversation.messageNodes, pendingQuestionnaire?.toolCallId) {
        if (pendingQuestionnaire != null) {
            null
        } else {
            conversation.currentMessages.findPendingToolApproval()
        }
    }
    val isToolApprovalActive = pendingToolApproval != null
    val questionnaire = pendingQuestionnaire?.questionnaire
    val questionnaireToolCallId = pendingQuestionnaire?.toolCallId
    var questionnaireIndex by rememberSaveable(questionnaireToolCallId) { mutableStateOf(0) }
    var questionnaireSelectedOptions by rememberSaveable(questionnaireToolCallId) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    var questionnaireCustomAnswers by rememberSaveable(questionnaireToolCallId) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    val questionnaireTextState = remember(questionnaireToolCallId) { TextFieldState() }
    val toolApprovalTextState = remember(pendingToolApproval?.toolCallId) { TextFieldState() }
    val currentQuestion = questionnaire?.questions?.getOrNull(questionnaireIndex)
    val isFinalQuestion = questionnaire != null && questionnaireIndex == questionnaire.questions.lastIndex

    val attachedUnsupportedArchives = remember(state.pendingAttachments) {
        state.pendingAttachments
            .map { it.part }
            .filterIsInstance<UIMessagePart.Document>()
            .filter { me.lucid.domchat.data.ai.transformers.isArchiveOrBinaryFile(it.fileName, it.mime) }
    }
    var dismissedWorkspaceRequiredCardForAttachments by remember { mutableStateOf<Set<String>>(emptySet()) }
    val activeUnsupportedArchive = attachedUnsupportedArchives.firstOrNull { it.url !in dismissedWorkspaceRequiredCardForAttachments }
    val isWorkspaceRequiredCardVisible = activeUnsupportedArchive != null && assistant.workspaceId == null
    var showPersonaPicker by remember { mutableStateOf(false) }
    var showPromptPresetsSheet by remember { mutableStateOf(false) }
    var showContextBudgetSheet by remember { mutableStateOf(false) }
    var showAuthorsNoteSheet by remember { mutableStateOf(false) }
    var showSamplersSheet by remember { mutableStateOf(false) }
    var showWallpaperSheet by remember { mutableStateOf(false) }
    var showCreatorNotesSheet by remember { mutableStateOf(false) }
    var showStreamingAnimationSheet by remember { mutableStateOf(false) }
    var showSearchPicker by remember { mutableStateOf(false) }
    var showReasoningPicker by remember { mutableStateOf(false) }
    var showSkillsPicker by remember { mutableStateOf(false) }
    var showLorebooksPicker by remember { mutableStateOf(false) }
    var showPluginsPicker by remember { mutableStateOf(false) }

    // OLED dark mode handling for picker sheet
    val amoledMode by me.lucid.domchat.ui.hooks.rememberAmoledDarkMode()
    val isDarkMode = me.lucid.domchat.ui.theme.LocalDarkMode.current
    val isAmoled = amoledMode && isDarkMode
    // Picker sheet styling - optical roundness: outer (40dp) = button corners (24dp) + padding (16dp)
    // Sheet uses surfaceContainerLow always, buttons inside handle OLED colors
    val pickerSheetColor = MaterialTheme.colorScheme.surfaceContainerLow
    val pickerSheetShape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
    
    // Camera permission - must be in parent, not inside ModalBottomSheet
    val cameraPermission = rememberPermissionState(PermissionCamera)
    val microphonePermission = rememberPermissionState(PermissionMicrophone)
    val stt = LocalSTTState.current
    val sttState by stt.state.collectAsStateWithLifecycle()
    val hasSelectedSttProvider = settings.sttModelId != null
    var sttDraft by remember { mutableStateOf("") }
    var acceptSttWhenIdle by remember { mutableStateOf(false) }
    var discardSttWhenIdle by remember { mutableStateOf(false) }
    var sttEpochAtStop by remember { mutableIntStateOf(0) }
    val sttRecording = sttState.isRecording
    val sttFinalizing = sttState.status == me.lucid.asr.ASRStatus.Stopping

    LaunchedEffect(sttState.errorMessage) {
        sttState.errorMessage?.let { error ->
            toaster.show(error, type = me.lucid.domchat.ui.components.ui.ToastType.Error)
            stt.clearError()
        }
    }

    fun startSttRecording() {
        if (!microphonePermission.allRequiredPermissionsGranted) {
            microphonePermission.requestPermissions()
            return
        }
        sttDraft = ""
        acceptSttWhenIdle = false
        discardSttWhenIdle = false
        sttEpochAtStop = state.sttCommitEpoch
        keyboardController?.hide()
        stt.start { transcript ->
            sttDraft = transcript
        }
    }

    fun stopSttRecording(accept: Boolean) {
        sttEpochAtStop = state.sttCommitEpoch
        acceptSttWhenIdle = accept
        discardSttWhenIdle = !accept
        stt.stop()
    }

    LaunchedEffect(sttState.transcript) {
        if (sttState.transcript.isNotBlank()) {
            sttDraft = sttState.transcript
        }
    }

    LaunchedEffect(sttRecording, sttFinalizing, acceptSttWhenIdle, discardSttWhenIdle) {
        if (!sttRecording && !sttFinalizing && (acceptSttWhenIdle || discardSttWhenIdle)) {
            if (
                shouldApplyPendingSttTranscript(
                    accept = acceptSttWhenIdle,
                    epochAtCapture = sttEpochAtStop,
                    epochNow = state.sttCommitEpoch,
                )
            ) {
                delay(220)
                if (
                    shouldApplyPendingSttTranscript(
                        accept = true,
                        epochAtCapture = sttEpochAtStop,
                        epochNow = state.sttCommitEpoch,
                    )
                ) {
                    val transcript = sttDraft.trim()
                    if (transcript.isNotBlank()) {
                        state.setMessageText(
                            mergeCommittedSttTranscript(
                                existing = state.textContent.text.toString(),
                                transcript = transcript,
                            )
                        )
                        runCatching { state.focusRequester.requestFocus() }
                    }
                }
            }
            sttDraft = ""
            acceptSttWhenIdle = false
            discardSttWhenIdle = false
        }
    }
    
    var showPicker by remember { mutableStateOf(false) }
    var showModelPickerSheet by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    var isExpandedFullScreen by remember { mutableStateOf(false) }
    var imageToCrop by remember { mutableStateOf<PendingImageCrop?>(null) }
    var pendingAttachmentImports by remember { mutableIntStateOf(0) }
    val isImportingAttachments = pendingAttachmentImports > 0
    val onAttachmentImportStarted = { pendingAttachmentImports += 1 }
    val onAttachmentImportFinished = {
        pendingAttachmentImports = (pendingAttachmentImports - 1).coerceAtLeast(0)
    }

    LaunchedEffect(questionnaireToolCallId, questionnaire?.questions?.size) {
        if (questionnaire == null) {
            questionnaireIndex = 0
            questionnaireTextState.setTextAndPlaceCursorAtEnd("")
        } else {
            questionnaireIndex = questionnaireIndex.coerceIn(0, questionnaire.questions.lastIndex)
            val initialText = questionnaire.questions
                .getOrNull(questionnaireIndex)
                ?.let { question -> questionnaireCustomAnswers[question.id].orEmpty() }
                .orEmpty()
            questionnaireTextState.setTextAndPlaceCursorAtEnd(initialText)
        }
    }

    LaunchedEffect(currentQuestion?.id) {
        val nextText = currentQuestion?.let { questionnaireCustomAnswers[it.id].orEmpty() }.orEmpty()
        if (questionnaireTextState.text.toString() != nextText) {
            questionnaireTextState.setTextAndPlaceCursorAtEnd(nextText)
        }
    }
    LaunchedEffect(currentQuestion?.id, questionnaireTextState.text.toString()) {
        val question = currentQuestion ?: return@LaunchedEffect
        questionnaireCustomAnswers = questionnaireCustomAnswers + (question.id to questionnaireTextState.text.toString())
    }
    LaunchedEffect(pendingToolApproval?.toolCallId) {
        toolApprovalTextState.setTextAndPlaceCursorAtEnd("")
    }

    // Collapse picker when keyboard opens
    val imeVisible = WindowInsets.isImeVisible
    val focusManager = LocalFocusManager.current
    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            showPicker = false
        } else {
            focusManager.clearFocus()
        }
    }
    LaunchedEffect(isQuestionnaireActive, isToolApprovalActive) {
        if (isQuestionnaireActive || isToolApprovalActive) {
            showPicker = false
        }
    }

    fun buildQuestionnairePayload(dismissed: Boolean): String? {
        val activeQuestionnaire = questionnaire ?: return null
        val payload = AskUserAnswerPayload(
            answers = activeQuestionnaire.questions.map { question ->
                val custom = questionnaireCustomAnswers[question.id]?.trim().orEmpty()
                val selectedOption = questionnaireSelectedOptions[question.id]?.trim().orEmpty()
                when {
                    custom.isNotBlank() -> AskUserAnswer(
                        id = question.id,
                        status = "answered",
                        source = "custom",
                        value = custom,
                    )

                    selectedOption.isNotBlank() -> AskUserAnswer(
                        id = question.id,
                        status = "answered",
                        source = "option",
                        value = selectedOption,
                    )

                    else -> AskUserAnswer(
                        id = question.id,
                        status = "skipped",
                    )
                }
            },
            dismissed = dismissed,
        )
        return JsonInstantPretty.encodeToString(
            kotlinx.serialization.json.JsonElement.serializer(),
            payload.toJsonElement()
        )
    }

    fun submitQuestionnaire(dismissed: Boolean) {
        val toolCallId = questionnaireToolCallId ?: return
        val payload = buildQuestionnairePayload(dismissed) ?: return
        keyboardController?.hide()
        haptics.perform(if (dismissed) HapticPattern.Pop else HapticPattern.Send)
        onToolApproval(toolCallId, true, "", payload)
    }

    fun approvePendingTool(alwaysApproveWorkspace: Boolean) {
        val pending = pendingToolApproval ?: return
        keyboardController?.hide()
        haptics.perform(if (alwaysApproveWorkspace) HapticPattern.Success else HapticPattern.Send)
        if (alwaysApproveWorkspace && pending.isWorkspaceTool) {
            val workspaceId = assistant.workspaceId?.toString()
            if (workspaceId != null) {
                scope.launch {
                    workspaceRepository.setToolApproval(workspaceId, pending.toolName, needsApproval = false)
                    onToolApproval(pending.toolCallId, true, "", null)
                }
                return
            }
        }
        onToolApproval(pending.toolCallId, true, "", null)
    }

    fun denyPendingToolWithInstruction() {
        val pending = pendingToolApproval ?: return
        val reason = toolApprovalTextState.text.toString().trim()
        if (reason.isBlank()) {
            haptics.perform(HapticPattern.Pop)
            state.focusRequester.requestFocus()
            keyboardController?.show()
            return
        }
        keyboardController?.hide()
        haptics.perform(HapticPattern.Send)
        onToolApproval(pending.toolCallId, false, reason, null)
    }

    fun advanceQuestionnaire() {
        val activeQuestionnaire = questionnaire ?: return
        if (isFinalQuestion) {
            submitQuestionnaire(dismissed = false)
            return
        }
        questionnaireIndex = (questionnaireIndex + 1).coerceAtMost(activeQuestionnaire.questions.lastIndex)
        haptics.perform(HapticPattern.Pop)
    }

    fun sendMessage() {
        if (isQuestionnaireActive) {
            val question = currentQuestion
            if (question != null) {
                val trimmed = questionnaireTextState.text.toString().trim()
                questionnaireCustomAnswers = questionnaireCustomAnswers + (question.id to trimmed)
            }
            advanceQuestionnaire()
            return
        }
        if (isToolApprovalActive) {
            denyPendingToolWithInstruction()
            return
        }
        keyboardController?.hide()
        haptics.perform(HapticPattern.Send)
        if (state.loading) onCancelClick() else onSendClick()
    }

    fun replaceCroppedImage(original: PendingImageCrop, croppedUri: Uri) {
        scope.launch {
            val importedUri = withContext(Dispatchers.IO) {
                ChatAttachmentManager.importChatFiles(listOf(croppedUri)).firstOrNull()
            }
            if (importedUri == null) {
                Log.w("MinimalChatInput", "Failed to import cropped image: $croppedUri")
                toaster.show(context.getString(R.string.chat_input_selected_image_failed))
                withContext(Dispatchers.IO) {
                    if (croppedUri.scheme == "file") {
                        runCatching { File(croppedUri.path.orEmpty()).delete() }
                    }
                }
                return@launch
            }

            state.replaceAttachment(
                instanceId = original.instanceId,
                part = UIMessagePart.Image(importedUri.toString()),
            )
            withContext(Dispatchers.IO) {
                if (croppedUri.scheme == "file") {
                    runCatching { File(croppedUri.path.orEmpty()).delete() }
                }
            }
            haptics.perform(HapticPattern.Success)
        }
    }

    imageToCrop?.let { image ->
        CropImageScreen(
            sourceUri = image.image.url.toUri(),
            onCropComplete = { croppedUri ->
                imageToCrop = null
                replaceCroppedImage(image, croppedUri)
            },
            onCancel = {
                imageToCrop = null
                haptics.perform(HapticPattern.Pop)
            }
        )
    }
    
    val isAnySheetOpen = showPicker || showModelPickerSheet || showPersonaPicker ||
        showPromptPresetsSheet || showContextBudgetSheet || showAuthorsNoteSheet ||
        showSamplersSheet || showWallpaperSheet || showCreatorNotesSheet ||
        showStreamingAnimationSheet || showSearchPicker || showReasoningPicker ||
        showSkillsPicker || showLorebooksPicker || showPluginsPicker
    Box(
        modifier = if (isAnySheetOpen) Modifier.fillMaxSize() else modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = bottomPadding, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val showSuggestions = settings.displaySetting.showChatSuggestions && !isQuestionnaireActive && !isToolApprovalActive && chatSuggestions.isNotEmpty()
            val showScrollToBottom = !isQuestionnaireActive && !isToolApprovalActive && showScrollToBottomButton

            // Suggestions and scroll-to-bottom affordance share a row so they never overlap.
            androidx.compose.animation.AnimatedVisibility(
                visible = showSuggestions || showScrollToBottom,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showSuggestions) {
                        ChatSuggestionsRow(
                            modifier = Modifier.weight(1f),
                            suggestions = chatSuggestions,
                            onClickSuggestion = onClickSuggestion
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showScrollToBottom,
                        enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End),
                    ) {
                        ChatScrollToBottomButton(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                onScrollToBottomClick()
                            }
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isWorkspaceRequiredCardVisible && !isQuestionnaireActive && !isToolApprovalActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                activeUnsupportedArchive?.let { doc ->
                    WorkspaceRequiredCard(
                        fileName = doc.fileName,
                        onDismiss = {
                            dismissedWorkspaceRequiredCardForAttachments = dismissedWorkspaceRequiredCardForAttachments + doc.url
                            haptics.perform(HapticPattern.Pop)
                        }
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isQuestionnaireActive && questionnaire != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                questionnaire?.let { activeQuestionnaire ->
                    CharacterQuestionsCard(
                        questionnaire = activeQuestionnaire,
                        currentIndex = questionnaireIndex,
                        selectedOptionLabel = currentQuestion?.let { question ->
                            questionnaireSelectedOptions[question.id]
                        },
                        onPrevious = {
                            questionnaireIndex = (questionnaireIndex - 1).coerceAtLeast(0)
                            haptics.perform(HapticPattern.Pop)
                        },
                        onNext = {
                            questionnaireIndex = (questionnaireIndex + 1)
                                .coerceAtMost(activeQuestionnaire.questions.lastIndex)
                            haptics.perform(HapticPattern.Pop)
                        },
                        onDismiss = {
                            currentQuestion?.let { question ->
                                questionnaireCustomAnswers =
                                    questionnaireCustomAnswers + (question.id to questionnaireTextState.text.toString())
                            }
                            submitQuestionnaire(dismissed = true)
                        },
                        onSelectOption = { option ->
                            val question = currentQuestion ?: return@CharacterQuestionsCard
                            questionnaireSelectedOptions = questionnaireSelectedOptions + (question.id to option.label)
                            questionnaireCustomAnswers = questionnaireCustomAnswers + (question.id to "")
                            questionnaireTextState.setTextAndPlaceCursorAtEnd("")
                            haptics.perform(HapticPattern.Pop)
                            if (!isFinalQuestion) {
                                questionnaireIndex = (questionnaireIndex + 1)
                                    .coerceAtMost(activeQuestionnaire.questions.lastIndex)
                            }
                        }
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isToolApprovalActive && pendingToolApproval != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                pendingToolApproval?.let { pending ->
                    ToolApprovalCard(
                        approval = pending,
                        onApprove = { approvePendingTool(alwaysApproveWorkspace = false) },
                        onAlwaysApproveWorkspace = { approvePendingTool(alwaysApproveWorkspace = true) },
                        onDenyWithInstruction = {
                            state.focusRequester.requestFocus()
                            keyboardController?.show()
                            haptics.perform(HapticPattern.Pop)
                        }
                    )
                }
            }
            
            // Content receiver for clipboard image paste (must be outside Surface lambda)
            val receiveContentListener = remember(isQuestionnaireActive) {
                ReceiveContentListener { transferableContent ->
                    when {
                        isQuestionnaireActive -> transferableContent
                        transferableContent.hasMediaType(MediaType.Image) -> {
                            transferableContent.consume { item ->
                                item.uri?.let { uri ->
                                    onAttachmentImportStarted()
                                    scope.launch {
                                        try {
                                            val importedUris = withContext(Dispatchers.IO) {
                                                ChatAttachmentManager.importChatFiles(listOf(uri))
                                            }
                                            if (importedUris.isNotEmpty()) {
                                                state.addImages(importedUris)
                                            }
                                        } finally {
                                            onAttachmentImportFinished()
                                        }
                                    }
                                }
                                item.uri != null
                            }
                        }
                        else -> transferableContent
                    }
                }
            }
            
            // Minimal input bar - plus button + text field with embedded action button
            DomChatComposerRow {
                // Plus button - 48dp pill button
                if (!isQuestionnaireActive && !isToolApprovalActive) {
                    DomChatComposerAddButton(
                        onLongClick = {
                            if (!sttRecording && !sttFinalizing && hasSelectedSttProvider) {
                                haptics.perform(HapticPattern.Pop)
                                startSttRecording()
                            }
                        },
                        onClick = {
                            haptics.perform(HapticPattern.Pop)
                            if (sttRecording) {
                                stopSttRecording(accept = true)
                            } else {
                                showPicker = true
                                keyboardController?.hide()
                            }
                        },
                        containerColor = domChatFloatingSurfaceColor(),
                        modifier = Modifier
                            .size(AppSize.ChromePill)
                            .liquidGlassSurface(
                                shape = CircleShape,
                                glassMode = settings.displaySetting.liquidGlassMode,
                                isFloatingControl = true
                            )
                            .domChatBlurEffect(domChatFloatingSurfaceColor(), CircleShape),
                    ) {
                        Icon(
                            imageVector = if (sttRecording) Icons.Rounded.Stop else Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (isDarkMode) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Text field capsule with embedded action button
                // Corner radius = 24dp (user confirmed this was correct)
                val inputShape = DomChatComposerInputShape
                val composerBg = domChatFloatingSurfaceColor()
                DomChatComposerCapsule(
                    containerColor = composerBg,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = AppSize.ChromePill)
                        .liquidGlassSurface(
                            shape = inputShape,
                            glassMode = settings.displaySetting.liquidGlassMode,
                            isFloatingControl = true
                        )
                        .domChatBlurEffect(composerBg, inputShape),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isQuestionnaireActive && !isToolApprovalActive && state.messageContent.isNotEmpty(),
                            enter = fadeIn(
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
                            ) + expandVertically(
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                                expandFrom = Alignment.Top
                            ),
                            exit = fadeOut(
                                animationSpec = spring(dampingRatio = 0.75f, stiffness = 360f)
                            ) + shrinkVertically(
                                animationSpec = spring(dampingRatio = 0.75f, stiffness = 360f),
                                shrinkTowards = Alignment.Top
                            )
                        ) {
                            MediaFileInputRow(
                                state = state,
                                onDelete = { uri ->
                                    haptics.perform(HapticPattern.Pop)
                                    onDeleteFile(uri)
                                },
                                onCropImage = { instanceId, image ->
                                    haptics.perform(HapticPattern.Pop)
                                    imageToCrop = PendingImageCrop(instanceId, image)
                                }
                            )
                        }

                        // Editing indicator - shown when editing a message
                        if (!isQuestionnaireActive && !isToolApprovalActive && state.isEditing()) {
                            Surface(
                                color = if (LocalDarkMode.current) 
                                    MaterialTheme.colorScheme.surfaceContainerLowest  // Darker in dark mode
                                else 
                                    MaterialTheme.colorScheme.surfaceContainerHighest,  // Darker in light mode
                                shape = RoundedCornerShape(16.dp),  // Optical roundness: 24dp outer - 8dp padding = 16dp
                                modifier = Modifier.padding(start = 12.dp, top = 10.dp, end = 8.dp, bottom = 4.dp)  // Aligned with text
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.editing),
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            state.editingMessage = null
                                            state.clearInput()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(R.string.cancel),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Text input with content receiver for paste + overlaid action button
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = (sttRecording || sttFinalizing) && hasSelectedSttProvider,
                                enter = fadeIn(spring(dampingRatio = 0.6f, stiffness = 300f)),
                                exit = fadeOut(tween(140)),
                                modifier = Modifier
                                    .matchParentSize()
                                    .zIndex(10f)
                            ) {
                                Surface(
                                    color = blurredContainerColor(MaterialTheme.colorScheme.surfaceContainer),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .domChatBlurEffect(MaterialTheme.colorScheme.surfaceContainer, inputShape)
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (sttRecording || sttFinalizing) {
                                                haptics.perform(HapticPattern.Pop)
                                                stopSttRecording(accept = true)
                                            }
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(
                                            top = 12.dp,
                                            bottom = 12.dp,
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = true,
                                            enter = slideInHorizontally(
                                                initialOffsetX = { it / 2 },
                                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
                                            ),
                                            exit = slideOutHorizontally(
                                                targetOffsetX = { it / 2 },
                                                animationSpec = tween(140)
                                            )
                                        ) {
                                            if (sttFinalizing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            } else {
                                                STTWaveformLine(
                                                    amplitudes = sttState.amplitudes,
                                                    active = sttRecording,
                                                    modifier = Modifier.fillMaxWidth().height(24.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            val activeTextState = when {
                                isQuestionnaireActive -> questionnaireTextState
                                isToolApprovalActive -> toolApprovalTextState
                                else -> state.textContent
                            }
                            var visualLineCount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(1) }
                            val lineCount = androidx.compose.runtime.derivedStateOf {
                                maxOf(activeTextState.text.toString().lines().size, visualLineCount)
                            }
                            TextField(
                                state = activeTextState,
                                onTextLayout = { getResult ->
                                    val result = getResult()
                                    if (result != null) {
                                        visualLineCount = result.lineCount
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 1.dp)  // Override internal min height (56dp)
                                    .focusRequester(state.focusRequester)
                                    .then(
                                        if (isQuestionnaireActive || isToolApprovalActive) {
                                            Modifier
                                        } else {
                                            Modifier.contentReceiver(receiveContentListener)
                                        }
                                    )
                                    .onFocusChanged { isFocused = it.isFocused },
                                placeholder = {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = !((sttRecording || sttFinalizing) && hasSelectedSttProvider),
                                        enter = fadeIn(tween(220)),
                                        exit = fadeOut(tween(140))
                                    ) {
                                        Text(
                                            text = if (isQuestionnaireActive) {
                                                stringResource(R.string.character_questions_custom_answer_placeholder)
                                            } else if (isToolApprovalActive) {
                                                stringResource(R.string.tool_approval_input_placeholder)
                                            } else {
                                                stringResource(R.string.minimal_chat_input_placeholder, assistant.name)
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 5),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    top = 12.dp,
                                    end = androidx.compose.animation.core.animateDpAsState(
                                        targetValue = if ((sttRecording || sttFinalizing) && hasSelectedSttProvider) 150.dp else 96.dp,
                                        animationSpec = tween(220),
                                        label = "input_padding"
                                    ).value,
                                    bottom = 12.dp,
                                ),
                                colors = TextFieldDefaults.colors().copy(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )
                            
                            ExpandButtonOverlay(
                                isVisible = !isExpandedFullScreen && lineCount.value >= 5 && !isQuestionnaireActive && !isToolApprovalActive,
                                onExpand = {
                                    haptics.perform(HapticPattern.Pop)
                                    isExpandedFullScreen = true
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 4.dp, top = 4.dp)
                            )
                            
                            // Action button row - bottom-right for multiline, optically centered when collapsed
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Small optical liquid glass hollow circle for smooth Chat info and Token usage popup
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            haptics.perform(HapticPattern.Pop)
                                            onShowChatInfo()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .border(
                                                width = 1.2.dp,
                                                brush = me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush,
                                                shape = CircleShape
                                            )
                                    )
                                }

                                AttachmentImportAction(
                                    isImporting = isImportingAttachments,
                                ) {
                                    val currentAction = when {
                                    isQuestionnaireActive && isFinalQuestion ->
                                        DomChatComposerAction.QuestionnaireSubmit
                                    isQuestionnaireActive -> DomChatComposerAction.QuestionnaireNext
                                    isToolApprovalActive -> DomChatComposerAction.ToolApprovalDeny
                                    state.loading -> DomChatComposerAction.Loading
                                    !state.isEmpty() -> DomChatComposerAction.Send
                                    hasSelectedSttProvider && sttRecording ->
                                        DomChatComposerAction.SttRecording
                                    hasSelectedSttProvider && sttFinalizing ->
                                        DomChatComposerAction.SttFinalizing
                                    hasSelectedSttProvider && settings.displaySetting.sttReplaceModelIcon ->
                                        DomChatComposerAction.Stt
                                    else -> DomChatComposerAction.Picker
                                }
                                DomChatComposerActionButton(
                                    action = currentAction,
                                    onClick = {
                                        when (currentAction) {
                                            DomChatComposerAction.Send,
                                            DomChatComposerAction.Loading,
                                            DomChatComposerAction.ToolApprovalDeny,
                                            DomChatComposerAction.QuestionnaireNext,
                                            DomChatComposerAction.QuestionnaireSubmit -> sendMessage()
                                            DomChatComposerAction.Stt -> {
                                                haptics.perform(HapticPattern.Pop)
                                                startSttRecording()
                                            }
                                            DomChatComposerAction.SttRecording -> {
                                                haptics.perform(HapticPattern.Pop)
                                                stopSttRecording(accept = true)
                                            }
                                            DomChatComposerAction.Picker -> {
                                                haptics.perform(HapticPattern.Pop)
                                                showPersonaPicker = true
                                            }
                                            DomChatComposerAction.SttFinalizing -> {
                                                showPicker = true
                                            }
                                        }
                                    },
                                ) { action ->
                                    when (action) {
                                        DomChatComposerAction.Loading -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Stop,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                        DomChatComposerAction.Send,
                                        DomChatComposerAction.QuestionnaireSubmit -> {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowUpward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        }
                                        DomChatComposerAction.QuestionnaireNext -> {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        }
                                        DomChatComposerAction.ToolApprovalDeny -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                        DomChatComposerAction.Stt,
                                        DomChatComposerAction.SttRecording -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Mic,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = if (action == DomChatComposerAction.SttRecording) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                            )
                                        }
                                        DomChatComposerAction.SttFinalizing -> Unit
                                        DomChatComposerAction.Picker -> {
                                            val activePersona = settings.displaySetting.getActivePersona()
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .border(
                                                        0.5.dp,
                                                        me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                me.lucid.domchat.ui.components.ui.UIAvatar(
                                                    name = activePersona.name,
                                                    value = activePersona.avatar,
                                                    modifier = Modifier.size(32.dp).clip(CircleShape),
                                                    onClick = {
                                                        haptics.perform(HapticPattern.Pop)
                                                        showPersonaPicker = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                }
                            }
                        }  // Box for TextField + Action button ends
                    }  // Column ends
                }  // Surface ends
            }  // Row ends

            bottomAccessory?.invoke()
        }  // Column ends
    }  // Box ends
    
    if (isExpandedFullScreen) {
        Dialog(
            onDismissRequest = { isExpandedFullScreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            val dialogView = LocalView.current
            val isDarkTheme = LocalDarkMode.current
            val backgroundColor = MaterialTheme.colorScheme.background

            SideEffect {
                val dialogWindow = findDialogWindow(dialogView)
                dialogWindow?.let { window ->
                    window.setDimAmount(0f)
                    @Suppress("DEPRECATION")
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    @Suppress("DEPRECATION")
                    window.navigationBarColor = android.graphics.Color.TRANSPARENT
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = backgroundColor.luminance() > 0.5f
                        isAppearanceLightNavigationBars = !isDarkTheme
                    }
                }
            }

            val transitionState = remember { MutableTransitionState(false) }
            LaunchedEffect(Unit) {
                transitionState.targetState = true
            }
            
            // Share the same activeTextState so the text is preserved
            val activeTextState = when {
                isQuestionnaireActive -> questionnaireTextState
                isToolApprovalActive -> toolApprovalTextState
                else -> state.textContent
            }
            
            // Request focus when dialog opens
            val expandedFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
            LaunchedEffect(transitionState.currentState) {
                if (transitionState.currentState) {
                    expandedFocusRequester.requestFocus()
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + fadeIn(tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(200, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                ) + fadeOut(tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .statusBarsPadding()
                        .imePadding()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top bar with minimize button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { 
                                    haptics.perform(HapticPattern.Pop)
                                    isExpandedFullScreen = false 
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FullscreenExit,
                                    contentDescription = "Minimize",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        
                        // Text input field taking up remaining space
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            TextField(
                                state = activeTextState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .focusRequester(expandedFocusRequester),
                                placeholder = {
                                    Text(
                                        text = if (isQuestionnaireActive) {
                                            stringResource(R.string.character_questions_custom_answer_placeholder)
                                        } else if (isToolApprovalActive) {
                                            stringResource(R.string.tool_approval_input_placeholder)
                                        } else {
                                            stringResource(R.string.minimal_chat_input_placeholder, assistant.name)
                                        }
                                    )
                                },
                                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 5),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    top = 16.dp,
                                    end = 16.dp,
                                    bottom = 80.dp, // Leave space for send button
                                ),
                                colors = TextFieldDefaults.colors().copy(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )
                            
                            // Floating action buttons at the bottom right
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            haptics.perform(HapticPattern.Pop)
                                            onShowChatInfo()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .border(
                                                width = 1.2.dp,
                                                brush = me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush,
                                                shape = CircleShape
                                            )
                                    )
                                }

                                AttachmentImportAction(
                                    isImporting = isImportingAttachments,
                                    modifier = Modifier.size(AppSize.ChromePill),
                                ) {
                                    val currentAction = when {
                                    isQuestionnaireActive && isFinalQuestion ->
                                        DomChatComposerAction.QuestionnaireSubmit
                                    isQuestionnaireActive -> DomChatComposerAction.QuestionnaireNext
                                    isToolApprovalActive -> DomChatComposerAction.ToolApprovalDeny
                                    state.loading -> DomChatComposerAction.Loading
                                    !state.isEmpty() -> DomChatComposerAction.Send
                                    else -> DomChatComposerAction.Picker
                                }
                                DomChatComposerActionButton(
                                    action = currentAction,
                                    onClick = {
                                        when (currentAction) {
                                            DomChatComposerAction.Send,
                                            DomChatComposerAction.Loading,
                                            DomChatComposerAction.ToolApprovalDeny,
                                            DomChatComposerAction.QuestionnaireNext,
                                            DomChatComposerAction.QuestionnaireSubmit -> {
                                                sendMessage()
                                                isExpandedFullScreen = false
                                            }
                                            DomChatComposerAction.Picker -> {
                                                haptics.perform(HapticPattern.Pop)
                                                showPersonaPicker = true
                                            }
                                            else -> Unit
                                        }
                                    },
                                    modifier = Modifier.size(AppSize.ChromePill),
                                    containerColorOverride = if (currentAction == DomChatComposerAction.Picker) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        null
                                    },
                                ) { action ->
                                    when (action) {
                                        DomChatComposerAction.Loading -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Stop,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                        DomChatComposerAction.Send,
                                        DomChatComposerAction.QuestionnaireSubmit -> {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowUpward,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        }
                                        DomChatComposerAction.QuestionnaireNext -> {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        }
                                        DomChatComposerAction.ToolApprovalDeny -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowUpward,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = if (isDarkMode) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurfaceVariant,
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
    }

    // In-window bottom sheet picker with custom MinimalPickerContent
    if (showPicker) {
        AetherInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showPicker = false },
        ) {
            MinimalPickerContent(
                state = state,
                conversation = conversation,
                settings = settings,
                assistant = assistant,
                currentChatModel = currentChatModel,
                cameraPermission = cameraPermission,
                enableSearch = enableSearch,
                onToggleSearch = onToggleSearch,
                onUpdateChatModel = onUpdateChatModel,
                onUpdateConversation = onUpdateConversation,
                onUpdateAssistant = onUpdateAssistant,
                onUpdateSearchService = onUpdateSearchService,
                onNavigateToLorebook = onNavigateToLorebook,
                onRefreshContext = onRefreshContext,
                importScope = scope,
                onAttachmentImportStarted = onAttachmentImportStarted,
                onAttachmentImportFinished = onAttachmentImportFinished,
                onOpenModelPicker = {
                    showPicker = false
                    showModelPickerSheet = true
                },
                onOpenPersonaPicker = {
                    showPicker = false
                    showPersonaPicker = true
                },
                onOpenPromptPresets = {
                    showPicker = false
                    showPromptPresetsSheet = true
                },
                onOpenContextBudget = {
                    showPicker = false
                    showContextBudgetSheet = true
                },
                onOpenAuthorsNote = {
                    showPicker = false
                    showAuthorsNoteSheet = true
                },
                onOpenSamplers = {
                    showPicker = false
                    showSamplersSheet = true
                },
                onOpenWallpaper = {
                    showPicker = false
                    showWallpaperSheet = true
                },
                onOpenCreatorNotes = {
                    showPicker = false
                    showCreatorNotesSheet = true
                },
                onOpenStreamingAnimation = {
                    showPicker = false
                    showStreamingAnimationSheet = true
                },
                onOpenSearchPicker = {
                    showPicker = false
                    showSearchPicker = true
                },
                onOpenReasoningPicker = {
                    showPicker = false
                    showReasoningPicker = true
                },
                onOpenSkillsPicker = {
                    showPicker = false
                    showSkillsPicker = true
                },
                onOpenLorebooksPicker = {
                    showPicker = false
                    showLorebooksPicker = true
                },
                onOpenPluginsPicker = {
                    showPicker = false
                    showPluginsPicker = true
                },
                onDismiss = { showPicker = false }
            )
        }
    }

    if (showModelPickerSheet) {
        val filteredProviders = remember(settings.providers) {
            settings.providers.filter { it.enabled }
        }
        AetherInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showModelPickerSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.85f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModelList(
                    currentModel = assistant.chatModelId ?: settings.chatModelId,
                    providers = filteredProviders,
                    modelType = me.lucid.ai.provider.ModelType.CHAT,
                    onSelect = { selectedModel: Model ->
                        onUpdateChatModel(selectedModel)
                        showModelPickerSheet = false
                    },
                    onDismiss = {
                        showModelPickerSheet = false
                    }
                )
            }
        }
    }

    if (showPersonaPicker) {
        PersonaPickerBottomSheet(
            settings = settings,
            onDismissRequest = { showPersonaPicker = false }
        )
    }

    if (showPromptPresetsSheet) {
        PromptPresetsBottomSheet(
            settings = settings,
            conversation = conversation,
            onUpdateConversation = onUpdateConversation,
            onDismissRequest = { showPromptPresetsSheet = false }
        )
    }

    if (showContextBudgetSheet) {
        me.lucid.domchat.ui.components.chat.ContextBudgetBottomSheet(
            settings = settings,
            perChatBudget = conversation.perChatContextBudget,
            isPerChatMode = true,
            onSavePerChatBudget = { newBudget ->
                onUpdateConversation(conversation.copy(perChatContextBudget = newBudget))
            },
            onDismissRequest = { showContextBudgetSheet = false }
        )
    }

    if (showCreatorNotesSheet) {
        me.lucid.domchat.ui.components.chat.CharacterNotesBottomSheet(
            assistant = assistant,
            onDismissRequest = { showCreatorNotesSheet = false }
        )
    }

    if (showStreamingAnimationSheet) {
        val currentUi = assistant.uiSettings
        var fadeInDraft by remember {
            mutableStateOf(currentUi.enableStreamingFadeIn ?: settings.displaySetting.enableStreamingFadeIn)
        }
        var continuousDraft by remember {
            mutableStateOf(currentUi.streamingContinuousFlow ?: settings.displaySetting.streamingContinuousFlow)
        }
        var durationDraft by remember {
            mutableFloatStateOf((currentUi.streamingFadeDurationMs ?: settings.displaySetting.streamingFadeDurationMs).toFloat())
        }
        var startAlphaDraft by remember {
            mutableFloatStateOf(currentUi.streamingFadeStartAlpha ?: settings.displaySetting.streamingFadeStartAlpha)
        }

        AetherInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showStreamingAnimationSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AI Streaming Animation",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Customize how AI replies appear during streaming: fade in smoothly vs snapping word by word.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smooth Fade-in Animation", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        Text("Fade text smoothly from transparency", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    me.lucid.domchat.ui.components.ui.HapticSwitch(
                        checked = fadeInDraft,
                        onCheckedChange = { fadeInDraft = it }
                    )
                }

                if (fadeInDraft) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Continuous Fluid Stream", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                            Text("Flow text continuously rather than waiting for word chunks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        me.lucid.domchat.ui.components.ui.HapticSwitch(
                            checked = continuousDraft,
                            onCheckedChange = { continuousDraft = it }
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fade Duration", style = MaterialTheme.typography.bodyMedium)
                            Text("${durationDraft.toInt()} ms", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = durationDraft,
                            onValueChange = { durationDraft = it },
                            valueRange = 100f..1000f,
                            steps = 17
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Starting Opacity", style = MaterialTheme.typography.bodyMedium)
                            Text("${(startAlphaDraft * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = startAlphaDraft,
                            onValueChange = { startAlphaDraft = it },
                            valueRange = 0f..0.8f,
                            steps = 15
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val newUi = currentUi.copy(
                                enableStreamingFadeIn = fadeInDraft,
                                streamingContinuousFlow = continuousDraft,
                                streamingFadeDurationMs = durationDraft.toInt(),
                                streamingFadeStartAlpha = kotlin.math.round(startAlphaDraft * 100f) / 100f
                            )
                            onUpdateAssistant(assistant.copy(uiSettings = newUi))
                            showStreamingAnimationSheet = false
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }

    if (showAuthorsNoteSheet) {
        var noteDraft by remember { mutableStateOf(assistant.authorsNote.orEmpty()) }
        var depthDraft by remember { mutableFloatStateOf(assistant.authorsNoteDepth.toFloat()) }
        AetherInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showAuthorsNoteSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Author's Note",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Injected into context depth to guide tone, scenario, or persona behavior.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    label = { Text("Author's Note Directive") },
                    placeholder = { Text("[Writing style: vivid, slow-burn, emotional]") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 220.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Injection Depth",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${depthDraft.toInt()} messages from bottom",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = depthDraft,
                        onValueChange = { depthDraft = it },
                        valueRange = 0f..10f,
                        steps = 9
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showAuthorsNoteSheet = false }) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onUpdateAssistant(
                                assistant.copy(
                                    authorsNote = noteDraft.trim().takeIf { it.isNotEmpty() },
                                    authorsNoteDepth = depthDraft.toInt()
                                )
                            )
                            showAuthorsNoteSheet = false
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showSamplersSheet) {
        var tempDraft by remember { mutableFloatStateOf(assistant.temperature ?: 1.0f) }
        var minPDraft by remember { mutableFloatStateOf(assistant.minP ?: 0.0f) }
        var topPDraft by remember { mutableFloatStateOf(assistant.topP ?: 1.0f) }
        var freqDraft by remember { mutableFloatStateOf(assistant.frequencyPenalty ?: 0.0f) }
        var presDraft by remember { mutableFloatStateOf(assistant.presencePenalty ?: 0.0f) }
        var repDraft by remember { mutableFloatStateOf(assistant.repetitionPenalty ?: 1.0f) }

        AetherInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showSamplersSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Samplers & Generation",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Temperature
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Temperature", style = MaterialTheme.typography.bodyMedium)
                        Text(String.format(java.util.Locale.US, "%.2f", tempDraft), color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = tempDraft, onValueChange = { tempDraft = it }, valueRange = 0.0f..2.0f)
                }

                // Min-P
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Min-P Sampling", style = MaterialTheme.typography.bodyMedium)
                        Text(String.format(java.util.Locale.US, "%.2f", minPDraft), color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = minPDraft, onValueChange = { minPDraft = it }, valueRange = 0.0f..1.0f)
                }

                // Top-P
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Top-P", style = MaterialTheme.typography.bodyMedium)
                        Text(String.format(java.util.Locale.US, "%.2f", topPDraft), color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = topPDraft, onValueChange = { topPDraft = it }, valueRange = 0.0f..1.0f)
                }

                // Frequency Penalty
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Frequency Penalty", style = MaterialTheme.typography.bodyMedium)
                        Text(String.format(java.util.Locale.US, "%.2f", freqDraft), color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = freqDraft, onValueChange = { freqDraft = it }, valueRange = -2.0f..2.0f)
                }

                // Presence Penalty
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Presence Penalty", style = MaterialTheme.typography.bodyMedium)
                        Text(String.format(java.util.Locale.US, "%.2f", presDraft), color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = presDraft, onValueChange = { presDraft = it }, valueRange = -2.0f..2.0f)
                }

                // Repetition Penalty
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Repetition Penalty", style = MaterialTheme.typography.bodyMedium)
                        Text(String.format(java.util.Locale.US, "%.2f", repDraft), color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = repDraft, onValueChange = { repDraft = it }, valueRange = 1.0f..2.0f)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showSamplersSheet = false }) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onUpdateAssistant(
                                assistant.copy(
                                    temperature = tempDraft,
                                    minP = minPDraft.takeIf { it > 0f },
                                    topP = topPDraft,
                                    frequencyPenalty = freqDraft.takeIf { it != 0f },
                                    presencePenalty = presDraft.takeIf { it != 0f },
                                    repetitionPenalty = repDraft.takeIf { it != 1f }
                                )
                            )
                            showSamplersSheet = false
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }

    if (showWallpaperSheet) {
        val convId = conversation.id.toString()
        val existingConfig = settings.displaySetting.conversationWallpapers[convId]
        var chatBgDraft by remember(existingConfig) { mutableStateOf(existingConfig?.backgroundUri ?: assistant.background) }
        var blurDraft by remember(existingConfig) { mutableFloatStateOf(existingConfig?.backgroundBlur ?: assistant.backgroundBlur) }
        var dimDraft by remember(existingConfig) { mutableFloatStateOf(existingConfig?.backgroundDim ?: assistant.backgroundDim) }
        var blurModeDraft by remember(existingConfig) { mutableStateOf(existingConfig?.blurMode ?: settings.displaySetting.backgroundBlurMode) }
        var blurHeightFractionDraft by remember(existingConfig) { mutableFloatStateOf(existingConfig?.blurHeightFraction ?: 0.75f) }

        AetherInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showWallpaperSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Chat Wallpaper & Effects",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            wallpaperPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text("Choose Wallpaper")
                    }
                    if (chatBgDraft != null) {
                        OutlinedButton(
                            onClick = {
                                chatBgDraft = null
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }

                // Blur Mode: Full Image vs Bottom Fade
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Blur Mode", style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { blurModeDraft = me.lucid.domchat.data.model.BackgroundBlurMode.FULL },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (blurModeDraft == me.lucid.domchat.data.model.BackgroundBlurMode.FULL) MaterialTheme.colorScheme.primaryContainer else domChatCardContainerColor(),
                            contentColor = if (blurModeDraft == me.lucid.domchat.data.model.BackgroundBlurMode.FULL) MaterialTheme.colorScheme.onPrimaryContainer else if (isDarkMode) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurface,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, if (blurModeDraft == me.lucid.domchat.data.model.BackgroundBlurMode.FULL) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f))
                        ) {
                            Text(
                                "Full Image",
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }

                        Surface(
                            onClick = { blurModeDraft = me.lucid.domchat.data.model.BackgroundBlurMode.BOTTOM_FADE },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (blurModeDraft == me.lucid.domchat.data.model.BackgroundBlurMode.BOTTOM_FADE) MaterialTheme.colorScheme.primaryContainer else domChatCardContainerColor(),
                            contentColor = if (blurModeDraft == me.lucid.domchat.data.model.BackgroundBlurMode.BOTTOM_FADE) MaterialTheme.colorScheme.onPrimaryContainer else if (isDarkMode) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurface,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, if (blurModeDraft == me.lucid.domchat.data.model.BackgroundBlurMode.BOTTOM_FADE) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f))
                        ) {
                            Text(
                                "Bottom Fade",
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                // Blur slider (expanded up to 100dp)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Background Blur", style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface)
                        Text("${blurDraft.toInt()} dp", color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = blurDraft,
                        onValueChange = { blurDraft = it },
                        valueRange = 0f..100f
                    )
                }

                // If Bottom Fade, show Blur Height Reach slider
                if (blurModeDraft == me.lucid.domchat.data.model.BackgroundBlurMode.BOTTOM_FADE) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Blur Height Reach", style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface)
                            Text("${(blurHeightFractionDraft * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = blurHeightFractionDraft,
                            onValueChange = { blurHeightFractionDraft = it },
                            valueRange = 0.05f..1.0f
                        )
                    }
                }

                // Dim slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Background Dim", style = MaterialTheme.typography.bodyMedium)
                        Text("${(dimDraft * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = dimDraft,
                        onValueChange = { dimDraft = it },
                        valueRange = 0f..0.95f
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val updatedConfig = me.lucid.domchat.data.datastore.ConversationWallpaperConfig(
                                backgroundUri = chatBgDraft,
                                backgroundBlur = blurDraft,
                                backgroundDim = dimDraft,
                                blurMode = blurModeDraft,
                                blurHeightFraction = blurHeightFractionDraft,
                            )
                            scope.launch {
                                settingsStore.update { cur ->
                                    val updatedMap = cur.displaySetting.conversationWallpapers.toMutableMap()
                                    if (chatBgDraft.isNullOrBlank()) {
                                        updatedMap.remove(convId)
                                    } else {
                                        updatedMap[convId] = updatedConfig
                                    }
                                    cur.copy(
                                        displaySetting = cur.displaySetting.copy(
                                            conversationWallpapers = updatedMap
                                        )
                                    )
                                }
                            }
                            showWallpaperSheet = false
                        }
                    ) {
                        Text("Save for this chat")
                    }
                }
            }
        }
    }

    if (showReasoningPicker) {
        ReasoningPicker(
            model = currentChatModel,
            reasoningTokens = assistant.thinkingBudget ?: 0,
            onDismissRequest = { showReasoningPicker = false },
            onUpdateReasoningTokens = { tokens ->
                onUpdateAssistant(assistant.copy(thinkingBudget = tokens))
                showReasoningPicker = false
            }
        )
    }

    if (showSkillsPicker) {
        SkillsPickerSheet(
            settings = settings,
            assistant = assistant,
            conversation = conversation,
            onUpdateConversation = onUpdateConversation,
            onDismiss = { showSkillsPicker = false }
        )
    }

    if (showLorebooksPicker) {
        LorebooksPickerSheet(
            settings = settings,
            assistant = assistant,
            conversation = conversation,
            onUpdateConversation = onUpdateConversation,
            onNavigateToLorebook = { lorebookId ->
                showLorebooksPicker = false
                onNavigateToLorebook(lorebookId)
            },
            onDismiss = { showLorebooksPicker = false }
        )
    }

    if (showPluginsPicker) {
        PluginsPickerSheet(
            settings = settings,
            assistant = assistant,
            onUpdateAssistant = onUpdateAssistant,
            onDismiss = { showPluginsPicker = false },
        )
    }

    if (showSearchPicker) {
        AetherInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showSearchPicker = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.search_picker_title),
                    style = MaterialTheme.typography.titleLarge
                )
                
                SearchPicker(
                    enableSearch = enableSearch,
                    settings = settings,
                    model = currentChatModel,
                    preferBuiltInSearch = assistant.preferBuiltInSearch,
                    onTogglePreferBuiltInSearch = { enabled ->
                        onUpdateAssistant(assistant.copy(preferBuiltInSearch = enabled))
                    },
                    onToggleSearch = onToggleSearch,
                    onUpdateSearchService = onUpdateSearchService,
                    onDismiss = { showSearchPicker = false }
                )
            }
        }
    }
}

@Composable
private fun AttachmentImportAction(
    isImporting: Boolean,
    modifier: Modifier = Modifier.size(36.dp),
    idleContent: @Composable () -> Unit,
) {
    AnimatedContent(
        targetState = isImporting,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(tween(150)) + scaleIn(tween(250), initialScale = 0.6f)) togetherWith
                (fadeOut(tween(150)) + scaleOut(tween(250), targetScale = 0.6f))
        },
        contentAlignment = Alignment.Center,
        label = "AttachmentImportAction",
    ) { importing ->
        if (importing) {
            ContainedLoadingIndicator(modifier = Modifier.fillMaxSize())
        } else {
            idleContent()
        }
    }
}

@Composable
private fun WorkspaceRequiredCard(
    fileName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = AppShapes.CardMedium,
        color = domChatCardContainerColor(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderBrush),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.workspace_required_card_title, fileName),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.workspace_required_card_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CharacterQuestionsCard(
    questionnaire: AskUserQuestionnaire,
    currentIndex: Int,
    selectedOptionLabel: String?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    onSelectOption: (AskUserOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = questionnaire.questions.getOrNull(currentIndex) ?: return
    val canGoPrevious = currentIndex > 0
    val canGoNext = currentIndex < questionnaire.questions.lastIndex

    Surface(
        shape = AppShapes.CardMedium,
        color = domChatCardContainerColor(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderBrush),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPrevious,
                        enabled = canGoPrevious,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.previous),
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.character_questions_progress,
                            currentIndex + 1,
                            questionnaire.questions.size
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onNext,
                        enabled = canGoNext,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.next),
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.banner_dismiss),
                    )
                }
            }

            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium
            )

            question.options.forEach { option ->
                CharacterQuestionOptionRow(
                    option = option,
                    selected = selectedOptionLabel == option.label,
                    onClick = { onSelectOption(option) }
                )
            }
        }
    }
}

@Composable
private fun CharacterQuestionOptionRow(
    option: AskUserOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberPremiumHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "question_option_scale"
    )

    Surface(
        onClick = {
            haptics.perform(HapticPattern.Pop)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            option.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class PendingToolApproval(
    val toolCallId: String,
    val toolName: String,
    val arguments: String,
) {
    val isWorkspaceTool: Boolean
        get() = toolName.startsWith("workspace_")
}

private fun List<me.lucid.ai.ui.UIMessage>.findPendingToolApproval(): PendingToolApproval? {
    return firstNotNullOfOrNull { message ->
        message.getToolCalls().firstOrNull { toolCall ->
            toolCall.toolName != ASK_USER_TOOL_NAME &&
                toolCall.approvalState is ToolApprovalState.Pending
        }?.let { toolCall ->
            PendingToolApproval(
                toolCallId = toolCall.toolCallId,
                toolName = toolCall.toolName,
                arguments = toolCall.arguments,
            )
        }
    }
}

@Composable
private fun ToolApprovalCard(
    approval: PendingToolApproval,
    onApprove: () -> Unit,
    onAlwaysApproveWorkspace: () -> Unit,
    onDenyWithInstruction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = AppShapes.CardMedium,
        color = domChatCardContainerColor(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderBrush),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tool_approval_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = approval.displayName(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val summary = approval.summary()
            if (summary.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            ToolApprovalActionRow(
                label = stringResource(R.string.tool_approval_approve),
                description = stringResource(R.string.tool_approval_approve_desc),
                icon = Icons.Rounded.Check,
                selected = true,
                onClick = onApprove,
            )
            if (approval.isWorkspaceTool) {
                ToolApprovalActionRow(
                    label = stringResource(R.string.tool_approval_always_workspace),
                    description = stringResource(R.string.tool_approval_always_workspace_desc),
                    icon = Icons.Rounded.Save,
                    selected = false,
                    onClick = onAlwaysApproveWorkspace,
                )
            }
            ToolApprovalActionRow(
                label = stringResource(R.string.tool_approval_deny_with_instruction),
                description = stringResource(R.string.tool_approval_deny_with_instruction_desc),
                icon = Icons.Rounded.Edit,
                selected = false,
                onClick = onDenyWithInstruction,
            )
        }
    }
}

@Composable
private fun ToolApprovalActionRow(
    label: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberPremiumHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "tool_approval_action_scale"
    )

    Surface(
        onClick = {
            haptics.perform(HapticPattern.Pop)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PendingToolApproval.displayName(): String = when (toolName) {
    "workspace_shell" -> stringResource(R.string.activity_timeline_tool_workspace_shell)
    "workspace_read_file" -> stringResource(R.string.activity_timeline_tool_workspace_read_file)
    "workspace_write_file" -> stringResource(R.string.activity_timeline_tool_workspace_write_file)
    "workspace_edit_file" -> stringResource(R.string.activity_timeline_tool_workspace_edit_file)
    "workspace_view_image" -> stringResource(R.string.activity_timeline_tool_workspace_view_image)
    else -> toolName.replace("_", " ").replaceFirstChar { it.uppercase() }
}

private fun PendingToolApproval.summary(): String {
    val args = runCatching { JsonInstantPretty.parseToJsonElement(arguments).jsonObject }.getOrNull()
    val key = when (toolName) {
        "workspace_shell" -> "command"
        "workspace_read_file", "workspace_write_file", "workspace_edit_file", "workspace_view_image" -> "path"
        else -> null
    }
    return key?.let { args?.get(it)?.jsonPrimitiveOrNull?.contentOrNull }
        ?: arguments.take(240)
}

@Composable
private fun MinimalPickerContent(
    state: ChatInputState,
    conversation: Conversation,
    settings: Settings,
    assistant: Assistant,
    currentChatModel: Model?,
    cameraPermission: me.lucid.domchat.ui.components.ui.permission.PermissionState,
    enableSearch: Boolean,
    onToggleSearch: (Boolean) -> Unit,
    onUpdateChatModel: (Model) -> Unit,
    onUpdateConversation: (Conversation) -> Unit,
    onUpdateAssistant: (Assistant) -> Unit,
    onUpdateSearchService: (Int) -> Unit,
    onNavigateToLorebook: (String) -> Unit,
    onRefreshContext: suspend () -> ChatService.ContextRefreshResult,
    importScope: CoroutineScope,
    onAttachmentImportStarted: () -> Unit,
    onAttachmentImportFinished: () -> Unit,
    onOpenModelPicker: () -> Unit = {},
    onOpenPersonaPicker: () -> Unit = {},
    onOpenPromptPresets: () -> Unit = {},
    onOpenContextBudget: () -> Unit = {},
    onOpenAuthorsNote: () -> Unit = {},
    onOpenSamplers: () -> Unit = {},
    onOpenWallpaper: () -> Unit = {},
    onOpenCreatorNotes: () -> Unit = {},
    onOpenStreamingAnimation: () -> Unit = {},
    onOpenSearchPicker: () -> Unit = {},
    onOpenReasoningPicker: () -> Unit = {},
    onOpenSkillsPicker: () -> Unit = {},
    onOpenLorebooksPicker: () -> Unit = {},
    onOpenPluginsPicker: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val availableSkills = remember(settings.skills) {
        settings.skills
    }
    val availableSkillIds = remember(availableSkills) { availableSkills.map { it.id }.toSet() }
    val assistantAvailableSkillIds = remember(settings.skills, assistant.id) {
        settings.skills.filter { it.isAvailableForAssistant(assistant.id) }.map { it.id }.toSet()
    }
    
    // OLED dark mode detection for buttons (not sheet backgrounds)
    val amoledMode by me.lucid.domchat.ui.hooks.rememberAmoledDarkMode()
    val isDarkMode = me.lucid.domchat.ui.theme.LocalDarkMode.current
    val isAmoled = amoledMode && isDarkMode
    // Sheet background uses surfaceContainerLow always
    val sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
    
    // Camera state
    var cameraOutputUri by remember { mutableStateOf<Uri?>(null) }
    var cameraOutputFile by remember { mutableStateOf<File?>(null) }
    
    var showContextRefreshDialog by remember { mutableStateOf(false) }
    var showContextSummaryEditDialog by remember { mutableStateOf(false) }
    var editableContextSummary by remember(conversation.contextSummary) {
        mutableStateOf(conversation.contextSummary.orEmpty())
    }
    val modelCatalogService: ModelCatalogService = koinInject()
    val settingsStore: me.lucid.domchat.data.datastore.SettingsStore = koinInject()
    val catalogSnapshot by modelCatalogService.snapshotFlow.collectAsStateWithLifecycle()
    val assistantDefaultSkillIds = assistant.enabledSkillIds.intersect(assistantAvailableSkillIds)
    val alwaysEnabledSkillIds = availableSkills
        .filter { it.alwaysEnabled && assistantAvailableSkillIds.contains(it.id) }
        .map { it.id }
        .toSet()
    val effectiveActiveSkillIds = if (conversation.enabledModeIds.hasManualSkillSelectionOverride() || conversation.enabledModeIds.isNotEmpty()) {
        conversation.enabledModeIds.withoutSkillSelectionOverride()
    } else {
        assistantDefaultSkillIds + alwaysEnabledSkillIds
    }.intersect(availableSkillIds)
    
    // Track the last valid search provider index so selection persists when search is disabled
    // Initialize from assistant's searchMode if available, otherwise use global setting
    val initialProviderIndex = when (val mode = assistant.searchMode) {
        is me.lucid.domchat.data.model.AssistantSearchMode.Provider -> mode.index
        else -> settings.searchServiceSelected.coerceAtLeast(0)
    }
    var lastValidProviderIndex by rememberSaveable(initialProviderIndex) { mutableStateOf(initialProviderIndex) }
    
    // Update lastValidProviderIndex when a valid external index is set
    val currentProviderIndex = when (val mode = assistant.searchMode) {
        is me.lucid.domchat.data.model.AssistantSearchMode.Provider -> mode.index
        else -> -1
    }
    // Sync immediately when currentProviderIndex changes (no LaunchedEffect delay to prevent flickering)
    if (currentProviderIndex >= 0 && currentProviderIndex < settings.searchServices.size && currentProviderIndex != lastValidProviderIndex) {
        lastValidProviderIndex = currentProviderIndex
    }
    
    // Calculate effective provider index (use tracked value when current is invalid)
    val effectiveProviderIndex = if (currentProviderIndex >= 0 && currentProviderIndex < settings.searchServices.size) {
        currentProviderIndex
    } else {
        lastValidProviderIndex.coerceIn(0, (settings.searchServices.size - 1).coerceAtLeast(0))
    }
    
    // Button shapes for grouped appearance (24dp outer corners, 10dp inner, matches floating toolbar)
    val leftButtonShape = RoundedCornerShape(topStart = 24.dp, topEnd = 10.dp, bottomStart = 24.dp, bottomEnd = 10.dp)
    val middleButtonShape = RoundedCornerShape(10.dp)
    val rightButtonShape = RoundedCornerShape(topStart = 10.dp, topEnd = 24.dp, bottomStart = 10.dp, bottomEnd = 24.dp)

    fun importImages(
        uris: List<Uri>,
        onFinally: () -> Unit = {}
    ) {
        if (uris.isEmpty()) {
            onFinally()
            return
        }

        onAttachmentImportStarted()
        importScope.launch {
            try {
                val importedUris = withContext(Dispatchers.IO) {
                    ChatAttachmentManager.importChatFiles(uris)
                }
                if (importedUris.isEmpty()) {
                    Log.w("MinimalChatInput", "Failed to import ${uris.size} selected image(s)")
                    toaster.show(context.getString(R.string.chat_input_selected_image_failed))
                } else {
                    state.addImages(importedUris)
                }
            } finally {
                onAttachmentImportFinished()
                onFinally()
            }
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { captureSuccessful ->
        val capturedUri = cameraOutputUri
        val capturedFile = cameraOutputFile
        if (captureSuccessful && capturedUri != null) {
            onDismiss()
            importImages(
                uris = listOf(capturedUri),
                onFinally = {
                    capturedFile?.delete()
                    cameraOutputFile = null
                    cameraOutputUri = null
                }
            )
        } else {
            cameraOutputFile?.delete()
            cameraOutputFile = null
            cameraOutputUri = null
        }
    }
    
    // Photo picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { selectedUris ->
        if (selectedUris.isNotEmpty()) {
            onDismiss()
            importImages(uris = selectedUris)
        }
    }
    
    // File picker launcher - categorizes files by type
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { selectedUris ->
        if (selectedUris.isNotEmpty()) {
            onDismiss()
            val isWorkspaceEnabled = assistant.workspaceId != null
            onAttachmentImportStarted()
            importScope.launch {
                try {
                    val importedFiles = withContext(Dispatchers.IO) {
                        context.prepareImportedPickerFiles(
                            selectedUris = selectedUris,
                            isWorkspaceEnabled = isWorkspaceEnabled,
                        )
                    }

                    importedFiles.unsupportedFileNames.forEach { fileName ->
                        toaster.show(
                            context.getString(
                                R.string.chat_input_unsupported_file_type,
                                fileName
                            )
                        )
                    }
                    importedFiles.failedFileNames.forEach { fileName ->
                        toaster.show(context.getString(R.string.chat_input_add_file_failed, fileName))
                    }

                    if (importedFiles.imageUris.isNotEmpty()) {
                        state.addImages(importedFiles.imageUris)
                    }
                    if (importedFiles.documents.isNotEmpty()) {
                        state.addFiles(importedFiles.documents)
                    }
                } finally {
                    onAttachmentImportFinished()
                }
            }
        }
    }
    
    val pickerTabs = listOf("Media", "Roleplay", "Models")
    val pagerState = rememberPagerState { pickerTabs.size }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF141520).copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                pickerTabs.forEachIndexed { index, tabTitle ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Color.White.copy(alpha = 0.16f) else Color.Transparent)
                            .clickable {
                                haptics.perform(HapticPattern.Tick)
                                importScope.launch { pagerState.animateScrollToPage(index) }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        ) { pageIndex ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (pageIndex) {
                0 -> {
                    // File upload buttons - grouped with corner shapes (no outer container)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Camera button - icon only, no label
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            PermissionManager(permissionState = cameraPermission) {
                                MinimalFileButtonGroupedIconOnly(
                                    icon = Icons.Rounded.CameraAlt,
                                    shape = leftButtonShape,
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = {
                                        if (cameraPermission.allRequiredPermissionsGranted) {
                                            cameraOutputFile = context.cacheDir.resolve("camera_${Uuid.random()}.jpg")
                                            cameraOutputUri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                cameraOutputFile!!
                                            )
                                            cameraLauncher.launch(cameraOutputUri!!)
                                        } else {
                                            cameraPermission.requestPermissions()
                                        }
                                    }
                                )
                            }
                        }
                        
                        // Photos button - icon only, no label
                        MinimalFileButtonGroupedIconOnly(
                            icon = Icons.Rounded.Image,
                            shape = middleButtonShape,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        )
                        
                        // Files button - icon only, no label
                        MinimalFileButtonGroupedIconOnly(
                            icon = Icons.Rounded.FolderOpen,
                            shape = rightButtonShape,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            }
                        )
                    }
                }
                1 -> {
                    // User Persona picker
                    val activePersona = settings.displaySetting.getActivePersona()
                    MinimalPickerItem(
                        icon = {
                            me.lucid.domchat.ui.components.ui.UIAvatar(
                                name = activePersona.name,
                                value = activePersona.avatar,
                                modifier = Modifier.size(28.dp).clip(CircleShape)
                            )
                        },
                        title = "User Persona: ${activePersona.name}",
                        subtitle = activePersona.description.ifBlank { "Switch active roleplay persona" },
                        onClick = onOpenPersonaPicker
                    )

                    // Prompt Presets picker
                    val activePresetsCount = (conversation.enabledPromptPresetIds ?: settings.displaySetting.globalEnabledPromptPresetIds).size
                    MinimalPickerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        title = "Prompt Presets",
                        subtitle = "$activePresetsCount active preset(s) for this chat",
                        onClick = onOpenPromptPresets
                    )

                    // SillyTavern Author's Note
                    val hasAuthorsNote = !assistant.authorsNote.isNullOrBlank()
                    MinimalPickerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (hasAuthorsNote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        title = "Author's Note",
                        subtitle = if (hasAuthorsNote) "Active (Depth ${assistant.authorsNoteDepth})" else "Inject scene guidance & directives",
                        onClick = onOpenAuthorsNote
                    )

                    if (settings.lorebooks.isNotEmpty()) {
                        // Lorebooks - show active count and blue icon when enabled
                        val lorebookIds = settings.lorebooks.map { it.id }.toSet()
                        val activeLorebookIds = (conversation.enabledLorebookIds ?: assistant.enabledLorebookIds).intersect(lorebookIds)
                        val activeLorebooksCount = activeLorebookIds.size
                        val lorebooksActive = activeLorebooksCount > 0
                        MinimalPickerItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (lorebooksActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            title = stringResource(R.string.minimal_input_lorebooks),
                            subtitle = if (activeLorebooksCount > 0) "$activeLorebooksCount active" else stringResource(R.string.minimal_input_lorebooks_desc),
                            onClick = onOpenLorebooksPicker
                        )
                    }

                    // Chat Wallpaper & Effects
                    val hasCustomBg = assistant.background != null
                    MinimalPickerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Image,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (hasCustomBg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        title = "Wallpaper & Effects",
                        subtitle = if (hasCustomBg) "Custom Wallpaper · Blur ${assistant.backgroundBlur.toInt()}dp" else "Default / Global Wallpaper",
                        onClick = onOpenWallpaper
                    )

                    // Character Creator Notes
                    if (!assistant.creatorNotes.isNullOrBlank()) {
                        MinimalPickerItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            title = "Creator Notes",
                            subtitle = "View author lore & character instructions",
                            onClick = onOpenCreatorNotes
                        )
                    }
                }
                2 -> {
                    // Model picker - uses actual model icon, full-width clickable
                    val currentModel = currentChatModel
                    val provider = currentModel?.findProvider(providers = settings.providers)
                    MinimalPickerItem(
                        icon = {
                            if (currentModel != null) {
                                me.lucid.domchat.ui.components.ui.ModelIcon(
                                    model = currentModel,
                                    provider = provider,
                                    modifier = Modifier.size(28.dp),
                                    color = androidx.compose.ui.graphics.Color.Transparent
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.ViewModule,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        title = currentModel?.displayName ?: "Select Model",
                        subtitle = currentModel?.modelId ?: "Choose a model to use",
                        onClick = { 
                            onOpenModelPicker()
                        }
                    )

                    // Context Window & Token Budget Allocation Sliders
                    val effectiveContextBudget = conversation.perChatContextBudget ?: settings.displaySetting.contextBudgetConfig
                    MinimalPickerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        title = "Context & Token Budget",
                        subtitle = "${effectiveContextBudget.maxContextTokens / 1024}k limit · ${effectiveContextBudget.chatHistoryBudgetPercent}% history · ${effectiveContextBudget.maxResponseTokens} output",
                        onClick = onOpenContextBudget
                    )

                    // Reasoning picker - only show if model has reasoning ability (same as floating toolbar)
                    if (currentModel?.abilities?.contains(me.lucid.ai.provider.ModelAbility.REASONING) == true) {
                        MinimalPickerItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            title = stringResource(R.string.minimal_input_thinking),
                            subtitle = stringResource(R.string.minimal_input_thinking_desc),
                            onClick = onOpenReasoningPicker
                        )
                    }

                    // Generation Samplers (Temperature, Min-P, Top-P, Penalties)
                    MinimalPickerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        title = "Samplers & Generation",
                        subtitle = "Temp: ${assistant.temperature ?: 1.0f} · Min-P: ${assistant.minP ?: 0.0f}",
                        onClick = onOpenSamplers
                    )

                    // AI Streaming Response Animation Settings
                    val streamingFadeEnabled = assistant.uiSettings.enableStreamingFadeIn ?: settings.displaySetting.enableStreamingFadeIn
                    val streamingFadeDuration = assistant.uiSettings.streamingFadeDurationMs ?: settings.displaySetting.streamingFadeDurationMs
                    MinimalPickerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (streamingFadeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        title = "Streaming Animation",
                        subtitle = if (streamingFadeEnabled) "Smooth Fade (${streamingFadeDuration}ms)" else "Instant / Pop",
                        onClick = onOpenStreamingAnimation
                    )

                    // Search picker - show selected provider if enabled (use effectiveProviderIndex to track current selection)
                    val searchService = settings.searchServices.getOrNull(effectiveProviderIndex)
                    val searchProviderName = if (searchService != null) {
                        SearchServiceOptions.TYPES[searchService::class]
                    } else null
                    
                    MinimalPickerItem(
                        icon = {
                            if (enableSearch && searchProviderName != null) {
                                SearchProviderIcon(
                                    name = searchProviderName,
                                    service = searchService,
                                    catalogSnapshot = catalogSnapshot,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (enableSearch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        title = if (enableSearch && searchProviderName != null) searchProviderName else stringResource(R.string.minimal_input_search),
                        subtitle = if (enableSearch) stringResource(R.string.web_search_enabled) else stringResource(R.string.minimal_input_search_desc),
                        onClick = onOpenSearchPicker
                    )
                    
                    if (settings.skills.isNotEmpty()) {
                        val activeSkills = availableSkills.filter { skill ->
                            effectiveActiveSkillIds.contains(skill.id)
                        }
                        val activeSkillsCount = activeSkills.size
                        val skillsActive = activeSkillsCount > 0
                        val singleActiveSkillIcon = activeSkills.singleOrNull()?.icon
                        MinimalPickerItem(
                            icon = {
                                Icon(
                                    imageVector = if (singleActiveSkillIcon != null) {
                                        ModeIcons.getIcon(singleActiveSkillIcon)
                                    } else {
                                        Icons.Rounded.Category
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (skillsActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            title = stringResource(R.string.minimal_input_skills),
                            subtitle = if (activeSkillsCount > 0) {
                                stringResource(R.string.skills_picker_active_count, activeSkillsCount)
                            } else {
                                stringResource(R.string.minimal_input_skills_desc)
                            },
                            onClick = onOpenSkillsPicker
                        )
                    }

                    if (settings.mcpServers.isNotEmpty()) {
                        val availablePluginIds = settings.mcpServers
                            .filter { it.commonOptions.enable }
                            .map { it.id }
                            .toSet()
                        val activePluginsCount = assistant.mcpServers.intersect(availablePluginIds).size
                        val pluginsActive = activePluginsCount > 0
                        MinimalPickerItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Extension,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (pluginsActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            title = stringResource(R.string.minimal_input_plugins),
                            subtitle = if (pluginsActive) {
                                stringResource(R.string.plugins_picker_active_count, activePluginsCount)
                            } else {
                                stringResource(R.string.minimal_input_plugins_desc)
                            },
                            onClick = onOpenPluginsPicker
                        )
                    }
                    
                    // Summarize button - show whenever there is enough history to summarize
                    if (assistant.canManuallySummarizeConversation(conversation.currentMessages.size)) {
                        MinimalPickerItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Summarize,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            title = stringResource(R.string.minimal_input_summarize),
                            subtitle = stringResource(R.string.minimal_input_summarize_desc),
                            onClick = {
                                showContextRefreshDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    }
    
    // Context Refresh dialog (same as floating toolbar)
    if (showContextRefreshDialog) {
        ContextRefreshDialog(
            conversation = conversation,
            onRefresh = onRefreshContext,
            onEditSummary = {
                editableContextSummary = conversation.contextSummary.orEmpty()
                showContextSummaryEditDialog = true
            },
            onRevertSummary = {
                onUpdateConversation(
                    conversation.copy(
                        contextSummary = null,
                        contextSummaryUpToIndex = -1,
                        lastRefreshTime = 0L,
                        updateAt = Instant.now()
                    )
                )
            },
            onDismiss = { showContextRefreshDialog = false }
        )
    }

    if (showContextSummaryEditDialog) {
        AlertDialog(
            onDismissRequest = { showContextSummaryEditDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            title = {
                Text(
                    text = stringResource(R.string.context_refresh_edit_summary),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                TextField(
                    value = editableContextSummary,
                    onValueChange = { editableContextSummary = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp),
                    placeholder = {
                        Text(stringResource(R.string.context_refresh_edit_summary_placeholder))
                    }
                )
            },
            confirmButton = {
                IconButton(
                    onClick = {
                        haptics.perform(HapticPattern.Success)
                        val trimmedSummary = editableContextSummary.trim()
                        onUpdateConversation(
                            conversation.copy(
                                contextSummary = trimmedSummary.takeIf { it.isNotBlank() },
                                contextSummaryUpToIndex = if (trimmedSummary.isBlank()) {
                                    -1
                                } else {
                                    conversation.contextSummaryUpToIndex
                                },
                                lastRefreshTime = if (trimmedSummary.isBlank()) 0L else System.currentTimeMillis(),
                                updateAt = Instant.now()
                            )
                        )
                        showContextSummaryEditDialog = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = stringResource(R.string.context_refresh_save_summary)
                    )
                }
            },
            dismissButton = {
                IconButton(
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        showContextSummaryEditDialog = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(android.R.string.cancel)
                    )
                }
            }
        )
    }
}

@Composable
private fun STTWaveformLine(
    amplitudes: List<Float>,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val count = 21
    val center = count / 2
    val historyNeeded = center + 1

    val bars = remember(amplitudes, active) {
        val source = amplitudes.takeLast(historyNeeded).reversed()
        List(count) { index ->
            val distFromCenter = kotlin.math.abs(index - center)
            if (source.isEmpty()) {
                if (active) 0.15f + ((distFromCenter % 3) * 0.05f) else 0.08f
            } else {
                val rawValue = source.getOrNull(distFromCenter) ?: 0.08f
                rawValue
            }
        }
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        bars.forEachIndexed { index, value ->
            val animatedHeight by animateFloatAsState(
                targetValue = value.coerceIn(0.08f, 1f),
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                label = "stt_waveform_$index",
            )
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = (4.dp + 20.dp * animatedHeight))
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = if (active) 0.85f else 0.45f)),
            )
        }
    }
}

@Composable
private fun MinimalFileButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = domChatCardContainerColor(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
        modifier = modifier.height(80.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Compact file button for use inside grouped container (24dp inner radius for optical roundness)
@Composable
private fun MinimalFileButtonCompact(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.CardMedium,  // Optically round with 40dp outer container
        color = domChatCardContainerColor(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
        modifier = modifier.height(72.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Grouped file button with custom shape for grouped appearance (same as floating toolbar)
@Composable
private fun MinimalFileButtonGrouped(
    icon: ImageVector,
    label: String,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // OLED-aware button color (same as floating toolbar's BigIconTextButton)
    val amoledMode by me.lucid.domchat.ui.hooks.rememberAmoledDarkMode()
    val isDarkMode = me.lucid.domchat.ui.theme.LocalDarkMode.current
    val isAmoled = amoledMode && isDarkMode
    val buttonColor = domChatCardContainerColor()
    
    Surface(
        onClick = onClick,
        shape = shape,
        color = buttonColor,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Grouped file button icon-only variant (no text label) for compact picker display
@Composable
private fun MinimalFileButtonGroupedIconOnly(
    icon: ImageVector,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDarkMode = me.lucid.domchat.ui.theme.LocalDarkMode.current
    val buttonColor = domChatCardContainerColor()
    
    Surface(
        onClick = onClick,
        shape = shape,
        color = buttonColor,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MinimalPickerItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark = me.lucid.domchat.ui.theme.LocalDarkMode.current
    val cardBg = domChatCardContainerColor()
    Surface(
        onClick = onClick,
        color = cardBg,
        contentColor = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class PendingImageCrop(
    val instanceId: String,
    val image: UIMessagePart.Image,
)

private data class IndexedAttachment<T : UIMessagePart>(
    val id: String,
    val part: T,
)

private fun UIMessagePart.attachmentUrl(): String? = when (this) {
    is UIMessagePart.Image -> url
    is UIMessagePart.Video -> url
    is UIMessagePart.Audio -> url
    is UIMessagePart.Document -> url
    else -> null
}

@Composable
private fun MediaFileInputRow(
    state: ChatInputState,
    onDelete: (Uri) -> Unit,
    onCropImage: (String, UIMessagePart.Image) -> Unit,
) {
    val images = remember(state.pendingAttachments) {
        state.pendingAttachments.mapNotNull { attachment ->
            (attachment.part as? UIMessagePart.Image)?.let { image ->
                IndexedAttachment(attachment.id, image)
            }
        }
    }
    val videos = remember(state.pendingAttachments) {
        state.pendingAttachments.mapNotNull { attachment ->
            (attachment.part as? UIMessagePart.Video)?.let { video ->
                IndexedAttachment(attachment.id, video)
            }
        }
    }
    val audios = remember(state.pendingAttachments) {
        state.pendingAttachments.mapNotNull { attachment ->
            (attachment.part as? UIMessagePart.Audio)?.let { audio ->
                IndexedAttachment(attachment.id, audio)
            }
        }
    }
    val documents = remember(state.pendingAttachments) {
        state.pendingAttachments.mapNotNull { attachment ->
            (attachment.part as? UIMessagePart.Document)?.let { document ->
                IndexedAttachment(attachment.id, document)
            }
        }
    }

    fun removePart(instanceId: String): Uri? {
        val removedPart = state.removeAttachment(instanceId) ?: return null
        val removedUrl = removedPart.attachmentUrl() ?: return null
        return if (state.messageContent.none { it.attachmentUrl() == removedUrl }) {
            removedUrl.toUri()
        } else {
            null
        }
    }

    DomChatComposerAttachmentRow {
        items(
            items = images,
            key = { attachment -> "image:${attachment.id}" }
        ) { attachment ->
            val image = attachment.part
            DomChatComposerImageAttachment(
                onClick = { onCropImage(attachment.id, image) },
                onRemove = { removePart(attachment.id)?.let(onDelete) },
                removeContentDescription = stringResource(R.string.delete),
            ) {
                AsyncImage(
                    model = image.url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        items(
            items = videos,
            key = { attachment -> "video:${attachment.id}" }
        ) { attachment ->
            DomChatComposerMediaAttachment(
                onRemove = { removePart(attachment.id)?.let(onDelete) },
            ) {
                DomChatComposerVideoIcon()
            }
        }
        items(
            items = audios,
            key = { attachment -> "audio:${attachment.id}" }
        ) { attachment ->
            DomChatComposerMediaAttachment(
                onRemove = { removePart(attachment.id)?.let(onDelete) },
            ) {
                DomChatComposerAudioIcon()
            }
        }
        items(
            items = documents,
            key = { attachment -> "document:${attachment.id}" }
        ) { attachment ->
            val document = attachment.part
            DomChatDocumentAttachmentTile(
                fileName = document.fileName,
                modifier = Modifier.size(60.dp),
                onRemove = {
                    removePart(attachment.id)?.let(onDelete)
                }
            )
        }
    }
}

@Composable
private fun ChatScrollToBottomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val haptics = rememberPremiumHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "scroll_to_bottom_scale"
    )

    Surface(
        shape = CircleShape,
        color = blurredContainerColor(MaterialTheme.colorScheme.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .size(36.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .domChatBlurEffect(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    onClick()
                },
            )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = stringResource(R.string.chat_page_scroll_to_bottom),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChatSuggestionsRow(
    modifier: Modifier = Modifier,
    suggestions: List<String>,
    onClickSuggestion: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    var pressedSuggestionIndex by remember { mutableStateOf<Int?>(null) }
    var selectedSuggestionIndex by remember { mutableStateOf<Int?>(null) }

    val canScrollLeft by remember { androidx.compose.runtime.derivedStateOf { scrollState.value > 0 } }
    val canScrollRight by remember { androidx.compose.runtime.derivedStateOf { scrollState.value < scrollState.maxValue } }
    val leftFadeAlpha by animateFloatAsState(
        targetValue = if (canScrollLeft) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(150),
        label = "left_fade"
    )
    val rightFadeAlpha by animateFloatAsState(
        targetValue = if (canScrollRight) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(150),
        label = "right_fade"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                if (leftFadeAlpha > 0f || rightFadeAlpha > 0f) {
                    val fadeWidthPx = 24.dp.toPx()
                    val leftEnd = (fadeWidthPx / size.width).coerceAtMost(0.4f)
                    val rightStart = (1f - fadeWidthPx / size.width).coerceAtLeast(0.6f)
                    val colorStops = arrayOf(
                        0f to Color.Black.copy(alpha = 1f - leftFadeAlpha),
                        leftEnd to Color.Black,
                        rightStart to Color.Black,
                        1f to Color.Black.copy(alpha = 1f - rightFadeAlpha)
                    )
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(colorStops = colorStops),
                        blendMode = androidx.compose.ui.graphics.BlendMode.DstIn
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
        suggestions.forEachIndexed { index, suggestion ->
            var visible by remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }
            val isInteractionPressed by interactionSource.collectIsPressedAsState()

            LaunchedEffect(isInteractionPressed) {
                if (isInteractionPressed) {
                    pressedSuggestionIndex = index
                } else if (pressedSuggestionIndex == index) {
                    pressedSuggestionIndex = null
                }
            }

            LaunchedEffect(suggestion) {
                kotlinx.coroutines.delay(index * 50L)
                visible = true
            }

            val isSelected = selectedSuggestionIndex == index
            val isPressed = pressedSuggestionIndex == index
            val isAnythingSelected = selectedSuggestionIndex != null
            val isAnythingPressed = pressedSuggestionIndex != null
            
            val targetScale = when {
                isSelected -> 1.05f
                isPressed -> 0.9f
                else -> 1f
            }
            
            val targetAlpha = when {
                isSelected -> 0f
                isAnythingSelected -> 0f
                isAnythingPressed && !isPressed -> 0.5f 
                visible -> 1f
                else -> 0f
            }

            val scale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                label = "suggestion_scale"
            )

            val alpha by animateFloatAsState(
                targetValue = targetAlpha,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                label = "suggestion_alpha"
            )
            
            LaunchedEffect(isSelected) {
                if (isSelected) {
                    kotlinx.coroutines.delay(200)
                    onClickSuggestion(suggestion)
                }
            }

            if (visible || targetAlpha > 0f) {
                val suggestionShape = RoundedCornerShape(16.dp)
                val isDark = me.lucid.domchat.ui.theme.LocalDarkMode.current
                val chipColor = if (isDark) Color(0xFF141522).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)
                val chipBorder = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
                Surface(
                    shape = suggestionShape,
                    color = chipColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder),
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .domChatBlurEffect(chipColor, suggestionShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            selectedSuggestionIndex = index
                        }
                ) {
                    Text(
                        text = suggestion,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun ExpandButtonOverlay(
    isVisible: Boolean,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200)),
        exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)),
        modifier = modifier
    ) {
        androidx.compose.material3.IconButton(
            onClick = onExpand,
            modifier = Modifier.size(36.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Rounded.Fullscreen,
                contentDescription = "Expand",
                modifier = Modifier.size(20.dp),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun findDialogWindow(view: View): Window? {
    var current: Any? = view
    while (current != null) {
        if (current is DialogWindowProvider) {
            return current.window
        }
        current = (current as? View)?.parent
    }
    return null
}
