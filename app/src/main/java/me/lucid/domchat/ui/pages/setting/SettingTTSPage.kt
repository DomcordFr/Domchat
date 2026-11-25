package me.lucid.domchat.ui.pages.setting

import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.motion.pressScale
import me.lucid.domchat.ui.motion.LocalMotionPolicy
import me.lucid.domchat.ui.theme.appFilterChipColors
import me.lucid.domchat.ui.theme.appFilterChipBorder
import me.lucid.domchat.ui.theme.appFieldColors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalConfiguration
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import me.lucid.domchat.ui.components.ui.HapticSwitch
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import me.lucid.domchat.R
import me.lucid.domchat.data.ai.models.ModelCatalogSnapshot
import me.lucid.domchat.data.ai.models.ttsProviderIconUri
import me.lucid.domchat.data.datastore.DEFAULT_SYSTEM_TTS_ID
import me.lucid.domchat.data.datastore.DEFAULT_SYSTEM_TTS_VOICE_ID
import me.lucid.domchat.Screen
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.ui.AutoAIIconWithUrl
import me.lucid.domchat.ui.components.ui.Tag
import me.lucid.domchat.ui.components.ui.TagType
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.context.LocalTTSState
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.pages.setting.components.TTSProviderConfigure
import me.lucid.domchat.utils.plus
import me.lucid.tts.provider.TTSProviderSetting
import me.lucid.tts.provider.withDefaultVoices
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import me.lucid.domchat.ui.modifier.fadeEdges
import me.lucid.domchat.ui.components.ui.ItemPosition
import me.lucid.domchat.ui.components.ui.listItemShape
import me.lucid.domchat.ui.components.ui.DomChatDestructiveConfirmDialog
import me.lucid.domchat.ui.components.ui.PhysicsSwipeToDelete
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.pages.setting.components.TTSProviderIcon
import me.lucid.domchat.ui.pages.setting.components.toTTSProviderPresets
import me.lucid.domchat.ui.pages.setting.components.FALLBACK_TTS_PROVIDER_PRESETS
import me.lucid.domchat.ui.pages.setting.components.toTTSProviderSetting

