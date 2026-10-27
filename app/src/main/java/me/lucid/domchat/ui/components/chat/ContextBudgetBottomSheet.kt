package me.lucid.domchat.ui.components.chat

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.model.ContextBudgetConfig
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.modifier.domChatBlurEffect
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

    fun commitChanges() {
        val newConfig = ContextBudgetConfig(
            maxContextTokens = maxContextTokens,
            maxResponseTokens = maxResponseTokens,
            characterSystemBudgetPercent = systemPercent,
            chatHistoryBudgetPercent = historyPercent,
            memoryLorebookBudgetPercent = memoryPercent,
            autoPruneOverflow = autoPrune
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
    val cardBg = domChatCardContainerColor()

    AetherInWindowBottomSheet(
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
                .fillMaxHeight(0.82f)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPerChatMode) "Chat Context Allocation" else "Global Context Budget",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            text = "Token window & capacity division sliders",
                            fontSize = 11.5.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        commitChanges()
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Inherit Global Context Settings",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Text(
                                    text = if (inheritGlobal) "Following app global context sliders" else "Custom context allocation for this chat",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                            Switch(
                                checked = inheritGlobal,
                                onCheckedChange = { inheritGlobal = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3B82F6)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                AnimatedVisibility(visible = !inheritGlobal || !isPerChatMode) {
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
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color(0xFF222228))
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
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
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
                                Slider(
                                    value = currentStepIndex.toFloat(),
                                    onValueChange = { index: Float ->
                                        maxContextTokens = contextSteps[index.toInt().coerceIn(0, contextSteps.lastIndex)]
                                    },
                                    valueRange = 0f..7f,
                                    steps = 6,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color(0xFF3B82F6)
                                    )
                                )
                                Text(
                                    text = "Supported for 128k+ / 1M+ context models. Context Meter gauges against this limit.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Slider 2: Response Output Reservation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
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
                                Slider(
                                    value = maxResponseTokens.toFloat(),
                                    onValueChange = { newVal: Float -> maxResponseTokens = newVal.toInt() },
                                    valueRange = 1024f..16384f,
                                    steps = 14,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color(0xFFF59E0B)
                                    )
                                )
                                Text(
                                    text = "Tokens safely reserved for AI message generation to prevent mid-sentence cutoff.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Slider 3: System Prompt & Character Persona %
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
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
                                Slider(
                                    value = systemPercent.toFloat(),
                                    onValueChange = { newVal: Float ->
                                        systemPercent = newVal.toInt()
                                        // Auto adjust history so sum is ~100
                                        historyPercent = (100 - systemPercent - memoryPercent).coerceIn(20, 85)
                                    },
                                    valueRange = 10f..45f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color(0xFF3B82F6)
                                    )
                                )
                                Text(
                                    text = "Allocated to character card definition, user persona, scenario, and prompt presets.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Slider 4: Chat History Budget %
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
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
                                Slider(
                                    value = historyPercent.toFloat(),
                                    onValueChange = { newVal: Float ->
                                        historyPercent = newVal.toInt()
                                        memoryPercent = (100 - systemPercent - historyPercent).coerceIn(5, 40)
                                    },
                                    valueRange = 30f..85f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color(0xFF10B981)
                                    )
                                )
                                Text(
                                    text = "Maximum historical conversational turns packed into prompt before summarization.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Slider 5: Lorebook & Memory Budget %
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
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
                                Slider(
                                    value = memoryPercent.toFloat(),
                                    onValueChange = { newVal: Float ->
                                        memoryPercent = newVal.toInt()
                                        historyPercent = (100 - systemPercent - memoryPercent).coerceIn(20, 85)
                                    },
                                    valueRange = 5f..35f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color(0xFF8B5CF6)
                                    )
                                )
                                Text(
                                    text = "Reserved for dynamic world info entries triggered by keywords and associative memories.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auto-prune toggle
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Smart Overflow Auto-Pruning",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "Gracefully trims oldest non-essential turns to fit inside target context",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF888888)
                                    )
                                }
                                Switch(
                                    checked = autoPrune,
                                    onCheckedChange = { autoPrune = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF10B981)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reset Button
                        Button(
                            onClick = {
                                maxContextTokens = 131072
                                maxResponseTokens = 4096
                                systemPercent = 25
                                historyPercent = 60
                                memoryPercent = 15
                                autoPrune = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF1A1A22) else Color(0xFFE5E5EA),
                                contentColor = if (isDark) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset to Recommended 128k Defaults", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
