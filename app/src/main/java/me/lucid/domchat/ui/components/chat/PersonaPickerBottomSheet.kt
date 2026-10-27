package me.lucid.domchat.ui.components.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import me.lucid.domchat.Screen
import me.lucid.domchat.ui.context.LocalNavController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import me.lucid.domchat.ui.theme.LocalDarkMode
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.domchat.data.datastore.withActivePersona
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.data.model.UserPersona
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaPickerBottomSheet(
    settings: Settings,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsStore: SettingsStore = koinInject()
    val scope = rememberCoroutineScope()
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val amoledMode by rememberAmoledDarkMode()
    val isDark = LocalDarkMode.current
    val navController = LocalNavController.current

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

    val currentPersonas = remember(settings.displaySetting.personas, settings.displaySetting.userNickname) {
        if (settings.displaySetting.personas.isEmpty()) {
            listOf(
                UserPersona(
                    id = "default",
                    name = settings.displaySetting.userNickname.ifBlank { "User" },
                    description = "",
                    avatar = settings.displaySetting.userAvatar,
                    isDefault = true
                )
            )
        } else {
            settings.displaySetting.personas
        }
    }

    val activePersona = settings.displaySetting.getActivePersona()
    var isEditingOrCreating by remember { mutableStateOf(false) }
    var editingTargetId by remember { mutableStateOf<String?>(null) }
    var formName by remember { mutableStateOf("") }
    var formPronouns by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    var formAvatarText by remember { mutableStateOf("") }

    val avatarGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            formAvatarText = uri.toString()
        }
    }

    fun openCreateForm() {
        editingTargetId = null
        formName = ""
        formPronouns = ""
        formDescription = ""
        formAvatarText = ""
        isEditingOrCreating = true
    }

    fun openEditForm(persona: UserPersona) {
        editingTargetId = persona.id
        formName = persona.name
        formPronouns = persona.pronouns
        formDescription = persona.description
        formAvatarText = when (val a = persona.avatar) {
            is Avatar.Emoji -> a.content
            is Avatar.Image -> a.url
            else -> ""
        }
        isEditingOrCreating = true
    }

    fun savePersona() {
        val targetName = formName.trim().ifBlank { "User" }
        val targetAvatar = when {
            formAvatarText.trim().startsWith("http") || formAvatarText.trim().startsWith("content://") || formAvatarText.trim().startsWith("file://") -> Avatar.Image(formAvatarText.trim())
            formAvatarText.trim().isNotBlank() -> Avatar.Emoji(formAvatarText.trim())
            else -> Avatar.Dummy
        }

        scope.launch {
            settingsStore.update { cur ->
                val display = cur.displaySetting
                val existing = if (display.personas.isEmpty()) currentPersonas else display.personas
                val updatedList = if (editingTargetId == null) {
                    val newP = UserPersona(
                        name = targetName,
                        pronouns = formPronouns.trim(),
                        description = formDescription.trim(),
                        avatar = targetAvatar,
                        isDefault = existing.isEmpty()
                    )
                    existing + newP
                } else {
                    existing.map { p ->
                        if (p.id == editingTargetId) {
                            p.copy(
                                name = targetName,
                                pronouns = formPronouns.trim(),
                                description = formDescription.trim(),
                                avatar = targetAvatar
                            )
                        } else p
                    }
                }
                val newActiveId = editingTargetId ?: updatedList.last().id
                cur.copy(
                    displaySetting = display.copy(personas = updatedList).withActivePersona(newActiveId)
                )
            }
            isEditingOrCreating = false
        }
    }

    fun deletePersona(id: String) {
        scope.launch {
            settingsStore.update { cur ->
                val remaining = cur.displaySetting.personas.filterNot { it.id == id }
                val fallbackId = remaining.firstOrNull()?.id
                cur.copy(
                    displaySetting = cur.displaySetting.copy(
                        personas = remaining,
                        activePersonaId = if (cur.displaySetting.activePersonaId == id) fallbackId else cur.displaySetting.activePersonaId
                    )
                )
            }
        }
    }

    fun selectPersona(id: String) {
        haptics.perform(HapticPattern.Pop)
        scope.launch {
            settingsStore.update { cur ->
                cur.copy(
                    displaySetting = cur.displaySetting.withActivePersona(id)
                )
            }
            sheetState.hide()
            onDismissRequest()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    val filteredPersonas = remember(currentPersonas, searchQuery) {
        if (searchQuery.isBlank()) currentPersonas
        else currentPersonas.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    AetherInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.48f)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "User Personas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Switch active identity for {{user}} context",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isEditingOrCreating) {
                    val isDark = LocalDarkMode.current
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                onDismissRequest()
                                navController.navigate(Screen.SettingPersonas)
                            },
                            shape = CircleShape,
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Manage Personas",
                                    tint = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                openCreateForm()
                            },
                            shape = CircleShape,
                            color = if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f),
                            border = BorderStroke(
                                0.75.dp,
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = if (isDark) 0.40f else 0.50f),
                                        Color.White.copy(alpha = if (isDark) 0.12f else 0.18f)
                                    )
                                )
                            ),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "New Persona",
                                    tint = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    val isDark = LocalDarkMode.current
                    Surface(
                        onClick = { isEditingOrCreating = false },
                        shape = CircleShape,
                        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Cancel",
                                tint = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (!isEditingOrCreating) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search personas...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = domChatCardContainerColor(),
                        unfocusedContainerColor = domChatCardContainerColor(),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inline Persona Creator/Editor
            AnimatedVisibility(
                visible = isEditingOrCreating,
                enter = expandVertically(spring(dampingRatio = 0.75f, stiffness = 350f)) + fadeIn(),
                exit = shrinkVertically(spring(dampingRatio = 0.75f, stiffness = 350f)) + fadeOut()
            ) {
                Surface(
                    color = domChatCardContainerColor(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (editingTargetId == null) "Create Persona" else "Edit Persona",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = formName,
                            onValueChange = { formName = it },
                            label = { Text("Persona Name (e.g. Detective, Anon)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = formPronouns,
                            onValueChange = { formPronouns = it },
                            label = { Text("Pronouns (e.g. they/them, she/her, he/him)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = formAvatarText,
                            onValueChange = { formAvatarText = it },
                            label = { Text("Avatar Emoji (e.g. 🦊) or Image URL / Pick") },
                            trailingIcon = {
                                IconButton(onClick = { avatarGalleryLauncher.launch("image/*") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.PhotoCamera,
                                        contentDescription = "Pick Image",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = formDescription,
                            onValueChange = { formDescription = it },
                            label = { Text("Persona Lore / Description (injected into roleplay)") },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { isEditingOrCreating = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    haptics.perform(HapticPattern.Pop)
                                    savePersona()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Save Persona")
                            }
                        }
                    }
                }
            }

            // Personas List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .nestedScroll(overscrollAbsorber)
            ) {
                items(filteredPersonas, key = { it.id }) { persona ->
                    val isActive = persona.id == activePersona.id || (persona.isDefault && activePersona.id == null)

                    Surface(
                        onClick = { selectPersona(persona.id) },
                        color = if (isActive) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            domChatCardContainerColor()
                        },
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = if (isActive) 1.5.dp else 0.5.dp,
                            color = if (isActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UIAvatar(
                                name = persona.name,
                                value = persona.avatar,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = persona.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (persona.pronouns.isNotBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
                                        ) {
                                            Text(
                                                text = persona.pronouns,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (isActive) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (persona.description.isNotBlank()) {
                                    Text(
                                        text = persona.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        openEditForm(persona)
                                    },
                                    shape = CircleShape,
                                    color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                                    border = BorderStroke(0.5.dp, if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "Edit",
                                            tint = if (isDark) Color(0xFFD1D1D8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                if (currentPersonas.size > 1) {
                                    Surface(
                                        onClick = {
                                            haptics.perform(HapticPattern.Pop)
                                            deletePersona(persona.id)
                                        },
                                        shape = CircleShape,
                                        color = if (isDark) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }

                                if (isActive) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(start = 4.dp)
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
