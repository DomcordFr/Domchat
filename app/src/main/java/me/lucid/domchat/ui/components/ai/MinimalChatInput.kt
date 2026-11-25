package me.lucid.domchat.ui.components.ai

import me.lucid.domchat.ui.theme.liquidGlass
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.theme.liquidInteractiveGlass
import me.lucid.domchat.ui.motion.pressScale
import androidx.compose.foundation.border
import me.lucid.domchat.ui.modifier.fadeEdges
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
import androidx.compose.animation.animateContentSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import me.lucid.ai.ui.UIMessagePart
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.ui.zIndex
import androidx.compose.foundation.combinedClickable
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.verticalScroll
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material.icons.rounded.FormatItalic
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
import me.lucid.domchat.ui.modifier.domChatFloatingSurfaceColor
import me.lucid.domchat.ui.components.ui.DomChatSlider
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.components.ui.DomChatGlassChip
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContentColor
import me.lucid.domchat.ui.components.ui.ToastType
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Save
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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import me.lucid.domchat.ui.components.chat.LocalSheetDismiss
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
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
import me.lucid.domchat.ui.components.chat.RoundedStopIcon
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
    val conversationContext = remember(settings, conversation.id, conversation.assistantId) {
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
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
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
    val pendingQuestionnaire = remember(conversation.messageNodes, state.loading) {
        if (state.loading) null else conversation.currentMessages.findPendingAskUserToolCall()
    }
    val isQuestionnaireActive = pendingQuestionnaire != null
    val pendingToolApproval = remember(conversation.messageNodes, pendingQuestionnaire?.toolCallId, state.loading) {
        if (state.loading || pendingQuestionnaire != null) {
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
    var showTextStylingDialog by remember { mutableStateOf(false) }
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

    // Isolate WindowInsets.isImeVisible read in a leaf composable so opening the keyboard
    // never recomposes the entire MinimalChatInput tree.
    val focusManager = LocalFocusManager.current
    ImeFocusSyncEffect(
        onImeOpened = { showPicker = false },
        onImeClosed = { focusManager.clearFocus() },
    )
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

    var lastSendClickTimestamp by remember { mutableLongStateOf(0L) }

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
        val now = android.os.SystemClock.uptimeMillis()
        if (state.loading) {
            // Guard against rapid double click cancelling newly initiated response
            if (now - lastSendClickTimestamp < 1000L) {
                return
            }
            keyboardController?.hide()
            haptics.perform(HapticPattern.Send)
            onCancelClick()
        } else {
            // Guard against rapid double click sending duplicate message
            if (now - lastSendClickTimestamp < 800L) {
                return
            }
            lastSendClickTimestamp = now
            keyboardController?.hide()
            haptics.perform(HapticPattern.Send)
            onSendClick()
        }
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
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                .padding(bottom = bottomPadding, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val showScrollToBottom = !isQuestionnaireActive && !isToolApprovalActive && showScrollToBottomButton

            androidx.compose.animation.AnimatedVisibility(
                visible = showScrollToBottom,
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
                    Spacer(modifier = Modifier.weight(1f))
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
                        containerColor = Color.Transparent,
                        border = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                            null
                        } else {
                            BorderStroke(0.dp, Color.Transparent)
                        },
                        modifier = Modifier
                            .size(AppSize.ChromePill)
                            .then(
                                if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                                    Modifier.frostedGlassSurface(
                                        shape = CircleShape,
                                    )
                                } else {
                                    Modifier.liquidGlassSurface(
                                        shape = CircleShape,
                                        glassMode = settings.displaySetting.liquidGlassMode,
                                        isFloatingControl = true
                                    )
                                }
                            ),
                    ) {
                        Icon(
                            imageVector = if (sttRecording) RoundedStopIcon else Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                }
                // Text field capsule with embedded action button
                // Corner radius = 24dp (user confirmed this was correct)
                val inputShape = DomChatComposerInputShape
                DomChatComposerCapsule(
                    containerColor = Color.Transparent,
                    border = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                        BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                    } else {
                        null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = AppSize.ChromePill)
                        .then(
                            if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                                Modifier.frostedGlassSurface(
                                    shape = inputShape,
                                )
                            } else {
                                Modifier.liquidGlassSurface(
                                    shape = inputShape,
                                    glassMode = settings.displaySetting.liquidGlassMode,
                                    isFloatingControl = true
                                )
                            }
                        ),
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

                        // Editing indicator - shown when editing a message, animated in subtly with liquid glass
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isQuestionnaireActive && !isToolApprovalActive && state.isEditing(),
                            enter = fadeIn(spring(dampingRatio = 0.85f, stiffness = 300f)) +
                                    androidx.compose.animation.expandVertically(spring(dampingRatio = 0.85f, stiffness = 300f)),
                            exit = fadeOut(spring(dampingRatio = 0.85f, stiffness = 300f)) +
                                   androidx.compose.animation.shrinkVertically(spring(dampingRatio = 0.85f, stiffness = 300f)),
                        ) {
                            val badgeShape = RoundedCornerShape(14.dp)
                            Box(
                                modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .liquidGlassSurface(
                                            shape = badgeShape,
                                            fillColor = Color.Transparent,
                                            borderWidth = 0.5.dp,
                                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                        )
                                        .clip(badgeShape)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.editing),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White.copy(alpha = 0.90f),
                                            modifier = Modifier.padding(end = 4.dp)
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
                                                tint = Color.White.copy(alpha = 0.70f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
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
                                    color = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                                        blurredContainerColor(Color(0xFF000000))
                                    } else {
                                        Color.Transparent
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                                                Modifier.frostedGlassSurface(
                                                    shape = inputShape,
                                                )
                                            } else {
                                                Modifier.liquidGlassSurface(
                                                    shape = inputShape,
                                                    glassMode = settings.displaySetting.liquidGlassMode,
                                                )
                                            }
                                        )
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
                            val lineCount = androidx.compose.runtime.remember {
                                androidx.compose.runtime.derivedStateOf { visualLineCount }
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
                                                val charName = assistant.effectiveChatName.ifBlank { stringResource(R.string.assistant_page_default_assistant) }
                                                stringResource(R.string.minimal_chat_input_placeholder, charName)
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
                                    end = if ((sttRecording || sttFinalizing) && hasSelectedSttProvider) 150.dp else 96.dp,
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
                                // Compact liquid glass context ring for Chat info and Token usage popup
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Transparent)
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
                                            .background(Color.Transparent)
                                            .border(
                                                width = 0.8.dp,
                                                brush = LiquidGlassTokens.SpecularBorderSubtle,
                                                shape = CircleShape
                                            )
                                    )
                                }

                                AttachmentImportAction(
                                    isImporting = isImportingAttachments,
                                ) {
                                    val isComposerInputEmpty by remember(state) {
                                        derivedStateOf { state.isEmpty() }
                                    }
                                    val currentAction = when {
                                        isQuestionnaireActive && isFinalQuestion ->
                                            DomChatComposerAction.QuestionnaireSubmit
                                        isQuestionnaireActive -> DomChatComposerAction.QuestionnaireNext
                                        isToolApprovalActive -> DomChatComposerAction.ToolApprovalDeny
                                        state.loading -> DomChatComposerAction.Loading
                                        !isComposerInputEmpty -> DomChatComposerAction.Send
                                        else -> DomChatComposerAction.Picker
                                    }
                                DomChatComposerActionButton(
                                    action = currentAction,
                                    containerColorOverride = if (
                                        currentAction == DomChatComposerAction.Loading ||
                                        currentAction == DomChatComposerAction.Send ||
                                        currentAction == DomChatComposerAction.QuestionnaireSubmit
                                    ) Color.Transparent else null,
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
                                            val display = settings.displaySetting
                                            val stopBorderBrush = Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFFFF4D4D).copy(alpha = 0.50f),
                                                    Color(0xFF8B0000).copy(alpha = 0.25f),
                                                    Color.Transparent
                                                )
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .then(
                                                        if (display.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                                                            Modifier.frostedGlassSurface(
                                                                shape = CircleShape,
                                                                borderWidth = 0.5.dp,
                                                                specularBrush = stopBorderBrush,
                                                                fillColor = Color(0xFF5A0C0C).copy(alpha = 0.45f),
                                                            )
                                                        } else {
                                                            Modifier.liquidGlass(
                                                                shape = CircleShape,
                                                                blurRadius = display.glassBlur.dp,
                                                                refractionHeight = (display.glassDepthScale * 8f).dp,
                                                                refractionAmount = (display.glassDepthScale * 12f).dp,
                                                                depthEffect = true,
                                                                tintColor = Color.Transparent,
                                                                specularIntensity = display.glassSpecularIntensity,
                                                                borderWidth = 0.5.dp,
                                                                specularBrush = stopBorderBrush,
                                                                fillColor = Color(0xFF5A0C0C).copy(alpha = 0.45f),
                                                                isFloatingControl = true,
                                                            )
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(13.dp)
                                                        .clip(RoundedCornerShape(3.5.dp))
                                                        .background(Color(0xFFFF1E1E))
                                                )
                                            }
                                        }
                                        DomChatComposerAction.Send,
                                        DomChatComposerAction.QuestionnaireSubmit -> {
                                            val display = settings.displaySetting
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .then(
                                                        if (display.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                                                            Modifier.frostedGlassSurface(
                                                                shape = CircleShape,
                                                                borderWidth = 0.5.dp,
                                                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                                                fillColor = Color.Transparent,
                                                            )
                                                        } else {
                                                            Modifier.liquidGlass(
                                                                shape = CircleShape,
                                                                blurRadius = display.glassBlur.dp,
                                                                refractionHeight = (display.glassDepthScale * 5f).dp,
                                                                refractionAmount = (display.glassDepthScale * 8f).dp,
                                                                depthEffect = false,
                                                                tintColor = Color(display.glassTintColor),
                                                                specularIntensity = (display.glassSpecularIntensity * 0.55f).coerceIn(0f, 1f),
                                                                borderWidth = 0.5.dp,
                                                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                                                fillColor = Color.Transparent,
                                                                isFloatingControl = true,
                                                            )
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.ArrowUpward,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = Color.White,
                                                )
                                            }
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
                                                tint = Color(0xFFFF453A),
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
                                                    Color.White.copy(alpha = 0.65f)
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
                                            val charName = assistant.effectiveChatName.ifBlank { stringResource(R.string.assistant_page_default_assistant) }
                                            stringResource(R.string.minimal_chat_input_placeholder, charName)
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
                                        .size(28.dp)
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
                                            .background(Color.Transparent)
                                            .border(
                                                width = 0.8.dp,
                                                brush = LiquidGlassTokens.SpecularBorderSubtle,
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
                                        Color(0xFF000000)
                                    } else if (
                                        currentAction == DomChatComposerAction.Loading ||
                                        currentAction == DomChatComposerAction.Send ||
                                        currentAction == DomChatComposerAction.QuestionnaireSubmit
                                    ) {
                                        Color.Transparent
                                    } else {
                                        null
                                    },
                                ) { action ->
                                    when (action) {
                                        DomChatComposerAction.Loading -> {
                                            val display = settings.displaySetting
                                            val stopBorderBrush = Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFFFF4D4D).copy(alpha = 0.50f),
                                                    Color(0xFF8B0000).copy(alpha = 0.25f),
                                                    Color.Transparent
                                                )
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .then(
                                                        if (display.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                                                            Modifier.frostedGlassSurface(
                                                                shape = CircleShape,
                                                                borderWidth = 0.5.dp,
                                                                specularBrush = stopBorderBrush,
                                                                fillColor = Color(0xFF5A0C0C).copy(alpha = 0.45f),
                                                            )
                                                        } else {
                                                            Modifier.liquidGlass(
                                                                shape = CircleShape,
                                                                blurRadius = display.glassBlur.dp,
                                                                refractionHeight = (display.glassDepthScale * 8f).dp,
                                                                refractionAmount = (display.glassDepthScale * 12f).dp,
                                                                depthEffect = true,
                                                                tintColor = Color.Transparent,
                                                                specularIntensity = display.glassSpecularIntensity,
                                                                borderWidth = 0.5.dp,
                                                                specularBrush = stopBorderBrush,
                                                                fillColor = Color(0xFF5A0C0C).copy(alpha = 0.45f),
                                                                isFloatingControl = true,
                                                            )
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(13.dp)
                                                        .clip(RoundedCornerShape(3.5.dp))
                                                        .background(Color(0xFFFF1E1E))
                                                )
                                            }
                                        }
                                        DomChatComposerAction.Send,
                                        DomChatComposerAction.QuestionnaireSubmit -> {
                                            val display = settings.displaySetting
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .then(
                                                        if (display.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                                                            Modifier.frostedGlassSurface(
                                                                shape = CircleShape,
                                                                borderWidth = 0.5.dp,
                                                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                                                fillColor = Color.Transparent,
                                                            )
                                                        } else {
                                                            Modifier.liquidGlass(
                                                                shape = CircleShape,
                                                                blurRadius = display.glassBlur.dp,
                                                                refractionHeight = (display.glassDepthScale * 5f).dp,
                                                                refractionAmount = (display.glassDepthScale * 8f).dp,
                                                                depthEffect = false,
                                                                tintColor = Color(display.glassTintColor),
                                                                specularIntensity = (display.glassSpecularIntensity * 0.55f).coerceIn(0f, 1f),
                                                                borderWidth = 0.5.dp,
                                                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                                                fillColor = Color.Transparent,
                                                                isFloatingControl = true,
                                                            )
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.ArrowUpward,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = Color.White,
                                                )
                                            }
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
                                                tint = Color(0xFFFF453A),
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowUpward,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = if (isDarkMode) Color(0xFFF2F2F7) else Color.White.copy(alpha = 0.65f),
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
        DomChatInWindowBottomSheet(
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
                onOpenTextStyling = {
                    showPicker = false
                    showTextStylingDialog = true
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
        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showModelPickerSheet = false },
        ) {
            val sheetDismiss = LocalSheetDismiss.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DomChatSheetHeader(
                    title = "Select Model",
                    subtitle = "Choose AI model for current conversation",
                    onCloseClick = { sheetDismiss?.invoke() ?: run { showModelPickerSheet = false } }
                )
                ModelList(
                    currentModel = assistant.chatModelId ?: settings.chatModelId,
                    providers = filteredProviders,
                    modelType = me.lucid.ai.provider.ModelType.CHAT,
                    style = me.lucid.domchat.ui.components.ai.ModelPickerStyle.LIQUID_GLASS,
                    onSelect = { selectedModel: Model ->
                        onUpdateChatModel(selectedModel)
                        sheetDismiss?.invoke() ?: run { showModelPickerSheet = false }
                    },
                    onDismiss = {
                        sheetDismiss?.invoke() ?: run { showModelPickerSheet = false }
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

    if (showTextStylingDialog) {
        val currentRules = settings.displaySetting.rpStyleRules
        var expandedIndex by rememberSaveable { mutableStateOf<Int?>(null) }
        val existingCustom = currentRules.find { it.pattern !in listOf("\"", "*", "`") }
        var customSymbols by rememberSaveable { mutableStateOf(existingCustom?.pattern ?: "**") }

        fun parseColor(hex: String): Color {
            return try {
                val clean = hex.removePrefix("#")
                val c = clean.toLong(16)
                Color(
                    red = ((c shr 16) and 0xff) / 255f,
                    green = ((c shr 8) and 0xff) / 255f,
                    blue = (c and 0xff) / 255f,
                    alpha = 1f
                )
            } catch (_: Exception) {
                Color(0xFF808080)
            }
        }

        fun parseRgb(hex: String): Triple<Int, Int, Int> {
            return try {
                val clean = hex.removePrefix("#")
                val c = clean.toInt(16)
                Triple(
                    (c shr 16) and 0xff,
                    (c shr 8) and 0xff,
                    c and 0xff
                )
            } catch (_: Exception) {
                Triple(128, 128, 128)
            }
        }

        fun rgbToHex(r: Int, g: Int, b: Int): String =
            "#${r.coerceIn(0, 255).toString(16).padStart(2, '0')}${g.coerceIn(0, 255).toString(16).padStart(2, '0')}${b.coerceIn(0, 255).toString(16).padStart(2, '0')}"

        fun updateRule(pattern: String, newHex: String) {
            val normalizedHex = if (newHex.startsWith("#")) newHex.uppercase() else "#${newHex.uppercase()}"
            scope.launch {
                settingsStore.update { cur ->
                    val list = cur.displaySetting.rpStyleRules.toMutableList()
                    val idx = list.indexOfFirst { it.pattern == pattern }
                    if (idx >= 0) {
                        list[idx] = list[idx].copy(colorHex = normalizedHex, enabled = true)
                    } else {
                        list.add(me.lucid.domchat.data.datastore.RpStyleRule(pattern = pattern, colorHex = normalizedHex, enabled = true))
                    }
                    cur.copy(displaySetting = cur.displaySetting.copy(rpStyleRules = list))
                }
            }
        }

        fun resetToGlobal() {
            haptics.perform(HapticPattern.Pop)
            scope.launch {
                settingsStore.update { cur ->
                    cur.copy(displaySetting = cur.displaySetting.copy(rpStyleRules = emptyList()))
                }
            }
            toaster.show("Reset text styling to global defaults")
        }

        val styleDefs = listOf(
            Triple("Quote", "\"", "#808080"),
            Triple("Italic", "*", "#A0A0A0"),
            Triple("Code", "`", "#7EC8E3"),
            Triple("Custom", customSymbols.ifBlank { "**" }, "#E57373"),
        )

        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showTextStylingDialog = false },
            dragHandle = { DomChatSheetDragHandle() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                DomChatSheetHeader(
                    title = "Text Styling",
                    subtitle = "Customize appearance for italics & markdown",
                    onCloseClick = { showTextStylingDialog = false }
                )

                val styleScrollState = rememberScrollState()
                val canScrollUp by remember { derivedStateOf { styleScrollState.canScrollBackward } }
                val canScrollDown by remember { derivedStateOf { styleScrollState.canScrollForward } }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 36f)
                        .verticalScroll(styleScrollState)
                        .padding(top = 8.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap a style to configure its color and settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    DomChatGlassPillButton(
                        text = "Reset",
                        icon = Icons.Rounded.Refresh,
                        onClick = { resetToGlobal() },
                        modifier = Modifier.height(26.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Compact accordion cards
                styleDefs.forEachIndexed { idx, (label, pattern, defaultHex) ->
                    val isExpanded = expandedIndex == idx
                    val effectivePattern = if (idx == 3) customSymbols.ifBlank { "**" } else pattern
                    val activeRule = currentRules.find { it.pattern == effectivePattern }
                    val savedHex = (activeRule?.colorHex ?: defaultHex).uppercase()
                    val (savedR, savedG, savedB) = parseRgb(savedHex)
                    var draftR by remember(effectivePattern, savedHex) { mutableIntStateOf(savedR) }
                    var draftG by remember(effectivePattern, savedHex) { mutableIntStateOf(savedG) }
                    var draftB by remember(effectivePattern, savedHex) { mutableIntStateOf(savedB) }
                    var draftHex by remember(effectivePattern, savedHex) { mutableStateOf(savedHex) }
                    val currentColor = parseColor(draftHex)

                    // Auto-save whenever the user sets a valid color (live debounced + on leave)
                    val latestDraftHex by androidx.compose.runtime.rememberUpdatedState(draftHex)
                    val latestSavedHex by androidx.compose.runtime.rememberUpdatedState(savedHex)
                    LaunchedEffect(effectivePattern, draftHex) {
                        val hexDigits = draftHex.trim().removePrefix("#").uppercase()
                        if (hexDigits.length == 6 && hexDigits.all { it in "0123456789ABCDEF" }) {
                            val normalized = "#$hexDigits"
                            if (normalized != savedHex) {
                                kotlinx.coroutines.delay(150L)
                                updateRule(effectivePattern, normalized)
                            }
                        }
                    }
                    androidx.compose.runtime.DisposableEffect(effectivePattern) {
                        onDispose {
                            val hexDigits = latestDraftHex.trim().removePrefix("#").uppercase()
                            if (hexDigits.length == 6 && hexDigits.all { it in "0123456789ABCDEF" }) {
                                val normalized = "#$hexDigits"
                                if (normalized != latestSavedHex) {
                                    updateRule(effectivePattern, normalized)
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent,
                        border = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassSurface(
                                shape = RoundedCornerShape(14.dp),
                                fillColor = Color.Transparent,
                                borderWidth = 0.5.dp,
                                specularBrush = if (isExpanded) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptics.perform(HapticPattern.Pop)
                                        expandedIndex = if (isExpanded) null else idx
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(currentColor)
                                        .border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isExpanded) MaterialTheme.colorScheme.primary else Color.White
                                    )
                                    Text(
                                        text = if (idx == 3) "Symbol: $effectivePattern" else "Syntax: $pattern",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.55f)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .liquidGlassSurface(
                                            shape = RoundedCornerShape(6.dp),
                                            fillColor = Color.Transparent,
                                            borderWidth = 0.5.dp,
                                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = draftHex.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (isExpanded) {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                )

                                if (idx == 3) {
                                    OutlinedTextField(
                                        value = customSymbols,
                                        onValueChange = { newSym ->
                                            val oldSym = customSymbols
                                            customSymbols = newSym
                                            if (newSym.isNotBlank() && newSym !in listOf("\"", "*", "`")) {
                                                scope.launch {
                                                    settingsStore.update { cur ->
                                                        val list = cur.displaySetting.rpStyleRules.toMutableList()
                                                        val existingIdx = list.indexOfFirst { it.pattern == oldSym && it.pattern !in listOf("\"", "*", "`") }
                                                        if (existingIdx >= 0) {
                                                            list[existingIdx] = list[existingIdx].copy(pattern = newSym, enabled = true)
                                                        } else {
                                                            list.add(me.lucid.domchat.data.datastore.RpStyleRule(pattern = newSym, colorHex = draftHex, enabled = true))
                                                        }
                                                        cur.copy(displaySetting = cur.displaySetting.copy(rpStyleRules = list))
                                                    }
                                                }
                                            }
                                        },
                                        label = { Text("Custom symbols (e.g. ** or ~~)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedBorderColor = Color.White.copy(alpha = 0.35f),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            cursorColor = Color.White,
                                        )
                                    )
                                }

                                // Liquid glass Hex Code input with label positioned cleanly above
                                Text(
                                    text = "Hex Code",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color.White.copy(alpha = 0.72f),
                                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                                )
                                OutlinedTextField(
                                    value = draftHex,
                                    onValueChange = { rawInput ->
                                        val cleaned = rawInput.trim().uppercase().take(7)
                                        draftHex = cleaned
                                        val hexDigits = cleaned.removePrefix("#")
                                        if (hexDigits.length == 6 && hexDigits.all { it in "0123456789ABCDEF" }) {
                                            val normalized = "#$hexDigits"
                                            val (nr, ng, nb) = parseRgb(normalized)
                                            draftR = nr
                                            draftG = ng
                                            draftB = nb
                                            updateRule(effectivePattern, normalized)
                                        }
                                    },
                                    placeholder = { Text("#FFFFFF", color = Color.White.copy(alpha = 0.45f)) },
                                    singleLine = true,
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(currentColor)
                                                .border(0.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassSurface(
                                            shape = RoundedCornerShape(12.dp),
                                            fillColor = Color.Transparent,
                                            borderWidth = 0.5.dp,
                                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color.White,
                                    )
                                )
                                Spacer(Modifier.height(12.dp))

                                // Smooth RGB sliders with local drag state and commit on release
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Red Slider
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Red", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF5252), fontWeight = FontWeight.SemiBold)
                                        Text("$draftR", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                    DomChatSlider(
                                        value = draftR.toFloat(),
                                        onValueChange = { newR ->
                                            draftR = newR.toInt().coerceIn(0, 255)
                                            draftHex = rgbToHex(draftR, draftG, draftB).uppercase()
                                        },
                                        onValueChangeFinished = {
                                            updateRule(effectivePattern, rgbToHex(draftR, draftG, draftB))
                                        },
                                        valueRange = 0f..255f,
                                        activeTrackColorOverride = Color(0xFFFF5252),
                                        thumbColorOverride = Color(0xFFFF5252),
                                    )

                                    // Green Slider
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Green", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
                                        Text("$draftG", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                    DomChatSlider(
                                        value = draftG.toFloat(),
                                        onValueChange = { newG ->
                                            draftG = newG.toInt().coerceIn(0, 255)
                                            draftHex = rgbToHex(draftR, draftG, draftB).uppercase()
                                        },
                                        onValueChangeFinished = {
                                            updateRule(effectivePattern, rgbToHex(draftR, draftG, draftB))
                                        },
                                        valueRange = 0f..255f,
                                        activeTrackColorOverride = Color(0xFF4CAF50),
                                        thumbColorOverride = Color(0xFF4CAF50),
                                    )

                                    // Blue Slider
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Blue", style = MaterialTheme.typography.bodySmall, color = Color(0xFF448AFF), fontWeight = FontWeight.SemiBold)
                                        Text("$draftB", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                    DomChatSlider(
                                        value = draftB.toFloat(),
                                        onValueChange = { newB ->
                                            draftB = newB.toInt().coerceIn(0, 255)
                                            draftHex = rgbToHex(draftR, draftG, draftB).uppercase()
                                        },
                                        onValueChangeFinished = {
                                            updateRule(effectivePattern, rgbToHex(draftR, draftG, draftB))
                                        },
                                        valueRange = 0f..255f,
                                        activeTrackColorOverride = Color(0xFF448AFF),
                                        thumbColorOverride = Color(0xFF448AFF),
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

        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showStreamingAnimationSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                DomChatSheetHeader(
                    title = "AI Streaming Animation",
                    subtitle = "Customize response fade and streaming dynamics",
                    onCloseClick = { showStreamingAnimationSheet = false }
                )

                val animScrollState = rememberScrollState()
                val canScrollUp by remember { derivedStateOf { animScrollState.canScrollBackward } }
                val canScrollDown by remember { derivedStateOf { animScrollState.canScrollForward } }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 36f)
                        .verticalScroll(animScrollState)
                        .padding(top = 8.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                Text(
                    text = "Customize how AI replies appear during streaming: fade in smoothly vs snapping word by word.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smooth Fade-in Animation", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        Text("Fade text smoothly from transparency", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.65f))
                    }
                    me.lucid.domchat.ui.components.ui.DomChatGlassSwitch(
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
                            Text("Flow text continuously rather than waiting for word chunks", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.65f))
                        }
                        me.lucid.domchat.ui.components.ui.DomChatGlassSwitch(
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
                        DomChatSlider(
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
                        DomChatSlider(
                            value = startAlphaDraft,
                            onValueChange = { startAlphaDraft = it },
                            valueRange = 0f..0.8f,
                            steps = 15
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    }

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    DomChatGlassPillButton(
                        text = "Apply",
                        onClick = {
                            val newUi = currentUi.copy(
                                enableStreamingFadeIn = fadeInDraft,
                                streamingContinuousFlow = continuousDraft,
                                streamingFadeDurationMs = durationDraft.toInt(),
                                streamingFadeStartAlpha = kotlin.math.round(startAlphaDraft * 100f) / 100f,
                            )
                            onUpdateAssistant(assistant.copy(uiSettings = newUi))
                            showStreamingAnimationSheet = false
                        },
                        modifier = Modifier.height(36.dp)
                    )
                }
            }
        }
    }
    }

    if (showAuthorsNoteSheet) {
        var noteDraft by remember { mutableStateOf(assistant.authorsNote.orEmpty()) }
        var depthDraft by remember { mutableFloatStateOf(assistant.authorsNoteDepth.toFloat()) }
        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showAuthorsNoteSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
                    .imePadding()
            ) {
                DomChatSheetHeader(
                    title = "Author's Note",
                    subtitle = "Inject directives into conversation depth",
                    onCloseClick = { showAuthorsNoteSheet = false }
                )

                val noteScrollState = rememberScrollState()
                val canScrollUp by remember { derivedStateOf { noteScrollState.canScrollBackward } }
                val canScrollDown by remember { derivedStateOf { noteScrollState.canScrollForward } }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 36f)
                        .verticalScroll(noteScrollState)
                        .padding(top = 8.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Injected into context depth to guide tone, scenario, or persona behavior.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    val inputShape = RoundedCornerShape(14.dp)
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        label = { Text("Author's Note Directive", color = Color.White.copy(alpha = 0.7f)) },
                        placeholder = { Text("[Writing style: vivid, slow-burn, emotional]", color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 220.dp)
                            .liquidGlassSurface(
                                shape = inputShape,
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                fillColor = Color.Transparent,
                            ),
                        shape = inputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White,
                        )
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
                        DomChatSlider(
                            value = depthDraft,
                            onValueChange = { depthDraft = it },
                            valueRange = 0f..10f,
                            steps = 9
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DomChatGlassPillButton(
                            text = "Cancel",
                            onClick = { showAuthorsNoteSheet = false }
                        )
                        Spacer(Modifier.width(10.dp))
                        DomChatGlassPillButton(
                            text = "Save",
                            accent = true,
                            onClick = {
                                onUpdateAssistant(
                                    assistant.copy(
                                        authorsNote = noteDraft.trim().takeIf { it.isNotEmpty() },
                                        authorsNoteDepth = depthDraft.toInt()
                                    )
                                )
                                showAuthorsNoteSheet = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSamplersSheet) {
        var tempDraft by remember { mutableFloatStateOf(assistant.temperature ?: 1.0f) }
        var minPDraft by remember { mutableFloatStateOf(assistant.minP ?: 0.0f) }
        var topKDraft by remember { mutableFloatStateOf(assistant.topK?.toFloat() ?: 0.0f) }
        var topPDraft by remember { mutableFloatStateOf(assistant.topP ?: 1.0f) }
        var freqDraft by remember { mutableFloatStateOf(assistant.frequencyPenalty ?: 0.0f) }
        var presDraft by remember { mutableFloatStateOf(assistant.presencePenalty ?: 0.0f) }
        var repDraft by remember { mutableFloatStateOf(assistant.repetitionPenalty ?: 1.0f) }

        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showSamplersSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                DomChatSheetHeader(
                    title = "Samplers & Generation",
                    subtitle = "Fine-tune model temperature, Top-P, and penalties",
                    onCloseClick = { showSamplersSheet = false }
                )

                val samplerScrollState = rememberScrollState()
                val canScrollUp by remember { derivedStateOf { samplerScrollState.canScrollBackward } }
                val canScrollDown by remember { derivedStateOf { samplerScrollState.canScrollForward } }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 36f)
                        .verticalScroll(samplerScrollState)
                        .padding(top = 8.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                // Temperature
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(16.dp),
                            fillColor = Color(0xFF000000).copy(alpha = 0.40f),
                            borderWidth = 0.5.dp,
                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Temperature", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                modifier = Modifier.liquidGlassSurface(
                                    shape = RoundedCornerShape(10.dp),
                                    fillColor = Color.Transparent,
                                    borderWidth = 0.5.dp,
                                    specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                )
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.2f", tempDraft),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        DomChatSlider(
                            value = tempDraft,
                            onValueChange = {
                                if (kotlin.math.abs(it - tempDraft) >= 0.05f) haptics.perform(HapticPattern.Tick)
                                tempDraft = it
                            },
                            valueRange = 0.0f..2.0f
                        )
                    }
                }

                // Top-K (continuous track without dot ticks)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(16.dp),
                            fillColor = Color(0xFF000000).copy(alpha = 0.40f),
                            borderWidth = 0.5.dp,
                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Top-K", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            val topKInt = topKDraft.toInt()
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                modifier = Modifier.liquidGlassSurface(
                                    shape = RoundedCornerShape(10.dp),
                                    fillColor = Color.Transparent,
                                    borderWidth = 0.5.dp,
                                    specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                )
                            ) {
                                Text(
                                    text = if (topKInt <= 0) "Off (Default)" else "$topKInt",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        DomChatSlider(
                            value = topKDraft,
                            onValueChange = {
                                if (it.toInt() != topKDraft.toInt()) haptics.perform(HapticPattern.Tick)
                                topKDraft = it
                            },
                            valueRange = 0.0f..100.0f
                        )
                    }
                }

                // Min-P
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(16.dp),
                            fillColor = Color(0xFF000000).copy(alpha = 0.40f),
                            borderWidth = 0.5.dp,
                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Min-P Sampling", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                modifier = Modifier.liquidGlassSurface(
                                    shape = RoundedCornerShape(10.dp),
                                    fillColor = Color.Transparent,
                                    borderWidth = 0.5.dp,
                                    specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                )
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.2f", minPDraft),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        DomChatSlider(
                            value = minPDraft,
                            onValueChange = {
                                if (kotlin.math.abs(it - minPDraft) >= 0.05f) haptics.perform(HapticPattern.Tick)
                                minPDraft = it
                            },
                            valueRange = 0.0f..1.0f
                        )
                    }
                }

                // Top-P
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(16.dp),
                            fillColor = Color(0xFF000000).copy(alpha = 0.40f),
                            borderWidth = 0.5.dp,
                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Top-P", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                modifier = Modifier.liquidGlassSurface(
                                    shape = RoundedCornerShape(10.dp),
                                    fillColor = Color.Transparent,
                                    borderWidth = 0.5.dp,
                                    specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                )
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.2f", topPDraft),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        DomChatSlider(
                            value = topPDraft,
                            onValueChange = {
                                if (kotlin.math.abs(it - topPDraft) >= 0.05f) haptics.perform(HapticPattern.Tick)
                                topPDraft = it
                            },
                            valueRange = 0.0f..1.0f
                        )
                    }
                }

                // Frequency Penalty
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(16.dp),
                            fillColor = Color(0xFF000000).copy(alpha = 0.40f),
                            borderWidth = 0.5.dp,
                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Frequency Penalty", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                modifier = Modifier.liquidGlassSurface(
                                    shape = RoundedCornerShape(10.dp),
                                    fillColor = Color.Transparent,
                                    borderWidth = 0.5.dp,
                                    specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                )
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.2f", freqDraft),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        DomChatSlider(
                            value = freqDraft,
                            onValueChange = {
                                if (kotlin.math.abs(it - freqDraft) >= 0.1f) haptics.perform(HapticPattern.Tick)
                                freqDraft = it
                            },
                            valueRange = -2.0f..2.0f
                        )
                    }
                }

                // Presence Penalty
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(16.dp),
                            fillColor = Color(0xFF000000).copy(alpha = 0.40f),
                            borderWidth = 0.5.dp,
                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Presence Penalty", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                modifier = Modifier.liquidGlassSurface(
                                    shape = RoundedCornerShape(10.dp),
                                    fillColor = Color.Transparent,
                                    borderWidth = 0.5.dp,
                                    specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                )
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.2f", presDraft),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        DomChatSlider(
                            value = presDraft,
                            onValueChange = {
                                if (kotlin.math.abs(it - presDraft) >= 0.1f) haptics.perform(HapticPattern.Tick)
                                presDraft = it
                            },
                            valueRange = -2.0f..2.0f
                        )
                    }
                }

                // Repetition Penalty
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(16.dp),
                            fillColor = Color(0xFF000000).copy(alpha = 0.40f),
                            borderWidth = 0.5.dp,
                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Repetition Penalty", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                modifier = Modifier.liquidGlassSurface(
                                    shape = RoundedCornerShape(10.dp),
                                    fillColor = Color.Transparent,
                                    borderWidth = 0.5.dp,
                                    specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                )
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.2f", repDraft),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        DomChatSlider(
                            value = repDraft,
                            onValueChange = {
                                if (kotlin.math.abs(it - repDraft) >= 0.05f) haptics.perform(HapticPattern.Tick)
                                repDraft = it
                            },
                            valueRange = 1.0f..2.0f
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DomChatGlassPillButton(
                        text = "Reset to Defaults",
                        icon = Icons.Rounded.Refresh,
                        onClick = {
                            haptics.perform(HapticPattern.Pop)
                            tempDraft = 1.0f
                            topKDraft = 0.0f
                            minPDraft = 0.0f
                            topPDraft = 1.0f
                            freqDraft = 0.0f
                            presDraft = 0.0f
                            repDraft = 1.0f
                        }
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DomChatGlassPillButton(text = "Cancel", onClick = { showSamplersSheet = false })
                        DomChatGlassPillButton(
                            text = "Apply",
                            accent = true,
                            onClick = {
                                onUpdateAssistant(
                                    assistant.copy(
                                        temperature = tempDraft,
                                        topK = topKDraft.toInt().takeIf { it > 0 },
                                        minP = minPDraft.takeIf { it > 0f },
                                        topP = topPDraft,
                                        frequencyPenalty = freqDraft.takeIf { it != 0f },
                                        presencePenalty = presDraft.takeIf { it != 0f },
                                        repetitionPenalty = repDraft.takeIf { it != 1f }
                                    )
                                )
                                showSamplersSheet = false
                            }
                        )
                    }
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

        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showWallpaperSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                DomChatSheetHeader(
                    title = "Per-Chat Wallpaper",
                    subtitle = "Override global background for this conversation",
                    onCloseClick = { showWallpaperSheet = false }
                )

                val wallpaperScrollState = rememberScrollState()
                val canScrollUp by remember { derivedStateOf { wallpaperScrollState.canScrollBackward } }
                val canScrollDown by remember { derivedStateOf { wallpaperScrollState.canScrollForward } }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 24f)
                        .verticalScroll(wallpaperScrollState)
                        .padding(top = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status & Action row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DomChatGlassChip(
                            text = if (chatBgDraft != null) "Active for this chat" else "Using global wallpaper",
                            accent = chatBgDraft != null
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DomChatGlassPillButton(
                                text = if (chatBgDraft == null) "Choose" else "Change",
                                onClick = {
                                    wallpaperPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            if (chatBgDraft != null) {
                                DomChatGlassPillButton(
                                    text = "Reset",
                                    destructive = true,
                                    onClick = { chatBgDraft = null }
                                )
                            }
                        }
                    }

                    // Live Wallpaper Preview Card
                    val activeWpUri = chatBgDraft ?: settings.displaySetting.globalBackground
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(AppShapes.CardMedium)
                            .border(
                                BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                                AppShapes.CardMedium
                            )
                            .background(Color(0xFF000000))
                    ) {
                        if (!activeWpUri.isNullOrBlank()) {
                            AsyncImage(
                                model = activeWpUri,
                                contentDescription = "Wallpaper preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                            // Live dim overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Color.Black.copy(
                                            alpha = dimDraft.coerceIn(0f, 0.95f)
                                        )
                                    )
                            )
                            // Live blur overlay
                            if (blurDraft > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(
                                                        alpha = (blurDraft / 200f).coerceIn(0f, 0.5f)
                                                    )
                                                )
                                            )
                                        )
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No wallpaper selected (Pure OLED)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    // Blur Mode Segmented Tabs: Full Image vs Bottom Fade
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Blur Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isFull = blurModeDraft == me.lucid.domchat.data.model.BackgroundBlurMode.FULL
                            Surface(
                                onClick = { blurModeDraft = me.lucid.domchat.data.model.BackgroundBlurMode.FULL },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .liquidGlassSurface(
                                        shape = AppShapes.CardMedium,
                                        fillColor = if (isFull) Color(0xFF000000).copy(alpha = 0.50f) else Color(0xFF000000).copy(alpha = 0.20f),
                                        borderWidth = if (isFull) 1.dp else 0.5.dp,
                                        specularBrush = if (isFull) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle,
                                    ),
                                shape = AppShapes.CardMedium,
                                color = Color.Transparent,
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        "Full Image",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isFull) FontWeight.Bold else FontWeight.Medium),
                                        color = if (isFull) Color.White else Color.White.copy(alpha = 0.60f)
                                    )
                                }
                            }

                            val isBottomFade = blurModeDraft == me.lucid.domchat.data.model.BackgroundBlurMode.BOTTOM_FADE
                            Surface(
                                onClick = { blurModeDraft = me.lucid.domchat.data.model.BackgroundBlurMode.BOTTOM_FADE },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .liquidGlassSurface(
                                        shape = AppShapes.CardMedium,
                                        fillColor = if (isBottomFade) Color(0xFF000000).copy(alpha = 0.50f) else Color(0xFF000000).copy(alpha = 0.20f),
                                        borderWidth = if (isBottomFade) 1.dp else 0.5.dp,
                                        specularBrush = if (isBottomFade) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle,
                                    ),
                                shape = AppShapes.CardMedium,
                                color = Color.Transparent,
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        "Bottom Fade",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isBottomFade) FontWeight.Bold else FontWeight.Medium),
                                        color = if (isBottomFade) Color.White else Color.White.copy(alpha = 0.60f)
                                    )
                                }
                            }
                        }
                    }

                    // Background Blur Slider
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Background Blur", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Text("${blurDraft.toInt()} dp", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
                        }
                        DomChatSlider(
                            value = blurDraft,
                            onValueChange = { blurDraft = it },
                            valueRange = 0f..100f
                        )
                    }

                    // Blur Height Reach (only when Bottom Fade)
                    AnimatedVisibility(
                        visible = blurModeDraft == me.lucid.domchat.data.model.BackgroundBlurMode.BOTTOM_FADE,
                        enter = expandVertically(spring(0.85f, 380f)) + fadeIn(tween(200)),
                        exit = shrinkVertically(spring(0.85f, 380f)) + fadeOut(tween(180))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Blur Height Reach", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                Text("${(blurHeightFractionDraft * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
                            }
                            DomChatSlider(
                                value = blurHeightFractionDraft,
                                onValueChange = { blurHeightFractionDraft = it },
                                valueRange = 0.05f..1.0f
                            )
                        }
                    }

                    // Background Dim Slider
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Background Dim", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Text("${(dimDraft * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
                        }
                        DomChatSlider(
                            value = dimDraft,
                            onValueChange = { dimDraft = it },
                            valueRange = 0f..0.95f
                        )
                    }
                }

                // Sticky Bottom Apply Button with top fade gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.50f)
                                    )
                                )
                            )
                    )
                    Surface(
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
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .liquidGlassSurface(
                                shape = AppShapes.ButtonPill,
                                fillColor = Color(0xFF000000).copy(alpha = 0.35f),
                                borderWidth = 1.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderBrush,
                            ),
                        shape = AppShapes.ButtonPill,
                        color = Color.Transparent,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Apply to This Chat",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                        }
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
            },
            isChatPage = true,
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
        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showSearchPicker = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                DomChatSheetHeader(
                    title = stringResource(R.string.search_picker_title),
                    onCloseClick = { showSearchPicker = false }
                )

                val searchScrollState = rememberScrollState()
                val canScrollUp by remember { derivedStateOf { searchScrollState.canScrollBackward } }
                val canScrollDown by remember { derivedStateOf { searchScrollState.canScrollForward } }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 36f)
                        .verticalScroll(searchScrollState)
                        .padding(top = 8.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                    .background(Color(0xFF000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderOpen,
                    contentDescription = null,
                    tint = Color(0xFFFFFFFF),
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
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.workspace_required_card_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = Color.White.copy(alpha = 0.65f)
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
                        color = Color.White.copy(alpha = 0.65f)
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

    Surface(
        onClick = {
            haptics.perform(HapticPattern.Pop)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            Color(0xFF000000)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.White
            )
            option.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
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
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .liquidGlassSurface(
                            shape = CircleShape,
                            fillColor = Color(0xFF000000).copy(alpha = 0.10f),
                            borderWidth = 0.5.dp,
                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                        )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFFFFFFF)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tool_approval_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    Text(
                        text = approval.displayName(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val summary = approval.summary()
            if (summary.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassSurface(
                            shape = RoundedCornerShape(18.dp),
                            fillColor = Color(0xFF000000).copy(alpha = 0.10f),
                            borderWidth = 1.dp,
                            specularBrush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        )
                ) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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

    Surface(
        onClick = {
            haptics.perform(HapticPattern.Pop)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            Color(0xFF000000)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
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
                tint = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.65f)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
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
    onOpenTextStyling: () -> Unit = {},
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
    
    // Camera state persisted across process death
    var cameraOutputUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraOutputFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraOutputUri = cameraOutputUriString?.let { Uri.parse(it) }
    val cameraOutputFile = cameraOutputFilePath?.let { File(it) }
    

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
                    cameraOutputFilePath = null
                    cameraOutputUriString = null
                }
            )
        } else {
            capturedFile?.delete()
            cameraOutputFilePath = null
            cameraOutputUriString = null
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
            .padding(bottom = 16.dp)
    ) {
        DomChatSheetHeader(
            title = "Add to Chat",
            subtitle = "Quick attachments, roleplay tools & model controls",
            onCloseClick = onDismiss,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(16.dp),
                        fillColor = Color(0xFF000000).copy(alpha = 0.25f),
                        borderWidth = 0.5.dp,
                        specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                    )
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
    }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
        ) { pageIndex ->
            val pageScrollState = rememberScrollState()
            val canScrollUp by remember { derivedStateOf { pageScrollState.canScrollBackward } }
            val canScrollDown by remember { derivedStateOf { pageScrollState.canScrollForward } }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 36f)
                    .verticalScroll(pageScrollState)
                    .padding(top = 10.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (pageIndex) {
                0 -> {
                    // File upload buttons - grouped with corner shapes (no outer container)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Photos button - full width row
                        MinimalPickerItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            title = stringResource(R.string.minimal_input_photos),
                            subtitle = "Import images into the chat",
                            onClick = {
                                runCatching {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }.onFailure { e ->
                                    Log.e("MinimalChatInput", "Failed to launch image picker", e)
                                    toaster.show("Unable to open photo picker", type = ToastType.Error)
                                }
                            }
                        )

                        // Files button - full width row
                        MinimalPickerItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            title = stringResource(R.string.minimal_input_files),
                            subtitle = "Attach documents and files",
                            onClick = {
                                runCatching {
                                    filePickerLauncher.launch("*/*")
                                }.onFailure { e ->
                                    Log.e("MinimalChatInput", "Failed to launch file picker", e)
                                    toaster.show("Unable to open file picker", type = ToastType.Error)
                                }
                            }
                        )

                        // Camera button - full width row
                        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                            PermissionManager(permissionState = cameraPermission) {
                                MinimalPickerItem(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Rounded.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    title = stringResource(R.string.minimal_input_camera),
                                    subtitle = "Capture a photo for the chat",
                                    onClick = {
                                        if (cameraPermission.allRequiredPermissionsGranted) {
                                            val file = context.cacheDir.resolve("camera_${Uuid.random()}.jpg")
                                            val uri = runCatching {
                                                FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    file
                                                )
                                            }.getOrNull()
                                            if (uri != null) {
                                                cameraOutputFilePath = file.absolutePath
                                                cameraOutputUriString = uri.toString()
                                                runCatching {
                                                    cameraLauncher.launch(uri)
                                                }.onFailure { e ->
                                                    Log.e("MinimalChatInput", "Failed to launch camera", e)
                                                    file.delete()
                                                    cameraOutputFilePath = null
                                                    cameraOutputUriString = null
                                                    toaster.show("Unable to open camera app", type = ToastType.Error)
                                                }
                                            } else {
                                                toaster.show("Failed to create temporary camera file", type = ToastType.Error)
                                            }
                                        } else {
                                            cameraPermission.requestPermissions()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Character Creator Notes (moved to top)
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
                                tint = if (hasAuthorsNote) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.65f)
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
                                    tint = if (lorebooksActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.65f)
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
                                tint = if (hasCustomBg) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.65f)
                            )
                        },
                        title = "Wallpaper & Effects",
                        subtitle = if (hasCustomBg) "Custom Wallpaper · Blur ${assistant.backgroundBlur.toInt()}dp" else "Default / Global Wallpaper",
                        onClick = onOpenWallpaper
                    )

                    // Text Styling - opens dialog for per-chat text styling (e.g. custom italic colors)
                    MinimalPickerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.FormatItalic,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        title = "Text Styling",
                        subtitle = "Custom italic colors & per-chat text styles",
                        onClick = onOpenTextStyling
                    )
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
                                tint = if (streamingFadeEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.65f)
                            )
                        },
                        title = "Streaming Animation",
                        subtitle = if (streamingFadeEnabled) "Smooth Fade (${streamingFadeDuration}ms)" else "Instant / Pop",
                        onClick = onOpenStreamingAnimation
                    )

                    // Search, Skills, and Plugins cards hidden from plus icon menu
                    
                }
            }
        }
    }
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
                tint = Color.White.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.65f)
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
                tint = Color.White.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.65f)
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
                tint = Color.White.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

