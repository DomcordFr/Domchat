package me.lucid.domchat.ui.pages.setting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clipToBounds
import me.lucid.domchat.ui.components.ui.HapticSwitch
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import me.lucid.domchat.ui.modifier.fadeEdges
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import me.lucid.domchat.ui.components.dialog.SettingsDestructiveConfirmDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import me.lucid.domchat.data.model.Lorebook
import me.lucid.domchat.data.model.UserPersona
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.domchat.data.datastore.withActivePersona
import me.lucid.domchat.ui.components.crop.SquareCropImageScreen
import me.lucid.domchat.ui.components.chat.DomChatSearchBar
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.appFieldColors
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.AppShapes
import androidx.compose.ui.res.stringResource
import me.lucid.domchat.R
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.modifier.domChatDialogContainerColor
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import androidx.compose.ui.graphics.Brush
import me.lucid.domchat.utils.plus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.compose.foundation.layout.statusBarsPadding

private enum class EditorStep {
    Persona,
    LorebookPicker
}

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
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(120)
        isInitialized = true
    }

    // Editor state
    var showEditor by remember { mutableStateOf(false) }
    var editingTargetId by remember { mutableStateOf<String?>(null) }
    var personaToDelete by remember { mutableStateOf<UserPersona?>(null) }
    var formName by remember { mutableStateOf("") }
    var formPronouns by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    var formAvatar by remember { mutableStateOf<Avatar>(Avatar.Dummy) }
    var formLorebookIds by remember { mutableStateOf(emptySet<String>()) }
    var editorStep by remember { mutableStateOf(EditorStep.Persona) }
    var pickerQuery by remember { mutableStateOf("") }
    var pickerSelectedIds by remember { mutableStateOf(emptySet<String>()) }

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
        formLorebookIds = emptySet()
        editorStep = EditorStep.Persona
        showEditor = true
    }

    fun openEdit(persona: UserPersona) {
        haptics.perform(HapticPattern.Pop)
        editingTargetId = persona.id
        formName = persona.name
        formPronouns = persona.pronouns
        formDescription = persona.description
        formAvatar = persona.avatar
        formLorebookIds = persona.enabledLorebookIds
        editorStep = EditorStep.Persona
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
                        enabledLorebookIds = formLorebookIds,
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
                                avatar = formAvatar,
                                enabledLorebookIds = formLorebookIds
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

    val personaListState = rememberLazyListState()
    val canScrollPersonaBackward by remember { derivedStateOf { personaListState.canScrollBackward } }
    val personaTopFadeProgress by animateFloatAsState(
        targetValue = if (canScrollPersonaBackward) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "persona_top_fade"
    )

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = "Personas",
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openCreate() },
                containerColor = Color(0xFF000000),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "New Persona", tint = Color.White)
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            state = personaListState,
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .fadeEdges(topProgress = personaTopFadeProgress, fadeHeight = 44f),
            contentPadding = innerPadding + PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
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
                            tint = Color.White.copy(alpha = 0.65f)
                        )
                    },
                    singleLine = true,
                    shape = me.lucid.domchat.ui.theme.AppShapes.SearchField,
                    colors = appFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(me.lucid.domchat.ui.theme.AppShapes.SearchField)
                        .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SearchField)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Personas List
            items(filteredPersonas, key = { it.id }) { persona ->
                val isActive = persona.id == activePersona.id || (persona.isDefault && activePersona.id == null)
                val cardShape = RoundedCornerShape(18.dp)
                val surfaceColor = Color(0xFF000000)
                val borderWidth by animateDpAsState(
                    targetValue = if (isActive) 1.dp else 0.5.dp,
                    animationSpec = if (isInitialized) tween(durationMillis = 240) else snap(),
                    label = "persona_card_border_width"
                )
                val borderBrush = if (isActive) {
                    LiquidGlassTokens.SpecularBorderBrush
                } else {
                    LiquidGlassTokens.SpecularBorderSubtle
                }

                Surface(
                    color = surfaceColor,
                    shape = cardShape,
                    border = BorderStroke(borderWidth, borderBrush),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .clickable {
                            if (!isActive) {
                                setActive(persona.id)
                            }
                        }
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
                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
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
                                        color = Color.White
                                    )

                                    if (persona.pronouns.isNotBlank()) {
                                        Surface(
                                            color = Color(0xFF000000),
                                            shape = AppShapes.SettingBadge,
                                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                        ) {
                                            Text(
                                                text = persona.pronouns,
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.75f),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = isActive,
                                        enter = if (isInitialized) (fadeIn(tween(200)) + expandHorizontally(spring(stiffness = 380f))) else EnterTransition.None,
                                        exit = fadeOut(tween(150)) + shrinkHorizontally(spring(stiffness = 380f))
                                    ) {
                                        Surface(
                                            color = Color(0xFF000000),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = "ACTIVE",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }

                                if (persona.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = persona.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color.White.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.6f),
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
                            AnimatedVisibility(
                                visible = !isActive,
                                enter = fadeIn(tween(150)) + expandHorizontally(spring(stiffness = 380f)),
                                exit = fadeOut(tween(150)) + shrinkHorizontally(spring(stiffness = 380f))
                            ) {
                                Row {
                                    DomChatGlassPillButton(
                                        text = "Use in Chat",
                                        icon = Icons.Rounded.Check,
                                        onClick = { setActive(persona.id) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }

                            DomChatGlassPillButton(
                                text = "Edit",
                                icon = Icons.Rounded.Edit,
                                onClick = { openEdit(persona) }
                            )

                            if (personas.size > 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { personaToDelete = persona },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFFF453A).copy(alpha = 0.8f),
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
        val dialogShape = me.lucid.domchat.ui.theme.AppShapes.SettingDialog

        // Re-seed picker selection whenever the picker step is entered
        LaunchedEffect(editorStep) {
            if (editorStep == EditorStep.LorebookPicker) {
                pickerSelectedIds = formLorebookIds.toSet()
            }
        }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditor = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(horizontal = 12.dp)
                .border(0.5.dp, LiquidGlassTokens.SpecularBorderBrush, dialogShape),
            shape = dialogShape,
            containerColor = Color(0xFF000000),
            titleContentColor = Color(0xFFF2F2F7),
            textContentColor = Color(0xFFF2F2F7),
            title = {
                AnimatedContent(
                    targetState = editorStep,
                    transitionSpec = {
                        if (targetState == EditorStep.LorebookPicker) {
                            (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { it / 3 } + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { -it / 3 } + fadeOut(tween(160)))
                        } else {
                            (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { -it / 3 } + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { it / 3 } + fadeOut(tween(160)))
                        }
                    },
                    label = "persona_title_step_anim"
                ) { step ->
                    when (step) {
                        EditorStep.Persona -> {
                            Text(
                                text = if (editingTargetId == null) "Create Persona" else "Edit Persona",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        EditorStep.LorebookPicker -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        formLorebookIds = pickerSelectedIds
                                        editorStep = EditorStep.Persona
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Lorebooks",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                ) {
                    AnimatedContent(
                        targetState = editorStep,
                        transitionSpec = {
                            if (targetState == EditorStep.LorebookPicker) {
                                (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { it } + fadeIn(tween(200))) togetherWith
                                (slideOutHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { -it } + fadeOut(tween(160)))
                            } else {
                                (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { -it } + fadeIn(tween(200))) togetherWith
                                (slideOutHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { it } + fadeOut(tween(160)))
                            }.using(SizeTransform(clip = false) { _, _ -> snap() })
                        },
                        label = "persona_body_step_anim"
                    ) { step ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            when (step) {
                        EditorStep.Persona -> {
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
                                        .clip(CircleShape)
                                        .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                                    onUpdate = { formAvatar = it }
                                )

                                Surface(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF000000),
                                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
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

                            val inputShape = RoundedCornerShape(14.dp)
                            OutlinedTextField(
                                value = formName,
                                onValueChange = { formName = it },
                                label = { Text("Name (e.g. Detective, Anon)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = inputShape,
                                colors = appFieldColors(),
                            )

                            OutlinedTextField(
                                value = formPronouns,
                                onValueChange = { formPronouns = it },
                                label = { Text("Pronouns (e.g. he/him, they/them)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = inputShape,
                                colors = appFieldColors(),
                            )

                            OutlinedTextField(
                                value = formDescription,
                                onValueChange = { formDescription = it },
                                label = { Text("Lore / Background (Injected for {{user}})") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier.fillMaxWidth(),
                                shape = inputShape,
                                colors = appFieldColors(),
                            )

                            // Attach Lorebooks — secondary picker opened from this glass card
                            val attachedCount = formLorebookIds.size
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF000000).copy(alpha = 0.16f)),
                                shape = AppShapes.CardSmall,
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptics.perform(HapticPattern.Pop)
                                        editorStep = EditorStep.LorebookPicker
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Attach Lorebooks",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (attachedCount == 0) "None" else "$attachedCount attached",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        EditorStep.LorebookPicker -> {
                            val allLorebooks = settings.lorebooks
                            val filteredLorebooks = remember(allLorebooks, pickerQuery) {
                                if (pickerQuery.isBlank()) allLorebooks
                                else allLorebooks.filter { it.name.contains(pickerQuery, ignoreCase = true) }
                            }

                            DomChatSearchBar(
                                value = pickerQuery,
                                onValueChange = { pickerQuery = it },
                                placeholder = "Search lorebooks...",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (filteredLorebooks.isEmpty()) {
                                    item(key = "empty") {
                                        Text(
                                            text = "No matching lorebooks",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                } else {
                                    items(filteredLorebooks, key = { it.id.toString() }) { lorebook ->
                                        val idStr = lorebook.id.toString()
                                        val isSelected = idStr in pickerSelectedIds
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(AppShapes.CardSmall)
                                                .clickable {
                                                    haptics.perform(HapticPattern.Tick)
                                                    pickerSelectedIds = if (isSelected) {
                                                        pickerSelectedIds - idStr
                                                    } else {
                                                        pickerSelectedIds + idStr
                                                    }
                                                }
                                                .padding(horizontal = 16.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(1.dp)
                                            ) {
                                                Text(
                                                    text = lorebook.name.ifEmpty { stringResource(R.string.lorebooks_page_unnamed) },
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (lorebook.description.isNotBlank()) {
                                                        lorebook.description
                                                    } else {
                                                        stringResource(R.string.lorebooks_page_entries_count, lorebook.entries.size)
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.65f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            HapticSwitch(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    haptics.perform(HapticPattern.Tick)
                                                    pickerSelectedIds = if (checked) {
                                                        pickerSelectedIds + idStr
                                                    } else {
                                                        pickerSelectedIds - idStr
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
        }
    },
            confirmButton = {
                Button(
                    onClick = {
                        when (editorStep) {
                            EditorStep.Persona -> savePersona()
                            EditorStep.LorebookPicker -> {
                                formLorebookIds = pickerSelectedIds
                                editorStep = EditorStep.Persona
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        when (editorStep) {
                            EditorStep.Persona -> showEditor = false
                            EditorStep.LorebookPicker -> editorStep = EditorStep.Persona
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Persona deletion confirmation dialog
    personaToDelete?.let { persona ->
        SettingsDestructiveConfirmDialog(
            visible = true,
            title = "Delete ${persona.name}?",
            message = "This will permanently delete this persona.",
            onDismiss = { personaToDelete = null },
            onConfirm = {
                val id = persona.id
                personaToDelete = null
                deletePersona(id)
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
