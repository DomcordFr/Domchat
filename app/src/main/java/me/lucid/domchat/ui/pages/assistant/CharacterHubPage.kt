package me.lucid.domchat.ui.pages.assistant

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.darkColorScheme
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.modifier.fadeEdges
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.Download
import me.lucid.domchat.ui.theme.liquidGlassSurface
import androidx.core.net.toUri
import me.lucid.domchat.utils.saveToDownloads
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Folder
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import me.lucid.domchat.ui.components.chat.LocalSheetDismiss
import me.lucid.domchat.ui.components.ui.ItemPosition
import me.lucid.domchat.ui.components.ui.PhysicsSwipeToDelete
import me.lucid.domchat.ui.components.ui.DomChatDestructiveConfirmDialog
import me.lucid.domchat.ui.components.dialog.SettingsDestructiveConfirmDialog
import me.lucid.domchat.ui.components.dialog.CharacterExportDialog
import me.lucid.domchat.ui.component.backdrop.LocalAppBackdrop
import me.lucid.domchat.ui.component.backdrop.backdrops.rememberLayerBackdrop
import me.lucid.domchat.ui.component.backdrop.backdrops.layerBackdrop
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lucid.domchat.Screen
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.Lorebook
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.utils.truncateCreator
import me.lucid.domchat.utils.JsonInstant
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
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
import coil3.request.ImageRequest
import me.lucid.domchat.utils.safeDrawableResId
import kotlinx.coroutines.launch
import kotlin.text.Regex
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.ui.components.chat.CharacterNotesBottomSheet
import me.lucid.domchat.ui.components.chat.formatRoleplayTextWithCdnImages
import me.lucid.domchat.ui.components.chat.cleanHtmlImgToMarkdown
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.components.richtext.SimpleHtmlBlock
import me.lucid.domchat.ui.components.ui.AppToasterState
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.components.ui.Tag
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.modifier.domChatDialogContainerColor
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.data.ai.models.ModelCatalogService
import me.lucid.domchat.data.ai.models.ModelCatalogSnapshot
import org.koin.compose.koinInject
import me.lucid.domchat.utils.AssistantExportImport
import me.lucid.domchat.utils.navigateToChatPage
import androidx.compose.material.icons.rounded.RecordVoiceOver
import me.lucid.domchat.ui.components.ai.VoiceSelector
import me.lucid.tts.provider.TTSProviderSetting
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import kotlin.uuid.Uuid
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.HapticPattern

private val HTML_TAG_REGEX = Regex("<[^>]*>")
private val WHITESPACE_REGEX = Regex("\\s+")
private val IMG_TAG_REGEX = Regex("""<img[^>]+src=['"]([^'"]+)['"][^>]*>""", RegexOption.IGNORE_CASE)
private val DECIMAL_ENTITY_REGEX = Regex("&#(\\d+);")
private val HEX_ENTITY_REGEX = Regex("&#x([0-9a-fA-F]+);")
private val RAW_IMAGE_URL_REGEX = Regex("""https?://[^\s<>"]+\.(?:png|jpg|jpeg|webp|gif)""", RegexOption.IGNORE_CASE)
private val HORIZONTAL_WHITESPACE_REGEX = Regex("[ \\t]+")
private val MARKDOWN_IMAGE_REGEX = Regex("!\\[([^\\]]*)\\]\\(([^)]+)\\)")
private val HTML_IMG_SRC_REGEX = Regex("<img[^>]+src=['\"]([^'\"]+)['\"]", RegexOption.IGNORE_CASE)
private val BR_TAG_REGEX = Regex("(?i)<br\\s*/?>")
private val BLOCK_TAG_REGEX = Regex("(?i)</?(p|div|h[1-6]|blockquote|li)[^>]*>")
private val MULTI_NEWLINE_REGEX = Regex("\n{3,}")

enum class CharacterSortOption {
    NAME, RECENT, CREATOR, NEWEST, OLDEST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterHubPage(vm: AssistantVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val modelCatalogService: ModelCatalogService = koinInject()
    val catalogSnapshot by modelCatalogService.snapshotFlow.collectAsStateWithLifecycle()
    val navController = LocalNavController.current
    val isDark = LocalDarkMode.current

    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }
    var sortOption by remember { mutableStateOf(CharacterSortOption.RECENT) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    val sortArrowRotation by animateFloatAsState(
        targetValue = if (sortMenuOpen) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 400f
        ),
        label = "sort_arrow_rotation"
    )
    var filterMenuOpen by remember { mutableStateOf(false) }
    var selectedTagIds by remember { mutableStateOf(emptySet<String>()) }
    var inspectingAssistant by remember { mutableStateOf<Assistant?>(null) }
    var showingCreatorNotesFor by remember { mutableStateOf<Assistant?>(null) }
    var characterToDelete by remember { mutableStateOf<Assistant?>(null) }
    val hubBackdrop = rememberLayerBackdrop()
    var showExportDialogFor by remember { mutableStateOf<Assistant?>(null) }

    // Group / folder state
    var selectedGroupId by remember { mutableStateOf<String?>(null) } // null = "All"
    var groupNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var groupOrder by remember { mutableStateOf<List<String>>(emptyList()) }
    var charGroupAssignments by remember { mutableStateOf<Map<String, String>>(emptyMap()) } // characterId -> groupId
    var newGroupName by remember { mutableStateOf("") }
    var showNewGroupDialog by remember { mutableStateOf(false) }
    var characterToMove by remember { mutableStateOf<Assistant?>(null) }
    var showMoveToGroupDialog by remember { mutableStateOf(false) }
    var showReorderSheet by remember { mutableStateOf(false) }
    var groupSearchQuery by remember { mutableStateOf("") }
    var isGroupTabsInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(120)
        isGroupTabsInitialized = true
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toaster: AppToasterState? = LocalToaster.current
    val haptics = rememberPremiumHaptics()

    /**
     * Persists group names and character-to-group assignments to SharedPreferences
     * ('character_hub_groups') on the IO dispatcher.
     */
    fun persistGroups() {
        val groupsSnapshot = groupNames
        val orderSnapshot = groupOrder
        val assignmentsSnapshot = charGroupAssignments
        scope.launch {
            vm.saveGroups(context, groupsSnapshot, orderSnapshot, assignmentsSnapshot)
        }
    }

    fun moveGroup(groupId: String, newIndex: Int) {
        val ordered = (groupOrder.filter { it in groupNames } + groupNames.keys.filter { it !in groupOrder }).toMutableList()
        val currentIdx = ordered.indexOf(groupId)
        if (currentIdx == -1) return
        val moved = ordered.removeAt(currentIdx)
        ordered.add(newIndex.coerceIn(0, ordered.size), moved)
        groupOrder = ordered
        persistGroups()
    }

    var pendingExportAssistantId by rememberSaveable { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf<Throwable?>(null) }

