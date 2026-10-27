package me.lucid.domchat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

import me.lucid.domchat.ui.components.ui.AppToasterHost
import me.lucid.domchat.ui.components.ui.ToastAction
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.components.ui.rememberAppToasterState
import me.lucid.ai.util.KeyRoulette
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.filterNotNull
import me.lucid.highlight.Highlighter
import me.lucid.highlight.LocalHighlighter
import me.lucid.domchat.data.datastore.SpontaneousMessagingStateStore
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.ui.context.LocalAnimatedVisibilityScope
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.context.LocalSharedTransitionScope
import me.lucid.domchat.ui.context.LocalTTSState
import me.lucid.domchat.ui.context.LocalSTTState
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.readBooleanPreference
import me.lucid.domchat.ui.hooks.readStringPreference
import me.lucid.domchat.ui.hooks.rememberCustomTtsState
import me.lucid.domchat.ui.hooks.rememberCustomSttState
import me.lucid.domchat.ui.motion.LocalMotionPolicy
import me.lucid.domchat.ui.motion.rememberSystemMotionPolicy
import me.lucid.domchat.ui.motion.rootEnterTransition
import me.lucid.domchat.ui.motion.rootExitTransition
import me.lucid.domchat.ui.motion.rootPopEnterTransition
import me.lucid.domchat.ui.motion.rootPopExitTransition
import me.lucid.domchat.ui.motion.lateralEnterTransition
import me.lucid.domchat.ui.motion.lateralExitTransition
import me.lucid.domchat.navigation.CHAT_ROUTE_TARGET_KEY
import me.lucid.domchat.navigation.toChatRouteTarget
import me.lucid.domchat.ui.pages.chat.ChatSessionDraftStore
import me.lucid.domchat.ui.pages.assistant.AssistantPage
import me.lucid.domchat.ui.pages.assistant.detail.AssistantDetailPage
import me.lucid.domchat.ui.pages.backup.BackupPage
import me.lucid.domchat.ui.pages.chat.ChatPage
import me.lucid.domchat.ui.pages.developer.DeveloperPage
import me.lucid.domchat.ui.pages.imggen.ImageGenPage
import me.lucid.domchat.ui.pages.menu.MenuPage
import me.lucid.domchat.ui.pages.onboarding.OnboardingPage
import me.lucid.domchat.ui.pages.setting.SettingAboutPage
import me.lucid.domchat.ui.pages.setting.SettingChatStoragePage
import me.lucid.domchat.ui.pages.setting.SettingChatsPage
import me.lucid.domchat.ui.pages.setting.SettingContextBudgetPage
import me.lucid.domchat.ui.pages.setting.SettingDisplayPage
import me.lucid.domchat.ui.pages.setting.SettingPersonaPage

import me.lucid.domchat.ui.pages.setting.SettingMcpPage
// import me.lucid.domchat.ui.pages.setting.locallm.SettingLocalLlmPage
import me.lucid.domchat.ui.pages.extensions.workspace.WorkspacePage
import me.lucid.domchat.ui.pages.extensions.workspace.WorkspaceDetailPage
import me.lucid.domchat.ui.pages.extensions.workspace.WorkspaceTerminalPage
import me.lucid.domchat.ui.pages.setting.SettingModelPage
import me.lucid.domchat.ui.pages.setting.SettingPage
import me.lucid.domchat.ui.pages.setting.SettingProviderDetailPage
import me.lucid.domchat.ui.pages.setting.SettingProviderPage
import me.lucid.domchat.ui.pages.setting.SettingSearchPage
import me.lucid.domchat.ui.pages.setting.SettingTTSProviderDetailPage
import me.lucid.domchat.ui.pages.setting.SettingTTSPage
import me.lucid.domchat.ui.pages.setting.SettingWebPage
import me.lucid.domchat.ui.pages.setting.SettingRpOptimizationsPage
import me.lucid.domchat.ui.pages.setting.SettingPromptInjectionsPage
import me.lucid.domchat.ui.pages.setting.SettingPresetsPage
import me.lucid.domchat.ui.pages.setting.SettingLorebooksPage
import me.lucid.domchat.ui.pages.setting.SettingLorebookDetailPage
import me.lucid.domchat.ui.pages.setting.SettingSkillsPage
import me.lucid.domchat.ui.pages.share.handler.ShareHandlerPage
import me.lucid.domchat.ui.pages.webview.WebViewPage
import me.lucid.domchat.ui.pages.setting.SettingAndroidIntegrationPage
import me.lucid.domchat.ui.pages.setting.SettingUICustomizationPage
import me.lucid.domchat.ui.pages.setting.SettingFontsPage
import me.lucid.domchat.ui.pages.setting.AdaptiveSettingsScaffold
import me.lucid.domchat.ui.pages.setting.SettingsDestination
import me.lucid.domchat.share.ResolvedSharePayload
import me.lucid.domchat.share.readResolvedSharePayload
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.DomchatTheme
import me.lucid.domchat.service.EXTRA_IS_SPONTANEOUS_NOTIFICATION
import me.lucid.domchat.service.EXTRA_SPONTANEOUS_EVENT_ID
import me.lucid.domchat.service.EXTRA_SPONTANEOUS_MESSAGE
import me.lucid.domchat.service.EXTRA_SPONTANEOUS_RELATION
import me.lucid.domchat.service.ChatPersistenceMode
import me.lucid.domchat.data.model.getInitialMessageNodes
import me.lucid.domchat.ui.activity.QuickAskContinuationData
import me.lucid.domchat.ui.activity.readQuickAskContinuationData
import me.lucid.domchat.ui.activity.seedQuickAskChat
import me.lucid.domchat.utils.navigateToChatPage
import org.koin.android.ext.android.inject
import me.lucid.domchat.utils.fileSizeToString
import kotlin.uuid.Uuid

private const val TAG = "RouteActivity"

private fun String?.toUuidOrNull(): Uuid? =
    this?.let { raw -> runCatching { Uuid.parse(raw) }.getOrNull() }

