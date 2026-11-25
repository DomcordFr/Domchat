package me.lucid.domchat.ui.pages.extensions.workspace

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.R
import me.lucid.domchat.Screen
import me.lucid.domchat.data.db.entity.WorkspaceEntity
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.ui.ItemPosition
import me.lucid.domchat.ui.components.ui.PhysicsSwipeToDelete
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.utils.plus
import org.koin.androidx.compose.koinViewModel

@Composable
fun WorkspacePage(vm: WorkspaceVM = koinViewModel()) {
    val navController = LocalNavController.current
    val haptics = rememberPremiumHaptics()
    val workspaces by vm.workspaces.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<WorkspaceEntity?>(null) }

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = stringResource(R.string.workspace_page_title),
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                FloatingActionButton(
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        showAddDialog = true
                    },
                    shape = AppShapes.CardLarge,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.workspace_page_create))
                }
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = innerPadding + PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (workspaces.isEmpty()) {
                item {
                    EmptyWorkspaceState()
                }
            }

            itemsIndexed(workspaces, key = { _, workspace -> workspace.id }) { index, workspace ->
                val position = when {
                    workspaces.size == 1 -> ItemPosition.ONLY
                    index == 0 -> ItemPosition.FIRST
                    index == workspaces.lastIndex -> ItemPosition.LAST
                    else -> ItemPosition.MIDDLE
                }

                WorkspaceCard(
                    workspace = workspace,
                    position = position,
                    onDelete = { deleteTarget = workspace },
                    onOpen = {
                        haptics.perform(HapticPattern.Pop)
                        navController.navigate(Screen.WorkspaceDetail(workspace.id))
                    },
                )
            }
        }
    }

    if (showAddDialog) {
        EditWorkspaceDialog(
            title = stringResource(R.string.workspace_page_create),
            initialName = "",
            existingNames = workspaces.map { it.name.trim() }.toSet(),
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                vm.create(name)
                showAddDialog = false
            },
        )
    }

    deleteTarget?.let { workspace ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = me.lucid.domchat.ui.modifier.domChatDialogContainerColor(),
            title = {
    me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
    Text(stringResource(R.string.workspace_page_delete)) },
            text = { Text(stringResource(R.string.workspace_page_delete_confirm)) },
            confirmButton = {
                DomChatGlassPillButton(
                    text = stringResource(R.string.common_confirm),
                    onClick = {
                        haptics.perform(HapticPattern.Thud)
                        vm.delete(workspace)
                        deleteTarget = null
                    }
                )
            },
            dismissButton = {
                DomChatGlassPillButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        deleteTarget = null
                    }
                )
            },
        )
    }
}

@Composable
private fun EmptyWorkspaceState() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141520).copy(alpha = 0.55f)
        ),
        shape = AppShapes.CardLarge,
        modifier = Modifier.border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, AppShapes.CardLarge)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Terminal,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = stringResource(R.string.workspace_page_empty),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.workspace_page_empty_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WorkspaceCard(
    workspace: WorkspaceEntity,
    position: ItemPosition,
    onDelete: () -> Unit,
    onOpen: () -> Unit,
) {
    PhysicsSwipeToDelete(
        position = position,
        onDelete = onDelete,
    ) { shape ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = workspace.name,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = workspace.shellStatus.toShellStatusLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditWorkspaceDialog(
    title: String,
    initialName: String,
    existingNames: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    val trimmedName = name.trim()
    val isDuplicate = trimmedName.isNotEmpty() && trimmedName in existingNames

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = me.lucid.domchat.ui.modifier.domChatDialogContainerColor(),
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.workspace_page_name)) },
                singleLine = true,
                isError = isDuplicate,
                shape = AppShapes.InputField,
                supportingText = if (isDuplicate) {
                    { Text(stringResource(R.string.workspace_page_name_duplicate)) }
                } else null,
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
                    focusedSupportingTextColor = Color(0xFFFF6B6B),
                ),
            )
        },
        confirmButton = {
            DomChatGlassPillButton(
                text = stringResource(R.string.common_save),
                accent = true,
                enabled = name.isNotBlank() && !isDuplicate,
                onClick = { onConfirm(trimmedName) },
            )
        },
        dismissButton = {
            DomChatGlassPillButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
            )
        },
    )
}
