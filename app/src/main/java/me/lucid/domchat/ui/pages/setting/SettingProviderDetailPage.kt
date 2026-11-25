package me.lucid.domchat.ui.pages.setting

import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.appSegmentedButtonColors
import me.lucid.domchat.ui.theme.appFieldColors
import me.lucid.domchat.ui.modifier.domChatBlurEffect

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.components.ui.DebouncedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material.icons.rounded.Widgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.lucid.ai.core.InputSchema
import me.lucid.ai.core.ReasoningLevel
import me.lucid.ai.core.Tool
import me.lucid.ai.provider.CustomBody
import me.lucid.ai.provider.BuiltInTools
import me.lucid.ai.provider.Modality
import me.lucid.ai.provider.Model
import me.lucid.ai.provider.ModelAbility
import me.lucid.ai.provider.ModelType
import me.lucid.ai.provider.ImageGenerationMethod
import me.lucid.ai.provider.Provider
import me.lucid.ai.provider.ProviderManager
import me.lucid.ai.provider.ProviderSetting
import me.lucid.ai.provider.ContextLimitSource
import me.lucid.ai.provider.providers.isLikelyOllama
import me.lucid.ai.provider.TextGenerationParams
import me.lucid.ai.registry.ModelIdNormalizer
import me.lucid.ai.ui.MessageChunk
import me.lucid.ai.ui.UIMessage
import me.lucid.ai.ui.UIMessagePart
import me.lucid.domchat.R
import me.lucid.domchat.data.ai.models.ModelMetadataResolver
import me.lucid.domchat.ui.components.ai.ModelAbilityTag
import me.lucid.domchat.ui.components.ai.ModelModalityTag
import me.lucid.domchat.ui.components.ai.ModelSelector
import me.lucid.domchat.ui.components.ai.ModelTypeTag
import me.lucid.domchat.ui.components.ai.ProviderBalanceText
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.ui.AutoAIIcon
import me.lucid.domchat.ui.components.ui.AutoAIIconWithUrl
import me.lucid.domchat.ui.components.ui.EmptyStateCard
import me.lucid.domchat.ui.components.ui.ProviderIcon
import me.lucid.domchat.ui.components.ui.ModelIcon
import me.lucid.domchat.ui.components.ui.ShareSheet
import me.lucid.domchat.ui.components.ui.SiliconFlowPowerByIcon
import me.lucid.domchat.ui.components.ui.Tag
import me.lucid.domchat.ui.components.ui.TagType
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.components.ui.TagsInput
import me.lucid.domchat.ui.components.ui.ItemPosition
import me.lucid.domchat.ui.components.ui.PhysicsSwipeToDelete
import me.lucid.domchat.ui.components.ui.DomChatDestructiveConfirmDialog
import me.lucid.domchat.ui.components.chat.DomChatGlassBottomSheet
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import me.lucid.domchat.ui.modifier.fadeEdges
import me.lucid.domchat.ui.components.ui.rememberShareSheetState
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.useEditState
import me.lucid.domchat.ui.components.ui.lobeHubIconUri
import me.lucid.domchat.ui.pages.assistant.detail.CustomBodies
import me.lucid.domchat.ui.pages.assistant.detail.CustomHeaders
import me.lucid.domchat.ui.pages.setting.components.CustomIconSelector
import me.lucid.domchat.ui.pages.setting.components.ModelReasoningConfigEditor
import me.lucid.domchat.ui.pages.setting.components.ProviderConfigure
import me.lucid.domchat.ui.pages.setting.components.SettingProviderBalanceOption
import me.lucid.domchat.ui.theme.extendColors
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContentColor
import me.lucid.domchat.utils.UiState
import me.lucid.domchat.utils.ImageUtils
import me.lucid.domchat.utils.plus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.util.Locale
import kotlin.uuid.Uuid
import me.lucid.domchat.data.model.Tag as DataTag
import me.lucid.domchat.ui.components.ui.FormItem
import me.lucid.domchat.ui.pages.backup.ExportKeySelectionDialog
import androidx.compose.foundation.layout.widthIn
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.AppShapes
import androidx.compose.ui.graphics.SolidColor
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi

internal fun resolveProviderModel(
    resolver: ModelMetadataResolver,
    provider: ProviderSetting,
    model: Model,
): Model {
    return resolver.applyToModel(
        model = model,
        providerHint = provider,
    )
}

internal fun resolveProviderModels(
    resolver: ModelMetadataResolver,
    provider: ProviderSetting,
    models: List<Model>,
): List<Model> {
    return resolver.applyToModels(
        models = models,
        providerHint = provider,
    )
}

private val QUALIFIER_SPLIT_REGEX = Regex("[\\-_:\\/\\s()]+")

internal fun modelsReferToSameApiModel(savedModel: Model, apiModel: Model): Boolean {
    if (savedModel.modelId.equals(apiModel.modelId, ignoreCase = true)) {
        return true
    }
    if (!savedModel.canonicalModelId.isNullOrBlank() &&
        savedModel.canonicalModelId.equals(apiModel.canonicalModelId, ignoreCase = true)) {
        return true
    }
    val savedKeys = savedModel.matchKeys()
    val apiKeys = apiModel.matchKeys()
    if (savedKeys.isEmpty() || apiKeys.isEmpty() || savedKeys.intersect(apiKeys).isEmpty()) {
        return false
    }

    if (savedModel.qualifiers() != apiModel.qualifiers()) {
        return false
    }

    val savedSlug = savedModel.providerSlug?.normalizeModelMatchToken()
    val apiSlug = apiModel.providerSlug?.normalizeModelMatchToken()
    return savedSlug == null || apiSlug == null || savedSlug == apiSlug
}

private fun Model.qualifiers(): Set<String> {
    return buildSet {
        addAll(ModelIdNormalizer.extractModelTagTokens(modelId))
        modelId.lowercase().split(QUALIFIER_SPLIT_REGEX).forEach { token ->
            if (token in ModelIdNormalizer.removableSuffixes) {
                add(token)
            }
        }
        canonicalModelId?.lowercase()?.split(QUALIFIER_SPLIT_REGEX)?.forEach { token ->
            if (token in ModelIdNormalizer.removableSuffixes) {
                add(token)
            }
        }
    }
}

private fun Model.matchKeys(): Set<String> {
    return buildSet {
        modelId.takeIf { it.isNotBlank() }?.let {
            add(it.lowercase())
            add(ModelIdNormalizer.preprocess(it))
            add(ModelIdNormalizer.canonicalize(it))
        }
        canonicalModelId?.takeIf { it.isNotBlank() }?.let {
            add(it.lowercase())
            add(ModelIdNormalizer.canonicalize(modelId, it))
            add(ModelIdNormalizer.preprocess(it))
        }
    }.filterTo(linkedSetOf()) { it.isNotBlank() }
}

private fun String.normalizeModelMatchToken(): String {
    return lowercase()
        .replace('_', '-')
        .replace('.', '-')
}

private fun ProviderSetting.apiModelCacheKey(): String {
    return when (this) {
        is ProviderSetting.OpenAI -> listOf(
            "openai",
            id.toString(),
            baseUrl,
            chatCompletionsPath,
            useResponseApi.toString(),
            apiKey.hashCode().toString(),
            apiKeyPool.map { it.id to it.enabled }.hashCode().toString(),
        )

        is ProviderSetting.Google -> listOf(
            "google",
            id.toString(),
            baseUrl,
            vertexAI.toString(),
            location,
            projectId,
            apiKey.hashCode().toString(),
            apiKeyPool.map { it.id to it.enabled }.hashCode().toString(),
        )

        is ProviderSetting.Claude -> listOf(
            "claude",
            id.toString(),
            baseUrl,
            apiKey.hashCode().toString(),
            apiKeyPool.map { it.id to it.enabled }.hashCode().toString(),
        )

        is ProviderSetting.ComfyUI -> listOf(
            "comfyui",
            id.toString(),
            baseUrl,
            workflowJson.hashCode().toString(),
        )

        is ProviderSetting.LiteRtLocal -> listOf(
            "litert_local",
            id.toString(),
            models.size.toString(),
        )
    }.joinToString("|")
}

private fun ProviderSetting.canFetchApiModels(): Boolean {
    return when (this) {
        is ProviderSetting.OpenAI -> apiKeyPool.any { it.enabled } || apiKey.isNotBlank() || isLikelyOllama()
        is ProviderSetting.Google -> if (vertexAI) {
            serviceAccountEmail.isNotBlank() && privateKey.isNotBlank() && projectId.isNotBlank()
        } else {
            apiKeyPool.any { it.enabled } || apiKey.isNotBlank()
        }
        is ProviderSetting.Claude -> apiKeyPool.any { it.enabled } || apiKey.isNotBlank()
        is ProviderSetting.ComfyUI -> workflowJson.isNotBlank()
        is ProviderSetting.LiteRtLocal -> false // on-device: no remote model list
    }
}

internal fun syncFreshModelMetadata(
    freshModels: List<Model>,
    currentProvider: ProviderSetting,
): ProviderSetting {
    if (currentProvider.models.isEmpty()) {
        return currentProvider
    }
    val updatedModels = currentProvider.models.map { savedModel ->
        val freshModel = freshModels.firstOrNull { apiModel ->
            modelsReferToSameApiModel(savedModel, apiModel)
        }
        if (freshModel != null) {
            val preserveManualContext = savedModel.contextLimitSource == ContextLimitSource.MANUAL ||
                (savedModel.contextLimitSource == null && savedModel.contextWindowTokens != null)
            savedModel.copy(
                canonicalModelId = freshModel.canonicalModelId ?: savedModel.canonicalModelId,
                iconUrl = freshModel.iconUrl,
                providerSlug = freshModel.providerSlug,
                contextWindowTokens = if (preserveManualContext) {
                    savedModel.contextWindowTokens
                } else {
                    freshModel.contextWindowTokens ?: savedModel.contextWindowTokens
                },
                maxInputTokens = if (preserveManualContext) {
                    savedModel.maxInputTokens
                } else {
                    freshModel.maxInputTokens ?: savedModel.maxInputTokens
                },
                maxOutputTokens = if (preserveManualContext) {
                    savedModel.maxOutputTokens
                } else {
                    freshModel.maxOutputTokens ?: savedModel.maxOutputTokens
                },
                contextLimitSource = if (preserveManualContext) {
                    ContextLimitSource.MANUAL
                } else {
                    freshModel.contextLimitSource ?: savedModel.contextLimitSource
                },
            )
        } else {
            savedModel
        }
    }
    return currentProvider.copyProvider(models = updatedModels)
}

