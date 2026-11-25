package me.lucid.domchat.ui.pages.setting

import android.net.Uri
import androidx.compose.ui.platform.LocalConfiguration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Input
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.BottomSheetDefaults
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedTextFieldDefaults
import me.lucid.domchat.ui.components.chat.DomChatGlassBottomSheet
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.isSystemInDarkTheme
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.appFilterChipColors
import me.lucid.domchat.ui.theme.appFilterChipBorder
import me.lucid.domchat.ui.theme.appFieldColors
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContentColor
import me.lucid.domchat.ui.modifier.fadeEdges
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.components.ui.DomChatDestructiveConfirmDialog
import me.lucid.domchat.ui.components.ui.AppToasterState
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.launch
import me.lucid.ai.provider.ProviderSetting
import me.lucid.common.platform.PlatformHttpClient
import me.lucid.domchat.R
import me.lucid.domchat.Screen
import me.lucid.domchat.data.datastore.ProviderViewMode
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.ui.AutoAIIconWithUrl
import me.lucid.domchat.ui.components.ui.ProviderIcon
import me.lucid.domchat.ui.components.ui.ItemPosition
import me.lucid.domchat.ui.components.ui.listItemShape
import me.lucid.domchat.ui.components.ui.PhysicsSwipeToDelete
import me.lucid.domchat.ui.components.ui.Tag
import me.lucid.domchat.ui.components.ui.TagType
import me.lucid.domchat.ui.components.ui.ProviderShareQrContent
import me.lucid.domchat.ui.components.ui.ProviderShareQrPart
import me.lucid.domchat.ui.components.ui.computeAIIconByName
import me.lucid.domchat.ui.components.ui.decodeProviderSettingParts
import me.lucid.domchat.ui.components.ui.getProviderSlugFromName
import me.lucid.domchat.ui.components.ui.parseProviderShareQrContent
import me.lucid.domchat.ui.components.ui.searchLobeHubIcon
import me.lucid.domchat.ui.components.ui.lobeHubIconUri
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.useEditState
import me.lucid.domchat.ui.pages.setting.components.FALLBACK_PROVIDER_PRESETS
import me.lucid.domchat.ui.pages.setting.components.NANO_GPT_PRESET
import me.lucid.domchat.ui.pages.setting.components.ProviderConfigure
import me.lucid.domchat.ui.pages.setting.components.toProviderSetting
import me.lucid.domchat.ui.pages.setting.components.toProviderPresets
import me.lucid.domchat.ui.pages.setting.components.withSpecialProviderPresets
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.tts.provider.withDefaultVoices
import me.lucid.domchat.utils.ImageUtils
import me.lucid.domchat.utils.plus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import me.lucid.domchat.data.model.Tag as DataTag
import kotlin.uuid.Uuid
import androidx.compose.foundation.layout.widthIn
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.modifier.domChatDialogContainerColor


