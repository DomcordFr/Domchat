package me.lucid.domchat.ui.components.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.lucid.domchat.R
import me.lucid.domchat.data.datastore.NewChatContentStyle
import me.lucid.domchat.data.datastore.NewChatHeaderStyle
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.ui.components.ui.Greeting
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.modifier.blurredContainerColor
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.theme.AppShapes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewChatContent(
    assistant: Assistant,
    headerStyle: NewChatHeaderStyle,
    contentStyle: NewChatContentStyle,
    showAvatarInHeader: Boolean = true,
    hasBackgroundImage: Boolean = false,
    onTemplateClick: (String) -> Unit,
    onNavigateToImageGen: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val continuePrompt = "*takes a breath and continues from the moment we paused...* "
    val describePrompt = "*looks around and describes the surrounding atmosphere and sensory details...* "
    val actionPrompt = "*steps forward with resolve and takes a decisive action...* "
    val thoughtsPrompt = "*reflects quietly on what just unfolded, weighing inner feelings...* "

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (headerStyle) {
            NewChatHeaderStyle.BIG_ICON -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = if (onAvatarClick != null && showAvatarInHeader) Modifier.clickable { onAvatarClick() } else Modifier
                ) {
                    if (showAvatarInHeader) {
                        UIAvatar(
                            name = assistant.name.ifBlank { "Character" },
                            value = assistant.avatar,
                            modifier = Modifier.size(80.dp),
                            onClick = onAvatarClick
                        )
                    }
                    Text(
                        text = assistant.name.ifBlank { "Character" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            NewChatHeaderStyle.GREETING -> {
                if (showAvatarInHeader) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = if (onAvatarClick != null) Modifier.clickable { onAvatarClick() } else Modifier
                    ) {
                        UIAvatar(
                            name = assistant.name.ifBlank { "Character" },
                            value = assistant.avatar,
                            modifier = Modifier.size(44.dp),
                            onClick = onAvatarClick
                        )
                        Greeting(
                            style = MaterialTheme.typography.titleLarge,
                            assistant = assistant
                        )
                    }
                } else {
                    Greeting(
                        style = MaterialTheme.typography.titleLarge,
                        assistant = assistant
                    )
                }
            }

            NewChatHeaderStyle.NONE -> Unit
        }

        when (contentStyle) {
            NewChatContentStyle.TEMPLATES -> {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 2
                ) {
                    TemplateCard(
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        title = "Continue Story",
                        onClick = { onTemplateClick(continuePrompt) },
                        modifier = Modifier.weight(1f)
                    )
                    TemplateCard(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Describe Scene",
                        onClick = { onTemplateClick(describePrompt) },
                        modifier = Modifier.weight(1f)
                    )
                    TemplateCard(
                        icon = Icons.Rounded.PlayArrow,
                        title = "Take Action",
                        onClick = { onTemplateClick(actionPrompt) },
                        modifier = Modifier.weight(1f)
                    )
                    TemplateCard(
                        icon = Icons.Rounded.Lightbulb,
                        title = "Inner Thoughts",
                        onClick = { onTemplateClick(thoughtsPrompt) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            NewChatContentStyle.ACTIONS -> {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val writeColor = MaterialTheme.colorScheme.inversePrimary
                val brainstormColor = MaterialTheme.colorScheme.tertiary

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionPill(
                            icon = Icons.AutoMirrored.Rounded.MenuBook,
                            text = "Continue Story",
                            iconColor = primaryColor,
                            hasBackgroundImage = hasBackgroundImage,
                            onClick = { onTemplateClick(continuePrompt) }
                        )
                        ActionPill(
                            icon = Icons.Rounded.AutoAwesome,
                            text = "Describe Scene",
                            iconColor = brainstormColor,
                            hasBackgroundImage = hasBackgroundImage,
                            onClick = { onTemplateClick(describePrompt) }
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionPill(
                            icon = Icons.Rounded.PlayArrow,
                            text = "Take Action",
                            iconColor = secondaryColor,
                            hasBackgroundImage = hasBackgroundImage,
                            onClick = { onTemplateClick(actionPrompt) }
                        )
                        ActionPill(
                            icon = Icons.Rounded.Lightbulb,
                            text = "Inner Thoughts",
                            iconColor = writeColor,
                            hasBackgroundImage = hasBackgroundImage,
                            onClick = { onTemplateClick(thoughtsPrompt) }
                        )
                    }
                }
            }

            NewChatContentStyle.NONE -> Unit
        }
    }
}

@Composable
private fun TemplateCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = MaterialTheme.colorScheme.surfaceContainer

    Card(
        onClick = onClick,
        modifier = modifier
            .wrapContentHeight()
            .domChatBlurEffect(cardColor, AppShapes.CardMedium),
        shape = AppShapes.CardMedium,
        colors = CardDefaults.cardColors(containerColor = blurredContainerColor(cardColor)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActionPill(
    icon: ImageVector,
    text: String,
    iconColor: Color,
    onClick: () -> Unit,
    hasBackgroundImage: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(24.dp)
    val cardColor = domChatCardContainerColor()
    val isDark = LocalDarkMode.current
    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.25f else 0.15f),
            Color.White.copy(alpha = if (isDark) 0.08f else 0.04f)
        )
    )

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            onClick = onClick,
            modifier = modifier
                .domChatBlurEffect(cardColor, pillShape)
                .border(0.5.dp, borderBrush, pillShape),
            shape = pillShape,
            color = cardColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = iconColor
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