private data class ApiModelSource(
    val cacheKey: String,
    val canFetchModels: Boolean,
)

@Composable
private fun rememberApiModelSource(provider: ProviderSetting): ApiModelSource {
    return ApiModelSource(
        cacheKey = provider.apiModelCacheKey(),
        canFetchModels = provider.canFetchApiModels(),
    )
}

private fun iconFileExtension(context: android.content.Context, uri: android.net.Uri): String {
    val mimeType = context.contentResolver.getType(uri)?.lowercase()
    return when {
        mimeType == "image/svg+xml" -> "svg"
        mimeType == "image/png" -> "png"
        mimeType == "image/jpeg" -> "jpg"
        mimeType == "image/webp" -> "webp"
        !mimeType.isNullOrBlank() -> android.webkit.MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: "png"
        else -> uri.lastPathSegment
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.takeIf { it.length in 2..5 }
            ?: "png"
    }
}

private object ApiModelListCache {
    private val lock = Any()
    private val modelsByProvider = mutableMapOf<String, List<Model>>()

    fun get(key: String): List<Model>? = synchronized(lock) {
        modelsByProvider[key]
    }

    fun put(key: String, models: List<Model>) = synchronized(lock) {
        modelsByProvider[key] = models
    }
}

@Composable
fun SettingProviderDetailPage(id: Uuid, vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val navController = LocalNavController.current
    val provider = settings.providers.find { it.id == id } ?: return
    val pager = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    val context = LocalContext.current
    var savingResetJob by remember { mutableStateOf<Job?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var editingModel by remember { mutableStateOf<Model?>(null) }
    var isAddingModel by remember { mutableStateOf(false) }
    var isPickingModel by remember { mutableStateOf(false) }

    val onEdit = { newProvider: ProviderSetting ->
        val newSettings = settings.copy(
            providers = settings.providers.map {
                if (newProvider.id == it.id) {
                    newProvider
                } else {
                    it
                }
            }
        )
        isSaving = true
        savingResetJob?.cancel()
        vm.updateSettings(newSettings)
        savingResetJob = scope.launch {
            delay(600)
            isSaving = false
        }
    }
    val onDelete = {
        val newSettings = settings.copy(
            providers = settings.providers - provider
        )
        vm.updateSettings(newSettings)
        navController.popBackStack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton()
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProviderIcon(provider = provider, modifier = Modifier.size(22.dp))
                        Text(text = provider.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                actions = {
                    val shareSheetState = rememberShareSheetState()
                    ShareSheet(shareSheetState)

                    val secretKeyManager = koinInject<me.lucid.domchat.data.datastore.SecretKeyManager>()
                    var showExportKeyDialog by remember { mutableStateOf(false) }

                    if (showExportKeyDialog) {
                        ExportKeySelectionDialog(
                            providers = listOf(provider),
                            title = stringResource(R.string.backup_export_select_keys_title),
                            description = stringResource(R.string.backup_export_select_keys_desc),
                            onDismiss = { showExportKeyDialog = false },
                            onConfirm = { selectedKeyIds ->
                                showExportKeyDialog = false
                                val exportedPool = provider.apiKeyPool.map { entry ->
                                    val secret = if (entry.id in selectedKeyIds) {
                                        secretKeyManager.getPoolApiKey(provider.id, entry.id, entry.key)
                                    } else ""
                                    entry.copy(key = secret)
                                }
                                val exportedProvider = when (provider) {
                                    is ProviderSetting.OpenAI -> provider.copy(apiKeyPool = exportedPool)
                                    is ProviderSetting.Google -> provider.copy(apiKeyPool = exportedPool)
                                    is ProviderSetting.Claude -> provider.copy(apiKeyPool = exportedPool)
                                    else -> provider
                                }
                                shareSheetState.show(exportedProvider)
                            }
                        )
                    }

                    ConnectionTesterButton(
                        provider = provider,
                        scope = scope
                    )

                    IconButton(
                        onClick = {
                            if (provider.apiKeyPool.isNotEmpty()) {
                                showExportKeyDialog = true
                            } else {
                                shareSheetState.show(provider)
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Share, null)
                    }
                }
            )
        },
        bottomBar = {
            ProviderDetailBottomBar(
                pager = pager,
                modelCount = provider.models.size,
                isLiquidGlass = settings.displaySetting.liquidGlassMode != LiquidGlassMode.SOFT_FROSTED,
                scope = scope
            )
        }
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pager,
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(contentPadding)
            ) { page ->
                when {
                    page == 0 -> {
                        SettingProviderConfigPage(
                            provider = provider,
                            providerTags = settings.providerTags,
                            onEdit = {
                                onEdit(it)
                            },
                            onUpdateTags = { providerWithNewTags, updatedTags ->
                                // Update the provider first
                                val updatedProviders = settings.providers.map {
                                    if (it.id == providerWithNewTags.id) providerWithNewTags else it
                                }
                                
                                // Auto-cleanup: Filter out tags that are no longer used by any provider
                                val usedTagIds = updatedProviders.flatMap { it.tags }.toSet()
                                val cleanedTags = updatedTags.filter { tag -> tag.id in usedTagIds }
                                
                                val newSettings = settings.copy(
                                    providers = updatedProviders,
                                    providerTags = cleanedTags
                                )
                                vm.updateSettings(newSettings)
                            },
                            contentPadding = contentPadding,
                            isSaving = isSaving
                        )
                    }

                    page == 1 -> {
                        SettingProviderModelPage(
                            provider = provider,
                            onEdit = onEdit,
                            onOpenEditModel = { editingModel = it },
                            onOpenAddModel = { isAddingModel = true },
                            onOpenModelPicker = { isPickingModel = true },
                            contentPadding = contentPadding
                        )
                    }
                }
            }
        }
    }

        editingModel?.let { modelToEdit ->
            EditModelSheet(
                model = modelToEdit,
                parentProvider = provider,
                onEditModel = { updatedModel ->
                    onEdit(provider.editModel(updatedModel))
                    editingModel = null
                },
                onDismissRequest = { editingModel = null }
            )
        }

        if (isAddingModel) {
            AddNewModelSheet(
                parentProvider = provider,
                onAddModel = { newModel ->
                    onEdit(provider.addModel(newModel))
                    isAddingModel = false
                },
                onDismissRequest = { isAddingModel = false }
            )
        }

        if (isPickingModel) {
            ModelPickerSheet(
                parentProvider = provider,
                onAddModel = { onEdit(provider.addModel(it)) },
                onRemoveModel = { onEdit(provider.delModel(it)) },
                onAddModels = { models ->
                    var updated = provider
                    models.forEach { updated = updated.addModel(it) }
                    onEdit(updated)
                },
                onRemoveModels = { models ->
                    var updated = provider
                    models.forEach { updated = updated.delModel(it) }
                    onEdit(updated)
                },
                onDismissRequest = { isPickingModel = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProviderDetailBottomBar(
    pager: androidx.compose.foundation.pager.PagerState,
    modelCount: Int,
    isLiquidGlass: Boolean,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val haptics = rememberPremiumHaptics()
    val isPage0 = pager.currentPage == 0
    val isPage1 = pager.currentPage == 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        val barShape = RoundedCornerShape(32.dp)
        Surface(
            shape = barShape,
            color = Color(0xFF000000),
            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
            modifier = Modifier.clip(barShape)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 0: Configuration / Settings
                val tab0Alpha by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isPage0) 0.22f else 0f,
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 400f)
                )
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = tab0Alpha),
                    border = if (isPage0) BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)) else null,
                    modifier = Modifier.clickable {
                        haptics.perform(HapticPattern.Pop)
                        scope.launch { pager.animateScrollToPage(0) }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.setting_provider_page_configuration),
                            tint = if (isPage0) Color.White else Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isPage0) FontWeight.Bold else FontWeight.Medium,
                            color = if (isPage0) Color.White else Color.White.copy(alpha = 0.65f)
                        )
                    }
                }

                // Tab 1: Models (with count badge)
                val tab1Alpha by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isPage1) 0.22f else 0f,
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 400f)
                )
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = tab1Alpha),
                    border = if (isPage1) BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)) else null,
                    modifier = Modifier.clickable {
                        haptics.perform(HapticPattern.Pop)
                        scope.launch { pager.animateScrollToPage(1) }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Widgets,
                            contentDescription = stringResource(R.string.setting_provider_page_models),
                            tint = if (isPage1) Color.White else Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (modelCount > 0) "Models ($modelCount)" else "Models",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isPage1) FontWeight.Bold else FontWeight.Medium,
                            color = if (isPage1) Color.White else Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingProviderConfigPage(
    provider: ProviderSetting,
    providerTags: List<DataTag>,
    onEdit: (ProviderSetting) -> Unit,
    onUpdateTags: (ProviderSetting, List<DataTag>) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isSaving: Boolean
) {
    var internalProvider by remember(provider) { mutableStateOf(provider) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
                color = Color(0xFF000000),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Connection & Credentials",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                    Text(
                        text = "Configure API keys, base endpoints, and network options. Once saved, switch to the Models tab below to manage and select models for your chats.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.65f),
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Card(
                shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF000000)
                )
            ) {
                ProviderConfigure(
                    provider = internalProvider,
                    modifier = Modifier.padding(16.dp),
                    showSavingIndicator = isSaving,
                    onEdit = {
                        internalProvider = it
                        // Auto-save immediately
                        onEdit(it)
                    }
                )
            }
            
            // Tags section
            Card(
                shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF000000)
                ),
                border = BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormItem(
                        label = {
                            Text(stringResource(R.string.assistant_page_tags))
                        },
                    ) {
                        TagsInput(
                            value = internalProvider.tags,
                            tags = providerTags,
                            onValueChange = { tagIds, updatedTags ->
                                // Update internal provider with new tag IDs
                                val updatedProvider = internalProvider.copyProvider(tags = tagIds)
                                internalProvider = updatedProvider
                                // Update both provider and global tags
                                onUpdateTags(updatedProvider, updatedTags)
                            },
                        )
                    }
                }
            }

            if (internalProvider is ProviderSetting.OpenAI) {
                Card(
                    shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF000000)
                    ),
                    border = BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingProviderBalanceOption(
                            provider = internalProvider,
                            balanceOption = internalProvider.balanceOption,
                            onEdit = { 
                                val updated = internalProvider.copyProvider(balanceOption = it)
                                internalProvider = updated
                                onEdit(updated)  // Auto-save like other config fields
                            }
                        )
                        ProviderBalanceText(providerSetting = provider, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // SiliconFlow icon
            if (provider is ProviderSetting.OpenAI && provider.baseUrl.contains("siliconflow.cn")) {
                SiliconFlowPowerByIcon(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                )
            }
        }
        
        // Bottom fade gradient
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
private fun SettingProviderModelPage(
    provider: ProviderSetting,
    onEdit: (ProviderSetting) -> Unit,
    onOpenEditModel: (Model) -> Unit,
    onOpenAddModel: () -> Unit,
    onOpenModelPicker: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    ModelList(
        providerSetting = provider,
        onUpdateProvider = onEdit,
        onOpenEditModel = onOpenEditModel,
        onOpenAddModel = onOpenAddModel,
        onOpenModelPicker = onOpenModelPicker,
        contentPadding = contentPadding
    )
}

