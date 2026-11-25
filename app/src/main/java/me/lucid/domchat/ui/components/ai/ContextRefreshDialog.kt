package me.lucid.domchat.ui.components.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import me.lucid.domchat.R
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.service.ChatService
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.liquidGlassSurface

enum class RefreshDialogState {
    CONFIRM,
    LOADING,
    SUCCESS,
    ERROR
}

@Composable
fun ContextRefreshDialog(
    conversation: Conversation,
    onRefresh: suspend () -> ChatService.ContextRefreshResult,
    onEditSummary: () -> Unit = {},
    onRevertSummary: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberPremiumHaptics()
    var state by remember { mutableStateOf(RefreshDialogState.CONFIRM) }
    var errorResId by remember { mutableStateOf<Int?>(null) }
    var errorArgs by remember { mutableStateOf<List<Any>>(emptyList()) }
    var summarizedCount by remember { mutableIntStateOf(0) }
    var tokensSaved by remember { mutableIntStateOf(0) }

    val messageCount = conversation.currentMessages.size
    val lastSummaryIndex = conversation.contextSummaryUpToIndex
    val hasPreviousSummary = !conversation.contextSummary.isNullOrBlank()

    // Keep last 2 messages (user + assistant exchange)
    val messagesToKeep = 2
    val lastIndexToSummarize = (messageCount - messagesToKeep - 1).coerceAtLeast(0)
    val startIndex = if (hasPreviousSummary && lastSummaryIndex >= 0 && lastSummaryIndex < messageCount) {
        (lastSummaryIndex + 1).coerceAtMost(messageCount)
    } else {
        0
    }
    val messagesToSummarize = if (startIndex <= lastIndexToSummarize) {
        lastIndexToSummarize - startIndex + 1
    } else {
        0
    }

    val messagesToProcess = if (startIndex <= lastIndexToSummarize && lastIndexToSummarize < conversation.currentMessages.size) {
        conversation.currentMessages.subList(startIndex, (lastIndexToSummarize + 1).coerceAtMost(conversation.currentMessages.size))
    } else {
        emptyList()
    }
    val tokensToCleanUp = messagesToProcess.sumOf { msg ->
        msg.parts.sumOf { part ->
            when (part) {
                is me.lucid.ai.ui.UIMessagePart.Text -> part.text.length / 4
                else -> 50
            }
        }
    }

    val displayedErrorMessage = errorResId?.let { resId ->
        context.getString(resId, *errorArgs.toTypedArray())
    } ?: stringResource(R.string.context_refresh_no_summarizer)

    ApplyDialogWindowBlur()
    val dialogShape = RoundedCornerShape(26.dp)

    Dialog(
        onDismissRequest = { if (state != RefreshDialogState.LOADING) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = state != RefreshDialogState.LOADING,
            dismissOnClickOutside = state != RefreshDialogState.LOADING,
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .liquidGlassSurface(
                    shape = dialogShape,
                    fillColor = Color(0xFF000000).copy(alpha = 0.75f),
                    borderWidth = 0.5.dp,
                    specularBrush = LiquidGlassTokens.SpecularBorderBrush,
                )
                .clip(dialogShape)
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .liquidGlassSurface(
                                shape = CircleShape,
                                fillColor = Color.White.copy(alpha = 0.08f),
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (state) {
                                RefreshDialogState.CONFIRM -> stringResource(R.string.context_refresh_confirm_title)
                                RefreshDialogState.LOADING -> stringResource(R.string.context_refresh_loading)
                                RefreshDialogState.SUCCESS -> stringResource(R.string.context_refresh_complete)
                                RefreshDialogState.ERROR -> stringResource(R.string.context_refresh_error_title)
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = when (state) {
                                RefreshDialogState.SUCCESS -> Color(0xFF34C759)
                                RefreshDialogState.ERROR -> MaterialTheme.colorScheme.error
                                else -> Color.White
                            }
                        )
                        Text(
                            text = "Compress older messages into memory",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    }
                }

                // Content Animated Section
                AnimatedContent(
                    targetState = state,
                    label = "summarizer_content_anim",
                    transitionSpec = {
                        (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                            scaleIn(initialScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessLow)))
                            .togetherWith(
                                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                                scaleOut(targetScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                            )
                    }
                ) { currentState ->
                    when (currentState) {
                        RefreshDialogState.CONFIRM -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.context_refresh_confirm_message),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.80f),
                                    lineHeight = 20.sp,
                                )

                                // Clean Impact Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassSurface(
                                            shape = RoundedCornerShape(16.dp),
                                            fillColor = Color.White.copy(alpha = 0.05f),
                                            borderWidth = 0.5.dp,
                                            specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                        )
                                        .clip(RoundedCornerShape(16.dp))
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Messages to Summarize",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White.copy(alpha = 0.70f)
                                            )
                                            Text(
                                                text = "$messagesToSummarize",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Estimated Token Savings",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White.copy(alpha = 0.70f)
                                            )
                                            Text(
                                                text = "~$tokensToCleanUp tokens",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF34C759)
                                            )
                                        }

                                        if (hasPreviousSummary) {
                                            HorizontalDivider(
                                                color = Color.White.copy(alpha = 0.08f),
                                                thickness = 0.5.dp
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.context_refresh_previous_summary),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = Color.White.copy(alpha = 0.90f)
                                                )

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    IconButton(
                                                        onClick = {
                                                            haptics.perform(HapticPattern.Pop)
                                                            onEditSummary()
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Edit,
                                                            contentDescription = stringResource(R.string.context_refresh_edit_summary),
                                                            tint = Color.White.copy(alpha = 0.75f),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            haptics.perform(HapticPattern.Thud)
                                                            onRevertSummary()
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Rounded.Undo,
                                                            contentDescription = stringResource(R.string.context_refresh_revert_summary),
                                                            tint = Color.White.copy(alpha = 0.75f),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = conversation.contextSummary?.take(180) + if ((conversation.contextSummary?.length ?: 0) > 180) "..." else "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.60f),
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        RefreshDialogState.LOADING -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = Color.White,
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    text = "Analyzing and condensing conversation...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }

                        RefreshDialogState.SUCCESS -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34C759),
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = stringResource(R.string.context_refresh_cleaned, summarizedCount),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                                if (tokensSaved > 0) {
                                    Text(
                                        text = stringResource(R.string.context_refresh_tokens_saved, tokensSaved),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF34C759),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        RefreshDialogState.ERROR -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = displayedErrorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // Action Buttons Row
                if (state == RefreshDialogState.CONFIRM) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF000000),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                        ) {
                            Text(
                                text = stringResource(android.R.string.cancel),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                state = RefreshDialogState.LOADING
                                scope.launch {
                                    val result = onRefresh()
                                    if (result.success) {
                                        summarizedCount = result.messagesSummarized
                                        tokensSaved = result.tokensSaved
                                        state = RefreshDialogState.SUCCESS
                                    } else {
                                        errorResId = result.errorResId
                                        errorArgs = result.errorArgs
                                        state = RefreshDialogState.ERROR
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.context_refresh_button),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (state == RefreshDialogState.SUCCESS || state == RefreshDialogState.ERROR) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = stringResource(android.R.string.ok),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
