package me.lucid.domchat.ui.pages.chat

import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import me.lucid.domchat.ui.theme.LocalDarkMode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.R
import me.lucid.domchat.Screen
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.ui.components.ui.Tooltip
import me.lucid.domchat.ui.components.chat.ConversationRowSurface
import me.lucid.domchat.ui.components.nav.AetherDrawerSearch
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.ui.text.style.TextAlign
import me.lucid.domchat.ui.modifier.domChatSoftEdgeBorder
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.utils.toLocalString
import me.lucid.domchat.utils.toLocalTime
import java.time.LocalDate
import java.time.ZoneId
import kotlin.uuid.Uuid

/**
 * Represents different types of items in the conversation list
 */
sealed class ConversationListItem {
    data class DateHeader(
        val date: LocalDate,
        val label: String
    ) : ConversationListItem()
    data object PinnedHeader : ConversationListItem()
    data class Item(
        val conversation: Conversation
    ) : ConversationListItem()
}

@Composable
fun ColumnScope.ConversationList(
    current: Conversation,
    conversations: LazyPagingItems<ConversationListItem>,
    conversationJobs: Set<Uuid>,
    completedGenerationIds: Set<Uuid> = emptySet(),
    recentlyRestoredIds: Set<Uuid> = emptySet(),
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchExpanded: Boolean = false,
    onSearchExpandedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    onClick: (Conversation) -> Unit = {},
    onDelete: (Conversation) -> Unit = {},
    onRegenerateTitle: (Conversation) -> Unit = {},
    onEditTitle: (Conversation, String) -> Unit = { _, _ -> },
    onPin: (Conversation) -> Unit = {},
    getAssistant: (Uuid) -> Assistant? = { null },
    quickActions: (@Composable () -> Unit)? = null,
    bottomContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    val navController = LocalNavController.current
    Box(modifier = modifier) {
        val listState = rememberLazyListState()
        val canScrollBackward by remember {
            derivedStateOf { listState.canScrollBackward }
        }
        val canScrollForward by remember {
            derivedStateOf { listState.canScrollForward }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = if (isSearchExpanded || searchQuery.isNotBlank()) 88.dp else 64.dp,
                bottom = if (bottomContent != null) 58.dp else 8.dp,
            ),
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Quick action buttons (Imagine, Stats) - animated visibility
                    AnimatedVisibility(
                        visible = !isSearchExpanded && quickActions != null,
                        enter = fadeIn(animationSpec = spring(stiffness = 300f)) + expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)),
                        exit = fadeOut(animationSpec = spring(stiffness = 500f)) + shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f))
                    ) {
                        Column {
                            if (quickActions != null) {
                                quickActions()
                            }
                        }
                    }
                }
            }
            if (conversations.itemCount == 0) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = domChatCardContainerColor(),
                        border = domChatSoftEdgeBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = stringResource(id = R.string.chat_page_no_conversations),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Start a new conversation or select a character to begin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(
                count = conversations.itemCount,
                key = conversations.itemKey { item ->
                    when (item) {
                        is ConversationListItem.DateHeader -> "date_${item.date}"
                        is ConversationListItem.PinnedHeader -> "pinned_header"
                        is ConversationListItem.Item -> item.conversation.id.toString()
                    }
                }
            ) { index ->
                when (val item = conversations[index]) {
                    is ConversationListItem.DateHeader -> {
                        DateHeaderItem(
                            label = item.label,
                            modifier = Modifier.animateItem(
                                fadeInSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 300f),
                                fadeOutSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 300f),
                                placementSpec = androidx.compose.animation.core.spring(
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                                    visibilityThreshold = androidx.compose.ui.unit.IntOffset.VisibilityThreshold
                                )
                            )
                        )
                    }

                    is ConversationListItem.PinnedHeader -> {
                        PinnedHeader(
                            modifier = Modifier.animateItem(
                                fadeInSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 300f),
                                fadeOutSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 300f),
                                placementSpec = androidx.compose.animation.core.spring(
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                                    visibilityThreshold = androidx.compose.ui.unit.IntOffset.VisibilityThreshold
                                )
                            )
                        )
                    }

                    is ConversationListItem.Item -> {
                        ConversationItem(
                            conversation = item.conversation,
                            selected = item.conversation.id == current.id,
                            loading = item.conversation.id in conversationJobs,
                            generationComplete = item.conversation.id in completedGenerationIds,
                            isRecentlyRestored = item.conversation.id in recentlyRestoredIds,
                            onClick = onClick,
                            onDelete = onDelete,
                            onRegenerateTitle = onRegenerateTitle,
                            onEditTitle = onEditTitle,
                            onPin = onPin,
                            searchQuery = searchQuery,
                            getAssistant = getAssistant,
                            modifier = Modifier.animateItem(
                                fadeInSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 300f),
                                fadeOutSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 300f),
                                placementSpec = androidx.compose.animation.core.spring(
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                                    visibilityThreshold = androidx.compose.ui.unit.IntOffset.VisibilityThreshold
                                )
                            )
                        )
                    }

                    null -> {
                        // Placeholder for loading state
                    }
                }
            }
        }

        if (canScrollBackward) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(112.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to MaterialTheme.colorScheme.surfaceContainerLow,
                                0.35f to MaterialTheme.colorScheme.surfaceContainerLow,
                                1f to Color.Transparent,
                            )
                        )
                    )
            )
        }

        if (canScrollForward) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.78f to MaterialTheme.colorScheme.surfaceContainerLow,
                                1f to MaterialTheme.colorScheme.surfaceContainerLow,
                            )
                        )
                    )
            )
        }

        AetherDrawerSearch(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            expanded = isSearchExpanded,
            onExpandedChange = onSearchExpandedChange,
            placeholder = stringResource(id = R.string.chat_page_search_placeholder),
            hint = stringResource(R.string.chat_page_search_hint),
            containerColor = domChatCardContainerColor(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f),
        )

        bottomContent?.invoke(this)
    }
}

