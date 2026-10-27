package me.lucid.domchat.ui.components.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.context.LocalTTSState
import me.lucid.domchat.ui.hooks.CustomTtsState
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.tts.model.PlaybackState
import me.lucid.tts.model.PlaybackStatus
import kotlin.math.roundToInt

@Composable
fun TTSController() {
    val ttsState = LocalTTSState.current
    val settings = LocalSettings.current
    val isDark = LocalDarkMode.current
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)

    val isSpeaking by ttsState.isSpeaking.collectAsState()
    val playbackState by ttsState.playbackState.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    var expand by remember { mutableStateOf(false) }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Follow global playback state
    LaunchedEffect(isSpeaking, playbackState.status) {
        if (isSpeaking ||
            playbackState.status == PlaybackStatus.Playing ||
            playbackState.status == PlaybackStatus.Buffering ||
            playbackState.status == PlaybackStatus.Paused
        ) {
            isVisible = true
        } else if (playbackState.status == PlaybackStatus.Idle || playbackState.status == PlaybackStatus.Ended) {
            delay(1500)
            if (!isSpeaking && (playbackState.status == PlaybackStatus.Idle || playbackState.status == PlaybackStatus.Ended)) {
                isVisible = false
                expand = false
            }
        }
    }

    val sheetBg = if (isDark) Color(0xFF16171E).copy(alpha = 0.70f) else Color.White.copy(alpha = 0.72f)
    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.30f else 0.16f),
            Color.White.copy(alpha = if (isDark) 0.08f else 0.04f),
            Color.Transparent
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .zIndex(150f),
        contentAlignment = Alignment.BottomStart
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(spring(dampingRatio = 0.82f, stiffness = 420f)) +
                    scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)) +
                    slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)),
            exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.85f) + slideOutVertically(targetOffsetY = { it / 3 }),
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .padding(start = 16.dp, bottom = 88.dp)
        ) {
            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = if (isDark) 14.dp else 8.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.35f),
                        spotColor = Color.Black.copy(alpha = 0.45f)
                    )
                    .domChatBlurEffect(containerColor = sheetBg, shape = CircleShape),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(0.6.dp, borderBrush)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glassy Play/Pause Button with circular progress rings
                    GlassyPlayPauseButton(
                        playbackState = playbackState,
                        ttsState = ttsState,
                        isDark = isDark,
                        onTap = { haptics.perform(HapticPattern.Pop) }
                    )

                    // Skip Next Chunk Button
                    IconButton(
                        onClick = {
                            haptics.perform(HapticPattern.Selection)
                            ttsState.skipNext()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FastForward,
                            contentDescription = "Next chunk",
                            tint = if (isDark) Color.White.copy(alpha = 0.80f) else Color.Black.copy(alpha = 0.70f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Stop & Close Button
                    IconButton(
                        onClick = {
                            haptics.perform(HapticPattern.Tick)
                            ttsState.stop()
                            isVisible = false
                            expand = false
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Stop",
                            tint = if (isDark) Color.White.copy(alpha = 0.80f) else Color.Black.copy(alpha = 0.70f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Expandable speed and fast forward controls
                    AnimatedVisibility(
                        visible = expand,
                        enter = expandHorizontally(spring(dampingRatio = 0.85f, stiffness = 420f)) + fadeIn(),
                        exit = shrinkHorizontally(spring(dampingRatio = 0.85f, stiffness = 420f)) + fadeOut()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Glassy Speed Pill
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
                                    .border(0.5.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.08f), CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = LocalIndication.current,
                                        onClick = {
                                            haptics.perform(HapticPattern.Selection)
                                            val nextSpeed = when {
                                                playbackState.speed < 0.95f -> 1.0f
                                                playbackState.speed < 1.15f -> 1.25f
                                                playbackState.speed < 1.45f -> 1.5f
                                                playbackState.speed < 1.95f -> 2.0f
                                                else -> 0.8f
                                            }
                                            ttsState.setSpeed(nextSpeed)
                                        }
                                    )
                                    .padding(horizontal = 9.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${playbackState.speed}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else Color(0xFF1C1C20)
                                )
                            }

                            // Fast forward 5s button
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = LocalIndication.current,
                                        onClick = {
                                            haptics.perform(HapticPattern.Pop)
                                            ttsState.fastForward(5000)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+5s",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    // Expand / Collapse Chevron Toggle
                    IconButton(
                        onClick = {
                            haptics.perform(HapticPattern.Tick)
                            expand = !expand
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (expand) Icons.Rounded.ChevronLeft else Icons.Rounded.ChevronRight,
                            contentDescription = if (expand) "Collapse" else "Expand",
                            tint = if (isDark) Color.White.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassyPlayPauseButton(
    playbackState: PlaybackState,
    ttsState: CustomTtsState,
    isDark: Boolean,
    onTap: () -> Unit
) {
    val isPlaying = playbackState.status == PlaybackStatus.Playing

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = {
                    onTap()
                    if (isPlaying) {
                        ttsState.pause()
                    } else {
                        ttsState.resume()
                    }
                }
            )
            .background(
                if (isPlaying) {
                    MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.32f else 0.22f)
                } else {
                    if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.07f)
                }
            )
            .border(
                0.5.dp,
                if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else Color.White.copy(alpha = if (isDark) 0.20f else 0.10f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (playbackState.status == PlaybackStatus.Playing ||
            playbackState.status == PlaybackStatus.Buffering ||
            playbackState.status == PlaybackStatus.Paused
        ) {
            val progress = if (playbackState.durationMs > 0 && isPlaying) {
                (playbackState.positionMs.toFloat() / playbackState.durationMs).coerceIn(0f, 1f)
            } else 0f

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 1.8.dp,
                trackColor = Color.Transparent
            )
        }

        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = if (isPlaying) MaterialTheme.colorScheme.primary else if (isDark) Color.White else Color(0xFF1C1C20),
            modifier = Modifier.size(19.dp)
        )
    }
}
