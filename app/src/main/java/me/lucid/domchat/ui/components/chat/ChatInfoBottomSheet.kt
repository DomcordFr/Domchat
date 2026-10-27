package me.lucid.domchat.ui.components.chat

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lucid.ai.core.MessageRole
import me.lucid.ai.provider.Model
import me.lucid.ai.ui.UIMessagePart
import me.lucid.ai.ui.currentVersionMessages
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import androidx.compose.foundation.border
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInfoBottomSheet(
    settings: Settings,
    conversation: Conversation,
    assistant: Assistant,
    model: Model?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val amoledMode by rememberAmoledDarkMode()
    val activePersona = settings.displaySetting.getActivePersona()

    // Calculate actual tokens and message metrics
    val currentMessages = remember(conversation.messageNodes) {
        conversation.messageNodes.currentVersionMessages()
    }

    val totalUserChars = remember(currentMessages) {
        currentMessages
            .filter { it.role == MessageRole.USER }
            .flatMap { it.parts }
            .filterIsInstance<UIMessagePart.Text>()
            .sumOf { it.text.length }
    }

    val totalAssistantChars = remember(currentMessages) {
        currentMessages
            .filter { it.role == MessageRole.ASSISTANT }
            .flatMap { it.parts }
            .filterIsInstance<UIMessagePart.Text>()
            .sumOf { it.text.length }
    }

    val systemPromptChars = remember(assistant.systemPrompt, activePersona.description) {
        assistant.systemPrompt.length + activePersona.description.length
    }

    // Realistic token estimation (1 token ~= 3.75 chars in English/markdown)
    val estimatedInputTokens = remember(totalUserChars, systemPromptChars) {
        ((totalUserChars + systemPromptChars) / 3.75).toInt().coerceAtLeast(12)
    }

    val estimatedOutputTokens = remember(totalAssistantChars) {
        (totalAssistantChars / 3.75).toInt().coerceAtLeast(0)
    }

    val totalConversationTokens = estimatedInputTokens + estimatedOutputTokens

    // Context capacity - defaults to 128k as requested by user if model has no explicit limit
    val contextCapacity = remember(model) {
        model?.contextWindowTokens ?: 128_000
    }

    val contextUsageRatio = (totalConversationTokens.toFloat() / contextCapacity.toFloat()).coerceIn(0f, 1f)
    val animatedRatio by animateFloatAsState(
        targetValue = contextUsageRatio,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "context_usage_ratio"
    )

    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val cardBg = domChatCardContainerColor()

    AetherInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            // Header: Character + Model overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UIAvatar(
                    name = assistant.name,
                    value = assistant.avatar,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assistant.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = model?.displayName ?: model?.modelId ?: "128k+ Long-Context Model",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Liquid Glass Context Window Card
            Surface(
                color = cardBg,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.DataUsage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Context Capacity",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${(contextUsageRatio * 100).toInt()}% Used",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (contextUsageRatio > 0.85f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { animatedRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (contextUsageRatio > 0.85f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${numberFormatter.format(totalConversationTokens)} tokens",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${numberFormatter.format(contextCapacity)} max",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Detailed Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Input Tokens",
                    value = numberFormatter.format(estimatedInputTokens),
                    subtitle = "Prompts & History",
                    modifier = Modifier.weight(1f),
                    isAmoled = amoledMode
                )
                MetricCard(
                    title = "Output Tokens",
                    value = numberFormatter.format(estimatedOutputTokens),
                    subtitle = "AI Generations",
                    modifier = Modifier.weight(1f),
                    isAmoled = amoledMode
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Messages",
                    value = "${currentMessages.size}",
                    subtitle = "${conversation.messageNodes.size} total nodes",
                    modifier = Modifier.weight(1f),
                    isAmoled = amoledMode
                )
                MetricCard(
                    title = "Active Persona",
                    value = activePersona.name,
                    subtitle = if (activePersona.description.isNotBlank()) "Lore active" else "Default",
                    modifier = Modifier.weight(1f),
                    isAmoled = amoledMode
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isAmoled: Boolean = false
) {
    val cardBg = domChatCardContainerColor()
    Surface(
        color = cardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                color = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}
