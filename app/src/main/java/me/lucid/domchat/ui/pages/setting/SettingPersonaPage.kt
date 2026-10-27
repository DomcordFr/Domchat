package me.lucid.domchat.ui.pages.setting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.data.model.UserPersona
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.domchat.data.datastore.withActivePersona
import me.lucid.domchat.ui.components.crop.SquareCropImageScreen
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.modifier.domChatFloatingSurfaceColor
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import androidx.compose.ui.graphics.Brush
import me.lucid.domchat.utils.plus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPersonaPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val settingsStore: SettingsStore = koinInject()
    val scope = rememberCoroutineScope()
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val amoledMode by rememberAmoledDarkMode()
    val isDark = LocalDarkMode.current

    val personas = remember(settings.displaySetting.personas, settings.displaySetting.userNickname) {
        if (settings.displaySetting.personas.isEmpty()) {
            listOf(
                UserPersona(
                    id = "default",
                    name = settings.displaySetting.userNickname.ifBlank { "User" },
                    pronouns = "",
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
    var searchQuery by remember { mutableStateOf("") }

    // Editor state
    var showEditor by remember { mutableStateOf(false) }
    var editingTargetId by remember { mutableStateOf<String?>(null) }
    var formName by remember { mutableStateOf("") }
    var formPronouns by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    var formAvatar by remember { mutableStateOf<Avatar>(Avatar.Dummy) }

    // Built-in cropper state
    var showCropper by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showCropper = true
        }
    }

    fun openCreate() {
        haptics.perform(HapticPattern.Pop)
        editingTargetId = null
        formName = ""
        formPronouns = ""
        formDescription = ""
        formAvatar = Avatar.Dummy
        showEditor = true
    }

    fun openEdit(persona: UserPersona) {
        haptics.perform(HapticPattern.Pop)
        editingTargetId = persona.id
        formName = persona.name
        formPronouns = persona.pronouns
        formDescription = persona.description
        formAvatar = persona.avatar
        showEditor = true
    }

    fun savePersona() {
        haptics.perform(HapticPattern.Pop)
        val nameToSave = formName.trim().ifBlank { "User" }
        scope.launch {
            settingsStore.update { cur ->
                val display = cur.displaySetting
                val existing = if (display.personas.isEmpty()) personas else display.personas
                val updated = if (editingTargetId == null) {
                    val newP = UserPersona(
                        name = nameToSave,
                        pronouns = formPronouns.trim(),
                        description = formDescription.trim(),
                        avatar = formAvatar,
                        isDefault = existing.isEmpty()
                    )
                    existing + newP
                } else {
                    existing.map { p ->
                        if (p.id == editingTargetId) {
                            p.copy(
                                name = nameToSave,
                                pronouns = formPronouns.trim(),
                                description = formDescription.trim(),
                                avatar = formAvatar
                            )
                        } else p
                    }
                }
                val newActiveId = editingTargetId ?: updated.last().id
                cur.copy(
                    displaySetting = display.copy(
                        personas = updated,
                        activePersonaId = if (display.activePersonaId == null) newActiveId else display.activePersonaId
                    )
                )
            }
            showEditor = false
        }
    }

    fun setActive(id: String) {
        haptics.perform(HapticPattern.Pop)
        scope.launch {
            settingsStore.update { cur ->
                cur.copy(displaySetting = cur.displaySetting.withActivePersona(id))
            }
        }
    }

    fun deletePersona(id: String) {
        haptics.perform(HapticPattern.Pop)
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

    val filteredPersonas = remember(personas, searchQuery) {
        if (searchQuery.isBlank()) personas
        else personas.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.pronouns.contains(searchQuery, ignoreCase = true)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = "Personas",
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openCreate() },
                containerColor = if (isDark) Color.White else Color.Black,
                contentColor = if (isDark) Color.Black else Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "New Persona")
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding),
            contentPadding = innerPadding + PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search personas...") },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Personas List
            items(filteredPersonas, key = { it.id }) { persona ->
                val isActive = persona.id == activePersona.id || (persona.isDefault && activePersona.id == null)
                val cardShape = RoundedCornerShape(20.dp)
                val borderBrush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isActive) 0.38f else (if (isDark) 0.22f else 0.12f)),
                        Color.White.copy(alpha = if (isActive) 0.15f else (if (isDark) 0.06f else 0.03f))
                    )
                )

                Surface(
                    color = if (isActive) {
                        if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)
                    } else {
                        domChatCardContainerColor()
                    },
                    shape = cardShape,
                    border = BorderStroke(
                        width = if (isActive) 1.dp else 0.5.dp,
                        brush = borderBrush
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UIAvatar(
                                name = persona.name,
                                value = persona.avatar,
                                modifier = Modifier
                                    .size(52.dp)
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
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                                    )

                                    if (persona.pronouns.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = persona.pronouns,
                                                fontSize = 11.sp,
                                                color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF555555)
                                            )
                                        }
                                    }

                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isDark) Color.White else Color.Black)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }

                                if (persona.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = persona.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isActive) {
                                Button(
                                    onClick = { setActive(persona.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDark) Color.White else Color.Black,
                                        contentColor = if (isDark) Color.Black else Color.White
                                    )
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Use in Chat", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Surface(
                                onClick = { openEdit(persona) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Edit", fontSize = 12.sp)
                                }
                            }

                            if (personas.size > 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { deletePersona(persona.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Persona Creator/Editor Dialog
    if (showEditor) {
        ApplyDialogWindowBlur()
        val dialogShape = RoundedCornerShape(28.dp)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditor = false },
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .border(0.5.dp, LiquidGlassTokens.SpecularBorderBrush, dialogShape),
            shape = dialogShape,
            containerColor = domChatFloatingSurfaceColor(),
            title = {
                Text(
                    text = if (editingTargetId == null) "Create Persona" else "Edit Persona",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                val inputShape = RoundedCornerShape(14.dp)
                val inputColors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                    unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
                    focusedBorderColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f),
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Built-in Avatar Picker with Square Crop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        UIAvatar(
                            name = formName.ifBlank { "User" },
                            value = formAvatar,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            onUpdate = { formAvatar = it }
                        )

                        Surface(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.18f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pick & Crop", fontSize = 12.5.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formName,
                        onValueChange = { formName = it },
                        label = { Text("Name (e.g. Detective, Anon)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = inputShape,
                        colors = inputColors
                    )

                    OutlinedTextField(
                        value = formPronouns,
                        onValueChange = { formPronouns = it },
                        label = { Text("Pronouns (e.g. he/him, they/them)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = inputShape,
                        colors = inputColors
                    )

                    OutlinedTextField(
                        value = formDescription,
                        onValueChange = { formDescription = it },
                        label = { Text("Lore / Background (Injected for {{user}})") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                        shape = inputShape,
                        colors = inputColors
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { savePersona() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color.White else Color.Black,
                        contentColor = if (isDark) Color.Black else Color.White
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false }) {
                    Text("Cancel", color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666))
                }
            }
        )
    }

    // Square Cropper integration
    if (showCropper && selectedImageUri != null) {
        SquareCropImageScreen(
            sourceUri = selectedImageUri!!,
            onCropComplete = { croppedUri ->
                showCropper = false
                selectedImageUri = null
                formAvatar = Avatar.Image(croppedUri.toString())
            },
            onCancel = {
                showCropper = false
                selectedImageUri = null
            }
        )
    }
}