internal data class SpontaneousNotificationData(
    val assistantId: String,
    val conversationId: String?,
    val eventId: String,
    val message: String,
    val relation: me.lucid.domchat.service.SpontaneousMessageRelation,
)

internal data class ResolvedSpontaneousChatTarget(
    val conversationId: Uuid,
    val persistenceMode: ChatPersistenceMode,
    val assistantId: Uuid,
    val focusLatestMessageKey: String? = null,
)

internal fun resolveSpontaneousNotificationRelation(
    relationExtra: String?,
    conversationId: String?,
): me.lucid.domchat.service.SpontaneousMessageRelation {
    return me.lucid.domchat.service.SpontaneousMessageRelation.fromWireValue(relationExtra)
        ?: if (conversationId.isNullOrBlank()) {
            me.lucid.domchat.service.SpontaneousMessageRelation.UNRELATED
        } else {
            me.lucid.domchat.service.SpontaneousMessageRelation.RECENT_CHAT
        }
}

internal suspend fun resolveSpontaneousNotificationTarget(
    data: SpontaneousNotificationData,
    isEventConsumed: (String) -> Boolean,
    getConsumedTarget: (String) -> ResolvedSpontaneousChatTarget?,
    updateAssistantSelection: suspend (Uuid) -> Unit,
    hasConversation: suspend (Uuid) -> Boolean,
    appendToConversation: suspend (Uuid, String, Uuid) -> Uuid?,
    seedDraftConversation: suspend (Uuid, String, Uuid?) -> Uuid?,
    markEventConsumed: (String, ResolvedSpontaneousChatTarget) -> Unit,
): ResolvedSpontaneousChatTarget? {
    val assistantId = runCatching { Uuid.parse(data.assistantId) }.getOrNull() ?: return null
    val message = data.message.trim()
    if (message.isBlank()) return null

    getConsumedTarget(data.eventId)?.let { consumedTarget ->
        updateAssistantSelection(consumedTarget.assistantId)
        val conversationExists = hasConversation(consumedTarget.conversationId)
        if (conversationExists) {
            val resolvedTarget = consumedTarget.copy(
                persistenceMode = ChatPersistenceMode.NORMAL,
                focusLatestMessageKey = data.eventId,
            )
            markEventConsumed(data.eventId, resolvedTarget)
            return resolvedTarget
        }
        if (consumedTarget.persistenceMode != ChatPersistenceMode.NORMAL) {
            seedDraftConversation(
                consumedTarget.assistantId,
                message,
                consumedTarget.conversationId,
            ) ?: return null
            return consumedTarget.copy(focusLatestMessageKey = data.eventId)
        } else {
            return null
        }
    }

    if (isEventConsumed(data.eventId)) {
        val conversationId = data.conversationId
            ?.let { raw -> runCatching { Uuid.parse(raw) }.getOrNull() }
            ?.takeIf { data.relation == me.lucid.domchat.service.SpontaneousMessageRelation.RECENT_CHAT }
            ?.takeIf { hasConversation(it) }
            ?: return null
        updateAssistantSelection(assistantId)
        return ResolvedSpontaneousChatTarget(
            conversationId = conversationId,
            persistenceMode = ChatPersistenceMode.NORMAL,
            assistantId = assistantId,
            focusLatestMessageKey = data.eventId,
        )
    }

    updateAssistantSelection(assistantId)

    val originalConversationId = data.conversationId?.let { raw ->
        runCatching { Uuid.parse(raw) }.getOrNull()
    }

    val target = when (data.relation) {
        me.lucid.domchat.service.SpontaneousMessageRelation.RECENT_CHAT -> {
            val existingConversationId = if (
                originalConversationId != null &&
                hasConversation(originalConversationId)
            ) {
                appendToConversation(assistantId, message, originalConversationId)
            } else {
                null
            }
            val conversationId = existingConversationId
                ?: seedDraftConversation(assistantId, message, null)
                ?: return null
            ResolvedSpontaneousChatTarget(
                conversationId = conversationId,
                persistenceMode = if (existingConversationId != null) {
                    ChatPersistenceMode.NORMAL
                } else {
                    ChatPersistenceMode.PERSIST_ON_REPLY
                },
                assistantId = assistantId,
                focusLatestMessageKey = data.eventId,
            )
        }

        me.lucid.domchat.service.SpontaneousMessageRelation.UNRELATED -> {
            val conversationId = seedDraftConversation(assistantId, message, null) ?: return null
            ResolvedSpontaneousChatTarget(
                conversationId = conversationId,
                persistenceMode = ChatPersistenceMode.PERSIST_ON_REPLY,
                assistantId = assistantId,
                focusLatestMessageKey = data.eventId,
            )
        }
    }

    markEventConsumed(data.eventId, target)
    return target
}

internal fun determineInitialChatScreen(
    defaultScreen: Screen.Chat,
    deepLinkedConversationId: String?,
    spontaneousTarget: ResolvedSpontaneousChatTarget?,
): Screen.Chat {
    return when {
        spontaneousTarget != null -> spontaneousTarget.toScreen()
        !deepLinkedConversationId.isNullOrBlank() -> Screen.Chat(id = deepLinkedConversationId)
        else -> defaultScreen
    }
}

private fun isSettingsPaneRoute(route: String?): Boolean {
    return route != null && (
        route.contains("Setting") ||
            route.contains("Assistant") ||
            route.contains("Backup") ||
            route.contains("Workspace")
        )
}

private fun ResolvedSpontaneousChatTarget.toScreen(): Screen.Chat {
    return Screen.Chat(
        id = conversationId.toString(),
        persistenceMode = persistenceMode.routeValue.takeIf { persistenceMode != ChatPersistenceMode.NORMAL },
        focusLatestMessageKey = focusLatestMessageKey,
    )
}

