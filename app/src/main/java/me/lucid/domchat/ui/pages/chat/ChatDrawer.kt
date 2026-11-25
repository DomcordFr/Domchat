package me.lucid.domchat.ui.pages.chat

import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.theme.liquidInteractiveGlass
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatChromeSurfaceColor

import androidx.core.net.toUri
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.lucid.domchat.R
import me.lucid.domchat.Screen
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.getAssistantById
import me.lucid.domchat.data.datastore.getCurrentAssistant
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.ui.components.dialog.UniversalDestructiveConfirmDialog
import me.lucid.domchat.service.ChatPersistenceMode
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.domchat.ui.components.chat.PersonaPickerBottomSheet
import me.lucid.domchat.ui.components.ui.Greeting
import me.lucid.domchat.ui.components.ui.Tooltip
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.components.nav.DomChatModalDrawerSheet
import me.lucid.domchat.ui.components.nav.DomChatDrawerAction
import me.lucid.domchat.ui.components.nav.DomChatSettingsIcon
import me.lucid.domchat.ui.components.nav.DomChatBarChartIcon
import me.lucid.domchat.ui.components.nav.DomChatDrawerQuickAction
import me.lucid.domchat.ui.components.nav.DomChatDrawerQuickActionGroup

import me.lucid.domchat.ui.hooks.rememberAvatarShape
import me.lucid.domchat.ui.hooks.ChatInputState
import me.lucid.domchat.ui.hooks.EditStateContent
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberIsPlayStoreVersion
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.useEditState
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.modifier.onClick
import me.lucid.domchat.utils.navigateToChatPage
import me.lucid.domchat.utils.toDp
import me.lucid.domchat.BuildConfig
import me.lucid.domchat.utils.Version
import me.lucid.domchat.utils.onSuccess
import coil3.compose.AsyncImage
import me.lucid.domchat.utils.safeDrawableResId
import kotlin.uuid.Uuid