@Composable
private fun DateHeaderItem(
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PinnedHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.PushPin,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.pinned_chats),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    selected: Boolean,
    loading: Boolean,
    generationComplete: Boolean,
    isRecentlyRestored: Boolean = false,
    modifier: Modifier = Modifier,
    onDelete: (Conversation) -> Unit = {},
    onRegenerateTitle: (Conversation) -> Unit = {},
    onEditTitle: (Conversation, String) -> Unit = { _, _ -> },
    onPin: (Conversation) -> Unit = {},
    searchQuery: String = "",
    getAssistant: (Uuid) -> Assistant? = { null },
    onClick: (Conversation) -> Unit
) {
    val haptics = rememberPremiumHaptics()
    val loadingDescription = stringResource(R.string.loading)
    val isDark = LocalDarkMode.current

    // Fade-in animation for recently restored items
    var hasAnimated by remember { mutableStateOf(!isRecentlyRestored) }
    val restoredAlpha by animateFloatAsState(
        targetValue = if (hasAnimated) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "restored_alpha"
    )
    LaunchedEffect(isRecentlyRestored) {
        if (isRecentlyRestored && !hasAnimated) {
            kotlinx.coroutines.delay(50) // Small delay to ensure item is in layout
            hasAnimated = true
        }
    }
    
    var showDropdownMenu by remember {
        mutableStateOf(false)
    }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var editedTitle by remember(conversation.id) { mutableStateOf(conversation.title) }
    val messageOnlyMatch = searchQuery.isNotBlank() &&
        !conversation.title.contains(searchQuery, ignoreCase = true)

    val assistant = getAssistant(conversation.assistantId)

    ConversationRowSurface(
        selected = selected,
        restingAlpha = restoredAlpha,
        modifier = modifier,
        onClick = {
            haptics.perform(HapticPattern.Tick)
            onClick(conversation)
        },
        onLongClick = {
            haptics.perform(HapticPattern.Buildup)
            showDropdownMenu = true
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 0.5.dp,
                    brush = if (selected) LiquidGlassTokens.SpecularBorderSubtle else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Edge-to-edge character pfp on the right, showing 35% of the image smoothly blending into the left
            if (assistant != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .fillMaxWidth(0.35f)
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                ) {
                    when (val av = assistant.avatar) {
                        is Avatar.Image -> {
                            AsyncImage(
                                model = av.url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                alignment = BiasAlignment(0f, -0.14f),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                0.0f to Color.Transparent,
                                                0.35f to Color.Black.copy(alpha = 0.45f),
                                                1.0f to Color.Black
                                            ),
                                            blendMode = BlendMode.DstIn
                                        )
                                    }
                            )
                        }
                        is Avatar.Resource -> {
                            AsyncImage(
                                model = av.id,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                alignment = BiasAlignment(0f, -0.14f),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                0.0f to Color.Transparent,
                                                0.35f to Color.Black.copy(alpha = 0.45f),
                                                1.0f to Color.Black
                                            ),
                                            blendMode = BlendMode.DstIn
                                        )
                                    }
                            )
                        }
                        else -> {}
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (conversation.isFork) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = me.lucid.domchat.R.drawable.ic_fork_left),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(20.dp),
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = if (assistant != null) 48.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = conversation.title.ifBlank { stringResource(id = R.string.chat_page_new_message) },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                    AnimatedVisibility(visible = messageOnlyMatch) {
                        Text(
                            text = stringResource(R.string.chat_page_search_message_match),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val msgCount = conversation.messageNodes.size
                    val zdt = conversation.updateAt.atZone(ZoneId.systemDefault())
                    val hour12 = if (zdt.hour % 12 == 0) 12 else zdt.hour % 12
                    val amPm = if (zdt.hour < 12) "AM" else "PM"
                    val timeStr = "%d:%02d %s".format(hour12, zdt.minute, amPm)
                    Text(
                        text = "$msgCount messages · $timeStr",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                AnimatedVisibility(conversation.isPinned) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = stringResource(R.string.chat_message_pinned),
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                AnimatedVisibility(loading) {
                    LoadingIndicator(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(18.dp)
                            .semantics {
                                contentDescription = loadingDescription
                            },
                        color = LocalContentColor.current,
                    )
                }
                AnimatedVisibility(!loading && generationComplete) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(LocalContentColor.current)
                            .size(7.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false },
            shape = RoundedCornerShape(18.dp),
            containerColor = if (isDark) Color(0xFF141418) else MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (conversation.isPinned) stringResource(R.string.unpin_chat) else stringResource(R.string.pin_chat)
                    )
                },
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    onPin(conversation)
                    showDropdownMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.PushPin,
                        null
                    )
                }
            )

            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.chat_page_edit_title))
                },
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    editedTitle = conversation.title
                    showEditTitleDialog = true
                    showDropdownMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Edit, null)
                }
            )

            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.chat_page_regenerate_title))
                },
                onClick = {
                    haptics.perform(HapticPattern.Tick)
                    onRegenerateTitle(conversation)
                    showDropdownMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Refresh, null)
                }
            )

            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.chat_page_delete))
                },
                onClick = {
                    haptics.perform(HapticPattern.Error)
                    onDelete(conversation)
                    showDropdownMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Delete, null)
                }
            )
        }

        if (showEditTitleDialog) {
            AlertDialog(
                onDismissRequest = { showEditTitleDialog = false },
                title = { Text(stringResource(R.string.chat_page_edit_title)) },
                text = {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        singleLine = true,
                        shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEditTitle(conversation, editedTitle)
                            showEditTitleDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.chat_page_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditTitleDialog = false }) {
                        Text(stringResource(R.string.chat_page_cancel))
                    }
                }
            )
        }
    }
}
