package me.lucid.domchat.ui.components.ai

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.lucid.domchat.ui.components.ui.Tag
import me.lucid.domchat.ui.components.ui.TagType
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.extendColors
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.tts.provider.TTSProviderSetting
import me.lucid.tts.provider.TTSVoice
import me.lucid.tts.provider.findTtsVoice
import me.lucid.domchat.ui.pages.setting.components.TTSProviderIcon
import me.lucid.domchat.data.ai.models.ModelCatalogSnapshot
import kotlin.uuid.Uuid

@Composable
fun VoiceSelector(
    voiceId: Uuid?,
    providers: List<TTSProviderSetting>,
    modifier: Modifier = Modifier,
    allowClear: Boolean = false,
    onClear: (() -> Unit)? = null,
    onSelect: (TTSVoice) -> Unit,
    catalogSnapshot: ModelCatalogSnapshot? = null,
) {
    var showSheet by remember { mutableStateOf(false) }
    val selected = providers.findTtsVoice(voiceId)
    val scope = rememberCoroutineScope()

    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(
            onClick = { showSheet = true },
            modifier = modifier,
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
        ) {
            Text(
                text = selected?.second?.name?.ifBlank { selected.second.providerVoiceId }
                    ?: "Select voice",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
            )
        }
        if (allowClear && selected != null) {
            IconButton(onClick = { onClear?.invoke() }) {
                Icon(Icons.Rounded.Close, null, tint = Color.White)
            }
        }
    }

    if (showSheet) {
        Dialog(
            onDismissRequest = { showSheet = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            DomChatInWindowBottomSheet(
                visible = true,
                onDismissRequest = { showSheet = false },
                containerColor = Color(0xFF000000),
                dragHandle = { DomChatSheetDragHandle() },
                maxHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * 0.88f).dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DomChatSheetHeader(
                        title = "Select Voice",
                        subtitle = "Select voice model for spoken messages",
                        onCloseClick = { showSheet = false },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                    VoiceList(
                        currentVoice = voiceId,
                        providers = providers.filter { it.voices.isNotEmpty() },
                        onSelect = {
                            onSelect(it)
                            showSheet = false
                        },
                        catalogSnapshot = catalogSnapshot,
                    )
                }
            }
        }
    }
}