@Composable
fun ChatDrawerContent(
    navController: NavHostController,
    vm: ChatVM,
    settings: Settings,
    current: Conversation,
    inputState: ChatInputState,
    activePersistenceMode: ChatPersistenceMode,
    drawerState: androidx.compose.material3.DrawerState? = null,  // Optional for animated close
    presentation: ChatDrawerPresentation = ChatDrawerPresentation.Modal,
    collapsedWidth: Dp = 320.dp,
    expandedWidth: Dp = 600.dp,
    onCollapseRequest: (() -> Unit)? = null,
    onOpenAssistantPicker: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val toaster = me.lucid.domchat.ui.context.LocalToaster.current
    val isPlayStore = rememberIsPlayStoreVersion()
    val currentAssistant = settings.getAssistantById(current.assistantId) ?: settings.getCurrentAssistant()

    // Search expansion state - hoisted here so drawer width can animate
    var isSearchExpanded by remember(vm) { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<Conversation?>(null) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    val drawerWidth by animateDpAsState(
        targetValue = if (isSearchExpanded) expandedWidth else collapsedWidth,
        animationSpec = if (isSearchExpanded) {
            spring(dampingRatio = 0.8f, stiffness = 400f)
        } else {
            androidx.compose.animation.core.tween(durationMillis = 250, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        },
        label = "drawer_width"
    )

    val conversations = vm.conversations.collectAsLazyPagingItems()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()

    val conversationJobs by vm.conversationJobs.collectAsStateWithLifecycle(
        initialValue = emptyMap(),
    )
    var completedGenerationIds by remember { mutableStateOf(emptySet<Uuid>()) }
    val viewingConversationId by rememberUpdatedState(current.id)

    LaunchedEffect(vm) {
        vm.generationDoneFlow.collect { conversationId ->
            completedGenerationIds = completedGenerationIdsAfterDone(
                currentCompleted = completedGenerationIds,
                finishedConversationId = conversationId,
                viewingConversationId = viewingConversationId,
            )
        }
    }
    LaunchedEffect(current.id) {
        completedGenerationIds = completedGenerationIds - current.id
    }
    LaunchedEffect(conversationJobs.keys.toSet()) {
        completedGenerationIds = completedGenerationIds - conversationJobs.keys
    }

    val recentlyRestoredIds by vm.recentlyRestoredIds.collectAsStateWithLifecycle()

    fun dismissDrawerAfterSelection() {
        when (presentation) {
            ChatDrawerPresentation.Modal -> drawerState?.let { state ->
                scope.launch { state.close() }
            }

            ChatDrawerPresentation.PermanentPane -> onCollapseRequest?.invoke()
        }
    }

    // 昵称编辑状态
    val nicknameEditState = useEditState<String> { newNickname ->
        val activeP = settings.displaySetting.getActivePersona()
        val updatedPersonas = settings.displaySetting.personas.map {
            if (it.id == activeP.id) it.copy(name = newNickname) else it
        }
        vm.updateSettings(
            settings.copy(
                displaySetting = settings.displaySetting.copy(
                    userNickname = newNickname,
                    personas = updatedPersonas
                )
            )
        )
    }

    val drawerContent: @Composable () -> Unit = {
        val isDark = LocalDarkMode.current
        Column(
            modifier = Modifier.statusBarsPadding().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val activePersona = settings.displaySetting.getActivePersona()
            val displayName = activePersona.name.trim().takeIf { it.isNotBlank() }
                ?: settings.displaySetting.userNickname.trim().takeIf { it.isNotBlank() && it != "User" }
                ?: "User"

            // Glassy User Profile Header (Static display, non-interactive)
            val profileCardShape = RoundedCornerShape(20.dp)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassSurface(
                        shape = profileCardShape,
                        fillColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.25f),
                        borderWidth = 0.5.dp,
                        specularBrush = if (isDark) LiquidGlassTokens.SpecularBorderSubtle else LiquidGlassTokens.LightSpecularBorder,
                    ),
                color = Color.Transparent,
                shape = profileCardShape,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    val headerActionColor = domChatChromeSurfaceColor()
                    UIAvatar(
                        name = displayName,
                        value = activePersona.avatar,
                        onUpdate = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Greeting(
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = if (isDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            assistant = currentAssistant
                        )
                    }

                    if (onCollapseRequest != null) {
                        DrawerAction(
                            icon = {
                                Icon(Icons.Rounded.ChevronLeft, null)
                            },
                            label = { Text(stringResource(R.string.activity_timeline_collapse)) },
                            onClick = onCollapseRequest,
                            containerColor = headerActionColor,
                            size = 38.dp
                        )
                    }
                }
            }

            fun navigateToAssistantConversation(assistant: me.lucid.domchat.data.model.Assistant) {
                scope.launch {
                    val targetAssistant = if (assistant.isDeleted) {
                        settings.getCurrentAssistant()
                    } else {
                        assistant
                    }
                    val draft = inputState.toDraft()
                    val newConversation = vm.createConversationForAssistant(targetAssistant.id)
                    ChatSessionDraftStore.moveDraft(
                        fromConversationId = current.id,
                        toConversationId = newConversation.id,
                        draft = draft,
                        isTemporary = (activePersistenceMode == ChatPersistenceMode.TEMPORARY),
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
                    dismissDrawerAfterSelection()
                }
            }
            val defaultAssistantName = stringResource(R.string.assistant_page_default_assistant)
            val assistantMap = remember(settings.assistants) { settings.assistants.associateBy { it.id } }

            ConversationList(
                current = current,
                conversations = conversations,
                conversationJobs = conversationJobs.keys,
                completedGenerationIds = completedGenerationIds,
                recentlyRestoredIds = recentlyRestoredIds,
                searchQuery = searchQuery,
                onSearchQueryChange = { vm.updateSearchQuery(it) },
                isSearchExpanded = isSearchExpanded,
                onSearchExpandedChange = { isSearchExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onClick = {
                    completedGenerationIds = completedGenerationIds - it.id
                    // Only pass search query if the match was from message content (not title)
                    // This scrolls to the matching message; for title matches, just open normally
                    val selectedSearchQuery = searchQuery.ifBlank { null }
                    val titleMatches = selectedSearchQuery != null &&
                        it.title.contains(selectedSearchQuery, ignoreCase = true)
                    vm.updateSearchQuery("")
                    isSearchExpanded = false
                    navigateToChatPage(
                        navController,
                        it.id,
                        searchQuery = if (titleMatches) null else selectedSearchQuery,
                    )
                    dismissDrawerAfterSelection()
                },
                onRegenerateTitle = {
                    vm.generateTitle(it, true)
                },
                onEditTitle = { conversation, title ->
                    vm.updateConversationTitle(conversation, title)
                },
                onDelete = {
                    conversationToDelete = it
                    showConfirmDelete = true
                },
                onPin = {
                    vm.updatePinnedStatus(it)
                },
                getAssistant = { assistantMap[it] },
                // Imagine + Stats buttons (visibility handled by ConversationList)
                quickActions = {
                    val haptics = rememberPremiumHaptics()
                    val qaGlassFill = if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f)
                    DomChatDrawerQuickActionGroup {
                        DomChatDrawerQuickAction(
                            label = "Home",
                            modifier = Modifier.liquidGlassSurface(shape = RoundedCornerShape(12.dp), borderWidth = 0.5.dp, fillColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.30f)),
                            containerColor = Color.Transparent,
                            onClick = {
                                navigateToChatPage(navController)
                                dismissDrawerAfterSelection()
                            },
                            onHaptic = { haptics.perform(HapticPattern.Tick) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        )
                        DomChatDrawerQuickAction(
                            label = stringResource(R.string.menu_statistics_title),
                            modifier = Modifier.liquidGlassSurface(shape = RoundedCornerShape(12.dp), borderWidth = 0.5.dp, fillColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.30f)),
                            containerColor = Color.Transparent,
                            onClick = {
                                navController.navigate(Screen.Menu)
                                dismissDrawerAfterSelection()
                            },
                            onHaptic = { haptics.perform(HapticPattern.Tick) },
                            icon = { DomChatBarChartIcon(contentDescription = null, tint = if (isDark) Color(0xFFF2F2F7) else Color.Unspecified) },
                        )
                        DomChatDrawerQuickAction(
                            label = "JanitorAI Hub",
                            modifier = Modifier.liquidGlassSurface(shape = RoundedCornerShape(12.dp), borderWidth = 0.5.dp, fillColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.30f)),
                            containerColor = Color.Transparent,
                            onClick = {
                                navController.navigate(Screen.SettingJanitor)
                                dismissDrawerAfterSelection()
                            },
                            onHaptic = { haptics.perform(HapticPattern.Tick) },
                            icon = {
                                Image(
                                    painter = painterResource(id = R.drawable.janitorai_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            },
                        )
                        DomChatDrawerQuickAction(
                            label = "Character Hub",
                            modifier = Modifier.liquidGlassSurface(shape = RoundedCornerShape(12.dp), borderWidth = 0.5.dp, fillColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.30f)),
                            containerColor = Color.Transparent,
                            onClick = {
                                navController.navigate(Screen.CharacterHub)
                                dismissDrawerAfterSelection()
                            },
                            onHaptic = { haptics.perform(HapticPattern.Tick) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        )
                    }
                },
                bottomContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .zIndex(1f),
                    ) {
                        val actionButtonSize = 42.dp
                        val assistantAvatarSize = 30.dp
                        val itemColor = domChatChromeSurfaceColor()
                        val haptics = rememberPremiumHaptics()
                        // Guard against a missing assistant/persona so the bottom character
                        // chip never crashes the drawer composition or navigation.
                        val assistantName = currentAssistant?.name?.ifEmpty { defaultAssistantName } ?: defaultAssistantName

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(actionButtonSize)
                                .liquidGlassSurface(
                                    shape = me.lucid.domchat.ui.theme.AppShapes.ButtonPill,
                                    fillColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.25f),
                                    borderWidth = 0.5.dp,
                                    specularBrush = if (isDark) LiquidGlassTokens.SpecularBorderSubtle else LiquidGlassTokens.LightSpecularBorder,
                                )
                                .clickable {
                                    haptics.perform(HapticPattern.Pop)
                                    // Await drawer close animation completion before opening the
                                    // assistant picker so drawer sliding and sheet loading do not
                                    // fight on the UI thread.
                                    scope.launch {
                                        when (presentation) {
                                            ChatDrawerPresentation.Modal -> drawerState?.close()
                                            ChatDrawerPresentation.PermanentPane -> onCollapseRequest?.invoke()
                                        }
                                        onOpenAssistantPicker()
                                    }
                                },
                            color = Color.Transparent,
                            shape = me.lucid.domchat.ui.theme.AppShapes.ButtonPill,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 12.dp, end = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Text(
                                        text = assistantName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(assistantAvatarSize)
                                        .clip(rememberAvatarShape(false)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    DrawerAvatarVisual(
                                        name = assistantName,
                                        avatar = currentAssistant.avatar,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }

                        DrawerAction(
                            icon = {
                                DomChatSettingsIcon(contentDescription = null)
                            },
                            label = { Text(stringResource(R.string.settings)) },
                            onClick = {
                                navController.navigate(Screen.Setting)
                                dismissDrawerAfterSelection()
                            },
                            containerColor = itemColor,
                            border = null,
                            size = actionButtonSize,
                        )
                    }
                },
            )
        }
    }

    // Drawer container & blur
    val drawerBg = domChatSheetContainerColor()
    val drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)

    when (presentation) {
        ChatDrawerPresentation.Modal -> {
            val drawerModifier = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                Modifier
                    .widthIn(max = drawerWidth)
                    .frostedGlassSurface(shape = drawerShape)
            } else {
                Modifier
                    .widthIn(max = drawerWidth)
                    .liquidGlassSurface(shape = drawerShape)
            }
            DomChatModalDrawerSheet(
                drawerShape = drawerShape,
                drawerContainerColor = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                    Color(0xFF101014).copy(alpha = settings.displaySetting.glassAlpha.coerceIn(0.15f, 0.85f))
                } else {
                    Color.Transparent
                },
                modifier = drawerModifier,
            ) {
                drawerContent()
            }
        }

        ChatDrawerPresentation.PermanentPane -> {
            val drawerModifier = if (settings.displaySetting.liquidGlassMode == me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED) {
                Modifier
                    .fillMaxHeight()
                    .width(drawerWidth)
                    .statusBarsPadding()
                    .frostedGlassSurface(shape = drawerShape)
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
            } else {
                Modifier
                    .fillMaxHeight()
                    .width(drawerWidth)
                    .statusBarsPadding()
                    .liquidGlassSurface(shape = drawerShape)
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
            }
            Surface(
                modifier = drawerModifier,
                shape = drawerShape,
                color = Color.Transparent,
                tonalElevation = 1.dp,
            ) {
                drawerContent()
            }
        }
    }

    nicknameEditState.EditStateContent { nickname, onUpdate ->
        AlertDialog(
            onDismissRequest = {
                nicknameEditState.dismiss()
            },
            title = {
                Text(stringResource(R.string.chat_page_edit_nickname))
            },
            text = {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = onUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.chat_page_nickname_placeholder)) },
                    shape = me.lucid.domchat.ui.theme.AppShapes.InputField
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        nicknameEditState.confirm()
                    }
                ) {
                    Text(stringResource(R.string.chat_page_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        nicknameEditState.dismiss()
                    }
                ) {
                    Text(stringResource(R.string.chat_page_cancel))
                }
            }
        )
    }

    conversationToDelete?.let { target ->
        UniversalDestructiveConfirmDialog(
            visible = showConfirmDelete,
            title = "Delete conversation?",
            message = "This will permanently remove \"${target.title.ifBlank { "Unnamed conversation" }}\" from your history.",
            onConfirm = {
                vm.deleteConversation(target)
                toaster.show(
                    message = context.getString(R.string.conversation_deleted),
                    duration = me.lucid.domchat.data.deletion.DESTRUCTIVE_UNDO_WINDOW_MS,
                    action = me.lucid.domchat.ui.components.ui.ToastAction(
                        label = context.getString(R.string.undo),
                        onClick = {
                            if (!vm.undoDeleteConversation(target.id)) {
                                toaster.show(
                                    message = context.getString(R.string.undo_no_longer_available),
                                    type = me.lucid.domchat.ui.components.ui.ToastType.Error,
                                )
                            }
                        }
                    )
                )
                if (target.id == current.id) {
                    navigateToChatPage(navController)
                    dismissDrawerAfterSelection()
                }
                conversationToDelete = null
                showConfirmDelete = false
            },
            onDismiss = {
                conversationToDelete = null
                showConfirmDelete = false
            }
        )
    }
}