    // Export file launcher (JSON)
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val assistantId = pendingExportAssistantId
        if (uri == null) {
            pendingExportAssistantId = null
            return@rememberLauncherForActivityResult
        }
        if (assistantId != null) {
            scope.launch {
                try {
                    val currentSettings = vm.settings.first { !it.init }
                    val target = currentSettings.assistants.find { it.id.toString() == assistantId }
                    if (target != null) {
                        val result = AssistantExportHelper.exportJson(context, target, uri)
                        if (result.isSuccess) {
                            toaster?.show("Character Card exported successfully")
                        } else {
                            toaster?.show("Export failed: ${result.exceptionOrNull()?.message}", ToastType.Error)
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    toaster?.show("Export failed: ${e.message}", ToastType.Error)
                } finally {
                    pendingExportAssistantId = null
                }
            }
        }
    }

    // Export file launcher (PNG)
    val exportPngLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        val assistantId = pendingExportAssistantId
        if (uri == null) {
            pendingExportAssistantId = null
            return@rememberLauncherForActivityResult
        }
        if (assistantId != null) {
            scope.launch {
                try {
                    val currentSettings = vm.settings.first { !it.init }
                    val target = currentSettings.assistants.find { it.id.toString() == assistantId }
                    if (target != null) {
                        val result = AssistantExportHelper.exportPng(context, target, uri)
                        if (result.isSuccess && result.getOrDefault(false)) {
                            toaster?.show("SillyTavern PNG Card exported successfully")
                        } else {
                            val msg = result.exceptionOrNull()?.message ?: "could not generate PNG card"
                            toaster?.show("Export failed: $msg", ToastType.Error)
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    toaster?.show("Export failed: ${e.message}", ToastType.Error)
                } finally {
                    pendingExportAssistantId = null
                }
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
                            toaster?.show("Imported ${result.assistant.name}")
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
                            toaster?.show("Imported ${finalAssistant.name}")
                        }
                        is AssistantExportImport.ImportResult.Error -> {
                            toaster?.show(result.message, ToastType.Error)
                        }
                    }
                } catch (e: Exception) {
                    toaster?.show("Import failed: ${e.message}", ToastType.Error)
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

    // Restore persisted groups + character-to-group assignments from SharedPreferences
    LaunchedEffect(Unit) {
        scope.launch {
            val groups = vm.loadGroupNames(context)
            val order = vm.loadGroupOrder(context)
            val assignments = vm.loadCharAssignments(context)
            val mergedOrder = (order.filter { it in groups } + groups.keys.filter { it !in order }).distinct()
            withContext(Dispatchers.Main) {
                groupNames = groups
                groupOrder = mergedOrder
                charGroupAssignments = assignments
            }
        }
    }

    val visibleAssistants = remember(settings.assistants) {
        settings.assistants.filter { !it.isDeleted }
    }
    var baseFilteredAssistants by remember { mutableStateOf(visibleAssistants) }

    // Fast indexed background search: searches name, creator without scanning multi-KB prompt blobs
    LaunchedEffect(
        visibleAssistants,
        debouncedQuery,
        sortOption,
        selectedTagIds
    ) {
        try {
            withContext(Dispatchers.Default) {
                val base = if (debouncedQuery.isEmpty()) {
                    visibleAssistants
                } else {
                    visibleAssistants.filter { char ->
                        char.name.lowercase().contains(debouncedQuery) ||
                            (char.creator?.lowercase()?.contains(debouncedQuery) == true)
                    }
                }
                val tagFiltered = if (selectedTagIds.isEmpty()) {
                    base
                } else {
                    base.filter { char ->
                        val charTagIds = char.tags.map { it.toString() }.toSet()
                        selectedTagIds.all { it in charTagIds }
                    }
                }
                val sorted = when (sortOption) {
                    CharacterSortOption.NAME -> tagFiltered.sortedBy { it.name.lowercase() }
                    CharacterSortOption.CREATOR -> tagFiltered.sortedBy { it.creator?.lowercase() ?: "" }
                    CharacterSortOption.NEWEST -> tagFiltered.asReversed()
                    CharacterSortOption.OLDEST -> tagFiltered
                    CharacterSortOption.RECENT -> {
                        val activityMap = runCatching { vm.getAllAssistantsLastUpdateAt() }.getOrDefault(emptyMap())
                        tagFiltered.sortedByDescending { activityMap[it.id] ?: Instant.MIN }
                    }
                }
                val distinctSorted = sorted.distinctBy { it.id }
                withContext(Dispatchers.Main) {
                    loadError = null
                    baseFilteredAssistants = distinctSorted
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                loadError = e
                baseFilteredAssistants = emptyList()
            }
        }
    }

    // Group filter applied synchronously so cards update on the exact frame the group tab switches
    val filteredAssistants = remember(baseFilteredAssistants, selectedGroupId, charGroupAssignments) {
        if (selectedGroupId == null) {
            baseFilteredAssistants
        } else {
            baseFilteredAssistants.filter { char ->
                charGroupAssignments[char.id.toString()] == selectedGroupId
            }
        }
    }

    val collapseBehavior = androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        containerColor = Color(0xFF000000),
        topBar = {
            me.lucid.domchat.ui.components.nav.OneUITopAppBar(
                title = "Characters",
                scrollBehavior = collapseBehavior,
                navigationIcon = { BackButton() }
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF000000))
                    .border(1.dp, LiquidGlassTokens.SpecularBorderBrush, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        scope.launch {
                            val newChar = Assistant(name = "New Character")
                            vm.addAssistant(newChar)
                            navController.navigate(Screen.AssistantDetail(id = newChar.id.toString()))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Character",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { padding ->
        var previousGroupId by remember { mutableStateOf<String?>(null) }
        var hasSeenInitialGroup by remember { mutableStateOf(false) }
        val groupCardsAlpha = remember { Animatable(1f) }
        val groupCardsOffsetX = remember { Animatable(0f) }

        LaunchedEffect(selectedGroupId) {
            if (!hasSeenInitialGroup) {
                hasSeenInitialGroup = true
                previousGroupId = selectedGroupId
                return@LaunchedEffect
            }
            val allOrdered = listOf<String?>(null) + (groupOrder.filter { it in groupNames } + groupNames.keys.filter { it !in groupOrder })
            val prevIdx = allOrdered.indexOf(previousGroupId).let { if (it == -1) 0 else it }
            val nextIdx = allOrdered.indexOf(selectedGroupId).let { if (it == -1) 0 else it }
            previousGroupId = selectedGroupId
            val dir = if (nextIdx >= prevIdx) 1f else -1f
            groupCardsOffsetX.snapTo(dir * 48f)
            groupCardsAlpha.snapTo(0f)
            launch {
                groupCardsOffsetX.animateTo(0f, spring(dampingRatio = 0.86f, stiffness = 420f))
            }
            launch {
                groupCardsAlpha.animateTo(1f, tween(180, easing = LinearOutSlowInEasing))
            }
        }

        AnimatedContent(
            targetState = isGridView,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)) { -it / 3 } + fadeIn(animationSpec = tween(200)))
                        .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)) { it / 3 } + fadeOut(animationSpec = tween(160)))
                } else {
                    (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)) { it / 3 } + fadeIn(animationSpec = tween(200)))
                        .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)) { -it / 3 } + fadeOut(animationSpec = tween(160)))
                }
            },
            contentAlignment = Alignment.TopCenter,
            label = "CharacterGallery",
            modifier = Modifier.fillMaxSize().padding(padding).layerBackdrop(hubBackdrop)
        ) { targetGrid ->
            if (targetGrid) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(collapseBehavior.nestedScrollConnection)
                ) {
                    // Header items: scroll away with content (only OneUITopAppBar stays sticky)
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(me.lucid.domchat.ui.theme.AppShapes.SearchField)
                                    .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SearchField),
                                placeholder = { Text("Search by character or creator...", fontSize = 13.5.sp) },
                                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF8A8A96)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = Color(0xFF8A8A96))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = me.lucid.domchat.ui.theme.AppShapes.SearchField,
                                colors = me.lucid.domchat.ui.theme.appFieldColors()
                            )
                        }
                    }

                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF000000))
                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${filteredAssistants.size} Characters",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Sort dropdown pill
                                Box {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFF000000))
                                            .border(
                                                0.5.dp,
                                                LiquidGlassTokens.SpecularBorderSubtle,
                                                RoundedCornerShape(20.dp)
                                            ),
                                        color = Color.Transparent,
                                        onClick = { sortMenuOpen = true },
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Sort,
                                                contentDescription = "Sort",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = when (sortOption) {
                                                    CharacterSortOption.RECENT -> "Recent"
                                                    CharacterSortOption.NAME -> "Name A-Z"
                                                    CharacterSortOption.CREATOR -> "Creator"
                                                    CharacterSortOption.NEWEST -> "Newest"
                                                    CharacterSortOption.OLDEST -> "Oldest"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp).rotate(sortArrowRotation)
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = sortMenuOpen,
                                        onDismissRequest = { sortMenuOpen = false },
                                        shape = RoundedCornerShape(20.dp),
                                        containerColor = Color(0xFF000000),
                                        modifier = Modifier
                                            .background(Color(0xFF000000))
                                            .border(
                                                0.5.dp,
                                                LiquidGlassTokens.SpecularBorderSubtle,
                                                RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Recent",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (sortOption == CharacterSortOption.RECENT) Color.White else Color(0xFFCCCCCC)
                                                )
                                            },
                                            onClick = {
                                                haptics.perform(HapticPattern.Pop)
                                                sortOption = CharacterSortOption.RECENT
                                                sortMenuOpen = false
                                            },
                                            trailingIcon = if (sortOption == CharacterSortOption.RECENT) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Name A-Z",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (sortOption == CharacterSortOption.NAME) Color.White else Color(0xFFCCCCCC)
                                                )
                                            },
                                            onClick = {
                                                haptics.perform(HapticPattern.Pop)
                                                sortOption = CharacterSortOption.NAME
                                                sortMenuOpen = false
                                            },
                                            trailingIcon = if (sortOption == CharacterSortOption.NAME) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Creator",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (sortOption == CharacterSortOption.CREATOR) Color.White else Color(0xFFCCCCCC)
                                                )
                                            },
                                            onClick = {
                                                haptics.perform(HapticPattern.Pop)
                                                sortOption = CharacterSortOption.CREATOR
                                                sortMenuOpen = false
                                            },
                                            trailingIcon = if (sortOption == CharacterSortOption.CREATOR) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Newest",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (sortOption == CharacterSortOption.NEWEST) Color.White else Color(0xFFCCCCCC)
                                                )
                                            },
                                            onClick = {
                                                haptics.perform(HapticPattern.Pop)
                                                sortOption = CharacterSortOption.NEWEST
                                                sortMenuOpen = false
                                            },
                                            trailingIcon = if (sortOption == CharacterSortOption.NEWEST) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Oldest",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (sortOption == CharacterSortOption.OLDEST) Color.White else Color(0xFFCCCCCC)
                                                )
                                            },
                                            onClick = {
                                                haptics.perform(HapticPattern.Pop)
                                                sortOption = CharacterSortOption.OLDEST
                                                sortMenuOpen = false
                                            },
                                            trailingIcon = if (sortOption == CharacterSortOption.OLDEST) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                    }
                                }
                                // Filter pill button
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF000000))
                                        .border(
                                            0.5.dp,
                                            LiquidGlassTokens.SpecularBorderSubtle,
                                            RoundedCornerShape(20.dp)
                                        ),
                                    color = Color.Transparent,
                                    onClick = { filterMenuOpen = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FilterAlt,
                                            contentDescription = "Filter",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (selectedTagIds.isEmpty()) "Filter" else "Filter (${selectedTagIds.size})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            UploadCharacterButton(
                                onClick = {
                                    importLauncher.launch(arrayOf("application/json", "image/png", "image/webp", "*/*"))
                                }
                            )

                            Surface(
                                onClick = { isGridView = !isGridView },
                                shape = AppShapes.ButtonPill,
                                color = Color(0xFF000000),
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isGridView) Icons.Rounded.ViewList else Icons.Rounded.GridView,
                                        contentDescription = "Toggle View",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isGridView) "List View" else "Grid View",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Group tabs row
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        GroupTabsRow(
                            selectedGroupId = selectedGroupId,
                            groupNames = groupNames,
                            groupOrder = groupOrder,
                            allCount = visibleAssistants.size,
                            isInitialized = isGroupTabsInitialized,
                            onGroupSelected = { groupId ->
                                if (selectedGroupId != groupId) {
                                    selectedGroupId = groupId
                                }
                            },
                            onNewGroup = {
                                showNewGroupDialog = true
                                newGroupName = ""
                            },
                            onGroupRenamed = { groupId, newName ->
                                groupNames = groupNames + (groupId to newName)
                                persistGroups()
                            },
                            onGroupMoved = ::moveGroup,
                            onGroupDeleted = { groupId ->
                                charGroupAssignments = charGroupAssignments.filterValues { it != groupId }
                                groupNames = groupNames - groupId
                                groupOrder = groupOrder.filter { it != groupId }
                                if (selectedGroupId == groupId) {
                                    selectedGroupId = null
                                }
                                persistGroups()
                                toaster?.show("Group deleted — characters moved to All", ToastType.Info)
                            },
                            onReorderGroups = { newOrder ->
                                groupOrder = newOrder
                                persistGroups()
                            },
                            onOpenReorder = { showReorderSheet = true }
                        )
                    }

                    loadError?.let { error ->
                        item {
                            CharacterHubErrorCard(
                                assistant = Assistant(name = "Character Hub"),
                                error = error
                            )
                        }
                    }

                    items(filteredAssistants, key = { it.id.toString() }) { char ->
                        Box(
                            modifier = Modifier.graphicsLayer {
                                translationX = groupCardsOffsetX.value
                                alpha = groupCardsAlpha.value
                            }
                        ) {
                            CharacterPortraitCard(
                                assistant = char,
                                settings = settings,
                                isDark = isDark,
                                onClick = { inspectingAssistant = char },
                                onLongClick = {
                                    characterToMove = char
                                    showMoveToGroupDialog = true
                                }
                            )
                        }
                    }

                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(collapseBehavior.nestedScrollConnection)
                ) {
                    // Header items: scroll away with content (only OneUITopAppBar stays sticky)
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(me.lucid.domchat.ui.theme.AppShapes.SearchField)
                                    .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SearchField),
                                placeholder = { Text("Search by character or creator...", fontSize = 13.5.sp) },
                                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF8A8A96)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = Color(0xFF8A8A96))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = me.lucid.domchat.ui.theme.AppShapes.SearchField,
                                colors = me.lucid.domchat.ui.theme.appFieldColors()
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF000000))
                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${filteredAssistants.size} Characters",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Sort dropdown pill
                                Box {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFF000000))
                                            .border(
                                                0.5.dp,
                                                LiquidGlassTokens.SpecularBorderSubtle,
                                                RoundedCornerShape(20.dp)
                                            ),
                                        color = Color.Transparent,
                                        onClick = { sortMenuOpen = true },
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Sort,
                                                contentDescription = "Sort",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = when (sortOption) {
                                                    CharacterSortOption.RECENT -> "Recent"
                                                    CharacterSortOption.NAME -> "Name A-Z"
                                                    CharacterSortOption.CREATOR -> "Creator"
                                                    CharacterSortOption.NEWEST -> "Newest"
                                                    CharacterSortOption.OLDEST -> "Oldest"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp).rotate(sortArrowRotation)
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = sortMenuOpen,
                                        onDismissRequest = { sortMenuOpen = false },
                                        shape = RoundedCornerShape(20.dp),
                                        containerColor = Color(0xFF000000),
                                        modifier = Modifier
                                            .background(Color(0xFF000000))
                                            .border(
                                                0.5.dp,
                                                LiquidGlassTokens.SpecularBorderSubtle,
                                                RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Recent",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (sortOption == CharacterSortOption.RECENT) Color.White else Color(0xFFCCCCCC)
                                                )
                                            },
                                            onClick = {
                                                haptics.perform(HapticPattern.Pop)
                                                sortOption = CharacterSortOption.RECENT
                                                sortMenuOpen = false
                                            },
                                            trailingIcon = if (sortOption == CharacterSortOption.RECENT) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Name A-Z",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (sortOption == CharacterSortOption.NAME) Color.White else Color(0xFFCCCCCC)
                                                )
                                            },
                                            onClick = {
                                                haptics.perform(HapticPattern.Pop)
                                                sortOption = CharacterSortOption.NAME
                                                sortMenuOpen = false
                                            },
                                            trailingIcon = if (sortOption == CharacterSortOption.NAME) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Creator",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (sortOption == CharacterSortOption.CREATOR) Color.White else Color(0xFFCCCCCC)
                                                )
                                            },
                                            onClick = {
                                                haptics.perform(HapticPattern.Pop)
                                                sortOption = CharacterSortOption.CREATOR
                                                sortMenuOpen = false
                                            },
                                            trailingIcon = if (sortOption == CharacterSortOption.CREATOR) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Newest",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (sortOption == CharacterSortOption.NEWEST) Color.White else Color(0xFFCCCCCC)
                                                )
                                            },
                                            onClick = {
                                                haptics.perform(HapticPattern.Pop)
                                                sortOption = CharacterSortOption.NEWEST
                                                sortMenuOpen = false
                                            },
                                            trailingIcon = if (sortOption == CharacterSortOption.NEWEST) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Oldest",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (sortOption == CharacterSortOption.OLDEST) Color.White else Color(0xFFCCCCCC)
                                                )
                                            },
                                            onClick = {
                                                haptics.perform(HapticPattern.Pop)
                                                sortOption = CharacterSortOption.OLDEST
                                                sortMenuOpen = false
                                            },
                                            trailingIcon = if (sortOption == CharacterSortOption.OLDEST) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            } else null
                                        )
                                    }
                                }
                                // Filter pill button
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF000000))
                                        .border(
                                            0.5.dp,
                                            LiquidGlassTokens.SpecularBorderSubtle,
                                            RoundedCornerShape(20.dp)
                                        ),
                                    color = Color.Transparent,
                                    onClick = { filterMenuOpen = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FilterAlt,
                                            contentDescription = "Filter",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (selectedTagIds.isEmpty()) "Filter" else "Filter (${selectedTagIds.size})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            UploadCharacterButton(
                                onClick = {
                                    importLauncher.launch(arrayOf("application/json", "image/png", "image/webp", "*/*"))
                                }
                            )

                            Surface(
                                onClick = { isGridView = !isGridView },
                                shape = AppShapes.ButtonPill,
                                color = Color(0xFF000000),
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isGridView) Icons.Rounded.ViewList else Icons.Rounded.GridView,
                                        contentDescription = "Toggle View",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isGridView) "List View" else "Grid View",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Group tabs row
                    item {
                        GroupTabsRow(
                            selectedGroupId = selectedGroupId,
                            groupNames = groupNames,
                            groupOrder = groupOrder,
                            allCount = visibleAssistants.size,
                            isInitialized = isGroupTabsInitialized,
                            onGroupSelected = { groupId ->
                                if (selectedGroupId != groupId) {
                                    selectedGroupId = groupId
                                }
                            },
                            onNewGroup = {
                                showNewGroupDialog = true
                                newGroupName = ""
                            },
                            onGroupRenamed = { groupId, newName ->
                                groupNames = groupNames + (groupId to newName)
                                persistGroups()
                            },
                            onGroupMoved = ::moveGroup,
                            onGroupDeleted = { groupId ->
                                charGroupAssignments = charGroupAssignments.filterValues { it != groupId }
                                groupNames = groupNames - groupId
                                groupOrder = groupOrder.filter { it != groupId }
                                if (selectedGroupId == groupId) {
                                    selectedGroupId = null
                                }
                                persistGroups()
                                toaster?.show("Group deleted — characters moved to All", ToastType.Info)
                            },
                            onReorderGroups = { newOrder ->
                                groupOrder = newOrder
                                persistGroups()
                            },
                            onOpenReorder = { showReorderSheet = true }
                        )
                    }

                    loadError?.let { error ->
                        item {
                            CharacterHubErrorCard(
                                assistant = Assistant(name = "Character Hub"),
                                error = error
                            )
                        }
                    }

                    items(filteredAssistants, key = { it.id.toString() }) { char ->
                        Box(
                            modifier = Modifier.graphicsLayer {
                                translationX = groupCardsOffsetX.value
                                alpha = groupCardsAlpha.value
                            }
                        ) {
                            CharacterListRow(
                                assistant = char,
                                isDark = isDark,
                                onClick = { inspectingAssistant = char },
                                onLongClick = {
                                    characterToMove = char
                                    showMoveToGroupDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Filter Dialog - glassy tag filter sheet
    if (filterMenuOpen) {
        Dialog(onDismissRequest = { filterMenuOpen = false }) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF000000))
                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderBrush, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter by Tags",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { filterMenuOpen = false }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    val allTags = settings.assistantTags
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allTags.forEach { tag ->
                            val isSelected = selectedTagIds.contains(tag.id.toString())
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Transparent)
                                    .border(
                                        if (isSelected) 1.dp else 0.5.dp,
                                        if (isSelected) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        haptics.perform(HapticPattern.Pop)
                                        selectedTagIds = if (isSelected) {
                                            selectedTagIds - tag.id.toString()
                                        } else {
                                            selectedTagIds + tag.id.toString()
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tag.name,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.72f),
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            selectedTagIds = emptySet()
                            filterMenuOpen = false
                        }) {
                            Text("Reset", color = Color(0xFF8A8A96))
                        }
                        TextButton(onClick = { filterMenuOpen = false }) {
                            Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
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
            settings = settings,
            isDark = isDark,
            ttsProviders = settings.ttsProviders,
            catalogSnapshot = catalogSnapshot,
            onUpdateVoice = { newVoiceId ->
                val updated = char.copy(ttsVoiceId = newVoiceId)
                vm.updateAssistant(updated)
                inspectingAssistant = updated
            },
            onDismiss = { inspectingAssistant = null },
            onStartChat = {
                inspectingAssistant = null
                scope.launch {
                    try {
                        vm.updateSettings(settings.copy(assistantId = char.id))
                        val conversation = withContext(Dispatchers.IO) {
                            vm.createConversation(char.id)
                        }
                        withContext(Dispatchers.Main) {
                            navigateToChatPage(navController, conversation.id)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            loadError = e
                            toaster?.show(
                                "Failed to start chat: ${e.message?.takeIf { it.isNotBlank() } ?: e.toString()}",
                                ToastType.Error
                            )
                        }
                    }
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
                scope.launch { vm.addAssistant(cloned) }
                inspectingAssistant = null
                toaster?.show("Cloned ${char.name}")
            },
            onExport = {
                showExportDialogFor = char
            },
            onDelete = {
                characterToDelete = char
            },
            onUpdateLorebookIds = { newIds ->
                val updated = char.copy(enabledLorebookIds = newIds)
                vm.updateAssistant(updated)
                inspectingAssistant = updated
            }
        )
    }

    // Delete confirmation dialog
    characterToDelete?.let { char ->
        SettingsDestructiveConfirmDialog(
            visible = true,
            title = "Delete ${char.name}?",
            message = "This will permanently remove this character from your library. Associated chat sessions will be preserved.",
            onConfirm = {
                val toDelete = char
                characterToDelete = null
                charGroupAssignments = charGroupAssignments - toDelete.id.toString()
                persistGroups()
                if (inspectingAssistant?.id == toDelete.id) {
                    inspectingAssistant = null
                }
                vm.removeAssistant(toDelete)
                toaster?.show(
                    message = "Deleted ${toDelete.name}",
                    action = me.lucid.domchat.ui.components.ui.ToastAction(
                        label = "Undo",
                        onClick = { vm.undoRemoveAssistant(toDelete) }
                    )
                )
            },
            onDismiss = { characterToDelete = null }
        )
    }

    // Export options dialog
    showExportDialogFor?.let { char ->
        CharacterExportDialog(
            assistant = char,
            onDismissRequest = { showExportDialogFor = null }
        )
    }

    // Creator Notes Modal
    showingCreatorNotesFor?.let { char ->
        CharacterNotesBottomSheet(
            characterName = char.displayName?.takeIf { it.isNotBlank() } ?: char.name.ifBlank { "Assistant" },
            creatorName = char.creator,
            characterVersion = char.characterVersion,
            notes = char.creatorNotes.orEmpty(),
            onDismissRequest = { showingCreatorNotesFor = null }
        )
    }

    // New Group Dialog
    NewGroupDialog(
        visible = showNewGroupDialog,
        onDismissRequest = {
            showNewGroupDialog = false
            newGroupName = ""
        },
        groupName = newGroupName,
        onGroupNameChange = { newGroupName = it },
        onCreate = { groupName ->
            val newGroupId = java.util.UUID.randomUUID().toString()
            groupNames = groupNames + (newGroupId to groupName)
            groupOrder = (groupOrder + newGroupId).distinct()
            persistGroups()
            showNewGroupDialog = false
            newGroupName = ""
            toaster?.show("Created group '$groupName'", ToastType.Success)
        }
    )

    // Character Action Popover (long-click)
    characterToMove?.let { char ->
        val charGroupId = charGroupAssignments[char.id.toString()]
        CharacterActionPopover(
            visible = showMoveToGroupDialog,
            assistant = char,
            characterGroupId = charGroupId,
            groupNames = groupNames,
            onDismiss = {
                showMoveToGroupDialog = false
                characterToMove = null
            },
            onDelete = {
                characterToDelete = char
                characterToMove = null
                showMoveToGroupDialog = false
            },
            onExportPng = {
                val target = char
                pendingExportAssistantId = target.id.toString()
                runCatching {
                    exportPngLauncher.launch("${target.name.replace(" ", "_")}.png")
                }.onFailure {
                    toaster?.show("Failed to launch document picker", ToastType.Error)
                }
            },
            onExportJson = {
                val target = char
                pendingExportAssistantId = target.id.toString()
                runCatching {
                    exportJsonLauncher.launch("${target.name.replace(" ", "_")}.json")
                }.onFailure {
                    toaster?.show("Failed to launch document picker", ToastType.Error)
                }
            },
            onMoveToGroup = { groupId ->
                val newGroupId = if (groupId.isNullOrBlank()) null else groupId
                charGroupAssignments = if (newGroupId == null) {
                    charGroupAssignments - char.id.toString()
                } else {
                    charGroupAssignments + (char.id.toString() to newGroupId)
                }
                persistGroups()
                showMoveToGroupDialog = false
                characterToMove = null
                selectedGroupId = newGroupId
                toaster?.show("Moved '${char.effectiveDisplayName}'", ToastType.Success)
            },
            onDeleteGroup = { groupId ->
                groupNames = groupNames - groupId
                charGroupAssignments = charGroupAssignments.filterValues { it != groupId }
                persistGroups()
                if (selectedGroupId == groupId) {
                    selectedGroupId = null
                }
                showMoveToGroupDialog = false
                characterToMove = null
                toaster?.show("Group deleted", ToastType.Success)
            }
        )
    }

    // Group re-ordering bottom sheet (root level so it slides from bottom of screen)
    GroupReorderSheet(
        visible = showReorderSheet,
        groupOrder = groupOrder,
        groupNames = groupNames,
        onDismissRequest = { showReorderSheet = false },
        onReorder = { newOrder ->
            groupOrder = newOrder
            persistGroups()
        },
        onDeleteGroup = { groupId ->
            charGroupAssignments = charGroupAssignments.filterValues { it != groupId }
            groupNames = groupNames - groupId
            groupOrder = groupOrder.filter { it != groupId }
            if (selectedGroupId == groupId) {
                selectedGroupId = null
            }
            persistGroups()
            toaster?.show("Group deleted — characters moved to All", ToastType.Info)
        }
    )
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
                // ImageRequest.build() can throw on a malformed avatar URL; never let one bad
                // avatar crash hub composition — fall back to a null model (AsyncImage is null-safe).
                val request = remember(avatar.url) {
                    try {
                        coil3.request.ImageRequest.Builder(context)
                            .data(avatar.url)
                            .size(360, 520)
                            .crossfade(true)
                            .build()
                    } catch (_: Exception) {
                        null
                    }
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
                val context = androidx.compose.ui.platform.LocalContext.current
                val safeId = androidx.compose.runtime.remember(avatar.id, context) {
                    safeDrawableResId(context, avatar.id, me.lucid.domchat.R.drawable.default_generical_pfp)
                }
                AsyncImage(
                    model = safeId,
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
                        .background(Color(0xFF000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = avatar.content, fontSize = 48.sp)
                }
            }
            is Avatar.Dummy -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000)),
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
 * Tall Vertical Portrait Card — full-bleed avatar with bottom glass scrim,
 * bold display name with right-edge alpha fade, and a horizontal scrollable
 * tag row with a dynamic trailing fade.
 */
@Composable
fun CharacterPortraitCard(
    assistant: Assistant,
    settings: Settings,
    isDark: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val tagList = assistant.tags

    // Resolve tag UUIDs to display names via settings.assistantTags; filter out raw UUIDs and missing names
    val resolvedTagNames = remember(tagList, settings?.assistantTags) {
        val tagMap = settings?.assistantTags?.associateBy { it.id }.orEmpty()
        tagList.mapNotNull { tagId ->
            tagMap[tagId]?.name?.takeIf { name ->
                name.isNotBlank() && name.length <= 20 && !name.contains("-")
            }
        }.distinct()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF000000))
            .border(
                0.5.dp,
                if (isDark) LiquidGlassTokens.SpecularBorderSubtle
                else Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)),
                RoundedCornerShape(18.dp)
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        // Full-bleed avatar background
        FullCharacterCardImage(
            name = assistant.effectiveDisplayName,
            avatar = assistant.avatar,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom vertical gradient scrim: transparent -> black for smooth fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.42f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF000000).copy(alpha = 0.90f),
                        )
                    )
                )
        )

        // Bottom content: display name + tag row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Character display name: bold white, max 2 lines, ellipsis overflow.
            val displayName = assistant.effectiveDisplayName
            Text(
                text = displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal scrollable tag pill row (lightweight Row avoids SubcomposeLayout per card)
            if (resolvedTagNames.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    resolvedTagNames.forEach { tagName ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF000000))
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = tagName.replace("_", " "),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.92f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }


        }
    }
}