@OptIn(kotlinx.coroutines.FlowPreview::class)
@Composable
fun ColumnScope.VoiceList(
    currentVoice: Uuid?,
    providers: List<TTSProviderSetting>,
    onSelect: (TTSVoice) -> Unit,
    catalogSnapshot: ModelCatalogSnapshot? = null,
) {
    var activeProviderId by remember { mutableStateOf<Uuid?>(null) }
    var searchKeywords by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    val providerBadgeListState = rememberLazyListState()
    val density = LocalDensity.current
    val settings = LocalSettings.current
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val coroutineScope = rememberCoroutineScope()
    var viewportHeight by remember { mutableStateOf(0) }

    val providerListItems = remember(providers, searchKeywords) {
        buildList {
            providers.forEach { provider ->
                val filteredVoices = provider.voices.filter { voice ->
                    searchKeywords.isBlank() ||
                        voice.name.contains(searchKeywords, ignoreCase = true) ||
                        voice.providerVoiceId.contains(searchKeywords, ignoreCase = true) ||
                        voice.locale?.contains(searchKeywords, ignoreCase = true) == true ||
                        provider.name.contains(searchKeywords, ignoreCase = true)
                }
                if (filteredVoices.isNotEmpty()) {
                    add(VoiceListItem.Header(provider))
                    filteredVoices.forEach { voice ->
                        add(
                            VoiceListItem.VoiceEntry(
                                voice = voice,
                                provider = provider,
                            )
                        )
                    }
                }
            }
        }
    }

    val selectedVoicePosition = remember(currentVoice, providerListItems) {
        providerListItems.indexOfFirst { item ->
            item is VoiceListItem.VoiceEntry && item.voice.id == currentVoice
        }.coerceAtLeast(0)
    }

    val providerPositions = remember(providers, providerListItems) {
        providers.mapNotNull { provider ->
            val headerIndex = providerListItems.indexOfFirst { item ->
                item is VoiceListItem.Header && item.provider.id == provider.id
            }
            if (headerIndex >= 0) provider.id to headerIndex else null
        }.toMap()
    }

    LaunchedEffect(currentVoice, viewportHeight) {
        if (currentVoice != null && selectedVoicePosition > 0 && viewportHeight > 0) {
            delay(100)
            val itemHeightPx = with(density) { 60.dp.toPx().toInt() }
            val centerOffset = -(viewportHeight / 2) + (itemHeightPx / 2)
            lazyListState.animateScrollToItem(selectedVoicePosition, centerOffset)
        }
    }

    LaunchedEffect(lazyListState, providerPositions) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(100)
            .collect { index ->
                val currentProvider = providerPositions.entries.findLast { index >= it.value }
                val badgeIndex = providers.indexOfFirst { it.id == currentProvider?.key }
                if (badgeIndex >= 0) {
                    providerBadgeListState.animateScrollToItem(badgeIndex)
                } else {
                    providerBadgeListState.requestScrollToItem(0)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
    ) {
        // Sticky Search Field with liquid glass & clean spacing
        val searchFieldShape = AppShapes.SearchField
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp)
        ) {
            OutlinedTextField(
                value = searchKeywords,
                onValueChange = { searchKeywords = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassSurface(
                        shape = searchFieldShape,
                        borderWidth = 0.5.dp,
                        fillColor = Color(0xFF000000)
                    ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color(0xFFF2F2F7),
                    unfocusedTextColor = Color(0xFFF2F2F7),
                    disabledTextColor = Color(0xFF8A8A96),
                    cursorColor = Color(0xFFF2F2F7),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Color.White.copy(alpha = 0.6f)) },
                trailingIcon = {
                    if (searchKeywords.isNotEmpty()) {
                        IconButton(onClick = { searchKeywords = "" }) {
                            Icon(Icons.Rounded.Close, null, tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                },
                placeholder = { Text("Search voices", color = Color.White.copy(alpha = 0.45f)) },
                maxLines = 1,
                singleLine = true,
                shape = searchFieldShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
        }

        // Provider Pills
        if (providers.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                state = providerBadgeListState,
            ) {
                items(providers) { provider ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.85f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                        label = "voice_provider_badge_scale",
                    )
                    val isActive = activeProviderId == provider.id
                    Surface(
                        onClick = {
                            haptics.perform(HapticPattern.Pop)
                            activeProviderId = provider.id
                            val position = providerPositions[provider.id] ?: 0
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(position)
                            }
                        },
                        interactionSource = interactionSource,
                        shape = me.lucid.domchat.ui.theme.AppShapes.ButtonPill,
                        color = Color(0xFF000000),
                        contentColor = Color.White,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .border(
                                width = 0.5.dp,
                                brush = if (isActive) {
                                    me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush
                                } else {
                                    me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
                                },
                                shape = me.lucid.domchat.ui.theme.AppShapes.ButtonPill
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TTSProviderIcon(
                                provider = provider,
                                catalogSnapshot = catalogSnapshot,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Text(
                                text = provider.name.ifBlank { "TTS" },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        // Voice List
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onGloballyPositioned { coordinates ->
                    viewportHeight = coordinates.size.height
                },
        ) {
            if (providers.isEmpty() || providerListItems.isEmpty()) {
                item {
                    Text(
                        text = "No TTS voices found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            items(
                items = providerListItems,
                key = { item ->
                    when (item) {
                        is VoiceListItem.Header -> "voice-provider:${item.provider.id}"
                        is VoiceListItem.VoiceEntry -> "voice:${item.provider.id}:${item.voice.id}"
                    }
                },
            ) { item ->
                when (item) {
                    is VoiceListItem.Header -> VoiceSectionHeader(
                        title = item.provider.name.ifBlank { "TTS Provider" },
                    )
                    is VoiceListItem.VoiceEntry -> VoiceItem(
                        voice = item.voice,
                        provider = item.provider,
                        selected = currentVoice == item.voice.id,
                        onSelect = onSelect,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

private sealed class VoiceListItem {
    data class Header(val provider: TTSProviderSetting) : VoiceListItem()
    data class VoiceEntry(
        val voice: TTSVoice,
        val provider: TTSProviderSetting,
    ) : VoiceListItem()
}

@Composable
private fun VoiceSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    topPadding: Dp = 12.dp,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.55f),
        )
        Spacer(modifier = Modifier.weight(1f))
        trailingContent()
    }
}

@Composable
private fun VoiceItem(
    voice: TTSVoice,
    provider: TTSProviderSetting,
    selected: Boolean,
    onSelect: (TTSVoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(14.dp)
    val containerColor = Color(0xFF000000)
    val contentColor = Color(0xFFFFFFFF)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .border(
                width = 0.5.dp,
                brush = if (selected) {
                    me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush
                } else {
                    me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
                },
                shape = shape,
            )
            .combinedClickable(
                enabled = true,
                onClick = { onSelect(voice) },
                interactionSource = interactionSource,
                indication = LocalIndication.current,
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = voice.name.ifBlank { voice.providerVoiceId.ifBlank { "Voice" } },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = contentColor,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val locale = voice.locale
                if (!locale.isNullOrBlank()) {
                    Tag(type = TagType.DEFAULT) { Text(locale, color = Color.White) }
                }
                Tag(type = TagType.DEFAULT) { Text("x${"%.2f".format(voice.speed)}", color = Color.White) }
                Tag(type = TagType.DEFAULT) { Text("p${"%.2f".format(voice.pitch)}", color = Color.White) }
                if (provider !is TTSProviderSetting.SystemTTS) {
                    Tag(type = TagType.DEFAULT) { Text(provider.name.ifBlank { "TTS" }, color = Color.White) }
                }
            }
        }
        if (selected) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF000000),
                border = BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
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
}
