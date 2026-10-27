package me.lucid.domchat.ui.pages.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import me.lucid.domchat.R
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.repository.ConversationRepository
import me.lucid.domchat.data.repository.LightConversationEntity
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
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val settings by settingsStore.settingsFlow.collectAsStateWithLifecycle()
    val allAssistants = remember(settings.assistants) { settings.assistants }
    val assistantMap = remember(allAssistants) { allAssistants.associateBy { it.id.toString() } }

    val rawConversations by conversationRepository.getAllLight().collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedAssistantFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var sortOrder by rememberSaveable { mutableStateOf(ChatSortOrder.RECENTLY_UPDATED) }
    var showSortMenu by remember { mutableStateOf(false) }

    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedChatIds by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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

    val cardBg = domChatCardContainerColor()
    val cardBorder = BorderStroke(
        0.5.dp,
        if (isDark) LiquidGlassTokens.SpecularBorderSubtle else LiquidGlassTokens.LightSpecularBorder
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
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            shape = RoundedCornerShape(18.dp),
                            containerColor = if (isDark) Color(0xFF141418) else MaterialTheme.colorScheme.surfaceContainerHigh,
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
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search all chats...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Assistant Filter Chips
                if (allAssistants.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedAssistantFilter == null,
                                onClick = { selectedAssistantFilter = null },
                                label = { Text("All Characters (${rawConversations.size})") },
                                shape = RoundedCornerShape(50)
                            )
                        }
                        items(allAssistants, key = { it.id.toString() }) { assistant ->
                            val assistantIdStr = assistant.id.toString()
                            val count = rawConversations.count { it.assistantId == assistantIdStr }
                            FilterChip(
                                selected = selectedAssistantFilter == assistantIdStr,
                                onClick = {
                                    selectedAssistantFilter = if (selectedAssistantFilter == assistantIdStr) null else assistantIdStr
                                },
                                label = { Text("${assistant.name} ($count)") },
                                shape = RoundedCornerShape(50)
                            )
                        }
                    }
                }
            }

            // Selection Mode Action Bar
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = if (isDark) Color(0xFF141522).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .domChatBlurEffect(if (isDark) Color(0xFF141522) else Color.White, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f))
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
                            color = if (isDark) Color.White else Color.Black
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
                                    color = if (isDark) Color(0xFF93C5FD) else MaterialTheme.colorScheme.primary,
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching conversations found" else "No conversations yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        val isSelected = conv.id in selectedChatIds
                        val assistant = assistantMap[conv.assistantId]
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
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedChatIds = setOf(conv.id)
                                        }
                                    }
                                ),
                            shape = cardShape,
                            color = cardBg,
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else cardBorder
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
                                    UIAvatar(
                                        name = assistant.name,
                                        value = assistant.avatar,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (conv.isPinned) {
                                            Icon(
                                                Icons.Rounded.PushPin,
                                                contentDescription = "Pinned",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = conv.title.ifBlank { "Untitled Conversation" },
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (assistant != null) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = assistant.name,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = formatTimestamp(conv.updateAt),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
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

    // Delete confirmation dialog
    if (showDeleteConfirmDialog && selectedChatIds.isNotEmpty()) {
        val count = selectedChatIds.size
        ApplyDialogWindowBlur()
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = if (isDark) Color(0xFF141520).copy(alpha = 0.95f) else Color.White,
            title = { Text("Delete $count ${if (count == 1) "chat" else "chats"}?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove the selected conversations from your database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idsToDelete = selectedChatIds.toList()
                        showDeleteConfirmDialog = false
                        isSelectionMode = false
                        selectedChatIds = emptySet()
                        haptics.perform(HapticPattern.Thud)

                        scope.launch {
                            conversationRepository.deleteConversationsByIds(idsToDelete)
                            toaster.show(
                                message = "Deleted $count ${if (count == 1) "conversation" else "conversations"}",
                                type = ToastType.Info
                            )
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f))
                }
            }
        )
    }
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
