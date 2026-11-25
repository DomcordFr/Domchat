package me.lucid.domchat.ui.components.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.derivedStateOf
import me.lucid.domchat.ui.modifier.fadeEdges
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import me.lucid.domchat.R
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.ai.mcp.McpManager
import me.lucid.domchat.data.ai.mcp.McpStatus
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.data.model.SKILL_SELECTION_OVERRIDE_ID
import me.lucid.domchat.data.model.hasManualSkillSelectionOverride
import me.lucid.domchat.data.model.withoutSkillSelectionOverride
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import me.lucid.domchat.ui.components.chat.DomChatSearchBar
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import me.lucid.domchat.ui.components.ui.icons.ModeIcons
import me.lucid.domchat.ui.components.ui.DomChatGlassSwitch
import me.lucid.domchat.ui.components.ui.ItemPosition
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject

/**
 * Liquid glass (BALANCED_BLEND) or frosted glass (SOFT_FROSTED) surface for lorebook picker
 * rows and the sheet itself — no solid black backdrop. Reuses the shared
 * [frostedGlassSurface] / [liquidGlassSurface] primitives so the sheet matches the
 * DomChat unified glass card theme (0xFF141520).
 */
@Composable
fun Modifier.lorebookGlassSurface(shape: Shape, isFrosted: Boolean): Modifier = if (isFrosted) {
    frostedGlassSurface(
        shape = shape,
        borderWidth = 0.5.dp,
        specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
        fillColor = Color.Transparent,
    )
} else {
    liquidGlassSurface(
        shape = shape,
        borderWidth = 0.5.dp,
        specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
        fillColor = Color.Transparent,
        glassMode = LiquidGlassMode.BALANCED_BLEND,
    )
}

