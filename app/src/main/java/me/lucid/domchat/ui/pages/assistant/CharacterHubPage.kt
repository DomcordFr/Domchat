package me.lucid.domchat.ui.pages.assistant

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import me.lucid.domchat.Screen
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.utils.truncateCreator
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import coil3.request.crossfade
import androidx.compose.ui.graphics.SolidColor
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.ui.components.chat.CharacterNotesBottomSheet
import me.lucid.domchat.ui.components.chat.formatRoleplayTextWithCdnImages
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.utils.AssistantExportImport
import me.lucid.domchat.utils.navigateToChatPage
import androidx.compose.material.icons.rounded.RecordVoiceOver
import me.lucid.domchat.ui.components.ai.VoiceSelector
import me.lucid.tts.provider.TTSProviderSetting
import org.koin.androidx.compose.koinViewModel
import kotlin.uuid.Uuid
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.HapticPattern

enum class CharacterSortOption {
    NAME, RECENT, CREATOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterHubPage(vm: AssistantVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val navController = LocalNavController.current
    val isDark = LocalDarkMode.current

    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }
    var sortOption by remember { mutableStateOf(CharacterSortOption.RECENT) }
    var inspectingAssistant by remember { mutableStateOf<Assistant?>(null) }
    var showingCreatorNotesFor by remember { mutableStateOf<Assistant?>(null) }
    var characterToDelete by remember { mutableStateOf<Assistant?>(null) }
    var showExportDialogFor by remember { mutableStateOf<Assistant?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current

    var pendingExportJson by remember { mutableStateOf("") }
    var pendingExportBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Export file launcher (JSON)
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && pendingExportJson.isNotEmpty()) {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { 
                        it.write(pendingExportJson.toByteArray())
                    }
                    toaster.show("Character Card exported successfully")
                } catch (e: Exception) {
                    toaster.show("Export failed: ${e.message}", ToastType.Error)
                }
                pendingExportJson = ""
            }
        }
    }

    // Export file launcher (PNG)
    val exportPngLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri != null && pendingExportBytes != null) {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { 
                        it.write(pendingExportBytes!!)
                    }
                    toaster.show("SillyTavern PNG Card exported successfully")
                } catch (e: Exception) {
                    toaster.show("Export failed: ${e.message}", ToastType.Error)
                }
                pendingExportBytes = null
            }
        }
    }

    // Import file launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        AssistantExportImport.parseImport(uri, context)
                    }
                    when (result) {
                        is AssistantExportImport.ImportResult.Success -> {
                            vm.addAssistant(result.assistant)
                            toaster.show("Imported ${result.assistant.name}")
                        }
                        is AssistantExportImport.ImportResult.Configurable -> {
                            val finalAssistant = result.exportV1?.let {
                                AssistantExportImport.finalizeLastChatImport(
                                    export = it,
                                    context = context,
                                    importMemories = result.hasMemories,
                                    importLorebooks = result.hasLorebooks
                                )
                            } ?: result.assistant
                            vm.addAssistant(finalAssistant)
                            toaster.show("Imported ${finalAssistant.name}")
                        }
                        is AssistantExportImport.ImportResult.Error -> {
                            toaster.show(result.message, ToastType.Error)
                        }
                    }
                } catch (e: Exception) {
                    toaster.show("Import failed: ${e.message}", ToastType.Error)
                }
            }
        }
    }

    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    // 250ms debouncer prevents main-thread keyboard freeze on hundreds of characters
    LaunchedEffect(Unit) {
        snapshotFlow { searchQuery }
            .debounce(250)
            .distinctUntilChanged()
            .collect { debouncedQuery = it.trim().lowercase() }
    }

    var filteredAssistants by remember { mutableStateOf(settings.assistants) }

    // Fast indexed background search: searches name, creator without scanning multi-KB prompt blobs
    LaunchedEffect(settings.assistants, debouncedQuery, sortOption) {
        withContext(Dispatchers.Default) {
            val base = if (debouncedQuery.isEmpty()) {
                settings.assistants
            } else {
                settings.assistants.filter { char ->
                    char.name.lowercase().contains(debouncedQuery) ||
                    (char.creator?.lowercase()?.contains(debouncedQuery) == true)
                }
            }
            val sorted = when (sortOption) {
                CharacterSortOption.NAME -> base.sortedBy { it.name.lowercase() }
                CharacterSortOption.CREATOR -> base.sortedBy { it.creator?.lowercase() ?: "" }
                CharacterSortOption.RECENT -> base
            }
            val distinctSorted = sorted.distinctBy { it.id }
            withContext(Dispatchers.Main) {
                filteredAssistants = distinctSorted
            }
        }
    }

    Scaffold(
        containerColor = if (isDark) Color(0xFF000000) else Color(0xFFF7F7F7),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val newChar = Assistant(name = "New Character")
                    vm.addAssistant(newChar)
                    navController.navigate(Screen.AssistantDetail(id = newChar.id.toString()))
                },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Character")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton()
                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Characters",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
                    )
                    Text(
                        text = "${filteredAssistants.size} characters loaded",
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }

                // Import Character button
                IconButton(onClick = {
                    importLauncher.launch(arrayOf("application/json", "image/png", "image/webp", "*/*"))
                }) {
                    Icon(
                        imageVector = Icons.Rounded.FileUpload,
                        contentDescription = "Import Character Card",
                        tint = if (isDark) Color.White else Color.Black
                    )
                }

                // Grid/List toggle
                IconButton(onClick = { isGridView = !isGridView }) {
                    Icon(
                        imageVector = if (isGridView) Icons.Rounded.ViewList else Icons.Rounded.GridView,
                        contentDescription = "Toggle View",
                        tint = if (isDark) Color.White else Color.Black
                    )
                }

                // Sort toggle
                IconButton(onClick = {
                    val next = when (sortOption) {
                        CharacterSortOption.RECENT -> CharacterSortOption.NAME
                        CharacterSortOption.NAME -> CharacterSortOption.CREATOR
                        CharacterSortOption.CREATOR -> CharacterSortOption.RECENT
                    }
                    sortOption = next
                    val label = when (next) {
                        CharacterSortOption.RECENT -> "Recent"
                        CharacterSortOption.NAME -> "Name (A-Z)"
                        CharacterSortOption.CREATOR -> "Creator"
                    }
                    toaster.show("Sorted by: $label")
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Sort,
                        contentDescription = "Sort",
                        tint = if (isDark) Color.White else Color.Black
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                placeholder = { Text("Search by character or creator...", fontSize = 13.5.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF888888)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = Color(0xFF888888))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isDark) Color(0xFF0D0D0D) else Color.White,
                    unfocusedContainerColor = if (isDark) Color(0xFF080808) else Color(0xFFEFEFEF),
                    focusedBorderColor = if (isDark) Color.White.copy(0.3f) else Color.Black.copy(0.3f),
                    unfocusedBorderColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE0E0E0)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Character Gallery Content
            AnimatedContent(
                targetState = isGridView,
                transitionSpec = { fadeIn(spring()) togetherWith fadeOut(spring()) },
                label = "CharacterGallery"
            ) { gridMode ->
                if (gridMode) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredAssistants, key = { it.id.toString() }) { char ->
                            CharacterPortraitCard(
                                assistant = char,
                                isDark = isDark,
                                onClick = { inspectingAssistant = char }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredAssistants, key = { it.id.toString() }) { char ->
                            CharacterListRow(
                                assistant = char,
                                isDark = isDark,
                                onClick = { inspectingAssistant = char }
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick Character Profile Sheet
    inspectingAssistant?.let { char ->
        CharacterInspectorSheet(
            assistant = char,
            isDark = isDark,
            ttsProviders = settings.ttsProviders,
            onUpdateVoice = { newVoiceId ->
                val updated = char.copy(ttsVoiceId = newVoiceId)
                vm.updateAssistant(updated)
                inspectingAssistant = updated
            },
            onDismiss = { inspectingAssistant = null },
            onStartChat = {
                inspectingAssistant = null
                scope.launch {
                    vm.updateSettings(settings.copy(assistantId = char.id))
                    val conversation = withContext(Dispatchers.IO) {
                        vm.createConversation(char.id)
                    }
                    navigateToChatPage(navController, conversation.id)
                }
            },
            onEdit = {
                inspectingAssistant = null
                navController.navigate(Screen.AssistantDetail(id = char.id.toString()))
            },
            onViewCreatorNotes = {
                showingCreatorNotesFor = char
            },
            onDuplicate = {
                val cloned = char.copy(
                    id = Uuid.random(),
                    name = "${char.name} (Copy)"
                )
                vm.addAssistant(cloned)
                inspectingAssistant = null
                toaster.show("Cloned ${char.name}")
            },
            onExport = {
                showExportDialogFor = char
            },
            onDelete = {
                characterToDelete = char
            }
        )
    }

    // Delete confirmation dialog
    characterToDelete?.let { char ->
        AlertDialog(
            onDismissRequest = { characterToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isDark) Color(0xFF141418) else Color.White,
            title = {
                Text(
                    text = "Delete ${char.name}?",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
            },
            text = {
                Text(
                    text = "This will permanently remove this character from your library. Associated chat sessions will be preserved in your history.",
                    fontSize = 13.5.sp,
                    color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF555555)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = char
                        characterToDelete = null
                        if (inspectingAssistant?.id == toDelete.id) {
                            inspectingAssistant = null
                        }
                        vm.removeAssistant(toDelete)
                        toaster.show(
                            message = "Deleted ${toDelete.name}",
                            action = me.lucid.domchat.ui.components.ui.ToastAction(
                                label = "Undo",
                                onClick = { vm.undoRemoveAssistant(toDelete) }
                            )
                        )
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { characterToDelete = null }) {
                    Text("Cancel", color = if (isDark) Color.White else Color.Black)
                }
            }
        )
    }

    // Export options dialog
    showExportDialogFor?.let { char ->
        AlertDialog(
            onDismissRequest = { showExportDialogFor = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isDark) Color(0xFF141418) else Color.White,
            title = {
                Text(
                    text = "Export ${char.name}",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val target = char
                            showExportDialogFor = null
                            scope.launch {
                                val bytes = withContext(Dispatchers.IO) {
                                    AssistantExportImport.exportToCharacterCardPng(target, context)
                                }
                                if (bytes != null) {
                                    pendingExportBytes = bytes
                                    exportPngLauncher.launch("${target.name.replace(" ", "_")}.png")
                                } else {
                                    toaster.show("Failed to create PNG card with avatar", ToastType.Error)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF22222A) else Color(0xFFF0F0F4),
                            contentColor = if (isDark) Color.White else Color.Black
                        )
                    ) {
                        Text("SillyTavern Character Card (PNG)")
                    }

                    Button(
                        onClick = {
                            val target = char
                            showExportDialogFor = null
                            scope.launch {
                                val jsonStr = withContext(Dispatchers.IO) {
                                    AssistantExportImport.exportToCharacterCardV2(target, context)
                                }
                                pendingExportJson = jsonStr
                                exportJsonLauncher.launch("${target.name.replace(" ", "_")}.json")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF22222A) else Color(0xFFF0F0F4),
                            contentColor = if (isDark) Color.White else Color.Black
                        )
                    ) {
                        Text("Character Card V2 (JSON)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportDialogFor = null }) {
                    Text("Close", color = if (isDark) Color.White else Color.Black)
                }
            }
        )
    }

    // Creator Notes Modal
    showingCreatorNotesFor?.let { char ->
        CharacterNotesBottomSheet(
            characterName = char.name,
            creatorName = char.creator,
            characterVersion = char.characterVersion,
            notes = char.creatorNotes.orEmpty(),
            onDismissRequest = { showingCreatorNotesFor = null }
        )
    }
}

/**
 * 3:4 Portrait Character Card — optimized for anime/RP card art with liquid glass border.
 */
@Composable
fun FullCharacterCardImage(
    name: String,
    avatar: Avatar,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (avatar) {
            is Avatar.Image -> {
                val context = LocalContext.current
                val request = remember<coil3.request.ImageRequest>(avatar.url) {
                    coil3.request.ImageRequest.Builder(context)
                        .data(avatar.url)
                        .size(360, 520)
                        .crossfade(true)
                        .build()
                }
                AsyncImage(
                    model = request,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.TopCenter,
                    contentScale = ContentScale.Crop
                )
            }
            is Avatar.Resource -> {
                AsyncImage(
                    model = avatar.id,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.TopCenter,
                    contentScale = ContentScale.Crop
                )
            }
            is Avatar.Emoji -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF18181B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = avatar.content, fontSize = 48.sp)
                }
            }
            is Avatar.Dummy -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Color(0xFF2C2C2E), Color(0xFF121214)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase().ifBlank { "?" },
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Tall Vertical Portrait Card — full-card character image with bottom gradient overlay.
 * Centered character name (handles long names), creator byline, and greeting count badge.
 */
@Composable
fun CharacterPortraitCard(
    assistant: Assistant,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) Color(0xFF0C0C0E) else Color(0xFF1A1A1E))
            .border(
                0.5.dp,
                if (isDark) LiquidGlassTokens.SpecularBorderSubtle
                else Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
    ) {
        // Full-card image
        FullCharacterCardImage(
            name = assistant.name,
            avatar = assistant.avatar,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient overlay with centered content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.98f)
                        )
                    )
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = assistant.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            if (!assistant.creator.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "by ${assistant.creator.truncateCreator(10)}",
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFCCCCCC)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val introsCount = assistant.alternateGreetings.size + 1
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$introsCount ${if (introsCount == 1) "greeting" else "greetings"}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}

