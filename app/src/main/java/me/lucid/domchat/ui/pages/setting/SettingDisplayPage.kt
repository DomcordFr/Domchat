package me.lucid.domchat.ui.pages.setting

import android.os.Build
import java.text.DateFormat
import java.util.Date
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import me.lucid.domchat.ui.components.ui.DomChatSlider
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.components.ui.HapticSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.lucid.domchat.data.datastore.AnimationSpeedLevel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.R
import me.lucid.domchat.data.datastore.DisplaySetting
import me.lucid.domchat.data.datastore.InterfaceStyle
import me.lucid.domchat.data.ai.models.ModelCatalogSource
import me.lucid.domchat.data.ai.models.ModelCatalogStatus
import me.lucid.domchat.ui.components.nav.BackButton
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.roundToInt
import kotlin.ranges.ClosedFloatingPointRange
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.data.model.BackgroundBlurMode
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.components.ui.Select
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.components.ui.ToastAction
import me.lucid.domchat.ui.components.ui.permission.PermissionManager
import me.lucid.domchat.ui.components.ui.permission.PermissionNotification
import me.lucid.domchat.ui.components.ui.permission.rememberPermissionState
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.rememberSharedPreferenceBoolean
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.HorizontalDivider
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.pages.setting.components.SettingsGroup
import me.lucid.domchat.ui.pages.setting.components.SettingGroupItem
import me.lucid.domchat.ui.pages.setting.components.SettingGroupInputItem
import me.lucid.domchat.utils.plus
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.widthIn
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur

@Composable
fun SettingDisplayPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val modelCatalogStatus by vm.modelCatalogStatus.collectAsStateWithLifecycle()
    var displaySetting by remember(settings) { mutableStateOf(settings.displaySetting) }

    fun updateDisplaySetting(setting: DisplaySetting) {
        vm.updateDisplaySetting(setting)
    }
    var uiIsGlass by remember(settings.displaySetting) {
        mutableStateOf(
            settings.displaySetting.liquidGlassMode != me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED
        )
    }
    var bubbleIsGlass by remember(settings.displaySetting) {
        mutableStateOf(
            settings.displaySetting.bubbleInterfaceStyle == me.lucid.domchat.data.datastore.InterfaceStyle.GLASS
        )
    }
    var linkedStyles by remember(settings.displaySetting) {
        mutableStateOf(settings.displaySetting.linkUiAndBubbleStyle)
    }

    // Keep linked styles consistent with persisted settings (e.g. after a catalog merge).
    LaunchedEffect(linkedStyles, uiIsGlass) {
        if (linkedStyles && bubbleIsGlass != uiIsGlass) {
            bubbleIsGlass = uiIsGlass
            updateDisplaySetting(
                vm.settings.value.displaySetting.copy(
                    liquidGlassMode = if (uiIsGlass) me.lucid.domchat.data.model.LiquidGlassMode.BALANCED_BLEND
                    else me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED,
                    bubbleInterfaceStyle = if (uiIsGlass) InterfaceStyle.GLASS
                    else InterfaceStyle.FROSTED,
                )
            )
        }
    }

    var amoledDarkMode by rememberAmoledDarkMode()
    val context = LocalContext.current
    val toaster = LocalToaster.current
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()

    var userToggledAdvanced by remember { mutableStateOf(false) }

    val permissionState = rememberPermissionState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) setOf(
            PermissionNotification
        ) else emptySet(),
    )
    PermissionManager(permissionState = permissionState)

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = stringResource(R.string.setting_display_page_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton()
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            // =====================================================================
            // Section 1: Glass & Interface Appearance
            // =====================================================================
            item {
                SettingsGroup(
                    title = "Glass & Interface Appearance"
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                        // Glass mode segmented tabs: Liquid / Frosted, each showing a "glass" subtext.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            GlassModeTab(
                                selected = uiIsGlass,
                                label = "Liquid",
                                subtitle = "glass",
                                onClick = {
                                    haptics.perform(HapticPattern.Pop)
                                    uiIsGlass = true
                                    if (linkedStyles) {
                                        bubbleIsGlass = true
                                        updateDisplaySetting(
                                            vm.settings.value.displaySetting.copy(
                                                liquidGlassMode = me.lucid.domchat.data.model.LiquidGlassMode.BALANCED_BLEND,
                                                bubbleInterfaceStyle = me.lucid.domchat.data.datastore.InterfaceStyle.GLASS,
                                            )
                                        )
                                    } else {
                                        updateDisplaySetting(
                                            vm.settings.value.displaySetting.copy(
                                                liquidGlassMode = me.lucid.domchat.data.model.LiquidGlassMode.BALANCED_BLEND,
                                            )
                                        )
                                    }
                                    toaster.show(
                                        message = "Switched to Liquid Glass. Restart recommended for full shader reinitialization.",
                                        type = ToastType.Normal,
                                        action = ToastAction("Restart") {
                                            val packageManager = context.packageManager
                                            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                                            val componentName = intent?.component
                                            val mainIntent = android.content.Intent.makeRestartActivityTask(componentName)
                                            context.startActivity(mainIntent)
                                            kotlin.system.exitProcess(0)
                                        }
                                    )
                                },
                            )
                            GlassModeTab(
                                selected = !uiIsGlass,
                                label = "Frosted",
                                subtitle = "glass",
                                onClick = {
                                    haptics.perform(HapticPattern.Pop)
                                    uiIsGlass = false
                                    if (linkedStyles) {
                                        bubbleIsGlass = false
                                        updateDisplaySetting(
                                            vm.settings.value.displaySetting.copy(
                                                liquidGlassMode = me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED,
                                                bubbleInterfaceStyle = me.lucid.domchat.data.datastore.InterfaceStyle.FROSTED,
                                            )
                                        )
                                    } else {
                                        updateDisplaySetting(
                                            vm.settings.value.displaySetting.copy(
                                                liquidGlassMode = me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED,
                                            )
                                        )
                                    }
                                    toaster.show(
                                        message = "Switched to Frosted Glass. Restart recommended for full shader reinitialization.",
                                        type = ToastType.Normal,
                                        action = ToastAction("Restart") {
                                            val packageManager = context.packageManager
                                            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                                            val componentName = intent?.component
                                            val mainIntent = android.content.Intent.makeRestartActivityTask(componentName)
                                            context.startActivity(mainIntent)
                                            kotlin.system.exitProcess(0)
                                        }
                                    )
                                },
                            )
                        }
                    }

                    SettingGroupItem(
                        title = "Link UI & Bubbles",
                        subtitle = "Synchronize glass style changes across UI and chat bubbles",
                        trailing = {
                            HapticSwitch(
                                checked = linkedStyles,
                                onCheckedChange = {
                                    linkedStyles = it
                                    updateDisplaySetting(vm.settings.value.displaySetting.copy(linkUiAndBubbleStyle = it))
                                }
                            )
                        }
                    )

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                Text(
                                    text = "Advanced Appearance",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                )
                            }
                            HapticSwitch(
                                checked = settings.displaySetting.isAdvancedGlassEnabled,
                                onCheckedChange = {
                                    userToggledAdvanced = true
                                    updateDisplaySetting(
                                        vm.settings.value.displaySetting.copy(
                                            isAdvancedGlassEnabled = it
                                        )
                                    )
                                }
                            )
                        }

                        AnimatedVisibility(
                            visible = settings.displaySetting.isAdvancedGlassEnabled,
                            enter = if (userToggledAdvanced) expandVertically() + fadeIn() else androidx.compose.animation.EnterTransition.None,
                            exit = if (userToggledAdvanced) shrinkVertically() + fadeOut() else androidx.compose.animation.ExitTransition.None,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                var localGlassAlpha by remember(settings.displaySetting.glassAlpha) {
                                    mutableFloatStateOf(settings.displaySetting.glassAlpha)
                                }
                                glassSliderRow(
                                    label = "UI Glass Opacity",
                                    valueText = "${(localGlassAlpha * 100).roundToInt()}%",
                                    value = localGlassAlpha,
                                    onValueChange = {
                                        val rounded = (it * 100f).roundToInt() / 100f
                                        if (rounded != localGlassAlpha) {
                                            haptics.perform(HapticPattern.Tick)
                                            localGlassAlpha = rounded
                                        }
                                    },
                                    onFinished = {
                                        updateDisplaySetting(vm.settings.value.displaySetting.copy(glassAlpha = localGlassAlpha))
                                    },
                                )

                                var localGlassBlur by remember(settings.displaySetting.glassBlur) {
                                    mutableFloatStateOf(settings.displaySetting.glassBlur)
                                }
                                glassSliderRow(
                                    label = "UI Glass Blur Radius",
                                    valueText = "${localGlassBlur.roundToInt()} dp",
                                    value = localGlassBlur,
                                    onValueChange = {
                                        val rounded = it.roundToInt().toFloat()
                                        if (rounded != localGlassBlur) {
                                            haptics.perform(HapticPattern.Tick)
                                            localGlassBlur = rounded
                                        }
                                    },
                                    onFinished = {
                                        updateDisplaySetting(vm.settings.value.displaySetting.copy(glassBlur = localGlassBlur))
                                    },
                                    valueRange = 0f..100f,
                                )

                                var localDepthScale by remember(settings.displaySetting.glassDepthScale) {
                                    mutableFloatStateOf(settings.displaySetting.glassDepthScale)
                                }
                                AnimatedVisibility(
                                    visible = uiIsGlass,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    glassSliderRow(
                                        label = "UI Glass Depth / Refraction",
                                        valueText = "${localDepthScale.roundToInt()}x",
                                        value = localDepthScale,
                                        onValueChange = { localDepthScale = it },
                                        onFinished = {
                                            val rounded = (localDepthScale * 10f).roundToInt() / 10f
                                            localDepthScale = rounded
                                            updateDisplaySetting(vm.settings.value.displaySetting.copy(glassDepthScale = localDepthScale))
                                        },
                                        valueRange = 0f..2.5f,
                                    )
                                }

                                var localGlassSpecular by remember(settings.displaySetting.glassSpecularIntensity) {
                                    mutableFloatStateOf(settings.displaySetting.glassSpecularIntensity)
                                }
                                AnimatedVisibility(
                                    visible = uiIsGlass,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    glassSliderRow(
                                        label = "UI Glass Specular Highlight",
                                        valueText = "${(localGlassSpecular * 100).roundToInt()}%",
                                        value = localGlassSpecular,
                                        onValueChange = { localGlassSpecular = it },
                                        onFinished = {
                                            val rounded = (localGlassSpecular * 100f).roundToInt() / 100f
                                            localGlassSpecular = rounded
                                            updateDisplaySetting(vm.settings.value.displaySetting.copy(glassSpecularIntensity = localGlassSpecular))
                                        },
                                        valueRange = 0f..1.0f,
                                    )
                                }

                                var localTint by remember(settings.displaySetting.glassTintColor) {
                                    mutableStateOf(settings.displaySetting.glassTintColor)
                                }
                                var uiTintPickerOpen by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Glass Tint Color",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Tint wash over glass surfaces",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.wrapContentWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(localTint))
                                        )
                                        Text(
                                            text = "Pick color",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                        IconButton(
                                            onClick = { uiTintPickerOpen = true }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }

                                if (uiTintPickerOpen) {
                                    TintColorPickerDialog(
                                        open = true,
                                        onDismiss = { uiTintPickerOpen = false },
                                        initialColor = localTint,
                                        onPick = { picked ->
                                            uiTintPickerOpen = false
                                            localTint = picked
                                            updateDisplaySetting(
                                                vm.settings.value.displaySetting.copy(glassTintColor = picked)
                                            )
                                        },
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.White.copy(alpha = 0.08f)
                                )

                                var localBarGradientDensity by remember(settings.displaySetting.barGradientDensity) {
                                    mutableFloatStateOf(settings.displaySetting.barGradientDensity)
                                }
                                glassSliderRow(
                                    label = "Bar Gradient Density",
                                    valueText = "${(localBarGradientDensity * 100).roundToInt()}%",
                                    value = localBarGradientDensity,
                                    onValueChange = { localBarGradientDensity = it },
                                    onFinished = {
                                        val rounded = (localBarGradientDensity * 100f).roundToInt() / 100f
                                        localBarGradientDensity = rounded
                                        updateDisplaySetting(vm.settings.value.displaySetting.copy(barGradientDensity = localBarGradientDensity))
                                    },
                                    valueRange = 0f..1.0f,
                                )

                                var localBarGradientBlur by remember(settings.displaySetting.barGradientBlur) {
                                    mutableFloatStateOf(settings.displaySetting.barGradientBlur)
                                }
                                glassSliderRow(
                                    label = "Bar Gradient Blur",
                                    valueText = "${localBarGradientBlur.roundToInt()} dp",
                                    value = localBarGradientBlur,
                                    onValueChange = { localBarGradientBlur = it },
                                    onFinished = {
                                        val rounded = (localBarGradientBlur * 100f).roundToInt() / 100f
                                        localBarGradientBlur = rounded
                                        updateDisplaySetting(vm.settings.value.displaySetting.copy(barGradientBlur = localBarGradientBlur))
                                    },
                                    valueRange = 0f..60f,
                                )

                                AnimatedVisibility(
                                    visible = uiIsGlass,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = Color.White.copy(alpha = 0.08f)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                                Text(
                                                    text = "Chromatic Dispersion",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                                Text(
                                                    text = "Refracts spectral rainbow highlights along glass curvature edges.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.65f)
                                                )
                                            }
                                            HapticSwitch(
                                                checked = settings.displaySetting.chromaticDispersionEnabled,
                                                onCheckedChange = {
                                                    updateDisplaySetting(vm.settings.value.displaySetting.copy(chromaticDispersionEnabled = it))
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

            // =====================================================================
            // Section 2: Chat Bubbles Appearance
            // =====================================================================
            item {
                SettingsGroup(
                    title = "Chat Bubbles Appearance"
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        me.lucid.domchat.ui.components.chat.ChatMessageStyleSelector(
                            selectedStyle = settings.displaySetting.chatMessageStyle,
                            onStyleSelected = { newStyle ->
                                vm.updateSettings(
                                    settings.copy(
                                        displaySetting = vm.settings.value.displaySetting.copy(chatMessageStyle = newStyle)
                                    )
                                )
                            }
                        )
                    }

                    if (!linkedStyles) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            // Glass mode segmented tabs: Liquid / Frosted, each showing a "glass" subtext.
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                GlassModeTab(
                                    selected = bubbleIsGlass,
                                    label = "Liquid",
                                    subtitle = "glass",
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        bubbleIsGlass = true
                                        updateDisplaySetting(
                                            vm.settings.value.displaySetting.copy(
                                                bubbleInterfaceStyle = me.lucid.domchat.data.datastore.InterfaceStyle.GLASS
                                            )
                                        )
                                    },
                                )
                                GlassModeTab(
                                    selected = !bubbleIsGlass,
                                    label = "Frosted",
                                    subtitle = "glass",
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        bubbleIsGlass = false
                                        updateDisplaySetting(
                                            vm.settings.value.displaySetting.copy(
                                                bubbleInterfaceStyle = me.lucid.domchat.data.datastore.InterfaceStyle.FROSTED
                                            )
                                        )
                                    },
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                Text(
                                    text = "Advanced Appearance",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                )
                            }
                            HapticSwitch(
                                checked = settings.displaySetting.isAdvancedGlassEnabled,
                                onCheckedChange = {
                                    userToggledAdvanced = true
                                    updateDisplaySetting(
                                        vm.settings.value.displaySetting.copy(
                                            isAdvancedGlassEnabled = it
                                        )
                                    )
                                }
                            )
                        }

                        AnimatedVisibility(
                            visible = settings.displaySetting.isAdvancedGlassEnabled,
                            enter = if (userToggledAdvanced) expandVertically() + fadeIn() else androidx.compose.animation.EnterTransition.None,
                            exit = if (userToggledAdvanced) shrinkVertically() + fadeOut() else androidx.compose.animation.ExitTransition.None,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                var localBubbleAlpha by remember(settings.displaySetting.chatBubbleAlpha) {
                                    mutableFloatStateOf(settings.displaySetting.chatBubbleAlpha)
                                }
                                glassSliderRow(
                                    label = "Bubble Opacity",
                                    valueText = "${(localBubbleAlpha * 100).roundToInt()}%",
                                    value = localBubbleAlpha,
                                    onValueChange = {
                                        val rounded = (it * 100f).roundToInt() / 100f
                                        if (rounded != localBubbleAlpha) {
                                            haptics.perform(HapticPattern.Tick)
                                            localBubbleAlpha = rounded
                                        }
                                    },
                                    onFinished = {
                                        updateDisplaySetting(vm.settings.value.displaySetting.copy(chatBubbleAlpha = localBubbleAlpha))
                                    },
                                )

                                var localBubbleBlur by remember(settings.displaySetting.chatBubbleBlur) {
                                    mutableFloatStateOf(settings.displaySetting.chatBubbleBlur)
                                }
                                glassSliderRow(
                                    label = "Bubble Blur Radius",
                                    valueText = "${localBubbleBlur.roundToInt()} dp",
                                    value = localBubbleBlur,
                                    onValueChange = {
                                        val rounded = it.roundToInt().toFloat()
                                        if (rounded != localBubbleBlur) {
                                            haptics.perform(HapticPattern.Tick)
                                            localBubbleBlur = rounded
                                        }
                                    },
                                    onFinished = {
                                        updateDisplaySetting(vm.settings.value.displaySetting.copy(chatBubbleBlur = localBubbleBlur))
                                    },
                                    valueRange = 0f..100f,
                                )

                                var localBubbleDepth by remember(settings.displaySetting.bubbleDepthScale) {
                                    mutableFloatStateOf(settings.displaySetting.bubbleDepthScale)
                                }
                                AnimatedVisibility(
                                    visible = bubbleIsGlass,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    glassSliderRow(
                                        label = "Bubble Depth / Refraction",
                                        valueText = "${localBubbleDepth.roundToInt()}x",
                                        value = localBubbleDepth,
                                        onValueChange = { localBubbleDepth = it },
                                        onFinished = {
                                            val rounded = (localBubbleDepth * 10f).roundToInt() / 10f
                                            localBubbleDepth = rounded
                                            updateDisplaySetting(vm.settings.value.displaySetting.copy(bubbleDepthScale = localBubbleDepth))
                                        },
                                        valueRange = 0f..2.5f,
                                    )
                                }

                                var localBubbleSpecular by remember(settings.displaySetting.bubbleSpecularIntensity) {
                                    mutableFloatStateOf(settings.displaySetting.bubbleSpecularIntensity)
                                }
                                AnimatedVisibility(
                                    visible = bubbleIsGlass,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    glassSliderRow(
                                        label = "Bubble Specular Intensity",
                                        valueText = "${(localBubbleSpecular * 100).roundToInt()}%",
                                        value = localBubbleSpecular,
                                        onValueChange = { localBubbleSpecular = it },
                                        onFinished = {
                                            val rounded = (localBubbleSpecular * 100f).roundToInt() / 100f
                                            localBubbleSpecular = rounded
                                            updateDisplaySetting(vm.settings.value.displaySetting.copy(bubbleSpecularIntensity = localBubbleSpecular))
                                        },
                                        valueRange = 0f..1.0f,
                                    )
                                }

                                var localBubbleTintColor by remember(settings.displaySetting.chatBubbleTintColor) {
                                    mutableStateOf(settings.displaySetting.chatBubbleTintColor)
                                }
                                var bubbleTintPickerOpen by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                        Text(
                                            text = "Bubble Tint Color",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Tint wash over chat bubbles",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.wrapContentWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(localBubbleTintColor))
                                        )
                                        Text(
                                            text = "Pick color",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                        IconButton(
                                            onClick = { bubbleTintPickerOpen = true }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }

                                if (bubbleTintPickerOpen) {
                                    TintColorPickerDialog(
                                        open = true,
                                        onDismiss = { bubbleTintPickerOpen = false },
                                        initialColor = localBubbleTintColor,
                                        onPick = { picked ->
                                            bubbleTintPickerOpen = false
                                            localBubbleTintColor = picked
                                            updateDisplaySetting(
                                                vm.settings.value.displaySetting.copy(
                                                    chatBubbleTintColor = localBubbleTintColor
                                                )
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =====================================================================
            // Section 3: Wallpaper & Atmosphere
            // =====================================================================
            item {
                val globalBg = settings.displaySetting.globalBackground
                val globalDim = settings.displaySetting.globalBackgroundDim
                val globalBlur = settings.displaySetting.globalBackgroundBlur
                val globalWallpaperPicker = rememberLauncherForActivityResult(
                    ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) {
                        val localPath = me.lucid.domchat.utils.ImageUtils.copyImageToAppStorage(
                            context = context,
                            sourceUri = uri,
                            subDir = "wallpapers",
                            fileName = "wp_global_${System.currentTimeMillis()}.jpg"
                        )
                        if (localPath != null) {
                            haptics.perform(HapticPattern.Pop)
                            updateDisplaySetting(
                                vm.settings.value.displaySetting.copy(globalBackground = localPath)
                            )
                        }
                    }
                }

                SettingsGroup(
                    title = "Wallpaper & Atmosphere"
                ) {
                    SettingGroupItem(
                        title = "Global Wallpaper",
                        subtitle = if (globalBg.isNullOrBlank()) "No wallpaper set (pure OLED canvas)" else "Custom wallpaper active across all chats",
                        trailing = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!globalBg.isNullOrBlank()) {
                                    IconButton(
                                        onClick = {
                                            haptics.perform(HapticPattern.Pop)
                                            updateDisplaySetting(
                                                vm.settings.value.displaySetting.copy(globalBackground = null)
                                            )
                                        }
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Remove Wallpaper",
                                            tint = Color(0xFFFF453A)
                                        )
                                    }
                                }
                                DomChatGlassPillButton(
                                    text = if (globalBg.isNullOrBlank()) "Choose" else "Change",
                                    onClick = {
                                        globalWallpaperPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                            }
                        }
                    )

                    // Live Wallpaper Preview Card under Global Wallpaper
                    if (!globalBg.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(AppShapes.CardMedium)
                                    .border(
                                        BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle),
                                        AppShapes.CardMedium
                                    )
                                    .background(Color(0xFF000000))
                            ) {
                                AsyncImage(
                                    model = globalBg,
                                    contentDescription = "Global wallpaper preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                // Live dim overlay applied to preview
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Color.Black.copy(
                                                alpha = globalDim.coerceIn(0f, 0.95f)
                                            )
                                        )
                                )
                                // Live blur overlay (bottom scrim) applied to preview
                                if (globalBlur > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(
                                                            alpha = (globalBlur / 200f).coerceIn(0f, 0.5f)
                                                        )
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // Progressive Disclosure: Dimming and Blur sliders only show when wallpaper is set
                    AnimatedVisibility(visible = !globalBg.isNullOrBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            var localDim by remember(globalDim) { mutableFloatStateOf(globalDim) }
                            var localBlur by remember(globalBlur) { mutableFloatStateOf(globalBlur) }
                            var localFadeHeight by remember(settings.displaySetting.globalBackgroundBlurHeightFraction) {
                                mutableFloatStateOf(settings.displaySetting.globalBackgroundBlurHeightFraction)
                            }

                            glassSliderRow(
                                label = "Wallpaper Dimming",
                                valueText = "${(localDim * 100).roundToInt()}%",
                                value = localDim,
                                onValueChange = {
                                    if ((it * 100).toInt() != (localDim * 100).toInt()) {
                                        haptics.perform(HapticPattern.Tick)
                                    }
                                    localDim = it
                                },
                                onFinished = {
                                    updateDisplaySetting(vm.settings.value.displaySetting.copy(globalBackgroundDim = localDim))
                                },
                                valueRange = 0f..0.95f
                            )

                            glassSliderRow(
                                label = "Wallpaper Blur",
                                valueText = "${localBlur.roundToInt()} dp",
                                value = localBlur,
                                onValueChange = {
                                    if (it.roundToInt() != localBlur.roundToInt()) {
                                        haptics.perform(HapticPattern.Tick)
                                    }
                                    localBlur = it
                                },
                                onFinished = {
                                    updateDisplaySetting(vm.settings.value.displaySetting.copy(globalBackgroundBlur = localBlur))
                                },
                                valueRange = 0f..100f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Blur Style", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                Select(
                                    options = BackgroundBlurMode.entries,
                                    selectedOption = settings.displaySetting.backgroundBlurMode,
                                    onOptionSelected = {
                                        haptics.perform(HapticPattern.Tick)
                                        updateDisplaySetting(vm.settings.value.displaySetting.copy(backgroundBlurMode = it))
                                    },
                                    optionToString = { mode ->
                                        when (mode) {
                                            BackgroundBlurMode.FULL -> "Full Image"
                                            BackgroundBlurMode.BOTTOM_FADE -> "Bottom Fade"
                                        }
                                    },
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }

                            if (settings.displaySetting.backgroundBlurMode == BackgroundBlurMode.BOTTOM_FADE) {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut(),
                                ) {
                                    glassSliderRow(
                                        label = "Fade Blur Height",
                                        valueText = "${(localFadeHeight * 100).roundToInt()}%",
                                        value = localFadeHeight,
                                        onValueChange = {
                                            if ((it * 100).toInt() != (localFadeHeight * 100).toInt()) {
                                                haptics.perform(HapticPattern.Tick)
                                            }
                                            localFadeHeight = it
                                        },
                                        onFinished = {
                                            updateDisplaySetting(vm.settings.value.displaySetting.copy(globalBackgroundBlurHeightFraction = localFadeHeight))
                                        },
                                        valueRange = 0.10f..1.0f
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =====================================================================
            // Section 4: Chat Layout & Experience
            // =====================================================================
            item {
                var createNewConversationOnStart by rememberSharedPreferenceBoolean(
                    "create_new_conversation_on_start",
                    true
                )
                SettingsGroup(
                    title = "Chat Layout & Experience"
                ) {
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_create_new_conversation_on_start_title),
                        subtitle = stringResource(R.string.setting_display_page_create_new_conversation_on_start_desc),
                        trailing = {
                            HapticSwitch(
                                checked = createNewConversationOnStart,
                                onCheckedChange = {
                                    createNewConversationOnStart = it
                                }
                            )
                        }
                    )

                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_notification_message_generated),
                        subtitle = stringResource(R.string.setting_display_page_notification_message_generated_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.enableNotificationOnMessageGeneration,
                                onCheckedChange = {
                                    if (it && !permissionState.allPermissionsGranted) {
                                        permissionState.requestPermissions()
                                    }
                                    updateDisplaySetting(displaySetting.copy(enableNotificationOnMessageGeneration = it))
                                }
                            )
                        }
                    )
                }
            }

            // =====================================================================
            // Section 5: Motion & Animations
            // =====================================================================
            item {
                SettingsGroup(
                    title = "Motion & Animations"
                ) {
                    SettingGroupItem(
                        title = "Animation Speed",
                        subtitle = "${settings.displaySetting.animationSpeed.title} (${settings.displaySetting.animationSpeed.subtitle})",
                        trailing = {
                            Select(
                                options = AnimationSpeedLevel.entries,
                                selectedOption = settings.displaySetting.animationSpeed,
                                onOptionSelected = {
                                    haptics.perform(HapticPattern.Tick)
                                    updateDisplaySetting(vm.settings.value.displaySetting.copy(animationSpeed = it))
                                },
                                optionToString = { it.title },
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    )

                    SettingGroupItem(
                        title = "Chat Switching Animation",
                        subtitle = "Cinematic animated transition when switching between conversations",
                        trailing = {
                            HapticSwitch(
                                checked = settings.displaySetting.enableChatSwitchAnimation,
                                onCheckedChange = {
                                    updateDisplaySetting(vm.settings.value.displaySetting.copy(enableChatSwitchAnimation = it))
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Segmented tab for Liquid / Frosted glass mode with a small centered "glass" subtext.
 * 48dp minimum touch target; animated transition between active and inactive states.
 */
@Composable
private fun RowScope.GlassModeTab(
    selected: Boolean,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick),
        shape = AppShapes.CardMedium,
        color = Color(0xFF000000),
        border = if (selected) BorderStroke(1.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderBrush)
                 else BorderStroke(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (selected) Color.White else Color.White.copy(alpha = 0.60f),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.40f),
            )
        }
    }
}

/**
 * Label + value row paired with [DomChatSlider] for glass appearance controls.
 * Uses continuous slider without tick separation dots and padded labels to prevent clipping.
 */
@Composable
private fun glassSliderRow(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.End
            )
        }
        DomChatSlider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onFinished,
            valueRange = valueRange,
            steps = 0,
        )
    }
}

/**
 * HSV / Hex color picker dialog for glass and bubble tint washes
 * (Color wheel replacement: sliders + hex input + preview swatch).
 */
@Composable
private fun TintColorPickerDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    initialColor: Long,
    onPick: (Long) -> Unit,
) {
    if (!open) return
    var pickerColor by remember { mutableStateOf(initialColor) }
    LaunchedEffect(initialColor) { pickerColor = initialColor }

    val hsv = FloatArray(3)
    fun syncSliders() {
        android.graphics.Color.colorToHSV(pickerColor.toInt(), hsv)
    }
    syncSliders()
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1] * 100f) }
    var brightness by remember { mutableFloatStateOf(hsv[2] * 100f) }
    var alpha by remember { mutableFloatStateOf(Color(pickerColor).alpha * 100f) }
    var hex by remember { mutableStateOf(String.format("%08X", pickerColor.toInt())) }

    fun derive() {
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation / 100f, brightness / 100f))
        val a = (alpha / 100f * 255f).toInt().coerceIn(0, 255)
        val r = android.graphics.Color.red(argb)
        val g = android.graphics.Color.green(argb)
        val b = android.graphics.Color.blue(argb)
        pickerColor = (android.graphics.Color.argb(a, r, g, b).toLong() and 0xFFFFFFFFL)
        hex = String.format("%08X", pickerColor.toInt())
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF000000),
            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp).width(340.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(pickerColor))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = hex,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            tintSliderRow("Hue", "${hue.roundToInt()}°", hue, 0f..360f, 360) {
                hue = it
                derive()
            }
            tintSliderRow("Saturation", "${saturation.roundToInt()}%", saturation, 0f..100f, 100) {
                saturation = it
                derive()
            }
            tintSliderRow("Brightness", "${brightness.roundToInt()}%", brightness, 0f..100f, 100) {
                brightness = it
                derive()
            }
            tintSliderRow("Alpha", "${alpha.roundToInt()}%", alpha, 0f..100f, 100) {
                alpha = it
                derive()
            }

            OutlinedTextField(
                value = hex,
                onValueChange = { input ->
                    hex = input
                    try {
                        val raw = input.trimStart()
                        val parsed = android.graphics.Color.parseColor(if (raw.startsWith("#")) raw else "#$raw")
                        pickerColor = parsed.toLong()
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(parsed, hsv)
                        hue = hsv[0]
                        saturation = hsv[1] * 100f
                        brightness = hsv[2] * 100f
                        alpha = Color(parsed).alpha * 100f
                    } catch (_: IllegalArgumentException) {
                    }
                },
                singleLine = true,
                maxLines = 1,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Hex") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.White.copy(alpha = 0.35f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White.copy(alpha = 0.8f),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.45f),
                    cursorColor = Color.White.copy(alpha = 0.9f),
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                DomChatGlassPillButton(
                    text = "Cancel",
                    onClick = { onDismiss() },
                    modifier = Modifier.weight(1f).height(48.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                DomChatGlassPillButton(
                    text = "Apply",
                    onClick = {
                        onPick(pickerColor)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                )
            }
        }
        }
    }
}

