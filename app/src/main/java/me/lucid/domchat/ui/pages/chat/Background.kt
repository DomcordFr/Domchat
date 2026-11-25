package me.lucid.domchat.ui.pages.chat

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter.State
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.BackgroundBlurMode
import me.lucid.domchat.ui.context.LocalDisplaySetting
import me.lucid.domchat.ui.motion.LocalMotionPolicy
import me.lucid.domchat.ui.theme.LocalDarkMode

@Composable
fun AssistantBackground(
    assistant: Assistant,
    conversationBackground: String? = null,
    conversationId: String? = null,
    modifier: Modifier = Modifier,
    onWallpaperLoaded: (() -> Unit)? = null,
) {
    val motionPolicy = LocalMotionPolicy.current
    val displaySetting = LocalDisplaySetting.current

    val perChatConfig = conversationId?.let { displaySetting.conversationWallpapers[it] }

    // 4-Tier Background Precedence: Per-Chat Config -> Conversation -> Assistant -> Global Setting
    val resolvedBackground = perChatConfig?.backgroundUri?.takeIf { it.isNotBlank() }
        ?: conversationBackground?.takeIf { it.isNotBlank() }
        ?: assistant.background?.takeIf { it.isNotBlank() }
        ?: displaySetting.globalBackground?.takeIf { it.isNotBlank() }

    val resolvedDim = if (perChatConfig != null && !perChatConfig.backgroundUri.isNullOrBlank()) {
        perChatConfig.backgroundDim
    } else if (!assistant.background.isNullOrBlank()) {
        assistant.backgroundDim
    } else {
        displaySetting.globalBackgroundDim
    }

    // Preserve explicit 0f blur on character wallpaper without wrongly falling back to global blur
    val resolvedBlur = if (perChatConfig != null && !perChatConfig.backgroundUri.isNullOrBlank()) {
        perChatConfig.backgroundBlur
    } else if (!assistant.background.isNullOrBlank()) {
        assistant.backgroundBlur
    } else {
        displaySetting.globalBackgroundBlur
    }

    val blurMode = if (perChatConfig != null && !perChatConfig.backgroundUri.isNullOrBlank()) {
        perChatConfig.blurMode
    } else {
        displaySetting.backgroundBlurMode
    }

    val blurHeightFraction = if (perChatConfig != null && !perChatConfig.backgroundUri.isNullOrBlank()) {
        perChatConfig.blurHeightFraction
    } else {
        displaySetting.globalBackgroundBlurHeightFraction
    }

    AnimatedContent(
        targetState = resolvedBackground,
        transitionSpec = {
            val fadeDuration = if (motionPolicy.reduceMotion) 80 else 180
            // Enter opaque on frame 1 so [wallpaperBackdrop] records opaque
            // pixels immediately instead of a transparent fade-in frame that
            // leaves liquid glass surfaces blank until the next redraw.
            fadeIn(animationSpec = tween(fadeDuration), initialAlpha = 1f) togetherWith
                fadeOut(animationSpec = tween(fadeDuration))
        },
        contentAlignment = Alignment.Center,
        label = "chat_wallpaper_transition",
        modifier = modifier,
    ) { bgUrl ->
        if (!bgUrl.isNullOrBlank()) {
            val isDark = LocalDarkMode.current
            val baseCanvasColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(baseCanvasColor)
            ) {
                val scrimAlpha = resolvedDim.coerceIn(0f, 0.95f)
                val scrimColor = if (isDark) {
                    Color.Black.copy(alpha = scrimAlpha)
                } else {
                    Color.White.copy(alpha = scrimAlpha)
                }
                val edgeFadeColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background

                val blurPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && resolvedBlur > 0f) {
                    with(LocalDensity.current) { resolvedBlur.dp.toPx().coerceAtLeast(1f) }
                } else {
                    0f
                }
                val cachedBlurRenderEffect = if (blurPx > 0f) {
                    remember(blurPx) {
                        try {
                            RenderEffect
                                .createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
                                .asComposeRenderEffect()
                        } catch (t: Throwable) {
                            null
                        }
                    }
                } else {
                    null
                }

                // Base Wallpaper Image
                AsyncImage(
                    model = bgUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.4f),
                    onState = { state ->
                        if (state is State.Success) {
                            // Wallpaper bitmap decoded: tell every liquid glass sampler to
                            // invalidate and re-record so they sample the real background
                            // instead of a stale or empty snapshot.
                            onWallpaperLoaded?.invoke()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (cachedBlurRenderEffect != null && blurMode == BackgroundBlurMode.FULL) {
                                Modifier.graphicsLayer {
                                    renderEffect = cachedBlurRenderEffect
                                }
                            } else {
                                Modifier
                            }
                        )
                )

                // Optional Bottom Fade Blur Layer (blurs bottom, fades out near top)
                if (cachedBlurRenderEffect != null && blurMode == BackgroundBlurMode.BOTTOM_FADE) {
                    val fadeStart = (1f - blurHeightFraction).coerceIn(0.01f, 0.95f)
                    val bottomFadeBrush = remember(fadeStart) {
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            fadeStart to Color.Transparent,
                            1f to Color.Black
                        )
                    }
                    AsyncImage(
                        model = bgUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.4f),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                renderEffect = cachedBlurRenderEffect
                                alpha = 0.99f
                            }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = bottomFadeBrush,
                                    blendMode = BlendMode.DstIn
                                )
                            }
                    )
                }

                // Dimmer Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(scrimColor)
                )

                // Gradient Edge Fades for Top/Bottom Navigation Text Contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to edgeFadeColor.copy(alpha = 0.55f),
                                0.18f to Color.Transparent,
                                0.72f to Color.Transparent,
                                1f to edgeFadeColor.copy(alpha = 0.65f),
                            )
                        )
                )
            }
        } else {
            // Default Canvas: Theme background + optical caustics for Haze refraction.
            // There is no async image decode here, so signal liquid glass samplers that
            // the backdrop is opaque and ready immediately on frame 0 (cold start /
            // blank chat) instead of leaving surfaces transparent until the next redraw.
            LaunchedEffect(Unit) {
                onWallpaperLoaded?.invoke()
            }
            val isDark = LocalDarkMode.current
            val baseCanvasColor = if (isDark) {
                Color.Black
            } else {
                MaterialTheme.colorScheme.background
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(baseCanvasColor)
            ) {
                // Subtle optical gradient providing luminous caustics for glassy bubbles and surfaces
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isDark) Color(0xFF1E2235).copy(alpha = 0.40f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                radius = 1200f
                            )
                        )
                )
            }
        }
    }
}
