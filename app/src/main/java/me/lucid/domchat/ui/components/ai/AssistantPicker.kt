package me.lucid.domchat.ui.components.ai

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.lucid.domchat.R
import me.lucid.domchat.Screen
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.hooks.rememberAssistantState
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.components.ai.DomChatAssistantPickerItem
import me.lucid.domchat.ui.components.ai.DomChatAssistantPickerTag
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import me.lucid.domchat.ui.components.chat.DomChatSearchBar
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import me.lucid.domchat.ui.components.ui.DomChatGlassChip
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.modifier.fadeEdges
import me.lucid.domchat.ui.theme.AppShapes
import androidx.compose.ui.text.font.FontWeight
import kotlin.uuid.Uuid

@Composable
fun AssistantPicker(
    settings: Settings,
    onUpdateSettings: (Settings) -> Unit,
    onNavigate: (Assistant) -> Unit = {},  // Called after panels close
    modifier: Modifier = Modifier,
    onClickSetting: () -> Unit,
) {
    val state = rememberAssistantState(settings, onUpdateSettings)
    val defaultAssistantName = stringResource(R.string.assistant_page_default_assistant)
    var showPicker by remember { mutableStateOf(false) }

    NavigationDrawerItem(
        icon = null,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.currentAssistant.name.ifEmpty { defaultAssistantName },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.weight(1f))

                UIAvatar(
                    name = state.currentAssistant.name.ifEmpty { defaultAssistantName },
                    value = state.currentAssistant.avatar,
                    onClick = onClickSetting
                )
            }
        },
        onClick = {
            showPicker = true
        },
        modifier = modifier,
        selected = false,
    )

    if (showPicker) {
        AssistantPickerSheet(
            settings = settings,
            currentAssistant = state.currentAssistant,
            onAssistantSelected = { assistant ->
                // Settings update happens immediately inside the sheet
                state.setSelectAssistant(assistant)
            },
            onNavigate = { assistant ->
                // Navigation callback - called after animation
                showPicker = false
                onNavigate(assistant)
            },
            onDismiss = {
                showPicker = false
            }
        )
    }
}

