package me.lucid.domchat.ui.components.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.modifier.fadeEdges
import java.util.Locale
import me.lucid.domchat.ui.components.ui.DomChatGlassSwitch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Psychology
import kotlin.math.roundToInt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.data.model.PresetInjectionPosition
import me.lucid.domchat.data.model.PromptPreset
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.components.ui.DomChatDestructiveConfirmDialog
import me.lucid.domchat.ui.components.ui.DomChatGlassChip
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.components.ui.DomChatSlider
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import org.koin.compose.koinInject

/**
 * Single selectable position button for the 2x2 injection-position grid.
 */
@Composable
private fun PositionButton(
    label: String,
    position: PresetInjectionPosition,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val btnShape = RoundedCornerShape(12.dp)
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = btnShape,
        modifier = modifier.liquidGlassSurface(
            shape = btnShape,
            fillColor = Color.White.copy(alpha = 0.05f),

            borderWidth = if (selected) 1.dp else 0.5.dp,
            specularBrush = if (selected) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle,
        ),
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
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.5.sp
                ),
                color = if (selected) Color.White else Color.White.copy(alpha = 0.65f),
                maxLines = 1,
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptPresetsBottomSheet(
    settings: Settings,
    conversation: Conversation? = null,
    onUpdateConversation: ((Conversation) -> Unit)? = null,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settingsStore: SettingsStore = koinInject()
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val amoledMode by rememberAmoledDarkMode()
    val isDark = LocalDarkMode.current

    val borderBrush = LiquidGlassTokens.SpecularBorderSubtle
    val isFrosted = settings.displaySetting.liquidGlassMode == LiquidGlassMode.SOFT_FROSTED
    val presetCardFill = Color.White.copy(alpha = 0.05f)
    @Composable
    fun glassSurface(shape: Shape): Modifier = if (isFrosted) {
        Modifier.frostedGlassSurface(
            shape = shape,
            borderWidth = 0.5.dp,
            specularBrush = borderBrush,
            fillColor = presetCardFill,
        )
    } else {
        Modifier.liquidGlassSurface(
            shape = shape,
            borderWidth = 0.5.dp,
            specularBrush = borderBrush,
            fillColor = presetCardFill,
            glassMode = LiquidGlassMode.BALANCED_BLEND,
        )
    }

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
    var presetSearchQuery by remember { mutableStateOf("") }
    var debouncedPresetQuery by remember { mutableStateOf("") }
    var editingTargetId by remember { mutableStateOf<String?>(null) }

    // 250ms debouncer for preset search
    LaunchedEffect(Unit) {
        snapshotFlow { presetSearchQuery }
            .debounce(250)
            .distinctUntilChanged()
            .collect { debouncedPresetQuery = it.trim().lowercase() }
    }

    val filteredPresets = allPresets.filter { preset ->
        debouncedPresetQuery.isEmpty() ||
            preset.name.lowercase().contains(debouncedPresetQuery) ||
            preset.description.lowercase().contains(debouncedPresetQuery) ||
            preset.content.lowercase().contains(debouncedPresetQuery)
    }
    var presetToDelete by remember { mutableStateOf<PromptPreset?>(null) }
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

    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            DomChatSheetHeader(
                title = "Prompt Presets",
                modifier = Modifier.padding(horizontal = 20.dp),
                onCloseClick = {
                    if (isEditingOrCreating) {
                        isEditingOrCreating = false
                    } else {
                        onDismissRequest()
                    }
                },
                actions = {
                    if (!isEditingOrCreating) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .liquidGlassSurface(
                                    shape = CircleShape,
                                    borderWidth = 0.5.dp,
                                    specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                    isFloatingControl = true,
                                )
                                .clip(CircleShape)
                                .clickable {
                                    haptics.perform(HapticPattern.Pop)
                                    openCreate()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "New Preset",
                                tint = Color.White.copy(alpha = 0.92f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
            )

            if (!isEditingOrCreating) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
                    DomChatSearchBar(
                        value = presetSearchQuery,
                        onValueChange = { presetSearchQuery = it },
                        placeholder = "Search presets...",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            val canScrollUp by remember { androidx.compose.runtime.derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
            val canScrollDown by remember { androidx.compose.runtime.derivedStateOf { listState.canScrollForward } }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
                    .nestedScroll(overscrollAbsorber)
                    .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 40f),
                contentPadding = PaddingValues(top = 10.dp, bottom = 44.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {

        item {
            val allEnabled = allPresets.isNotEmpty() && allPresets.all { it.id in effectiveEnabledIds }
            val cardShape = RoundedCornerShape(18.dp)
            Surface(
                color = Color.Transparent,
                shape = cardShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassSurface(
                        shape = cardShape,
                        fillColor = Color.Transparent,
                        borderWidth = 0.5.dp,
                        specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(spring(dampingRatio = 0.82f, stiffness = 400f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Enable / Disable All with Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All Presets",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = "${effectiveEnabledIds.size} of ${allPresets.size} active",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                        DomChatGlassSwitch(
                            checked = allEnabled,
                            onCheckedChange = { toggleAll(it) }
                        )
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        thickness = 0.5.dp
                    )

                    // Row 2: Scope badge + Reset button
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(26.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Scope",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.55f)
                                )
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(24.dp)
                                        .liquidGlassSurface(
                                            shape = RoundedCornerShape(8.dp),
                                            fillColor = Color.Transparent,
                                            borderWidth = 0.5.dp,
                                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                        )
                                        .padding(horizontal = 8.dp)
                                ) {
                                    AnimatedContent(
                                        targetState = isChatSpecific,
                                        contentAlignment = Alignment.Center,
                                        transitionSpec = {
                                            fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                                        },
                                        label = "scope_badge_text"
                                    ) { chatSpecific ->
                                        Text(
                                            text = if (chatSpecific) "Using Chat" else "Global",
                                            fontSize = 11.sp,
                                            lineHeight = 13.sp,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Medium,
                                            color = if (chatSpecific) Color.White else Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = isChatSpecific,
                                enter = fadeIn(tween(200)) + expandHorizontally(spring(dampingRatio = 0.8f, stiffness = 400f)),
                                exit = fadeOut(tween(150)) + shrinkHorizontally(spring(dampingRatio = 0.8f, stiffness = 400f))
                            ) {
                                Surface(
                                    onClick = { resetToGlobal() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .height(24.dp)
                                        .liquidGlassSurface(
                                            shape = RoundedCornerShape(8.dp),
                                            fillColor = Color.Transparent,
                                            borderWidth = 0.5.dp,
                                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Reset",
                                            fontSize = 11.sp,
                                            lineHeight = 13.sp,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Inline Creator / Editor
            AnimatedContent(
                targetState = isEditingOrCreating,
                transitionSpec = {
                    (expandVertically(spring(dampingRatio = 0.75f, stiffness = 350f)) + fadeIn()) togetherWith
                        (shrinkVertically(spring(dampingRatio = 0.75f, stiffness = 350f)) + fadeOut())
                },
                contentAlignment = Alignment.TopCenter,
            ) { visible ->
                if (visible) {
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .then(glassSurface(RoundedCornerShape(20.dp)))
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
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.White.copy(alpha = 0.30f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White.copy(alpha = 0.9f),
                                focusedLabelColor = Color.White.copy(alpha = 0.8f),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.45f),
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = formDescription,
                            onValueChange = { formDescription = it },
                            label = { Text("Short Description / Tagline") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.White.copy(alpha = 0.30f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White.copy(alpha = 0.9f),
                                focusedLabelColor = Color.White.copy(alpha = 0.8f),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.45f),
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = formContent,
                            onValueChange = { formContent = it },
                            label = { Text("Instructions (supports {{char}}, {{user}})") },
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.White.copy(alpha = 0.30f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White.copy(alpha = 0.9f),
                                focusedLabelColor = Color.White.copy(alpha = 0.8f),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.45f),
                            )
                        )

                        val formEstTokens = (formContent.length / 3.8).roundToInt()
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp).then(glassSurface(RoundedCornerShape(12.dp)))
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
                                    text = "~$formEstTokens · ${formContent.length} chars",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }

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
                                    selected = formPosition == PresetInjectionPosition.SYSTEM_PROMPT,
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        formPosition = PresetInjectionPosition.SYSTEM_PROMPT
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                PositionButton(
                                    label = "Post-History",
                                    position = PresetInjectionPosition.POST_HISTORY_INSTRUCTIONS,
                                    selected = formPosition == PresetInjectionPosition.POST_HISTORY_INSTRUCTIONS,
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        formPosition = PresetInjectionPosition.POST_HISTORY_INSTRUCTIONS
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PositionButton(
                                    label = "Before User",
                                    position = PresetInjectionPosition.BEFORE_LAST_USER,
                                    selected = formPosition == PresetInjectionPosition.BEFORE_LAST_USER,
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        formPosition = PresetInjectionPosition.BEFORE_LAST_USER
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                PositionButton(
                                    label = "At Depth",
                                    position = PresetInjectionPosition.AT_DEPTH,
                                    selected = formPosition == PresetInjectionPosition.AT_DEPTH,
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        formPosition = PresetInjectionPosition.AT_DEPTH
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = formPosition == PresetInjectionPosition.AT_DEPTH,
                            enter = expandVertically(animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessLow
                            )) + fadeIn(animationSpec = tween(400)),
                            exit = shrinkVertically(animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessLow
                            )) + fadeOut(animationSpec = tween(300))
                        ) {
                            Column {
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
                                DomChatSlider(
                                    value = formDepth.toFloat(),
                                    onValueChange = { formDepth = it.toInt() },
                                    valueRange = 0f..10f,
                                    steps = 9,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            DomChatGlassPillButton(
                                text = "Cancel",
                                onClick = { isEditingOrCreating = false }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            DomChatGlassPillButton(
                                text = "Save Preset",
                                accent = true,
                                onClick = {
                                    savePreset()
                                }
                            )
                        }
                    }
                }
            } // end if (visible)
        }
    } // end editor item

    itemsIndexed(filteredPresets, key = { _, it: PromptPreset -> it.id }) { index: Int, preset: PromptPreset ->
        val isEnabled = effectiveEnabledIds.contains(preset.id)
        val snippet = preset.description.ifBlank { preset.content.take(140) }
        val estTokens = (preset.content.length / 3.8).roundToInt()
        val positionLabel = when (preset.injectionPosition) {
            PresetInjectionPosition.SYSTEM_PROMPT -> "System"
            PresetInjectionPosition.POST_HISTORY_INSTRUCTIONS -> "Post-History"
            PresetInjectionPosition.BEFORE_LAST_USER -> "Before User"
            PresetInjectionPosition.AT_DEPTH -> "At Depth ${preset.depth}"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp)
                // Mode-aware glass card: frostedGlassSurface in SOFT_FROSTED mode,
                // stable liquidGlassSurface otherwise — no per-card AGSL lens pass
                // while the LazyColumn scrolls.
                .then(glassSurface(RoundedCornerShape(16.dp))),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = positionLabel,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                        )
                        Text(
                            text = "~$estTokens tokens",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.65f),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = snippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                DomChatGlassSwitch(
                    checked = isEnabled,
                    onCheckedChange = { enabled ->
                        togglePreset(preset.id, enabled)
                        toaster.show(
                            message = if (enabled) "Enabled: ${preset.name}" else "Disabled: ${preset.name}",
                            type = ToastType.Info,
                        )
                    },
                )
            }
        }
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

    // Preset delete confirmation dialog (glassy black, destructive)
    DomChatDestructiveConfirmDialog(
        visible = presetToDelete != null,
        title = "Delete ${presetToDelete?.name}?",
        consequence = "This preset will be permanently removed from your presets list.",
        confirmLabel = "Delete",
        cancelLabel = "Cancel",
        onConfirm = { presetToDelete?.let { deletePreset(it.id) }; presetToDelete = null },
        onDismiss = { presetToDelete = null }
    )
}
}
