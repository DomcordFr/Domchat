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
import me.lucid.domchat.ui.motion.EntranceDirection
import me.lucid.domchat.ui.motion.LocalMotionPolicy
import me.lucid.domchat.ui.motion.entrance
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.modifier.blurredContainerColor
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassBubbleSurface
import me.lucid.domchat.ui.theme.liquidInteractiveGlass
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.context.LocalSettings

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
    val motionPolicy = LocalMotionPolicy.current
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
                        modifier = Modifier
                            .weight(1f)
                            .entrance(
                                key = "newchat:tpl:0:Continue Story",
                                motionPolicy = motionPolicy,
                                from = EntranceDirection.UP,
                                delayMs = 0,
                            )
                    )
                    TemplateCard(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Describe Scene",
                        onClick = { onTemplateClick(describePrompt) },
                        modifier = Modifier
                            .weight(1f)
                            .entrance(
                                key = "newchat:tpl:1:Describe Scene",
                                motionPolicy = motionPolicy,
                                from = EntranceDirection.UP,
                                delayMs = 60,
                            )
                    )
                    TemplateCard(
                        icon = Icons.Rounded.PlayArrow,
                        title = "Take Action",
                        onClick = { onTemplateClick(actionPrompt) },
                        modifier = Modifier
                            .weight(1f)
                            .entrance(
                                key = "newchat:tpl:2:Take Action",
                                motionPolicy = motionPolicy,
                                from = EntranceDirection.UP,
                                delayMs = 120,
                            )
                    )
                    TemplateCard(
                        icon = Icons.Rounded.Lightbulb,
                        title = "Inner Thoughts",
                        onClick = { onTemplateClick(thoughtsPrompt) },
                        modifier = Modifier
                            .weight(1f)
                            .entrance(
                                key = "newchat:tpl:3:Inner Thoughts",
                                motionPolicy = motionPolicy,
                                from = EntranceDirection.UP,
                                delayMs = 180,
                            )
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
                            onClick = { onTemplateClick(continuePrompt) },
                            modifier = Modifier.entrance(
                                key = "newchat:pill:0:Continue Story",
                                motionPolicy = motionPolicy,
                                from = EntranceDirection.UP,
                                delayMs = 0,
                            )
                        )
                        ActionPill(
                            icon = Icons.Rounded.AutoAwesome,
                            text = "Describe Scene",
                            iconColor = brainstormColor,
                            hasBackgroundImage = hasBackgroundImage,
                            onClick = { onTemplateClick(describePrompt) },
                            modifier = Modifier.entrance(
                                key = "newchat:pill:1:Describe Scene",
                                motionPolicy = motionPolicy,
                                from = EntranceDirection.UP,
                                delayMs = 60,
                            )
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
                            onClick = { onTemplateClick(actionPrompt) },
                            modifier = Modifier.entrance(
                                key = "newchat:pill:2:Take Action",
                                motionPolicy = motionPolicy,
                                from = EntranceDirection.UP,
                                delayMs = 120,
                            )
                        )
                        ActionPill(
                            icon = Icons.Rounded.Lightbulb,
                            text = "Inner Thoughts",
                            iconColor = writeColor,
                            hasBackgroundImage = hasBackgroundImage,
                            onClick = { onTemplateClick(thoughtsPrompt) },
                            modifier = Modifier.entrance(
                                key = "newchat:pill:3:Inner Thoughts",
                                motionPolicy = motionPolicy,
                                from = EntranceDirection.UP,
                                delayMs = 180,
                            )
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
    val isDark = LocalDarkMode.current
    val cardShape = AppShapes.CardMedium
    val settings = LocalSettings.current
    val borderBrush = LiquidGlassTokens.SpecularBorderSubtle
    val cardFill = Color(0xFF000000)

    Card(
        onClick = onClick,
        modifier = modifier
            .wrapContentHeight()
            .liquidGlassSurface(
                shape = cardShape,
                borderWidth = 0.5.dp,
                fillColor = cardFill,
                specularBrush = borderBrush,
                glassMode = settings.displaySetting.liquidGlassMode,
            )
            .border(0.5.dp, borderBrush, cardShape),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardFill, contentColor = Color.White),
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
                tint = Color.White
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color.White,
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
    val isDark = LocalDarkMode.current
    val settings = LocalSettings.current
    val borderBrush = LiquidGlassTokens.SpecularBorderSubtle
    val pillFill = Color(0xFF000000)

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Surface(
            onClick = onClick,
            modifier = modifier
                .liquidGlassSurface(
                    shape = pillShape,
                    borderWidth = 0.5.dp,
                    fillColor = pillFill,
                    specularBrush = borderBrush,
                    glassMode = settings.displaySetting.liquidGlassMode,
                )
                .border(0.5.dp, borderBrush, pillShape),
            shape = pillShape,
            color = pillFill,
            contentColor = Color.White
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
                    tint = if (isDark) Color.White.copy(alpha = 0.95f) else Color.Black.copy(alpha = 0.85f)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = if (isDark) Color.White.copy(alpha = 0.95f) else Color.Black.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

