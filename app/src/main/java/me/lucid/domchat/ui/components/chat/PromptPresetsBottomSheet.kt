package me.lucid.domchat.ui.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import me.lucid.domchat.ui.theme.LocalDarkMode
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.data.model.PresetInjectionPosition
import me.lucid.domchat.data.model.PromptPreset
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptPresetsBottomSheet(
    settings: Settings,
    conversation: Conversation? = null,
    onUpdateConversation: ((Conversation) -> Unit)? = null,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsStore: SettingsStore = koinInject()
    val scope = rememberCoroutineScope()
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val amoledMode by rememberAmoledDarkMode()
    val isDark = LocalDarkMode.current

    val overscrollAbsorber = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return available
            }
        }
    }

    val allPresets = settings.displaySetting.promptPresets

    // If conversation is provided and has specific overrides, use those; otherwise use global settings
    val effectiveEnabledIds = remember(conversation?.enabledPromptPresetIds, settings.displaySetting.globalEnabledPromptPresetIds) {
        conversation?.enabledPromptPresetIds ?: settings.displaySetting.globalEnabledPromptPresetIds
    }

    val isChatSpecific = conversation?.enabledPromptPresetIds != null

    var isEditingOrCreating by remember { mutableStateOf(false) }
    var editingTargetId by remember { mutableStateOf<String?>(null) }
    var formName by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    var formContent by remember { mutableStateOf("") }
    var formPosition by remember { mutableStateOf(PresetInjectionPosition.SYSTEM_PROMPT) }
    var formDepth by remember { mutableStateOf(0) }

    fun openCreate() {
        editingTargetId = null
        formName = ""
        formDescription = ""
        formContent = ""
        formPosition = PresetInjectionPosition.SYSTEM_PROMPT
        formDepth = 0
        isEditingOrCreating = true
    }

    fun openEdit(preset: PromptPreset) {
        editingTargetId = preset.id
        formName = preset.name
        formDescription = preset.description
        formContent = preset.content
        formPosition = preset.injectionPosition
        formDepth = preset.depth
        isEditingOrCreating = true
    }

    fun savePreset() {
        val targetName = formName.trim().ifBlank { "Custom Preset" }
        scope.launch {
            settingsStore.update { cur ->
                val display = cur.displaySetting
                val updatedList = if (editingTargetId == null) {
                    val newP = PromptPreset(
                        name = targetName,
                        content = formContent.trim(),
                        description = formDescription.trim(),
                        injectionPosition = formPosition,
                        depth = formDepth
                    )
                    display.promptPresets + newP
                } else {
                    display.promptPresets.map { p ->
                        if (p.id == editingTargetId) {
                            p.copy(
                                name = targetName,
                                content = formContent.trim(),
                                description = formDescription.trim(),
                                injectionPosition = formPosition,
                                depth = formDepth
                            )
                        } else p
                    }
                }
                cur.copy(displaySetting = display.copy(promptPresets = updatedList))
            }
            isEditingOrCreating = false
        }
    }

    fun deletePreset(id: String) {
        scope.launch {
            settingsStore.update { cur ->
                val display = cur.displaySetting
                val updatedList = display.promptPresets.filterNot { it.id == id }
                val updatedGlobal = display.globalEnabledPromptPresetIds - id
                cur.copy(
                    displaySetting = display.copy(
                        promptPresets = updatedList,
                        globalEnabledPromptPresetIds = updatedGlobal
                    )
                )
            }
            if (conversation != null && onUpdateConversation != null) {
                val curSet = conversation.enabledPromptPresetIds ?: settings.displaySetting.globalEnabledPromptPresetIds
                onUpdateConversation(conversation.copy(enabledPromptPresetIds = curSet - id))
            }
        }
    }

    fun togglePreset(id: String, enabled: Boolean) {
        haptics.perform(HapticPattern.Tick)
        val newSet = if (enabled) effectiveEnabledIds + id else effectiveEnabledIds - id

        if (conversation != null && onUpdateConversation != null) {
            // Per-chat mode
            onUpdateConversation(conversation.copy(enabledPromptPresetIds = newSet))
        } else {
            // Global mode
            scope.launch {
                settingsStore.update { cur ->
                    cur.copy(
                        displaySetting = cur.displaySetting.copy(globalEnabledPromptPresetIds = newSet)
                    )
                }
            }
        }
    }

    fun toggleAll(enableAll: Boolean) {
        haptics.perform(HapticPattern.Pop)
        val targetSet = if (enableAll) allPresets.map { it.id }.toSet() else emptySet()

        if (conversation != null && onUpdateConversation != null) {
            onUpdateConversation(conversation.copy(enabledPromptPresetIds = targetSet))
        } else {
            scope.launch {
                settingsStore.update { cur ->
                    cur.copy(
                        displaySetting = cur.displaySetting.copy(globalEnabledPromptPresetIds = targetSet)
                    )
                }
            }
        }
    }

    fun resetToGlobal() {
        haptics.perform(HapticPattern.Pop)
        if (conversation != null && onUpdateConversation != null) {
            onUpdateConversation(conversation.copy(enabledPromptPresetIds = null))
        }
    }

    fun movePreset(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in allPresets.indices || toIndex !in allPresets.indices) return
        val mutable = allPresets.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        scope.launch {
            settingsStore.update { cur ->
                cur.copy(displaySetting = cur.displaySetting.copy(promptPresets = mutable))
            }
        }
    }

    ApplyDialogWindowBlur()
    val sheetBase = if (isDark) Color(0xFF14151C) else Color(0xFFF6F6F9)
    val effectiveSheetContainerColor = androidx.compose.ui.graphics.lerp(
        sheetBase,
        domChatSheetContainerColor(),
        settings.displaySetting.glassAlpha.coerceIn(0.2f, 1f)
    ).copy(alpha = settings.displaySetting.glassAlpha.coerceIn(0.88f, 0.98f))

    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        containerColor = effectiveSheetContainerColor,
        shape = sheetShape,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = Color.White.copy(alpha = 0.25f),
                shape = CircleShape
            ) {
                Box(modifier = Modifier.size(width = 36.dp, height = 4.dp))
            }
        },
        modifier = modifier
            .border(0.5.dp, LiquidGlassTokens.SpecularBorderBrush, sheetShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Prompt Presets",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (conversation != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (isChatSpecific) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = if (isChatSpecific) "Chat Specific" else "Using Global",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isChatSpecific) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Toggle & inject multiple roleplay instruction cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isEditingOrCreating) {
                    IconButton(
                        onClick = {
                            haptics.perform(HapticPattern.Pop)
                            openCreate()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "New Preset",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { isEditingOrCreating = false },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Enable All / Disable All / Reset to Global
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { toggleAll(true) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Enable All", fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = { toggleAll(false) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Turn Off All", fontSize = 12.sp)
                    }
                }

                if (isChatSpecific) {
                    TextButton(
                        onClick = { resetToGlobal() },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset to Global", fontSize = 12.sp)
                    }
                }
            }

            // Inline Creator / Editor
            AnimatedVisibility(
                visible = isEditingOrCreating,
                enter = expandVertically(spring(dampingRatio = 0.75f, stiffness = 350f)) + fadeIn(),
                exit = shrinkVertically(spring(dampingRatio = 0.75f, stiffness = 350f)) + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (editingTargetId == null) "Create Prompt Preset" else "Edit Preset",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = formName,
                            onValueChange = { formName = it },
                            label = { Text("Preset Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = formDescription,
                            onValueChange = { formDescription = it },
                            label = { Text("Short Description / Tagline") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = formContent,
                            onValueChange = { formContent = it },
                            label = { Text("Instructions (supports {{char}}, {{user}})") },
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Injection Position",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetInjectionPosition.entries.forEach { pos ->
                                val isSelected = formPosition == pos
                                Surface(
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        formPosition = pos
                                    },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        0.5.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (pos) {
                                                PresetInjectionPosition.SYSTEM_PROMPT -> "System"
                                                PresetInjectionPosition.AT_DEPTH -> "Depth D"
                                                PresetInjectionPosition.BEFORE_LAST_USER -> "Before User"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (formPosition == PresetInjectionPosition.AT_DEPTH) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Depth (Messages from end)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$formDepth",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = formDepth.toFloat(),
                                onValueChange = { formDepth = it.toInt() },
                                valueRange = 0f..10f,
                                steps = 9,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isEditingOrCreating = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    haptics.perform(HapticPattern.Pop)
                                    savePreset()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save Preset")
                            }
                        }
                    }
                }
            }

            // Presets List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .nestedScroll(overscrollAbsorber)
            ) {
                itemsIndexed(allPresets, key = { _, it -> it.id }) { index, preset ->
                    val isEnabled = effectiveEnabledIds.contains(preset.id)

                    Surface(
                        color = if (isEnabled) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = if (isEnabled) 1.5.dp else 0.5.dp,
                            color = if (isEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (allPresets.size > 1) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            haptics.perform(HapticPattern.Pop)
                                            movePreset(index, index - 1)
                                        },
                                        enabled = index > 0,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.KeyboardArrowUp,
                                            contentDescription = "Move Up",
                                            tint = if (index > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            haptics.perform(HapticPattern.Pop)
                                            movePreset(index, index + 1)
                                        },
                                        enabled = index < allPresets.size - 1,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = "Move Down",
                                            tint = if (index < allPresets.size - 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
                                    ) {
                                        Text(
                                            text = when (preset.injectionPosition) {
                                                PresetInjectionPosition.SYSTEM_PROMPT -> "System"
                                                PresetInjectionPosition.AT_DEPTH -> "Depth ${preset.depth}"
                                                PresetInjectionPosition.BEFORE_LAST_USER -> "Before User"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (preset.description.isNotBlank()) {
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = preset.content,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color.White.copy(alpha = 0.5f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        openEdit(preset)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                if (!preset.isSystem && allPresets.size > 1) {
                                    IconButton(
                                        onClick = {
                                            haptics.perform(HapticPattern.Pop)
                                            deletePreset(preset.id)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = { togglePreset(preset.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
