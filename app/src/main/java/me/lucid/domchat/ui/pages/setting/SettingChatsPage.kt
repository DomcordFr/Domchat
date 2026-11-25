package me.lucid.domchat.ui.pages.setting

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import me.lucid.domchat.ui.theme.appFieldColors
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.derivedStateOf
import me.lucid.domchat.ui.modifier.fadeEdges
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import me.lucid.domchat.utils.safeDrawableResId
import me.lucid.domchat.data.model.Avatar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.graphics.SolidColor
import me.lucid.ai.core.MessageRole
import me.lucid.ai.ui.UIMessagePart
import me.lucid.ai.ui.currentVersionMessages
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject
import me.lucid.domchat.utils.JsonInstant
import java.time.ZoneOffset
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.lucid.domchat.ui.components.dialog.SettingsDestructiveConfirmDialog
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lucid.domchat.ui.theme.appFilterChipBorder
import me.lucid.domchat.ui.theme.appFilterChipColors
import me.lucid.domchat.R
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.repository.ConversationRepository
import me.lucid.domchat.data.repository.LightConversationEntity
import me.lucid.domchat.data.sync.importer.SillyTavernImporter
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.ui.ToastAction
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.modifier.domChatDialogContainerColor
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.utils.navigateToChatPage
import org.koin.compose.koinInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.uuid.Uuid
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn

