package me.lucid.domchat.ui.pages.assistant.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import me.lucid.domchat.ui.components.ui.DomChatSlider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.components.ui.DebouncedTextField
import me.lucid.domchat.ui.motion.ExpandableContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.lucid.ai.core.ReasoningLevel
import me.lucid.ai.provider.ModelType
import me.lucid.ai.provider.ProviderSetting
import me.lucid.domchat.R
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.ui.components.ai.ModelSelector
import me.lucid.domchat.ui.components.ai.ReasoningButton
import me.lucid.domchat.ui.components.ai.VoiceSelector
import me.lucid.domchat.ui.components.ui.AutoSaveIndicator
import me.lucid.domchat.ui.components.ui.Tag
import me.lucid.domchat.ui.components.ui.TagType
import me.lucid.domchat.ui.pages.setting.components.SettingsGroup
import me.lucid.domchat.ui.pages.setting.components.SettingGroupItem
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.data.ai.models.ModelCatalogSnapshot
import me.lucid.domchat.utils.toFixed
import me.lucid.tts.provider.TTSProviderSetting

/**
 * Model tab - All model and generation-related settings.
 * Designed with cohesive SettingsGroup pattern.
 */
@Composable
fun AssistantModelSubPage(
    assistant: Assistant,
    providers: List<ProviderSetting>,
    ttsProviders: List<TTSProviderSetting>,
    catalogSnapshot: ModelCatalogSnapshot? = null,
    onUpdate: (Assistant) -> Unit
) {
    val haptics = rememberPremiumHaptics()
    var maxTokensPending by remember { mutableStateOf(false) }
    val currentModel = remember(assistant.chatModelId, providers) {
        providers.flatMap { it.models }.firstOrNull { it.id == assistant.chatModelId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ═══════════════════════════════════════════════════════════════════
        // MODELS GROUP
        // ═══════════════════════════════════════════════════════════════════
        SettingsGroup(title = stringResource(R.string.assistant_model_group_models)) {
            // Chat Model (Primary)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.assistant_page_chat_model),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.assistant_page_chat_model_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ModelSelector(
                        modelId = assistant.chatModelId,
                        providers = providers,
                        type = ModelType.CHAT,
                        onSelect = { onUpdate(assistant.copy(chatModelId = it.id)) },
                    )
                }
            }

            // Voice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Voice",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Character voice for TTS. Leave empty to use the global default.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    VoiceSelector(
                        voiceId = assistant.ttsVoiceId,
                        providers = ttsProviders,
                        allowClear = true,
                        onClear = { onUpdate(assistant.copy(ttsVoiceId = null)) },
                        onSelect = { onUpdate(assistant.copy(ttsVoiceId = it.id)) },
                        catalogSnapshot = catalogSnapshot
                    )
                }
            }
            
            // Background Model
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.assistant_model_background_model),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.assistant_model_background_model_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ModelSelector(
                        modelId = assistant.backgroundModelId,
                        providers = providers,
                        type = ModelType.CHAT,
                        onSelect = { onUpdate(assistant.copy(backgroundModelId = it.id)) },
                    )
                }
            }
            
        }

        // ═══════════════════════════════════════════════════════════════════
        // GENERATION GROUP
        // ═══════════════════════════════════════════════════════════════════
        SettingsGroup(title = stringResource(R.string.assistant_model_group_generation)) {
            // Temperature
            val tempLabel = if (assistant.temperature != null) {
                val temp = assistant.temperature
                when (temp) {
                    in 0.0f..0.3f -> stringResource(R.string.assistant_model_temp_strict_value, temp)
                    in 0.3f..1.0f -> stringResource(R.string.assistant_model_temp_balanced_value, temp)
                    in 1.0f..1.5f -> stringResource(R.string.assistant_model_temp_creative_value, temp)
                    else -> stringResource(R.string.assistant_model_temp_chaotic_value, temp)
                }
            } else stringResource(R.string.common_default)
            
            SettingGroupItem(
                title = stringResource(R.string.assistant_page_temperature),
                subtitle = tempLabel,
                trailing = {
                    HapticSwitch(
                        checked = assistant.temperature != null,
                        onCheckedChange = { enabled ->
                            onUpdate(assistant.copy(temperature = if (enabled) 1.0f else null))
                        }
                    )
                }
            )
            
            // Temperature Slider
            ExpandableContent(visible = assistant.temperature != null) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DomChatSlider(
                            value = assistant.temperature ?: 1.0f,
                            onValueChange = { onUpdate(assistant.copy(temperature = it.toFixed(2).toFloatOrNull() ?: 1.0f)) },
                            valueRange = 0f..2f,
                            steps = 39,
                            stickyPoints = listOf(0.0f, 0.7f, 1.0f, 1.2f, 1.5f, 2.0f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentTemp = assistant.temperature ?: 1.0f
                            val tagType = when (currentTemp) {
                                in 0.0f..0.3f -> TagType.INFO
                                in 0.3f..1.0f -> TagType.SUCCESS
                                in 1.0f..1.5f -> TagType.WARNING
                                else -> TagType.ERROR
                            }
                            Tag(type = tagType) {
                                Text(when (currentTemp) {
                                    in 0.0f..0.3f -> stringResource(R.string.assistant_page_strict)
                                    in 0.3f..1.0f -> stringResource(R.string.assistant_page_balanced)
                                    in 1.0f..1.5f -> stringResource(R.string.assistant_page_creative)
                                    else -> stringResource(R.string.assistant_page_chaotic)
                                })
                            }
                        }
                    }
                }
            }

            // Top-P
            SettingGroupItem(
                title = stringResource(R.string.assistant_page_top_p),
                subtitle = if (assistant.topP != null) {
                    stringResource(R.string.common_enabled_value, assistant.topP.toString())
                } else {
                    stringResource(R.string.common_default)
                },
                trailing = {
                    HapticSwitch(
                        checked = assistant.topP != null,
                        onCheckedChange = { enabled ->
                            onUpdate(assistant.copy(topP = if (enabled) 0.9f else null))
                        }
                    )
                }
            )

            // Top-P Slider
            ExpandableContent(visible = assistant.topP != null) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DomChatSlider(
                            value = assistant.topP ?: 0.9f,
                            onValueChange = { onUpdate(assistant.copy(topP = it.toFixed(2).toFloatOrNull() ?: 0.9f)) },
                            valueRange = 0f..1f,
                            steps = 19,
                            stickyPoints = listOf(0.0f, 0.7f, 0.9f, 0.95f, 1.0f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Top-K
            SettingGroupItem(
                title = "Top-K",
                subtitle = if (assistant.topK != null) {
                    "${assistant.topK}"
                } else {
                    stringResource(R.string.common_default)
                },
                trailing = {
                    HapticSwitch(
                        checked = assistant.topK != null,
                        onCheckedChange = { enabled ->
                            onUpdate(assistant.copy(topK = if (enabled) 40 else null))
                        }
                    )
                }
            )

            // Top-K Slider
            ExpandableContent(visible = assistant.topK != null) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DomChatSlider(
                            value = (assistant.topK ?: 40).toFloat(),
                            onValueChange = {
                                val intVal = it.toInt().coerceAtLeast(1)
                                onUpdate(assistant.copy(topK = intVal))
                            },
                            valueRange = 1f..100f,
                            steps = 99,
                            stickyPoints = listOf(20f, 40f, 50f, 80f, 100f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Min-P Sampling
            SettingGroupItem(
                title = "Min-P Sampling",
                subtitle = if (assistant.minP != null) {
                    "${assistant.minP}"
                } else {
                    stringResource(R.string.common_default)
                },
                trailing = {
                    HapticSwitch(
                        checked = assistant.minP != null,
                        onCheckedChange = { enabled ->
                            onUpdate(assistant.copy(minP = if (enabled) 0.05f else null))
                        }
                    )
                }
            )

            // Min-P Slider
            ExpandableContent(visible = assistant.minP != null) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DomChatSlider(
                            value = assistant.minP ?: 0.05f,
                            onValueChange = { onUpdate(assistant.copy(minP = it.toFixed(2).toFloatOrNull() ?: 0.05f)) },
                            valueRange = 0f..1f,
                            steps = 39,
                            stickyPoints = listOf(0.0f, 0.05f, 0.10f, 0.20f, 1.0f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Repetition Penalty
            SettingGroupItem(
                title = "Repetition Penalty",
                subtitle = if (assistant.repetitionPenalty != null) {
                    "${assistant.repetitionPenalty}"
                } else {
                    stringResource(R.string.common_default)
                },
                trailing = {
                    HapticSwitch(
                        checked = assistant.repetitionPenalty != null,
                        onCheckedChange = { enabled ->
                            onUpdate(assistant.copy(repetitionPenalty = if (enabled) 1.1f else null))
                        }
                    )
                }
            )

            // Repetition Penalty Slider
            ExpandableContent(visible = assistant.repetitionPenalty != null) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DomChatSlider(
                            value = assistant.repetitionPenalty ?: 1.1f,
                            onValueChange = { onUpdate(assistant.copy(repetitionPenalty = it.toFixed(2).toFloatOrNull() ?: 1.1f)) },
                            valueRange = 1.0f..2.0f,
                            steps = 19,
                            stickyPoints = listOf(1.0f, 1.05f, 1.10f, 1.15f, 1.20f, 1.50f, 2.0f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Frequency Penalty
            SettingGroupItem(
                title = "Frequency Penalty",
                subtitle = if (assistant.frequencyPenalty != null) {
                    "${assistant.frequencyPenalty}"
                } else {
                    stringResource(R.string.common_default)
                },
                trailing = {
                    HapticSwitch(
                        checked = assistant.frequencyPenalty != null,
                        onCheckedChange = { enabled ->
                            onUpdate(assistant.copy(frequencyPenalty = if (enabled) 0.5f else null))
                        }
                    )
                }
            )

            // Frequency Penalty Slider
            ExpandableContent(visible = assistant.frequencyPenalty != null) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DomChatSlider(
                            value = assistant.frequencyPenalty ?: 0.5f,
                            onValueChange = { onUpdate(assistant.copy(frequencyPenalty = it.toFixed(2).toFloatOrNull() ?: 0.5f)) },
                            valueRange = -2.0f..2.0f,
                            steps = 39,
                            stickyPoints = listOf(-2.0f, -1.0f, 0.0f, 0.5f, 1.0f, 2.0f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Presence Penalty
            SettingGroupItem(
                title = "Presence Penalty",
                subtitle = if (assistant.presencePenalty != null) {
                    "${assistant.presencePenalty}"
                } else {
                    stringResource(R.string.common_default)
                },
                trailing = {
                    HapticSwitch(
                        checked = assistant.presencePenalty != null,
                        onCheckedChange = { enabled ->
                            onUpdate(assistant.copy(presencePenalty = if (enabled) 0.5f else null))
                        }
                    )
                }
            )

            // Presence Penalty Slider
            ExpandableContent(visible = assistant.presencePenalty != null) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DomChatSlider(
                            value = assistant.presencePenalty ?: 0.5f,
                            onValueChange = { onUpdate(assistant.copy(presencePenalty = it.toFixed(2).toFloatOrNull() ?: 0.5f)) },
                            valueRange = -2.0f..2.0f,
                            steps = 39,
                            stickyPoints = listOf(-2.0f, -1.0f, 0.0f, 0.5f, 1.0f, 2.0f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // OUTPUT GROUP
        // ═══════════════════════════════════════════════════════════════════
        SettingsGroup(title = stringResource(R.string.assistant_model_group_output)) {
            // Stream Output
            SettingGroupItem(
                title = stringResource(R.string.assistant_page_stream_output),
                subtitle = stringResource(R.string.assistant_page_stream_output_desc),
                trailing = {
                    HapticSwitch(
                        checked = assistant.streamOutput,
                        onCheckedChange = { onUpdate(assistant.copy(streamOutput = it)) }
                    )
                }
            )

            // Thinking Budget
            SettingGroupItem(
                title = stringResource(R.string.assistant_page_thinking_budget),
                subtitle = when (ReasoningLevel.fromBudgetTokens(assistant.thinkingBudget)) {
                    ReasoningLevel.OFF -> stringResource(R.string.reasoning_off)
                    ReasoningLevel.AUTO -> stringResource(R.string.reasoning_auto)
                    ReasoningLevel.LOW -> stringResource(R.string.reasoning_light)
                    ReasoningLevel.MEDIUM -> stringResource(R.string.reasoning_medium)
                    ReasoningLevel.HIGH -> stringResource(R.string.reasoning_heavy)
                    ReasoningLevel.MAX -> "Max"
                },
                trailing = {
                    ReasoningButton(
                        model = currentModel,
                        reasoningTokens = assistant.thinkingBudget ?: 0,
                        onUpdateReasoningTokens = { tokens ->
                            onUpdate(assistant.copy(thinkingBudget = tokens))
                        }
                    )
                }
            )

            // Max Tokens
            SettingGroupItem(
                title = stringResource(R.string.assistant_page_max_tokens),
                subtitle = if (assistant.maxTokens != null) 
                    stringResource(R.string.assistant_page_max_tokens_limit, assistant.maxTokens) 
                else 
                    stringResource(R.string.assistant_page_max_tokens_no_token_limit),
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AutoSaveIndicator(visible = maxTokensPending)
                        DebouncedTextField(
                            value = assistant.maxTokens?.toString() ?: "",
                            onValueChange = { text ->
                                val tokens = if (text.isBlank()) null else text.filter { it.isDigit() }.toIntOrNull()?.takeIf { it > 0 }
                                onUpdate(assistant.copy(maxTokens = tokens))
                            },
                            stateKey = "assistant_max_tokens_${assistant.id}",
                            modifier = Modifier.width(100.dp),
                            placeholder = stringResource(R.string.assistant_model_auto),
                            singleLine = true,
                            onPendingChange = { maxTokensPending = it },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            )
        }
    }
}