@Composable
fun CharacterListRow(
    assistant: Assistant,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) Color(0xFF0C0C0E) else Color.White)
            .border(
                width = 0.5.dp,
                brush = if (isDark) LiquidGlassTokens.SpecularBorderSubtle else SolidColor(Color(0xFFE5E5E5)),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            FullCharacterCardImage(
                name = assistant.name,
                avatar = assistant.avatar,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = assistant.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isDark) Color.White else Color.Black
            )
            if (!assistant.creator.isNullOrBlank()) {
                Text(
                    text = "by ${assistant.creator.truncateCreator(10)}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
        }
        val introsCount = assistant.alternateGreetings.size + 1
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDark) Color.White.copy(0.08f) else Color.Black.copy(0.05f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "$introsCount greeting${if (introsCount > 1) "s" else ""}",
                fontSize = 11.sp,
                color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterInspectorSheet(
    assistant: Assistant,
    isDark: Boolean,
    ttsProviders: List<TTSProviderSetting> = emptyList(),
    onUpdateVoice: ((Uuid?) -> Unit)? = null,
    onDismiss: () -> Unit,
    onStartChat: () -> Unit,
    onEdit: () -> Unit,
    onViewCreatorNotes: () -> Unit,
    onDuplicate: () -> Unit = {},
    onExport: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val sheetBgColor = if (isDark) Color(0xFF07080C) else Color(0xFFF7F7FA)
    val contentTextColor = if (isDark) Color(0xFFF2F2F7) else Color(0xFF14151C)
    val cardBgColor = if (isDark) Color(0xFF0F1018) else Color.White
    val cardBorderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val haptics = rememberPremiumHaptics()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = true,
        containerColor = sheetBgColor,
        contentColor = contentTextColor,
        dragHandle = null,
        shape = sheetShape,
        modifier = Modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Image with smooth multi-stop cubic vertical gradient fade into sheet background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    FullCharacterCardImage(
                        name = assistant.name,
                        avatar = assistant.avatar,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        sheetBgColor.copy(alpha = 0.15f),
                                        sheetBgColor.copy(alpha = 0.50f),
                                        sheetBgColor.copy(alpha = 0.85f),
                                        sheetBgColor
                                    )
                                )
                            )
                    )
                }

                // Content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    // Character Title and Creator
                    Text(
                        text = assistant.name,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentTextColor
                    )
                    if (!assistant.creator.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Created by ${assistant.creator.truncateCreator(10)}",
                            fontSize = 13.5.sp,
                            color = if (isDark) Color(0xFFA0A0A8) else Color(0xFF6B6B75)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val introsCount = assistant.alternateGreetings.size + 1
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f))
                                .border(0.5.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$introsCount scenario greeting${if (introsCount > 1) "s" else ""}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = contentTextColor.copy(alpha = 0.9f)
                            )
                        }
                        if (!assistant.characterVersion.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f))
                                    .border(0.5.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "v${assistant.characterVersion}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color(0xFFBBBBBB) else Color(0xFF666666)
                                )
                            }
                        }
                    }

                    // Top few lines of creator notes as a blurb
                    val notesBlurb = remember(assistant.creatorNotes) {
                        assistant.creatorNotes?.lineSequence()
                            ?.map { it.trim() }
                            ?.filter { it.isNotBlank() }
                            ?.take(3)
                            ?.joinToString("\n")
                            .orEmpty()
                    }
                    if (notesBlurb.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = notesBlurb,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = if (isDark) Color(0xFFC0C0C8) else Color(0xFF4A4A52),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action Buttons: Start Chat & Edit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                onStartChat()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color.White else Color(0xFF14151C),
                                contentColor = if (isDark) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(19.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Chat", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.5.dp, cardBorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = contentTextColor
                            )
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Character Management Buttons (Export, Clone, Delete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExport,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, cardBorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentTextColor)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export", fontSize = 12.5.sp)
                        }

                        OutlinedButton(
                            onClick = onDuplicate,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, cardBorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentTextColor)
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Clone", fontSize = 12.5.sp)
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        }
                    }

                    // Character Voice (TTS) Quick Selector
                    if (ttsProviders.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBgColor)
                                .border(0.5.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Character Voice",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = contentTextColor
                                        )
                                        Text(
                                            text = "TTS voice binding for audio",
                                            fontSize = 11.sp,
                                            color = if (isDark) Color(0xFF888888) else Color(0xFF777777)
                                        )
                                    }
                                }
                                VoiceSelector(
                                    voiceId = assistant.ttsVoiceId,
                                    providers = ttsProviders,
                                    allowClear = true,
                                    onClear = { onUpdateVoice?.invoke(null) },
                                    onSelect = { onUpdateVoice?.invoke(it.id) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3 TABS: 0: Notes, 1: Personality, 2: Greetings
                    var selectedTab by remember { mutableIntStateOf(0) }
                    val tabTitles = listOf("Notes", "Personality", "Greetings")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBgColor)
                            .border(0.5.dp, cardBorderColor, RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.08f)
                                        } else Color.Transparent
                                    )
                                    .clickable { selectedTab = index }
                                    .padding(horizontal = 4.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) contentTextColor else contentTextColor.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            val isForward = targetState > initialState
                            (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { width -> if (isForward) width / 4 else -width / 4 }) togetherWith
                                (fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { width -> if (isForward) -width / 4 else width / 4 })
                        },
                        label = "CharacterHubTabsContent"
                    ) { tab ->
                        when (tab) {
                            0 -> {
                                // Creator Notes Tab
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(cardBgColor)
                                        .border(0.5.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    val notes = assistant.creatorNotes?.trim().orEmpty()
                                    if (notes.isNotBlank()) {
                                        MarkdownBlock(
                                            content = formatRoleplayTextWithCdnImages(notes)
                                        )
                                    } else {
                                        Text(
                                            text = "No creator notes provided for this character.",
                                            fontSize = 13.sp,
                                            color = if (isDark) Color(0xFF888888) else Color(0xFF777777)
                                        )
                                    }
                                }
                            }
                            1 -> {
                                // Personality Tab
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val promptExcerpt = assistant.systemPrompt.trim()
                                    if (promptExcerpt.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(cardBgColor)
                                                .border(0.5.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                                .padding(16.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Character Definition & System Prompt",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFA78BFA)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                MarkdownBlock(
                                                    content = formatRoleplayTextWithCdnImages(promptExcerpt)
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No system prompt or definition configured for this character.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // Opening Greetings Tab with animated carousel
                                val allGreetings = remember(assistant.firstMessage, assistant.alternateGreetings) {
                                    val list = mutableListOf<String>()
                                    if (assistant.firstMessage.isNotBlank()) {
                                        list.add(assistant.firstMessage)
                                    }
                                    list.addAll(assistant.alternateGreetings)
                                    list
                                }

                                if (allGreetings.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(cardBgColor)
                                            .border(0.5.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "No scenario opening greeting set. Starting a chat will open into a freeform conversation.",
                                            fontSize = 12.5.sp,
                                            color = if (isDark) Color(0xFF888888) else Color(0xFF777777)
                                        )
                                    }
                                } else {
                                    var activeGreetingIndex by remember { mutableIntStateOf(0) }

                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (allGreetings.size > 1) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isDark) Color(0xFF141520) else Color(0xFFEBEBF0))
                                                    .border(0.5.dp, cardBorderColor, RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                LazyRow(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    items(allGreetings.size) { idx ->
                                                        val isGreetingSelected = activeGreetingIndex == idx
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(
                                                                    if (isGreetingSelected) {
                                                                        Color(0xFFA78BFA).copy(alpha = 0.35f)
                                                                    } else {
                                                                        if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f)
                                                                    }
                                                                )
                                                                .border(
                                                                    0.5.dp,
                                                                    if (isGreetingSelected) Color(0xFFA78BFA) else cardBorderColor,
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .clickable { activeGreetingIndex = idx }
                                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = if (idx == 0) "Intro #1 (Default)" else "Greeting #${idx + 1}",
                                                                fontSize = 12.sp,
                                                                fontWeight = if (isGreetingSelected) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isGreetingSelected) (if (isDark) Color.White else Color(0xFF5B21B6)) else contentTextColor.copy(alpha = 0.75f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        val safeIndex = activeGreetingIndex.coerceIn(0, allGreetings.lastIndex)
                                        AnimatedContent(
                                            targetState = safeIndex,
                                            transitionSpec = {
                                                val direction = if (targetState > initialState) 1 else -1
                                                (slideInHorizontally(animationSpec = tween(240)) { width -> width / 3 * direction } + fadeIn(animationSpec = tween(240))) togetherWith
                                                    (slideOutHorizontally(animationSpec = tween(200)) { width -> -width / 3 * direction } + fadeOut(animationSpec = tween(200)))
                                            },
                                            label = "greetings_carousel"
                                        ) { currentIdx ->
                                            val greetingText = allGreetings.getOrNull(currentIdx).orEmpty()
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(cardBgColor)
                                                    .border(0.5.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                                    .padding(16.dp)
                                            ) {
                                                MarkdownBlock(
                                                    content = formatRoleplayTextWithCdnImages(greetingText)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }

            // Close button floating at top-right with safe margin
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(top = 16.dp, end = 16.dp)
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.50f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