@Composable
private fun ConnectionTesterButton(
    provider: ProviderSetting,
    scope: CoroutineScope
) {
    var showTestDialog by remember { mutableStateOf(false) }
    val providerManager = koinInject<ProviderManager>()
    IconButton(
        onClick = {
            showTestDialog = true
        }
    ) {
        Icon(Icons.Rounded.NetworkCheck, null)
    }
    if (showTestDialog) {
        var model by remember(provider) {
            mutableStateOf(provider.models.firstOrNull { it.type == ModelType.CHAT })
        }
        var testState: UiState<String> by remember { mutableStateOf(UiState.Idle) }
        ApplyDialogWindowBlur()
        AlertDialog(
            onDismissRequest = { showTestDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            containerColor = Color(0xFF000000),
            shape = me.lucid.domchat.ui.theme.AppShapes.SettingDialog,
            modifier = Modifier.widthIn(max = 360.dp).border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SettingDialog),
            titleContentColor = Color(0xFFF2F2F7),
            textContentColor = Color(0xFFF2F2F7),
            title = {
                Text(stringResource(R.string.setting_provider_page_test_connection))
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ModelSelector(
                        modelId = model?.id,
                        providers = listOf(provider),
                        type = ModelType.CHAT,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        model = it
                    }
                    when (testState) {
                        is UiState.Loading -> {
                            LinearWavyProgressIndicator()
                        }

                        is UiState.Success -> {
                            Text(
                                text = stringResource(R.string.setting_provider_page_test_success),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.extendColors.green6
                            )
                        }

                        is UiState.Error -> {
                            Text(
                                text = (testState as UiState.Error).error.message ?: "Error",
                                color = MaterialTheme.extendColors.red6,
                                maxLines = 10
                            )
                        }

                        else -> {}
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showTestDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {

                TextButton(
                    onClick = {
                        if (model == null) return@TextButton
                        val providerInstance = providerManager.getProviderByType(provider)
                        scope.launch {
                            runCatching {
                                testState = UiState.Loading
                                providerInstance.generateText(
                                    providerSetting = provider,
                                    messages = listOf(
                                        UIMessage.user("hello")
                                    ),
                                    params = TextGenerationParams(
                                        model = model!!,
                                    )
                                )
                                testState = UiState.Success("Success")
                            }.onFailure {
                                testState = UiState.Error(it)
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.setting_provider_page_test))
                }
            }
        )
    }
}

@Composable
private fun ModelList(
    providerSetting: ProviderSetting,
    onUpdateProvider: (ProviderSetting) -> Unit,
    onOpenEditModel: (Model) -> Unit,
    onOpenAddModel: () -> Unit,
    onOpenModelPicker: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val providerManager = koinInject<ProviderManager>()
    val scope = rememberCoroutineScope()
    var expanded by rememberSaveable { mutableStateOf(true) }
    val lazyListState = rememberLazyListState()
    val modelItemIndexOffset = 0
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromModelIndex = from.index - modelItemIndexOffset
        val toModelIndex = to.index - modelItemIndexOffset
        if (
            fromModelIndex in providerSetting.models.indices &&
            toModelIndex in providerSetting.models.indices
        ) {
            onUpdateProvider(providerSetting.moveModel(fromModelIndex, toModelIndex))
        }
    }
    val density = LocalDensity.current
    val haptics = me.lucid.domchat.ui.hooks.rememberPremiumHaptics()
    val modelMetadataResolver = koinInject<ModelMetadataResolver>()
    val apiModelSource = rememberApiModelSource(providerSetting)
    val apiModelCacheKey = apiModelSource.cacheKey
    var modelList by remember(apiModelCacheKey) { mutableStateOf(ApiModelListCache.get(apiModelCacheKey)) }
    var isReloadingModels by remember(apiModelCacheKey) { mutableStateOf(false) }
    var reloadModelsError by remember(apiModelCacheKey) { mutableStateOf<String?>(null) }

    fun reloadApiModels() {
        if (isReloadingModels) return
        if (!apiModelSource.canFetchModels) return
        scope.launch {
            isReloadingModels = true
            reloadModelsError = null
            runCatching {
                withContext(Dispatchers.IO) {
                    val raw = providerManager.getProviderByType(providerSetting)
                        .listModels(providerSetting)
                        .sortedBy { it.modelId }
                        .toList()
                    resolveProviderModels(modelMetadataResolver, providerSetting, raw)
                }
            }.onSuccess { freshModels ->
                ApiModelListCache.put(apiModelCacheKey, freshModels)
                modelList = freshModels
                val updatedProvider = withContext(Dispatchers.Default) {
                    syncFreshModelMetadata(
                        freshModels = freshModels,
                        currentProvider = providerSetting,
                    )
                }
                if (updatedProvider != providerSetting) {
                    onUpdateProvider(updatedProvider)
                }
            }.onFailure { error ->
                reloadModelsError = error.message ?: error::class.simpleName
            }
            isReloadingModels = false
        }
    }

    LaunchedEffect(apiModelCacheKey, apiModelSource.canFetchModels) {
        if (modelList == null && apiModelSource.canFetchModels) {
            reloadApiModels()
        }
    }
    
    // State for swipe neighbor tracking
    var draggingIndex by remember { mutableStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isUnlocked by remember { mutableStateOf(false) }
    var neighborsUnlocked by remember { mutableStateOf(false) }
    var pendingDeleteModel by remember { mutableStateOf<me.lucid.ai.provider.Model?>(null) }
    
    
    val canDelete = true
    
    // Reset neighborsUnlocked when offset returns to 0
    if (dragOffset == 0f && neighborsUnlocked) {
        neighborsUnlocked = false
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            debouncedSearchQuery = ""
        } else {
            kotlinx.coroutines.delay(100)
            debouncedSearchQuery = searchQuery
        }
    }
    val displayedModels = remember(providerSetting.models, debouncedSearchQuery) {
        if (debouncedSearchQuery.isBlank()) {
            providerSetting.models
        } else {
            val q = debouncedSearchQuery.trim()
            providerSetting.models.filter {
                it.modelId.contains(q, ignoreCase = true) || it.displayName.contains(q, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .floatingToolbarVerticalNestedScroll(
                    expanded = expanded,
                    onExpand = { expanded = true },
                    onCollapse = { expanded = false },
                ),
            contentPadding = contentPadding + PaddingValues(horizontal = 16.dp, vertical = 8.dp) + PaddingValues(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            state = lazyListState
        ) {
            item(key = "model_page_guided_banner") {
                Surface(
                    shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
                    color = Color(0xFF000000),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Widgets,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Active Models (${providerSetting.models.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                        Text(
                            text = "These models are available in chat. Drag handle to reorder, swipe left to remove, tap to edit parameters, or tap '+' to discover and import models.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.65f),
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            if (providerSetting.models.size > 4 || searchQuery.isNotEmpty()) {
                item(key = "model_search_input") {
                    androidx.compose.material3.OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search provider models...") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.65f))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        colors = appFieldColors(),
                        shape = me.lucid.domchat.ui.theme.AppShapes.SearchField,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )
                }
            }

            // 模型列表
            itemsIndexed(displayedModels, key = { _, item -> item.id }) { index, item ->
                val position = when {
                    displayedModels.size == 1 -> ItemPosition.ONLY
                    index == 0 -> ItemPosition.FIRST
                    index == displayedModels.lastIndex -> ItemPosition.LAST
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
                    state = reorderableLazyListState,
                    key = item.id
                ) { isDragging ->

                    androidx.compose.runtime.key(canDelete) {
                        ModelCard(
                            model = item,
                            position = position,
                            canDelete = canDelete,
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
                                pendingDeleteModel = item
                            },
                            onEdit = {
                                onOpenEditModel(item)
                            },
                            parentProvider = providerSetting,
                            dragHandle = {
                                IconButton(
                                    onClick = {},
                                    modifier = Modifier.longPressDraggableHandle(
                                        onDragStarted = {
                                            haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
                                        },
                                        onDragStopped = {
                                            haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Thud)
                                        }
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DragIndicator,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier
                                .graphicsLayer {
                                    if (isDragging) {
                                        scaleX = 0.95f
                                        scaleY = 0.95f
                                    } else {
                                        scaleX = 1f
                                        scaleY = 1f
                                    }
                                },
                        )
                    }
                }
            }
            
            // Empty state for saved models
            if (providerSetting.models.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight(0.8f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyStateCard(
                            icon = Icons.Rounded.Cloud,
                            title = stringResource(R.string.setting_provider_page_no_models),
                            description = stringResource(R.string.setting_provider_page_add_models_hint),
                        )
                    }
                }
            }
        }
        // Bottom fade gradient
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
        
        // Stacked FABs for adding models
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .offset(y = -ScreenOffset),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Model picker FAB (gray, like lorebook toggle button)
            ModelPickerFab(
                onClick = onOpenModelPicker
            )
            
            AddNewModelFab(
                onClick = onOpenAddModel
            )
        }
    }

    DomChatDestructiveConfirmDialog(
        visible = pendingDeleteModel != null,
        title = stringResource(R.string.setting_provider_delete_model_title),
        consequence = stringResource(R.string.setting_provider_delete_model_consequence),
        onDismiss = { pendingDeleteModel = null },
        onConfirm = {
            haptics.perform(HapticPattern.Pop)
            pendingDeleteModel?.let { model ->
                onUpdateProvider(providerSetting.delModel(model))
            }
            pendingDeleteModel = null
        },
    )
}



@Composable
private fun LocalHuggingFaceInstallFab(
    isDownloading: Boolean,
    onInstallUrl: (String) -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }
    var url by remember { mutableStateOf("") }
    val haptics = me.lucid.domchat.ui.hooks.rememberPremiumHaptics()

    FloatingActionButton(
        onClick = {
            haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
            showSheet = true
        },
        shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
        containerColor = androidx.compose.ui.graphics.Color(0xFF000000),
        contentColor = Color(0xFFFFFFFF),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.local_llm_install_url_action))
    }

    if (showSheet) {
        DomChatGlassBottomSheet(
            visible = true,
            containerColor = Color(0xFF000000),
            onDismissRequest = { showSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.local_llm_install_url_action),
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.local_llm_install_url_label)) },
                    supportingText = { Text(stringResource(R.string.local_llm_install_url_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
                    colors = appFieldColors(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    OutlinedButton(
                        onClick = { showSheet = false },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            onInstallUrl(url)
                            url = ""
                            showSheet = false
                        },
                        enabled = url.isNotBlank() && !isDownloading,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.local_llm_install_url_action))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelSettingsForm(
    model: Model,
    onModelChange: (Model) -> Unit,
    isEdit: Boolean,
    parentProvider: ProviderSetting? = null
) {
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()
    val providerManager = koinInject<ProviderManager>()
    val toaster = LocalToaster.current
    val context = LocalContext.current
    var isProbingCapabilities by remember(model.id, parentProvider?.id) { mutableStateOf(false) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val extension = iconFileExtension(context, uri)
            val copiedUri = withContext(Dispatchers.IO) {
                ImageUtils.copyImageToInternalStorage(
                    context = context,
                    sourceUri = uri,
                    fileName = "model_icon_${model.id}.$extension",
                )
            }
            copiedUri?.let { iconUri ->
                onModelChange(model.copy(customIconUri = iconUri.toString()))
            }
        }
    }

    fun setModelId(id: String) {
        onModelChange(model.copy(modelId = id, canonicalModelId = null))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            SegmentedButton(
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = appSegmentedButtonColors(),
                label = {
                    Text(
                        stringResource(R.string.setting_provider_page_basic_settings),
                        fontWeight = if (pagerState.currentPage == 0) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            )
            SegmentedButton(
                selected = pagerState.currentPage == 1,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = appSegmentedButtonColors(),
                label = {
                    Text(
                        stringResource(R.string.setting_provider_page_advanced_settings),
                        fontWeight = if (pagerState.currentPage == 1) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            )
        }

        val basicScrollState = rememberScrollState()
        val advancedScrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        // 基本设置页面
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(basicScrollState)
                                .padding(top = 16.dp, bottom = 16.dp)
                        ) {
                        OutlinedTextField(
                            value = model.modelId,
                            onValueChange = {
                                setModelId(it.trim())
                            },
                            label = { Text(stringResource(R.string.setting_provider_page_model_id)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(stringResource(R.string.setting_provider_page_model_id_placeholder))
                            },
                            enabled = true,
                            shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
                            colors = appFieldColors(),
                        )

                        // Full-width display name with embedded custom icon selector
                        OutlinedTextField(
                            value = model.displayName,
                            onValueChange = {
                                onModelChange(model.copy(displayName = it))
                            },
                            label = { Text(stringResource(if (isEdit) R.string.setting_provider_page_model_name else R.string.setting_provider_page_model_display_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                if (!isEdit) {
                                    Text(stringResource(R.string.setting_provider_page_model_display_name_placeholder))
                                }
                            },
                            trailingIcon = {
                                Box(modifier = Modifier.padding(end = 6.dp)) {
                                    CustomIconSelector(
                                        customIconUri = model.customIconUri,
                                        onPickFile = {
                                            imagePickerLauncher.launch(arrayOf("image/*", "image/svg+xml"))
                                        },
                                        onPickLobeHubIcon = { slug ->
                                            onModelChange(model.copy(customIconUri = lobeHubIconUri(slug)))
                                        },
                                        onReset = {
                                            onModelChange(model.copy(customIconUri = null))
                                        },
                                    ) { iconModifier ->
                                        ModelIcon(
                                            model = model,
                                            provider = parentProvider,
                                            modifier = iconModifier,
                                        )
                                    }
                                }
                            },
                            shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
                            colors = appFieldColors(),
                        )

                        ModelTypeSelector(
                            selectedType = model.type,
                            onTypeSelected = {
                                onModelChange(model.copy(type = it))
                            }
                        )

                        if (model.type == ModelType.STT) {
                            Spacer(modifier = Modifier.height(16.dp))
                            ModelSttOptionsForm(
                                sttOptions = model.sttOptions,
                                onUpdate = {
                                    onModelChange(model.copy(sttOptions = it))
                                }
                            )
                        }


                        if (model.type == ModelType.CHAT && parentProvider != null) {
                            ModelCapabilityProbeButton(
                                enabled = model.modelId.isNotBlank(),
                                isLoading = isProbingCapabilities,
                                onClick = {
                                    scope.launch {
                                        isProbingCapabilities = true
                                        runCatching {
                                            probeModelCapabilities(
                                                providerManager = providerManager,
                                                provider = parentProvider,
                                                model = model,
                                            )
                                        }.onSuccess { probedCapabilities ->
                                            if (probedCapabilities == null) {
                                                toaster.show(
                                                    context.getString(R.string.setting_provider_page_probe_capabilities_no_data),
                                                    type = ToastType.Info,
                                                )
                                            } else {
                                                val updatedModel = model.copy(
                                                    inputModalities = probedCapabilities.inputModalities,
                                                    outputModalities = probedCapabilities.outputModalities,
                                                    abilities = probedCapabilities.abilities,
                                                )
                                                if (
                                                    updatedModel.inputModalities == model.inputModalities &&
                                                    updatedModel.outputModalities == model.outputModalities &&
                                                    updatedModel.abilities == model.abilities
                                                ) {
                                                    toaster.show(
                                                        context.getString(R.string.setting_provider_page_probe_capabilities_unchanged),
                                                        type = ToastType.Info,
                                                    )
                                                } else {
                                                    onModelChange(updatedModel)
                                                    toaster.show(
                                                        context.getString(R.string.setting_provider_page_probe_capabilities_success),
                                                        type = ToastType.Success,
                                                    )
                                                }
                                            }
                                        }.onFailure { error ->
                                            toaster.show(
                                                context.getString(
                                                    R.string.setting_provider_page_probe_capabilities_error,
                                                    error.message ?: context.getString(R.string.backup_page_unknown_error),
                                                ),
                                                type = ToastType.Error,
                                            )
                                        }
                                        isProbingCapabilities = false
                                    }
                                },
                            )
                        }

                        // Image Generation Method selector (only for IMAGE type)
                        if (model.type == ModelType.IMAGE) {
                            ImageGenerationMethodSelector(
                                selectedMethod = model.imageGenerationMethod,
                                onMethodSelected = {
                                    onModelChange(model.copy(imageGenerationMethod = it))
                                },
                                supportsImageInput = model.inputModalities.contains(Modality.IMAGE),
                                onImageInputChanged = { supportsImage ->
                                    val newInputModalities = if (supportsImage) {
                                        model.inputModalities + Modality.IMAGE
                                    } else {
                                        model.inputModalities - Modality.IMAGE
                                    }
                                    onModelChange(model.copy(inputModalities = newInputModalities))
                                }
                            )
                        }

                        ModelModalitySelector(
                            model = model,
                            inputModalities = model.inputModalities,
                            onUpdateInputModalities = {
                                onModelChange(model.copy(inputModalities = it))
                            },
                            outputModalities = model.outputModalities,
                            onUpdateOutputModalities = {
                                onModelChange(model.copy(outputModalities = it))
                            }
                        )

                        if (model.type == ModelType.CHAT) {
                            ModalAbilitySelector(
                                abilities = model.abilities,
                                onUpdateAbilities = {
                                    onModelChange(model.copy(abilities = it))
                                }
                            )
                        }

                        if (model.type == ModelType.CHAT) {
                            ModelContextLimits(
                                model = model,
                                onModelChange = onModelChange,
                            )
                            BackendModelToggle(
                                backend = model.backend,
                                onBackendChange = {
                                    onModelChange(model.copy(backend = it))
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }

                    1 -> {
                        // 高级设置页面
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(advancedScrollState)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                        ProviderOverrideSettings(
                            providerOverride = model.providerOverwrite,
                            onUpdateProviderOverride = { providerOverride ->
                                onModelChange(model.copy(providerOverwrite = providerOverride))
                            },
                            parentProvider = parentProvider
                        )

                        CustomHeaders(
                            headers = model.customHeaders,
                            onUpdate = { headers ->
                                onModelChange(model.copy(customHeaders = headers))
                            }
                        )

                        CustomBodies(
                            customBodies = model.customBodies,
                            onUpdate = { bodies ->
                                onModelChange(model.copy(customBodies = bodies))
                            }
                        )

                        if (model.type == ModelType.CHAT) {
                            ModelReasoningConfigEditor(
                                model = model,
                                onModelChange = onModelChange,
                            )
                        }
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }
            }

            val activeScrollPx = if (pagerState.currentPage == 0) basicScrollState.value else advancedScrollState.value
            val topFadeAlpha = (activeScrollPx / 40f).coerceIn(0f, 1f)
            if (topFadeAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color(0xFF000000).copy(alpha = topFadeAlpha),
                                0.55f to Color(0xFF000000).copy(alpha = 0.60f * topFadeAlpha),
                                1.0f to Color.Transparent,
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun ModelPickerFab(
    onClick: () -> Unit
) {
    val haptics = me.lucid.domchat.ui.hooks.rememberPremiumHaptics()
    FloatingActionButton(
        onClick = { 
            haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Tick)
            onClick()
        },
        shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
        containerColor = androidx.compose.ui.graphics.Color(0xFF000000),
        contentColor = Color.White,
        modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.CardLarge)
    ) {
        Icon(
            Icons.Rounded.Widgets,
            contentDescription = stringResource(R.string.setting_provider_page_add_from_list)
        )
    }
}

@Composable
private fun ModelPickerSheet(
    parentProvider: ProviderSetting,
    onAddModel: (Model) -> Unit,
    onRemoveModel: (Model) -> Unit,
    onAddModels: (List<Model>) -> Unit,
    onRemoveModels: (List<Model>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val providerManager = koinInject<ProviderManager>()
    val modelMetadataResolver = koinInject<ModelMetadataResolver>()
    val scope = rememberCoroutineScope()
    val apiModelSource = rememberApiModelSource(parentProvider)
    val apiModelCacheKey = apiModelSource.cacheKey
    var modelList by remember(apiModelCacheKey) { mutableStateOf(ApiModelListCache.get(apiModelCacheKey)) }
    var isReloadingModels by remember(apiModelCacheKey) { mutableStateOf(false) }
    var reloadModelsError by remember(apiModelCacheKey) { mutableStateOf<String?>(null) }

    fun reloadApiModels() {
        if (isReloadingModels) return
        if (!apiModelSource.canFetchModels) return
        scope.launch {
            isReloadingModels = true
            reloadModelsError = null
            runCatching {
                withContext(Dispatchers.IO) {
                    val raw = providerManager.getProviderByType(parentProvider)
                        .listModels(parentProvider)
                        .sortedBy { it.modelId }
                        .toList()
                    resolveProviderModels(modelMetadataResolver, parentProvider, raw)
                }
            }.onSuccess { freshModels ->
                ApiModelListCache.put(apiModelCacheKey, freshModels)
                modelList = freshModels
            }.onFailure { error ->
                reloadModelsError = error.message ?: error::class.simpleName
            }
            isReloadingModels = false
        }
    }

    LaunchedEffect(apiModelCacheKey, apiModelSource.canFetchModels) {
        if (modelList == null && apiModelSource.canFetchModels) {
            reloadApiModels()
        }
    }

    DomChatGlassBottomSheet(
        visible = true,
        containerColor = Color(0xFF000000),
        maxHeight = (LocalConfiguration.current.screenHeightDp * 0.90f).dp,
        dragHandle = { DomChatSheetDragHandle() },
        onDismissRequest = onDismissRequest
    ) {
        val haptics = me.lucid.domchat.ui.hooks.rememberPremiumHaptics()
        val models = modelList.orEmpty()
        var filterText by rememberSaveable { mutableStateOf("") }
        var debouncedQuery by remember { mutableStateOf("") }
        LaunchedEffect(filterText) {
            if (filterText.isBlank()) {
                debouncedQuery = ""
            } else {
                kotlinx.coroutines.delay(100)
                debouncedQuery = filterText
            }
        }
        var resolvedModels by remember(models) { mutableStateOf(models) }
        LaunchedEffect(models, parentProvider.id) {
            if (models.isNotEmpty()) {
                val resolved = withContext(Dispatchers.Default) {
                    resolveProviderModels(modelMetadataResolver, parentProvider, models)
                }
                resolvedModels = resolved
            }
        }
        val filteredModels = remember(resolvedModels, debouncedQuery) {
            if (debouncedQuery.isBlank()) {
                resolvedModels
            } else {
                val queryWords = debouncedQuery.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
                resolvedModels.filter { model ->
                    val id = model.modelId.lowercase()
                    val name = model.displayName.lowercase()
                    queryWords.all { word ->
                        id.contains(word) || name.contains(word)
                    }
                }
            }
        }
        val selectedModelMap = remember(parentProvider.models) {
            val map = HashMap<String, Model>(parentProvider.models.size * 2)
            for (m in parentProvider.models) {
                map[m.modelId] = m
                map[m.modelId.lowercase()] = m
            }
            map
        }
        val allFilteredSelected = remember(filteredModels, selectedModelMap) {
            filteredModels.isNotEmpty() && filteredModels.all { 
                selectedModelMap.containsKey(it.modelId) || selectedModelMap.containsKey(it.modelId.lowercase())
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DomChatSheetHeader(
                title = stringResource(R.string.setting_provider_page_add_from_list),
                actions = {
                    if (apiModelSource.canFetchModels) {
                        IconButton(onClick = ::reloadApiModels) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Reload",
                                tint = Color.White,
                                modifier = if (isReloadingModels) Modifier.graphicsLayer { rotationZ = 360f } else Modifier
                            )
                        }
                    }
                },
                onCloseClick = onDismissRequest
            )

            androidx.compose.material3.OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                placeholder = { Text("Search models...") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.65f))
                },
                trailingIcon = {
                    if (filterText.isNotBlank()) {
                        IconButton(onClick = { filterText = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(me.lucid.domchat.ui.theme.AppShapes.SearchField)
                    .border(
                        0.5.dp,
                        me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle,
                        me.lucid.domchat.ui.theme.AppShapes.SearchField
                    ),
                shape = me.lucid.domchat.ui.theme.AppShapes.SearchField,
                singleLine = true,
                colors = me.lucid.domchat.ui.theme.appFieldColors(),
            )

            if (filteredModels.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedModelMap.size} selected (${filteredModels.size} found)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    TextButton(
                        onClick = {
                            haptics.perform(HapticPattern.Pop)
                            if (allFilteredSelected) {
                                val toRemove = filteredModels.mapNotNull { selectedModelMap[it.modelId] }
                                onRemoveModels(toRemove)
                            } else {
                                val toAdd = filteredModels.filter { !selectedModelMap.containsKey(it.modelId) }
                                onAddModels(toAdd)
                            }
                        }
                    ) {
                        Text(
                            text = if (allFilteredSelected) "Deselect all" else "Select all",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            if (isReloadingModels && models.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else if (reloadModelsError != null && models.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = reloadModelsError ?: "Error loading models",
                        color = MaterialTheme.extendColors.red6,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val listState = rememberLazyListState()
                    val canScrollUp by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 28.dp)
                    ) {
                        items(filteredModels, key = { it.modelId }) { model ->
                        val isSelected = selectedModelMap.containsKey(model.modelId) || selectedModelMap.containsKey(model.modelId.lowercase())
                        Surface(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                if (isSelected) {
                                    val current = selectedModelMap[model.modelId] ?: selectedModelMap[model.modelId.lowercase()] ?: model
                                    onRemoveModel(current)
                                } else {
                                    onAddModel(model)
                                }
                            },
                            shape = me.lucid.domchat.ui.theme.AppShapes.CardMedium,
                            color = Color(0xFF000000),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    0.5.dp,
                                    if (isSelected) me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush else me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle,
                                    me.lucid.domchat.ui.theme.AppShapes.CardMedium
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ModelIcon(
                                    model = model,
                                    provider = parentProvider,
                                    modifier = Modifier.size(32.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = model.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (!model.displayName.equals(model.modelId, ignoreCase = true)) {
                                        Text(
                                            text = model.modelId,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.55f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                ModelSelectionCircle(selected = isSelected)
                            }
                        }
                    }
                }

                // Soft top blend overlay with subtle blur - only visible when scrolled
                if (canScrollUp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .align(Alignment.TopCenter)
                            .domChatBlurEffect(
                                containerColor = Color.Transparent,
                                blurRadius = 12.dp
                            )
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF000000),
                                        Color(0xFF000000).copy(alpha = 0.88f),
                                        Color(0xFF000000).copy(alpha = 0.65f),
                                        Color(0xFF000000).copy(alpha = 0.38f),
                                        Color(0xFF000000).copy(alpha = 0.15f),
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
private fun AddNewModelFab(
    onClick: () -> Unit
) {
    val haptics = me.lucid.domchat.ui.hooks.rememberPremiumHaptics()
    FloatingActionButton(
        onClick = { 
            haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
            onClick()
        },
        shape = CircleShape,
        containerColor = Color(0xFF000000),
        contentColor = Color.White,
        modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
    ) {
        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.setting_provider_page_add_model), tint = Color.White)
    }
}

@Composable
private fun AddNewModelSheet(
    parentProvider: ProviderSetting,
    onAddModel: (Model) -> Unit,
    onDismissRequest: () -> Unit
) {
    var modelState by remember { mutableStateOf(Model()) }
    val modelMetadataResolver = koinInject<ModelMetadataResolver>()
    val haptics = rememberPremiumHaptics()

    DomChatGlassBottomSheet(
        visible = true,
        containerColor = Color(0xFF000000),
        maxHeight = (LocalConfiguration.current.screenHeightDp * 0.90f).dp,
        dragHandle = { DomChatSheetDragHandle() },
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DomChatSheetHeader(
                title = stringResource(R.string.setting_provider_page_add_model),
                onCloseClick = onDismissRequest
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    ModelSettingsForm(
                        model = modelState,
                        onModelChange = { modelState = it },
                        isEdit = false,
                        parentProvider = parentProvider
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.30f to Color(0xFF000000).copy(alpha = 0.50f),
                                0.65f to Color(0xFF000000).copy(alpha = 0.90f),
                                1.0f to Color(0xFF000000),
                            )
                        )
                        .padding(top = 48.dp, bottom = 12.dp, start = 4.dp, end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f),
                            shape = AppShapes.CardLarge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF000000),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        Button(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                val trimmedId = modelState.modelId.trim()
                                if (trimmedId.isNotBlank()) {
                                    val isVision = me.lucid.ai.provider.isVisionSupportedModel(trimmedId)
                                    val preparedModel = modelState.copy(
                                        modelId = trimmedId,
                                        displayName = modelState.displayName.trim().ifBlank { trimmedId },
                                        inputModalities = if (isVision && !modelState.inputModalities.contains(Modality.IMAGE)) {
                                            modelState.inputModalities + Modality.IMAGE
                                        } else {
                                            modelState.inputModalities
                                        }
                                    )
                                    val resolvedModel = modelMetadataResolver.applyToModel(
                                        model = preparedModel,
                                        providerHint = parentProvider,
                                    )
                                    onAddModel(resolvedModel)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = AppShapes.CardLarge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF000000),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                        ) {
                            Text(stringResource(R.string.setting_provider_page_add), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelSelectionCircle(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(Color.White)
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.25f),
                        shape = CircleShape,
                    )
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun ModelPicker(
    models: List<Model>,
    selectedModels: List<Model>,
    onModelSelected: (Model) -> Unit,
    onModelDeselected: (Model) -> Unit,
    onModelsSelected: (List<Model>) -> Unit = {},
    onModelsDeselected: (List<Model>) -> Unit = {},
    parentProvider: ProviderSetting
) {
    var showModal by remember { mutableStateOf(false) }
    val modelMetadataResolver = koinInject<ModelMetadataResolver>()
    if (showModal) {
        DomChatGlassBottomSheet(
            visible = true,
            containerColor = Color(0xFF000000),
            maxHeight = (LocalConfiguration.current.screenHeightDp * 0.90f).dp,
            onDismissRequest = { showModal = false }
        ) {
                var filterText by remember { mutableStateOf("") }
                val filterKeywords = filterText.split(" ").filter { it.isNotBlank() }
                val filteredModels = models.fastFilter {
                    if (filterKeywords.isEmpty()) {
                        true
                    } else {
                        filterKeywords.all { keyword ->
                            it.modelId.contains(keyword, ignoreCase = true) ||
                                it.displayName.contains(keyword, ignoreCase = true)
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.90f)
                        .padding(8.dp)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                // Select All / Deselect All - show only one based on selection state
                val allFilteredSelected = filteredModels.isNotEmpty() && filteredModels.all { model ->
                    selectedModels.any { selected -> modelsReferToSameApiModel(selected, model) }
                }
                
                if (allFilteredSelected) {
                    // All filtered models are selected, show Deselect All
                    TextButton(onClick = {
                        val modelsToRemove = filteredModels.mapNotNull { model ->
                            selectedModels.firstOrNull { selected -> modelsReferToSameApiModel(selected, model) }
                        }
                        if (modelsToRemove.isNotEmpty()) {
                            onModelsDeselected(modelsToRemove)
                        }
                    }) {
                        Text(stringResource(R.string.deselect_all))
                    }
                } else {
                    // Not all selected, show Select All
                    TextButton(onClick = {
                        val modelsToAdd = filteredModels.filter { model ->
                            !selectedModels.any { selected -> modelsReferToSameApiModel(selected, model) }
                        }.map { model ->
                            resolveProviderModel(modelMetadataResolver, parentProvider, model)
                        }
                        if (modelsToAdd.isNotEmpty()) {
                            onModelsSelected(modelsToAdd)
                        }
                    }) {
                        Text(stringResource(R.string.select_all))
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clipToBounds(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    // Empty state for API model list
                    if (models.isEmpty()) {
                        item {
                            // Check if provider has an API key
                            val hasApiKey = when (parentProvider) {
                                is ProviderSetting.OpenAI -> parentProvider.apiKeyPool.any { it.enabled } || parentProvider.apiKey.isNotBlank()
                                is ProviderSetting.Google -> parentProvider.apiKeyPool.any { it.enabled } || parentProvider.apiKey.isNotBlank()
                                is ProviderSetting.Claude -> parentProvider.apiKeyPool.any { it.enabled } || parentProvider.apiKey.isNotBlank()
                                is ProviderSetting.ComfyUI -> parentProvider.workflowJson.isNotBlank()
                                is ProviderSetting.LiteRtLocal -> true
                            }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(
                                        if (hasApiKey) R.string.setting_provider_page_no_models_with_api_key
                                        else R.string.setting_provider_page_no_models_no_api_key
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.65f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                    items(filteredModels) {
                        Card(
                            shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = Color(0xFF000000)
                            ),
                            border = BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(
                                    8.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                            ) {
                                ModelIcon(
                                    model = it,
                                    provider = parentProvider,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(
                                        4.dp
                                    ),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text = it.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        text = it.modelId,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f),
                                    )

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        val modelMeta = remember(it) {
                                            resolveProviderModel(modelMetadataResolver, parentProvider, it)
                                        }
                                        ModelTypeTag(
                                            model = modelMeta,
                                        )
                                        ModelModalityTag(
                                            model = modelMeta,
                                        )
                                        ModelAbilityTag(
                                            model = modelMeta,
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        val selectedModel = selectedModels.firstOrNull { model -> modelsReferToSameApiModel(model, it) }
                                        if (selectedModel != null) {
                                            // 从selectedModels中计算出要删除的model，因为删除需要id匹配，而不是ModelId
                                            onModelDeselected(selectedModel)
                                        } else {
                                            onModelSelected(resolveProviderModel(modelMetadataResolver, parentProvider, it))
                                        }
                                    }
                                ) {
                                    ModelSelectionCircle(
                                        selected = selectedModels.any { model -> modelsReferToSameApiModel(model, it) }
                                    )
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = filterText,
                    onValueChange = {
                        filterText = it
                    },
                    label = { Text(stringResource(R.string.setting_provider_page_filter_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(R.string.setting_provider_page_filter_example))
                    },
                    singleLine = true,
                    shape = me.lucid.domchat.ui.theme.AppShapes.SearchField,
                    colors = appFieldColors(),
                )
            }
            }
        }
    BadgedBox(
        badge = {
            if (models.isNotEmpty()) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF000000),
                    border = BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
                ) {
                    Text(
                        text = models.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                    )
                }
            }
        }
    ) {
        IconButton(
            onClick = {
                showModal = true
            }
        ) {
            Icon(Icons.Rounded.Widgets, null)
        }
    }
}

private data class ProbedModelCapabilities(
    val inputModalities: List<Modality>,
    val outputModalities: List<Modality>,
    val abilities: List<ModelAbility>,
)

private const val CAPABILITY_PROBE_TOOL_NAME = "domchat_capability_probe_tool"
private const val CAPABILITY_PROBE_IMAGE_DATA_URI =
    "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aF9sAAAAASUVORK5CYII="

private suspend fun probeModelCapabilities(
    providerManager: ProviderManager,
    provider: ProviderSetting,
    model: Model,
): ProbedModelCapabilities? {
    return when (provider) {
        is ProviderSetting.OpenAI -> probeModelCapabilities(
            providerInstance = providerManager.getProviderByType(provider),
            provider = provider,
            model = model,
        )

        is ProviderSetting.Google -> probeModelCapabilities(
            providerInstance = providerManager.getProviderByType(provider),
            provider = provider,
            model = model,
        )

        is ProviderSetting.Claude -> probeModelCapabilities(
            providerInstance = providerManager.getProviderByType(provider),
            provider = provider,
            model = model,
        )

        is ProviderSetting.ComfyUI -> null
        is ProviderSetting.LiteRtLocal -> null
    }
}

private suspend fun <T : ProviderSetting> probeModelCapabilities(
    providerInstance: Provider<T>,
    provider: T,
    model: Model,
): ProbedModelCapabilities? {
    val apiModel = runCatching { providerInstance.listModels(provider).findExactModel(model.modelId) }
        .getOrNull()

    ensureModelResponds(
        providerInstance = providerInstance,
        provider = provider,
        model = model,
    )

    val supportsVisionInput = apiModel?.inputModalities?.contains(Modality.IMAGE) == true ||
        runCatching {
            probeVisionInputSupport(
                providerInstance = providerInstance,
                provider = provider,
                model = model,
            )
        }.getOrDefault(false)

    val supportsToolCalling = apiModel?.abilities?.contains(ModelAbility.TOOL) == true ||
        runCatching {
            probeToolSupport(
                providerInstance = providerInstance,
                provider = provider,
                model = model,
            )
        }.getOrDefault(false)

    val supportsReasoning = apiModel?.abilities?.contains(ModelAbility.REASONING) == true ||
        runCatching {
            probeReasoningSupport(
                providerInstance = providerInstance,
                provider = provider,
                model = model,
            )
        }.getOrDefault(false)

    val hasExternalSignal = apiModel != null || supportsVisionInput || supportsToolCalling || supportsReasoning
    if (!hasExternalSignal) {
        return null
    }

    val inputModalities = linkedSetOf<Modality>().apply {
        addAll(model.inputModalities.ifEmpty { listOf(Modality.TEXT) })
        add(Modality.TEXT)
        addAll(apiModel?.inputModalities.orEmpty())
        if (supportsVisionInput) {
            add(Modality.IMAGE)
        }
    }.toList()

    val outputModalities = linkedSetOf<Modality>().apply {
        addAll(model.outputModalities.ifEmpty { listOf(Modality.TEXT) })
        add(Modality.TEXT)
        addAll(apiModel?.outputModalities.orEmpty())
    }.toList()

    val abilities = linkedSetOf<ModelAbility>().apply {
        addAll(model.abilities)
        addAll(apiModel?.abilities.orEmpty())
        if (supportsToolCalling) {
            add(ModelAbility.TOOL)
        }
        if (supportsReasoning) {
            add(ModelAbility.REASONING)
        }
    }.toList()

    return ProbedModelCapabilities(
        inputModalities = inputModalities,
        outputModalities = outputModalities,
        abilities = abilities,
    )
}

private suspend fun <T : ProviderSetting> ensureModelResponds(
    providerInstance: Provider<T>,
    provider: T,
    model: Model,
) {
    providerInstance.generateText(
        providerSetting = provider,
        messages = listOf(UIMessage.user("Reply with OK only.")),
        params = TextGenerationParams(
            model = model.copy(abilities = emptyList()),
            maxTokens = 8,
            thinkingBudget = 0,
            customHeaders = model.customHeaders,
            customBody = model.customBodies,
        ),
    )
}

private suspend fun <T : ProviderSetting> probeToolSupport(
    providerInstance: Provider<T>,
    provider: T,
    model: Model,
): Boolean {
    val response = providerInstance.generateText(
        providerSetting = provider,
        messages = listOf(
            UIMessage.system("You are testing tool support. Call the provided tool immediately and do not answer with plain text."),
            UIMessage.user("Call the capability probe tool now."),
        ),
        params = TextGenerationParams(
            model = model.copy(
                abilities = (model.abilities + ModelAbility.TOOL).distinct(),
            ),
            maxTokens = 32,
            tools = listOf(
                Tool(
                    name = CAPABILITY_PROBE_TOOL_NAME,
                    description = "Simple capability probe tool.",
                    parameters = {
                        InputSchema.Obj(
                            properties = JsonObject(emptyMap()),
                            required = emptyList(),
                        )
                    },
                    execute = { JsonNull },
                )
            ),
            thinkingBudget = 0,
            customHeaders = model.customHeaders,
            customBody = model.customBodies + buildToolProbeCustomBodies(provider),
        ),
    )

    return response.primaryMessage()
        ?.parts
        ?.filterIsInstance<UIMessagePart.ToolCall>()
        ?.any { it.toolName == CAPABILITY_PROBE_TOOL_NAME }
        ?: false
}

private suspend fun <T : ProviderSetting> probeReasoningSupport(
    providerInstance: Provider<T>,
    provider: T,
    model: Model,
): Boolean {
    val response = providerInstance.generateText(
        providerSetting = provider,
        messages = listOf(UIMessage.user("Reply with the single word OK.")),
        params = TextGenerationParams(
            model = model.copy(
                abilities = (model.abilities + ModelAbility.REASONING).distinct(),
            ),
            maxTokens = 32,
            thinkingBudget = ReasoningLevel.LOW.budgetTokens,
            customHeaders = model.customHeaders,
            customBody = model.customBodies,
        ),
    )

    return response.primaryMessage()
        ?.parts
        ?.any { part ->
            part is UIMessagePart.Reasoning && part.reasoning.isNotBlank()
        }
        ?: false
}

private suspend fun <T : ProviderSetting> probeVisionInputSupport(
    providerInstance: Provider<T>,
    provider: T,
    model: Model,
): Boolean {
    providerInstance.generateText(
        providerSetting = provider,
        messages = listOf(
            UIMessage(
                role = me.lucid.ai.core.MessageRole.USER,
                parts = listOf(
                    UIMessagePart.Text("Reply with OK only."),
                    UIMessagePart.Image(CAPABILITY_PROBE_IMAGE_DATA_URI),
                ),
            )
        ),
        params = TextGenerationParams(
            model = model,
            maxTokens = 8,
            thinkingBudget = 0,
            customHeaders = model.customHeaders,
            customBody = model.customBodies,
        ),
    )

    return true
}

private fun List<Model>.findExactModel(modelId: String): Model? {
    return firstOrNull { it.modelId == modelId }
        ?: firstOrNull { it.modelId.equals(modelId, ignoreCase = true) }
}

private fun MessageChunk.primaryMessage() = choices.firstOrNull()?.message ?: choices.firstOrNull()?.delta

private fun buildToolProbeCustomBodies(provider: ProviderSetting): List<CustomBody> {
    return when (provider) {
        is ProviderSetting.OpenAI -> listOf(
            CustomBody(
                key = "tool_choice",
                value = JsonPrimitive("required"),
            )
        )

        is ProviderSetting.Claude -> listOf(
            CustomBody(
                key = "tool_choice",
                value = buildJsonObject {
                    put("type", "any")
                },
            )
        )

        is ProviderSetting.Google -> listOf(
            CustomBody(
                key = "toolConfig",
                value = buildJsonObject {
                    put("functionCallingConfig", buildJsonObject {
                        put("mode", "ANY")
                    })
                },
            )
        )

        is ProviderSetting.ComfyUI -> emptyList()
        is ProviderSetting.LiteRtLocal -> emptyList()
    }
}

@Composable
private fun ModelCapabilityProbeButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val haptics = me.lucid.domchat.ui.hooks.rememberPremiumHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && !isLoading && isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "probeCapabilityScale",
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            onClick = {
                haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
                onClick()
            },
            enabled = enabled && !isLoading,
            shape = me.lucid.domchat.ui.theme.AppShapes.ButtonPill,
            color = androidx.compose.ui.graphics.Color(0xFF000000),
            contentColor = Color(0xFFFFFFFF),
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.NetworkCheck,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(
                        if (isLoading) {
                            R.string.setting_provider_page_probe_capabilities_loading
                        } else {
                            R.string.setting_provider_page_probe_capabilities
                        }
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }

        if (isLoading) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = stringResource(R.string.setting_provider_page_probe_capabilities_progress),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                )
            }
        }
    }
}

@Composable
private fun ModelContextLimits(
    model: Model,
    onModelChange: (Model) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DebouncedTextField(
            value = model.contextWindowTokens?.toString().orEmpty(),
            onValueChange = { value ->
                val manualLimit = value.toIntOrNull()?.takeIf { it > 0 }
                onModelChange(
                    model.copy(
                        contextWindowTokens = manualLimit,
                        maxInputTokens = null,
                        maxOutputTokens = null,
                        contextLimitSource = ContextLimitSource.MANUAL.takeIf { manualLimit != null },
                    )
                )
            },
            stateKey = "context_window_${model.id}",
            label = stringResource(R.string.setting_provider_page_context_window),
            placeholder = stringResource(R.string.setting_provider_page_optional_number),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (Modality.IMAGE in model.inputModalities) {
            DebouncedTextField(
                value = model.maxImagesInContext?.toString().orEmpty(),
                onValueChange = { value ->
                    onModelChange(model.copy(maxImagesInContext = value.toIntOrNull()?.takeIf { it > 0 }))
                },
                stateKey = "max_context_images_${model.id}",
                label = stringResource(R.string.setting_provider_page_max_context_images),
                placeholder = stringResource(R.string.setting_provider_page_optional_number),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

}

@Composable
private fun BackendModelToggle(
    backend: Boolean,
    onBackendChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                stringResource(R.string.setting_provider_page_backend_model),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                stringResource(R.string.setting_provider_page_backend_model_desc),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
        HapticSwitch(
            checked = backend,
            onCheckedChange = onBackendChange
        )
    }
}

@Composable
private fun ModelTypeSelector(
    selectedType: ModelType,
    onTypeSelected: (ModelType) -> Unit
) {
    Text(
        stringResource(R.string.setting_provider_page_model_type),
        style = MaterialTheme.typography.titleSmall
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        ModelType.entries.forEachIndexed { index, type ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index, ModelType.entries.size),
                colors = appSegmentedButtonColors(),
                label = {
                    Text(
                        text = stringResource(
                            when (type) {
                                ModelType.CHAT -> R.string.setting_provider_page_chat_model
                                ModelType.EMBEDDING -> R.string.setting_provider_page_embedding_model
                                ModelType.IMAGE -> R.string.setting_provider_page_image_model
                                ModelType.STT -> R.string.setting_provider_page_stt_model
                            }
                        ),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 8.sp,
                            maxFontSize = 14.sp,
                            stepSize = 1.sp
                        )
                    )
                },
                selected = selectedType == type,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

@Composable
private fun ImageGenerationMethodSelector(
    selectedMethod: ImageGenerationMethod?,
    onMethodSelected: (ImageGenerationMethod) -> Unit,
    supportsImageInput: Boolean = false,
    onImageInputChanged: (Boolean) -> Unit = {}
) {
    Text(
        stringResource(R.string.setting_provider_page_image_method),
        style = MaterialTheme.typography.titleSmall
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        ImageGenerationMethod.entries.forEachIndexed { index, method ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index, ImageGenerationMethod.entries.size),
                colors = appSegmentedButtonColors(),
                label = {
                    Text(
                        text = stringResource(
                            when (method) {
                                ImageGenerationMethod.DIFFUSION -> R.string.setting_provider_page_image_method_diffusion
                                ImageGenerationMethod.MULTIMODAL -> R.string.setting_provider_page_image_method_multimodal
                            }
                        )
                    )
                },
                selected = selectedMethod == method,
                onClick = { onMethodSelected(method) }
            )
        }
    }

    // Image input toggle (for image-to-image generation)
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.setting_provider_page_image_input),
            style = MaterialTheme.typography.bodyMedium
        )
        HapticSwitch(
            checked = supportsImageInput,
            onCheckedChange = onImageInputChanged
        )
    }
}

@Composable
private fun ModelModalitySelector(
    model: Model,
    inputModalities: List<Modality>,
    onUpdateInputModalities: (List<Modality>) -> Unit,
    outputModalities: List<Modality>,
    onUpdateOutputModalities: (List<Modality>) -> Unit
) {
    if (model.type == ModelType.CHAT) {
        val selectableInputModalities = Modality.entries.filter { it != Modality.AUDIO }
        val selectableOutputModalities = Modality.entries.filter { it != Modality.AUDIO }

        Text(
            stringResource(R.string.setting_provider_page_input_modality),
            style = MaterialTheme.typography.titleSmall
        )
        MultiChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            selectableInputModalities.forEachIndexed { index, modality ->
                SegmentedButton(
                    checked = modality in inputModalities,
                    shape = SegmentedButtonDefaults.itemShape(index, selectableInputModalities.size),
                    colors = appSegmentedButtonColors(),
                    onCheckedChange = {
                        if (it) {
                            onUpdateInputModalities(inputModalities + modality)
                        } else {
                            onUpdateInputModalities(inputModalities - modality)
                        }
                    }
                ) {
                    Text(
                        text = stringResource(
                            when (modality) {
                                Modality.TEXT -> R.string.setting_provider_page_text
                                Modality.IMAGE -> R.string.setting_provider_page_image
                                Modality.AUDIO -> R.string.setting_provider_page_audio
                            }
                        )
                    )
                }
            }
        }

        Text(
            stringResource(R.string.setting_provider_page_output_modality),
            style = MaterialTheme.typography.titleSmall
        )
        MultiChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            selectableOutputModalities.forEachIndexed { index, modality ->
                SegmentedButton(
                    checked = modality in outputModalities,
                    shape = SegmentedButtonDefaults.itemShape(index, selectableOutputModalities.size),
                    colors = appSegmentedButtonColors(),
                    onCheckedChange = {
                        if (it) {
                            onUpdateOutputModalities(outputModalities + modality)
                        } else {
                            onUpdateOutputModalities(outputModalities - modality)
                        }
                    }
                ) {
                    Text(
                        text = stringResource(
                            when (modality) {
                                Modality.TEXT -> R.string.setting_provider_page_text
                                Modality.IMAGE -> R.string.setting_provider_page_image
                                Modality.AUDIO -> R.string.setting_provider_page_audio
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ModalAbilitySelector(
    abilities: List<ModelAbility>,
    onUpdateAbilities: (List<ModelAbility>) -> Unit
) {
    Text(
        stringResource(R.string.setting_provider_page_abilities),
        style = MaterialTheme.typography.titleSmall
    )
    MultiChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ModelAbility.entries.forEachIndexed { index, ability ->
            SegmentedButton(
                checked = ability in abilities,
                shape = SegmentedButtonDefaults.itemShape(index, ModelAbility.entries.size),
                colors = appSegmentedButtonColors(),
                onCheckedChange = {
                    if (it) {
                        onUpdateAbilities(abilities + ability)
                    } else {
                        onUpdateAbilities(abilities - ability)
                    }
                },
                label = {
                    Text(
                        text = stringResource(
                            when (ability) {
                                ModelAbility.TOOL -> R.string.setting_provider_page_tool
                                ModelAbility.REASONING -> R.string.setting_provider_page_reasoning
                            }
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun LocalLiteRtModelIdentitySheet(
    model: Model,
    onModelChange: (Model) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    parentProvider: ProviderSetting,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val extension = iconFileExtension(context, uri)
            val copiedUri = withContext(Dispatchers.IO) {
                ImageUtils.copyImageToInternalStorage(
                    context = context,
                    sourceUri = uri,
                    fileName = "model_icon_${model.id}.$extension",
                )
            }
            copiedUri?.let { iconUri ->
                onModelChange(model.copy(customIconUri = iconUri.toString()))
            }
        }
    }

    DomChatGlassBottomSheet(
        visible = true,
        containerColor = Color(0xFF000000),
        maxHeight = (LocalConfiguration.current.screenHeightDp * 0.90f).dp,
        dragHandle = { DomChatSheetDragHandle() },
        onDismissRequest = onDismiss,
    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.local_llm_edit_identity_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cancel), tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomIconSelector(
                        customIconUri = model.customIconUri,
                        onPickFile = {
                            imagePickerLauncher.launch(arrayOf("image/*", "image/svg+xml"))
                        },
                        onPickLobeHubIcon = { slug ->
                            onModelChange(model.copy(customIconUri = lobeHubIconUri(slug)))
                        },
                        onReset = {
                            onModelChange(model.copy(customIconUri = null))
                        },
                    ) { iconModifier ->
                        ModelIcon(
                            model = model,
                            provider = parentProvider,
                            modifier = iconModifier.size(48.dp),
                        )
                    }

                    OutlinedTextField(
                        value = model.displayName,
                        onValueChange = { onModelChange(model.copy(displayName = it)) },
                        label = { Text(stringResource(R.string.setting_provider_page_model_display_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
                        colors = appFieldColors(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = AppShapes.CardLarge,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = model.displayName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = AppShapes.CardLarge,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF000000),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }
        }
    }

@Composable
private fun ModelCard(
    model: Model,
    position: ItemPosition,
    canDelete: Boolean,
    neighborOffset: Float,
    onDragProgress: (Float, Boolean) -> Unit,
    onDragEnd: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    parentProvider: ProviderSetting,
    dragHandle: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    PhysicsSwipeToDelete(
        position = ItemPosition.ONLY,
        deleteEnabled = canDelete,
        neighborOffset = neighborOffset,
        onDragProgress = onDragProgress,
        onDragEnd = onDragEnd,
        onDelete = onDelete,
        groupCornerRadius = 24.dp,
        itemCornerRadius = 24.dp,
        modifier = modifier.fillMaxWidth()
    ) { swipeShape ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF000000), shape = swipeShape)
                .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, swipeShape)
                .clip(swipeShape)
                .clickable {
                    onEdit()
                }
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModelIcon(
                model = model,
                provider = parentProvider,
                modifier = Modifier.size(32.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = model.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (model.providerOverwrite != null) {
                        Tag(type = TagType.DEFAULT) {
                            Text(
                                model.providerOverwrite?.let { it::class.simpleName } ?: model.providerOverwrite?.name
                                ?: "ProviderOverwrite"
                            )
                        }
                    }
                    ModelTypeTag(model = model)
                    ModelModalityTag(model = model)
                    ModelAbilityTag(model = model)
                }
            }
            dragHandle()
        }
    }
}

@Composable
private fun BuiltInToolsSettings(
    tools: Set<BuiltInTools>,
    onUpdateTools: (Set<BuiltInTools>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.setting_page_built_in_tools),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(R.string.setting_page_built_in_tools_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.65f)
        )

        val availableTools = listOf(
            BuiltInTools.Search to Pair(
                stringResource(R.string.setting_page_built_in_tools_search),
                stringResource(R.string.setting_page_built_in_tools_search_desc)
            ),
            BuiltInTools.UrlContext to Pair(
                stringResource(R.string.setting_page_built_in_tools_url_context),
                stringResource(R.string.setting_page_built_in_tools_url_context_desc)
            )
        )

        availableTools.forEach { (tool, info) ->
            val (title, description) = info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xFF000000)
                ),
                border = BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                    HapticSwitch(
                        checked = tool in tools,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onUpdateTools(tools + tool)
                            } else {
                                onUpdateTools(tools - tool)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditModelSheet(
    model: Model,
    parentProvider: ProviderSetting,
    onEditModel: (Model) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var editingModel by remember(model) { mutableStateOf(model.copy()) }
    val haptics = rememberPremiumHaptics()

    DomChatGlassBottomSheet(
        visible = true,
        containerColor = Color(0xFF000000),
        maxHeight = (LocalConfiguration.current.screenHeightDp * 0.90f).dp,
        dragHandle = { DomChatSheetDragHandle() },
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DomChatSheetHeader(
                title = stringResource(R.string.setting_provider_page_edit_model),
                onCloseClick = onDismissRequest
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    ModelSettingsForm(
                        model = editingModel,
                        onModelChange = { editingModel = it },
                        isEdit = true,
                        parentProvider = parentProvider
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.30f to Color(0xFF000000).copy(alpha = 0.50f),
                                0.65f to Color(0xFF000000).copy(alpha = 0.90f),
                                1.0f to Color(0xFF000000),
                            )
                        )
                        .padding(top = 48.dp, bottom = 12.dp, start = 4.dp, end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f),
                            shape = AppShapes.CardLarge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF000000),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        Button(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                if (editingModel.displayName.isNotBlank()) {
                                    onEditModel(editingModel)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = AppShapes.CardLarge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF000000),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                        ) {
                            Text(stringResource(R.string.confirm), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderOverrideSettings(
    providerOverride: ProviderSetting?,
    onUpdateProviderOverride: (ProviderSetting?) -> Unit,
    parentProvider: ProviderSetting?
) {
    var showProviderConfig by remember { mutableStateOf(false) }
    var editingProvider by remember { mutableStateOf<ProviderSetting?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.setting_provider_page_provider_override),
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            text = stringResource(R.string.setting_provider_page_provider_override_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.65f)
        )

        if (providerOverride != null) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = me.lucid.domchat.ui.theme.AppShapes.CardLarge,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProviderIcon(
                            provider = providerOverride,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${providerOverride.name} (Override)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                editingProvider = providerOverride
                                showProviderConfig = true
                            }
                        ) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.a11y_edit_provider_override)
                            )
                        }
                        IconButton(
                            onClick = {
                                onUpdateProviderOverride(null)
                            }
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.a11y_remove_provider_override)
                            )
                        }
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    editingProvider = parentProvider?.copyProvider(
                        id = Uuid.random(),
                        builtIn = false,
                        models = emptyList(), // 这里必须设置为空，不然会导致循环依赖JSON
                    )
                    showProviderConfig = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.setting_provider_page_add_provider_override))
            }
        }

        // Provider configuration modal
        if (showProviderConfig && editingProvider != null) {
            DomChatGlassBottomSheet(
                visible = true,
                containerColor = Color(0xFF000000),
                dragHandle = { DomChatSheetDragHandle() },
                onDismissRequest = {
                    showProviderConfig = false
                    editingProvider = null
                }
            ) {
                var internalProvider by remember(editingProvider) { mutableStateOf(editingProvider!!) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.88f)
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.setting_provider_page_configure_provider_override),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = {
                            showProviderConfig = false
                            editingProvider = null
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cancel), tint = Color.White)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProviderConfigure(
                            provider = internalProvider,
                            onEdit = { internalProvider = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        TextButton(
                            onClick = {
                                showProviderConfig = false
                                editingProvider = null
                            },
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(
                            onClick = {
                                onUpdateProviderOverride(internalProvider)
                                showProviderConfig = false
                                editingProvider = null
                            },
                        ) {
                            Text(stringResource(R.string.setting_provider_page_save))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelSttOptionsForm(
    sttOptions: me.lucid.ai.provider.SttOptions?,
    onUpdate: (me.lucid.ai.provider.SttOptions) -> Unit
) {
    val options = sttOptions ?: me.lucid.ai.provider.SttOptions()
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = options.language,
            onValueChange = { onUpdate(options.copy(language = it)) },
            label = { Text(stringResource(R.string.setting_provider_page_stt_language)) },
            supportingText = { Text(stringResource(R.string.setting_provider_page_stt_language_desc)) },
            modifier = Modifier.fillMaxWidth(),
            shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
            colors = appFieldColors(),
        )
        OutlinedTextField(
            value = options.prompt,
            onValueChange = { onUpdate(options.copy(prompt = it)) },
            label = { Text(stringResource(R.string.setting_provider_page_stt_prompt)) },
            supportingText = { Text(stringResource(R.string.setting_provider_page_stt_prompt_desc)) },
            modifier = Modifier.fillMaxWidth(),
            shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
            colors = appFieldColors(),
        )
        OutlinedTextField(
            value = options.responseFormat,
            onValueChange = { onUpdate(options.copy(responseFormat = it)) },
            label = { Text(stringResource(R.string.setting_provider_page_stt_response_format)) },
            supportingText = { Text(stringResource(R.string.setting_provider_page_stt_response_format_desc)) },
            modifier = Modifier.fillMaxWidth(),
            shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
            colors = appFieldColors(),
        )
    }
}
