package me.lucid.domchat.ui.pages.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.theme.AppShapes
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.data.model.PresetInjectionPosition
import me.lucid.domchat.data.model.PromptPreset
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, kotlin.ExperimentalStdlibApi::class)
@Composable
fun SettingPresetsPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()
    val haptics = rememberPremiumHaptics()
    val toaster = LocalToaster.current
    val isDark = LocalDarkMode.current

    val allPresets = settings.displaySetting.promptPresets
    val enabledIds = settings.displaySetting.globalEnabledPromptPresetIds

    var showEditDialog by remember { mutableStateOf(false) }
    var editingPreset by remember { mutableStateOf<PromptPreset?>(null) }
    var draftName by remember { mutableStateOf("") }
    var draftDescription by remember { mutableStateOf("") }
    var draftContent by remember { mutableStateOf("") }
    var draftPosition by remember { mutableStateOf(PresetInjectionPosition.SYSTEM_PROMPT) }
    var draftDepth by remember { mutableIntStateOf(0) }

    fun openEdit(preset: PromptPreset) {
        editingPreset = preset
        draftName = preset.name
        draftDescription = preset.description
        draftContent = preset.content
        draftPosition = preset.injectionPosition
        draftDepth = preset.depth
        showEditDialog = true
    }

    fun openCreate() {
        editingPreset = null
        draftName = ""
        draftDescription = ""
        draftContent = ""
        draftPosition = PresetInjectionPosition.SYSTEM_PROMPT
        draftDepth = 0
        showEditDialog = true
    }

    fun saveDraft() {
        val name = draftName.trim().ifBlank { "Untitled Preset" }
        val target = editingPreset
        val updated = if (target == null) {
            val newP = PromptPreset(
                name = name,
                description = draftDescription.trim(),
                content = draftContent.trim(),
                injectionPosition = draftPosition,
                depth = draftDepth
            )
            allPresets + newP
        } else {
            allPresets.map { p ->
                if (p.id == target.id) {
                    p.copy(
                        name = name,
                        description = draftDescription.trim(),
                        content = draftContent.trim(),
                        injectionPosition = draftPosition,
                        depth = draftDepth
                    )
                } else p
            }
        }
        vm.updateSettings(
            settings.copy(
                displaySetting = settings.displaySetting.copy(promptPresets = updated)
            )
        )
        haptics.perform(HapticPattern.Success)
        toaster.show("Preset saved", type = ToastType.Success)
        showEditDialog = false
    }

    fun togglePreset(presetId: String, enable: Boolean) {
        haptics.perform(HapticPattern.Pop)
        val newSet = if (enable) enabledIds + presetId else enabledIds - presetId
        vm.updateSettings(
            settings.copy(
                displaySetting = settings.displaySetting.copy(globalEnabledPromptPresetIds = newSet)
            )
        )
    }

    fun duplicatePreset(preset: PromptPreset) {
        haptics.perform(HapticPattern.Pop)
        val copy = preset.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = "${preset.name} (Copy)"
        )
        vm.updateSettings(
            settings.copy(
                displaySetting = settings.displaySetting.copy(promptPresets = allPresets + copy)
            )
        )
        toaster.show("Preset duplicated")
    }

    fun deletePreset(presetId: String) {
        haptics.perform(HapticPattern.Pop)
        vm.updateSettings(
            settings.copy(
                displaySetting = settings.displaySetting.copy(
                    promptPresets = settings.displaySetting.promptPresets.filterNot { it.id == presetId },
                    globalEnabledPromptPresetIds = settings.displaySetting.globalEnabledPromptPresetIds - presetId
                )
            )
        )
        toaster.show("Preset deleted")
    }

    fun movePreset(index: Int, delta: Int) {
        val newIndex = index + delta
        if (newIndex in allPresets.indices) {
            val mutable = allPresets.toMutableList()
            val item = mutable.removeAt(index)
            mutable.add(newIndex, item)
            vm.updateSettings(
                settings.copy(
                    displaySetting = settings.displaySetting.copy(promptPresets = mutable)
                )
            )
            haptics.perform(HapticPattern.Tick)
        }
    }

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = "Prompt Presets",
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    openCreate()
                },
                containerColor = if (isDark) Color.White else Color.Black,
                contentColor = if (isDark) Color.Black else Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Preset")
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF141522).copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(0.5.dp, if (isDark) LiquidGlassTokens.SpecularBorderSubtle else LiquidGlassTokens.LightSpecularBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System & Formatting Locks",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Presets inject dynamic rules, anti-AI guidelines, and context locks into your conversations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            itemsIndexed(allPresets, key = { _, it -> it.id }) { index, preset ->
                val isEnabled = enabledIds.contains(preset.id)
                val tokenEstimate = (preset.content.length / 3.8).roundToInt()
                val charCount = preset.content.length
                val lineCount = preset.content.lines().size

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDark) Color(0xFF141520).copy(alpha = 0.55f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f),
                    border = BorderStroke(
                        if (isEnabled) 1.dp else 0.5.dp,
                        if (isEnabled) {
                            if (isDark) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.LightSpecularBorder
                        } else {
                            if (isDark) LiquidGlassTokens.SpecularBorderSubtle else LiquidGlassTokens.LightSpecularBorder
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header row with Name and Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (preset.isSystem) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Default",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                if (preset.description.isNotBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            HapticSwitch(
                                checked = isEnabled,
                                onCheckedChange = { togglePreset(preset.id, it) }
                            )
                        }

                        // Data stats row (Token estimation, char count, injection position)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Token estimate badge
                            Surface(
                                color = if (isDark) Color(0xFF202234).copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Text(
                                    text = "~$tokenEstimate tok",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            // Char count badge
                            Surface(
                                color = if (isDark) Color(0xFF202234).copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Text(
                                    text = "$charCount c · ${lineCount}L",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            // Injection position pill
                            val (posLabel, posColor) = when (preset.injectionPosition) {
                                PresetInjectionPosition.SYSTEM_PROMPT -> "System" to Color(0xFFA78BFA)
                                PresetInjectionPosition.AT_DEPTH -> "Depth @ ${preset.depth}" to Color(0xFF38BDF8)
                                PresetInjectionPosition.BEFORE_LAST_USER -> "Before User" to Color(0xFF34D399)
                            }
                            Surface(
                                color = posColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = posLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = posColor,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Content preview box
                        if (preset.content.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = if (isDark) Color(0xFF0C0D14).copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.06f))
                            ) {
                                Text(
                                    text = preset.content,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    ),
                                    color = if (isDark) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Actions Row (Move Up/Down, Edit, Duplicate, Delete)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reorder buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { movePreset(index, -1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.KeyboardArrowUp,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) (if (isDark) Color.White else Color.Black) else Color.Gray.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { movePreset(index, 1) },
                                    enabled = index < allPresets.lastIndex,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Move Down",
                                        tint = if (index < allPresets.lastIndex) (if (isDark) Color.White else Color.Black) else Color.Gray.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { duplicatePreset(preset) }) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Duplicate", style = MaterialTheme.typography.labelSmall)
                                }

                                TextButton(onClick = { openEdit(preset) }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit", style = MaterialTheme.typography.labelSmall)
                                }

                                if (!preset.isSystem) {
                                    TextButton(onClick = { deletePreset(preset.id) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Delete", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(60.dp))
            }
        }
    }

    if (showEditDialog) {
        ApplyDialogWindowBlur()
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = if (isDark) Color(0xFF14151C).copy(alpha = 0.88f) else MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.border(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f), AppShapes.CardLarge),
            title = { Text(if (editingPreset == null) "Create Prompt Preset" else "Edit Prompt Preset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.InputField
                    )
                    OutlinedTextField(
                        value = draftDescription,
                        onValueChange = { draftDescription = it },
                        label = { Text("Description (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.InputField
                    )
                    OutlinedTextField(
                        value = draftContent,
                        onValueChange = { draftContent = it },
                        label = { Text("Instructions / Content") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.InputField
                    )
                    val estTokens = (draftContent.length / 3.8).roundToInt()
                    Text(
                        text = "Estimated tokens: ~$estTokens · ${draftContent.length} chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { saveDraft() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color.White else Color.Black,
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    shape = AppShapes.CardMedium
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Unspecified)
                }
            }
        )
    }
}
