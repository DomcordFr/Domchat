package me.lucid.domchat.ui.components.ai

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import me.lucid.domchat.ui.modifier.fadeEdges
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LightbulbCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import me.lucid.ai.core.ReasoningLevel
import me.lucid.ai.provider.Model
import me.lucid.ai.provider.ReasoningConfig
import me.lucid.ai.provider.ReasoningModeType
import me.lucid.domchat.R
import me.lucid.domchat.Screen
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.liquidGlassSurface
import androidx.compose.runtime.derivedStateOf
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import me.lucid.domchat.ui.components.ui.ToggleSurface
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.IconButton

fun resolveReasoningConfig(model: Model?): ReasoningConfig {
    val modelConfig = model?.reasoningConfig
    if (modelConfig != null) return modelConfig
    val modelId = model?.modelId?.lowercase() ?: ""

    return when {
        // Claude series -> Budget
        modelId.contains("claude") -> ReasoningConfig(
            type = ReasoningModeType.BUDGET,
            minTokens = 1024,
            maxTokens = 64_000,
            presetTokens = listOf(1024, 4096, 8192, 16_000, 32_000, 64_000)
        )
        // Gemini 2.5 -> Budget (0..65536)
        modelId.contains("gemini-2.5") -> ReasoningConfig(
            type = ReasoningModeType.BUDGET,
            minTokens = 0,
            maxTokens = 65_536,
            presetTokens = listOf(0, 1024, 4096, 8192, 16_000, 32_000)
        )
        // DashScope Qwen -> Budget
        modelId.contains(Regex("qwen.*(max|plus|turbo|3\\.|2\\.5)")) -> ReasoningConfig(
            type = ReasoningModeType.BUDGET,
            minTokens = 1024,
            maxTokens = 32_768,
            presetTokens = listOf(1024, 4096, 8192, 16_000, 32_000)
        )
        // DeepSeek, InternLM, Grok, Doubao Ark, GLM, Hunyuan -> Binary
        modelId.contains(Regex("deepseek|intern|grok|doubao|glm|hunyuan")) -> ReasoningConfig(
            type = ReasoningModeType.BINARY,
            supportedLevels = listOf("off", "auto")
        )
        // OpenAI o-series / gpt-5 / Gemini 3 -> Effort
        modelId.contains(Regex("o[134]|gpt-5|gemini-3")) -> ReasoningConfig(
            type = ReasoningModeType.EFFORT,
            supportedLevels = listOf("off", "auto", "low", "medium", "high", "max")
        )
        // Default backward compatible effort
        else -> ReasoningConfig(
            type = ReasoningModeType.EFFORT,
            supportedLevels = listOf("off", "auto", "low", "medium", "high")
        )
    }
}

@Composable
fun ReasoningButton(
    modifier: Modifier = Modifier,
    model: Model? = null,
    onlyIcon: Boolean = false,
    reasoningTokens: Int,
    shape: Shape = me.lucid.domchat.ui.theme.AppShapes.CardMedium,
    onUpdateReasoningTokens: (Int) -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var showReasoningPicker by remember { mutableStateOf(false) }

    if (showReasoningPicker) {
        ReasoningPicker(
            model = model,
            reasoningTokens = reasoningTokens,
            onDismissRequest = { showReasoningPicker = false },
            onUpdateReasoningTokens = onUpdateReasoningTokens
        )
    }

    ToggleSurface(
        checked = ReasoningLevel.fromBudgetTokens(reasoningTokens).isEnabled,
        checkedColor = Color.Transparent,
        uncheckedColor = Color.Transparent,
        contentColor = contentColor,
        onClick = {
            showReasoningPicker = true
        },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = if (onlyIcon) 8.dp else 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                )
            }
            if (!onlyIcon) Text(stringResource(R.string.setting_provider_page_reasoning))
        }
    }
}

