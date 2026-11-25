package me.lucid.domchat.ui.components.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.lucid.domchat.BuildConfig
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.utils.DownloadProgress
import me.lucid.domchat.utils.UpdateChecker
import me.lucid.domchat.utils.UpdateDownloadStatus
import me.lucid.domchat.utils.UpdateInfo

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format("%.1f KB", bytes / 1_024.0)
        else -> "$bytes B"
    }
}

@Composable
fun UpdateDialog(
    info: UpdateInfo,
    updateChecker: UpdateChecker,
    onDismiss: () -> Unit,
    onLater: () -> Unit,
    onIgnore: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberPremiumHaptics()
    val currentVersionName = BuildConfig.VERSION_NAME
    var downloadId by remember(info.version) { mutableStateOf<Long?>(null) }
    val progress by remember(downloadId) {
        val id = downloadId
        if (id != null && id != -1L) {
            updateChecker.observeDownload(context, id)
        } else {
            flowOf(DownloadProgress())
        }
    }.collectAsState(initial = DownloadProgress())

    val primaryDownload = info.downloads.firstOrNull()
    val isDownloading = progress.status == UpdateDownloadStatus.RUNNING
    val isDownloaded = progress.status == UpdateDownloadStatus.SUCCESSFUL && progress.localUri != null
    val downloadFailed = progress.status == UpdateDownloadStatus.FAILED
    val needsInstallPermission = isDownloaded && !updateChecker.canInstallDownloadedUpdate(context)
    val buttonShape = RoundedCornerShape(12.dp)

    // Animated entrance and exit state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun animateAndClose(action: () -> Unit) {
        if (!isVisible) return
        isVisible = false
        scope.launch {
            delay(160)
            action()
        }
    }

    ApplyDialogWindowBlur()
    val dialogShape = RoundedCornerShape(26.dp)

    Dialog(
        onDismissRequest = {
            if (!isDownloading) {
                animateAndClose(onDismiss)
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isDownloading,
            dismissOnClickOutside = !isDownloading,
        )
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                    scaleIn(
                        initialScale = 0.88f,
                        animationSpec = spring(
                            dampingRatio = 0.72f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis = 140)) +
                   scaleOut(targetScale = 0.92f, animationSpec = tween(durationMillis = 140)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .widthIn(max = 440.dp)
        ) {
            Surface(
                shape = dialogShape,
                color = Color(0xFF000000),
                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderBrush),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Row with animated status icon, texts, and cross button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF000000))
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = Triple(isDownloaded, downloadFailed, isDownloading),
                                transitionSpec = {
                                    (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                     scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.6f)))
                                        .togetherWith(fadeOut(tween(100)))
                                },
                                label = "update_icon_anim"
                            ) { (downloaded, failed, _) ->
                                Icon(
                                    imageVector = when {
                                        downloaded -> Icons.Rounded.CheckCircle
                                        failed -> Icons.Rounded.Refresh
                                        else -> Icons.Rounded.SystemUpdateAlt
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        downloaded -> Color(0xFF34C759)
                                        failed -> MaterialTheme.colorScheme.error
                                        else -> Color.White
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = when {
                                    needsInstallPermission -> "Allow Installation"
                                    isDownloaded -> "Ready to Install"
                                    downloadFailed -> "Download Failed"
                                    isDownloading -> "Downloading Update"
                                    currentVersionName == info.version -> "Latest Release"
                                    else -> "Update Available"
                                },
                                transitionSpec = {
                                    (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                     expandVertically())
                                        .togetherWith(fadeOut(tween(100)))
                                },
                                label = "update_title_anim"
                            ) { titleText ->
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = Color.White
                                )
                            }
                            AnimatedContent(
                                targetState = Triple(progress.status, needsInstallPermission, downloadFailed),
                                transitionSpec = {
                                    (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)))
                                        .togetherWith(fadeOut(tween(100)))
                                },
                                label = "update_subtitle_anim"
                            ) { (status, permissionNeeded, failed) ->
                                Text(
                                    text = when {
                                        permissionNeeded -> "Allow installs for Domchat to continue."
                                        failed -> progress.errorMessage ?: "Download failed. Please try again."
                                        status == UpdateDownloadStatus.RUNNING -> "Downloading the latest version..."
                                        status == UpdateDownloadStatus.SUCCESSFUL -> "Update verified and ready to install."
                                        currentVersionName == info.version -> "You are on the latest release."
                                        else -> "A new version of Domchat is ready."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }

                        // Close / Cross Button
                        val closeInteraction = remember { MutableInteractionSource() }
                        val isClosePressed by closeInteraction.collectIsPressedAsState()
                        val closeScale by animateFloatAsState(
                            targetValue = if (isClosePressed) 0.90f else 1f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                            label = "close_button_press"
                        )

                        Surface(
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                animateAndClose(onDismiss)
                            },
                            enabled = !isDownloading,
                            shape = CircleShape,
                            color = Color(0xFF000000),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                            interactionSource = closeInteraction,
                            modifier = Modifier
                                .size(32.dp)
                                .graphicsLayer {
                                    scaleX = closeScale
                                    scaleY = closeScale
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = Color.White.copy(alpha = 0.70f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Version & Metadata Card
                    Surface(
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (currentVersionName == info.version) {
                                        Text(
                                            text = "v$currentVersionName",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Surface(
                                            color = Color(0xFF000000),
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                        ) {
                                            Text(
                                                text = "Latest",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = Color.White.copy(alpha = 0.70f),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "v$currentVersionName",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.50f)
                                        )
                                        Text(
                                            text = "→",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.40f)
                                        )
                                        Text(
                                            text = "v${info.version}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }

                                if (primaryDownload != null && primaryDownload.size.isNotBlank()) {
                                    Surface(
                                        color = Color(0xFF000000),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                    ) {
                                        Text(
                                            text = primaryDownload.size,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            if (primaryDownload != null) {
                                Text(
                                    text = primaryDownload.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.50f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Download Progress with smooth height expansion
                    AnimatedVisibility(
                        visible = downloadId != null,
                        enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                        exit = shrinkVertically(tween(150)) + fadeOut()
                    ) {
                        val animatedProgress by animateFloatAsState(
                            targetValue = progress.progress.coerceIn(0f, 1f),
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "update_download_progress"
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.12f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDownloaded) "Download complete" else "${(progress.progress * 100).toInt().coerceIn(0, 100)}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.70f)
                                )
                                if (progress.totalBytes > 0) {
                                    Text(
                                        text = "${formatBytes(progress.bytesDownloaded)} / ${formatBytes(progress.totalBytes)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.45f)
                                    )
                                }
                            }
                        }
                    }

                    // Release Notes
                    Text(
                        text = "Release Notes",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF000000))
                            .border(BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle), RoundedCornerShape(16.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp)
                    ) {
                        MarkdownBlock(
                            content = info.changelog.ifBlank { "No release notes provided." }
                        )
                    }

                    // Action Buttons with tactile press scale animations
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!isDownloading) {
                            val primaryInteraction = remember { MutableInteractionSource() }
                            val isPrimaryPressed by primaryInteraction.collectIsPressedAsState()
                            val primaryScale by animateFloatAsState(
                                targetValue = if (isPrimaryPressed) 0.96f else 1f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                                label = "primary_button_press"
                            )

                            Button(
                                onClick = {
                                    haptics.perform(HapticPattern.Pop)
                                    when {
                                        isDownloaded && progress.localUri != null -> {
                                            val localUri = progress.localUri ?: return@Button
                                            scope.launch {
                                                updateChecker.installDownloadedUpdateAsync(context, localUri, primaryDownload?.sha256)
                                            }
                                        }
                                        primaryDownload != null -> {
                                            downloadId = updateChecker.downloadUpdate(context, primaryDownload)
                                        }
                                    }
                                },
                                enabled = (isDownloaded && progress.localUri != null) || primaryDownload != null,
                                shape = buttonShape,
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderBrush),
                                interactionSource = primaryInteraction,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .graphicsLayer {
                                        scaleX = primaryScale
                                        scaleY = primaryScale
                                    },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF000000),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF000000).copy(alpha = 0.5f),
                                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    AnimatedContent(
                                        targetState = when {
                                            isDownloaded && needsInstallPermission -> "Allow & Install"
                                            isDownloaded -> "Install"
                                            downloadFailed -> "Try Again"
                                            currentVersionName == info.version -> "Install Latest"
                                            else -> "Update Now"
                                        },
                                        label = "primary_btn_text_anim"
                                    ) { btnText ->
                                        Text(
                                            text = btnText,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        if (!isDownloading && !isDownloaded) {
                            val ignoreInteraction = remember { MutableInteractionSource() }
                            val isIgnorePressed by ignoreInteraction.collectIsPressedAsState()
                            val ignoreScale by animateFloatAsState(
                                targetValue = if (isIgnorePressed) 0.96f else 1f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                                label = "ignore_button_press"
                            )

                            val laterInteraction = remember { MutableInteractionSource() }
                            val isLaterPressed by laterInteraction.collectIsPressedAsState()
                            val laterScale by animateFloatAsState(
                                targetValue = if (isLaterPressed) 0.96f else 1f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                                label = "later_button_press"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        animateAndClose(onIgnore)
                                    },
                                    shape = buttonShape,
                                    interactionSource = ignoreInteraction,
                                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0xFF000000),
                                        contentColor = Color.White.copy(alpha = 0.85f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .graphicsLayer {
                                            scaleX = ignoreScale
                                            scaleY = ignoreScale
                                        }
                                ) {
                                    Text("Ignore")
                                }
                                OutlinedButton(
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        animateAndClose(onLater)
                                    },
                                    shape = buttonShape,
                                    interactionSource = laterInteraction,
                                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0xFF000000),
                                        contentColor = Color.White.copy(alpha = 0.85f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .graphicsLayer {
                                            scaleX = laterScale
                                            scaleY = laterScale
                                        }
                                ) {
                                    Text("Later")
                                }
                            }
                        } else {
                            val hideInteraction = remember { MutableInteractionSource() }
                            val isHidePressed by hideInteraction.collectIsPressedAsState()
                            val hideScale by animateFloatAsState(
                                targetValue = if (isHidePressed) 0.96f else 1f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                                label = "hide_button_press"
                            )

                            OutlinedButton(
                                onClick = {
                                    haptics.perform(HapticPattern.Pop)
                                    animateAndClose(onLater)
                                },
                                shape = buttonShape,
                                interactionSource = hideInteraction,
                                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFF000000),
                                    contentColor = Color.White.copy(alpha = 0.85f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .graphicsLayer {
                                        scaleX = hideScale
                                        scaleY = hideScale
                                    }
                            ) {
                                Text(if (isDownloading) "Hide (Background)" else "Later")
                            }
                        }
                    }
                }
            }
        }
    }
}