private fun me.lucid.domchat.data.datastore.ConsumedSpontaneousEventRecord.toResolvedSpontaneousTarget(): ResolvedSpontaneousChatTarget? {
    val conversationId = conversationId
        ?.let { raw -> runCatching { Uuid.parse(raw) }.getOrNull() }
        ?: return null
    val assistantId = assistantId
        ?.let { raw -> runCatching { Uuid.parse(raw) }.getOrNull() }
        ?: return null
    val persistenceMode = ChatPersistenceMode.fromRouteValue(persistenceMode)
        ?: ChatPersistenceMode.NORMAL
    return ResolvedSpontaneousChatTarget(
        conversationId = conversationId,
        persistenceMode = persistenceMode,
        assistantId = assistantId,
    )
}

class RouteActivity : ComponentActivity() {
    private val highlighter by inject<Highlighter>()
    private val settingsStore by inject<SettingsStore>()
    private val spontaneousMessagingStateStore by inject<SpontaneousMessagingStateStore>()
    private val chatService by inject<me.lucid.domchat.service.ChatService>()
    private val conversationRepo by inject<me.lucid.domchat.data.repository.ConversationRepository>()
    private var navStack by mutableStateOf<NavHostController?>(null)
    private var pendingTextSelection by mutableStateOf<QuickAskContinuationData?>(null)
    private var pendingConversationId by mutableStateOf<String?>(null)
    private var pendingResolvedSpontaneousTarget by mutableStateOf<ResolvedSpontaneousChatTarget?>(null)
    private var pendingShareIntent by mutableStateOf<ResolvedSharePayload?>(null)
    private var initialChatScreen by mutableStateOf<Screen.Chat?>(null)
    private var hasSuccessfulReplyBeforeSetup by mutableStateOf<Boolean?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            enableEdgeToEdge()
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
            disableNavigationBarContrast()
            super.onCreate(savedInstanceState)
            lifecycleScope.launch {
                hasSuccessfulReplyBeforeSetup = withContext(Dispatchers.IO) {
                    conversationRepo.hasSuccessfulAssistantReply()
                }
            }
            
            // Track app launch and initialize usage stats
            lifecycleScope.launch(Dispatchers.IO) {
                kotlinx.coroutines.delay(1500)
                runCatching { conversationRepo.initUsageStats() }
                    .onFailure { android.util.Log.e(TAG, "initUsageStats failed", it) }
                runCatching { conversationRepo.backfillDailyActivityFromConversationHistoryIfNeeded() }
                    .onFailure { android.util.Log.e(TAG, "daily activity backfill failed", it) }
                runCatching { conversationRepo.backfillUsageStatsFromHistoryIfNeeded() }
                    .onFailure { android.util.Log.e(TAG, "usage stats backfill failed", it) }
                runCatching { conversationRepo.incrementAppLaunches() }
                    .onFailure { android.util.Log.e(TAG, "increment app launches failed", it) }
            }
            
            val spontaneousNotification = intent.toSpontaneousNotificationData()
            val intentAssistantId = if (spontaneousNotification == null) {
                intent?.getStringExtra(EXTRA_ASSISTANT_ID)
            } else {
                null
            }
            val intentConversationId = if (spontaneousNotification == null) {
                intent?.getStringExtra(EXTRA_CONVERSATION_ID)
            } else {
                null
            }
            val intentWebServerSettings = intent?.getBooleanExtra("webServerSettings", false) == true
            pendingTextSelection = intent?.readQuickAskContinuationData()
            pendingShareIntent = intent?.readResolvedSharePayload()

            // Pre-seed initialChatScreen immediately so frame 1 renders without blank box delay
            initialChatScreen = determineInitialChatScreen(
                defaultScreen = defaultStartScreen(),
                deepLinkedConversationId = intentConversationId,
                spontaneousTarget = null,
            )

            lifecycleScope.launch {
                // A conversation hand-off from the assistant overlay must select its
                // character before ChatVM initializes. This also covers the short window
                // where the in-memory conversation is released while the app task resumes.
                if (!intentConversationId.isNullOrBlank()) {
                    intentAssistantId.toUuidOrNull()?.let { assistantId ->
                        settingsStore.updateAssistant(assistantId)
                        settingsStore.markAssistantUsed(assistantId)
                    }
                } else if (spontaneousNotification == null && readBooleanPreference("create_new_conversation_on_start", true)) {
                    // Reset to default blank assistant when starting a new conversation on app restart
                    settingsStore.updateAssistant(me.lucid.domchat.data.datastore.DEFAULT_ASSISTANT_ID)
                }
                if (spontaneousNotification != null) {
                    val spontaneousTarget = resolveSpontaneousChatTarget(spontaneousNotification)
                    if (spontaneousTarget != null) {
                        initialChatScreen = spontaneousTarget.toScreen()
                    }
                }
            }

