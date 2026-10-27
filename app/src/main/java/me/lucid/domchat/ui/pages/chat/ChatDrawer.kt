package me.lucid.domchat.ui.pages.chat

import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.modifier.domChatBlurEffect

import androidx.core.net.toUri
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import me.lucid.domchat.service.ChatPersistenceMode
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.domchat.ui.components.chat.PersonaPickerBottomSheet
import me.lucid.domchat.ui.components.ui.Greeting
import me.lucid.domchat.ui.components.ui.Tooltip
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.components.nav.AetherModalDrawerSheet
import me.lucid.domchat.ui.components.nav.AetherDrawerAction
import me.lucid.domchat.ui.components.nav.DomChatSettingsIcon
import me.lucid.domchat.ui.components.nav.DomChatBarChartIcon
import me.lucid.domchat.ui.components.nav.AetherDrawerQuickAction
import me.lucid.domchat.ui.components.nav.AetherDrawerQuickActionGroup

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
            val displayName = settings.displaySetting.userNickname.ifBlank { activePersona.name.ifBlank { "User" } }

            // Glassy User Profile Header (Static display, non-interactive)
            val profileCardShape = RoundedCornerShape(20.dp)
            val profileCardBg = domChatCardContainerColor()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .domChatBlurEffect(containerColor = profileCardBg, shape = profileCardShape),
                color = profileCardBg,
                shape = profileCardShape,
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isDark) me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle else me.lucid.domchat.ui.theme.LiquidGlassTokens.LightSpecularBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    val headerActionColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    UIAvatar(
                        name = displayName,
                        value = activePersona.avatar,
                        onUpdate = null,
                        modifier = Modifier.size(48.dp),
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
                            if (activePersona.pronouns.isNotBlank()) {
                                Surface(
                                    color = Color(0xFFA78BFA).copy(alpha = 0.20f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = activePersona.pronouns,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                        ),
                                        color = Color(0xFFA78BFA),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
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
                    val draft = inputState.toDraft()
                    val newConversation = vm.createConversationForAssistant(assistant.id)
                    ChatSessionDraftStore.moveDraft(
                        fromConversationId = current.id,
                        toConversationId = newConversation.id,
                        draft = draft,
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
                    vm.deleteConversation(it)
                    toaster.show(
                        message = context.getString(R.string.conversation_deleted),
                        duration = me.lucid.domchat.data.deletion.DESTRUCTIVE_UNDO_WINDOW_MS,
                        action = me.lucid.domchat.ui.components.ui.ToastAction(
                            label = context.getString(R.string.undo),
                            onClick = {
                                if (!vm.undoDeleteConversation(it.id)) {
                                    toaster.show(
                                        message = context.getString(R.string.undo_no_longer_available),
                                        type = me.lucid.domchat.ui.components.ui.ToastType.Error,
                                    )
                                }
                            }
                        )
                    )
                    if (it.id == current.id) {
                        navigateToChatPage(navController)
                        dismissDrawerAfterSelection()
                    }
                },
                onPin = {
                    vm.updatePinnedStatus(it)
                },
                getAssistant = { assistantMap[it] },
                // Imagine + Stats buttons (visibility handled by ConversationList)
                quickActions = {
                    val haptics = rememberPremiumHaptics()
                    val quickActionCardColor = domChatCardContainerColor()
                    AetherDrawerQuickActionGroup {
                        AetherDrawerQuickAction(
                            label = stringResource(R.string.chat_drawer_imagine),
                            containerColor = quickActionCardColor,
                            onClick = {
                                navController.navigate(Screen.ImageGen)
                                dismissDrawerAfterSelection()
                            },
                            onHaptic = { haptics.perform(HapticPattern.Tick) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        )
                        AetherDrawerQuickAction(
                            label = stringResource(R.string.menu_statistics_title),
                            containerColor = quickActionCardColor,
                            onClick = {
                                navController.navigate(Screen.Menu)
                                dismissDrawerAfterSelection()
                            },
                            onHaptic = { haptics.perform(HapticPattern.Tick) },
                            icon = { DomChatBarChartIcon(contentDescription = null, tint = if (isDark) Color(0xFFF2F2F7) else Color.Unspecified) },
                        )
                    }
                },
                bottomContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .zIndex(1f),
                    ) {
                        val actionButtonSize = 42.dp
                        val assistantAvatarSize = 30.dp
                        val itemColor = domChatCardContainerColor()
                        val pillBorder = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                        )
                        val haptics = rememberPremiumHaptics()
                        val assistantName = currentAssistant.name.ifEmpty { defaultAssistantName }

                        Surface(
                            color = itemColor,
                            shape = me.lucid.domchat.ui.theme.AppShapes.ButtonPill,
                            border = pillBorder,
                            modifier = Modifier
                                .weight(1f)
                                .height(actionButtonSize)
                                .domChatBlurEffect(itemColor, me.lucid.domchat.ui.theme.AppShapes.ButtonPill)
                                .clip(me.lucid.domchat.ui.theme.AppShapes.ButtonPill)
                                .clickable {
                                    haptics.perform(HapticPattern.Pop)
                                    navController.navigate(Screen.AssistantDetail(id = currentAssistant.id.toString()))
                                    dismissDrawerAfterSelection()
                                },
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
                            border = pillBorder,
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
            AetherModalDrawerSheet(
                drawerShape = drawerShape,
                drawerContainerColor = Color.Transparent,
                modifier = Modifier
                    .widthIn(max = drawerWidth)
                    .domChatBlurEffect(containerColor = drawerBg, shape = drawerShape),
            ) {
                drawerContent()
            }
        }

        ChatDrawerPresentation.PermanentPane -> {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(drawerWidth)
                    .statusBarsPadding()
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                    .domChatBlurEffect(containerColor = drawerBg, shape = RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
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
    onOpenImageGen: () -> Unit,
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
        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                size = 48.dp
            )
            Column(
                modifier = Modifier.clip(me.lucid.domchat.ui.theme.AppShapes.CardMedium),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawerAction(
                    icon = { Icon(Icons.Rounded.Image, null) },
                    label = { Text(stringResource(R.string.chat_drawer_imagine)) },
                    onClick = onOpenImageGen,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(10.dp),
                    size = 48.dp
                )
                DrawerAction(
                    icon = { DomChatBarChartIcon(contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_statistics_title)) },
                    onClick = onOpenStatistics,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    border: androidx.compose.foundation.BorderStroke? = null,
    shape: Shape = CircleShape,
    size: Dp = 42.dp,
) {
    val haptics = rememberPremiumHaptics()
    AetherDrawerAction(
        onClick = onClick,
        onHaptic = { haptics.perform(HapticPattern.Pop) },
        modifier = modifier,
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
                AsyncImage(
                    model = avatar.id,
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