/**
 * Label + value row paired with [DomChatSlider] inside the tint picker dialog.
 */
@Composable
private fun tintSliderRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.weight(1f).padding(end = 12.dp)
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                textAlign = TextAlign.End
            )
        }
        DomChatSlider(
            value = value,
            onValueChange = onChange,
            valueRange = valueRange,
            steps = 0,
        )
    }
}

private fun buildModelCatalogSubtitle(
    context: android.content.Context,
    status: ModelCatalogStatus,
): String {
    val sourceLabel = when (status.source) {
        ModelCatalogSource.BUNDLED -> context.getString(R.string.setting_display_model_catalog_source_bundled)
        ModelCatalogSource.DOWNLOADED -> context.getString(R.string.setting_display_model_catalog_source_downloaded)
    }
    val lastRefreshLabel = status.lastSuccessfulRefreshAt?.let {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(it))
    }
    return if (status.source == ModelCatalogSource.DOWNLOADED && lastRefreshLabel != null) {
        context.getString(
            R.string.setting_display_model_catalog_subtitle_downloaded,
            sourceLabel,
            status.entryCount,
            lastRefreshLabel,
        )
    } else {
        context.getString(
            R.string.setting_display_model_catalog_subtitle_bundled,
            sourceLabel,
            status.entryCount,
        )
    }
}