@Composable
fun SettingProviderPage(
    initialTab: ProvidersTab = ProvidersTab.Models,
    vm: SettingVM = koinViewModel()
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val catalogSnapshot by vm.modelCatalogSnapshot.collectAsStateWithLifecycle()
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val useWideLayout = LocalSettingsWideLayout.current
    val pager = rememberPagerState(initialPage = initialTab.ordinal) { ProvidersTab.entries.size }
    val currentTab = ProvidersTab.entries[pager.currentPage]
    
    var showTtsFilterSettings by remember { mutableStateOf(false) }
    var showAddProviderSheet by remember { mutableStateOf(false) }
    var showAddTtsSheet by remember { mutableStateOf(false) }
    val providerPresets = remember(catalogSnapshot, settings.providers) {
        (catalogSnapshot?.toProviderPresets()?.takeIf { it.isNotEmpty() }
            ?: FALLBACK_PROVIDER_PRESETS
        ).withSpecialProviderPresets().filterNot { preset ->
            preset.type == ProviderSetting.LiteRtLocal::class &&
                settings.providers.any { it is ProviderSetting.LiteRtLocal }
        }
    }
    
    // Search query state
    var searchQuery by remember { mutableStateOf("") }
    
    // View mode comes from settings for persistence
    val viewMode = settings.displaySetting.providerViewMode
    
    // Tag filter state
    var selectedTagIds by remember { mutableStateOf(emptySet<kotlin.uuid.Uuid>()) }
    
    // Filter providers based on search and tags
    val filteredProviders = remember(settings.providers, searchQuery, selectedTagIds) {
        var result = settings.providers.filterNot { it is ProviderSetting.LiteRtLocal }
        
        // Filter by search query
        if (searchQuery.isNotBlank()) {
            result = result.filter { provider ->
                provider.name.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Filter by tags
        if (selectedTagIds.isNotEmpty()) {
            result = result.filter { provider ->
                provider.tags.containsAll(selectedTagIds)
            }
        }
        
        result
    }
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val httpClient = koinInject<PlatformHttpClient>()
    fun addProvider(provider: ProviderSetting) {
        if (provider is ProviderSetting.LiteRtLocal && settings.providers.any { it is ProviderSetting.LiteRtLocal }) {
            navController.navigate(Screen.SettingLocalLlm)
            return
        }
        val providerToAdd = provider.withUniqueId(settings.providers)
        vm.updateSettings(
            settings.copy(
                providers = listOf(providerToAdd) + settings.providers
            ),
            afterPersist = if (providerToAdd is ProviderSetting.LiteRtLocal) {
                { navController.navigate(Screen.SettingLocalLlm) }
            } else {
                null
            },
        )
        
        // Asynchronously check if we can query LobeHub for a monochrome icon
        if (providerToAdd !is ProviderSetting.LiteRtLocal && providerToAdd.customIconUri.isNullOrBlank()) {
            val providerName = providerToAdd.name
            val hasLocalIcon = computeAIIconByName(providerName) != null || 
                    getProviderSlugFromName(providerName) != null
            if (!hasLocalIcon) {
                scope.launch {
                    val slug = searchLobeHubIcon(httpClient, providerName)
                    if (slug != null) {
                        val latestSettings = vm.settings.value
                        val updatedProviders = latestSettings.providers.map { p ->
                            if (p.id == providerToAdd.id) {
                                p.copyProvider(customIconUri = lobeHubIconUri(slug))
                            } else p
                        }
                        vm.updateSettings(latestSettings.copy(providers = updatedProviders))
                    }
                }
            }
        }
    }
    val customDialogState = useEditState<ProviderSetting> {
        addProvider(it)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                OneUITopAppBar(
                    title = stringResource(currentTab.titleRes),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        BackButton()
                    }
                )
            },
            bottomBar = {
                ProvidersBottomBar(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        scope.launch {
                            pager.animateScrollToPage(tab.ordinal)
                        }
                    }
                ) {
                    when (currentTab) {
                        ProvidersTab.Models -> ImportProviderButton(
                            asFab = true,
                            enableHaptics = settings.displaySetting.enableUIHaptics,
                            onAdd = { addProvider(it) }
                        )

                        ProvidersTab.Tts -> {
                            FloatingActionButton(
                                onClick = {
                                    haptics.perform(HapticPattern.Pop)
                                    showTtsFilterSettings = true
                                },
                                shape = CircleShape,
                                containerColor = Color(0xFF000000),
                                contentColor = Color.White,
                                modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
                            ) {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = stringResource(R.string.setting_tts_settings_title),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    when (currentTab) {
                        ProvidersTab.Models -> AddButton(
                            enableHaptics = settings.displaySetting.enableUIHaptics,
                            asFab = true,
                            onClick = { showAddProviderSheet = true }
                        )

                        ProvidersTab.Tts -> AddTTSProviderButton(
                            enableHaptics = settings.displaySetting.enableUIHaptics,
                            asFab = true,
                            onClick = { showAddTtsSheet = true }
                        )
                    }
                }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
        ) {
            HorizontalPager(
                state = pager,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !useWideLayout,
            ) { page ->
            when (ProvidersTab.entries[page]) {
                ProvidersTab.Models -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
            // Delete confirmation dialog state
            var showDeleteDialog by remember { mutableStateOf(false) }
            var providerToDelete by remember { mutableStateOf<ProviderSetting?>(null) }
            

            // Tag filter row - only show when there are tags
            if (settings.providerTags.isNotEmpty()) {
                ProviderTagsFilterRow(
                    providerTags = settings.providerTags,
                    selectedTagIds = selectedTagIds,
                    onUpdateSelectedTagIds = { selectedTagIds = it }
                )
            }
            
            // Provider list/grid view with AnimatedContent
            // Provider list view
            ProviderListView(
                providers = filteredProviders,
                settings = settings,
                haptics = haptics,
                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                searchQuery = searchQuery,
                onNavigateToDetail = { provider ->
                    if (provider is ProviderSetting.LiteRtLocal) {
                        navController.navigate(Screen.SettingLocalLlm)
                    } else {
                        navController.navigate(Screen.SettingProviderDetail(providerId = provider.id.toString()))
                    }
                },
                onDeleteRequest = { provider ->
                    providerToDelete = provider
                    showDeleteDialog = true
                },
                onReorder = { fromProvider, toProvider ->
                    val reorderedProviders = settings.providers.toMutableList()
                    val from = reorderedProviders.indexOfFirst { it.id == fromProvider.id }
                    val to = reorderedProviders.indexOfFirst { it.id == toProvider.id }
                    if (from >= 0 && to >= 0 && from != to) {
                        reorderedProviders.add(to, reorderedProviders.removeAt(from))
                        vm.updateSettings(settings.copy(providers = reorderedProviders))
                    }
                },
                onAddProvider = { provider ->
                    addProvider(provider)
                },
                providerPresets = providerPresets
            )
            
            // Delete confirmation dialog
            DomChatDestructiveConfirmDialog(
                visible = showDeleteDialog && providerToDelete != null,
                title = stringResource(R.string.confirm_delete),
                consequence = stringResource(
                    if (providerToDelete is ProviderSetting.LiteRtLocal) {
                        R.string.setting_provider_page_delete_local_dialog_text
                    } else {
                        R.string.setting_provider_page_delete_dialog_text
                    }
                ),
                onDismiss = {
                    showDeleteDialog = false
                    providerToDelete = null
                },
                onConfirm = {
                    providerToDelete?.let { p -> vm.deleteProvider(p) }
                    showDeleteDialog = false
                    providerToDelete = null
                },
            )
                    }
                }

                ProvidersTab.Tts -> TtsProvidersContent(vm = vm, contentPadding = innerPadding)
            }
        }
        }
    }

    if (showTtsFilterSettings) {
        TtsTextFilterSettingsDialog(
            rules = settings.displaySetting.ttsTextFilterRules,
            onDismiss = { showTtsFilterSettings = false },
            onUpdateRules = { newRules ->
                vm.updateSettings(
                    settings.copy(
                        displaySetting = settings.displaySetting.copy(
                            ttsTextFilterRules = newRules
                        )
                    )
                )
            }
        )
    }

    if (showAddProviderSheet) {
        ChooseProviderSheet(
            visible = true,
            providerPresets = providerPresets,
            onDismissRequest = { showAddProviderSheet = false },
            onSelectPreset = { preset ->
                addProvider(preset.toProviderSetting())
                showAddProviderSheet = false
            },
            onSelectCustom = {
                showAddProviderSheet = false
                customDialogState.open(ProviderSetting.OpenAI())
            }
        )
    }

    if (showAddTtsSheet) {
        AddTTSProviderSheet(
            visible = true,
            catalogSnapshot = catalogSnapshot,
            onDismissRequest = { showAddTtsSheet = false },
            onAdd = { newProvider ->
                val providerToAdd = newProvider.withDefaultVoices()
                vm.updateSettings(
                    settings.copy(
                        ttsProviders = listOf(providerToAdd) + settings.ttsProviders
                    )
                )
                showAddTtsSheet = false
            }
        )
    }

    if (customDialogState.isEditing) {
        CustomProviderSheet(
            dialogState = customDialogState,
            onDismissRequest = { customDialogState.dismiss() }
        )
    }
    }
}

