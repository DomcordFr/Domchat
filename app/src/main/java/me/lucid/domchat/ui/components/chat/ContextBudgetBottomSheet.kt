package me.lucid.domchat.ui.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import me.lucid.domchat.ui.components.ui.DomChatGlassSwitch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.model.ContextBudgetConfig
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.components.ui.DomChatSlider
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor

fun formatTokens(tokens: Int): String {
    return when {
        tokens >= 1_000_000 -> "${tokens / 1_000_000}M"
        tokens >= 1024 -> "${tokens / 1024}k"
        else -> tokens.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextBudgetBottomSheet(
    settings: Settings,
    perChatBudget: ContextBudgetConfig? = null,
    isPerChatMode: Boolean = false,
    onSaveGlobalBudget: ((ContextBudgetConfig) -> Unit)? = null,
    onSavePerChatBudget: ((ContextBudgetConfig?) -> Unit)? = null,
    onDismissRequest: () -> Unit
) {
    val isDark = LocalDarkMode.current

    val initialBudget = if (isPerChatMode && perChatBudget != null) {
        perChatBudget
    } else {
        settings.displaySetting.contextBudgetConfig
    }

    var inheritGlobal by remember { mutableStateOf(isPerChatMode && perChatBudget == null) }
    var maxContextTokens by remember { mutableIntStateOf(initialBudget.maxContextTokens) }
    var maxResponseTokens by remember { mutableIntStateOf(initialBudget.maxResponseTokens) }
    var systemPercent by remember { mutableIntStateOf(initialBudget.characterSystemBudgetPercent) }
    var historyPercent by remember { mutableIntStateOf(initialBudget.chatHistoryBudgetPercent) }
    var memoryPercent by remember { mutableIntStateOf(initialBudget.memoryLorebookBudgetPercent) }
    var autoPrune by remember { mutableStateOf(initialBudget.autoPruneOverflow) }
    var enableCompaction by remember { mutableStateOf(initialBudget.enableCompaction) }
    var compactionThreshold by remember { mutableIntStateOf(initialBudget.compactionThresholdPercent) }

    fun commitChanges() {
        val newConfig = ContextBudgetConfig(
            maxContextTokens = maxContextTokens,
            maxResponseTokens = maxResponseTokens,
            characterSystemBudgetPercent = systemPercent,
            chatHistoryBudgetPercent = historyPercent,
            memoryLorebookBudgetPercent = memoryPercent,
            autoPruneOverflow = autoPrune,
            enableCompaction = enableCompaction,
            compactionThresholdPercent = compactionThreshold
        )
        if (isPerChatMode) {
            onSavePerChatBudget?.invoke(if (inheritGlobal) null else newConfig)
        } else {
            onSaveGlobalBudget?.invoke(newConfig)
        }
    }

    val availableInputTokens = (maxContextTokens - maxResponseTokens).coerceAtLeast(1024)
    val systemTokens = ((availableInputTokens * systemPercent) / 100).coerceAtLeast(512)
    val historyTokens = ((availableInputTokens * historyPercent) / 100).coerceAtLeast(1024)
    val memoryTokens = ((availableInputTokens * memoryPercent) / 100).coerceAtLeast(256)

    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val borderBrush = LiquidGlassTokens.SpecularBorderSubtle
    val isFrosted = settings.displaySetting.liquidGlassMode == LiquidGlassMode.SOFT_FROSTED
    @Composable
    fun glassCard(shape: Shape): Modifier = if (isFrosted) {
        Modifier.frostedGlassSurface(
            shape = shape,
            borderWidth = 0.5.dp,
            specularBrush = borderBrush,
            fillColor = Color.Transparent,
        )
    } else {
        Modifier.liquidGlassSurface(
            shape = shape,
            borderWidth = 0.5.dp,
            specularBrush = borderBrush,
            fillColor = Color.Transparent,
            glassMode = LiquidGlassMode.BALANCED_BLEND,
        )
    }

    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = {
            commitChanges()
            onDismissRequest()
        },
        shape = sheetShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Unified header: title left, borderless close right, animated dismiss via sheet.
            DomChatSheetHeader(
                title = if (isPerChatMode) "Chat Context Allocation" else "Global Context Budget",
                onCloseClick = { onDismissRequest() },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Per-chat inheritance toggle
                if (isPerChatMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(glassCard(RoundedCornerShape(16.dp)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f).padding(end = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Inherit Global Context Settings",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Text(
                                    text = if (inheritGlobal) "Following app global context sliders" else "Custom context allocation for this chat",
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                            DomChatGlassSwitch(
                                checked = inheritGlobal,
                                onCheckedChange = { inheritGlobal = it }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                AnimatedVisibility(
                    visible = !inheritGlobal || !isPerChatMode,
                    enter = fadeIn(animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)) +
                        expandVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)),
                    exit = fadeOut(animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)) +
                        shrinkVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)),
                ) {
                    Column {
                        // Visual Segmented Allocation Bar
                        Text(
                            text = "Live Budget Allocation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFAAAAAA)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(18.dp)
                                .then(glassCard(RoundedCornerShape(9.dp)))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .weight(systemPercent.toFloat())
                                        .fillMaxHeight()
                                        .background(Color(0xFF3B82F6))
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
                                        .background(Color(0xFF8B5CF6))
                                )
                            }
                        }

                        // Legend
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF3B82F6)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("System: $systemPercent%", fontSize = 11.sp, color = Color(0xFFCCCCCC))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("History: $historyPercent%", fontSize = 11.sp, color = Color(0xFFCCCCCC))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF8B5CF6)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Memory: $memoryPercent%", fontSize = 11.sp, color = Color(0xFFCCCCCC))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Slider 1: Total Model Context Window Size
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(glassCard(RoundedCornerShape(16.dp)))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target Context Window Size",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${formatTokens(maxContextTokens)} ($maxContextTokens tokens)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF60A5FA)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val contextSteps = listOf(8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576)
                                val currentStepIndex = contextSteps.indexOfFirst { it >= maxContextTokens }.takeIf { it != -1 } ?: 4
                                DomChatSlider(
                                    value = currentStepIndex.toFloat(),
                                    onValueChange = { index: Float ->
                                        maxContextTokens = contextSteps[index.toInt().coerceIn(0, contextSteps.lastIndex)]
                                    },
                                    valueRange = 0f..7f,
                                    steps = 6,
                                )
                                Text(
                                    text = "Supported for 128k+ / 1M+ context models. Context Meter gauges against this limit.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Slider 2: Response Output Reservation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(glassCard(RoundedCornerShape(16.dp)))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Max Response Output Tokens",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "$maxResponseTokens tokens",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                                DomChatSlider(
                                    value = maxResponseTokens.toFloat(),
                                    onValueChange = { newVal: Float -> maxResponseTokens = newVal.toInt() },
                                    valueRange = 1024f..16384f,
                                    steps = 14,
                                )
                                Text(
                                    text = "Tokens safely reserved for AI message generation to prevent mid-sentence cutoff.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Slider 3: System Prompt & Character Persona %
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(glassCard(RoundedCornerShape(16.dp)))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Character & System Prompt Budget",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "$systemPercent% (~${formatTokens(systemTokens)})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3B82F6)
                                    )
                                }
                                DomChatSlider(
                                    value = systemPercent.toFloat(),
                                    onValueChange = { newVal: Float ->
                                        systemPercent = newVal.toInt()
                                        // Auto adjust history so sum is ~100
                                        historyPercent = (100 - systemPercent - memoryPercent).coerceIn(20, 85)
                                    },
                                    valueRange = 10f..45f,
                                )
                                Text(
                                    text = "Allocated to character card definition, user persona, scenario, and prompt presets.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Slider 4: Chat History Budget %
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(glassCard(RoundedCornerShape(16.dp)))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Chat Message History Budget",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "$historyPercent% (~${formatTokens(historyTokens)})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                DomChatSlider(
                                    value = historyPercent.toFloat(),
                                    onValueChange = { newVal: Float ->
                                        historyPercent = newVal.toInt()
                                        memoryPercent = (100 - systemPercent - historyPercent).coerceIn(5, 40)
                                    },
                                    valueRange = 30f..85f,
                                )
                                Text(
                                    text = "Maximum historical conversational turns packed into prompt before summarization.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Slider 5: Lorebook & Memory Budget %
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(glassCard(RoundedCornerShape(16.dp)))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Lorebook & Memory (RAG) Budget",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "$memoryPercent% (~${formatTokens(memoryTokens)})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B5CF6)
                                    )
                                }
                                DomChatSlider(
                                    value = memoryPercent.toFloat(),
                                    onValueChange = { newVal: Float ->
                                        memoryPercent = newVal.toInt()
                                        historyPercent = (100 - systemPercent - memoryPercent).coerceIn(20, 85)
                                    },
                                    valueRange = 5f..35f,
                                )
                                Text(
                                    text = "Reserved for dynamic world info entries triggered by keywords and associative memories.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auto-prune toggle
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(glassCard(RoundedCornerShape(16.dp)))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Smart Overflow Auto-Pruning",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "Gracefully trims oldest non-essential turns to fit inside target context",
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp,
                                        color = Color(0xFF888888)
                                    )
                                }
                                DomChatGlassSwitch(
                                    checked = autoPrune,
                                    onCheckedChange = { autoPrune = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Context Compaction card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(glassCard(RoundedCornerShape(16.dp)))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f).padding(end = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "Context Compaction",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color.White else Color.Black
                                        )
                                        Text(
                                            text = "Condenses older turns into a rolling summary when approaching the threshold",
                                            fontSize = 11.5.sp,
                                            lineHeight = 15.sp,
                                            color = Color(0xFF888888)
                                        )
                                    }
                                    DomChatGlassSwitch(
                                        checked = enableCompaction,
                                        onCheckedChange = { enableCompaction = it }
                                    )
                                }

                                AnimatedVisibility(
                                    visible = enableCompaction,
                                    enter = fadeIn(animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)) +
                                        expandVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)),
                                    exit = fadeOut(animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)) +
                                        shrinkVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f)),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Compaction Threshold",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isDark) Color.White else Color.Black
                                            )
                                            Text(
                                                text = "$compactionThreshold%",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        DomChatSlider(
                                            value = compactionThreshold.toFloat(),
                                            onValueChange = { newVal: Float ->
                                                compactionThreshold = newVal.toInt().coerceIn(50, 95)
                                            },
                                            valueRange = 50f..95f,
                                            steps = 44,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reset Button
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            DomChatGlassPillButton(
                                text = "Reset to Recommended 128k Defaults",
                                icon = Icons.Rounded.Refresh,
                                onClick = {
                                    maxContextTokens = 131072
                                    maxResponseTokens = 4096
                                    systemPercent = 25
                                    historyPercent = 60
                                    memoryPercent = 15
                                    autoPrune = true
                                    enableCompaction = false
                                    compactionThreshold = 75
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