/**
 * Strips raw HTML tags (<p>, <br>, etc.) and decodes common HTML entities from character descriptions.
 */
private fun cleanHtmlText(raw: String): String {
    return raw
        .replace(HTML_TAG_REGEX, " ")
        .replace("&nbsp;", " ")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&#39;", "'")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(WHITESPACE_REGEX, " ")
        .trim()
}

/**
 * Sleek Upload Character button with obsidian surface, subtle indigo specular gradient rim, and haptic feedback.
 */
@Composable
private fun UploadCharacterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberPremiumHaptics()
    Surface(
        onClick = {
            haptics.perform(HapticPattern.Pop)
            onClick()
        },
        shape = AppShapes.ButtonPill,
        color = Color(0xFF000000),
        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
        modifier = modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.FileUpload,
                contentDescription = "Upload Character",
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = "Upload",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}

@Composable
fun CharacterListRow(
    assistant: Assistant,
    isDark: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF000000),
        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(12.dp))
            ) {
                FullCharacterCardImage(
                    name = assistant.effectiveDisplayName,
                    avatar = assistant.avatar,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = assistant.effectiveDisplayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!assistant.creator.isNullOrBlank()) {
                    Text(
                        text = "by ${assistant.creator.truncateCreator(12)}",
                        fontSize = 12.sp,
                        color = Color(0xFF8A8A96),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val rawBlurb: String? = assistant.creatorNotes?.takeIf { it.isNotBlank() } ?: assistant.authorsNote?.takeIf { it.isNotBlank() }
                if (!rawBlurb.isNullOrBlank()) {
                    val blurb = remember(rawBlurb) {
                        val cleaned = cleanHtmlText(rawBlurb)
                        if (cleaned.length > 120) "${cleaned.take(120).trim()}..." else cleaned
                    }
                    if (blurb.isNotBlank()) {
                        Text(
                            text = blurb,
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            val introsCount = assistant.alternateGreetings.size + 1
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF000000))
                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$introsCount greeting${if (introsCount > 1) "s" else ""}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Crash-proof fallback card rendered when a single character fails to compose.
 * Isolates per-card failures so one bad assistant never takes down the whole hub gallery.
 */
@Composable
fun CharacterHubErrorCard(
    assistant: Assistant,
    error: Throwable
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF000000))
            .border(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.Error,
                contentDescription = null,
                tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = assistant.effectiveDisplayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Failed to load card",
                fontSize = 10.sp,
                color = Color(0xFF8A8A96),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = error.message?.takeIf { it.isNotBlank() } ?: error.toString(),
                fontSize = 10.sp,
                color = Color(0xFFEF4444).copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CharacterInspectorSheet(
    assistant: Assistant,
    settings: Settings? = null,
    isDark: Boolean,
    ttsProviders: List<TTSProviderSetting> = emptyList(),
    catalogSnapshot: ModelCatalogSnapshot? = null,
    onUpdateVoice: ((Uuid?) -> Unit)? = null,
    onDismiss: () -> Unit,
    onStartChat: () -> Unit,
    onEdit: () -> Unit,
    onViewCreatorNotes: () -> Unit,
    onDuplicate: () -> Unit = {},
    onExport: () -> Unit = {},
    onDelete: () -> Unit = {},
    onUpdateLorebookIds: ((Set<kotlin.uuid.Uuid>) -> Unit)? = null
) {
    val sheetShape = me.lucid.domchat.ui.theme.AppShapes.BottomSheet
    val contentTextColor = Color(0xFFF2F2F7)
    val cardBgColor = Color(0xFF000000)
    val cardBorderColor = me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
    val haptics = rememberPremiumHaptics()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var viewerImageUrls by remember { mutableStateOf(emptyList<String>()) }
    var viewerOpen by remember { mutableStateOf(false) }
    var viewerCurrentPage by remember { mutableIntStateOf(0) }

    // Lorebook management state
    var showAttachLorebookDialog by remember { mutableStateOf(false) }
    var lorebookToDetach by remember { mutableStateOf<Lorebook?>(null) }
    var showDetachConfirm by remember { mutableStateOf(false) }
    val attachedLorebookList = remember(settings?.lorebooks, assistant.enabledLorebookIds) {
        settings?.lorebooks?.filter { it.id in assistant.enabledLorebookIds } ?: emptyList()
    }
    val hasAttachedLorebooks = attachedLorebookList.isNotEmpty()

    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF000000),
        dragHandle = null,
        shape = sheetShape,
        maxHeight = screenHeight * 0.92f,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Color(0xFFF2F2F7),
            LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFF2F2F7))
        ) {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    surface = Color(0xFF000000),
                    onSurface = Color(0xFFF2F2F7),
                    surfaceVariant = Color(0xFF141418),
                    onSurfaceVariant = Color(0xFFC7C7CC),
                    background = Color(0xFF000000),
                    onBackground = Color(0xFFF2F2F7),
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Hero Image with smooth multi-stop cubic vertical gradient fade into sheet background
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clickable {
                                val url = when (val av = assistant.avatar) {
                                    is Avatar.Image -> av.url
                                    is Avatar.Resource -> "android.resource://${context.packageName}/${safeDrawableResId(context, av.id, me.lucid.domchat.R.drawable.default_generical_pfp)}"
                                    else -> null
                                }
                                if (!url.isNullOrBlank()) {
                                    viewerImageUrls = listOf(url)
                                    viewerCurrentPage = 0
                                    viewerOpen = true
                                }
                            }
                    ) {
                        FullCharacterCardImage(
                            name = assistant.effectiveDisplayName,
                            avatar = assistant.avatar,
                            modifier = Modifier.fillMaxSize()
                        )
                        val sheetBgColor = Color(0xFF000000)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf<Color>(
                                            Color.Transparent,
                                            Color.Transparent,
                                            sheetBgColor.copy(alpha = 0.15f),
                                            sheetBgColor.copy(alpha = 0.50f),
                                            sheetBgColor.copy(alpha = 0.85f),
                                            sheetBgColor.copy(alpha = 1f)
                                        )
                                    )
                                )
                            )
                    }
                    // Drag handle overlaid at top center over hero image
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 36.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.35f))
                        )
                    }
                }


                // Content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    // Character Title and Creator
                    Text(
                        text = assistant.effectiveDisplayName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentTextColor
                    )
                    if (!assistant.creator.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Created by ${assistant.creator.truncateCreator(10)}",
                            fontSize = 13.5.sp,
                            color = Color(0xFFA0A0A8)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    val introsCount = assistant.alternateGreetings.size + 1
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBgColor)
                                .border(0.5.dp, cardBorderColor, RoundedCornerShape(8.dp))
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
                            val version = assistant.characterVersion ?: ""
                            val isUuidLike = version.length > 20 || version.contains("-")
                            if (!isUuidLike) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cardBgColor)
                                        .border(0.5.dp, cardBorderColor, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "v${assistant.characterVersion}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFBBBBBB)
                                    )
                                }
                            }
                        }
                    }

                    // Horizontal scrollable tag pill row — resolves names from settings.assistantTags
                    val resolvedSheetTags = assistant.tags.mapNotNull { tagId ->
                        settings?.assistantTags?.find { it.id == tagId }?.name?.takeIf { name ->
                            name.isNotBlank() && name.length <= 20 && !name.contains("-")
                        }
                    }.distinct()
                    if (resolvedSheetTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tags",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentTextColor.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val tagRowState = rememberLazyListState()
                        LazyRow(
                            state = tagRowState,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(resolvedSheetTags, key = { it }) { tagName ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF000000))
                                        .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = tagName.replace("_", " "),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.92f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }

                    // Creator notes blurb excerpt above the Start Chat button:
                    // Strip HTML tags and normalize block tags so <p>/<center>/<span> markup never leaks,
                    // displaying the first paragraph smoothly without artificial punctuation cuts.
                    val creatorNotesBlurb = stripHtmlTags(assistant.creatorNotes.orEmpty())
                    if (creatorNotesBlurb.isNotBlank()) {
                        val firstPara = creatorNotesBlurb.split("\n\n").firstOrNull()?.trim().orEmpty()
                        if (firstPara.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = firstPara,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
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
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.Chat, contentDescription = null, tint = Color.Black, modifier = Modifier.size(19.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Chat", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Surface(
                            onClick = onEdit,
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF000000),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                            modifier = Modifier
                                .height(48.dp)
                                .width(48.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Character Management Buttons (Export, Clone, Delete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = onExport,
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF000000),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Export", fontSize = 12.5.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }

                        Surface(
                            onClick = onDuplicate,
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF000000),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Clone", fontSize = 12.5.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }

                        val isDefaultGenerical = assistant.id == me.lucid.domchat.data.datastore.DEFAULT_ASSISTANT_ID
                        Button(
                            onClick = onDelete,
                            enabled = !isDefaultGenerical,
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935),
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.12f),
                                disabledContentColor = Color.White.copy(alpha = 0.35f),
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = if (isDefaultGenerical) Color.White.copy(alpha = 0.35f) else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
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
                                        tint = Color.White,
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
                                            color = Color(0xFF8A8A96)
                                        )
                                    }
                                }
                                VoiceSelector(
                                    voiceId = assistant.ttsVoiceId,
                                    providers = ttsProviders,
                                    allowClear = true,
                                    onClear = { onUpdateVoice?.invoke(null) },
                                    onSelect = { onUpdateVoice?.invoke(it.id) },
                                    catalogSnapshot = catalogSnapshot
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // TABS: 0: Notes, 1: Personality, 2: Greetings, 3: Lorebooks (when attached)
                    var selectedTab by remember { mutableIntStateOf(0) }
                    val tabTitles = listOf("Notes", "Personality", "Greetings") + (if (hasAttachedLorebooks) listOf("Lorebooks") else emptyList())

                    LaunchedEffect(tabTitles) {
                        if (selectedTab >= tabTitles.size) {
                            selectedTab = tabTitles.lastIndex
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBgColor)
                            .border(0.5.dp, cardBorderColor, RoundedCornerShape(14.dp))
                            .padding(4.dp)
                            .horizontalScroll(rememberScrollState()),
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
                                            Color.White.copy(alpha = 0.16f)
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
                                        val hasHtml = remember(notes) {
                                            notes.contains("<p", ignoreCase = true) ||
                                            notes.contains("<div", ignoreCase = true) ||
                                            notes.contains("<img", ignoreCase = true) ||
                                            notes.contains("<span", ignoreCase = true) ||
                                            notes.contains("<center", ignoreCase = true) ||
                                            notes.contains("<h1", ignoreCase = true) ||
                                            notes.contains("<h2", ignoreCase = true) ||
                                            notes.contains("<h3", ignoreCase = true) ||
                                            notes.contains("<br", ignoreCase = true) ||
                                            notes.contains("<hr", ignoreCase = true)
                                        }
                                        if (hasHtml) {
                                            SimpleHtmlBlock(html = notes)
                                        } else {
                                            MarkdownBlock(
                                                content = notes
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "No creator notes provided for this character.",
                                            fontSize = 13.sp,
                                            color = Color(0xFF8A8A96)
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
                                                    color = Color.White
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
                                                color = Color(0xFF88888A)
                                            )
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // Opening Greetings Tab with animated carousel
                                val allGreetings = remember(assistant.firstMessage, assistant.alternateGreetings) {
                                    val list = mutableListOf<String>()
                                    if (assistant.firstMessage.isNotBlank() && !isDummyGreeting(assistant.firstMessage)) {
                                        list.add(assistant.firstMessage)
                                    }
                                    list.addAll(assistant.alternateGreetings.filter { !isDummyGreeting(it) })
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
                                            color = Color(0xFF8A8A96)
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
                                                    .background(Color(0xFF000000))
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
                                                                .background(Color(0xFF000000))
                                                                .border(
                                                                    0.5.dp,
                                                                    if (isGreetingSelected) {
                                                                        Brush.verticalGradient(
                                                                            listOf(
                                                                                Color.White.copy(alpha = 0.42f),
                                                                                Color.White.copy(alpha = 0.18f),
                                                                                Color.White.copy(alpha = 0.06f),
                                                                            )
                                                                        )
                                                                    } else cardBorderColor,
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .clickable { activeGreetingIndex = idx }
                                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = if (idx == 0) "Intro #1 (Default)" else "Greeting #${idx + 1}",
                                                                fontSize = 12.sp,
                                                                fontWeight = if (isGreetingSelected) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isGreetingSelected) Color(0xFFFFFFFF) else Color.White.copy(alpha = 0.65f)
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
                                                if (greetingText.isNotBlank()) {
                                                    MarkdownBlock(
                                                        content = formatRoleplayTextWithCdnImages(
                                                            sanitizeHtml(greetingText)
                                                        )
                                                    )
                                                } else {
                                                    Text(
                                                        text = "No greeting content.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            3 -> {
                                // Lorebooks Tab - manage attached lorebooks
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (attachedLorebookList.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(cardBgColor)
                                                .border(0.5.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = "No lorebooks attached to this character.",
                                                fontSize = 13.sp,
                                                color = Color(0xFF8A8A96)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Attached Lorebooks",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            attachedLorebookList.forEach { lorebook: Lorebook ->
                                                val isEnabled = assistant.enabledLorebookIds.contains(lorebook.id)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(cardBgColor)
                                                        .border(0.5.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                                        .padding(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.weight(1f),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text(
                                                                text = lorebook.name.ifEmpty { "Unnamed Lorebook" },
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (lorebook.description.isNotBlank()) {
                                                                Text(
                                                                    text = lorebook.description,
                                                                    fontSize = 12.sp,
                                                                    color = Color.White.copy(alpha = 0.65f),
                                                                    maxLines = 2,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                            Text(
                                                                text = "${lorebook.entries.size} entr${if (lorebook.entries.size == 1) "y" else "ies"}",
                                                                fontSize = 11.sp,
                                                                color = Color(0xFF8A8A96)
                                                            )
                                                        }
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            // Enable/Disable toggle
                                                            HapticSwitch(
                                                                checked = isEnabled,
                                                                onCheckedChange = { newVal ->
                                                                    val newIds = if (newVal) {
                                                                        assistant.enabledLorebookIds + lorebook.id
                                                                    } else {
                                                                        assistant.enabledLorebookIds - lorebook.id
                                                                    }
                                                                    onUpdateLorebookIds?.invoke(newIds)
                                                                },
                                                                modifier = Modifier.size(40.dp)
                                                            )
                                                            // Detach button
                                                            OutlinedButton(
                                                                onClick = {
                                                                    lorebookToDetach = lorebook
                                                                    showDetachConfirm = true
                                                                },
                                                                modifier = Modifier.height(36.dp),
                                                                shape = RoundedCornerShape(8.dp),
                                                                border = BorderStroke(0.5.dp, cardBorderColor),
                                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                                            ) {
                                                                Text(
                                                                    text = "Detach",
                                                                    fontSize = 11.sp,
                                                                    color = Color(0xFFEF4444)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        // Attach new lorebooks button
                                        Button(
                                            onClick = {
                                                showAttachLorebookDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF000000),
                                                contentColor = Color.White
                                            ),
                                            border = BorderStroke(0.5.dp, cardBorderColor)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Attach Lorebook",
                                                fontWeight = FontWeight.Bold
                                            )
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
            val sheetDismiss = LocalSheetDismiss.current
            IconButton(
                onClick = { sheetDismiss?.invoke() ?: onDismiss() },
                modifier = Modifier
                    .padding(top = 16.dp, end = 16.dp)
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF000000))
                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
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
    }

    // Attach Lorebook Dialog - shows all available lorebooks to attach
    if (showAttachLorebookDialog) {
        val availableLorebooks = settings?.lorebooks?.filter { it.id !in assistant.enabledLorebookIds } ?: emptyList()
        Dialog(
            onDismissRequest = { showAttachLorebookDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            ApplyDialogWindowBlur()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF000000),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Attach Lorebook",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Select lorebooks to attach to this character.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        if (availableLorebooks.isEmpty()) {
                            Text(
                                text = "No lorebooks available to attach.",
                                fontSize = 13.sp,
                                color = Color(0xFF8A8A96)
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableLorebooks.forEach { lorebook ->
                                    val alreadyAttached = assistant.enabledLorebookIds.contains(lorebook.id)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(cardBgColor)
                                            .border(0.5.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            HapticSwitch(
                                                checked = alreadyAttached,
                                                onCheckedChange = { newVal ->
                                                    if (newVal) {
                                                        val newIds = assistant.enabledLorebookIds + lorebook.id
                                                        onUpdateLorebookIds?.invoke(newIds)
                                                        showAttachLorebookDialog = false
                                                    }
                                                },
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = lorebook.name.ifEmpty { "Unnamed Lorebook" },
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (lorebook.description.isNotBlank()) {
                                                    Text(
                                                        text = lorebook.description,
                                                        fontSize = 12.sp,
                                                        color = Color.White.copy(alpha = 0.65f),
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = lorebook.entries.size.toString() + " entr" + (if (lorebook.entries.size == 1) "y" else "ies"),
                                                fontSize = 11.sp,
                                                color = Color(0xFF8A8A96)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showAttachLorebookDialog = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF000000),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(0.5.dp, cardBorderColor)
                        ) {
                            Text(
                                text = "Save",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Detach Confirmation Dialog
    lorebookToDetach?.let { lorebook ->
        Dialog(
            onDismissRequest = {
                lorebookToDetach = null
                showDetachConfirm = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            ApplyDialogWindowBlur()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF000000),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Detach Lorebook",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Remove " + lorebook.name.ifEmpty { "Unnamed Lorebook" } + " from this character?",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    lorebookToDetach = null
                                    showDetachConfirm = false
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.White.copy(alpha = 0.7f)
                                )
                            ) {
                                Text(text = "Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val newIds = assistant.enabledLorebookIds - lorebook.id
                                    onUpdateLorebookIds?.invoke(newIds)
                                    lorebookToDetach = null
                                    showDetachConfirm = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF000000),
                                    contentColor = Color(0xFFEF4444)
                                ),
                                border = BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                            ) {
                                Text(text = "Detach", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Image Viewer Dialog - Swipeable Embedded Image Viewer
    if (viewerOpen && viewerImageUrls.isNotEmpty()) {
        Dialog(
            onDismissRequest = { viewerOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            val viewerPagerState = rememberPagerState(initialPage = viewerCurrentPage.coerceIn(0, (viewerImageUrls.size - 1).coerceAtLeast(0))) { viewerImageUrls.size }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { viewerOpen = false },
            ) {
                HorizontalPager(
                    state = viewerPagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = viewerImageUrls[page],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
                // Page counter indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp),
                ) {
                    Text(
                        text = "${viewerPagerState.currentPage + 1} / ${viewerImageUrls.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
                // Close button top-end
                Surface(
                    onClick = { viewerOpen = false },
                    shape = CircleShape,
                    color = Color(0xFF000000),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 16.dp, end = 16.dp)
                        .align(Alignment.TopEnd)
                        .size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Download button bottom-end (matching chat)
                Surface(
                    onClick = {
                        val currentUrl = viewerImageUrls.getOrNull(viewerPagerState.currentPage)
                        if (!currentUrl.isNullOrBlank()) {
                            scope.launch {
                                try {
                                    val uri = currentUrl.toUri()
                                    val fileName = "character_${System.currentTimeMillis()}.png"
                                    context.saveToDownloads(uri, fileName)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    shape = CircleShape,
                    color = Color(0xFF000000),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp, end = 24.dp)
                        .align(Alignment.BottomEnd)
                        .size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = "Download Image",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Strips raw HTML tags and decodes common HTML entities so creator notes display as clean text.
 * Handles <p>, </p>, <br>, <span>, &nbsp; and numeric/named entities without external dependencies.
 */
/**
 * JAI-safe HTML cleanup: preserves Markdown and inline images instead of stripping them.
 * `` tags and raw image URLs are converted to Markdown image syntax so they render
 * in-place via MarkdownBlock. Other HTML/entity content is left intact for the renderer.
 */
private fun sanitizeHtml(html: String?): String {
    val text = html ?: return ""
    var result = text
        .replace(IMG_TAG_REGEX) { match ->
            "![Image](${match.groupValues[1]})"
        }
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&#39;", "'")
        .replace("&#x27;", "'")
        .replace(DECIMAL_ENTITY_REGEX) { match ->
            match.groupValues[1].toIntOrNull()?.let { it.toChar() }?.toString() ?: match.value
        }
        .replace(HEX_ENTITY_REGEX) { match ->
            match.groupValues[1].toIntOrNull(16)?.let { it.toChar() }?.toString() ?: match.value
        }
        .replace(RAW_IMAGE_URL_REGEX) { match ->
            "![Image](${match.value})"
        }
        .replace(HORIZONTAL_WHITESPACE_REGEX, " ")
        .trim()
    return result
}

private sealed interface ContentSegment {
    data class Text(val text: String) : ContentSegment
    data class Image(val url: String) : ContentSegment
}

/**
 * Parses a text string into a list of [ContentSegment]s, extracting markdown images
 * (![alt](url)), HTML images (<img src='url'>), and raw image URLs. Text between images
 * is preserved as [ContentSegment.Text], and each image URL becomes a [ContentSegment.Image].
 */
private fun parseContentWithImages(text: String): List<ContentSegment> {
    val segments = mutableListOf<ContentSegment>()

    val markdownRegex = MARKDOWN_IMAGE_REGEX
    val htmlImgRegex = HTML_IMG_SRC_REGEX
    val rawUrlRegex = RAW_IMAGE_URL_REGEX

    val allMatches = mutableListOf<Triple<Int, Int, String>>()

    markdownRegex.findAll(text).forEach { m ->
        if (m.groupValues.size > 2) {
            allMatches.add(Triple(m.range.first, m.range.last, m.groupValues[2]))
        }
    }
    htmlImgRegex.findAll(text).forEach { m ->
        if (m.groupValues.size > 1) {
            allMatches.add(Triple(m.range.first, m.range.last, m.groupValues[1]))
        }
    }
    rawUrlRegex.findAll(text).forEach { m ->
        allMatches.add(Triple(m.range.first, m.range.last, m.value))
    }

    allMatches.sortBy { it.first }

    val filtered = mutableListOf<Triple<Int, Int, String>>()
    var lastEnd = -1
    for ((start, end, url) in allMatches) {
        if (start > lastEnd) {
            filtered.add(Triple(start, end, url))
            lastEnd = end
        }
    }

    var textPos = 0
    for ((start, end, url) in filtered) {
        if (start > textPos) {
            segments.add(ContentSegment.Text(text.substring(textPos, start)))
        }
        segments.add(ContentSegment.Image(url))
        textPos = end + 1
    }

    if (textPos < text.length) {
        segments.add(ContentSegment.Text(text.substring(textPos)))
    }

    return segments.filter { segment ->
        when (segment) {
            is ContentSegment.Text -> segment.text.isNotBlank()
            is ContentSegment.Image -> segment.url.isNotBlank()
        }
    }
}

/**
 * Returns true if a greeting text is a dummy placeholder (single dot, dash, underscore, "none"/"None"/"null", empty).
 */
private fun isDummyGreeting(text: String?): Boolean {
    val t = text?.trim() ?: return true
    return when (t) {
        "", ".", "..", "...", "-", "_", "none", "None", "null" -> true
        else -> false
    }
}

/**
 * Horizontally scrollable row of glassy group tabs: 'All (count)', custom groups, and '+ New'.
 * Custom group tabs support long-press (combinedClickable) to open a glassy menu with
 * Rename, Move Forward, Move Backward and Delete Group. Deleting a group never deletes
 * characters — they are unassigned and stay in 'All'.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupTabsRow(
    selectedGroupId: String?,
    groupNames: Map<String, String>,
    groupOrder: List<String>,
    allCount: Int,
    isInitialized: Boolean = true,
    onGroupSelected: (String?) -> Unit,
    onNewGroup: () -> Unit,
    onGroupRenamed: (String, String) -> Unit = { _, _ -> },
    onGroupMoved: (String, Int) -> Unit = { _, _ -> },
    onGroupDeleted: (String) -> Unit = { _ -> },
    onReorderGroups: (List<String>) -> Unit = { _ -> },
    onOpenReorder: () -> Unit = {},
) {
    // Stable display order: persisted order first, then any groups not yet recorded.
    val orderedGroups = (groupOrder.filter { it in groupNames } + groupNames.keys.filter { it !in groupOrder }).toList()
    var longPressedGroupId by remember { mutableStateOf<String?>(null) }
    var renameDraft by remember { mutableStateOf("") }

    val activeGroupBorder = remember {
        BorderStroke(
            0.5.dp,
            LiquidGlassTokens.SpecularBorderBrush
        )
    }
    val inactiveGroupBorder = remember {
        BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" tab
        item(key = "__all_group__") {
            val allLabel = "All"
            val isAllSelected = selectedGroupId == null
            Surface(
                onClick = { onGroupSelected(null) },
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = if (isAllSelected) activeGroupBorder else inactiveGroupBorder,
                modifier = if (isInitialized) Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null) else Modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = allLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // Custom group tabs (long-press opens the group action menu)
        items(orderedGroups, key = { it }) { groupId ->
            val label = groupNames[groupId] ?: "Group"
            val isSelected = selectedGroupId == groupId
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = if (isSelected) activeGroupBorder else inactiveGroupBorder,
                modifier = (if (isInitialized) Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null) else Modifier)
                    .combinedClickable(
                        onClick = { onGroupSelected(groupId) },
                        onLongClick = {
                            longPressedGroupId = groupId
                            renameDraft = label
                        }
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // "+ New" tab
        item(key = "__new_group__") {
            Surface(
                onClick = onNewGroup,
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                modifier = if (isInitialized) Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null) else Modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New Group",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Group action menu (long-press): Rename, Save, Reorder, Delete (Centered popup dialog)
    longPressedGroupId?.let { groupId ->
        val label = groupNames[groupId] ?: "Group"
        Dialog(
            onDismissRequest = { longPressedGroupId = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ApplyDialogWindowBlur()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF000000),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier
                        .widthIn(min = 280.dp, max = 360.dp)
                        .imePadding()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF000000))
                                        .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Folder,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Manage Group",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }
                            IconButton(onClick = { longPressedGroupId = null }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        // Rename input with inline save
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = renameDraft,
                                onValueChange = { renameDraft = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Group name") },
                                shape = AppShapes.InputField,
                                colors = me.lucid.domchat.ui.theme.appFieldColors()
                            )

                            AnimatedVisibility(
                                visible = renameDraft.isNotBlank() && renameDraft != label,
                                enter = fadeIn(tween(150)) + expandVertically(),
                                exit = fadeOut(tween(150)) + shrinkVertically()
                            ) {
                                DomChatGlassPillButton(
                                    text = "Save Rename",
                                    icon = Icons.Rounded.Check,
                                    accent = true,
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    onClick = {
                                        onGroupRenamed(groupId, renameDraft)
                                        longPressedGroupId = null
                                    }
                                )
                            }
                        }

                        // Actions: Clean pill button layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DomChatGlassPillButton(
                                text = "Reorder",
                                icon = Icons.Rounded.Reorder,
                                modifier = Modifier.weight(1f).height(44.dp),
                                onClick = {
                                    longPressedGroupId = null
                                    onOpenReorder()
                                }
                            )
                            DomChatGlassPillButton(
                                text = "Delete",
                                icon = Icons.Rounded.Delete,
                                destructive = true,
                                modifier = Modifier.weight(1f).height(44.dp),
                                onClick = {
                                    onGroupDeleted(groupId)
                                    longPressedGroupId = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom sheet to smoothly reorder groups by dragging up/down, or swiping left to delete.
 */
@Composable
private fun GroupReorderSheet(
    visible: Boolean,
    groupOrder: List<String>,
    groupNames: Map<String, String>,
    onDismissRequest: () -> Unit,
    onReorder: (List<String>) -> Unit,
    onDeleteGroup: (String) -> Unit
) {
    if (!visible) return
    val haptics = rememberPremiumHaptics()
    val orderedGroups = remember(groupOrder, groupNames) {
        (groupOrder.filter { it in groupNames } + groupNames.keys.filter { it !in groupOrder }).toList()
    }
    var currentOrder by remember(visible) { mutableStateOf(orderedGroups) }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val list = currentOrder.toMutableList()
        val item = list.removeAt(from.index)
        list.add(to.index, item)
        currentOrder = list
        onReorder(list)
    }

    val canScrollUp by remember { derivedStateOf { lazyListState.canScrollBackward } }
    val canScrollDown by remember { derivedStateOf { lazyListState.canScrollForward } }

    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF000000),
        maxHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * 0.78f).dp,
        dragHandle = { DomChatSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            DomChatSheetHeader(
                title = "Reorder Groups",
                subtitle = "Drag handle to rearrange groups, swipe left to delete",
                onCloseClick = onDismissRequest,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (currentOrder.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No custom groups created",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 24f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentOrder, key = { it }) { groupId ->
                        ReorderableItem(
                            state = reorderableState,
                            key = groupId
                        ) { isDragging ->
                            PhysicsSwipeToDelete(
                                onDelete = {
                                    onDeleteGroup(groupId)
                                    val updated = currentOrder.filter { it != groupId }
                                    currentOrder = updated
                                    onReorder(updated)
                                },
                                position = ItemPosition.ONLY,
                                modifier = Modifier.fillMaxWidth()
                            ) { shape ->
                                ReorderGroupRow(
                                    groupName = groupNames[groupId] ?: "Group",
                                    dragHandle = {
                                        IconButton(
                                            onClick = {},
                                            modifier = Modifier
                                                .size(36.dp)
                                                .draggableHandle(
                                                    onDragStarted = { haptics.perform(HapticPattern.Pop) },
                                                    onDragStopped = { haptics.perform(HapticPattern.Thud) }
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DragIndicator,
                                                contentDescription = "Drag to reorder",
                                                tint = Color.White.copy(alpha = 0.65f),
                                                modifier = Modifier.size(22.dp)
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

/** Single row in Reorder Groups list with drag handle. Deletion is revealed via right-to-left swipe. */
@Composable
private fun ReorderGroupRow(
    groupName: String,
    dragHandle: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF000000),
        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            dragHandle()
        }
    }
}

/**
 * Glassy dialog to create a new group/folder, vertically centered.
 */
@Composable
private fun NewGroupDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    groupName: String,
    onGroupNameChange: (String) -> Unit,
    onCreate: (String) -> Unit
) {
    if (!visible) return
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ApplyDialogWindowBlur()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0xFF000000),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 360.dp)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "New Group",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Enter a name for this character group.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { onGroupNameChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Group name") },
                        shape = AppShapes.InputField,
                        colors = me.lucid.domchat.ui.theme.appFieldColors()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        DomChatGlassPillButton(
                            text = "Cancel",
                            onClick = { onDismissRequest() },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DomChatGlassPillButton(
                            text = "Create",
                            enabled = groupName.isNotBlank(),
                            onClick = {
                                if (groupName.isNotBlank()) {
                                    onCreate(groupName)
                                    onDismissRequest()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Centered popover shown on long-press of a character card. Shows the character's groups as a
 * horizontally scrollable row of chips and offers remove-from-group / delete-group / delete actions.
 * The dialog wraps its height and is vertically centered so it is never cut off at the top or bottom.
 */
@Composable
private fun CharacterActionPopover(
    visible: Boolean,
    assistant: Assistant,
    characterGroupId: String?,
    groupNames: Map<String, String>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onExportPng: () -> Unit,
    onExportJson: () -> Unit,
    onMoveToGroup: (String?) -> Unit,
    onDeleteGroup: (String) -> Unit
) {
    if (!visible) return
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(20.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF000000),
                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .heightIn(max = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * 0.82f).dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                var isExporting by remember { mutableStateOf(false) }
                AnimatedContent(
                    targetState = isExporting,
                    transitionSpec = {
                        if (targetState) {
                            slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) togetherWith
                                slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
                        } else {
                            slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) togetherWith
                                slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
                        }
                    },
                    label = "character_action_popover_view"
                ) { showExport ->
                    if (!showExport) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(12.dp))
                                    ) {
                                        FullCharacterCardImage(
                                            name = assistant.effectiveDisplayName,
                                            avatar = assistant.avatar,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = assistant.effectiveDisplayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Manage groups & character",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                                IconButton(onClick = onDismiss) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = 0.1f))

                            Text(
                                text = "Assign to group",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isAll = characterGroupId == null
                                GroupActionChip(
                                    text = "All",
                                    selected = isAll,
                                    onClick = { onMoveToGroup(null) }
                                )
                                groupNames.forEach { (groupId, groupName) ->
                                    val isSelected = characterGroupId == groupId
                                    GroupActionChip(
                                        text = groupName,
                                        selected = isSelected,
                                        onClick = { onMoveToGroup(groupId) }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    onClick = { isExporting = true },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .liquidGlassSurface(
                                            shape = RoundedCornerShape(16.dp),
                                            borderWidth = 0.5.dp,
                                            fillColor = Color(0xFF000000).copy(alpha = 0.50f),
                                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Share,
                                            contentDescription = "Export",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Export",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White
                                        )
                                    }
                                }

                                val isDefaultGenerical = assistant.id == me.lucid.domchat.data.datastore.DEFAULT_ASSISTANT_ID
                                Surface(
                                    onClick = { if (!isDefaultGenerical) onDelete() },
                                    enabled = !isDefaultGenerical,
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDefaultGenerical) Color.White.copy(alpha = 0.12f) else Color(0xFFE53935),
                                    border = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = "Delete",
                                            tint = if (isDefaultGenerical) Color.White.copy(alpha = 0.35f) else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Delete",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isDefaultGenerical) Color.White.copy(alpha = 0.35f) else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(onClick = { isExporting = false }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White
                                        )
                                    }
                                    Text(
                                        text = "Export ${assistant.effectiveDisplayName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = onDismiss) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = 0.1f))

                            Text(
                                text = "Choose an export format for this character:",
                                fontSize = 13.sp,
                                color = Color(0xFFAAAAAA)
                            )

                            // Option 1: SillyTavern PNG
                            Surface(
                                onClick = {
                                    onExportPng()
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF000000),
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(Color(0xFF000000), CircleShape)
                                            .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Character Card",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF000000), RoundedCornerShape(6.dp))
                                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "PNG",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Embedded avatar for SillyTavern",
                                            fontSize = 12.sp,
                                            color = Color(0xFF9E9EAA)
                                        )
                                    }
                                }
                            }

                            // Option 2: JSON
                            Surface(
                                onClick = {
                                    onExportJson()
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF000000),
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(Color(0xFF000000), CircleShape)
                                            .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Character Card V2",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF000000), RoundedCornerShape(6.dp))
                                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "JSON",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Standard V2 JSON spec",
                                            fontSize = 12.sp,
                                            color = Color(0xFF9E9EAA)
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
}

/**
 * Single group chip inside the action popover. Wraps content so the 'All' chip never expands
 * to full width when no extra groups exist.
 */
@Composable
private fun GroupActionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val activeBorder = remember {
        BorderStroke(
            0.5.dp,
            LiquidGlassTokens.SpecularBorderBrush
        )
    }
    val inactiveBorder = remember {
        BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = if (selected) activeBorder else inactiveBorder,
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = Color.White
            )
        }
    }
}

/**
 * Dialog to confirm deletion of a character.
 */
@Composable
private fun DeleteConfirmationDialog(
    visible: Boolean,
    assistant: Assistant,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return
    DomChatDestructiveConfirmDialog(
        visible = visible,
        title = "Delete ${assistant.effectiveDisplayName}?",
        consequence = "This will permanently remove this character from your library. Associated chat sessions will be preserved in your history.",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * Strips HTML tags and unescapes common entities, returning plain text. Used for short
 * blurbs (creatorNotes) so rich HTML descriptions don't get truncated mid-tag.
 */
private fun stripHtmlTags(html: String): String {
    return html
        .replace(BR_TAG_REGEX, "\n")
        .replace(BLOCK_TAG_REGEX, "\n")
        .replace(HTML_TAG_REGEX, "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&#39;", "'")
        .replace("&#x27;", "'")
        .replace(DECIMAL_ENTITY_REGEX) { match ->
            match.groupValues[1].toIntOrNull()?.let { it.toChar() }?.toString() ?: match.value
        }
        .replace(HEX_ENTITY_REGEX) { match ->
            match.groupValues[1].toIntOrNull(16)?.let { it.toChar() }?.toString() ?: match.value
        }
        .replace(MULTI_NEWLINE_REGEX, "\n\n")
        .trim()
}