@Composable
private fun SearchBarWithToggle(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    viewMode: ProviderViewMode,
    onToggleViewMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(stringResource(R.string.setting_provider_page_search_placeholder)) },
            modifier = Modifier.weight(1f),
            shape = me.lucid.domchat.ui.theme.AppShapes.SearchField,
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear_search))
                    }
                }
            } else null,
            colors = appFieldColors(),
        )
        IconButton(onClick = onToggleViewMode) {
            Icon(
                imageVector = if (viewMode == ProviderViewMode.LIST) Icons.Rounded.GridView else Icons.AutoMirrored.Rounded.ViewList,
                contentDescription = stringResource(R.string.a11y_toggle_view_mode)
            )
        }
    }
}

@Composable
private fun ProviderListView(
    providers: List<ProviderSetting>,
    settings: me.lucid.domchat.data.datastore.Settings,
    haptics: me.lucid.domchat.ui.hooks.PremiumHaptics,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    searchQuery: String,
    providerPresets: List<me.lucid.domchat.ui.pages.setting.components.ProviderPreset>,
    onNavigateToDetail: (ProviderSetting) -> Unit,
    onDeleteRequest: (ProviderSetting) -> Unit,
    onReorder: (ProviderSetting, ProviderSetting) -> Unit,
    onAddProvider: (ProviderSetting) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromProvider = providers.getOrNull(from.index)
        val toProvider = providers.getOrNull(to.index)
        if (fromProvider != null && toProvider != null) {
            onReorder(fromProvider, toProvider)
        }
    }
    
    // State for swipe neighbor tracking
    var draggingIndex by remember { mutableStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isUnlocked by remember { mutableStateOf(false) }
    var neighborsUnlocked by remember { mutableStateOf(false) }
    // Reset neighborsUnlocked when offset returns to 0
    if (dragOffset == 0f && neighborsUnlocked) {
        neighborsUnlocked = false
    }
    
    // Check for matching preset when no providers found
    val matchingPreset = remember(searchQuery, providers) {
        if (providers.isEmpty() && searchQuery.isNotBlank()) {
            providerPresets.find { preset ->
                preset.name.contains(searchQuery, ignoreCase = true) ||
                preset.description.contains(searchQuery, ignoreCase = true)
            }
        } else null
    }

    val canScrollBackward by remember {
        derivedStateOf { lazyListState.canScrollBackward }
    }
    val topFadeProgress by animateFloatAsState(
        targetValue = if (canScrollBackward) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "provider_top_fade"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .fadeEdges(topProgress = topFadeProgress, fadeHeight = 44f)
                .imePadding(),
            contentPadding = contentPadding + PaddingValues(horizontal = 16.dp, vertical = 8.dp) + PaddingValues(bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            state = lazyListState,
        ) {
            // Show empty state when no providers at all
            if (providers.isEmpty() && matchingPreset == null) {
                item(key = "empty") {
                    val isDark = LocalDarkMode.current
                    val emptyCardBg = Color(0xFF000000)
                    val emptyBorderColor = me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.CardLarge)
                            .background(emptyCardBg)
                            .border(1.dp, emptyBorderColor, AppShapes.CardLarge)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Cloud,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.White.copy(alpha = 0.65f).copy(alpha = 0.5f)
                            )
                            Text(
                                text = stringResource(R.string.setting_provider_page_empty_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.setting_provider_page_empty_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            // Show preset suggestion if no providers match but preset exists
            else if (matchingPreset != null) {
                item {
                    val isDark = LocalDarkMode.current
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    Text(
                        text = stringResource(R.string.setting_provider_page_no_providers_but_preset),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    
                    val presetBg = Color(0xFF000000)
                    val presetBorder = me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
                    Surface(
                        onClick = {
                            val provider = matchingPreset.toProviderSetting()
                            onAddProvider(provider)
                        },
                        modifier = Modifier.fillMaxWidth().border(0.5.dp, presetBorder, RoundedCornerShape(24.dp)),
                        shape = me.lucid.domchat.ui.theme.AppShapes.CardMedium,
                        color = presetBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (matchingPreset.type == ProviderSetting.LiteRtLocal::class) {
                                Icon(
                                    imageVector = Icons.Rounded.PhoneAndroid,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White,
                                )
                            } else {
                                AutoAIIconWithUrl(
                                    name = matchingPreset.name,
                                    customIconUri = matchingPreset.customIconUri,
                                    modifier = Modifier.size(40.dp),
                                    contentColor = Color.White,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = matchingPreset.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                )
                                Text(
                                    text = matchingPreset.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.65f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                }
            }

            itemsIndexed(providers, key = { _, it -> it.id }) { index, provider ->
                val position = when {
                    providers.size == 1 -> ItemPosition.ONLY
                    index == 0 -> ItemPosition.FIRST
                    index == providers.lastIndex -> ItemPosition.LAST
                    else -> ItemPosition.MIDDLE
                }
                
                // Calculate neighbor offset
                val thresholdPx = with(density) { 35.dp.toPx() }
                if (draggingIndex >= 0 && !neighborsUnlocked && kotlin.math.abs(dragOffset) >= thresholdPx) {
                    neighborsUnlocked = true
                }
                
                val shouldNeighborFollow = draggingIndex >= 0 && 
                    draggingIndex != index && 
                    !isUnlocked && 
                    !neighborsUnlocked
                
                val neighborOffset = if (shouldNeighborFollow) {
                    val distance = kotlin.math.abs(index - draggingIndex)
                    when (distance) {
                        1 -> dragOffset * 0.35f
                        2 -> dragOffset * 0.12f
                        else -> 0f
                    }
                } else {
                    0f
                }
                

                ReorderableItem(
                    state = reorderableState,
                    key = provider.id
                ) { isDragging ->
                    PhysicsSwipeToDelete(
                        position = position,
                        deleteEnabled = true,
                        neighborOffset = neighborOffset,
                        onDragProgress = { offset, unlocked ->
                            draggingIndex = index
                            dragOffset = offset
                            isUnlocked = unlocked
                        },
                        onDragEnd = {
                            if (draggingIndex == index) {
                                draggingIndex = -1
                                dragOffset = 0f
                            }
                        },
                        onDelete = {
                            onDeleteRequest(provider)
                        },
                        modifier = Modifier
                            .scale(if (isDragging) 0.95f else 1f)
                            .fillMaxWidth()
                    ) { animatedShape ->
                        ProviderItemContent(
                            provider = provider,
                            animatedShape = animatedShape,
                            providerTags = settings.providerTags,
                            haptics = haptics,
                            dragHandle = {
                                IconButton(
                                    onClick = {},
                                    modifier = Modifier
                                        .longPressDraggableHandle(
                                            onDragStarted = {
                                                haptics.perform(HapticPattern.Pop)
                                            },
                                            onDragStopped = {
                                                haptics.perform(HapticPattern.Thud)
                                            }
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DragIndicator,
                                        contentDescription = null
                                    )
                                }
                            },
                            onClick = {
                                onNavigateToDetail(provider)
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF000000)
                        )
                    )
                )
        )
    }
}

@Composable
private fun ImportProviderButton(
    asFab: Boolean = false,
    enableHaptics: Boolean,
    onAdd: (ProviderSetting) -> Unit
) {
    val toaster = LocalToaster.current
    val context = LocalContext.current
    val haptics = rememberPremiumHaptics(enabled = enableHaptics)
    var showImportDialog by remember { mutableStateOf(false) }
    var importSession by remember { mutableStateOf<ProviderQrImportSession?>(null) }

    val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        handleQRResult(
            result = result,
            onAdd = onAdd,
            toaster = toaster,
            context = context,
            importSession = importSession,
            onImportSessionChange = { importSession = it },
            onSuccess = { haptics.perform(HapticPattern.Success) },
            onProgress = { haptics.perform(HapticPattern.Pop) },
            onError = { haptics.perform(HapticPattern.Error) }
        )
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            handleImageQRCode(
                uri = it,
                onAdd = onAdd,
                toaster = toaster,
                context = context,
                importSession = importSession,
                onImportSessionChange = { session -> importSession = session },
                onSuccess = { haptics.perform(HapticPattern.Success) },
                onProgress = { haptics.perform(HapticPattern.Pop) },
                onError = { haptics.perform(HapticPattern.Error) }
            )
        }
    }

    val openImportDialog = {
        haptics.perform(HapticPattern.Pop)
        showImportDialog = true
    }

    val isDark = LocalDarkMode.current
    if (asFab) {
        FloatingActionButton(
            onClick = openImportDialog,
            shape = CircleShape,
            containerColor = Color(0xFF000000),
            contentColor = Color.White,
            modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Rounded.Input, stringResource(R.string.import_label), tint = Color.White)
        }
    } else {
        IconButton(onClick = openImportDialog) {
            Icon(Icons.AutoMirrored.Rounded.Input, null)
        }
    }

    if (showImportDialog) {
        ApplyDialogWindowBlur()
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .widthIn(max = 360.dp)
                .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, AppShapes.SettingDialog),
            shape = AppShapes.SettingDialog,
            containerColor = Color(0xFF000000),
            titleContentColor = Color(0xFFF2F2F7),
            textContentColor = Color(0xFFF2F2F7),
            title = {
                Text(
                    text = stringResource(R.string.setting_provider_page_import_dialog_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.setting_provider_page_import_dialog_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                showImportDialog = false
                                scanQrCodeLauncher.launch(null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = me.lucid.domchat.ui.theme.AppShapes.SettingControl,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFFFFF),
                                contentColor = Color(0xFF000000)
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.setting_provider_page_scan_qr_code),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                showImportDialog = false
                                pickImageLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = me.lucid.domchat.ui.theme.AppShapes.SettingControl,
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF000000),
                                contentColor = Color(0xFFF2F2F7)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.setting_provider_page_select_from_gallery),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        haptics.perform(HapticPattern.Cancel)
                        showImportDialog = false
                    },
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }

    importSession?.let { session ->
        ApplyDialogWindowBlur()
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF000000),
            shape = me.lucid.domchat.ui.theme.AppShapes.SettingDialog,
            modifier = Modifier.widthIn(max = 360.dp).border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SettingDialog),
            titleContentColor = Color(0xFFF2F2F7),
            textContentColor = Color(0xFFF2F2F7),
            title = {
                Text(
                    text = stringResource(R.string.setting_provider_page_multi_qr_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(
                            R.string.setting_provider_page_multi_qr_message,
                            session.scannedCount,
                            session.total
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    Text(
                        text = stringResource(
                            R.string.setting_provider_page_multi_qr_parts,
                            session.scannedIndexes
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        scanQrCodeLauncher.launch(null)
                    },
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(stringResource(R.string.setting_provider_page_scan_next_qr))
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            haptics.perform(HapticPattern.Pop)
                            importSession = session.removeLastScannedPart()
                        },
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(stringResource(R.string.setting_provider_page_back_step))
                    }
                    TextButton(
                        onClick = {
                            haptics.perform(HapticPattern.Cancel)
                            importSession = null
                        },
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        )
    }
}

private fun ProviderSetting.withUniqueId(existingProviders: List<ProviderSetting>): ProviderSetting {
    if (existingProviders.none { it.id == id }) {
        return this
    }
    return copyProvider(id = Uuid.random())
}

private data class ProviderQrImportSession(
    val transferId: String,
    val total: Int,
    val parts: Map<Int, ProviderShareQrPart>,
    val scanOrder: List<Int>
) {
    val scannedCount: Int get() = parts.size
    val scannedIndexes: String get() = parts.keys.sorted().joinToString(", ")

    fun removeLastScannedPart(): ProviderQrImportSession? {
        val lastIndex = scanOrder.lastOrNull() ?: return null
        val nextParts = parts - lastIndex
        val nextScanOrder = scanOrder.dropLast(1)
        return if (nextParts.isEmpty()) {
            null
        } else {
            copy(parts = nextParts, scanOrder = nextScanOrder)
        }
    }
}

private fun handleQRResult(
    result: QRResult,
    onAdd: (ProviderSetting) -> Unit,
    toaster: AppToasterState,
    context: android.content.Context,
    importSession: ProviderQrImportSession?,
    onImportSessionChange: (ProviderQrImportSession?) -> Unit,
    onSuccess: () -> Unit,
    onProgress: () -> Unit,
    onError: () -> Unit
) {
    runCatching {
        when (result) {
            is QRResult.QRError -> {
                onError()
                toaster.show(
                    context.getString(
                        R.string.setting_provider_page_scan_error,
                        result
                    ), type = ToastType.Error
                )
            }

            QRResult.QRMissingPermission -> {
                onError()
                toaster.show(
                    context.getString(R.string.setting_provider_page_no_permission),
                    type = ToastType.Error
                )
            }

            is QRResult.QRSuccess -> {
                handleProviderShareQrValue(
                    value = result.content.rawValue ?: "",
                    onAdd = onAdd,
                    toaster = toaster,
                    context = context,
                    importSession = importSession,
                    onImportSessionChange = onImportSessionChange,
                    onSuccess = onSuccess,
                    onProgress = onProgress,
                    onError = onError
                )
            }

            QRResult.QRUserCanceled -> {}
        }
    }.onFailure { error ->
        onError()
        toaster.show(
            context.getString(R.string.setting_provider_page_qr_decode_failed, error.message ?: ""),
            type = ToastType.Error
        )
    }
}

private fun handleImageQRCode(
    uri: Uri,
    onAdd: (ProviderSetting) -> Unit,
    toaster: AppToasterState,
    context: android.content.Context,
    importSession: ProviderQrImportSession?,
    onImportSessionChange: (ProviderQrImportSession?) -> Unit,
    onSuccess: () -> Unit,
    onProgress: () -> Unit,
    onError: () -> Unit
) {
    runCatching {
        val qrContent = ImageUtils.decodeQRCodeFromUri(context, uri)

        if (qrContent.isNullOrEmpty()) {
            onError()
            toaster.show(
                context.getString(R.string.setting_provider_page_no_qr_found),
                type = ToastType.Error
            )
            return
        }

        handleProviderShareQrValue(
            value = qrContent,
            onAdd = onAdd,
            toaster = toaster,
            context = context,
            importSession = importSession,
            onImportSessionChange = onImportSessionChange,
            onSuccess = onSuccess,
            onProgress = onProgress,
            onError = onError
        )
    }.onFailure { error ->
        onError()
        toaster.show(
            context.getString(R.string.setting_provider_page_image_qr_decode_failed, error.message ?: ""),
            type = ToastType.Error
        )
    }
}

private fun handleProviderShareQrValue(
    value: String,
    onAdd: (ProviderSetting) -> Unit,
    toaster: AppToasterState,
    context: android.content.Context,
    importSession: ProviderQrImportSession?,
    onImportSessionChange: (ProviderQrImportSession?) -> Unit,
    onSuccess: () -> Unit,
    onProgress: () -> Unit,
    onError: () -> Unit
) {
    when (val content = parseProviderShareQrContent(value)) {
        is ProviderShareQrContent.Single -> {
            onImportSessionChange(null)
            onAdd(content.provider)
            onSuccess()
            toaster.show(
                context.getString(R.string.setting_provider_page_import_success),
                type = ToastType.Success
            )
        }

        is ProviderShareQrContent.Part -> {
            val part = content.part
            val currentSession = importSession ?: ProviderQrImportSession(
                transferId = part.transferId,
                total = part.total,
                parts = emptyMap(),
                scanOrder = emptyList()
            )

            if (currentSession.transferId != part.transferId || currentSession.total != part.total) {
                onError()
                toaster.show(
                    context.getString(R.string.setting_provider_page_multi_qr_wrong_transfer),
                    type = ToastType.Error
                )
                return
            }

            if (currentSession.parts[part.index]?.data == part.data) {
                onProgress()
                toaster.show(
                    context.getString(R.string.setting_provider_page_multi_qr_duplicate),
                    type = ToastType.Info
                )
                onImportSessionChange(currentSession)
                return
            }

            val updatedSession = currentSession.copy(
                parts = currentSession.parts + (part.index to part),
                scanOrder = currentSession.scanOrder + part.index
            )

            if (updatedSession.scannedCount == updatedSession.total) {
                val setting = decodeProviderSettingParts(updatedSession.parts.values)
                onImportSessionChange(null)
                onAdd(setting)
                onSuccess()
                toaster.show(
                    context.getString(R.string.setting_provider_page_import_success),
                    type = ToastType.Success
                )
            } else {
                onImportSessionChange(updatedSession)
                onProgress()
                toaster.show(
                    context.getString(
                        R.string.setting_provider_page_multi_qr_progress,
                        updatedSession.scannedCount,
                        updatedSession.total
                    ),
                    type = ToastType.Info
                )
            }
        }
    }
}


@Composable
private fun AddButton(
    enableHaptics: Boolean,
    asFab: Boolean = false,
    onClick: () -> Unit
) {
    val haptics = rememberPremiumHaptics(enabled = enableHaptics)
    val handleClick = {
        haptics.perform(HapticPattern.Pop)
        onClick()
    }

    if (asFab) {
        FloatingActionButton(
            onClick = handleClick,
            shape = CircleShape,
            containerColor = Color(0xFF000000),
            contentColor = Color.White,
            modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
        ) {
            Icon(Icons.Rounded.Add, stringResource(R.string.add), tint = Color.White)
        }
    } else {
        IconButton(onClick = handleClick) {
            Icon(Icons.Rounded.Add, stringResource(R.string.add), tint = Color.White)
        }
    }
}

@Composable
private fun ChooseProviderSheet(
    visible: Boolean,
    providerPresets: List<me.lucid.domchat.ui.pages.setting.components.ProviderPreset>,
    onDismissRequest: () -> Unit,
    onSelectPreset: (me.lucid.domchat.ui.pages.setting.components.ProviderPreset) -> Unit,
    onSelectCustom: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val haptics = rememberPremiumHaptics()

    DomChatGlassBottomSheet(
        visible = visible,
        containerColor = Color(0xFF000000),
        maxHeight = (LocalConfiguration.current.screenHeightDp * 0.85f).dp,
        dragHandle = { DomChatSheetDragHandle() },
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .fillMaxHeight(0.85f)
                .clipToBounds()
        ) {
            // Header Row: title + close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.setting_provider_page_choose_provider),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cancel),
                        tint = Color.White
                    )
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.setting_provider_page_search_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(me.lucid.domchat.ui.theme.AppShapes.SearchField)
                    .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SearchField),
                shape = me.lucid.domchat.ui.theme.AppShapes.SearchField,
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear_search))
                        }
                    }
                } else null,
                colors = appFieldColors(),
            )

            Spacer(modifier = Modifier.height(6.dp))

            val filteredPresets = remember(searchQuery, providerPresets) {
                if (searchQuery.isBlank()) {
                    providerPresets
                } else {
                    providerPresets.filter { preset ->
                        preset.name.contains(searchQuery, ignoreCase = true) ||
                        preset.description.contains(searchQuery, ignoreCase = true)
                    }
                }
            }
            val localPreset = filteredPresets.firstOrNull {
                it.type == ProviderSetting.LiteRtLocal::class
            }
            val remotePresets = filteredPresets.filterNot {
                it.type == ProviderSetting.LiteRtLocal::class
            }

            CompositionLocalProvider(
                LocalOverscrollFactory provides null
            ) {
                val lazyListState = rememberLazyListState()
                val canScrollUp by remember {
                    derivedStateOf { lazyListState.canScrollBackward }
                }
                val nestedScrollConnection = remember {
                    object : NestedScrollConnection {
                        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                            if (lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0) {
                                return Offset.Zero
                            }
                            return Offset.Zero
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            .nestedScroll(nestedScrollConnection),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                    ) {

                    localPreset?.let { preset ->
                        item {
                            Surface(
                                onClick = {
                                    haptics.perform(HapticPattern.Pop)
                                    onSelectPreset(preset)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(24.dp)),
                                shape = me.lucid.domchat.ui.theme.AppShapes.CardMedium,
                                color = Color(0xFF000000),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PhoneAndroid,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = Color.White,
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = preset.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                        Text(
                                            text = preset.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.65f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    item {
                        Surface(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                onSelectCustom()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(24.dp)),
                            color = Color(0xFF000000),
                            shape = me.lucid.domchat.ui.theme.AppShapes.CardMedium
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.setting_provider_page_add_custom_provider),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item(key = "preset_nanogpt") {
                        Surface(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                onSelectPreset(NANO_GPT_PRESET)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.CardMedium),
                            shape = me.lucid.domchat.ui.theme.AppShapes.CardMedium,
                            color = Color(0xFF000000),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.nanogpt_logo),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "NanoGPT",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Privacy-first nano billing for many hosted models",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    items(remotePresets.filter { it.name != "NanoGPT" }, key = { preset -> preset.name }) { preset ->
                        val shape = me.lucid.domchat.ui.theme.AppShapes.CardMedium
                        
                        Surface(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                onSelectPreset(preset)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, shape),
                            shape = shape,
                            color = Color(0xFF000000)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AutoAIIconWithUrl(
                                    name = preset.name,
                                    customIconUri = preset.customIconUri,
                                    modifier = Modifier.size(40.dp),
                                    contentColor = Color.White,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (canScrollUp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF000000),
                                        Color(0xFF000000).copy(alpha = 0.75f),
                                        Color(0xFF000000).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun CustomProviderSheet(
    dialogState: me.lucid.domchat.ui.hooks.EditState<ProviderSetting>,
    onDismissRequest: () -> Unit
) {
    DomChatGlassBottomSheet(
        visible = dialogState.isEditing,
        containerColor = Color(0xFF000000),
        dragHandle = { DomChatSheetDragHandle() },
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.setting_provider_page_add_provider),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cancel),
                        tint = Color.White
                    )
                }
            }

            // Scrollable Provider Settings Form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dialogState.currentState?.let {
                    ProviderConfigure(
                        provider = it,
                        showEnabledToggle = false,
                        onEdit = { newState ->
                            dialogState.currentState = newState
                        }
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.CardLarge
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = { dialogState.confirm() },
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.CardLarge
                ) {
                    Text(stringResource(R.string.setting_provider_page_add))
                }
            }
        }
    }
}


@Composable
private fun ProviderItemContent(
    provider: ProviderSetting,
    animatedShape: Shape,
    providerTags: List<DataTag>,
    haptics: me.lucid.domchat.ui.hooks.PremiumHaptics,
    dragHandle: @Composable () -> Unit,
    onClick: () -> Unit
) {
    // Define the normal card color (glassy styling)
    val isDark = me.lucid.domchat.ui.theme.LocalDarkMode.current
    val normalCardColor = Color(0xFF000000)
    val cardBorderSpecular = me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
    val disabledBackground = Color(0xFF000000)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(animatedShape)
            .then(
                if (!provider.enabled) {
                    Modifier
                        .background(disabledBackground, animatedShape)
                        .border(0.5.dp, cardBorderSpecular, animatedShape)
                } else {
                    Modifier
                        .background(normalCardColor, animatedShape)
                        .border(0.5.dp, cardBorderSpecular, animatedShape)
                }
            )
            .clickable {
                haptics.perform(HapticPattern.Pop)
                onClick()
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Desaturation for disabled providers - use Paint with ColorFilter for grayscale
        // IMPORTANT: remember must be called unconditionally (outside of if/else)
        val saturationMatrix = remember { 
            android.graphics.ColorMatrix().apply { setSaturation(0f) } 
        }
        val colorFilter = remember(saturationMatrix) {
            android.graphics.ColorMatrixColorFilter(saturationMatrix)
        }
        val grayscalePaint = remember { 
            android.graphics.Paint().apply {
                this.colorFilter = colorFilter
            }
        }
        
        val grayscaleModifier = if (!provider.enabled) {
            Modifier
                .graphicsLayer { alpha = 0.99f } // Force offscreen buffer
                .drawWithContent {
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.saveLayer(null, grayscalePaint)
                        drawContent()
                        canvas.nativeCanvas.restore()
                    }
                }
        } else {
            Modifier
        }
        
        ProviderIcon(
            provider = provider,
            modifier = Modifier
                .size(40.dp)
                .then(grayscaleModifier)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = provider.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Non-interactive tag row with fixed height - fades to card background at right edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clipToBounds()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.wrapContentWidth(align = Alignment.Start, unbounded = true)
                ) {
                    // Show disabled tag only for disabled providers (gray styling)
                    if (!provider.enabled) {
                        Tag(type = TagType.DEFAULT) {
                            Text(stringResource(R.string.setting_provider_page_disabled))
                        }
                    }
                    Surface(
                        shape = me.lucid.domchat.ui.theme.AppShapes.Tag,
                        color = Color(0xFF000000),
                        border = BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.setting_provider_page_model_count,
                                    provider.models.size
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF2F2F7)
                            )
                        }
                    }
                    if (provider.name == "AiHubMix") {
                        Tag(type = TagType.DEFAULT) {
                            Text("10% off")
                        }
                    }
                    // Show provider's assigned tags
                    provider.tags.forEach { tagId ->
                        providerTags.find { it.id == tagId }?.let { tag ->
                            Tag(type = TagType.DEFAULT) {
                                Text(tag.name)
                            }
                        }
                    }
                }
                // Fade gradient overlay to card background color (matches enabled/disabled state)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(width = 40.dp, height = 24.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    if (provider.enabled) {
                                        normalCardColor
                                    } else {
                                        disabledBackground
                                    }
                                )
                            )
                        )
                )
            }
        }
        dragHandle()
    }
}