@Composable
internal fun SkillsPickerSheet(
    settings: Settings,
    assistant: Assistant,
    conversation: Conversation,
    onUpdateConversation: (Conversation) -> Unit,
    onDismiss: () -> Unit
) {
    val amoledMode by rememberAmoledDarkMode()
    val isDarkMode = LocalDarkMode.current
    val cornerRadius = 28.dp
    val smallCorner = 8.dp

    val availableSkills = remember(settings.skills) {
        settings.skills
    }
    val userInvocableSkills = remember(availableSkills) { availableSkills.filter { it.userInvocable } }
    val availableSkillIds = remember(availableSkills) { availableSkills.map { it.id }.toSet() }
    val assistantAvailableSkillIds = remember(settings.skills, assistant.id) {
        settings.skills.filter { it.isAvailableForAssistant(assistant.id) }.map { it.id }.toSet()
    }
    val alwaysEnabledSkillIds = remember(availableSkills, assistantAvailableSkillIds) {
        availableSkills.filter { it.alwaysEnabled && assistantAvailableSkillIds.contains(it.id) }.map { it.id }.toSet()
    }
    val effectiveEnabledIds = remember(
        conversation.enabledModeIds,
        assistant.enabledSkillIds,
        availableSkillIds,
        assistantAvailableSkillIds,
        alwaysEnabledSkillIds
    ) {
        val hasOverride = conversation.enabledModeIds.hasManualSkillSelectionOverride()
        val base = if (hasOverride || conversation.enabledModeIds.isNotEmpty()) {
            conversation.enabledModeIds.withoutSkillSelectionOverride()
        } else {
            assistant.enabledSkillIds.intersect(assistantAvailableSkillIds)
        }
        (base + if (hasOverride) emptySet() else alwaysEnabledSkillIds).intersect(availableSkillIds)
    }

    var localEnabledIds by remember(conversation.id, conversation.enabledModeIds, assistant.enabledSkillIds) {
        mutableStateOf(effectiveEnabledIds)
    }

    val scope = rememberCoroutineScope()

    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        maxHeight = (LocalConfiguration.current.screenHeightDp * 0.75f).dp,
    ) {
        val scrollState = rememberScrollState()
        val canScrollUp by remember { derivedStateOf { scrollState.value > 0 } }
        val canScrollDown by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 40f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DomChatSheetHeader(
                title = stringResource(R.string.skills_picker_title),
                onCloseClick = onDismiss,
            )

            if (userInvocableSkills.isEmpty()) {
                Text(
                    text = stringResource(R.string.skills_picker_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                userInvocableSkills.forEachIndexed { index, skill ->
                    val isEnabled = localEnabledIds.contains(skill.id)

                    val position = when {
                        userInvocableSkills.size == 1 -> ItemPosition.ONLY
                        index == 0 -> ItemPosition.FIRST
                        index == userInvocableSkills.lastIndex -> ItemPosition.LAST
                        else -> ItemPosition.MIDDLE
                    }

                    val shape = when (position) {
                        ItemPosition.ONLY -> RoundedCornerShape(cornerRadius)
                        ItemPosition.FIRST -> RoundedCornerShape(
                            topStart = cornerRadius,
                            topEnd = cornerRadius,
                            bottomStart = smallCorner,
                            bottomEnd = smallCorner
                        )

                        ItemPosition.MIDDLE -> RoundedCornerShape(smallCorner)
                        ItemPosition.LAST -> RoundedCornerShape(
                            topStart = smallCorner,
                            topEnd = smallCorner,
                            bottomStart = cornerRadius,
                            bottomEnd = cornerRadius
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassSurface(
                                shape = shape,
                                fillColor = Color.White.copy(alpha = 0.05f),

                                borderWidth = 0.5.dp,
                                specularBrush = if (isEnabled) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle,
                            )
                            .clip(shape)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF000000))
                                    .border(
                                        0.5.dp,
                                        if (isEnabled) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ModeIcons.getIcon(skill.icon ?: "category"),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else Color.White
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = skill.name.ifEmpty { stringResource(R.string.skills_page_unnamed) },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when {
                                        skill.description.isNotBlank() -> skill.description
                                        skill.instructions.isNotBlank() -> skill.instructions.take(50) + if (skill.instructions.length > 50) "..." else ""
                                        else -> stringResource(R.string.skills_page_empty_desc)
                                    },
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DomChatGlassSwitch(
                                checked = isEnabled,
                                onCheckedChange = { newEnabled ->
                                    val newEnabledIds = if (newEnabled) {
                                        localEnabledIds + skill.id
                                    } else {
                                        localEnabledIds - skill.id
                                    }
                                    localEnabledIds = newEnabledIds
                                    onUpdateConversation(
                                        conversation.copy(enabledModeIds = newEnabledIds + SKILL_SELECTION_OVERRIDE_ID)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun LorebooksPickerSheet(
    settings: Settings,
    assistant: Assistant,
    conversation: Conversation,
    onUpdateConversation: (Conversation) -> Unit,
    onNavigateToLorebook: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val amoledMode by rememberAmoledDarkMode()
    val isDarkMode = LocalDarkMode.current

    val lorebookIds = remember(settings.lorebooks) { settings.lorebooks.map { it.id }.toSet() }
    val effectiveEnabledIds = (conversation.enabledLorebookIds ?: assistant.enabledLorebookIds).intersect(lorebookIds)
    var localEnabledIds by remember(conversation.id, conversation.enabledLorebookIds, assistant.enabledLorebookIds, lorebookIds) {
        mutableStateOf(effectiveEnabledIds)
    }

    var lorebookSearchQuery by remember { mutableStateOf("") }
    val filteredLorebooks = remember(settings.lorebooks, lorebookSearchQuery) {
        settings.lorebooks.filter {
            lorebookSearchQuery.isEmpty() ||
                it.name.contains(lorebookSearchQuery, ignoreCase = true) ||
                it.id.toString().contains(lorebookSearchQuery, ignoreCase = true)
        }
    }

    val isFrosted = settings.displaySetting.liquidGlassMode == LiquidGlassMode.SOFT_FROSTED

    // Unified in-window sheet: glass container + scrim + drag handle, capped at 60% screen height.
    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            DomChatSheetHeader(
                title = stringResource(R.string.lorebooks_picker_title),
                onCloseClick = onDismiss,
            )

            DomChatSearchBar(
                value = lorebookSearchQuery,
                onValueChange = { lorebookSearchQuery = it },
                placeholder = "Search lorebooks...",
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredLorebooks.isEmpty()) {
                Text(
                    text = stringResource(R.string.lorebooks_picker_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filteredLorebooks, key = { it.id }) { lorebook ->
                        val isEnabled = localEnabledIds.contains(lorebook.id)

                        CompositionLocalProvider(LocalAbsoluteTonalElevation provides if (amoledMode && isDarkMode) 0.dp else LocalAbsoluteTonalElevation.current) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .lorebookGlassSurface(RoundedCornerShape(16.dp), isFrosted)
                                    .clickable { onNavigateToLorebook(lorebook.id.toString()) },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(width = 40.dp, height = 56.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            when (val cover = lorebook.cover) {
                                                is Avatar.Image -> {
                                                    AsyncImage(
                                                        model = cover.url,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }

                                                is Avatar.Emoji -> {
                                                    Text(
                                                        text = cover.content,
                                                        fontSize = 20.sp
                                                    )
                                                }

                                                else -> {
                                                    Text(
                                                        text = lorebook.name.take(1).uppercase().ifEmpty { "L" },
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = lorebook.name.ifEmpty { stringResource(R.string.lorebooks_page_unnamed) },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = stringResource(R.string.lorebooks_page_entries_count, lorebook.entries.size),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    DomChatGlassSwitch(
                                        checked = isEnabled,
                                        onCheckedChange = { newEnabled ->
                                            val newIds = if (newEnabled) {
                                                localEnabledIds + lorebook.id
                                            } else {
                                                localEnabledIds - lorebook.id
                                            }
                                            localEnabledIds = newIds
                                            onUpdateConversation(conversation.copy(enabledLorebookIds = newIds))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
internal fun PluginsPickerSheet(
    settings: Settings,
    assistant: Assistant,
    onUpdateAssistant: (Assistant) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val mcpManager = koinInject<McpManager>()
    val syncingStatus by mcpManager.syncingStatus.collectAsStateWithLifecycle()
    val loading = syncingStatus.values.any { it == McpStatus.Connecting }

    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        maxHeight = (LocalConfiguration.current.screenHeightDp * 0.75f).dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DomChatSheetHeader(
                title = stringResource(R.string.mcp_picker_title),
                onCloseClick = onDismiss,
            )
            if (loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    LinearWavyProgressIndicator()
                    Text(
                        text = stringResource(R.string.mcp_picker_syncing),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            McpPicker(
                assistant = assistant,
                servers = settings.mcpServers,
                onUpdateAssistant = onUpdateAssistant,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}
