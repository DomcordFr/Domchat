package me.lucid.domchat.ui.components.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import me.lucid.domchat.ui.components.chat.LocalSheetDismiss
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import me.lucid.domchat.R
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.modifier.fadeEdges
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatFloatingSurfaceColor
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material.icons.rounded.Mic
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.lucid.ai.provider.Modality
import me.lucid.ai.provider.Model
import me.lucid.ai.provider.ModelAbility
import me.lucid.ai.provider.ModelType
import me.lucid.ai.provider.ProviderSetting
import me.lucid.domchat.Screen
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.datastore.findModelById
import me.lucid.domchat.data.datastore.findProvider
import me.lucid.domchat.ui.components.ui.AutoAIIcon
import me.lucid.domchat.ui.components.ui.AutoAIIconWithUrl
import me.lucid.domchat.ui.components.ui.EmptyStateCard
import me.lucid.domchat.ui.components.ui.ProviderIcon
import me.lucid.domchat.ui.components.ui.ModelIcon
import me.lucid.domchat.ui.components.ui.Tag
import me.lucid.domchat.ui.components.ui.TagType
import me.lucid.domchat.ui.components.ui.icons.HeartIcon
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.extendColors
import me.lucid.domchat.ui.components.settings.DomChatGroupedModelRow
import me.lucid.domchat.ui.components.settings.DomChatModelGroupPosition
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.utils.toDp
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.roundToInt
import kotlin.uuid.Uuid
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height


