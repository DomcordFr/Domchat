package me.lucid.domchat.ui.components.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.lucid.domchat.R
import me.lucid.domchat.ui.motion.LocalMotionPolicy

/**
 * Full-screen brand splash: logo settles onto solid black, then the whole
 * overlay crossfades out while the mark drifts forward for depth.
 *
 * Every animated property here is transform/opacity (graphicsLayer) so the
 * startup hand-off stays on the GPU — no layout, no draw invalidation, no
 * dropped first frame over the app's own first composition. Reduce motion
 * skips straight to the app.
 */
@Composable
fun SplashRevealOverlay() {
    val motionPolicy = LocalMotionPolicy.current
    // Guards against re-showing when the composition is recreated for the same
    // activity instance (nav host edits, window insets changes).
    var visible by remember { mutableStateOf(true) }
    val overlayAlpha = remember { Animatable(1f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(LOGO_ENTER_SCALE_START) }

    LaunchedEffect(Unit) {
        if (motionPolicy.reduceMotion || SplashShown.once) {
            visible = false
            return@LaunchedEffect
        }
        SplashShown.once = true
        // Logo settles in: opacity + a compact scale-up spring.
        launch {
            logoAlpha.animateTo(
                1f,
                animationSpec = tween(durationMillis = motionPolicy.duration(LOGO_ENTER_MS))
            )
        }
        logoScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = motionPolicy.pressStiffness(520f)
            )
        )
        launch {
            // Drift forward while the overlay releases — reads as depth, not flicker.
            logoScale.animateTo(
                LOGO_EXIT_SCALE,
                animationSpec = tween(
                    durationMillis = motionPolicy.duration(SPLASH_FADE_MS),
                    easing = FastOutSlowInEasing
                )
            )
        }
        delay(motionPolicy.duration(SPLASH_HOLD_MS).toLong())
        overlayAlpha.animateTo(
            0f,
            animationSpec = tween(
                durationMillis = motionPolicy.duration(SPLASH_FADE_MS),
                easing = FastOutSlowInEasing
            )
        )
        visible = false
    }

    if (!visible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = overlayAlpha.value }
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.domchat_setup_icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .graphicsLayer {
                    this.alpha = logoAlpha.value
                    this.scaleX = logoScale.value
                    this.scaleY = logoScale.value
                }
                .size(136.dp)
        )
    }
}

private object SplashShown {
    var once = false
}

private const val LOGO_ENTER_SCALE_START = 0.94f
private const val LOGO_EXIT_SCALE = 1.045f
private const val LOGO_ENTER_MS = 190
private const val SPLASH_HOLD_MS = 240
private const val SPLASH_FADE_MS = 350