private enum class ChatSortOrder {
    RECENTLY_UPDATED,
    CREATION_DATE,
    TITLE_AZ
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingChatsPage(
    conversationRepository: ConversationRepository = koinInject(),
    settingsStore: SettingsStore = koinInject(),
) {
    val navController = LocalNavController.current
    val toaster = LocalToaster.current
    val haptics = rememberPremiumHaptics()
    val isDark = LocalDarkMode.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by settingsStore.settingsFlow.collectAsStateWithLifecycle()
    val allAssistants = remember(settings.assistants) { settings.assistants }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { fileUri ->
        fileUri?.let { uri ->
            scope.launch {
                val importedConv = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            SillyTavernImporter.import(stream, allAssistants).conversation
                        }
                    }
                }.onFailure { exception ->
                    exception.printStackTrace()
                }.getOrNull()

                if (importedConv != null) {
                    withContext(Dispatchers.IO) {
                        conversationRepository.insertConversation(importedConv)
                    }
                    toaster.show("Imported chat: ${importedConv.title}", type = ToastType.Success)
                } else {
                    toaster.show("Import failed: could not read chat file", type = ToastType.Error)
                }
            }
        }
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val assistantMap = remember(allAssistants) { allAssistants.associateBy { it.id.toString() } }

    val rawConversations by conversationRepository.getAllLight().collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedAssistantFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var sortOrder by rememberSaveable { mutableStateOf(ChatSortOrder.RECENTLY_UPDATED) }
    var showSortMenu by remember { mutableStateOf(false) }

    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedChatIds by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var managingConversation by remember { mutableStateOf<LightConversationEntity?>(null) }
    var pendingExportContent by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && pendingExportContent.isNotEmpty()) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(pendingExportContent.toByteArray(Charsets.UTF_8))
                }
                haptics.perform(HapticPattern.Success)
                toaster.show("Chat exported successfully in SillyTavern format", type = ToastType.Success)
            } catch (e: Exception) {
                haptics.perform(HapticPattern.Error)
                toaster.show("Export failed: ${e.message}", type = ToastType.Error)
            }
            pendingExportContent = ""
        }
    }

    // Filter and sort conversations
    val filteredConversations = remember(rawConversations, searchQuery, selectedAssistantFilter, sortOrder) {
        var list = rawConversations
        if (selectedAssistantFilter != null) {
            list = list.filter { it.assistantId == selectedAssistantFilter }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
        when (sortOrder) {
            ChatSortOrder.RECENTLY_UPDATED -> list.sortedWith(
                compareByDescending<LightConversationEntity> { it.isPinned }.thenByDescending { it.updateAt }
            )
            ChatSortOrder.CREATION_DATE -> list.sortedWith(
                compareByDescending<LightConversationEntity> { it.isPinned }.thenByDescending { it.createAt }
            )
            ChatSortOrder.TITLE_AZ -> list.sortedWith(
                compareByDescending<LightConversationEntity> { it.isPinned }.thenBy { it.title.lowercase() }
            )
        }
    }

    val cardBg = Color(0xFF000000)
    val cardBorder = BorderStroke(
        0.5.dp,
        if (isDark) LiquidGlassTokens.SpecularBorderSubtle else LiquidGlassTokens.LightSpecularBorder
    )

    val chatsListState = rememberLazyListState()
    val canScrollChatsBackward by remember { derivedStateOf { chatsListState.canScrollBackward } }
    val chatsTopFadeProgress by animateFloatAsState(
        targetValue = if (canScrollChatsBackward) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "chats_top_fade"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            OneUITopAppBar(
                title = "All Chats",
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedChatIds = emptySet()
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Cancel")
                        }
                    } else {
                        BackButton()
                    }
                },
                actions = {
                    if (!isSelectionMode) {
                        IconButton(onClick = {
                            importLauncher.launch(arrayOf("application/json", "application/jsonl", "text/plain", "*/*"))
                        }) {
                            Icon(Icons.Rounded.FileUpload, contentDescription = "Import chat")
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            shape = RoundedCornerShape(18.dp),
                            containerColor = Color(0xFF000000),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recently Updated") },
                                onClick = {
                                    sortOrder = ChatSortOrder.RECENTLY_UPDATED
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Creation Date") },
                                onClick = {
                                    sortOrder = ChatSortOrder.CREATION_DATE
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Title (A-Z)") },
                                onClick = {
                                    sortOrder = ChatSortOrder.TITLE_AZ
                                    showSortMenu = false
                                }
                            )
                        }

                        IconButton(onClick = {
                            isSelectionMode = true
                            haptics.perform(HapticPattern.Tick)
                        }) {
                            Icon(Icons.Rounded.SelectAll, contentDescription = "Select")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(3f)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search all chats...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White.copy(alpha = 0.65f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = me.lucid.domchat.ui.theme.AppShapes.SearchField,
                    colors = appFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(me.lucid.domchat.ui.theme.AppShapes.SearchField)
                        .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SearchField)
                )

                // Assistant Filter Chips (styled in black background + glassy border)
                if (allAssistants.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            val isSelected = selectedAssistantFilter == null
                            Surface(
                                color = Color(0xFF000000),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    if (isSelected) 1.dp else 0.5.dp,
                                    if (isSelected) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptics.perform(HapticPattern.Tick)
                                        selectedAssistantFilter = null
                                    }
                            ) {
                                Text(
                                    text = "All Characters (${rawConversations.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.70f),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                        items(allAssistants, key = { it.id.toString() }) { assistant ->
                            val assistantIdStr = assistant.id.toString()
                            val count = rawConversations.count { it.assistantId == assistantIdStr }
                            val isSelected = selectedAssistantFilter == assistantIdStr
                            val nameWords = assistant.name.trim().split(Regex("\\s+"))
                            val truncatedName = if (nameWords.size > 2) {
                                nameWords.take(2).joinToString(" ") + "..."
                            } else if (assistant.name.length > 18) {
                                assistant.name.take(16).trimEnd() + "..."
                            } else {
                                assistant.name
                            }
                            Surface(
                                color = Color(0xFF000000),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    0.5.dp,
                                    if (isSelected) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptics.perform(HapticPattern.Tick)
                                        selectedAssistantFilter = if (isSelected) null else assistantIdStr
                                    }
                            ) {
                                Text(
                                    text = "$truncatedName ($count)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.70f),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Selection Mode Action Bar
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 220, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) { height -> height / 2 } + fadeIn(
                    animationSpec = tween(durationMillis = 220, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 220, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) { height -> height / 2 } + fadeOut(
                    animationSpec = tween(durationMillis = 220, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ),
            ) {
                Surface(
                    color = Color(0xFF000000),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .domChatBlurEffect(Color(0xFF000000), RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${selectedChatIds.size} of ${filteredConversations.size} selected",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    haptics.perform(HapticPattern.Tick)
                                    selectedChatIds = if (selectedChatIds.size == filteredConversations.size) {
                                        emptySet()
                                    } else {
                                        filteredConversations.map { it.id }.toSet()
                                    }
                                }
                            ) {
                                Text(
                                    if (selectedChatIds.size == filteredConversations.size) "Deselect" else "Select All",
                                    color = if (isDark) Color(0xFFA6A6A6) else Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            FilledTonalButton(
                                enabled = selectedChatIds.isNotEmpty(),
                                onClick = { showDeleteConfirmDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFEF4444).copy(alpha = if (isDark) 0.22f else 0.12f),
                                    contentColor = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                                    disabledContainerColor = Color.White.copy(alpha = 0.04f),
                                    disabledContentColor = Color.White.copy(alpha = 0.25f)
                                ),
                                border = if (selectedChatIds.isNotEmpty()) {
                                    BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                                } else null
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Delete (${selectedChatIds.size})", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Conversations List
            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.65f).copy(alpha = 0.4f)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching conversations found" else "No conversations yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.65f).copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                AnimatedContent(
                    targetState = selectedAssistantFilter,
                    transitionSpec = {
                        val allIds = listOf<String?>(null) + allAssistants.map { it.id.toString() }
                        val initialIdx = allIds.indexOf(initialState).let { if (it == -1) 0 else it }
                        val targetIdx = allIds.indexOf(targetState).let { if (it == -1) 0 else it }
                        val forward = targetIdx >= initialIdx
                        val dir = if (forward) 1 else -1
                        (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)) { dir * it / 3 } + fadeIn(animationSpec = tween(180)))
                            .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)) { -dir * it / 3 } + fadeOut(animationSpec = tween(150)))
                    },
                    label = "chats_tab_slide"
                ) { _ ->
                    LazyColumn(
                        state = chatsListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .fadeEdges(topProgress = chatsTopFadeProgress, fadeHeight = 44f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        val isSelected = conv.id in selectedChatIds
                        val assistant = assistantMap[conv.assistantId] ?: allAssistants.firstOrNull { it.id.toString().equals(conv.assistantId, ignoreCase = true) }
                        val cardShape = RoundedCornerShape(18.dp)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(cardShape)
                                .domChatBlurEffect(containerColor = cardBg, shape = cardShape)
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedChatIds = if (isSelected) {
                                                selectedChatIds - conv.id
                                            } else {
                                                selectedChatIds + conv.id
                                            }
                                        } else {
                                            haptics.perform(HapticPattern.Tick)
                                            runCatching { Uuid.parse(conv.id) }.getOrNull()?.let { uuid ->
                                                navigateToChatPage(navController, chatId = uuid)
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        haptics.perform(HapticPattern.Buildup)
                                        if (isSelectionMode) {
                                            selectedChatIds = if (isSelected) {
                                                selectedChatIds - conv.id
                                            } else {
                                                selectedChatIds + conv.id
                                            }
                                        } else {
                                            managingConversation = conv
                                        }
                                    }
                                ),
                            shape = cardShape,
                            color = cardBg,
                            border = if (isSelected) BorderStroke(1.dp, Color.White) else cardBorder
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (isSelectionMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedChatIds = if (checked) {
                                                selectedChatIds + conv.id
                                            } else {
                                                selectedChatIds - conv.id
                                            }
                                        }
                                    )
                                }

                                if (assistant != null) {
                                    val avatarShape = RoundedCornerShape(14.dp)
                                    when (val av = assistant.avatar) {
                                        is Avatar.Image -> {
                                            AsyncImage(
                                                model = av.url,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(avatarShape)
                                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, avatarShape),
                                                contentScale = ContentScale.Crop,
                                            )
                                        }
                                        is Avatar.Resource -> {
                                            val context = androidx.compose.ui.platform.LocalContext.current
                                            val safeId = androidx.compose.runtime.remember(av.id, context) {
                                                safeDrawableResId(context, av.id, me.lucid.domchat.R.drawable.default_generical_pfp)
                                            }
                                            AsyncImage(
                                                model = safeId,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(avatarShape)
                                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, avatarShape),
                                                contentScale = ContentScale.Crop,
                                            )
                                        }
                                        is Avatar.Emoji -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(avatarShape)
                                                    .background(Color(0xFF000000))
                                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, avatarShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = av.content,
                                                    fontSize = 22.sp
                                                )
                                            }
                                        }
                                        is Avatar.Dummy -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(avatarShape)
                                                    .background(Color(0xFF000000))
                                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, avatarShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = assistant.name.firstOrNull()?.uppercase() ?: "A",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    // Row 1: Title + Pin badge (Left) & Timestamp (Right)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f, fill = false),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (conv.isPinned) {
                                                Icon(
                                                    Icons.Rounded.PushPin,
                                                    contentDescription = "Pinned",
                                                    modifier = Modifier.size(13.dp),
                                                    tint = Color(0xFFFFD60A)
                                                )
                                            }
                                            val chatTitle = conv.title.takeIf { it.isNotBlank() && it != "Untitled Conversation" && it != "New message" }
                                                ?: assistant?.chatName?.takeIf { it.isNotBlank() }
                                                ?: assistant?.displayName?.takeIf { it.isNotBlank() }
                                                ?: assistant?.name?.takeIf { it.isNotBlank() }
                                                ?: "Untitled Conversation"
                                            Text(
                                                text = chatTitle,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                ),
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = formatTimestamp(conv.updateAt),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            color = Color.White.copy(alpha = 0.50f),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    // Row 2: Character tag, Fork/Branch pill on Left & Chat counter on Right under activity
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            val characterBadgeText = assistant?.name?.trim()?.takeIf { it.isNotBlank() }
                                                ?: assistant?.displayName?.trim()?.takeIf { it.isNotBlank() }
                                                ?: assistant?.chatName?.trim()?.takeIf { it.isNotBlank() }
                                                ?: if (assistant != null) "Character" else null

                                            if (characterBadgeText != null) {
                                                Surface(
                                                    color = Color(0xFF000000),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                                ) {
                                                    Text(
                                                        text = characterBadgeText,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        ),
                                                        color = Color(0xFFF2F2F7),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier
                                                            .widthIn(max = 130.dp)
                                                            .padding(horizontal = 8.dp, vertical = 2.5.dp)
                                                    )
                                                }
                                            }

                                            if (conv.isFork) {
                                                Surface(
                                                    color = Color(0xFF64D2FF).copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(0.5.dp, Color(0xFF64D2FF).copy(alpha = 0.35f))
                                                ) {
                                                    Text(
                                                        text = "Branch",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Medium
                                                        ),
                                                        color = Color(0xFF64D2FF),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (conv.isConsolidated) {
                                                Surface(
                                                    color = Color(0xFFBF5AF2).copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(0.5.dp, Color(0xFFBF5AF2).copy(alpha = 0.35f))
                                                ) {
                                                    Text(
                                                        text = "Merged",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Medium
                                                        ),
                                                        color = Color(0xFFBF5AF2),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Chat and message counts under activity on the right
                                        val assistantChatCount = if (assistant != null) rawConversations.count { it.assistantId == conv.assistantId } else 0
                                        val countLabel = if (assistant != null) {
                                            "$assistantChatCount ${if (assistantChatCount == 1) "chat" else "chats"} · ${conv.assistantMessageCount} ${if (conv.assistantMessageCount == 1) "msg" else "msgs"}"
                                        } else {
                                            "${conv.assistantMessageCount} ${if (conv.assistantMessageCount == 1) "msg" else "msgs"}"
                                        }
                                        Text(
                                            text = countLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            color = Color.White.copy(alpha = 0.45f),
                                            maxLines = 1,
                                            softWrap = false
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

    // Delete confirmation dialog
    SettingsDestructiveConfirmDialog(
        visible = showDeleteConfirmDialog && selectedChatIds.isNotEmpty(),
        title = "Delete ${selectedChatIds.size} ${if (selectedChatIds.size == 1) "chat" else "chats"}?",
        message = "This will permanently remove the selected conversations from your database.",
        onDismiss = {
            showDeleteConfirmDialog = false
        },
        onConfirm = {
            val idsToDelete = selectedChatIds.toList()
            showDeleteConfirmDialog = false
            isSelectionMode = false
            selectedChatIds = emptySet()
            haptics.perform(HapticPattern.Thud)

            scope.launch {
                val contentResolver = context.contentResolver
                conversationRepository.deleteConversationsByIds(idsToDelete)
                toaster.show(
                    message = "Deleted ${idsToDelete.size} ${if (idsToDelete.size == 1) "conversation" else "conversations"}",
                    type = ToastType.Info
                )
            }
        },
    )

    // Chat Long-Click Management Card
    managingConversation?.let { conv ->
        val assistant = assistantMap[conv.assistantId] ?: allAssistants.firstOrNull { it.id.toString().equals(conv.assistantId, ignoreCase = true) }
        val assistantChatCount = rawConversations.count { it.assistantId == conv.assistantId }

        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = { managingConversation = null },
            modifier = Modifier.imePadding(),
            containerColor = Color(0xFF000000),
            dragHandle = {},
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = 4.5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.28f))
                    )
                }

                DomChatSheetHeader(
                    title = "Manage Chat",
                    onCloseClick = { managingConversation = null },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Header Card
                Surface(
                    color = Color(0xFF000000),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (assistant != null) {
                            UIAvatar(
                                name = assistant.name,
                                value = assistant.avatar,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val sheetChatTitle = conv.title.takeIf { it.isNotBlank() && it != "Untitled Conversation" && it != "New message" }
                                ?: assistant?.chatName?.takeIf { it.isNotBlank() }
                                ?: assistant?.displayName?.takeIf { it.isNotBlank() }
                                ?: assistant?.name?.takeIf { it.isNotBlank() }
                                ?: "Untitled Conversation"
                            Text(
                                text = sheetChatTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (assistant != null) {
                                    Text(
                                        text = "${assistant.name.trim()} · $assistantChatCount chats · ${conv.assistantMessageCount} msgs",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                                Text(
                                    text = formatTimestamp(conv.updateAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Action Items Card
                Surface(
                    color = Color(0xFF000000),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Open Chat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val targetId = conv.id
                                    managingConversation = null
                                    haptics.perform(HapticPattern.Tick)
                                    runCatching { Uuid.parse(targetId) }.getOrNull()?.let { uuid ->
                                        navigateToChatPage(navController, chatId = uuid)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.Forum, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("Open Chat", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Color.White)
                        }

                        // Export in SillyTavern format
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val targetConv = conv
                                    managingConversation = null
                                    haptics.perform(HapticPattern.Pop)
                                    scope.launch {
                                        runCatching { Uuid.parse(targetConv.id) }.getOrNull()?.let { uuid ->
                                            val fullConv = withContext(Dispatchers.IO) {
                                                conversationRepository.getConversationById(uuid)
                                            }
                                            if (fullConv != null) {
                                                val userNickname = settings.displaySetting.userNickname.ifBlank {
                                                    settings.displaySetting.getActivePersona().name.ifBlank { "User" }
                                                }
                                                val charName = assistant?.name ?: "Character"
                                                pendingExportContent = exportConversationToSillyTavern(fullConv, charName, userNickname)
                                                val cleanTitle = targetConv.title.ifBlank { "Chat" }.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                                                exportLauncher.launch("${cleanTitle}.jsonl")
                                            } else {
                                                toaster.show("Could not load full conversation for export", type = ToastType.Error)
                                            }
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Export Chat (SillyTavern)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Color.White)
                                Text("Exports as .jsonl compatible with SillyTavern", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                            }
                        }

                        // Toggle Pin
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val targetConv = conv
                                    managingConversation = null
                                    haptics.perform(HapticPattern.Tick)
                                    scope.launch {
                                        runCatching { Uuid.parse(targetConv.id) }.getOrNull()?.let { uuid ->
                                            withContext(Dispatchers.IO) {
                                                conversationRepository.togglePinStatus(uuid)
                                            }
                                            toaster.show(if (targetConv.isPinned) "Chat unpinned" else "Chat pinned")
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.PushPin, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text(if (conv.isPinned) "Unpin Chat" else "Pin Chat to Top", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Color.White)
                        }

                        // Select Chat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    isSelectionMode = true
                                    selectedChatIds = setOf(conv.id)
                                    managingConversation = null
                                    haptics.perform(HapticPattern.Tick)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.SelectAll, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("Select (Batch Actions)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Color.White)
                        }

                        // Delete Chat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val targetConv = conv
                                    managingConversation = null
                                    selectedChatIds = setOf(targetConv.id)
                                    showDeleteConfirmDialog = true
                                    haptics.perform(HapticPattern.Buildup)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            Text("Delete Chat", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Color(0xFFEF4444))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private fun exportConversationToSillyTavern(
    conversation: Conversation,
    assistantName: String,
    userName: String
): String {
    val messages = conversation.messageNodes.currentVersionMessages()
    val lines = mutableListOf<String>()

    val headerObj = buildJsonObject {
        put("user_name", userName)
        put("character_name", assistantName)
        put("title", conversation.title)
        put("create_date", conversation.createAt.toString())
        put("chat_metadata", buildJsonObject {})
    }
    lines.add(JsonInstant.encodeToString(JsonObject.serializer(), headerObj))

    for (node in messages) {
        val text = node.parts.filterIsInstance<UIMessagePart.Text>().joinToString("\n") { it.text }
        val isUser = node.role == MessageRole.USER
        val senderName = if (isUser) userName else assistantName
        val epochMillis = runCatching {
            node.createdAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        }.getOrDefault(System.currentTimeMillis())

        val msgObj = buildJsonObject {
            put("name", senderName)
            put("is_user", isUser)
            put("is_name", true)
            put("send_date", epochMillis)
            put("mes", text)
            put("extra", buildJsonObject {})
        }
        lines.add(JsonInstant.encodeToString(JsonObject.serializer(), msgObj))
    }
    return lines.joinToString("\n")
}

private fun formatTimestamp(epochMillis: Long): String {
    val instant = Instant.ofEpochMilli(epochMillis)
    val zone = ZoneId.systemDefault()
    val ldt = instant.atZone(zone).toLocalDateTime()
    val today = LocalDate.now(zone)
    val chatDate = ldt.toLocalDate()

    val hour12 = if (ldt.hour % 12 == 0) 12 else ldt.hour % 12
    val amPm = if (ldt.hour < 12) "AM" else "PM"
    val timeStr = "%d:%02d %s".format(hour12, ldt.minute, amPm)

    return when {
        chatDate == today -> "Today $timeStr"
        chatDate == today.minusDays(1) -> "Yesterday $timeStr"
        chatDate.year == today.year -> "%s %d, %s".format(
            ldt.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
            ldt.dayOfMonth,
            timeStr
        )
        else -> "%s %d, %d".format(
            ldt.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
            ldt.dayOfMonth,
            ldt.year
        )
    }
}