            setContent {
                val navStack = rememberNavController()
                this.navStack = navStack
                DomchatTheme {
                    val startScreen = initialChatScreen
                    if (startScreen == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        )
                    } else {
                        val context = LocalContext.current
                        ShareHandler(navStack)
                        TextSelectionHandler(navStack)
                        NotificationHandler(navStack)
                        AppRoutes(navStack, startScreen)
                        
                        LaunchedEffect(intentWebServerSettings) {
                            if (intentWebServerSettings) {
                                navStack.navigate(Screen.SettingWeb)
                            }
                        }
                    }
                }
            }
            
            // Handle assistant shortcut - navigate directly by waiting for navStack to be ready
            if (intentAssistantId != null && intentConversationId.isNullOrBlank()) {
                lifecycleScope.launch {
                    // Wait for navStack to be ready (set in composition)
                    while (navStack == null) {
                        kotlinx.coroutines.delay(50)
                    }
                    try {
                        val assistantId = Uuid.parse(intentAssistantId)
                        // Update the selected assistant
                        settingsStore.updateAssistant(assistantId)
                        // Mark as recently used
                        settingsStore.markAssistantUsed(assistantId)
                        // Navigate to a new chat
                        navStack?.let { navigateToChatPage(it, chatId = Uuid.random()) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Fatal error in RouteActivity.onCreate", e)
        }
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            ChatSessionDraftStore.clear()
        }
        super.onDestroy()
    }

    private fun disableNavigationBarContrast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun defaultStartScreen(): Screen.Chat {
        return Screen.Chat(
            id = if (readBooleanPreference("create_new_conversation_on_start", true)) {
                Uuid.random().toString()
            } else {
                readStringPreference(
                    "lastConversationId",
                    Uuid.random().toString()
                ) ?: Uuid.random().toString()
            }
        )
    }

    private fun navigateToIncomingConversation(conversationIdText: String) {
        conversationIdText.toUuidOrNull()?.let { conversationId ->
            val controller = navStack
            if (controller == null || initialChatScreen == null) {
                // A NavHostController is created before AppRoutes installs its graph. An
                // incoming intent in that window would make navigate() throw, so defer it
                // until NotificationHandler is composed with the real chat NavHost.
                pendingConversationId = conversationId.toString()
            } else {
                navigateToChatPage(controller, chatId = conversationId)
            }
        }
    }

    private fun Intent?.toSpontaneousNotificationData(): SpontaneousNotificationData? {
        if (this == null || !getBooleanExtra(EXTRA_IS_SPONTANEOUS_NOTIFICATION, false)) {
            return null
        }

        val assistantId = getStringExtra("assistantId") ?: return null
        val eventId = getStringExtra(EXTRA_SPONTANEOUS_EVENT_ID) ?: return null
        val message = getStringExtra(EXTRA_SPONTANEOUS_MESSAGE) ?: return null
        val conversationId = getStringExtra("conversationId")
        val relation = resolveSpontaneousNotificationRelation(
            relationExtra = getStringExtra(EXTRA_SPONTANEOUS_RELATION),
            conversationId = conversationId,
        )

        return SpontaneousNotificationData(
            assistantId = assistantId,
            conversationId = conversationId,
            eventId = eventId,
            message = message,
            relation = relation,
        )
    }

    @Composable
    private fun ShareHandler(navBackStack: NavHostController) {
        val shareData = pendingShareIntent
        LaunchedEffect(navBackStack, shareData) {
            val currentShareData = shareData ?: return@LaunchedEffect
            pendingShareIntent = null
            runCatching {
                navBackStack.navigate(
                    Screen.ShareHandler(
                        text = currentShareData.text,
                        files = currentShareData.attachmentUris()
                    )
                )
            }.onFailure { throwable ->
                android.util.Log.e(TAG, "Share navigation failed", throwable)
                navBackStack.navigate(
                    Screen.ShareHandler(
                        text = currentShareData.text,
                        files = currentShareData.attachmentUris()
                    )
                )
            }
        }
    }

    @Composable
    private fun NotificationHandler(navBackStack: NavHostController) {
        val spontaneousTarget = pendingResolvedSpontaneousTarget
        val conversationIdStr = pendingConversationId
        LaunchedEffect(spontaneousTarget, conversationIdStr) {
            if (spontaneousTarget != null) {
                pendingResolvedSpontaneousTarget = null
                navigateToChatPage(
                    navController = navBackStack,
                    chatId = spontaneousTarget.conversationId,
                    persistenceMode = spontaneousTarget.persistenceMode.routeValue
                        .takeIf { spontaneousTarget.persistenceMode != ChatPersistenceMode.NORMAL },
                    focusLatestMessageKey = spontaneousTarget.focusLatestMessageKey,
                )
            } else if (conversationIdStr != null) {
                pendingConversationId = null
                runCatching { Uuid.parse(conversationIdStr) }
                    .getOrNull()
                    ?.let { conversationId ->
                        navigateToChatPage(navBackStack, chatId = conversationId)
                    }
            }
        }
    }

    private suspend fun resolveSpontaneousChatTarget(
        data: SpontaneousNotificationData,
    ): ResolvedSpontaneousChatTarget? {
        return resolveSpontaneousNotificationTarget(
            data = data,
            isEventConsumed = spontaneousMessagingStateStore::isEventConsumed,
            getConsumedTarget = { eventId ->
                spontaneousMessagingStateStore.getConsumedEventRecord(eventId)
                    ?.toResolvedSpontaneousTarget()
            },
            updateAssistantSelection = { assistantId ->
                settingsStore.updateAssistant(assistantId)
                settingsStore.markAssistantUsed(assistantId)
            },
            hasConversation = { conversationId ->
                conversationRepo.getConversationById(conversationId) != null
            },
            appendToConversation = { assistantId, message, conversationId ->
                chatService.persistSpontaneousAssistantMessage(
                    assistantId = assistantId,
                    content = message,
                    conversationId = conversationId,
                ).id
            },
            seedDraftConversation = { assistantId, message, conversationId ->
                chatService.seedSpontaneousDraftConversation(
                    assistantId = assistantId,
                    content = message,
                    conversationId = conversationId ?: Uuid.random(),
                ).id
            },
            markEventConsumed = { eventId, target ->
                spontaneousMessagingStateStore.markEventConsumed(
                    eventId = eventId,
                    conversationId = target.conversationId,
                    assistantId = target.assistantId,
                    persistenceMode = target.persistenceMode.routeValue,
                )
            },
        )
    }

    @Composable
    private fun TextSelectionHandler(navBackStack: NavHostController) {
        val data = pendingTextSelection
        val settings by settingsStore.settingsFlow.collectAsStateWithLifecycle()
        
        
        LaunchedEffect(data) {
            if (data != null) {
                pendingTextSelection = null
                try {
                    val assistantId = data.assistantId?.takeIf { it.isNotBlank() }?.let {
                        try { Uuid.parse(it) } catch (e: Exception) { null }
                    } ?: settings.assistantId

                    // Select the right assistant and mark it as used
                    settingsStore.updateAssistant(assistantId)
                    settingsStore.markAssistantUsed(assistantId)

                    val assistant = settings.assistants.find { it.id == assistantId }
                        ?: settings.assistants.firstOrNull()
                    val existingConvos = conversationRepo.getRecentConversations(assistantId, limit = 1)
                    val seed = seedQuickAskChat(
                        data = data,
                        existingConversation = existingConvos.firstOrNull(),
                        introNodes = assistant?.getInitialMessageNodes().orEmpty(),
                    )
                    val conversationId = seed.reuseConversationId ?: Uuid.random()
                    if (seed.messageNodes.isNotEmpty()) {
                        val base = existingConvos.firstOrNull()?.takeIf { it.id == conversationId }
                        val conversation = (base ?: me.lucid.domchat.data.model.Conversation.ofId(
                            id = conversationId,
                            assistantId = assistantId,
                        )).copy(
                            assistantId = assistantId,
                            messageNodes = seed.messageNodes,
                        )
                        chatService.saveConversation(conversationId, conversation)
                    }
                    navigateToChatPage(navBackStack, chatId = conversationId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        android.util.Log.d(TAG, "onNewIntent called")
        val conversationIdText = intent.getStringExtra(EXTRA_CONVERSATION_ID)
        val assistantIdText = intent.getStringExtra(EXTRA_ASSISTANT_ID)
        android.util.Log.d(TAG, "Intent extras: conversationId=$conversationIdText, assistantId=$assistantIdText")
        pendingShareIntent = intent.readResolvedSharePayload()
        pendingTextSelection = intent.readQuickAskContinuationData() ?: pendingTextSelection

        if (intent.getBooleanExtra("webServerSettings", false)) {
            navStack?.navigate(Screen.SettingWeb)
            return
        }
        intent.toSpontaneousNotificationData()?.let { notification ->
            lifecycleScope.launch {
                resolveSpontaneousChatTarget(notification)?.let { target ->
                    if (navStack == null) {
                        initialChatScreen = target.toScreen()
                    } else {
                        pendingResolvedSpontaneousTarget = target
                    }
                }
            }
            return
        }
        
        // Overlay hand-offs include both IDs. Select the character first, then navigate so a
        // cold/recreated ChatService state cannot fall back to the previous character.
        if (!conversationIdText.isNullOrBlank() && !assistantIdText.isNullOrBlank()) {
            lifecycleScope.launch {
                assistantIdText.toUuidOrNull()?.let { assistantId ->
                    settingsStore.updateAssistant(assistantId)
                    settingsStore.markAssistantUsed(assistantId)
                }
                navigateToIncomingConversation(conversationIdText)
            }
            return
        }

        // Navigate to the chat screen if a conversation ID is provided.
        conversationIdText?.let { text ->
            android.util.Log.d(TAG, "Navigating to conversation: $text")
            navigateToIncomingConversation(text)
        }
        
        // Handle assistant shortcut - navigate directly instead of using state
        assistantIdText?.let { assistantIdStr ->
            android.util.Log.d(TAG, "Handling assistant shortcut directly: $assistantIdStr")
            lifecycleScope.launch {
                try {
                    val assistantId = Uuid.parse(assistantIdStr)
                    android.util.Log.d(TAG, "Updating to assistant: $assistantId")
                    // Update the selected assistant
                    settingsStore.updateAssistant(assistantId)
                    // Mark as recently used
                    settingsStore.markAssistantUsed(assistantId)
                    // Navigate to a new chat
                    val newChatId = Uuid.random()
                    android.util.Log.d(TAG, "Navigating to new chat: $newChatId")
                    navStack?.let { navigateToChatPage(it, chatId = newChatId) }
                    android.util.Log.d(TAG, "Navigation complete")
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error handling assistant shortcut", e)
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val EXTRA_CONVERSATION_ID = "conversationId"
        const val EXTRA_ASSISTANT_ID = "assistantId"
    }

    @Composable
    fun AppRoutes(navBackStack: NavHostController, startDestination: Screen.Chat) {
        val toastState = rememberAppToasterState()
        val settings by settingsStore.settingsFlow.collectAsStateWithLifecycle()
        val tts = rememberCustomTtsState()
        val stt = rememberCustomSttState()
        val motionPolicy = rememberSystemMotionPolicy(settings.displaySetting.animationSpeed.scaleFactor)
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalNavController provides navBackStack,
                LocalSharedTransitionScope provides this,
                LocalSettings provides settings,
                LocalHighlighter provides highlighter,
                LocalMotionPolicy provides motionPolicy,
                LocalToaster provides toastState,
                LocalTTSState provides tts,
                LocalSTTState provides stt,
            ) {
                // Listen for API key authentication / quota errors and notify user
                LaunchedEffect(Unit) {
                    KeyRoulette.default().authErrorEvents.collect { event ->
                        val keyLabel = event.keyName.ifBlank { "API Key" }
                        toastState.show(
                            message = this@RouteActivity.getString(R.string.api_key_auth_error_toast, keyLabel),
                            type = ToastType.Error,
                            action = ToastAction(
                                label = this@RouteActivity.getString(R.string.api_key_auth_error_action),
                                onClick = {
                                    if (event.providerId != kotlin.uuid.Uuid.NIL) {
                                        navBackStack.navigate(Screen.SettingProviderDetail(event.providerId.toString()))
                                    } else {
                                        navBackStack.navigate(Screen.SettingProvider)
                                    }
                                }
                            )
                        )
                    }
                }

                // Check for backup cleanup results and show toast
                LaunchedEffect(Unit) {
                    val prefs = this@RouteActivity.getSharedPreferences("backup_cleanup", MODE_PRIVATE)
                    val unsupportedBytes = prefs.getLong("unsupported_bytes", 0)
                    val issuesFixed = prefs.getInt("issues_fixed", 0)
                    val skippedRows = prefs.getInt("db_skipped_rows", 0)
                    
                    if (unsupportedBytes > 0 || issuesFixed > 0 || skippedRows > 0) {
                        // Clear the stored values
                        prefs.edit().clear().apply()
                        
                        // Build cleanup message
                        val parts = mutableListOf<String>()
                        if (unsupportedBytes > 0) {
                            parts.add(
                                this@RouteActivity.getString(
                                    R.string.backup_restore_import_cleanup_unsupported,
                                    unsupportedBytes.fileSizeToString()
                                )
                            )
                        }
                        if (issuesFixed > 0) {
                            parts.add(
                                this@RouteActivity.getString(
                                    R.string.backup_restore_import_cleanup_invalid_references,
                                    issuesFixed
                                )
                            )
                        }
                        if (skippedRows > 0) {
                            parts.add(
                                this@RouteActivity.getString(
                                    R.string.backup_restore_import_cleanup_corrupt_removed,
                                    skippedRows
                                )
                            )
                        }
                        
                        val message = this@RouteActivity.getString(
                            R.string.backup_restore_import_cleanup_summary,
                            parts.joinToString(", ")
                        )
                        toastState.show(message, type = me.lucid.domchat.ui.components.ui.ToastType.Info)
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                val shouldShowSetup = !settings.init &&
                    !settings.setupCompleted &&
                    hasSuccessfulReplyBeforeSetup == false
                LaunchedEffect(shouldShowSetup) {
                    if (shouldShowSetup) {
                        navBackStack.navigate(Screen.Setup) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                val actualStartDestination: Screen = if (shouldShowSetup) {
                    Screen.Setup
                } else {
                    startDestination
                }
                val windowSize = currentWindowDpSize()
                val useWideSettingsLayout = windowSize.width >= 840.dp && windowSize.height >= 600.dp
                NavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    startDestination = actualStartDestination,
                    navController = navBackStack,
                    enterTransition = {
                        if (
                            useWideSettingsLayout &&
                            isSettingsPaneRoute(initialState.destination.route) &&
                            isSettingsPaneRoute(targetState.destination.route)
                        ) {
                            EnterTransition.None
                        } else {
                            rootEnterTransition(motionPolicy)
                        }
                    },
                    exitTransition = {
                        if (
                            useWideSettingsLayout &&
                            isSettingsPaneRoute(initialState.destination.route) &&
                            isSettingsPaneRoute(targetState.destination.route)
                        ) {
                            ExitTransition.None
                        } else {
                            rootExitTransition(motionPolicy)
                        }
                    },
                    popEnterTransition = {
                        if (
                            useWideSettingsLayout &&
                            isSettingsPaneRoute(initialState.destination.route) &&
                            isSettingsPaneRoute(targetState.destination.route)
                        ) {
                            EnterTransition.None
                        } else {
                            rootPopEnterTransition(motionPolicy)
                        }
                    },
                    popExitTransition = {
                        if (
                            useWideSettingsLayout &&
                            isSettingsPaneRoute(initialState.destination.route) &&
                            isSettingsPaneRoute(targetState.destination.route)
                        ) {
                            ExitTransition.None
                        } else {
                            rootPopExitTransition(motionPolicy)
                        }
                    }
                ) {
                    composable<Screen.Chat> { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.Chat>()
                        val initialChatTarget = route.toChatRouteTarget()
                        val activeChatTarget by backStackEntry.savedStateHandle
                            .getStateFlow(CHAT_ROUTE_TARGET_KEY, initialChatTarget)
                            .collectAsStateWithLifecycle()
                        ChatPage(
                            target = activeChatTarget,
                        )
                    }

                    composable<Screen.ShareHandler> { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.ShareHandler>()
                        ShareHandlerPage(
                            text = route.text,
                            files = route.files
                        )
                    }

                    composable<Screen.Setup> {
                        OnboardingPage()
                    }



                    // All assistant-related routes share the same AnimatedVisibilityScope
                    // for seamless hero animations across all screens
                    composable<Screen.Assistant> {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                            AdaptiveSettingsScaffold(selected = SettingsDestination.Assistants) {
                                me.lucid.domchat.ui.pages.assistant.CharacterHubPage()
                            }
                        }
                    }

                    composable<Screen.CharacterHub> {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                            me.lucid.domchat.ui.pages.assistant.CharacterHubPage()
                        }
                    }

                    composable<Screen.AssistantDetail> { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.AssistantDetail>()
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                            AdaptiveSettingsScaffold(selected = SettingsDestination.Assistants) {
                                AssistantDetailPage(
                                    id = route.id,
                                    startRoute = route.startRoute,
                                    initialMemoryTab = route.initialMemoryTab,
                                    scrollToMemoryId = route.scrollToMemoryId
                                )
                            }
                        }
                    }

                    composable<Screen.Menu> {
                        MenuPage()
                    }

                    composable<Screen.Setting> {
                        AdaptiveSettingsScaffold(
                            selected = SettingsDestination.Display,
                            compactContent = { SettingPage() },
                        ) {
                            SettingDisplayPage()
                        }
                    }

                    composable<Screen.Backup> { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.Backup>()
                        val initialTab = me.lucid.domchat.ui.pages.backup.BackupTab.fromRoute(route.tab)
                        AdaptiveSettingsScaffold(
                            selected = when (initialTab) {
                                me.lucid.domchat.ui.pages.backup.BackupTab.WebDav -> SettingsDestination.BackupWebDav
                                me.lucid.domchat.ui.pages.backup.BackupTab.Local -> SettingsDestination.BackupLocal
                            }
                        ) {
                            BackupPage(initialTab = initialTab)
                        }
                    }

                    composable<Screen.BackupWebDav> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.BackupWebDav) {
                            BackupPage(initialTab = me.lucid.domchat.ui.pages.backup.BackupTab.WebDav)
                        }
                    }

                    composable<Screen.BackupLocal> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.BackupLocal) {
                            BackupPage(initialTab = me.lucid.domchat.ui.pages.backup.BackupTab.Local)
                        }
                    }

                    composable<Screen.SettingLocalLlm> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Providers) {
                            me.lucid.domchat.ui.pages.setting.locallm.SettingLocalLlmPage()
                        }
                    }

                    composable<Screen.ImageGen> {
                        ImageGenPage()
                    }

                    composable<Screen.WebView> { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.WebView>()
                        WebViewPage(route.url, route.content)
                    }

                    composable<Screen.SettingDisplay> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Display) {
                            SettingDisplayPage()
                        }
                    }

                    composable<Screen.SettingPersonas> {
                        SettingPersonaPage()
                    }

                    composable<Screen.SettingProvider>(
                        enterTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (
                                initialState.destination.route?.contains("SettingSearch") == true ||
                                initialState.destination.route?.contains("SettingTTS") == true
                            ) {
                                lateralEnterTransition(offset = { -it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        },
                        exitTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (
                                targetState.destination.route?.contains("SettingSearch") == true ||
                                targetState.destination.route?.contains("SettingTTS") == true
                            ) {
                                lateralExitTransition(offset = { -it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        },
                        popEnterTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (
                                initialState.destination.route?.contains("SettingSearch") == true ||
                                initialState.destination.route?.contains("SettingTTS") == true
                            ) {
                                lateralEnterTransition(offset = { -it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        },
                        popExitTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (
                                targetState.destination.route?.contains("SettingSearch") == true ||
                                targetState.destination.route?.contains("SettingTTS") == true
                            ) {
                                lateralExitTransition(offset = { -it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        }
                    ) {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.ProviderModels) {
                            SettingProviderPage()
                        }
                    }

                    composable<Screen.SettingProviderDetail> {
                        val route = it.toRoute<Screen.SettingProviderDetail>()
                        val id = Uuid.parse(route.providerId)
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Providers) {
                            SettingProviderDetailPage(id = id)
                        }
                    }

                    composable<Screen.SettingTTSProviderDetail> {
                        val route = it.toRoute<Screen.SettingTTSProviderDetail>()
                        val id = Uuid.parse(route.providerId)
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Tts) {
                            SettingTTSProviderDetailPage(id = id)
                        }
                    }

                    composable<Screen.SettingModels> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Models) {
                            SettingModelPage()
                        }
                    }

                    composable<Screen.SettingContextBudget> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Models) {
                            SettingContextBudgetPage()
                        }
                    }

                    composable<Screen.SettingAbout> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.About) {
                            SettingAboutPage()
                        }
                    }

                    composable<Screen.SettingChatStorage> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.ChatStorage) {
                            SettingChatStoragePage()
                        }
                    }

                    composable<Screen.SettingChats> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.ChatStorage) {
                            SettingChatsPage()
                        }
                    }

                    composable<Screen.SettingSearch>(
                        enterTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (initialState.destination.route?.contains("SettingProvider") == true) {
                                lateralEnterTransition(offset = { it }, motionPolicy = motionPolicy)
                            } else if (initialState.destination.route?.contains("SettingTTS") == true) {
                                lateralEnterTransition(offset = { -it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        },
                        exitTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (targetState.destination.route?.contains("SettingProvider") == true) {
                                lateralExitTransition(offset = { it }, motionPolicy = motionPolicy)
                            } else if (targetState.destination.route?.contains("SettingTTS") == true) {
                                lateralExitTransition(offset = { -it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        },
                        popEnterTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (initialState.destination.route?.contains("SettingProvider") == true) {
                                lateralEnterTransition(offset = { it }, motionPolicy = motionPolicy)
                            } else if (initialState.destination.route?.contains("SettingTTS") == true) {
                                lateralEnterTransition(offset = { -it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        },
                        popExitTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (targetState.destination.route?.contains("SettingProvider") == true) {
                                lateralExitTransition(offset = { it }, motionPolicy = motionPolicy)
                            } else if (targetState.destination.route?.contains("SettingTTS") == true) {
                                lateralExitTransition(offset = { -it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        }
                    ) {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Search) {
                            SettingProviderPage(initialTab = me.lucid.domchat.ui.pages.setting.ProvidersTab.Search)
                        }
                    }

                    composable<Screen.SettingTTS>(
                        enterTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (
                                initialState.destination.route?.contains("SettingProvider") == true ||
                                initialState.destination.route?.contains("SettingSearch") == true
                            ) {
                                lateralEnterTransition(offset = { it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        },
                        exitTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (
                                targetState.destination.route?.contains("SettingProvider") == true ||
                                targetState.destination.route?.contains("SettingSearch") == true
                            ) {
                                lateralExitTransition(offset = { it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        },
                        popEnterTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (
                                initialState.destination.route?.contains("SettingProvider") == true ||
                                initialState.destination.route?.contains("SettingSearch") == true
                            ) {
                                lateralEnterTransition(offset = { it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        },
                        popExitTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (
                                targetState.destination.route?.contains("SettingProvider") == true ||
                                targetState.destination.route?.contains("SettingSearch") == true
                            ) {
                                lateralExitTransition(offset = { it }, motionPolicy = motionPolicy)
                            } else {
                                null
                            }
                        }
                    ) {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Tts) {
                            SettingProviderPage(initialTab = me.lucid.domchat.ui.pages.setting.ProvidersTab.Tts)
                        }
                    }

                    composable<Screen.SettingWeb> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Web) {
                            SettingWebPage()
                        }
                    }

                    composable<Screen.SettingMcp> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Mcp) {
                            SettingMcpPage()
                        }
                    }

                    composable<Screen.SettingRpOptimizations> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.RpOptimizations) {
                            SettingRpOptimizationsPage()
                        }
                    }

                    composable<Screen.SettingPromptInjections> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.PromptInjections) {
                            SettingPromptInjectionsPage()
                        }
                    }

                    composable<Screen.SettingPresets> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.PromptInjections) {
                            SettingPresetsPage()
                        }
                    }

                    composable<Screen.SettingLorebooks>(
                        enterTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (initialState.destination.route?.contains("SettingSkills") == true) {
                                lateralEnterTransition(
                                    offset = { it },
                                    motionPolicy = motionPolicy
                                )
                            } else {
                                null
                            }
                        },
                        exitTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (targetState.destination.route?.contains("SettingSkills") == true) {
                                lateralExitTransition(
                                    offset = { it },
                                    motionPolicy = motionPolicy
                                )
                            } else {
                                null
                            }
                        },
                        popEnterTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (initialState.destination.route?.contains("SettingSkills") == true) {
                                lateralEnterTransition(
                                    offset = { it },
                                    motionPolicy = motionPolicy
                                )
                            } else {
                                null
                            }
                        },
                        popExitTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (targetState.destination.route?.contains("SettingSkills") == true) {
                                lateralExitTransition(
                                    offset = { it },
                                    motionPolicy = motionPolicy
                                )
                            } else {
                                null
                            }
                        }
                    ) {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Lorebooks) {
                            SettingLorebooksPage()
                        }
                    }

                    composable<Screen.SettingLorebookDetail> { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.SettingLorebookDetail>()
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Lorebooks) {
                            SettingLorebookDetailPage(id = route.id, scrollToEntryId = route.scrollToEntryId)
                        }
                    }

                    composable<Screen.SettingSkills>(
                        enterTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (initialState.destination.route?.contains("SettingLorebooks") == true) {
                                lateralEnterTransition(
                                    offset = { -it },
                                    motionPolicy = motionPolicy
                                )
                            } else {
                                null
                            }
                        },
                        exitTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (targetState.destination.route?.contains("SettingLorebooks") == true) {
                                lateralExitTransition(
                                    offset = { -it },
                                    motionPolicy = motionPolicy
                                )
                            } else {
                                null
                            }
                        },
                        popEnterTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (initialState.destination.route?.contains("SettingLorebooks") == true) {
                                lateralEnterTransition(
                                    offset = { -it },
                                    motionPolicy = motionPolicy
                                )
                            } else {
                                null
                            }
                        },
                        popExitTransition = {
                            if (useWideSettingsLayout) {
                                null
                            } else if (targetState.destination.route?.contains("SettingLorebooks") == true) {
                                lateralExitTransition(
                                    offset = { -it },
                                    motionPolicy = motionPolicy
                                )
                            } else {
                                null
                            }
                        }
                    ) { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.SettingSkills>()
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Skills) {
                            SettingSkillsPage(scrollToSkillId = route.scrollToSkillId)
                        }
                    }

                    composable<Screen.Developer> {
                        DeveloperPage()
                    }

                    composable<Screen.SettingAndroidIntegration> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.AndroidIntegration) {
                            SettingAndroidIntegrationPage()
                        }
                    }

                    composable<Screen.SettingUICustomization> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.UiCustomization) {
                            SettingUICustomizationPage()
                        }
                    }

                    composable<Screen.SettingFonts> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Fonts) {
                            SettingFontsPage()
                        }
                    }

                    composable<Screen.Workspaces> {
                        AdaptiveSettingsScaffold(selected = SettingsDestination.Workspaces) {
                            WorkspacePage()
                        }
                    }

                    composable<Screen.WorkspaceDetail> { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.WorkspaceDetail>()
                        WorkspaceDetailPage(route.id)
                    }

                    composable<Screen.WorkspaceTerminal> { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.WorkspaceTerminal>()
                        WorkspaceTerminalPage(route.id)
                    }
                }
                // Toast host must be last so it renders on top of all content
                AppToasterHost(state = toastState)
                }
            }
        }
    }
}

