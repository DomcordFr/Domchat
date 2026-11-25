package me.lucid.domchat.ui.pages.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import me.lucid.domchat.ui.theme.appFieldColors
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.DialogProperties
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
import me.lucid.domchat.ui.components.ui.DomChatSlider
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.widthIn
import me.lucid.domchat.ui.modifier.domChatDialogContainerColor

/**
 * Single selectable position button for the 2x2 injection-position grid.
 */
@Composable
private fun PositionButton(
    label: String,
    position: PresetInjectionPosition,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF000000),
        border = if (selected) {
            BorderStroke(1.dp, LiquidGlassTokens.SpecularBorderBrush)
        } else {
            BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
        },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) Color.White else Color.White.copy(alpha = 0.65f),
                maxLines = 1,
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

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
    var presetToDelete by remember { mutableStateOf<PromptPreset?>(null) }
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
                containerColor = Color(0xFF000000),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Preset", tint = Color.White)
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
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF000000),
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
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System & Formatting Locks",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.White
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Presets inject dynamic rules, anti-AI guidelines, and context locks into your conversations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.65f)
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
                        .padding(horizontal = 16.dp)
                        .animateItem(),
                    shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
                    color = Color(0xFF000000),
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
                                        color = if (isDark) Color.White else Color.White
                                    )
                                    if (preset.isSystem) {
                                        Surface(
                                            color = Color(0xFF000000),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                        ) {
                                            Text(
                                                text = "Default",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = Color.White,
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
                                        color = if (isDark) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.65f)
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
                                color = Color(0xFF000000),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                            ) {
                                Text(
                                    text = "~$tokenEstimate tok",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            // Char count badge
                            Surface(
                                color = Color(0xFF000000),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                            ) {
                                Text(
                                    text = "$charCount c · ${lineCount}L",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            // Injection position pill
                            val (posLabel, posColor) = when (preset.injectionPosition) {
                                PresetInjectionPosition.SYSTEM_PROMPT -> "System" to Color.White
                                PresetInjectionPosition.AT_DEPTH -> "Depth @ ${preset.depth}" to Color(0xFF38BDF8)
                                PresetInjectionPosition.BEFORE_LAST_USER -> "Before User" to Color(0xFF34D399)
                                PresetInjectionPosition.POST_HISTORY_INSTRUCTIONS -> "Post-History" to Color(0xFFD946EF)
                            }
                            Surface(
                                color = Color(0xFF000000),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                            ) {
                                Text(
                                    text = posLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Content preview box
                        if (preset.content.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF000000),
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
                                    color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.65f),
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
                                    TextButton(onClick = { presetToDelete = preset }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF453A))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Delete", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF453A))
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
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .widthIn(max = 360.dp)
                .border(0.5.dp, LiquidGlassTokens.SpecularBorderBrush, AppShapes.SettingDialog)
                .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SettingDialog),
            shape = AppShapes.SettingDialog,
            containerColor = Color(0xFF000000),
            titleContentColor = Color(0xFFF2F2F7),
            textContentColor = Color(0xFFF2F2F7),
            title = { Text(if (editingPreset == null) "Create Prompt Preset" else "Edit Prompt Preset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.InputField,
                        colors = appFieldColors(),
                    )
                    OutlinedTextField(
                        value = draftDescription,
                        onValueChange = { draftDescription = it },
                        label = { Text("Description (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.InputField,
                        colors = appFieldColors(),
                    )
                    OutlinedTextField(
                        value = draftContent,
                        onValueChange = { draftContent = it },
                        label = { Text("Instructions / Content") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.InputField,
                        colors = appFieldColors(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Injection Position",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PositionButton(
                                label = "System",
                                position = PresetInjectionPosition.SYSTEM_PROMPT,
                                selected = draftPosition == PresetInjectionPosition.SYSTEM_PROMPT,
                                modifier = Modifier.weight(1f),
                                onClick = { draftPosition = PresetInjectionPosition.SYSTEM_PROMPT }
                            )
                            PositionButton(
                                label = "Post-History",
                                position = PresetInjectionPosition.POST_HISTORY_INSTRUCTIONS,
                                selected = draftPosition == PresetInjectionPosition.POST_HISTORY_INSTRUCTIONS,
                                modifier = Modifier.weight(1f),
                                onClick = { draftPosition = PresetInjectionPosition.POST_HISTORY_INSTRUCTIONS }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PositionButton(
                                label = "Before User",
                                position = PresetInjectionPosition.BEFORE_LAST_USER,
                                selected = draftPosition == PresetInjectionPosition.BEFORE_LAST_USER,
                                modifier = Modifier.weight(1f),
                                onClick = { draftPosition = PresetInjectionPosition.BEFORE_LAST_USER }
                            )
                            PositionButton(
                                label = "At Depth",
                                position = PresetInjectionPosition.AT_DEPTH,
                                selected = draftPosition == PresetInjectionPosition.AT_DEPTH,
                                modifier = Modifier.weight(1f),
                                onClick = { draftPosition = PresetInjectionPosition.AT_DEPTH }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = draftPosition == PresetInjectionPosition.AT_DEPTH,
                        enter = expandVertically(animationSpec = spring(
                            dampingRatio = 0.5f,
                            stiffness = Spring.StiffnessLow
                        )) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkVertically(animationSpec = spring(
                            dampingRatio = 0.5f,
                            stiffness = Spring.StiffnessLow
                        )) + fadeOut(animationSpec = tween(300))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    text = "$draftDepth",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            DomChatSlider(
                                value = draftDepth.toFloat(),
                                onValueChange = { draftDepth = it.toInt() },
                                valueRange = 0f..10f,
                                steps = 9,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    val estTokens = (draftContent.length / 3.8).roundToInt()
                    Surface(
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = "Tokens",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Estimated tokens",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = "~$estTokens · ${draftContent.length} chars",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { saveDraft() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEditDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Preset delete confirmation dialog (glassy black, destructive)
    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .widthIn(max = 320.dp)
                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, AppShapes.Dialog),
            shape = AppShapes.Dialog,
            containerColor = domChatDialogContainerColor(),
            title = {
                ApplyDialogWindowBlur()
                Text(
                    text = "Delete ${preset.name}?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "This preset will be permanently removed from your presets list.",
                    fontSize = 13.5.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        deletePreset(preset.id)
                        presetToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF453A),
                        contentColor = Color.White
                    ),
                    shape = AppShapes.CardMedium
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}