enum class ChatDrawerPresentation {
    Modal,
    PermanentPane
}

@Composable
fun CollapsedChatSideRail(
    current: Conversation,
    settings: Settings,
    onExpand: () -> Unit,
    onOpenImageGen: () -> Unit = {},
    onOpenStatistics: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAssistant: () -> Unit,
    vm: ChatVM,
    modifier: Modifier = Modifier,
) {
    val currentAssistant = settings.getAssistantById(current.assistantId) ?: settings.getCurrentAssistant()
    val defaultAssistantName = stringResource(R.string.assistant_page_default_assistant)
    val assistantName = currentAssistant.name.ifEmpty { defaultAssistantName }
    val isPlayStore = rememberIsPlayStoreVersion()

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .statusBarsPadding()
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
        shape = me.lucid.domchat.ui.theme.AppShapes.ButtonPill,
        color = domChatChromeSurfaceColor(),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DrawerAction(
                icon = { Icon(Icons.Rounded.ChevronRight, null) },
                label = { Text(stringResource(R.string.activity_timeline_expand)) },
                onClick = onExpand,
                containerColor = domChatChromeSurfaceColor(),
                size = 48.dp
            )
            Column(
                modifier = Modifier.clip(me.lucid.domchat.ui.theme.AppShapes.CardMedium),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawerAction(
                    icon = { DomChatBarChartIcon(contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_statistics_title)) },
                    onClick = onOpenStatistics,
                    containerColor = domChatChromeSurfaceColor(),
                    shape = RoundedCornerShape(10.dp),
                    size = 48.dp
                )
            }

            Spacer(Modifier.weight(1f))

            Surface(
                onClick = onOpenAssistant,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    DrawerAvatarVisual(
                        name = assistantName,
                        avatar = currentAssistant.avatar,
                        modifier = Modifier.fillMaxSize(),
                        forceCircle = true
                    )
                }
            }

            DrawerAction(
                icon = { DomChatSettingsIcon(contentDescription = null) },
                label = { Text(stringResource(R.string.settings)) },
                onClick = onOpenSettings,
                containerColor = domChatChromeSurfaceColor(),
                size = 48.dp
            )
        }
    }
}