sealed interface Screen {
    @Serializable
    data class Chat(
        val id: String,
        val text: String? = null,
        val files: List<String> = emptyList(),
        val searchQuery: String? = null,
        val persistenceMode: String? = null,
        val focusLatestMessageKey: String? = null,
    ) : Screen

    @Serializable
    data class ShareHandler(val text: String, val files: List<String> = emptyList()) : Screen

    @Serializable
    data object Setup : Screen


    @Serializable
    data object Assistant : Screen

    @Serializable
    data object CharacterHub : Screen

    @Serializable
    data class AssistantDetail(
        val id: String,
        val startRoute: String? = null,  // Navigate directly to a sub-route (e.g., "memory")
        val initialMemoryTab: Int? = null,  // 0 = Core, 1 = Episodic
        val scrollToMemoryId: Int? = null  // Memory ID to scroll to
    ) : Screen

    @Serializable
    data object Menu : Screen

    @Serializable
    data object Setting : Screen

    @Serializable
    data class Backup(val tab: String = "webdav") : Screen

    @Serializable
    data object BackupWebDav : Screen

    @Serializable
    data object BackupLocal : Screen

    @Serializable
    data object ImageGen : Screen

    @Serializable
    data class WebView(val url: String = "", val content: String = "") : Screen

    @Serializable
    data object SettingDisplay : Screen