@Composable
fun AssistantPickerSheet(
    settings: Settings,
    currentAssistant: Assistant,
    onAssistantSelected: (Assistant) -> Unit,
    onNavigate: (Assistant) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val navController = LocalNavController.current
    val haptics = rememberPremiumHaptics()
    val defaultAssistantName = stringResource(R.string.assistant_page_default_assistant)
    val noSystemPromptLabel = stringResource(R.string.assistant_page_no_system_prompt)
    val assistantsById = remember(settings.assistants) {
        settings.assistants.filter { !it.isDeleted }.associateBy { it.id.toString() }
    }
    val pickerItems = remember(settings.assistants, defaultAssistantName) {
        settings.assistants.filter { !it.isDeleted }.map { assistant ->
            DomChatAssistantPickerItem(
                id = assistant.id.toString(),
                name = assistant.name.ifEmpty { defaultAssistantName },
                systemPrompt = assistant.systemPrompt,
                tagIds = assistant.tags.mapTo(linkedSetOf()) { it.toString() },
            )
        }
    }
    val pickerTags = remember(settings.assistantTags) {
        settings.assistantTags.map { tag ->
            DomChatAssistantPickerTag(id = tag.id.toString(), name = tag.name)
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTagIds by remember { mutableStateOf(emptySet<String>()) }
    val distinctTags = remember(pickerTags) { pickerTags.distinctBy { it.id } }
    val filteredAssistants = remember(pickerItems, selectedTagIds, searchQuery) {
        var list = if (selectedTagIds.isEmpty()) pickerItems
        else pickerItems.filter { it.tagIds.containsAll(selectedTagIds) }
        if (searchQuery.isNotBlank()) {
            list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
        list.distinctBy { it.id }
    }

    val itemCardBg = domChatCardContainerColor()
    val isDark = LocalDarkMode.current
    val tagBorderBrush = LiquidGlassTokens.SpecularBorderSubtle
    val tagIsFrosted = settings.displaySetting.liquidGlassMode == LiquidGlassMode.SOFT_FROSTED
    val tagGlassFill = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.25f)
    val cardGlassFill = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.18f)

    @Composable
    fun tagGlassSurface(): Modifier = if (tagIsFrosted) {
        Modifier.frostedGlassSurface(
            shape = RoundedCornerShape(20.dp),
            borderWidth = 0.5.dp,
            specularBrush = tagBorderBrush,
            fillColor = tagGlassFill,
        )
    } else {
        Modifier.liquidGlassSurface(
            shape = RoundedCornerShape(20.dp),
            borderWidth = 0.5.dp,
            specularBrush = tagBorderBrush,
            fillColor = tagGlassFill,
            glassMode = LiquidGlassMode.BALANCED_BLEND,
        )
    }

    @Composable
    fun cardGlassSurface(): Modifier = if (tagIsFrosted) {
        Modifier.frostedGlassSurface(
            shape = RoundedCornerShape(16.dp),
            borderWidth = 0.5.dp,
            specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
            fillColor = cardGlassFill,
        )
    } else {
        Modifier.liquidGlassSurface(
            shape = RoundedCornerShape(16.dp),
            borderWidth = 0.5.dp,
            specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
            fillColor = cardGlassFill,
            glassMode = LiquidGlassMode.BALANCED_BLEND,
        )
    }

    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        dragHandle = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp),
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
                title = stringResource(R.string.assistant_page_title),
                onCloseClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                DomChatSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search characters...",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .fadeEdges(fadeTop = true, fadeBottom = false, fadeHeight = 24f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Filter tags
            if (distinctTags.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    ) {
                        items(distinctTags, key = { it.id }) { tag ->
                            val isSelected = tag.id in selectedTagIds
                            Box(
                                modifier = Modifier
                                    .then(tagGlassSurface())
                                    .clickable {
                                        selectedTagIds = if (isSelected) {
                                            selectedTagIds - tag.id
                                        } else {
                                            selectedTagIds + tag.id
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = tag.name,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = if (isSelected) 1f else 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            // Character count badge above cards
            if (filteredAssistants.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        DomChatGlassChip(
                            text = "${filteredAssistants.size} ${if (filteredAssistants.size == 1) "Character" else "Characters"}"
                        )
                    }
                }

                itemsIndexed(filteredAssistants, key = { _, item -> item.id }) { _, assistant ->
                    val checked = assistant.id == currentAssistant.id.toString()
                    val cardShape = RoundedCornerShape(16.dp)
                    val cardBg = if (isDark) {
                        if (checked) Color(0xFF000000).copy(alpha = 0.70f) else Color(0xFF000000).copy(alpha = 0.45f)
                    } else {
                        if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else itemCardBg
                    }

                    Row(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .clip(cardShape)
                            .background(cardBg)
                            .then(cardGlassSurface())
                            .clickable {
                                if (!checked) {
                                    haptics.perform(HapticPattern.Pop)
                                    assistantsById[assistant.id]?.let { sel ->
                                        onAssistantSelected(sel)
                                        onNavigate(sel)
                                        onDismiss()
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val realAssistant = assistantsById[assistant.id]
                        if (realAssistant != null) {
                            UIAvatar(
                                name = assistant.name,
                                value = realAssistant.avatar,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = assistant.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isDark) Color.White else (if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = assistant.systemPrompt.ifBlank { noSystemPromptLabel },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else (if (checked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .liquidGlassSurface(
                                    shape = CircleShape,
                                    borderWidth = 0.5.dp,
                                    specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                    fillColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.20f),
                                    glassMode = LiquidGlassMode.BALANCED_BLEND,
                                )
                                .clip(CircleShape)
                                .clickable {
                                    onDismiss()
                                    navController.navigate(Screen.AssistantDetail(assistant.id))
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No characters found for \"$searchQuery\"" else "No characters available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantItem(
    assistant: Assistant,
    defaultAssistantName: String,
    onEdit: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = assistant.name.ifEmpty { defaultAssistantName },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = assistant.systemPrompt.ifBlank { stringResource(R.string.assistant_page_no_system_prompt) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        leadingContent = {
            UIAvatar(
                name = assistant.name.ifEmpty { defaultAssistantName },
                value = assistant.avatar,
                modifier = Modifier.size(32.dp)
            )
        },
        trailingContent = {
            IconButton(
                onClick = {
                    onEdit()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
