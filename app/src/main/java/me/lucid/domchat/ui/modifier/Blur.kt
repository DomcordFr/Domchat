package me.lucid.domchat.ui.modifier

import android.os.Build
import android.view.ViewParent
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.materials.HazeMaterials
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import me.lucid.domchat.ui.theme.AppSurface
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.liquidGlassSurfaceColor
import me.lucid.domchat.ui.context.LocalDisplaySetting

data class DomChatBlur(
    val enabled: Boolean = false,
    val hazeState: HazeState? = null,
)

val LocalDomChatBlur = staticCompositionLocalOf { DomChatBlur() }

@Composable
fun Modifier.domChatBlurSource(): Modifier {
    val blur = LocalDomChatBlur.current
    val hazeState = blur.hazeState
    val hostAttached = rememberHazeHostAttached()
    return if (blur.enabled && hazeState != null && hostAttached) {
        this.hazeSource(state = hazeState, zIndex = -1f)
    } else {
        this
    }
}

/**
 * Applies native hardware window blur behind Dialogs and ModalBottomSheets on Android 12+ (API 31+).
 * Safe, zero-overhead, and immune to cross-window composition detachments.
 */
@Composable
fun ApplyDialogWindowBlur(
    blurRadius: Dp? = null,
) {
    val view = LocalView.current
    val display = LocalDisplaySetting.current
    val glassAlpha = display.glassAlpha.coerceIn(0f, 1f)
    if (glassAlpha <= 0.02f) {
        return
    }
    val effectiveRadius = blurRadius ?: display.glassBlur.dp
    val blurActive = display.enableBlurEffect || 
                     display.liquidGlassMode != me.lucid.domchat.data.model.LiquidGlassMode.SOFT_FROSTED

    DisposableEffect(view, effectiveRadius, blurActive) {
        var resolvedWindow: android.view.Window? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurActive) {
            var current: ViewParent? = view.parent
            while (current != null) {
                if (current is DialogWindowProvider) {
                    resolvedWindow = current.window
                    break
                }
                current = current.parent
            }
            if (resolvedWindow == null) {
                var ctx = view.context
                while (ctx is android.content.ContextWrapper) {
                    if (ctx is android.app.Dialog) {
                        resolvedWindow = ctx.window
                        break
                    }
                    if (ctx is android.app.Activity) {
                        break
                    }
                    ctx = ctx.baseContext
                }
            }
            if (resolvedWindow == null) {
                try {
                    val root = view.rootView
                    var clazz: Class<*>? = root.javaClass
                    while (clazz != null && resolvedWindow == null) {
                        try {
                            val f = clazz.getDeclaredField("mWindow").apply { isAccessible = true }
                            resolvedWindow = f.get(root) as? android.view.Window
                        } catch (_: Throwable) {}
                        clazz = clazz.superclass
                    }
                } catch (_: Throwable) {}
            }
            resolvedWindow?.let { win ->
                win.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                val density = view.resources.displayMetrics.density
                val radiusPx = (effectiveRadius.value * density).toInt().coerceAtLeast(1)
                try {
                    win.setBackgroundBlurRadius(radiusPx)
                } catch (_: Throwable) {}
                try {
                    val lp = win.attributes
                    lp.blurBehindRadius = radiusPx
                    win.attributes = lp
                } catch (_: Throwable) {}
            }
        }
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurActive) {
                resolvedWindow?.let { win ->
                    try {
                        win.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                        win.setBackgroundBlurRadius(0)
                        val lp = win.attributes
                        lp.blurBehindRadius = 0
                        win.attributes = lp
                    } catch (_: Throwable) {}
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalHazeApi::class)
fun Modifier.domChatBlurEffect(
    containerColor: Color = domChatSheetContainerColor(),
    shape: Shape? = null,
    blurRadius: Dp? = null,
): Modifier {
    val blur = LocalDomChatBlur.current
    val hazeState = blur.hazeState
    val display = LocalDisplaySetting.current
    val glassAlpha = display.glassAlpha.coerceIn(0f, 1f)
    val effectiveRadius = blurRadius ?: display.glassBlur.dp
    val isEffectivelyTransparent = containerColor == Color.Transparent || containerColor.alpha <= 0.02f
    val isEffectivelyNoBlur = effectiveRadius <= 0.5.dp

    if (isEffectivelyNoBlur && isEffectivelyTransparent) {
        return if (shape != null) this.clip(shape) else this
    }

    val style = HazeMaterials.ultraThin(containerColor = if (isEffectivelyTransparent) Color.Transparent else containerColor)
    val hostAttached = rememberHazeHostAttached()
    
    val view = LocalView.current
    val isAttachedToSeparateWindow = remember(view) {
        val activity = view.context as? android.app.Activity
        val decorView = activity?.window?.decorView
        decorView != null && view.rootView != decorView
    }

    if (isAttachedToSeparateWindow) {
        ApplyDialogWindowBlur(blurRadius = effectiveRadius)
        val base = if (shape != null) this.clip(shape) else this
        return if (containerColor != Color.Unspecified && containerColor != Color.Transparent) {
            base.background(containerColor, shape ?: androidx.compose.ui.graphics.RectangleShape)
        } else {
            base
        }
    }

    return if (blur.enabled && hazeState != null && hostAttached) {
        val clippedModifier = if (shape != null) this.clip(shape) else this
        val effectModifier = clippedModifier.hazeEffect(state = hazeState) {
            clipToAreasBounds = true
            expandLayerBounds = false
            forceInvalidateOnPreDraw = false
            blurEffect {
                if (!isEffectivelyTransparent) {
                    this.style = style
                }
                this.blurRadius = effectiveRadius
            }
        }
        if (shape != null) {
            effectModifier.clip(shape)
        } else {
            effectModifier
        }
    } else {
        if (shape != null && containerColor != Color.Unspecified && containerColor != Color.Transparent) {
            this.clip(shape).background(containerColor, shape)
        } else {
            this
        }
    }
}

@Composable
fun domChatBaseToneColor(): Color {
    val display = LocalDisplaySetting.current
    val luminance = display.glassBaseLuminance.coerceIn(0f, 1f)
    return androidx.compose.ui.graphics.lerp(Color.Black, Color.White, luminance)
}

/**
 * Charcoal chrome surface for the side drawer/menu.
 * Follows the [DisplaySetting.linkChromeToGlass] toggle: when linked it tracks
 * glassAlpha + glassBaseLuminance (fully transparent at 0%), otherwise keeps the
 * legacy fixed charcoal glass.
 */
@Composable
fun domChatChromeSurfaceColor(): Color {
    val display = LocalDisplaySetting.current
    if (!display.linkChromeToGlass) {
        val alpha = display.glassAlpha.coerceIn(0f, 1f)
        if (alpha <= 0.001f) return Color.Transparent
        val base = Color(0xFF141520)
        return base.copy(alpha = alpha)
    }
    return liquidGlassSurfaceColor()
}

/** Charcoal glass floor for chrome fills so chrome never outshines the content behind it. */
const val DOMCHAT_CHROME_FLOOR_ALPHA = 0.45f

/**
 * True when the drawer/menu surfaces sit on a nearly transparent glass (linked chrome + low
 * glassAlpha). Callers use it to add text shadows so labels stay readable with no backdrop.
 */
@Composable
fun domChatNeedsLegibilityShadow(): Boolean {
    val display = LocalDisplaySetting.current
    return display.linkChromeToGlass && display.glassAlpha.coerceIn(0f, 1f) < 0.25f
}

/** Subtle dark halo for text rendered over transparent chrome surfaces. */
@Composable
fun domChatChromeTextShadow(softText: Boolean = false): androidx.compose.ui.graphics.Shadow? {
    if (!domChatNeedsLegibilityShadow()) return null
    return androidx.compose.ui.graphics.Shadow(
        color = Color.Black.copy(alpha = if (softText) 0.8f else 0.65f),
        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
        blurRadius = 4f,
    )
}

/**
 * Icon pill backgrounds (hamburger / toolbar actions / floating chrome buttons).
 * When linked, tracks glassAlpha exactly (fully transparent at 0%) — same behavior
 * as the chat bar. Falls back to fixed charcoal glass when unlinked.
 */
@Composable
fun domChatIconPillContainerColor(): Color {
    val display = LocalDisplaySetting.current
    if (!display.linkChromeToGlass) {
        val alpha = display.glassAlpha.coerceIn(0f, 1f)
        if (alpha <= 0.001f) return Color.Transparent
        val base = Color(0xFF141520)
        return base.copy(alpha = alpha)
    }
    // Match bottom bar: clean clear floating control fill
    return Color.Transparent
}

/**
 * Charcoal floating fill. Glass alpha and base luminance are dynamically derived from settings.
 */
@Composable
fun domChatFloatingSurfaceColor(
    charcoal: Color = AppSurface.fill(MaterialTheme.colorScheme),
): Color {
    val display = LocalDisplaySetting.current
    if (!display.linkChromeToGlass) {
        val alpha = display.glassAlpha.coerceIn(0f, 1f)
        if (alpha <= 0.001f) return Color.Transparent
        val base = Color(0xFF141520)
        return base.copy(alpha = alpha)
    }
    return liquidGlassSurfaceColor()
}

@Composable
fun blurredContainerColor(
    fallback: Color,
): Color {
    val display = LocalDisplaySetting.current
    val alpha = display.glassAlpha.coerceIn(0f, 1f)
    if (alpha <= 0.001f) return Color.Transparent
    return liquidGlassSurfaceColor()
}

/** Sheets use dynamic base luminance tone layered with glass alpha. Directly follows glassAlpha setting. */
@Composable
fun domChatSheetContainerColor(): Color {
    val display = LocalDisplaySetting.current
    val alpha = display.glassAlpha.coerceIn(0f, 1f)
    if (alpha <= 0.001f) return Color.Transparent
    return liquidGlassSurfaceColor()
}

@Composable
fun domChatSheetContentColor(): Color {
    val display = LocalDisplaySetting.current
    val isDark = LocalDarkMode.current
    val luminance = display.glassBaseLuminance.coerceIn(0f, 1f)
    return when {
        luminance >= 0.55f -> Color(0xFF000000)
        isDark -> Color(0xFFF2F2F7)
        else -> Color.White
    }
}

/** Glassy cards throughout the app follow user glass transparency and base luminance tone. */
@Composable
fun domChatCardContainerColor(): Color = liquidGlassSurfaceColor()

/**
 * Frosted surface color for chat bubbles: gentle tinted base over [base].
 * When [DisplaySetting.glassTintColor] is non-clear, the tint wash is layered over [base]
 * using the tint color's alpha as wash strength.
 */
@Composable
fun domChatFrostedContainerColor(base: Color): Color {
    val display = LocalDisplaySetting.current
    val tintColor = Color(display.glassTintColor)
    val strength = tintColor.alpha.coerceIn(0f, 1f)
    return if (strength > 0.02f) {
        lerp(base, tintColor, strength)
    } else {
        base
    }
}

@Composable
fun domChatCardContentColor(): Color {
    val display = LocalDisplaySetting.current
    val isDark = LocalDarkMode.current
    val luminance = display.glassBaseLuminance.coerceIn(0f, 1f)
    return when {
        luminance >= 0.55f -> Color(0xFF000000)
        isDark -> Color(0xFFF2F2F7)
        else -> Color.White
    }
}

/**
 * Pop-up surface for settings and persona editors (per design charter):
 * near-black glass plate with a fixed, readable tone — never transparent,
 * always behind the specular border dialogs carry.
 */
@Composable
fun domChatDialogContainerColor(): Color = Color(0xFF000000)

@Composable
fun domChatSoftEdgeBorder(): BorderStroke = BorderStroke(
    AppSurface.SoftEdgeWidth,
    AppSurface.softEdgeColor(MaterialTheme.colorScheme),
)

@Composable
fun domChatSheetTonalElevation(): Dp = AppSurface.TonalElevation

@Composable
private fun rememberHazeHostAttached(): Boolean {
    var attached by remember { mutableStateOf(true) }
    DisposableEffect(Unit) {
        attached = true
        onDispose { attached = false }
    }
    return attached
}

internal fun isDetachedLayoutCoordinate(error: Throwable): Boolean {
    return error is IllegalStateException &&
        error.message?.contains("isAttached") == true
}

internal fun isHazeDetachedCoordinateCrash(error: Throwable): Boolean {
    if (!isDetachedLayoutCoordinate(error)) return false
    return generateSequence(error) { it.cause }
        .any { throwable ->
            throwable.stackTrace.any { frame ->
                frame.className.startsWith("dev.chrisbanes.haze")
            }
        }
}

internal inline fun <T> runCatchingDetachedLayout(block: () -> T): T? {
    return try {
        block()
    } catch (error: IllegalStateException) {
        if (isDetachedLayoutCoordinate(error)) null else throw error
    }
}

// Backward compatibility aliases
typealias LastChatBlur = DomChatBlur
val LocalLastChatBlur get() = LocalDomChatBlur
@Composable fun Modifier.lastChatBlurSource() = this.domChatBlurSource()
@Composable fun Modifier.lastChatBlurEffect(containerColor: Color = domChatSheetContainerColor(), shape: Shape? = null, blurRadius: Dp? = null) = this.domChatBlurEffect(containerColor, shape, blurRadius)
@Composable fun lastChatBaseToneColor() = domChatBaseToneColor()
@Composable fun lastChatFloatingSurfaceColor(charcoal: Color = AppSurface.fill(MaterialTheme.colorScheme)) = domChatFloatingSurfaceColor(charcoal)
@Composable fun lastChatSheetContainerColor() = domChatSheetContainerColor()
@Composable fun lastChatSheetContentColor() = domChatSheetContentColor()
@Composable fun lastChatCardContainerColor() = domChatCardContainerColor()
@Composable fun lastChatCardContentColor() = domChatCardContentColor()
@Composable fun lastChatDialogContainerColor() = domChatDialogContainerColor()
@Composable fun lastChatSoftEdgeBorder() = domChatSoftEdgeBorder()
@Composable fun lastChatSheetTonalElevation() = domChatSheetTonalElevation()