@Composable
fun ModelSelector(
    modelId: Uuid?,
    providers: List<ProviderSetting>,
    type: ModelType,
    modifier: Modifier = Modifier,
    onlyIcon: Boolean = false,
    allowClear: Boolean = false,
    allowBackendModels: Boolean = false,
    modelFilter: (Model) -> Boolean = { true },
    onClear: (() -> Unit)? = null,
    onSelect: (Model) -> Unit
) {
    var popup by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptics = rememberPremiumHaptics()
    val model = providers.findModelById(modelId ?: Uuid.random())
    // Backend visibility only applies to chat models. Embedding, STT, and image models are chosen
    // from feature-specific settings rather than the chat picker.
    val effectiveModelFilter: (Model) -> Boolean = { m ->
        (type != ModelType.CHAT || allowBackendModels || !m.backend) && modelFilter(m)
    }

    if (!onlyIcon) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f)),
        ) {
            TextButton(
                onClick = {
                    popup = true
                },
                modifier = modifier
            ) {
                AnimatedContent(
                    targetState = model,
                    transitionSpec = {
                        (fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) +
                            scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)))
                            .togetherWith(
                                fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) +
                                    scaleOut(targetScale = 0.92f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
                            )
                    },
                    label = "model_selector_content"
                ) { currentModelItem ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        currentModelItem?.let { m ->
                            val provider = m.findProvider(providers = providers)
                            ModelIcon(
                                model = m,
                                provider = provider,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(36.dp)
                            )
                        }
                        Text(
                            text = currentModelItem?.displayName ?: stringResource(R.string.model_list_select_model),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = allowClear && model != null,
                enter = fadeIn(spring(dampingRatio = 0.8f, stiffness = 400f)) + expandHorizontally(spring(dampingRatio = 0.8f, stiffness = 400f)),
                exit = fadeOut(spring(dampingRatio = 0.8f, stiffness = 400f)) + shrinkHorizontally(spring(dampingRatio = 0.8f, stiffness = 400f))
            ) {
                IconButton(
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        onClear?.invoke() ?: onSelect(Model())
                    }
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.clear_search)
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .clickable { popup = true },
            contentAlignment = Alignment.Center
        ) {
            if (model != null) {
                val provider = model.findProvider(providers = providers)
                ModelIcon(
                    model = model,
                    provider = provider,
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                )
            } else {
                Icon(
                    Icons.Rounded.ViewModule,
                    contentDescription = stringResource(R.string.setting_model_page_chat_model),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (popup) {
        val sheetTitle = when (type) {
            ModelType.CHAT -> "Select Chat Model"
            ModelType.EMBEDDING -> "Select Embedding Model"
            ModelType.IMAGE -> "Select Image Model"
            ModelType.STT -> "Select Speech Model"
        }
        Dialog(
            onDismissRequest = { popup = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            DomChatInWindowBottomSheet(
                visible = true,
                onDismissRequest = {
                    popup = false
                },
                containerColor = Color(0xFF000000),
                dragHandle = { me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle() },
                maxHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * 0.94f).dp,
            ) {
                val sheetDismiss = LocalSheetDismiss.current
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.94f)
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DomChatSheetHeader(
                        title = sheetTitle,
                        onCloseClick = { sheetDismiss?.invoke() ?: run { popup = false } },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    val modelMetadataResolver = org.koin.compose.koinInject<me.lucid.domchat.data.ai.models.ModelMetadataResolver>()
                    val filteredProviderSettings = providers.fastFilter {
                        it.enabled && it.models.fastAny { model ->
                            val isTypeMatch = if (type == ModelType.STT) {
                                val resolvedModel = modelMetadataResolver.applyToModel(model, it)
                                resolvedModel.type == ModelType.STT
                            } else {
                                model.type == type
                            }
                            isTypeMatch && effectiveModelFilter(model)
                        }
                    }
                    ModelList(
                        currentModel = modelId,
                        providers = filteredProviderSettings,
                        modelType = type,
                        modelFilter = effectiveModelFilter,
                        onSelect = {
                            onSelect(it)
                            sheetDismiss?.invoke() ?: run { popup = false }
                        },
                        onDismiss = {
                            sheetDismiss?.invoke() ?: run { popup = false }
                        }
                    )
                }
            }
        }
    }
}

enum class ModelPickerStyle {
    SETTINGS,
    LIQUID_GLASS,
}

@OptIn(kotlinx.coroutines.FlowPreview::class)
@Composable
internal fun ColumnScope.ModelList(
    currentModel: Uuid? = null,
    providers: List<ProviderSetting>,
    modelType: ModelType,
    modelFilter: (Model) -> Boolean = { true },
    style: ModelPickerStyle = ModelPickerStyle.SETTINGS,
    onSelect: (Model) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val settingsStore = koinInject<SettingsStore>()
    val settings = settingsStore.settingsFlow
        .collectAsStateWithLifecycle()
    val haptics = rememberPremiumHaptics(enabled = settings.value.displaySetting.enableUIHaptics)
    val modelMetadataResolver = org.koin.compose.koinInject<me.lucid.domchat.data.ai.models.ModelMetadataResolver>()
    
    var activeProviderId by remember { mutableStateOf<Uuid?>(null) }
    
    // activeProviderId is null initially and is set upon explicit user click

    val displayedFavoriteIds = settings.value.favoriteModels

    val favoriteModels = displayedFavoriteIds.mapNotNull { modelId ->
        val model = providers.findModelById(modelId) ?: return@mapNotNull null

        val provider = model.findProvider(providers = providers, checkOverwrite = false) ?: return@mapNotNull null
        val isTypeMatch = if (modelType == ModelType.STT) {
            val resolvedModel = modelMetadataResolver.applyToModel(model, provider)
            resolvedModel.type == ModelType.STT
        } else {
            model.type == modelType
        }
        if (!isTypeMatch || !modelFilter(model)) return@mapNotNull null
        model to provider
    }
    val favoriteListItems = remember(favoriteModels) {
        favoriteModels.mapIndexed { index, (model, provider) ->
            FavoriteListItem(
                model = model,
                provider = provider,
                position = groupItemPosition(index = index, groupSize = favoriteModels.size)
            )
        }
    }

    var searchKeywords by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(ModelSortOrder.DEFAULT) }

    val recentModels = remember(settings.value.recentModelIds, providers, modelType, modelFilter) {
        settings.value.recentModelIds.mapNotNull { modelId ->
            val model = providers.findModelById(modelId) ?: return@mapNotNull null
            val provider = model.findProvider(providers = providers, checkOverwrite = false) ?: return@mapNotNull null
            val isTypeMatch = if (modelType == ModelType.STT) {
                val resolvedModel = modelMetadataResolver.applyToModel(model, provider)
                resolvedModel.type == ModelType.STT
            } else {
                model.type == modelType
            }
            if (!isTypeMatch || !modelFilter(model)) return@mapNotNull null
            model to provider
        }.filterNot { (model, _) -> favoriteModels.any { it.first.id == model.id } }
    }
    val recentListItems = remember(recentModels) {
        recentModels.mapIndexed { index, (model, provider) ->
            FavoriteListItem(
                model = model,
                provider = provider,
                position = groupItemPosition(index = index, groupSize = recentModels.size)
            )
        }
    }

    val handleSelect: (Model) -> Unit = { model ->
        haptics.perform(HapticPattern.Pop)
        val currentRecents = settings.value.recentModelIds
        val updatedRecents = (listOf(model.id) + currentRecents.filter { it != model.id }).take(12)
        coroutineScope.launch {
            settingsStore.update { it.copy(recentModelIds = updatedRecents) }
        }
        onSelect(model)
    }

    // Build a flat list of items for the LazyColumn - this enables precise scrolling to any model
    // Structure: [provider header, model, model, ...] for each provider
    
    val providerListItems = remember(providers, modelType, searchKeywords, settings.value.favoriteModels, modelFilter, sortOrder) {
        buildList {
            providers.forEach { providerSetting ->
                val filteredModels = providerSetting.models.fastFilter { model ->
                    val isTypeMatch = if (modelType == ModelType.STT) {
                        val resolvedModel = modelMetadataResolver.applyToModel(model, providerSetting)
                        resolvedModel.type == ModelType.STT
                    } else {
                        model.type == modelType
                    }
                    val matchesSearch = searchKeywords.isBlank() ||
                        model.displayName.contains(searchKeywords, true) ||
                        model.modelId.contains(searchKeywords, true) ||
                        providerSetting.name.contains(searchKeywords, true)
                    isTypeMatch && modelFilter(model) && matchesSearch
                }.let { list ->
                    when (sortOrder) {
                        ModelSortOrder.DEFAULT -> list
                        ModelSortOrder.NAME -> list.sortedBy { it.displayName.lowercase() }
                        ModelSortOrder.CONTEXT -> list.sortedByDescending { it.contextWindowTokens ?: 0 }
                    }
                }
                
                // Add provider header if provider has matching models or search is empty
                if (filteredModels.isNotEmpty()) {
                    add(ProviderListItem.Header(providerSetting))
                    
                    // Add each model as individual item
                    filteredModels.forEachIndexed { index, model ->
                        add(ProviderListItem.ModelEntry(
                            model = model,
                            provider = providerSetting,
                            position = groupItemPosition(index = index, groupSize = filteredModels.size),
                            isFavorite = settings.value.favoriteModels.contains(model.id)
                        ))
                    }
                }
            }
        }
    }
    
    // Calculate position of selected model in the flat list
    val selectedModelPosition = remember(currentModel, favoriteModels, providerListItems) {
        if (currentModel == null) return@remember 0

        var position = 0

        // Skip no-providers placeholder
        if (providers.isEmpty()) {
            position += 1
        }

        // Check if in favorites list - favorites are individual items
        val favoriteIndex = favoriteModels.indexOfFirst { it.first.id == currentModel }
        if (favoriteIndex >= 0) {
            if (favoriteModels.isNotEmpty()) {
                position += 1 // favorite header
            }
            position += favoriteIndex
            return@remember position
        }

        // Skip all favorites
        if (favoriteModels.isNotEmpty()) {
            position += 1 // favorite header
            position += favoriteModels.size
        }

        // Find the model in the flat provider list
        val modelIndexInProviderList = providerListItems.indexOfFirst { item ->
            item is ProviderListItem.ModelEntry && item.model.id == currentModel
        }
        if (modelIndexInProviderList >= 0) {
            return@remember position + modelIndexInProviderList
        }

        0
    }

    // List state for scrolling
    val lazyListState = rememberLazyListState()
    
    // Get viewport height for centering calculation
    val density = androidx.compose.ui.platform.LocalDensity.current
    var viewportHeight by remember { mutableStateOf(0) }
    
    // Scroll to selected model centered on first composition
    LaunchedEffect(currentModel, viewportHeight) {
        if (currentModel != null && selectedModelPosition > 0 && viewportHeight > 0) {
            // Small delay to ensure list is composed
            delay(100)
            // Scroll with negative offset to center the item (approximate item height ~60dp)
            val itemHeightPx = with(density) { 60.dp.toPx().toInt() }
            val centerOffset = -(viewportHeight / 2) + (itemHeightPx / 2)
            lazyListState.animateScrollToItem(selectedModelPosition, centerOffset)
        }
    }
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // 计算favorite models在列表中的位置偏移
        var favoriteStartIndex = 0
        if (providers.isEmpty()) {
            favoriteStartIndex = 1 // no providers item
        }
        if (favoriteModels.isNotEmpty()) {
            favoriteStartIndex += 1 // favorite header
        }

        val fromIndex = from.index - favoriteStartIndex
        val toIndex = to.index - favoriteStartIndex

        // 只处理favorite models范围内的拖拽
        if (fromIndex >= 0 && toIndex >= 0 &&
            fromIndex < favoriteModels.size && toIndex < favoriteModels.size
        ) {
            val newFavoriteModels = settings.value.favoriteModels.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
            coroutineScope.launch {
                settingsStore.update { oldSettings ->
                    oldSettings.copy(favoriteModels = newFavoriteModels)
                }
            }
        }
    }

    // Calculate the LazyColumn item index for each provider header
    val providerPositions = remember(providers, favoriteModels, providerListItems, searchKeywords, recentModels) {
        var baseIndex = 0
        if (providers.isEmpty()) {
            baseIndex = 1 // no providers item takes index 0
        }
        if (favoriteModels.isNotEmpty()) {
            baseIndex += 1 // favorite header
            baseIndex += favoriteModels.size // each favorite model is one item
        }
        if (searchKeywords.isBlank() && recentModels.isNotEmpty()) {
            baseIndex += 1 // recent header
            baseIndex += recentModels.size // each recent model is one item
        }

        // Find each provider header's position in the flat list
        providers.mapNotNull { provider ->
            val headerIndex = providerListItems.indexOfFirst { item ->
                item is ProviderListItem.Header && item.provider.id == provider.id
            }
            if (headerIndex >= 0) {
                provider.id to (baseIndex + headerIndex)
            } else null
        }.toMap()
    }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    
    // 供应商Badge行
    val providerBadgeListState = rememberLazyListState()
    LaunchedEffect(lazyListState) {
        // 当LazyColumn滚动时，LazyRow也跟随滚动，但不会改变已选中的provider id
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(100) // 防抖处理
            .collect { index ->
                if (index > 0) {
                    val currentProvider = providerPositions.entries.findLast {
                        index >= it.value
                    }
                    val currentProviderId = currentProvider?.key
                    
                    val badgeIndex = providers.indexOfFirst { it.id == currentProviderId }
                    if (badgeIndex in 0 until providers.size) {
                        runCatching { providerBadgeListState.animateScrollToItem(badgeIndex) }
                    } else {
                        runCatching { providerBadgeListState.requestScrollToItem(0) }
                    }
                } else {
                    runCatching { providerBadgeListState.requestScrollToItem(0) }
                }
            }
    }

    val canScrollUp by remember { derivedStateOf { lazyListState.canScrollBackward } }
    val canScrollDown by remember { derivedStateOf { lazyListState.canScrollForward } }

    // Floating Search field at top
    val searchFieldShape = me.lucid.domchat.ui.theme.AppShapes.SearchField
    val searchBorder = me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (style == ModelPickerStyle.LIQUID_GLASS) 0.dp else 16.dp,
                end = if (style == ModelPickerStyle.LIQUID_GLASS) 0.dp else 16.dp,
                top = if (style == ModelPickerStyle.LIQUID_GLASS) 6.dp else 12.dp,
                bottom = 10.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .liquidGlassSurface(
                    shape = searchFieldShape,
                    fillColor = if (style == ModelPickerStyle.LIQUID_GLASS) Color.Transparent else Color(0xFF000000).copy(alpha = 0.60f),
                    borderWidth = 0.5.dp,
                    specularBrush = searchBorder
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (searchKeywords.isEmpty()) {
                        Text(
                            text = stringResource(R.string.model_list_search_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchKeywords,
                        onValueChange = { searchKeywords = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                }
                if (searchKeywords.isNotEmpty()) {
                    IconButton(
                        onClick = { searchKeywords = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }
                IconButton(
                    onClick = {
                        sortOrder = when (sortOrder) {
                            ModelSortOrder.DEFAULT -> ModelSortOrder.NAME
                            ModelSortOrder.NAME -> ModelSortOrder.CONTEXT
                            ModelSortOrder.CONTEXT -> ModelSortOrder.DEFAULT
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Sort,
                        contentDescription = "Sort",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
            // 1. Scrollable List of Models
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(
                    start = if (style == ModelPickerStyle.LIQUID_GLASS) 0.dp else 16.dp,
                    end = if (style == ModelPickerStyle.LIQUID_GLASS) 0.dp else 16.dp,
                    top = 4.dp,
                    bottom = if (style == ModelPickerStyle.LIQUID_GLASS) 80.dp else 100.dp
                ),

                modifier = Modifier
                    .fillMaxSize()
                    .fadeEdges(
                        fadeTop = canScrollUp,
                        fadeBottom = if (style == ModelPickerStyle.LIQUID_GLASS) canScrollDown else false,
                        fadeHeight = 72f
                    )
                    .onGloballyPositioned { coordinates ->
                        viewportHeight = coordinates.size.height
                    },
            ) {
                if (providers.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.Cloud,
                            title = stringResource(R.string.model_list_no_providers),
                        )
                    }
                }

                if (favoriteModels.isNotEmpty()) {
                    item(key = "favorite-header") {
                        ModelSectionHeader(
                            title = stringResource(R.string.model_list_favorite),
                            topPadding = 8.dp
                        )
                    }

                    items(
                        items = favoriteListItems,
                        key = { "favorite:" + it.model.id.toString() }
                    ) { favoriteItem ->
                        ReorderableItem(
                            state = reorderableState,
                            key = "favorite:" + favoriteItem.model.id.toString()
                        ) { isDragging ->
                            val dragScale by animateFloatAsState(
                                targetValue = if (isDragging) 0.95f else 1f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                                label = "favorite_drag_scale"
                            )
                            ModelItem(
                                model = favoriteItem.model,
                                onSelect = handleSelect,
                                modifier = Modifier
                                    .scale(dragScale)
                                    .animateItem(
                                        fadeInSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                                        placementSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                                        fadeOutSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                                    ),
                                providerSetting = favoriteItem.provider,
                                select = favoriteItem.model.id == currentModel,
                                style = style,
                                inGroup = true,
                                position = favoriteItem.position,
                                onDismiss = {
                                    onDismiss()
                                },
                                tail = {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                settingsStore.update { settings ->
                                                    settings.copy(
                                                        favoriteModels = settings.favoriteModels.filter { it != favoriteItem.model.id }
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            HeartIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = Color.White,
                                        )
                                    }
                                },
                                dragHandle = {
                                    Icon(
                                        imageVector = Icons.Rounded.DragIndicator,
                                        contentDescription = null,
                                        modifier = Modifier.longPressDraggableHandle(
                                            onDragStarted = {
                                                haptics.perform(HapticPattern.DragStart)
                                            },
                                            onDragStopped = {
                                                haptics.perform(HapticPattern.DragEnd)
                                            }
                                        )
                                    )
                                },
                            )
                        }
                    }
                }

                // Render Recent Models when not searching
                if (searchKeywords.isBlank() && recentModels.isNotEmpty()) {
                    item(key = "recent-header") {
                        ModelSectionHeader(
                            title = "Recently Used",
                            topPadding = 8.dp
                        )
                    }

                    items(
                        items = recentListItems,
                        key = { "recent:" + it.model.id.toString() }
                    ) { recentItem ->
                        ModelItem(
                            model = recentItem.model,
                            onSelect = handleSelect,
                            modifier = Modifier.animateItem(),
                            providerSetting = recentItem.provider,
                            select = recentItem.model.id == currentModel,
                            style = style,
                            inGroup = true,
                            position = recentItem.position,
                            onDismiss = {
                                onDismiss()
                            }
                        )
                    }
                }

                // Render flattened provider items - each model is its own item
                items(
                    items = providerListItems,
                    key = { item ->
                        when (item) {
                            is ProviderListItem.Header -> "provider-header:${item.provider.id}"
                            is ProviderListItem.ModelEntry -> "provider-model:${item.provider.id}:${item.model.id}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is ProviderListItem.Header -> {
                            ModelSectionHeader(
                                title = item.provider.name
                            ) {
                                ProviderBalanceText(
                                    providerSetting = item.provider,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.65f),
                                )
                            }
                        }
                        is ProviderListItem.ModelEntry -> {
                            ModelItem(
                                model = item.model,
                                onSelect = handleSelect,
                                modifier = Modifier.animateItem(
                                    fadeInSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                                    placementSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                                    fadeOutSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                                ),
                                providerSetting = item.provider,
                                select = currentModel == item.model.id,
                                style = style,
                                inGroup = true,
                                position = item.position,
                                onDismiss = {
                                    onDismiss()
                                },
                                tail = {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                settingsStore.update { settings ->
                                                    if (item.isFavorite) {
                                                        settings.copy(
                                                            favoriteModels = settings.favoriteModels.filter { it != item.model.id }
                                                        )

                                                    } else {
                                                        settings.copy(
                                                            favoriteModels = settings.favoriteModels + item.model.id
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        AnimatedContent(
                                            targetState = item.isFavorite,
                                            transitionSpec = {
                                                (scaleIn(animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)) + fadeIn())
                                                    .togetherWith(scaleOut() + fadeOut())
                                            },
                                            label = "favorite_heart_anim"
                                        ) { isFav ->
                                            if (isFav) {
                                                Icon(
                                                    HeartIcon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = Color.White,
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Rounded.FavoriteBorder,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 3. Strong Bottom Floating Fade (Gradient + Solid background covering provider badges area)
            if (style != ModelPickerStyle.LIQUID_GLASS) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    // Gradient part (from transparent to solid surfaceContainerLow)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.75f),
                                        Color.Black
                                    )
                                )
                            )
                    )
                    // Solid part (completely covering the provider pills area)
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color.Black)
                    )
                }
            }

            // 4. Bottom floating provider options row
            if (providers.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        state = providerBadgeListState
                    ) {
                        items(providers) { provider ->
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.85f else 1f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                                label = "provider_badge_scale"
                            )
                            val isActive = activeProviderId == provider.id
                            val isLiquidGlassMode = style == ModelPickerStyle.LIQUID_GLASS
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .liquidGlassSurface(
                                        shape = me.lucid.domchat.ui.theme.AppShapes.ButtonPill,
                                        fillColor = if (isLiquidGlassMode) {
                                            Color.Transparent
                                        } else {
                                            if (isActive) Color.White.copy(alpha = 0.22f) else Color(0xFF000000).copy(alpha = 0.35f)
                                        },
                                        borderWidth = if (isActive) 1.dp else 0.5.dp,
                                        specularBrush = if (isActive) me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush else me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle,
                                        isFloatingControl = isLiquidGlassMode,
                                    )
                                    .clip(me.lucid.domchat.ui.theme.AppShapes.ButtonPill)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        haptics.perform(HapticPattern.Pop)
                                        activeProviderId = provider.id
                                        val position = providerPositions[provider.id] ?: 0
                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(position)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ProviderIcon(
                                        provider = provider,
                                        modifier = Modifier.size(20.dp),
                                        contentColor = Color.White
                                    )
                                    Text(
                                        text = provider.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
    }
}

// Model list sort ordering
enum class ModelSortOrder {
    DEFAULT,
    NAME,
    CONTEXT
}

// Position in a group for determining corner radius
private enum class ModelItemPosition {
    FIRST,   // Top rounded (24dp top, 10dp bottom)
    MIDDLE,  // All corners 10dp
    LAST,    // Bottom rounded (10dp top, 24dp bottom)
    SINGLE   // All corners 24dp (only item in group)
}

private data class FavoriteListItem(
    val model: Model,
    val provider: ProviderSetting,
    val position: ModelItemPosition
)

// Sealed class for flattened provider list items (enables precise scrolling)
private sealed class ProviderListItem {
    data class Header(val provider: ProviderSetting) : ProviderListItem()
    data class ModelEntry(
        val model: Model,
        val provider: ProviderSetting,
        val position: ModelItemPosition,
        val isFavorite: Boolean
    ) : ProviderListItem()
}

private fun groupItemPosition(index: Int, groupSize: Int): ModelItemPosition = when {
    groupSize <= 1 -> ModelItemPosition.SINGLE
    index == 0 -> ModelItemPosition.FIRST
    index == groupSize - 1 -> ModelItemPosition.LAST
    else -> ModelItemPosition.MIDDLE
}

private fun ModelItemPosition.toSharedPosition(): DomChatModelGroupPosition = when (this) {
    ModelItemPosition.FIRST -> DomChatModelGroupPosition.First
    ModelItemPosition.MIDDLE -> DomChatModelGroupPosition.Middle
    ModelItemPosition.LAST -> DomChatModelGroupPosition.Last
    ModelItemPosition.SINGLE -> DomChatModelGroupPosition.Single
}

@Composable
private fun ModelSectionHeader(
    title: String,
    provider: ProviderSetting? = null,
    modifier: Modifier = Modifier,
    topPadding: Dp = 12.dp,
    trailingContent: @Composable RowScope.() -> Unit = {}
) {
    val isNanoGPT = provider?.name.equals("NanoGPT", ignoreCase = true) == true ||
        (provider is ProviderSetting.OpenAI && provider.baseUrl.contains("nano-gpt.com", ignoreCase = true))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isNanoGPT) {
            Box(
                modifier = Modifier.size(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.nanogpt_logo),
                    contentDescription = null,
                    tint = Color(0xFFFFFFFF),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.65f),
        )

        Spacer(modifier = Modifier.weight(1f))

        trailingContent()
    }
}


@Composable
private fun ModelItem(
    model: Model,
    providerSetting: ProviderSetting,
    select: Boolean,
    onSelect: (Model) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    tail: @Composable RowScope.() -> Unit = {},
    dragHandle: @Composable (RowScope.() -> Unit)? = null,
    inGroup: Boolean = false,
    position: ModelItemPosition = ModelItemPosition.SINGLE,
    style: ModelPickerStyle = ModelPickerStyle.SETTINGS
) {
    val navController = LocalNavController.current

    val isLiquidGlass = style == ModelPickerStyle.LIQUID_GLASS
    val tagFill = if (isLiquidGlass) Color.Transparent else null

    val activeBadge: @Composable RowScope.() -> Unit = {
        if (select) {
            val badgeShape = RoundedCornerShape(8.dp)
            Surface(
                shape = badgeShape,
                color = Color.Transparent,
                contentColor = Color.White,
                modifier = if (isLiquidGlass) {
                    Modifier.liquidGlassSurface(
                        shape = badgeShape,
                        borderWidth = 0.5.dp,
                        specularBrush = me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush,
                        fillColor = Color.Transparent,
                        glassMode = me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED
                    )
                } else {
                    Modifier.liquidGlassSurface(
                        shape = badgeShape,
                        borderWidth = 0.5.dp,
                        specularBrush = me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush,
                        fillColor = Color(0xFF000000)
                    )
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = Color.White
                    )
                }
            }
        }
    }

    if(inGroup) {
        val rowContainerColor = if (isLiquidGlass) {
            Color.Transparent
        } else {
            if (select) Color(0xFF000000) else Color(0xFF000000).copy(alpha = 0.55f)
        }

        val target = me.lucid.domchat.ui.components.settings.groupedCornerRadii(select, position.toSharedPosition())
        val topStart by animateDpAsState(target.topStart, spring(dampingRatio = 0.5f, stiffness = 400f))
        val topEnd by animateDpAsState(target.topEnd, spring(dampingRatio = 0.5f, stiffness = 400f))
        val bottomStart by animateDpAsState(target.bottomStart, spring(dampingRatio = 0.5f, stiffness = 400f))
        val bottomEnd by animateDpAsState(target.bottomEnd, spring(dampingRatio = 0.5f, stiffness = 400f))
        val rowShape = RoundedCornerShape(18.dp)
        val glassModifier = if (isLiquidGlass) {
            modifier.liquidGlassSurface(
                shape = rowShape,
                fillColor = Color.Transparent,
                borderWidth = if (select) 1.dp else 0.5.dp,
                specularBrush = if (select) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle
            )
        } else {
            modifier
        }

        DomChatGroupedModelRow(
            title = model.displayName,
            selected = select,
            position = position.toSharedPosition(),
            onClick = { onSelect(model) },
            containerColor = rowContainerColor,
            drawBackground = !isLiquidGlass,
            drawBorder = !isLiquidGlass,
            uniformCorners = true,
            onLongClick = {
                onDismiss()
                navController.navigate(Screen.SettingProviderDetail(providerSetting.id.toString()))
            },
            modifier = glassModifier,
            icon = {
                ModelIcon(
                    model = model,
                    provider = providerSetting,
                    modifier = Modifier.size(32.dp),
                    color = Color.Transparent,
                    contentColor = Color.White,
                )
            },
            metadata = {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    ModelModalityTag(model = model, fillColor = tagFill)
                    ModelAbilityTag(model = model, fillColor = tagFill)
                }
            },
            titleTrailing = {
                activeBadge()
            },
            tail = {
                tail()
            },
            dragHandle = dragHandle,
        )
    } else {
        val interactionSource = remember { MutableInteractionSource() }
        val cardShape = me.lucid.domchat.ui.theme.AppShapes.CardLarge
        val resolvedFillColor = if (isLiquidGlass) {
            Color.Transparent
        } else {
            Color(0xFF000000)
        }

        Card(
            modifier = modifier.liquidGlassSurface(
                shape = cardShape,
                fillColor = resolvedFillColor,
                borderWidth = if (select) 1.dp else 0.5.dp,
                specularBrush = if (select) me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush else me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
            ),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            enabled = true,
                            onLongClick = {
                                onDismiss()
                                navController.navigate(
                                    Screen.SettingProviderDetail(
                                        providerSetting.id.toString()
                                    )
                                )
                            },
                            onClick = { onSelect(model) },
                            interactionSource = interactionSource,
                            indication = LocalIndication.current
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ModelIcon(
                        model = model,
                        provider = providerSetting,
                        modifier = Modifier.size(32.dp),
                        color = Color.Transparent,
                        contentColor = Color.White
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = model.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            activeBadge()
                        }

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            ModelModalityTag(model = model, fillColor = tagFill)

                            ModelAbilityTag(model = model, fillColor = tagFill)
                        }
                    }
                    tail()
                }
                dragHandle?.let { it() }
            }
        }
    }
}

