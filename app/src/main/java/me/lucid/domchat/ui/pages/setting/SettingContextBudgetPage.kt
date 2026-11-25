package me.lucid.domchat.ui.pages.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import me.lucid.domchat.ui.components.ui.DomChatSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.data.model.ContextBudgetConfig
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.theme.LocalDarkMode
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingContextBudgetPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val isDark = LocalDarkMode.current
    val cardBg = Color(0xFF000000)

    val currentConfig = settings.displaySetting.contextBudgetConfig

    var maxContextTokens by remember(currentConfig.maxContextTokens) {
        mutableIntStateOf(currentConfig.maxContextTokens)
    }
    var maxResponseTokens by remember(currentConfig.maxResponseTokens) {
        mutableIntStateOf(currentConfig.maxResponseTokens)
    }
    var historyPercent by remember(currentConfig.chatHistoryBudgetPercent) {
        mutableIntStateOf(currentConfig.chatHistoryBudgetPercent)
    }
    var memoryPercent by remember(currentConfig.memoryLorebookBudgetPercent) {
        mutableIntStateOf(currentConfig.memoryLorebookBudgetPercent)
    }
    var autoPrune by remember(currentConfig.autoPruneOverflow) {
        mutableStateOf(currentConfig.autoPruneOverflow)
    }
    var enableCompaction by remember(currentConfig.enableCompaction) {
        mutableStateOf(currentConfig.enableCompaction)
    }
    var compactionThreshold by remember(currentConfig.compactionThresholdPercent) {
        mutableIntStateOf(currentConfig.compactionThresholdPercent)
    }

    val systemPercent = (100 - historyPercent - memoryPercent).coerceAtLeast(10)
    val availableInputTokens = (maxContextTokens - maxResponseTokens).coerceAtLeast(1024)
    val historyTokens = ((availableInputTokens * historyPercent) / 100).coerceAtLeast(1024)
    val memoryTokens = ((availableInputTokens * memoryPercent) / 100).coerceAtLeast(256)
    val systemTokens = ((availableInputTokens * systemPercent) / 100).coerceAtLeast(512)

    fun persist(
        newMaxContext: Int = maxContextTokens,
        newMaxResponse: Int = maxResponseTokens,
        newHistoryPercent: Int = historyPercent,
        newMemoryPercent: Int = memoryPercent,
        newAutoPrune: Boolean = autoPrune,
        newEnableCompaction: Boolean = enableCompaction,
        newCompactionThreshold: Int = compactionThreshold,
    ) {
        val newSystem = (100 - newHistoryPercent - newMemoryPercent).coerceAtLeast(10)
        val config = ContextBudgetConfig(
            maxContextTokens = newMaxContext,
            maxResponseTokens = newMaxResponse,
            chatHistoryBudgetPercent = newHistoryPercent,
            memoryLorebookBudgetPercent = newMemoryPercent,
            characterSystemBudgetPercent = newSystem,
            autoPruneOverflow = newAutoPrune,
            enableCompaction = newEnableCompaction,
            compactionThresholdPercent = newCompactionThreshold,
        )
        vm.updateSettings(
            settings.copy(
                displaySetting = settings.displaySetting.copy(
                    contextBudgetConfig = config
                )
            )
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = "Context & Token Budget",
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton() }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(contentPadding),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Allocation Overview Card
            item {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            Modifier.liquidGlassSurface(
                                shape = RoundedCornerShape(16.dp),
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                fillColor = cardBg,
                            )
                        ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Budget Allocation",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${maxContextTokens / 1024}k total limit",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }

                        // Segmented Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF000000))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .weight(systemPercent.toFloat())
                                        .fillMaxHeight()
                                        .background(Color(0xFF8B5CF6))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(historyPercent.toFloat())
                                        .fillMaxHeight()
                                        .background(Color(0xFF10B981))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(memoryPercent.toFloat())
                                        .fillMaxHeight()
                                        .background(Color(0xFFF59E0B))
                                )
                            }
                        }

                        // Legend with calculated tokens
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF8B5CF6)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("System Prompt", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                                    Text("$systemTokens tk ($systemPercent%)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color.Black)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Chat History", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                                    Text("$historyTokens tk ($historyPercent%)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color.Black)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Memory & Lore", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                                    Text("$memoryTokens tk ($memoryPercent%)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            // Context Window Presets & Slider
            item {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            Modifier.liquidGlassSurface(
                                shape = RoundedCornerShape(16.dp),
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                fillColor = cardBg,
                            )
                        ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Total Context Window",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        // Quick presets chips
                        val presets = listOf(8192, 16384, 32768, 65536, 131072, 204800)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.take(3).forEach { tokens ->
                                val selected = maxContextTokens == tokens
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF000000),
                                    border = if (selected) {
                                        BorderStroke(1.dp, LiquidGlassTokens.SpecularBorderBrush)
                                    } else {
                                        BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                    },
                                    onClick = {
                                        haptics.perform(HapticPattern.Tick)
                                        maxContextTokens = tokens
                                        persist(newMaxContext = tokens)
                                    }
                                ) {
                                    Text(
                                        text = "${tokens / 1024}k",
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        ),
                                        color = if (selected) Color.White else Color.White.copy(alpha = 0.60f)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.drop(3).forEach { tokens ->
                                val selected = maxContextTokens == tokens
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF000000),
                                    border = if (selected) {
                                        BorderStroke(1.dp, LiquidGlassTokens.SpecularBorderBrush)
                                    } else {
                                        BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                    },
                                    onClick = {
                                        haptics.perform(HapticPattern.Tick)
                                        maxContextTokens = tokens
                                        persist(newMaxContext = tokens)
                                    }
                                ) {
                                    Text(
                                        text = "${tokens / 1024}k",
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        ),
                                        color = if (selected) Color.White else Color.White.copy(alpha = 0.60f)
                                    )
                                }
                            }
                        }

                        // Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Window Capacity", style = MaterialTheme.typography.bodyMedium)
                                Text("$maxContextTokens tokens", style = MaterialTheme.typography.labelLarge, color = Color.White)
                            }
                            DomChatSlider(
                                value = maxContextTokens.toFloat(),
                                onValueChange = {
                                    val step = 4096f
                                    val rounded = ((it / step).roundToInt() * step.toInt()).coerceIn(4096, 262144)
                                    if (rounded != maxContextTokens) {
                                        maxContextTokens = rounded
                                    }
                                },
                                onValueChangeFinished = {
                                    haptics.perform(HapticPattern.Tick)
                                    persist(newMaxContext = maxContextTokens)
                                },
                                valueRange = 4096f..262144f
                            )
                        }
                    }
                }
            }

            // Allocation Sliders Card
            item {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            Modifier.liquidGlassSurface(
                                shape = RoundedCornerShape(16.dp),
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                fillColor = cardBg,
                            )
                        ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Token Allocation Division",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        // Chat History
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Chat, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Chat History Budget", style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("$historyPercent% ($historyTokens tk)", style = MaterialTheme.typography.labelLarge, color = Color(0xFF10B981))
                            }
                            DomChatSlider(
                                value = historyPercent.toFloat(),
                                onValueChange = {
                                    val newHist = it.roundToInt().coerceIn(15, 80)
                                    if (newHist + memoryPercent <= 90) {
                                        historyPercent = newHist
                                    }
                                },
                                onValueChangeFinished = {
                                    haptics.perform(HapticPattern.Tick)
                                    persist(newHistoryPercent = historyPercent)
                                },
                                valueRange = 15f..80f,
                                steps = 64
                            )
                        }

                        // Memory & Lorebooks
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.AutoStories, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Memory & Lorebooks", style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("$memoryPercent% ($memoryTokens tk)", style = MaterialTheme.typography.labelLarge, color = Color(0xFFF59E0B))
                            }
                            DomChatSlider(
                                value = memoryPercent.toFloat(),
                                onValueChange = {
                                    val newMem = it.roundToInt().coerceIn(5, 50)
                                    if (newMem + historyPercent <= 90) {
                                        memoryPercent = newMem
                                    }
                                },
                                onValueChangeFinished = {
                                    haptics.perform(HapticPattern.Tick)
                                    persist(newMemoryPercent = memoryPercent)
                                },
                                valueRange = 5f..50f,
                                steps = 44
                            )
                        }

                        // System Prompt (automatic remaining)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Psychology, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("System Prompt & Persona", style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("$systemPercent% ($systemTokens tk)", style = MaterialTheme.typography.labelLarge, color = Color(0xFF8B5CF6))
                            }
                            Text(
                                text = "Automatically takes the remaining context allocation ($systemPercent%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            // Output Token Reservation Card
            item {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            Modifier.liquidGlassSurface(
                                shape = RoundedCornerShape(16.dp),
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                fillColor = cardBg,
                            )
                        ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Generation & Output Reservation",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Max Response Tokens", style = MaterialTheme.typography.bodyMedium)
                                Text("$maxResponseTokens tk", style = MaterialTheme.typography.labelLarge, color = Color.White)
                            }
                            DomChatSlider(
                                value = maxResponseTokens.toFloat(),
                                onValueChange = {
                                    val step = 256f
                                    val rounded = ((it / step).roundToInt() * step.toInt()).coerceIn(512, 16384)
                                    if (rounded != maxResponseTokens) {
                                        maxResponseTokens = rounded
                                    }
                                },
                                onValueChangeFinished = {
                                    haptics.perform(HapticPattern.Tick)
                                    persist(newMaxResponse = maxResponseTokens)
                                },
                                valueRange = 512f..16384f
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Prune Overflow Turns",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Automatically drop oldest message turns to maintain context headroom",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                            HapticSwitch(
                                checked = autoPrune,
                                onCheckedChange = {
                                    autoPrune = it
                                    persist(newAutoPrune = it)
                                }
                            )
                        }
                    }
                }
            }

            // Context Compaction Card
            item {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            Modifier.liquidGlassSurface(
                                shape = RoundedCornerShape(16.dp),
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                fillColor = cardBg,
                            )
                        ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Context Compaction",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Summarize older conversation turns into a rolling context summary when approaching the threshold, freeing room for new messages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Compaction",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Older turns are condensed into a summary that stays in context",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                            HapticSwitch(
                                checked = enableCompaction,
                                onCheckedChange = {
                                    enableCompaction = it
                                    persist(newEnableCompaction = it)
                                }
                            )
                        }

                        AnimatedVisibility(
                            visible = enableCompaction,
                            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 300f)) +
                                    androidx.compose.animation.slideInVertically(
                                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 300f)
                                    ) { -it },
                            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 300f)) +
                                   androidx.compose.animation.slideOutVertically(
                                       animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 300f)
                                   ) { -it },
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Compaction Threshold", style = MaterialTheme.typography.bodyMedium)
                                    Text("$compactionThreshold%", style = MaterialTheme.typography.labelLarge, color = Color.White)
                                }
                                DomChatSlider(
                                    value = compactionThreshold.toFloat(),
                                    onValueChange = {
                                        compactionThreshold = it.roundToInt().coerceIn(50, 95)
                                    },
                                    onValueChangeFinished = {
                                        haptics.perform(HapticPattern.Tick)
                                        persist(newCompactionThreshold = compactionThreshold)
                                    },
                                    valueRange = 50f..95f,
                                    steps = 45,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