@Composable
private fun DrawerAction(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    containerColor: Color = Color(0xFF000000).copy(alpha = 0.15f),
    border: androidx.compose.foundation.BorderStroke? = null,
    shape: Shape = CircleShape,
    size: Dp = 42.dp,
) {
    val haptics = rememberPremiumHaptics()
    DomChatDrawerAction(
        onClick = onClick,
        onHaptic = { haptics.perform(HapticPattern.Pop) },
        modifier = modifier
            .liquidGlassSurface(
                shape = shape,
                borderWidth = 0.5.dp,
                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
            )
            .liquidInteractiveGlass(scaleLiftDp = 2.dp),
        containerColor = containerColor,
        border = border,
        shape = shape,
        size = size,
    ) {
        containerSize, iconSize ->
        Tooltip(tooltip = { label() }) {
            Box(
                modifier = Modifier.size(containerSize),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(iconSize)) {
                    icon()
                }
            }
        }
    }
}

@Composable
private fun DrawerAvatarVisual(
    name: String,
    avatar: Avatar,
    modifier: Modifier = Modifier,
    forceCircle: Boolean = false
) {
    Box(
        modifier = modifier.clip(if (forceCircle) CircleShape else rememberAvatarShape(false)),
        contentAlignment = Alignment.Center
    ) {
        when (avatar) {
            is Avatar.Image -> {
                AsyncImage(
                    model = avatar.url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            is Avatar.Resource -> {
                val context = androidx.compose.ui.platform.LocalContext.current
                val safeId = androidx.compose.runtime.remember(avatar.id, context) {
                    safeDrawableResId(context, avatar.id, me.lucid.domchat.R.drawable.default_generical_pfp)
                }
                AsyncImage(
                    model = safeId,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            is Avatar.Emoji -> {
                Text(
                    text = avatar.content,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            is Avatar.Dummy -> {
                Text(
                    text = name
                        .ifBlank { "A" }
                        .take(1)
                        .uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}