    @Serializable
    data object SettingPersonas : Screen

    @Serializable
    data object SettingProvider : Screen

    @Serializable
    data class SettingProviderDetail(val providerId: String) : Screen

    @Serializable
    data object SettingLocalLlm : Screen

    @Serializable
    data class SettingTTSProviderDetail(val providerId: String) : Screen

    @Serializable
    data object SettingModels : Screen

    @Serializable
    data object SettingContextBudget : Screen

    @Serializable
    data object SettingAbout : Screen

    @Serializable
    data object SettingChatStorage : Screen

    @Serializable
    data object SettingChats : Screen

    @Serializable
    data object SettingSearch : Screen

    @Serializable
    data object SettingTTS : Screen

    @Serializable
    data object SettingWeb : Screen

    @Serializable
    data object SettingMcp : Screen

    @Serializable
    data object SettingRpOptimizations : Screen

    @Serializable
    data object SettingPromptInjections : Screen

    @Serializable
    data object SettingPresets : Screen

    @Serializable
    data object SettingLorebooks : Screen

    @Serializable
    data class SettingLorebookDetail(val id: String, val scrollToEntryId: String? = null) : Screen

    @Serializable
    data object Developer : Screen

    @Serializable
    data class SettingSkills(val scrollToSkillId: String? = null) : Screen

    @Serializable
    data object SettingAndroidIntegration : Screen

    @Serializable
    data object SettingUICustomization : Screen

    @Serializable
    data object SettingFonts : Screen

    @Serializable
    data object Workspaces : Screen

    @Serializable
    data class WorkspaceDetail(val id: String) : Screen

    @Serializable
    data class WorkspaceTerminal(val id: String) : Screen
}
