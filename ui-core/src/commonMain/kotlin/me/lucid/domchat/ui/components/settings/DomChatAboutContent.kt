package me.lucid.domchat.ui.components.settings

import androidx.compose.foundation.BorderStroke

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.lucid.domchat.ui.core.generated.resources.Res
import me.lucid.domchat.ui.core.generated.resources.ic_launcher_domchat_foreground
import org.jetbrains.compose.resources.painterResource

/** Android's production About-page body, shared without platform runtime dependencies. */
@Composable
fun DomChatAboutContent(
    appName: String,
    versionName: String,
    darkTheme: Boolean,
    platformTitle: String,
    platformSubtitle: String,
    platformIcon: ImageVector,
    deviceSubtitle: String,
    deviceIcon: ImageVector,
    architectureSubtitle: String,
    architectureIcon: ImageVector,
    onSourceCode: () -> Unit,
    onVersionLongPress: (() -> Unit)? = null,
    onHaptic: () -> Unit = {},
    extraProjectContent: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        Image(
            painter = painterResource(Res.drawable.ic_launcher_domchat_foreground),
            contentDescription = null,
            modifier = Modifier.size(240.dp),
        )
        Text(
            text = appName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            color = Color(0xFF000000),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                0.5.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent,
                    )
                )
            ),
            modifier = Modifier
                .padding(vertical = 4.dp)
                .pointerInput(onVersionLongPress) {
                    detectTapGestures(
                        onLongPress = {
                            if (onVersionLongPress != null) {
                                onHaptic()
                                onVersionLongPress()
                            }
                        },
                    )
                },
        ) {
            Text(
                text = if (versionName.startsWith("v")) versionName else "v$versionName",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
            )
        }
        Spacer(Modifier.height(32.dp))
        AboutSectionTitle("Project")
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DomChatAboutItem(
                icon = Icons.Rounded.Code,
                title = "Source Code",
                subtitle = "DomcordFr/Domchat",
                trailing = Icons.AutoMirrored.Rounded.OpenInNew,
                darkTheme = darkTheme,
                onHaptic = onHaptic,
                onClick = onSourceCode,
            )
            extraProjectContent?.invoke()
        }
        Spacer(Modifier.height(24.dp))
        AboutSectionTitle("System Information")
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DomChatAboutItem(
                icon = platformIcon,
                title = platformTitle,
                subtitle = platformSubtitle,
                darkTheme = darkTheme,
            )
            DomChatAboutItem(
                icon = deviceIcon,
                title = "Device",
                subtitle = deviceSubtitle,
                darkTheme = darkTheme,
            )
            DomChatAboutItem(
                icon = architectureIcon,
                title = "Architecture",
                subtitle = architectureSubtitle,
                darkTheme = darkTheme,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AboutSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    )
}

@Composable
fun DomChatAboutItem(
    icon: ImageVector,
    title: String,
    darkTheme: Boolean = true,
    subtitle: String? = null,
    subtitleMaxLines: Int = Int.MAX_VALUE,
    subtitleContent: (@Composable () -> Unit)? = null,
    trailing: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onHaptic: () -> Unit = {},
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "about_item_scale",
    )
    val alignTop = subtitleContent != null || subtitleMaxLines > 1
    Surface(
        onClick = {
            if (onClick != null) {
                onHaptic()
                onClick()
            }
        },
        enabled = onClick != null,
        color = Color(0xFF000000),
        contentColor = Color(0xFFF2F2F7),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.05f),
                    Color.Transparent
                )
            )
        ),
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = if (alignTop) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .then(if (alignTop) Modifier.padding(top = 2.dp) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White)
            }
            Column(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF2F2F7),
                )
                if (subtitleContent != null) {
                    subtitleContent()
                } else if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9A9AA2),
                        maxLines = subtitleMaxLines,
                        overflow = if (subtitleMaxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailingContent != null) {
                Box(
                    modifier = if (alignTop) Modifier.padding(top = 2.dp) else Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    trailingContent()
                }
            } else if (trailing != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .then(if (alignTop) Modifier.padding(top = 2.dp) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        trailing,
                        null,
                        tint = Color(0xFF9A9AA2),
                    )
                }
            }
        }
    }
}