// Grouped file button with translucent liquid glass and title + subtitle display
@Composable
private fun MinimalPickerItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String = "",
    onClick: () -> Unit
) {
    val isDark = me.lucid.domchat.ui.theme.LocalDarkMode.current
    val interactionSource = remember { MutableInteractionSource() }
    val itemShape = RoundedCornerShape(16.dp)
    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = Color.Transparent,
        contentColor = if (isDark) Color(0xFFF2F2F7) else Color.White,
        shape = itemShape,
        border = null,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .pressScale(interactionSource)
            .liquidGlassSurface(
                shape = itemShape,
                fillColor = if (isDark) Color(0xFF000000).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.45f),
                borderWidth = 0.5.dp,
                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(26.dp),
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
                    color = if (isDark) Color(0xFFF2F2F7) else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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

    Box(
        modifier = modifier
            .size(36.dp)
            .pressScale(interactionSource)
            .liquidGlassSurface(
                shape = CircleShape,
                fillColor = Color.Transparent,
                borderWidth = 0.5.dp,
                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                isFloatingControl = true,
            )
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
                tint = Color.White.copy(alpha = 0.65f),
            )
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
                tint = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun ImeFocusSyncEffect(
    onImeOpened: () -> Unit,
    onImeClosed: () -> Unit,
) {
    val imeVisible = WindowInsets.isImeVisible
    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            onImeOpened()
        } else {
            onImeClosed()
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
