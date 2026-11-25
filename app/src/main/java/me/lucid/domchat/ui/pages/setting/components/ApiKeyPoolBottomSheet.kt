package me.lucid.domchat.ui.pages.setting.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalConfiguration
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.lucid.ai.provider.ApiKeyEntry
import me.lucid.ai.provider.ProviderSetting
import me.lucid.ai.provider.withApiKeyPool
import me.lucid.ai.provider.withKeyPoolConfig
import me.lucid.ai.util.KeyRoulette
import me.lucid.domchat.R
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.components.ui.ItemPosition
import me.lucid.domchat.ui.components.ui.PhysicsSwipeToDelete
import me.lucid.domchat.ui.components.ui.ToastAction
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.appFieldColors
import me.lucid.domchat.ui.theme.LocalDarkMode
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.uuid.Uuid

/**
 * Bottom sheet dialog displaying the list of pooled API keys for a provider.
 * Follows the Lorebook interface pattern with list grouping, swipe-to-delete with undo,
 * and drag-to-reorder for priority assignment.
 */
@Composable
fun ApiKeyPoolBottomSheet(
    provider: ProviderSetting,
    onEdit: (ProviderSetting) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val toaster = LocalToaster.current
    val haptics = rememberPremiumHaptics()
    val isDark = LocalDarkMode.current
    val roulette = remember { KeyRoulette.default() }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<ApiKeyEntry?>(null) }

    val pool = provider.apiKeyPool
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val newList = pool.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        onEdit(provider.withApiKeyPool(newList))
        haptics.perform(HapticPattern.Selection)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF000000),
            shape = AppShapes.SettingBottomSheet,
            dragHandle = {
                DomChatSheetDragHandle()
            },
            maxHeight = (LocalConfiguration.current.screenHeightDp * 0.88f).dp,
        ) {
            me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DomChatSheetHeader(
                title = stringResource(R.string.api_key_pool_title),
                subtitle = provider.name.ifBlank { "Provider" },
                actions = {
                    IconButton(
                        onClick = {
                            haptics.perform(HapticPattern.Pop)
                            showSettingsDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = stringResource(R.string.api_key_pool_settings_title),
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    IconButton(
                        onClick = {
                            haptics.perform(HapticPattern.Pop)
                            showAddDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.api_key_pool_add_key),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                onCloseClick = onDismiss,
            )

            if (pool.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.65f).copy(alpha = 0.5f),
                    )
                    Text(
                        text = stringResource(R.string.api_key_pool_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                    Button(
                        onClick = {
                            haptics.perform(HapticPattern.Pop)
                            showAddDialog = true
                        },
                        shape = AppShapes.ButtonPill,
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.api_key_pool_add_key))
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    itemsIndexed(pool, key = { _, item -> item.id }) { index, entry ->
                        val health = roulette.getKeyHealth(entry.id)
                        val hasAuthError = health.hasAuthError
                        val hasQuotaError = health.hasQuotaError
                        val hasError = hasAuthError || hasQuotaError

                        val position = when {
                            pool.size == 1 -> ItemPosition.ONLY
                            index == 0 -> ItemPosition.FIRST
                            index == pool.lastIndex -> ItemPosition.LAST
                            else -> ItemPosition.MIDDLE
                        }

                        val resolvedSecret = provider.resolvedApiKeyPool
                            .find { it.id == entry.id }?.value
                            ?: entry.key
                        val maskedKey = when {
                            resolvedSecret.length > 8 -> "••••" + resolvedSecret.takeLast(4)
                            resolvedSecret.isNotBlank() -> "••••••••"
                            else -> "••••••••"
                        }

                        ReorderableItem(state = reorderableState, key = entry.id) { isDragging ->
                            PhysicsSwipeToDelete(
                                position = position,
                                deleteEnabled = true,
                                onDelete = {
                                    val deletedEntry = entry
                                    val updatedPool = pool.filter { it.id != entry.id }
                                    onEdit(provider.withApiKeyPool(updatedPool))
                                    toaster.show(
                                        message = context.getString(
                                            R.string.api_key_pool_deleted,
                                            entry.name.ifBlank { "Key ${index + 1}" }
                                        ),
                                        action = ToastAction(
                                            label = context.getString(R.string.undo),
                                            onClick = {
                                                val restored = pool.toMutableList().apply {
                                                    add(index.coerceAtMost(size), deletedEntry)
                                                }
                                                onEdit(provider.withApiKeyPool(restored))
                                            }
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .scale(if (isDragging) 0.95f else 1f)
                                    .fillMaxWidth()
                            ) { shape ->
                                ApiKeyCard(
                                    entry = entry,
                                    priority = index + 1,
                                    shape = shape,
                                    hasAuthError = hasAuthError,
                                    hasQuotaError = hasQuotaError,
                                    maskedKey = maskedKey,
                                    resolvedSecret = resolvedSecret,
                                    isDragging = isDragging,
                                    onEdit = { editingEntry = entry },
                                    onToggleEnabled = { enabled ->
                                        val updatedPool = pool.map { if (it.id == entry.id) it.copy(enabled = enabled) else it }
                                        onEdit(provider.withApiKeyPool(updatedPool))
                                    },
                                    dragHandle = {
                                        IconButton(
                                            onClick = {},
                                            modifier = Modifier
                                                .size(36.dp)
                                                .draggableHandle(
                                                    onDragStarted = { haptics.perform(HapticPattern.Pop) },
                                                    onDragStopped = { haptics.perform(HapticPattern.Thud) },
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DragIndicator,
                                                contentDescription = null,
                                                modifier = Modifier.size(22.dp),
                                                tint = Color.White.copy(alpha = 0.65f)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Pool Settings Dialog
    if (showSettingsDialog) {
        ApiKeyPoolSettingsDialog(
            initialConfig = provider.keyPoolConfig,
            onConfirm = { newConfig ->
                onEdit(provider.withKeyPoolConfig(newConfig))
                showSettingsDialog = false
            },
            onDismiss = { showSettingsDialog = false },
        )
    }

    // Add Key Dialog
    if (showAddDialog) {
        val defaultName = when (pool.size) {
            0 -> "Primary"
            1 -> "Secondary"
            2 -> "Tertiary"
            else -> "Key ${pool.size + 1}"
        }
        ApiKeyEditDialog(
            title = stringResource(R.string.api_key_pool_add_key),
            initialName = defaultName,
            initialKey = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, key ->
                val newEntry = ApiKeyEntry(
                    id = Uuid.random(),
                    name = name.ifBlank { defaultName },
                    enabled = true,
                    exportable = true,
                    key = key,
                )
                onEdit(provider.withApiKeyPool(pool + newEntry))
                showAddDialog = false
            }
        )
    }

    // Edit Key Dialog
    editingEntry?.let { entry ->
        val resolvedSecret = provider.resolvedApiKeyPool
            .find { it.id == entry.id }?.value
            ?: entry.key
        ApiKeyEditDialog(
            title = stringResource(R.string.api_key_pool_edit_key),
            initialName = entry.name,
            initialKey = resolvedSecret,
            onDismiss = { editingEntry = null },
            onConfirm = { name, key ->
                val updatedPool = pool.map {
                    if (it.id == entry.id) {
                        it.copy(
                            name = name.ifBlank { entry.name },
                            key = key,
                        )
                    } else it
                }
                onEdit(provider.withApiKeyPool(updatedPool))
                editingEntry = null
            }
        )
    }
}

/**
 * Individual API Key list item card inside PhysicsSwipeToDelete with grouped shapes.
 */
@Composable
private fun ApiKeyCard(
    entry: ApiKeyEntry,
    priority: Int,
    shape: Shape,
    hasAuthError: Boolean,
    hasQuotaError: Boolean,
    maskedKey: String,
    resolvedSecret: String,
    isDragging: Boolean,
    onEdit: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    dragHandle: @Composable () -> Unit,
) {
    val hasError = hasAuthError || hasQuotaError
    var isRevealed by remember { mutableStateOf(false) }

    Card(
        onClick = onEdit,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF000000),
            contentColor = Color(0xFFF2F2F7)
        ),
        border = if (hasError) {
            BorderStroke(0.5.dp, Color(0xFFFF453A))
        } else {
            BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 6.dp else 0.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Priority Circle (Left)
            Surface(
                shape = CircleShape,
                color = when {
                    hasError -> Color(0xFFFF453A)
                    !entry.enabled -> MaterialTheme.colorScheme.outlineVariant
                    else -> Color.White.copy(alpha = 0.65f)
                },
                modifier = Modifier.size(28.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = priority.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            hasError -> MaterialTheme.colorScheme.onError
                            !entry.enabled -> Color.White.copy(alpha = 0.65f)
                            else -> Color.Black
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Middle Content (Name, Masked Key / Plain Key, Auth Warning)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = entry.name.ifBlank { "Key $priority" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (hasError) {
                            Color(0xFFFF453A)
                        } else if (!entry.enabled) {
                            Color(0xFFF2F2F7).copy(alpha = 0.55f)
                        } else {
                            Color(0xFFF2F2F7)
                        }
                    )
                    IconButton(
                        onClick = { isRevealed = !isRevealed },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isRevealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (isRevealed) "Hide Key" else "Reveal Key",
                            tint = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = if (isRevealed && resolvedSecret.isNotBlank()) resolvedSecret else maskedKey,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (isRevealed) 3 else 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (hasError) {
                        Color(0xFFFF453A).copy(alpha = 0.8f)
                    } else {
                        Color(0xFFF2F2F7).copy(alpha = 0.55f)
                    }
                )

                if (hasError) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFF453A)
                        )
                        Text(
                            text = stringResource(R.string.api_key_auth_error_warning),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF453A),
                            maxLines = 2,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Switch to enable/disable this key
            HapticSwitch(
                checked = entry.enabled,
                onCheckedChange = onToggleEnabled
            )

            Spacer(modifier = Modifier.width(4.dp))

            // 4. Drag Handle (Right)
            dragHandle()
        }
    }
}

/**
 * Dialog for adding or editing an API key entry.
 * Simple and bloat-free: only asks for key name and key secret.
 */
@Composable
private fun ApiKeyEditDialog(
    title: String,
    initialName: String,
    initialKey: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, key: String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var key by remember { mutableStateOf(initialKey) }
    var keyVisible by remember { mutableStateOf(false) }
    val haptics = rememberPremiumHaptics()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF000000),
            shape = AppShapes.SettingBottomSheet,
            dragHandle = {
                DomChatSheetDragHandle()
            },
            modifier = Modifier.imePadding(),
        ) {
            me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DomChatSheetHeader(
                title = title,
                onCloseClick = onDismiss,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.api_key_pool_key_name)) },
                placeholder = { Text(initialName) },
                singleLine = true,
                shape = AppShapes.InputField,
                modifier = Modifier.fillMaxWidth(),
                colors = appFieldColors(),
            )

            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text(stringResource(R.string.api_key_pool_key_value)) },
                placeholder = { Text("sk-...") },
                singleLine = true,
                shape = AppShapes.InputField,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            imageVector = if (keyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.65f)
                        )
                    }
                },
                colors = appFieldColors(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = Color.White.copy(alpha = 0.7f))
                }
                Surface(
                    onClick = {
                        if (key.isNotBlank()) {
                            haptics.perform(HapticPattern.Pop)
                            onConfirm(name, key)
                        }
                    },
                    enabled = key.isNotBlank(),
                    shape = AppShapes.ButtonRounded,
                    color = Color(0xFF000000),
                    contentColor = Color.White,
                    border = BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier.height(44.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.confirm),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (key.isNotBlank()) Color.White else Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
}