@Composable
fun ReasoningPicker(
    model: Model? = null,
    reasoningTokens: Int,
    onDismissRequest: () -> Unit = {},
    onUpdateReasoningTokens: (Int) -> Unit,
    isChatPage: Boolean = false,
) {
    val config = remember(model) { resolveReasoningConfig(model) }
    val currentLevel = ReasoningLevel.fromBudgetTokens(reasoningTokens)
    val amoledMode by rememberAmoledDarkMode()
    val isDarkMode = LocalDarkMode.current
    val isAmoled = amoledMode && isDarkMode
    
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        DomChatInWindowBottomSheet(
            visible = true,
            onDismissRequest = onDismissRequest,
            containerColor = if (isChatPage) Color.Transparent else Color(0xFF000000),
            dragHandle = { DomChatSheetDragHandle() },
            maxHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * 0.88f).dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
            ) {
                DomChatSheetHeader(
                    title = "Thinking Budget",
                    subtitle = "Configure model reasoning depth",
                    onCloseClick = onDismissRequest,
                )
                val scrollState = rememberScrollState()
                val canScrollUp by remember { derivedStateOf { scrollState.value > 0 } }
                val canScrollDown by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 40f)
                        .verticalScroll(scrollState)
                        .padding(top = 8.dp, bottom = 20.dp),
                ) {
                when (config.type) {
                ReasoningModeType.BINARY -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ReasoningOptionItem(
                            selected = currentLevel == ReasoningLevel.OFF,
                            icon = { Icon(Icons.Rounded.LightbulbCircle, null, modifier = Modifier.size(20.dp)) },
                            title = stringResource(id = R.string.reasoning_off),
                            subtitle = stringResource(id = R.string.reasoning_off_desc),
                            onClick = { onUpdateReasoningTokens(0) },
                            isAmoled = isAmoled,
                            isChatPage = isChatPage
                        )
                        ReasoningOptionItem(
                            selected = currentLevel != ReasoningLevel.OFF,
                            icon = { Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(20.dp)) },
                            title = stringResource(id = R.string.reasoning_auto),
                            subtitle = stringResource(id = R.string.reasoning_auto_desc),
                            onClick = { onUpdateReasoningTokens(-1) },
                            isAmoled = isAmoled,
                            isChatPage = isChatPage
                        )
                    }
                }

                ReasoningModeType.EFFORT -> {
                    val supported = config.supportedLevels.ifEmpty { listOf("off", "auto", "low", "medium", "high", "max") }
                    val showOff = supported.contains("off")
                    val showAuto = supported.contains("auto")

                    data class EffortOption(
                        val level: ReasoningLevel,
                        val tokens: Int,
                        val title: String,
                        val desc: String,
                    )

                    val effortOptions = buildList {
                        if (supported.contains("low")) {
                            add(EffortOption(ReasoningLevel.LOW, 1024, stringResource(R.string.reasoning_light), stringResource(R.string.reasoning_light_desc)))
                        }
                        if (supported.contains("medium")) {
                            add(EffortOption(ReasoningLevel.MEDIUM, 16_000, stringResource(R.string.reasoning_medium), stringResource(R.string.reasoning_medium_desc)))
                        }
                        if (supported.contains("high")) {
                            add(EffortOption(ReasoningLevel.HIGH, 32_000, stringResource(R.string.reasoning_heavy), stringResource(R.string.reasoning_heavy_desc)))
                        }
                        if (supported.contains("max") || supported.contains("xhigh")) {
                            add(EffortOption(ReasoningLevel.MAX, 64_000, "Max", "Maximum reasoning effort"))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (showOff) {
                            ReasoningOptionItem(
                                selected = currentLevel == ReasoningLevel.OFF,
                                icon = { Icon(Icons.Rounded.LightbulbCircle, null, modifier = Modifier.size(20.dp)) },
                                title = stringResource(id = R.string.reasoning_off),
                                subtitle = stringResource(id = R.string.reasoning_off_desc),
                                onClick = { onUpdateReasoningTokens(0) },
                                isAmoled = isAmoled,
                                isChatPage = isChatPage
                            )
                        }
                        if (showAuto) {
                            ReasoningOptionItem(
                                selected = currentLevel == ReasoningLevel.AUTO,
                                icon = { Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(20.dp)) },
                                title = stringResource(id = R.string.reasoning_auto),
                                subtitle = stringResource(id = R.string.reasoning_auto_desc),
                                onClick = { onUpdateReasoningTokens(-1) },
                                isAmoled = isAmoled,
                                isChatPage = isChatPage
                            )
                        }
                        effortOptions.forEach { option ->
                            ReasoningOptionItem(
                                selected = currentLevel == option.level,
                                icon = { Icon(Icons.Rounded.Lightbulb, null, modifier = Modifier.size(20.dp)) },
                                title = option.title,
                                subtitle = option.desc,
                                onClick = { onUpdateReasoningTokens(option.tokens) },
                                isAmoled = isAmoled,
                                isChatPage = isChatPage
                            )
                        }
                    }
                }

                ReasoningModeType.BUDGET -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ReasoningOptionItem(
                            selected = currentLevel == ReasoningLevel.OFF,
                            icon = { Icon(Icons.Rounded.LightbulbCircle, null, modifier = Modifier.size(20.dp)) },
                            title = stringResource(id = R.string.reasoning_off),
                            subtitle = stringResource(id = R.string.reasoning_off_desc),
                            onClick = { onUpdateReasoningTokens(0) },
                            isAmoled = isAmoled,
                            isChatPage = isChatPage
                        )
                        if (config.minTokens == 0) {
                            ReasoningOptionItem(
                                selected = currentLevel == ReasoningLevel.AUTO,
                                icon = { Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(20.dp)) },
                                title = stringResource(id = R.string.reasoning_auto),
                                subtitle = stringResource(id = R.string.reasoning_auto_desc),
                                onClick = { onUpdateReasoningTokens(-1) },
                                isAmoled = isAmoled,
                                isChatPage = isChatPage
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.reasoning_custom),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    val presets = config.presetTokens.filter { it in config.minTokens..config.maxTokens }
                    if (presets.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            presets.forEach { tokens ->
                                val isSelected = reasoningTokens == tokens
                                val formattedTokens = if (tokens >= 1000) "${tokens / 1000}K tokens" else "$tokens tokens"
                                ReasoningOptionItem(
                                    selected = isSelected,
                                    icon = { Icon(Icons.Rounded.Lightbulb, null, modifier = Modifier.size(20.dp)) },
                                    title = formattedTokens,
                                    subtitle = stringResource(R.string.assistant_page_thinking_budget),
                                    onClick = { onUpdateReasoningTokens(tokens) },
                                    isAmoled = isAmoled,
                                    isChatPage = isChatPage
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

// Matches SettingGroupItem layout exactly but with selection support
@Composable
private fun ReasoningOptionItem(
    selected: Boolean,
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isAmoled: Boolean,  // Only use Color.Black when actually in OLED mode
    isChatPage: Boolean = false,
) {
    val haptics = me.lucid.domchat.ui.hooks.rememberPremiumHaptics()
    val itemShape = RoundedCornerShape(22.dp)
    
    val isDark = LocalDarkMode.current

    val animatedTitleColor by animateColorAsState(
        targetValue = if (selected) Color.White else (if (isDark) Color(0xFFEEEEEE) else MaterialTheme.colorScheme.onSurface),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "reasoning_title_color"
    )
    val animatedSubtitleColor by animateColorAsState(
        targetValue = if (selected) Color.White.copy(alpha = 0.75f) else (if (isDark) Color(0xFFAAAAAA) else MaterialTheme.colorScheme.onSurfaceVariant),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "reasoning_subtitle_color"
    )
    val borderBrush = if (selected) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle

    val itemModifier = if (isChatPage) {
        Modifier
            .fillMaxWidth()
            .liquidGlassSurface(
                shape = itemShape,
                fillColor = Color.Transparent,
                borderWidth = if (selected) 1.dp else 0.5.dp,
                specularBrush = borderBrush,
            )
            .clickable {
                haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
                onClick()
            }
    } else {
        Modifier
            .fillMaxWidth()
            .clip(itemShape)
            .background(Color(0xFF000000))
            .border(if (selected) 1.dp else 0.5.dp, borderBrush, itemShape)
            .clickable {
                haptics.perform(me.lucid.domchat.ui.hooks.HapticPattern.Pop)
                onClick()
            }
    }

    Row(
        modifier = itemModifier
            .animateContentSize(
                animationSpec = spring<IntSize>(
                    dampingRatio = 0.85f,
                    stiffness = 300f
                )
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = animatedTitleColor,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(spring(dampingRatio = 0.85f, stiffness = 300f)) + scaleIn(spring(dampingRatio = 0.85f, stiffness = 300f)),
                    exit = fadeOut(spring(dampingRatio = 0.85f, stiffness = 300f)) + scaleOut(spring(dampingRatio = 0.85f, stiffness = 300f))
                ) {
                    val badgeModifier = if (isChatPage) {
                        Modifier.liquidGlassSurface(
                            shape = RoundedCornerShape(8.dp),
                            fillColor = Color.Transparent,
                            borderWidth = 0.5.dp,
                            specularBrush = LiquidGlassTokens.SpecularBorderBrush
                        )
                    } else {
                        Modifier.border(BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderBrush), RoundedCornerShape(8.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChatPage) Color.Transparent else Color(0xFF000000),
                        modifier = badgeModifier
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
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = animatedSubtitleColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReasoningLevelCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    description: @Composable () -> Unit = {},
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(22.dp),
    containerColor: Color? = null,
    isChatPage: Boolean = false,
) {
    val amoledMode by rememberAmoledDarkMode()
    val isDarkMode = LocalDarkMode.current
    val isAmoled = amoledMode && isDarkMode
    
    val targetContainerColor = if (isChatPage) Color.Transparent else Color(0xFF000000)
    
    val defaultContentColor = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface
    val resolvedContentColor = if (selected) Color.White else defaultContentColor
    
    val elevation = 0.dp

    val cardElevation = CardDefaults.cardElevation(defaultElevation = elevation)
    val cardColors = CardDefaults.cardColors(
        containerColor = targetContainerColor,
        contentColor = resolvedContentColor
    )
    val resolvedShape = RoundedCornerShape(22.dp)
    val borderBrush = if (selected) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle
    val targetBorderWidth = if (selected) 1.dp else 0.5.dp
    val animatedBorderWidth by animateDpAsState(
        targetValue = targetBorderWidth,
        animationSpec = spring(stiffness = 400f),
        label = "reasoning_card_border_width"
    )

    val cardModifier = if (isChatPage) {
        modifier.liquidGlassSurface(
            shape = resolvedShape,
            fillColor = Color.Transparent,
            borderWidth = animatedBorderWidth,
            specularBrush = borderBrush
        )
    } else {
        modifier.border(animatedBorderWidth, borderBrush, resolvedShape)
    }

    Card(
        onClick = onClick,
        modifier = cardModifier,
        shape = resolvedShape,
        elevation = cardElevation,
        colors = cardColors
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        title()
                    }
                    AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn(spring(stiffness = 400f)) + scaleIn(spring(stiffness = 400f)),
                        exit = fadeOut(spring(stiffness = 400f)) + scaleOut(spring(stiffness = 400f))
                    ) {
                        val badgeModifier = if (isChatPage) {
                            Modifier.liquidGlassSurface(
                                shape = RoundedCornerShape(8.dp),
                                fillColor = Color.Transparent,
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderBrush
                            )
                        } else {
                            Modifier.border(BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderBrush), RoundedCornerShape(8.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isChatPage) Color.Transparent else Color(0xFF000000),
                            modifier = badgeModifier
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    description()
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ReasoningPickerPreview() {
    MaterialTheme {
        var reasoningTokens by remember { mutableIntStateOf(0) }
        ReasoningPicker(
            onDismissRequest = {},
            reasoningTokens = reasoningTokens,
            onUpdateReasoningTokens = {
                reasoningTokens = it
            }
        )
    }
}