@Composable
fun ModelTypeTag(model: Model) {
    if (model.type == ModelType.CHAT && model.backend) {
        Tag(
            type = TagType.DEFAULT
        ) {
            Text(text = stringResource(R.string.setting_provider_page_backend_model), color = Color.White)
        }
        return
    }
    Tag(
        type = TagType.DEFAULT
    ) {
        Text(
            text = stringResource(
                when (model.type) {
                    ModelType.CHAT -> R.string.setting_provider_page_chat_model
                    ModelType.EMBEDDING -> R.string.setting_provider_page_embedding_model
                    ModelType.IMAGE -> R.string.setting_provider_page_image_model
                    ModelType.STT -> R.string.setting_provider_page_stt_model
                }
            ),
            color = Color.White
        )
    }
}

@Composable
fun ModelModalityTag(model: Model, fillColor: Color? = null) {
    if (model.type == ModelType.STT) return
    
    Tag(
        type = TagType.DEFAULT,
        fillColor = fillColor,
    ) {
        model.inputModalities.fastForEach { modality ->
            if (modality == Modality.AUDIO) return@fastForEach
            Icon(
                imageVector = when (modality) {
                    Modality.TEXT -> Icons.Rounded.Title
                    Modality.IMAGE -> Icons.Rounded.Image
                    Modality.AUDIO -> Icons.Rounded.Mic
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(LocalTextStyle.current.lineHeight.toDp())
                    .padding(1.dp)
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(LocalTextStyle.current.lineHeight.toDp())
        )
        model.outputModalities.fastForEach { modality ->
            Icon(
                imageVector = when (modality) {
                    Modality.TEXT -> Icons.Rounded.Title
                    Modality.IMAGE -> Icons.Rounded.Image
                    Modality.AUDIO -> Icons.Rounded.Mic
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(LocalTextStyle.current.lineHeight.toDp())
                    .padding(1.dp)
            )
        }
    }
}

@Composable
fun ModelAbilityTag(model: Model, fillColor: Color? = null) {
    model.abilities.fastForEach { ability ->
        when (ability) {
            ModelAbility.TOOL -> {
                Tag(
                    type = TagType.DEFAULT,
                    fillColor = fillColor,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Build,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(LocalTextStyle.current.lineHeight.toDp())
                    )
                }
            }

            ModelAbility.REASONING -> {
                Tag(
                    type = TagType.DEFAULT,
                    fillColor = fillColor,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(LocalTextStyle.current.lineHeight.toDp()),
                    )
                }
            }
        }
    }
}
