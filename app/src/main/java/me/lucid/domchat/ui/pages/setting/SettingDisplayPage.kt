package me.lucid.domchat.ui.pages.setting

import android.os.Build
import java.text.DateFormat
import java.util.Date
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.R
import me.lucid.domchat.data.datastore.DisplaySetting
import me.lucid.domchat.data.ai.models.ModelCatalogSource
import me.lucid.domchat.data.ai.models.ModelCatalogStatus
import me.lucid.domchat.ui.components.nav.BackButton
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.roundToInt
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.data.model.BackgroundBlurMode
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.components.ui.Select
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.components.ui.permission.PermissionManager
import me.lucid.domchat.ui.components.ui.permission.PermissionNotification
import me.lucid.domchat.ui.components.ui.permission.rememberPermissionState
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import me.lucid.domchat.ui.hooks.rememberColorMode
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.rememberSharedPreferenceBoolean
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.HorizontalDivider
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.pages.setting.components.SettingsGroup
import me.lucid.domchat.ui.pages.setting.components.SettingGroupItem
import me.lucid.domchat.ui.pages.setting.components.SettingGroupInputItem
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.ColorMode
import me.lucid.domchat.utils.plus
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingDisplayPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val modelCatalogStatus by vm.modelCatalogStatus.collectAsStateWithLifecycle()
    var displaySetting by remember(settings) { mutableStateOf(settings.displaySetting) }
    var amoledDarkMode by rememberAmoledDarkMode()
    val context = LocalContext.current
    val toaster = LocalToaster.current
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)

    fun updateDisplaySetting(setting: DisplaySetting) {
        displaySetting = setting
        vm.updateSettings(
            settings.copy(
                displaySetting = setting
            )
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()

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
            // Chat Message Styles with Visual Representation
            item {
                SettingsGroup(
                    title = "Chat Message Layout & Roleplay Style"
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        me.lucid.domchat.ui.components.chat.ChatMessageStyleSelector(
                            selectedStyle = settings.displaySetting.chatMessageStyle,
                            onStyleSelected = { newStyle ->
                                vm.updateSettings(
                                    settings.copy(
                                        displaySetting = settings.displaySetting.copy(chatMessageStyle = newStyle)
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // Theme & Interface Customization
            item {
                val navController = me.lucid.domchat.ui.context.LocalNavController.current
                val isDark = LocalDarkMode.current
                
                SettingsGroup(
                    title = stringResource(R.string.setting_page_theme_setting)
                ) {
                    SettingGroupItem(
                        title = stringResource(R.string.setting_ui_customization_title),
                        subtitle = stringResource(R.string.setting_display_ui_customization_desc),
                        onClick = { navController.navigate(me.lucid.domchat.Screen.SettingUICustomization) }
                    )

                    SettingGroupItem(
                        title = "Themes",
                        subtitle = "OLED Liquid Glass",
                        trailing = {
                            Surface(
                                color = if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                            ) {
                                Text(
                                    text = "Coming Soon",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )
                }
            }

            // Global Wallpaper & Atmosphere
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
                                settings.displaySetting.copy(globalBackground = localPath)
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
                                                settings.displaySetting.copy(globalBackground = null)
                                            )
                                        }
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Remove Wallpaper",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                FilledTonalButton(
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        globalWallpaperPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = AppShapes.ButtonPill
                                ) {
                                    Text(if (globalBg.isNullOrBlank()) "Choose" else "Change")
                                }
                            }
                        }
                    )

                    // Progressive Disclosure: Dimming and Blur sliders only show when wallpaper is set
                    AnimatedVisibility(visible = !globalBg.isNullOrBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            var localDim by remember(globalDim) { mutableFloatStateOf(globalDim) }
                            var localBlur by remember(globalBlur) { mutableFloatStateOf(globalBlur) }
                            var localFadeHeight by remember(settings.displaySetting.globalBackgroundBlurHeightFraction) {
                                mutableFloatStateOf(settings.displaySetting.globalBackgroundBlurHeightFraction)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Wallpaper Dimming", style = MaterialTheme.typography.bodyMedium)
                                Text("${(localDim * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = localDim,
                                onValueChange = {
                                    if ((it * 100).toInt() != (localDim * 100).toInt()) {
                                        haptics.perform(HapticPattern.Tick)
                                    }
                                    localDim = it
                                },
                                onValueChangeFinished = {
                                    updateDisplaySetting(settings.displaySetting.copy(globalBackgroundDim = localDim))
                                },
                                valueRange = 0f..0.95f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Wallpaper Blur", style = MaterialTheme.typography.bodyMedium)
                                Text("${localBlur.roundToInt()} dp", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = localBlur,
                                onValueChange = {
                                    if (it.roundToInt() != localBlur.roundToInt()) {
                                        haptics.perform(HapticPattern.Tick)
                                    }
                                    localBlur = it
                                },
                                onValueChangeFinished = {
                                    updateDisplaySetting(settings.displaySetting.copy(globalBackgroundBlur = localBlur))
                                },
                                valueRange = 0f..100f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Blur Style", style = MaterialTheme.typography.bodyMedium)
                                Select(
                                    options = BackgroundBlurMode.entries,
                                    selectedOption = settings.displaySetting.backgroundBlurMode,
                                    onOptionSelected = {
                                        haptics.perform(HapticPattern.Tick)
                                        updateDisplaySetting(settings.displaySetting.copy(backgroundBlurMode = it))
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Fade Blur Height", style = MaterialTheme.typography.bodyMedium)
                                    Text("${(localFadeHeight * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = localFadeHeight,
                                    onValueChange = {
                                        if ((it * 100).toInt() != (localFadeHeight * 100).toInt()) {
                                            haptics.perform(HapticPattern.Tick)
                                        }
                                        localFadeHeight = it
                                    },
                                    onValueChangeFinished = {
                                        updateDisplaySetting(settings.displaySetting.copy(globalBackgroundBlurHeightFraction = localFadeHeight))
                                    },
                                    valueRange = 0.10f..1.0f
                                )
                            }
                        }
                    }
                }
            }

            // Appearance: Animation Speed Controls
            item {
                SettingsGroup(
                    title = "Interface Appearance & Transparency"
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
                                    updateDisplaySetting(settings.displaySetting.copy(animationSpeed = it))
                                },
                                optionToString = { it.title },
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isDark = LocalDarkMode.current
                        var localGlassAlpha by remember(settings.displaySetting.glassAlpha) { mutableFloatStateOf(settings.displaySetting.glassAlpha) }
                        var localGlassBlur by remember(settings.displaySetting.glassBlur) { mutableFloatStateOf(settings.displaySetting.glassBlur) }
                        var localBaseLuminance by remember(settings.displaySetting.glassBaseLuminance) { mutableFloatStateOf(settings.displaySetting.glassBaseLuminance) }
                        var localBubbleAlpha by remember(settings.displaySetting.chatBubbleAlpha) { mutableFloatStateOf(settings.displaySetting.chatBubbleAlpha) }
                        var localBubbleBlur by remember(settings.displaySetting.chatBubbleBlur) { mutableFloatStateOf(settings.displaySetting.chatBubbleBlur) }
                        var localBubbleTone by remember(settings.displaySetting.chatBubbleTone) { mutableFloatStateOf(settings.displaySetting.chatBubbleTone) }

                        val sliderColors = SliderDefaults.colors(
                            thumbColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
                            activeTrackColor = if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
                        )

                        // --- SECTION 1: PANELS & SHEETS ---
                        Text(
                            text = "Interface & Navigation Panels",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Controls background blur, opacity, and base tone across sheets, popovers, and menus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // UI Glass Presets
                        val activeUiPreset = when {
                            localGlassAlpha <= 0.10f && localBaseLuminance >= 0.65f -> "Clear Glass"
                            localBaseLuminance <= 0.20f && localGlassAlpha in 0.30f..0.60f -> "Dark Glass"
                            localBaseLuminance >= 0.60f && localGlassAlpha in 0.20f..0.45f -> "Liquid Frost"
                            else -> null
                        }
                        GlassPresetSelector(
                            selectedPreset = activeUiPreset,
                            onSelect = { preset ->
                                haptics.perform(HapticPattern.Pop)
                                when (preset) {
                                    "Clear Glass" -> {
                                        localGlassAlpha = 0.05f
                                        localBaseLuminance = 0.80f
                                        localGlassBlur = 24f
                                        updateDisplaySetting(settings.displaySetting.copy(
                                            glassAlpha = 0.05f,
                                            glassBaseLuminance = 0.80f,
                                            glassBlur = 24f
                                        ))
                                    }
                                    "Dark Glass" -> {
                                        localGlassAlpha = 0.40f
                                        localBaseLuminance = 0.0f
                                        localGlassBlur = 18f
                                        updateDisplaySetting(settings.displaySetting.copy(
                                            glassAlpha = 0.40f,
                                            glassBaseLuminance = 0.0f,
                                            glassBlur = 18f
                                        ))
                                    }
                                    "Liquid Frost" -> {
                                        localGlassAlpha = 0.28f
                                        localBaseLuminance = 0.75f
                                        localGlassBlur = 22f
                                        updateDisplaySetting(settings.displaySetting.copy(
                                            glassAlpha = 0.28f,
                                            glassBaseLuminance = 0.75f,
                                            glassBlur = 22f
                                        ))
                                    }
                                }
                            }
                        )

                        // Glass Opacity / Transparency Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("UI Glass Opacity", style = MaterialTheme.typography.bodyMedium)
                            Text("${(localGlassAlpha * 100).roundToInt()}% (${((1f - localGlassAlpha) * 100).roundToInt()}% transparent)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = localGlassAlpha,
                            onValueChange = {
                                val rounded = (it * 100f).roundToInt() / 100f
                                if (rounded != localGlassAlpha) {
                                    localGlassAlpha = rounded
                                    updateDisplaySetting(settings.displaySetting.copy(glassAlpha = rounded))
                                }
                            },
                            valueRange = 0f..1.0f,
                            colors = sliderColors
                        )

                        // Glass Blur Slider (0 to 100 dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("UI Glass Blur Radius", style = MaterialTheme.typography.bodyMedium)
                            Text("${localGlassBlur.roundToInt()} dp", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = localGlassBlur,
                            onValueChange = {
                                val rounded = it.roundToInt().toFloat()
                                if (rounded != localGlassBlur) {
                                    localGlassBlur = rounded
                                    updateDisplaySetting(settings.displaySetting.copy(glassBlur = rounded))
                                }
                            },
                            valueRange = 0f..100f,
                            colors = sliderColors
                        )

                        // Glass Foundation Tone Slider (0.0 = OLED Black, 1.0 = Pure White)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Glass Foundation Tone", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                when {
                                    localBaseLuminance <= 0.05f -> "Pitch Black (0%)"
                                    localBaseLuminance >= 0.95f -> "Pure White (100%)"
                                    else -> "${(localBaseLuminance * 100).roundToInt()}% Tint"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = localBaseLuminance,
                            onValueChange = {
                                val rounded = (it * 100f).roundToInt() / 100f
                                if (rounded != localBaseLuminance) {
                                    localBaseLuminance = rounded
                                    updateDisplaySetting(settings.displaySetting.copy(glassBaseLuminance = rounded))
                                }
                            },
                            valueRange = 0f..1.0f,
                            colors = sliderColors
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                        )

                        // --- SECTION 2: CHAT BUBBLES & REASONING BADGES ---
                        Text(
                            text = "Chat Bubbles & Reasoning Glass",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Controls transparency, tint, and blur for message bubbles and AI reasoning cards.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Chat Bubble Glass Presets
                        val activeBubblePreset = when {
                            localBubbleAlpha <= 0.08f && localBubbleTone >= 0.70f -> "Clear Glass"
                            localBubbleTone <= 0.20f && localBubbleAlpha in 0.30f..0.65f -> "Dark Glass"
                            localBubbleTone >= 0.80f && localBubbleAlpha >= 0.20f -> "Liquid Frost"
                            else -> null
                        }
                        GlassPresetSelector(
                            selectedPreset = activeBubblePreset,
                            onSelect = { preset ->
                                haptics.perform(HapticPattern.Pop)
                                when (preset) {
                                    "Clear Glass" -> {
                                        localBubbleAlpha = 0.0f
                                        localBubbleTone = 0.85f
                                        localBubbleBlur = 24f
                                        updateDisplaySetting(settings.displaySetting.copy(
                                            chatBubbleAlpha = 0.0f,
                                            chatBubbleTone = 0.85f,
                                            chatBubbleBlur = 24f
                                        ))
                                    }
                                    "Dark Glass" -> {
                                        localBubbleAlpha = 0.50f
                                        localBubbleTone = 0.05f
                                        localBubbleBlur = 18f
                                        updateDisplaySetting(settings.displaySetting.copy(
                                            chatBubbleAlpha = 0.50f,
                                            chatBubbleTone = 0.05f,
                                            chatBubbleBlur = 18f
                                        ))
                                    }
                                    "Liquid Frost" -> {
                                        localBubbleAlpha = 0.30f
                                        localBubbleTone = 0.90f
                                        localBubbleBlur = 20f
                                        updateDisplaySetting(settings.displaySetting.copy(
                                            chatBubbleAlpha = 0.30f,
                                            chatBubbleTone = 0.90f,
                                            chatBubbleBlur = 20f
                                        ))
                                    }
                                }
                            }
                        )

                        // Chat Bubble Opacity Slider (0% to 100%)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bubble Opacity", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                when {
                                    localBubbleAlpha <= 0.01f -> "Clear Glass (0%)"
                                    localBubbleAlpha >= 0.98f -> "Solid (100%)"
                                    else -> "${(localBubbleAlpha * 100).roundToInt()}%"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = localBubbleAlpha,
                            onValueChange = {
                                val rounded = (it * 100f).roundToInt() / 100f
                                if (rounded != localBubbleAlpha) {
                                    localBubbleAlpha = rounded
                                    updateDisplaySetting(settings.displaySetting.copy(chatBubbleAlpha = rounded))
                                }
                            },
                            valueRange = 0f..1.0f,
                            colors = sliderColors
                        )

                        // Chat Bubble Blur Slider (0 to 100 dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bubble Blur Radius", style = MaterialTheme.typography.bodyMedium)
                            Text("${localBubbleBlur.roundToInt()} dp", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = localBubbleBlur,
                            onValueChange = {
                                val rounded = it.roundToInt().toFloat()
                                if (rounded != localBubbleBlur) {
                                    localBubbleBlur = rounded
                                    updateDisplaySetting(settings.displaySetting.copy(chatBubbleBlur = rounded))
                                }
                            },
                            valueRange = 0f..100f,
                            colors = sliderColors
                        )

                        // Chat Bubble Base Tone Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bubble Base Tone", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                when {
                                    localBubbleTone <= 0.05f -> "Pure Black (0%)"
                                    localBubbleTone >= 0.95f -> "Pure White (100%)"
                                    else -> "${(localBubbleTone * 100).roundToInt()}%"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = localBubbleTone,
                            onValueChange = {
                                val rounded = (it * 100f).roundToInt() / 100f
                                if (rounded != localBubbleTone) {
                                    localBubbleTone = rounded
                                    updateDisplaySetting(settings.displaySetting.copy(chatBubbleTone = rounded))
                                }
                            },
                            valueRange = 0f..1.0f,
                            colors = sliderColors
                        )
                    }
                }
            }



            // Basic Settings
            item {
                var createNewConversationOnStart by rememberSharedPreferenceBoolean(
                    "create_new_conversation_on_start",
                    true
                )
                SettingsGroup(
                    title = stringResource(R.string.setting_page_basic_settings)
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
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_check_updates_title),
                        subtitle = stringResource(R.string.setting_display_check_updates_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.checkForUpdates,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(checkForUpdates = it))
                                }
                            )
                        }
                    )
                }
            }

//            item {
//                ListItem(
//                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
//                    headlineContent = {
//                        Text(stringResource(R.string.setting_display_page_developer_mode))
//                    },
//                    supportingContent = {
//                        Text(stringResource(R.string.setting_display_page_developer_mode_desc))
//                    },
//                    trailingContent = {
//                        HapticSwitch(
//                            checked = settings.developerMode,
//                            onCheckedChange = {
//                                vm.updateSettings(settings.copy(developerMode = it))
//                            }
//                        )
//                    },
//                )
//            }

        }
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

@Composable
private fun GlassPresetSelector(
    selectedPreset: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf("Clear Glass", "Dark Glass", "Liquid Frost")
    val isDark = LocalDarkMode.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF141522).copy(alpha = 0.70f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.50f),
        border = BorderStroke(0.5.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            presets.forEach { name ->
                val isSelected = selectedPreset == name
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                if (isDark) Color.White.copy(alpha = 0.20f) else MaterialTheme.colorScheme.primaryContainer
                            } else Color.Transparent
                        )
                        .clickable { onSelect(name) }
                        .padding(vertical = 8.dp, horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = if (isSelected) {
                            if (isDark) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            if (isDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