@Composable
fun SettingTTSPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val catalogSnapshot by vm.modelCatalogSnapshot.collectAsStateWithLifecycle()
    val navController = LocalNavController.current
    val context = LocalContext.current
    var editingProvider by remember { mutableStateOf<TTSProviderSetting?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val scope = rememberCoroutineScope()
    
    // Move lazyListState outside for canScroll detection
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val newProviders = settings.ttsProviders.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        vm.updateSettings(settings.copy(ttsProviders = newProviders))
    }

    // State for TTS text filter settings dialog
    var showFilterSettingsDialog by remember { mutableStateOf(false) }
    var showAddTtsProviderSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = stringResource(R.string.setting_tts_page_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton()
                }
            )
        },
        bottomBar = {
            ProvidersBottomBar(selectedTab = ProvidersTab.Tts) {
                FloatingActionButton(
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        showFilterSettingsDialog = true
                    },
                    shape = AppShapes.CardLarge,
                    containerColor = Color(0xFF000000),
                    contentColor = Color.White,
                    modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, AppShapes.CardLarge)
                ) {
                    Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.setting_tts_settings_title))
                }

                AddTTSProviderButton(
                    enableHaptics = settings.displaySetting.enableUIHaptics,
                    asFab = true,
                    onClick = { showAddTtsProviderSheet = true }
                )
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        // State for swipe neighbor tracking
        var draggingIndex by remember { mutableStateOf(-1) }
        var dragOffset by remember { mutableFloatStateOf(0f) }
        var isUnlocked by remember { mutableStateOf(false) }
        var neighborsUnlocked by remember { mutableStateOf(false) }
        
        
        val density = androidx.compose.ui.platform.LocalDensity.current
        
        // Reset neighborsUnlocked when offset returns to 0
        if (dragOffset == 0f && neighborsUnlocked) {
            neighborsUnlocked = false
        }
        

        
        // Delete confirmation state
        var showDeleteDialog by remember { mutableStateOf(false) }
        var providerToDelete by remember { mutableStateOf<TTSProviderSetting?>(null) }
        
        // TTS error state - show as toast notification
        val tts = LocalTTSState.current
        val ttsError by tts.error.collectAsState()
        val toaster = LocalToaster.current
        
        // Show error toast when TTS error changes
        LaunchedEffect(ttsError) {
            ttsError?.let { errorMessage ->
                toaster.show(
                    message = context.getString(R.string.setting_tts_error, errorMessage),
                    type = ToastType.Error
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentPadding = innerPadding + PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = lazyListState
            ) {
                if (settings.ttsProviders.isEmpty()) {
                    item(key = "empty") {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF000000)
                            ),
                            shape = AppShapes.CardLarge,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
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
                                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.White.copy(alpha = 0.65f).copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = stringResource(R.string.setting_tts_page_empty_title),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = stringResource(R.string.setting_tts_page_empty_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.65f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                itemsIndexed(settings.ttsProviders, key = { _, provider -> provider.id }) { index, provider ->
                val position = when {
                    settings.ttsProviders.size == 1 -> ItemPosition.ONLY
                    index == 0 -> ItemPosition.FIRST
                    index == settings.ttsProviders.lastIndex -> ItemPosition.LAST
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
                    key = provider.id,
                    animateItemModifier = Modifier.animateItem()
                ) { isDragging ->
                    val motionPolicy = LocalMotionPolicy.current
                    val dragScale by animateFloatAsState(
                        targetValue = if (isDragging) 0.95f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = motionPolicy.pressStiffness(300f)),
                        label = "tts_provider_drag_scale"
                    )
                    key(provider.id) {
                        PhysicsSwipeToDelete(
                            position = position,
                            groupCornerRadius = 24.dp,
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
                                providerToDelete = provider
                                showDeleteDialog = true
                            },
                            modifier = Modifier
                                .scale(dragScale)
                                .fillMaxWidth()
                        ) { _ ->
                        TTSProviderItemContent(
                            provider = provider,
                            catalogSnapshot = catalogSnapshot,
                            itemShape = position.listItemShape(),
                            haptics = haptics,
                            onClick = {
                                navController.navigate(Screen.SettingTTSProviderDetail(provider.id.toString()))
                            },
                            dragHandle = {
                                IconButton(
                                    onClick = {},
                                    modifier = Modifier.longPressDraggableHandle(
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
                            }
                        )
                    }
                    }
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
        
        DomChatDestructiveConfirmDialog(
            visible = showDeleteDialog && providerToDelete != null,
            title = stringResource(R.string.setting_tts_delete_title),
            consequence = stringResource(R.string.setting_tts_delete_consequence),
            onDismiss = {
                showDeleteDialog = false
                providerToDelete = null
            },
            onConfirm = {
                val p = providerToDelete
                if (p != null) {
                    val removedVoiceIds = p.voices.map { it.id }.toSet()
                    val newProviders = settings.ttsProviders - p
                    val newSelectedId =
                        if (settings.selectedTTSProviderId == p.id) {
                            newProviders.firstOrNull()?.id ?: DEFAULT_SYSTEM_TTS_ID
                        } else {
                            settings.selectedTTSProviderId
                        }
                    val newSelectedVoiceId = if (settings.selectedTTSVoiceId in removedVoiceIds) {
                        newProviders.find { it.id == newSelectedId }?.voices?.firstOrNull()?.id
                            ?: newProviders.firstOrNull()?.voices?.firstOrNull()?.id
                            ?: DEFAULT_SYSTEM_TTS_VOICE_ID
                    } else {
                        settings.selectedTTSVoiceId
                    }
                    vm.updateSettings(settings.copy(
                        ttsProviders = newProviders,
                        selectedTTSProviderId = newSelectedId,
                        selectedTTSVoiceId = newSelectedVoiceId
                    ))
                }
                showDeleteDialog = false
                providerToDelete = null
            },
        )
    }

    // Edit TTS Provider Bottom Sheet
    editingProvider?.let { provider ->
        var currentProvider by remember(provider) { mutableStateOf(provider) }
        val tts = LocalTTSState.current

        DomChatInWindowBottomSheet(
            containerColor = Color(0xFF000000),
            maxHeight = (LocalConfiguration.current.screenHeightDp * 0.85f).dp,
            onDismissRequest = {
                editingProvider = null
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .fillMaxHeight(0.8f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.setting_tts_page_edit_provider),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                tts.speak(
                                    text = context.getString(R.string.setting_tts_test_voice_preview),
                                    overrideSetting = currentProvider
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = stringResource(R.string.setting_tts_test_voice)
                        )
                    }
                }


                TTSProviderConfigure(
                    setting = currentProvider,
                    onValueChange = { newState ->
                        currentProvider = newState
                    },
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            editingProvider = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    TextButton(
                        onClick = {
                            val newProviders = settings.ttsProviders.map {
                                if (it.id == provider.id) currentProvider else it
                            }
                            vm.updateSettings(settings.copy(ttsProviders = newProviders))
                            editingProvider = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.chat_page_save))
                    }
                }
            }
        }
    }
    
    // TTS Text Filter Settings Dialog
    if (showFilterSettingsDialog) {
        TtsTextFilterSettingsDialog(
            rules = settings.displaySetting.ttsTextFilterRules,
            onDismiss = { showFilterSettingsDialog = false },
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

    if (showAddTtsProviderSheet) {
        AddTTSProviderSheet(
            visible = true,
            catalogSnapshot = catalogSnapshot,
            onDismissRequest = { showAddTtsProviderSheet = false },
            onAdd = { newProvider ->
                vm.updateSettings(
                    settings.copy(
                        ttsProviders = listOf(newProvider.withDefaultVoices()) + settings.ttsProviders
                    )
                )
                showAddTtsProviderSheet = false
            }
        )
    }
}

@Composable
internal fun TtsProvidersContent(
    vm: SettingVM = koinViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val catalogSnapshot by vm.modelCatalogSnapshot.collectAsStateWithLifecycle()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val lazyListState = rememberLazyListState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val newProviders = settings.ttsProviders.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        vm.updateSettings(settings.copy(ttsProviders = newProviders))
    }

    var editingProvider by remember { mutableStateOf<TTSProviderSetting?>(null) }
    var draggingIndex by remember { mutableStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isUnlocked by remember { mutableStateOf(false) }
    var neighborsUnlocked by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var providerToDelete by remember { mutableStateOf<TTSProviderSetting?>(null) }

    if (dragOffset == 0f && neighborsUnlocked) {
        neighborsUnlocked = false
    }

    val tts = LocalTTSState.current
    val ttsError by tts.error.collectAsState()
    val toaster = LocalToaster.current

    LaunchedEffect(ttsError) {
        ttsError?.let { errorMessage ->
            toaster.show(
                message = context.getString(R.string.setting_tts_error, errorMessage),
                type = ToastType.Error
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = contentPadding + PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            state = lazyListState
        ) {
            if (settings.ttsProviders.isEmpty()) {
                item(key = "empty") {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF000000)
                        ),
                        shape = AppShapes.CardLarge,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
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
                                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.White.copy(alpha = 0.65f).copy(alpha = 0.5f)
                                )
                                Text(
                                    text = stringResource(R.string.setting_tts_page_empty_title),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(R.string.setting_tts_page_empty_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.65f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
            itemsIndexed(settings.ttsProviders, key = { _, provider -> provider.id }) { index, provider ->
                val position = when {
                    settings.ttsProviders.size == 1 -> ItemPosition.ONLY
                    index == 0 -> ItemPosition.FIRST
                    index == settings.ttsProviders.lastIndex -> ItemPosition.LAST
                    else -> ItemPosition.MIDDLE
                }

                val thresholdPx = with(density) { 35.dp.toPx() }
                if (draggingIndex >= 0 && !neighborsUnlocked && kotlin.math.abs(dragOffset) >= thresholdPx) {
                    neighborsUnlocked = true
                }

                val shouldNeighborFollow = draggingIndex >= 0 &&
                    draggingIndex != index &&
                    !isUnlocked &&
                    !neighborsUnlocked

                val neighborOffset = if (shouldNeighborFollow) {
                    when (kotlin.math.abs(index - draggingIndex)) {
                        1 -> dragOffset * 0.35f
                        2 -> dragOffset * 0.12f
                        else -> 0f
                    }
                } else {
                    0f
                }

                ReorderableItem(
                    state = reorderableState,
                    key = provider.id,
                    animateItemModifier = Modifier.animateItem()
                ) { isDragging ->
                    val motionPolicy = LocalMotionPolicy.current
                    val dragScale by animateFloatAsState(
                        targetValue = if (isDragging) 0.95f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = motionPolicy.pressStiffness(300f)),
                        label = "tts_provider_drag_scale"
                    )
                    key(provider.id) {
                        PhysicsSwipeToDelete(
                            position = position,
                            groupCornerRadius = 24.dp,
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
                                providerToDelete = provider
                                showDeleteDialog = true
                            },
                            modifier = Modifier
                                .scale(dragScale)
                                .fillMaxWidth()
                        ) { _ ->
                            TTSProviderItemContent(
                                provider = provider,
                                catalogSnapshot = catalogSnapshot,
                                itemShape = position.listItemShape(),
                                haptics = haptics,
                                onClick = {
                                    navController.navigate(Screen.SettingTTSProviderDetail(provider.id.toString()))
                                },
                                dragHandle = {
                                    IconButton(
                                        onClick = {},
                                        modifier = Modifier.longPressDraggableHandle(
                                            onDragStarted = { haptics.perform(HapticPattern.Pop) },
                                            onDragStopped = { haptics.perform(HapticPattern.Thud) }
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DragIndicator,
                                            contentDescription = null
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

    DomChatDestructiveConfirmDialog(
        visible = showDeleteDialog && providerToDelete != null,
        title = stringResource(R.string.setting_tts_delete_title),
        consequence = stringResource(R.string.setting_tts_delete_consequence),
        onDismiss = {
            showDeleteDialog = false
            providerToDelete = null
        },
        onConfirm = {
            val provider = providerToDelete
            if (provider != null) {
                val newProviders = settings.ttsProviders - provider
                val newSelectedId =
                    if (settings.selectedTTSProviderId == provider.id) {
                        newProviders.firstOrNull()?.id ?: DEFAULT_SYSTEM_TTS_ID
                    } else {
                        settings.selectedTTSProviderId
                    }
                val removedVoiceIds = provider.voices.map { it.id }.toSet()
                val newSelectedVoiceId = if (settings.selectedTTSVoiceId in removedVoiceIds) {
                    newProviders.find { it.id == newSelectedId }?.voices?.firstOrNull()?.id
                        ?: newProviders.firstOrNull()?.voices?.firstOrNull()?.id
                        ?: DEFAULT_SYSTEM_TTS_VOICE_ID
                } else {
                    settings.selectedTTSVoiceId
                }
                vm.updateSettings(
                    settings.copy(
                        ttsProviders = newProviders,
                        selectedTTSProviderId = newSelectedId,
                        selectedTTSVoiceId = newSelectedVoiceId
                    )
                )
            }
            showDeleteDialog = false
            providerToDelete = null
        },
    )

    TtsProviderEditorSheet(
        provider = editingProvider,
        onDismiss = { editingProvider = null },
        onSave = { original, updated ->
            val newProviders = settings.ttsProviders.map {
                if (it.id == original.id) updated else it
            }
            vm.updateSettings(settings.copy(ttsProviders = newProviders))
            editingProvider = null
        }
    )
}

@Composable
private fun TtsProviderEditorSheet(
    provider: TTSProviderSetting?,
    onDismiss: () -> Unit,
    onSave: (TTSProviderSetting, TTSProviderSetting) -> Unit
) {
    provider ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentProvider by remember(provider) { mutableStateOf(provider) }
    val tts = LocalTTSState.current

    DomChatInWindowBottomSheet(
        containerColor = Color(0xFF000000),
        maxHeight = (LocalConfiguration.current.screenHeightDp * 0.85f).dp,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .fillMaxHeight(0.8f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.setting_tts_page_edit_provider),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            tts.speak(
                                text = context.getString(R.string.setting_tts_test_voice_preview),
                                overrideSetting = currentProvider
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = stringResource(R.string.setting_tts_test_voice)
                    )
                }
            }

            TTSProviderConfigure(
                setting = currentProvider,
                onValueChange = { currentProvider = it },
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }

                TextButton(
                    onClick = { onSave(provider, currentProvider) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.chat_page_save))
                }
            }
        }
    }
}

@Composable
internal fun TtsTextFilterSettingsDialog(
    rules: List<me.lucid.domchat.data.datastore.TtsTextFilterRule>,
    onDismiss: () -> Unit,
    onUpdateRules: (List<me.lucid.domchat.data.datastore.TtsTextFilterRule>) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<me.lucid.domchat.data.datastore.TtsTextFilterRule?>(null) }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color(0xFF000000),
        shape = me.lucid.domchat.ui.theme.AppShapes.SettingDialog,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SettingDialog),
        titleContentColor = Color(0xFFF2F2F7),
        textContentColor = Color(0xFFF2F2F7),
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Text Patterns",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.setting_tts_filter_add_rule))
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.Card(
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Color(0xFF000000)
                    ),
                    shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
                ) {
                    Text(
                        text = stringResource(R.string.setting_tts_filter_desc),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rules.isEmpty()) {
                        item {
                            androidx.compose.material3.Card(
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = Color(0xFF000000)
                                ),
                                shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.setting_tts_filter_empty),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.65f),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(onClick = { showAddDialog = true }) {
                                        Icon(
                                            Icons.Rounded.Add,
                                            contentDescription = stringResource(R.string.setting_tts_filter_add_rule)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(rules, key = { it.id }) { rule ->
                            TtsFilterRuleItem(
                                rule = rule,
                                onToggle = { enabled ->
                                    onUpdateRules(rules.map {
                                        if (it.id == rule.id) it.copy(enabled = enabled) else it
                                    })
                                },
                                onEdit = { editingRule = rule },
                                onDelete = {
                                    onUpdateRules(rules.filter { it.id != rule.id })
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF000000),
                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
            ) {
                Text(
                    text = stringResource(android.R.string.ok),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    )
    
    // Add/Edit Dialog
    if (showAddDialog || editingRule != null) {
        TtsFilterRuleEditDialog(
            rule = editingRule,
            onDismiss = {
                showAddDialog = false
                editingRule = null
            },
            onSave = { newRule ->
                val ruleToEdit = editingRule
                if (ruleToEdit != null) {
                    onUpdateRules(rules.map {
                        if (it.id == ruleToEdit.id) newRule else it
                    })
                } else {
                    onUpdateRules(rules + newRule)
                }
                showAddDialog = false
                editingRule = null
            }
        )
    }
}

@Composable
private fun TtsFilterRuleItem(
    rule: me.lucid.domchat.data.datastore.TtsTextFilterRule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val modeText = when (rule.mode) {
        me.lucid.domchat.data.datastore.TtsFilterMode.SKIP -> stringResource(R.string.setting_tts_filter_skip)
        me.lucid.domchat.data.datastore.TtsFilterMode.ONLY_READ -> stringResource(R.string.setting_tts_filter_only_read)
    }
    val modeColor = when (rule.mode) {
        me.lucid.domchat.data.datastore.TtsFilterMode.SKIP -> Color(0xFFFF453A)
        me.lucid.domchat.data.datastore.TtsFilterMode.ONLY_READ -> Color.White
    }
    
    androidx.compose.material3.Card(
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color(0xFF000000)
        ),
        shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
        onClick = onEdit
    ) {
        androidx.compose.material3.ListItem(
            colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            headlineContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.setting_rp_custom_preview, rule.pattern))
                }
            },
            supportingContent = {
                Text(
                    text = modeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = modeColor
                )
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Rounded.Delete,
                            stringResource(R.string.setting_tts_filter_delete),
                            tint = Color(0xFFFF453A)
                        )
                    }
                    HapticSwitch(
                        checked = rule.enabled,
                        onCheckedChange = onToggle
                    )
                }
            }
        )
    }
}

@Composable
private fun TtsFilterRuleEditDialog(
    rule: me.lucid.domchat.data.datastore.TtsTextFilterRule?,
    onDismiss: () -> Unit,
    onSave: (me.lucid.domchat.data.datastore.TtsTextFilterRule) -> Unit
) {
    var pattern by remember { mutableStateOf(rule?.pattern ?: "*") }
    var mode by remember { mutableStateOf(rule?.mode ?: me.lucid.domchat.data.datastore.TtsFilterMode.SKIP) }
    
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color(0xFF000000),
        shape = me.lucid.domchat.ui.theme.AppShapes.SettingDialog,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SettingDialog),
        titleContentColor = Color(0xFFF2F2F7),
        textContentColor = Color(0xFFF2F2F7),
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (rule != null) {
                    stringResource(R.string.setting_tts_filter_edit_rule)
                } else {
                    stringResource(R.string.setting_tts_filter_add_rule)
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text(stringResource(R.string.setting_tts_filter_pattern)) },
                    placeholder = { Text(stringResource(R.string.setting_tts_filter_pattern_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(stringResource(R.string.setting_tts_filter_pattern_desc, pattern))
                    },
                    shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
                    colors = appFieldColors(),
                )
                
                // Mode selector
                Text(
                    text = stringResource(R.string.setting_tts_filter_mode),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.FilterChip(
                        selected = mode == me.lucid.domchat.data.datastore.TtsFilterMode.SKIP,
                        onClick = { mode = me.lucid.domchat.data.datastore.TtsFilterMode.SKIP },
                        label = { Text(stringResource(R.string.setting_tts_filter_skip)) },
                        colors = appFilterChipColors(selected = mode == me.lucid.domchat.data.datastore.TtsFilterMode.SKIP),
                        border = appFilterChipBorder(selected = mode == me.lucid.domchat.data.datastore.TtsFilterMode.SKIP),
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.material3.FilterChip(
                        selected = mode == me.lucid.domchat.data.datastore.TtsFilterMode.ONLY_READ,
                        onClick = { mode = me.lucid.domchat.data.datastore.TtsFilterMode.ONLY_READ },
                        label = { Text(stringResource(R.string.setting_tts_filter_only_read)) },
                        colors = appFilterChipColors(selected = mode == me.lucid.domchat.data.datastore.TtsFilterMode.ONLY_READ),
                        border = appFilterChipBorder(selected = mode == me.lucid.domchat.data.datastore.TtsFilterMode.ONLY_READ),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Mode description
                Text(
                    text = when (mode) {
                        me.lucid.domchat.data.datastore.TtsFilterMode.SKIP -> stringResource(
                            R.string.setting_tts_filter_skip_desc,
                            pattern
                        )
                        me.lucid.domchat.data.datastore.TtsFilterMode.ONLY_READ -> stringResource(
                            R.string.setting_tts_filter_only_read_desc,
                            pattern
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (pattern.isNotBlank()) {
                        onSave(
                            me.lucid.domchat.data.datastore.TtsTextFilterRule(
                                id = rule?.id ?: kotlin.uuid.Uuid.random().toString(),
                                pattern = pattern,
                                mode = mode,
                                enabled = rule?.enabled ?: true
                            )
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
@Composable
internal fun AddTTSProviderButton(
    enableHaptics: Boolean = true,
    asFab: Boolean = false,
    onClick: (() -> Unit)? = null,
    catalogSnapshot: ModelCatalogSnapshot? = null,
    onAdd: ((TTSProviderSetting) -> Unit)? = null
) {
    var showInternalSheet by remember { mutableStateOf(false) }
    val haptics = rememberPremiumHaptics(enabled = enableHaptics)

    val handleOpen = {
        haptics.perform(HapticPattern.Pop)
        if (onClick != null) {
            onClick()
        } else {
            showInternalSheet = true
        }
    }

    if (asFab) {
        FloatingActionButton(
            onClick = handleOpen,
            shape = CircleShape,
            containerColor = Color(0xFF000000),
            contentColor = Color.White,
            modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
        ) {
            Icon(Icons.Rounded.Add, stringResource(R.string.setting_tts_page_add_provider_content_description), tint = Color.White)
        }
    } else {
        IconButton(onClick = handleOpen) {
            Icon(Icons.Rounded.Add, stringResource(R.string.setting_tts_page_add_provider_content_description), tint = Color.White)
        }
    }

    if (onClick == null && onAdd != null) {
        AddTTSProviderSheet(
            visible = showInternalSheet,
            catalogSnapshot = catalogSnapshot,
            onDismissRequest = { showInternalSheet = false },
            onAdd = onAdd
        )
    }
}

@Composable
internal fun AddTTSProviderSheet(
    visible: Boolean,
    catalogSnapshot: ModelCatalogSnapshot?,
    onDismissRequest: () -> Unit,
    onAdd: (TTSProviderSetting) -> Unit
) {
    if (!visible) return
    var searchQuery by remember { mutableStateOf("") }
    val haptics = rememberPremiumHaptics()

    val allTtsPresets = remember(catalogSnapshot) {
        catalogSnapshot?.toTTSProviderPresets() ?: FALLBACK_TTS_PROVIDER_PRESETS
    }        
    // Filter presets based on search
    val filteredPresets = if (searchQuery.isBlank()) {
        allTtsPresets
    } else {
        allTtsPresets.filter { preset ->
            preset.name.contains(searchQuery, ignoreCase = true) ||
                preset.description.contains(searchQuery, ignoreCase = true)
        }
    }
    
    DomChatInWindowBottomSheet(
        containerColor = Color(0xFF000000),
        maxHeight = (LocalConfiguration.current.screenHeightDp * 0.85f).dp,
        onDismissRequest = onDismissRequest,
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
                    text = stringResource(R.string.setting_tts_page_add_provider),
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
                colors = appFieldColors(),
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear_search))
                        }
                    }
                } else null
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            CompositionLocalProvider(
                LocalOverscrollFactory provides null
            ) {
                val lazyListState = rememberLazyListState()
                val canScrollUp by remember { derivedStateOf { lazyListState.canScrollBackward } }
                val canScrollDown by remember { derivedStateOf { lazyListState.canScrollForward } }
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
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .weight(1f)
                        .clipToBounds()
                        .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown)
                        .nestedScroll(nestedScrollConnection),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                ) {
                    item {
                        Card(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                onAdd(TTSProviderSetting.OpenAI(name = "Custom TTS"))
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF000000),
                                contentColor = Color(0xFFF2F2F7)
                            ),
                            shape = me.lucid.domchat.ui.theme.AppShapes.CardMedium,
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
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
                                    tint = Color(0xFFFFFFFF)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Custom OpenAI-compatible TTS",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFFFFFFFF)
                                    )
                                }
                            }
                        }
                    }
                    itemsIndexed(filteredPresets, key = { _, preset -> preset.name }) { index, preset ->
                        val shape = AppShapes.CardMedium

                        Surface(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                val newProvider = preset.toTTSProviderSetting()
                                onAdd(newProvider)
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = shape,
                            color = androidx.compose.ui.graphics.Color(0xFF000000),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (preset.isLocal) {
                                    Box(
                                        modifier = Modifier.size(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PhoneAndroid,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.White,
                                        )
                                    }
                                } else {
                                    AutoAIIconWithUrl(
                                        name = preset.name,
                                        customIconUri = preset.customIconUri,
                                        modifier = Modifier.size(40.dp),
                                        contentColor = Color.White,
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (preset.isLocal) {
                                        Tag(type = TagType.SUCCESS) {
                                            Text(stringResource(R.string.local_label))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
private fun TTSProviderItemContent(
    provider: TTSProviderSetting,
    catalogSnapshot: ModelCatalogSnapshot?,
    itemShape: androidx.compose.ui.graphics.Shape,
    haptics: me.lucid.domchat.ui.hooks.PremiumHaptics,
    onClick: () -> Unit,
    dragHandle: @Composable () -> Unit
) {
    val interactions = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val motionPolicy = LocalMotionPolicy.current
    val backgroundColor by androidx.compose.animation.animateColorAsState(
        targetValue = Color(0xFF000000),
        animationSpec = spring(dampingRatio = 0.6f, stiffness = motionPolicy.pressStiffness(400f)),
        label = "ttsProviderBackground"
    )
    val textColor by androidx.compose.animation.animateColorAsState(
        targetValue = Color.White,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = motionPolicy.pressStiffness(400f)),
        label = "ttsProviderTextColor"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(itemShape)
            .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, itemShape)
            .background(backgroundColor)
            .pressScale(interactions)
            .clickable(
                interactionSource = interactions,
                indication = null
            ) {
                haptics.perform(HapticPattern.Pop)
                onClick()
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TTSProviderIcon(
            provider = provider,
            catalogSnapshot = catalogSnapshot,
            modifier = Modifier.size(40.dp),
            tint = textColor
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = provider.name.ifEmpty { stringResource(R.string.setting_tts_page_default_name) },
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (provider is TTSProviderSetting.SystemTTS || provider.id == me.lucid.domchat.data.datastore.DEFAULT_SYSTEM_TTS_ID) {
                    Tag(type = TagType.SUCCESS) {
                        Text(stringResource(R.string.local_label))
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
                            text = "${provider.voices.size} voices",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        dragHandle()
    }
}