@Composable
private fun ProviderTagsFilterRow(
    providerTags: List<DataTag>,
    selectedTagIds: Set<kotlin.uuid.Uuid>,
    onUpdateSelectedTagIds: (Set<kotlin.uuid.Uuid>) -> Unit
) {
    val scrollState = rememberLazyListState()
    val canScrollBackward by remember { derivedStateOf { scrollState.canScrollBackward } }
    val canScrollForward by remember { derivedStateOf { scrollState.canScrollForward } }
    
    LazyRow(
        state = scrollState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                // Left edge fade (only if can scroll backward)
                if (canScrollBackward) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startX = 0f,
                            endX = 24.dp.toPx()
                        ),
                        blendMode = BlendMode.DstOut
                    )
                }
                // Right edge fade (only if can scroll forward)
                if (canScrollForward) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startX = size.width - 24.dp.toPx(),
                            endX = size.width
                        ),
                        blendMode = BlendMode.DstOut
                    )
                }
            }
    ) {
        items(providerTags) { tag ->
            val isSelected = tag.id in selectedTagIds
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = if (isSelected) {
                        selectedTagIds - tag.id
                    } else {
                        selectedTagIds + tag.id
                    }
                    onUpdateSelectedTagIds(newSelection)
                },
                label = { Text(tag.name) },
                shape = me.lucid.domchat.ui.theme.AppShapes.Chip,
                colors = appFilterChipColors(selected = isSelected),
                border = appFilterChipBorder(selected = isSelected)
            )
        }
    }
}





