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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.BackgroundBlurMode
import me.lucid.domchat.ui.hooks.rememberUserSettingsState
import me.lucid.domchat.ui.motion.LocalMotionPolicy
import me.lucid.domchat.ui.theme.LocalDarkMode

@Composable
fun AssistantBackground(
    assistant: Assistant,
    conversationBackground: String? = null,
    conversationId: String? = null,
    modifier: Modifier = Modifier,
) {
    val motionPolicy = LocalMotionPolicy.current
    val settings by rememberUserSettingsState()

    val perChatConfig = conversationId?.let { settings.displaySetting.conversationWallpapers[it] }

    // 4-Tier Background Precedence: Per-Chat Config -> Conversation -> Assistant -> Global Setting
    val resolvedBackground = perChatConfig?.backgroundUri?.takeIf { it.isNotBlank() }
        ?: conversationBackground?.takeIf { it.isNotBlank() }
        ?: assistant.background?.takeIf { it.isNotBlank() }
        ?: settings.displaySetting.globalBackground?.takeIf { it.isNotBlank() }

    val resolvedDim = if (perChatConfig != null && !perChatConfig.backgroundUri.isNullOrBlank()) {
        perChatConfig.backgroundDim
    } else if (!assistant.background.isNullOrBlank()) {
        assistant.backgroundDim
    } else {
        settings.displaySetting.globalBackgroundDim
    }

    // Preserve explicit 0f blur on character wallpaper without wrongly falling back to global blur
    val resolvedBlur = if (perChatConfig != null && !perChatConfig.backgroundUri.isNullOrBlank()) {
        perChatConfig.backgroundBlur
    } else if (!assistant.background.isNullOrBlank()) {
        assistant.backgroundBlur
    } else {
        settings.displaySetting.globalBackgroundBlur
    }

    val blurMode = if (perChatConfig != null && !perChatConfig.backgroundUri.isNullOrBlank()) {
        perChatConfig.blurMode
    } else {
        settings.displaySetting.backgroundBlurMode
    }

    val blurHeightFraction = if (perChatConfig != null && !perChatConfig.backgroundUri.isNullOrBlank()) {
        perChatConfig.blurHeightFraction
    } else {
        settings.displaySetting.globalBackgroundBlurHeightFraction
    }

    AnimatedContent(
        targetState = resolvedBackground,
        transitionSpec = {
            val fadeDuration = if (motionPolicy.reduceMotion) 80 else 180
            fadeIn(animationSpec = tween(fadeDuration)) togetherWith
                fadeOut(animationSpec = tween(fadeDuration))
        },
        contentAlignment = Alignment.Center,
        label = "chat_wallpaper_transition",
        modifier = modifier,
    ) { bgUrl ->
        if (!bgUrl.isNullOrBlank()) {
            Box(modifier = Modifier.fillMaxSize()) {
                val isDark = LocalDarkMode.current
                val scrimAlpha = resolvedDim.coerceIn(0f, 0.95f)
                val scrimColor = if (isDark) {
                    Color.Black.copy(alpha = scrimAlpha)
                } else {
                    Color.White.copy(alpha = scrimAlpha)
                }
                val edgeFadeColor = if (isDark) Color.Black else MaterialTheme.colorScheme.background

                // Base Wallpaper Image
                AsyncImage(
                    model = bgUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.4f),
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && resolvedBlur > 0f && blurMode == BackgroundBlurMode.FULL) {
                                Modifier.graphicsLayer {
                                    val blurPx = resolvedBlur.dp.toPx().coerceAtLeast(1f)
                                    renderEffect = try {
                                        RenderEffect
                                            .createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
                                            .asComposeRenderEffect()
                                    } catch (t: Throwable) {
                                        null
                                    }
                                }
                            } else {
                                Modifier
                            }
                        )
                )

                // Optional Bottom Fade Blur Layer (blurs bottom, fades out near top)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && resolvedBlur > 0f && blurMode == BackgroundBlurMode.BOTTOM_FADE) {
                    val fadeStart = (1f - blurHeightFraction).coerceIn(0.01f, 0.95f)
                    AsyncImage(
                        model = bgUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.4f),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val blurPx = resolvedBlur.dp.toPx().coerceAtLeast(1f)
                                renderEffect = try {
                                    RenderEffect
                                        .createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
                                        .asComposeRenderEffect()
                                } catch (t: Throwable) {
                                    null
                                }
                                alpha = 0.99f
                            }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        fadeStart to Color.Transparent,
                                        1f to Color.Black
                                    ),
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
            // Default Canvas: Theme background + optical caustics for Haze refraction
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
