package me.lucid.domchat.ui.pages.assistant.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.lucid.ai.core.MessageRole
import me.lucid.ai.ui.UIMessage
import me.lucid.ai.ui.UIMessagePart
import me.lucid.domchat.R
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.components.ui.DomChatMarkdownEditor
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.components.ui.PhysicsSwipeToDelete
import me.lucid.domchat.ui.components.ui.ItemPosition
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.pages.setting.components.SettingGroupItem
import me.lucid.domchat.ui.pages.setting.components.SettingsGroup
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.appFieldColors
import me.lucid.domchat.ui.theme.appFilterChipBorder
import me.lucid.domchat.ui.theme.appFilterChipColors
import me.lucid.domchat.ui.theme.liquidGlassSurface
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * Initial Messages sub-page - Focused on managing multiple initial messages/greetings.
 * Supports:
 * - Top scrollable row of tabs with greeting names
 * - "+ Add" creates a new greeting with a name prompt dialog (matching NewGroupDialog design)
 * - Long-clicking a tab opens a management dialog (Rename, Reorder, Delete)
 * - Drag-and-drop reorder bottom sheet
 * - Monospace Markdown editor with live preview
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssistantGreetingsSubPage(
    assistant: Assistant,
    onUpdate: (Assistant) -> Unit
) {
    val haptics = rememberPremiumHaptics()
    val toaster = LocalToaster.current

    val intros = remember(assistant.presetMessages, assistant.alternateGreetings) {
        val list = mutableListOf<String>()
        assistant.presetMessages.filter { it.role == MessageRole.ASSISTANT }.forEach { list.add(it.toText()) }
        list.addAll(assistant.alternateGreetings)
        list
    }

    val labels = remember(assistant.greetingLabels, intros.size) {
        val list = assistant.greetingLabels.toMutableList()
        while (list.size < intros.size) {
            list.add("")
        }
        list
    }

    fun updateIntros(newIntros: List<String>, newLabels: List<String>) {
        if (newIntros.isEmpty()) {
            onUpdate(
                assistant.copy(
                    presetMessages = emptyList(),
                    alternateGreetings = emptyList(),
                    greetingLabels = emptyList()
                )
            )
        } else {
            onUpdate(
                assistant.copy(
                    presetMessages = listOf(
                        UIMessage(
                            role = MessageRole.ASSISTANT,
                            parts = listOf(UIMessagePart.Text(newIntros.first()))
                        )
                    ),
                    alternateGreetings = newIntros.drop(1),
                    greetingLabels = newLabels
                )
            )
        }
    }

    var selectedIndex by remember(intros.size) {
        mutableIntStateOf(if (intros.isEmpty()) -1 else 0)
    }
    var editorTab by remember { mutableIntStateOf(0) } // 0: Write, 1: Preview

    // Dialog & popover states
    var showCreateDialog by remember { mutableStateOf(false) }
    var newGreetingDraftName by remember { mutableStateOf("") }

    var manageMenuIndex by remember { mutableStateOf<Int?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameDraftName by remember { mutableStateOf("") }
    var showReorderSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ═══════════════════════════════════════════════════════════════════
        // TOP TAB ROW (Initial #1, Initial #2, ..., + Add)
        // ═══════════════════════════════════════════════════════════════════
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            intros.forEachIndexed { index, _ ->
                val isSelected = selectedIndex == index
                val customName = labels.getOrNull(index)?.takeIf { it.isNotBlank() }
                val tabLabel = customName ?: "Initial #${index + 1}"

                Box(
                    modifier = Modifier
                        .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f))
                        .height(40.dp)
                        .clip(AppShapes.ButtonPill)
                        .combinedClickable(
                            onClick = {
                                haptics.perform(HapticPattern.Tick)
                                selectedIndex = index
                            },
                            onLongClick = {
                                haptics.perform(HapticPattern.Pop)
                                manageMenuIndex = index
                                renameDraftName = customName ?: tabLabel
                            },
                            role = Role.Tab
                        )
                        .background(
                            Color(0xFF000000),
                            shape = AppShapes.ButtonPill
                        )
                        .border(
                            if (isSelected) BorderStroke(
                                0.5.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.42f),
                                        Color.White.copy(alpha = 0.18f),
                                        Color.White.copy(alpha = 0.06f)
                                    )
                                )
                            )
                            else BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                            shape = AppShapes.ButtonPill
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFFFFFFFF) else Color.White.copy(alpha = 0.65f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // "+ Add" tab
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(AppShapes.ButtonPill)
                    .clickable {
                        haptics.perform(HapticPattern.Pop)
                        newGreetingDraftName = "Initial #${intros.size + 1}"
                        showCreateDialog = true
                    }
                    .background(Color(0xFF000000), shape = AppShapes.ButtonPill)
                    .border(
                        BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                        shape = AppShapes.ButtonPill
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // EMPTY STATE OR WORKSPACE
        // ═══════════════════════════════════════════════════════════════════
        if (intros.isEmpty()) {
            Surface(
                color = Color(0xFF000000),
                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                shape = AppShapes.CardLarge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No Initial Messages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Add one or more greetings for this character. When multiple greetings are configured, users can swipe or cycle through them.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    DomChatGlassPillButton(
                        text = "Create First Greeting",
                        onClick = {
                            haptics.perform(HapticPattern.Pop)
                            newGreetingDraftName = "Initial #1"
                            showCreateDialog = true
                        }
                    )
                }
            }
        } else if (selectedIndex in intros.indices) {
            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { if (targetState > initialState) it / 6 else -it / 6 }) togetherWith
                        (fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { if (targetState > initialState) -it / 6 else it / 6 })
                },
                label = "greeting_editor_transition"
            ) { targetIndex ->
                if (targetIndex in intros.indices) {
                    val currentIntro = intros[targetIndex]
                    DomChatMarkdownEditor(
                        value = currentIntro,
                        onValueChange = { newValue ->
                            val updatedIntros = intros.toMutableList()
                            updatedIntros[targetIndex] = newValue
                            updateIntros(updatedIntros, labels)
                        },
                        placeholder = "Write your character's opening greeting, action, and dialogue in Markdown...",
                        minLines = 14,
                        showMacros = true,
                        allowImageEmbed = true
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // BEHAVIOR SETTINGS
        // ═══════════════════════════════════════════════════════════════════
        if (intros.size > 1) {
            SettingsGroup(
                title = "Greeting Behavior",
                horizontalPadding = 0.dp,
                titleStartPadding = 0.dp
            ) {
                SettingGroupItem(
                    title = "Cycle Intros on New Chat",
                    subtitle = "Automatically select the next alternate greeting each time a new conversation is started.",
                    trailing = {
                        HapticSwitch(
                            checked = assistant.cycleIntrosOnNewChat,
                            onCheckedChange = {
                                onUpdate(assistant.copy(cycleIntrosOnNewChat = it))
                            }
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POPUP 1: CREATE NEW INITIAL MESSAGE DIALOG (matches NewGroupDialog style)
    // ═══════════════════════════════════════════════════════════════════════
    if (showCreateDialog) {
        Dialog(
            onDismissRequest = { showCreateDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                val dialogShape = RoundedCornerShape(20.dp)
                val inputShape = RoundedCornerShape(14.dp)
                Surface(
                    color = Color.Transparent,
                    shape = dialogShape,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .widthIn(min = 280.dp, max = 360.dp)
                        .imePadding()
                        .liquidGlassSurface(
                            shape = dialogShape,
                            fillColor = Color.Transparent,
                            borderWidth = 0.5.dp,
                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "New Initial Message",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Enter a name for this greeting (e.g. Default, Tavern, Battle).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        OutlinedTextField(
                            value = newGreetingDraftName,
                            onValueChange = { newGreetingDraftName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlassSurface(
                                    shape = inputShape,
                                    borderWidth = 0.5.dp,
                                    specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                    fillColor = Color.Transparent,
                                ),
                            singleLine = true,
                            placeholder = { Text("Greeting name", color = Color.White.copy(alpha = 0.5f)) },
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            DomChatGlassPillButton(
                                text = "Cancel",
                                onClick = { showCreateDialog = false },
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            DomChatGlassPillButton(
                                text = "Create",
                                enabled = newGreetingDraftName.isNotBlank(),
                                onClick = {
                                    val name = newGreetingDraftName.trim()
                                    val updatedIntros = intros + ""
                                    val updatedLabels = labels + name
                                    updateIntros(updatedIntros, updatedLabels)
                                    selectedIndex = updatedIntros.size - 1
                                    showCreateDialog = false
                                    toaster.show("Created $name")
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POPUP 2: MANAGEMENT POPUP (Centered dialog shown on long-press)
    // ═══════════════════════════════════════════════════════════════════════
    manageMenuIndex?.let { targetIndex ->
        if (targetIndex in intros.indices) {
            val currentLabel = labels.getOrNull(targetIndex)?.takeIf { it.isNotBlank() } ?: "Initial #${targetIndex + 1}"
            var renameDraft by remember(targetIndex) {
                mutableStateOf(labels.getOrNull(targetIndex).orEmpty())
            }
            Dialog(
                onDismissRequest = { manageMenuIndex = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val dialogShape = RoundedCornerShape(20.dp)
                    val inputShape = RoundedCornerShape(14.dp)
                    Surface(
                        color = Color.Transparent,
                        shape = dialogShape,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .widthIn(min = 280.dp, max = 360.dp)
                            .imePadding()
                            .liquidGlassSurface(
                                shape = dialogShape,
                                fillColor = Color.Transparent,
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .liquidGlassSurface(
                                                shape = CircleShape,
                                                fillColor = Color.Transparent,
                                                borderWidth = 0.5.dp,
                                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Manage Initial Message",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = currentLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                                IconButton(onClick = { manageMenuIndex = null }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Close",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Rename input with inline save
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = renameDraft,
                                    onValueChange = { renameDraft = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassSurface(
                                            shape = inputShape,
                                            borderWidth = 0.5.dp,
                                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                            fillColor = Color.Transparent,
                                        ),
                                    singleLine = true,
                                    placeholder = { Text("Initial message name", color = Color.White.copy(alpha = 0.5f)) },
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

                                AnimatedVisibility(
                                    visible = renameDraft.isNotBlank() && renameDraft != currentLabel,
                                    enter = fadeIn(tween(150)) + expandVertically(),
                                    exit = fadeOut(tween(150)) + shrinkVertically()
                                ) {
                                    DomChatGlassPillButton(
                                        text = "Save Rename",
                                        icon = Icons.Rounded.Check,
                                        accent = true,
                                        modifier = Modifier.fillMaxWidth().height(42.dp),
                                        onClick = {
                                            val updatedLabels = labels.toMutableList()
                                            while (updatedLabels.size <= targetIndex) {
                                                updatedLabels.add("")
                                            }
                                            updatedLabels[targetIndex] = renameDraft.trim()
                                            updateIntros(intros, updatedLabels)
                                            manageMenuIndex = null
                                            toaster.show("Renamed to ${renameDraft.trim()}")
                                        }
                                    )
                                }
                            }

                            // Actions: Clean pill button layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DomChatGlassPillButton(
                                    text = "Reorder",
                                    icon = Icons.Rounded.Reorder,
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    onClick = {
                                        manageMenuIndex = null
                                        showReorderSheet = true
                                    }
                                )
                                DomChatGlassPillButton(
                                    text = "Delete",
                                    icon = Icons.Rounded.Delete,
                                    destructive = true,
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    onClick = {
                                        val updatedIntros = intros.toMutableList().apply { removeAt(targetIndex) }
                                        val updatedLabels = labels.toMutableList().apply {
                                            if (targetIndex in indices) removeAt(targetIndex)
                                        }
                                        updateIntros(updatedIntros, updatedLabels)
                                        selectedIndex = (targetIndex - 1).coerceAtLeast(0)
                                        manageMenuIndex = null
                                        toaster.show("Deleted $currentLabel")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POPUP 3: REORDER GREETINGS SHEET
    // ═══════════════════════════════════════════════════════════════════════
    if (showReorderSheet) {
        val listState = rememberLazyListState()
        var currentEntries by remember(showReorderSheet, intros) {
            mutableStateOf(intros.indices.map { i ->
                GreetingItemData(
                    id = "greeting_uid_${i}_${intros[i].hashCode()}",
                    text = intros[i],
                    label = labels.getOrNull(i)?.takeIf { it.isNotBlank() } ?: "Initial #${i + 1}"
                )
            })
        }

        val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
            val list = currentEntries.toMutableList()
            val item = list.removeAt(from.index)
            list.add(to.index, item)
            currentEntries = list
            updateIntros(list.map { it.text }, list.map { it.label })
        }

        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = { showReorderSheet = false },
            containerColor = Color(0xFF000000),
            maxHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * 0.78f).dp,
            dragHandle = { DomChatSheetDragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                DomChatSheetHeader(
                    title = "Reorder Initial Messages",
                    subtitle = "Drag handle to rearrange greetings, swipe left to delete",
                    onCloseClick = { showReorderSheet = false },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (currentEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No greetings created",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentEntries, key = { it.id }) { item ->
                            ReorderableItem(
                                state = reorderableState,
                                key = item.id
                            ) { isDragging ->
                                PhysicsSwipeToDelete(
                                    onDelete = {
                                        val updated = currentEntries.filter { it.id != item.id }
                                        currentEntries = updated
                                        updateIntros(updated.map { it.text }, updated.map { it.label })
                                        selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                                        toaster.show("Deleted ${item.label}")
                                    },
                                    position = ItemPosition.ONLY,
                                    modifier = Modifier.fillMaxWidth()
                                ) { shape ->
                                    Surface(
                                        color = Color(0xFF000000),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                                Text(
                                                    text = item.label,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White
                                                )
                                                if (item.text.isNotBlank()) {
                                                    Text(
                                                        text = item.text.take(60).replace("\n", " "),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White.copy(alpha = 0.5f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = {},
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .draggableHandle(
                                                        onDragStarted = { haptics.perform(HapticPattern.Pop) },
                                                        onDragStopped = { haptics.perform(HapticPattern.Thud) }
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.DragIndicator,
                                                    contentDescription = "Drag to reorder",
                                                    tint = Color.White.copy(alpha = 0.65f),
                                                    modifier = Modifier.size(22.dp)
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
}

private data class GreetingItemData(
    val id: String,
    val text: String,
    val label: String
)
