package me.lucid.domchat.ui.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Message nodes present in the conversation at open time. Anything in this set
 * is history — it never animates. Only nodes that arrive during the session
 * (the user's new message and the bot's reply) play their one-shot entrance.
 */
val LocalPreexistingMessageNodes = androidx.compose.runtime.compositionLocalOf<Set<String>> { emptySet() }

/**
 * Process-lifetime registry of entrances that already played, keyed by stable
 * element id. Guarantees: opening a chat animates its visible turns once,
 * scrolling back and forth never replays, recompositions don't replay.
 */
object EntrancePlayed {
    // Bounded: a long chat stream could otherwise grow this without limit.
    private const val MAX = 4096
    private val played = mutableSetOf<String>()

    fun markIfNew(key: String): Boolean {
        if (played.size >= MAX) played.clear()
        return played.add(key)
    }

    /** Clear the registry so entrances replay on the next conversation open. */
    fun reset() {
        played.clear()
    }
}

/** Screen-space entrance direction, chosen by the caller from element position. */
enum class EntranceDirection { UP, DOWN, LEFT, RIGHT, DIAGONAL, NONE }

/**
 * One-shot entrance: slide-in from [from] toward rest + alpha fade, staggered
 * via [delayMs]. All animation is transform/opacity (graphicsLayer) so it
 * stays on the GPU and never triggers layout or draw invalidation. Skipped
 * entirely under reduce motion and for elements whose entrance already
 * played (kept smooth by never re-animating).
 */
@Composable
fun Modifier.entrance(
    key: String,
    motionPolicy: MotionPolicy,
    from: EntranceDirection = EntranceDirection.UP,
    delayMs: Int = 0,
    distance: Dp = 30.dp,
    allow: Boolean = true,
    blur: Dp = 0.dp,
): Modifier {
    val shouldAnimate = remember(key) {
        allow && !motionPolicy.reduceMotion && EntrancePlayed.markIfNew(key)
    }
    if (!shouldAnimate) {
        return this
    }
    val progress = remember(key) { Animatable(0f) }
    val distancePx = with(LocalDensity.current) { distance.toPx() }
    val blurPx = with(LocalDensity.current) { blur.toPx() }
    LaunchedEffect(key) {
        val delayTime = motionPolicy.duration(delayMs.coerceIn(0, ENTRANCE_MAX_STAGGER_MS)).toLong()
        if (delayTime > 0) {
            delay(delayTime)
        }
        progress.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = 0.74f,
                stiffness = 260f,
            )
        )
    }
    var result = this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        // Smooth organic scale-in: 0.94f -> 1.0f, with subtle spring settle.
        val scale = 0.94f + 0.06f * p
        scaleX = scale
        scaleY = scale
        val d = distancePx * (1f - p)
        when (from) {
            EntranceDirection.UP -> translationY = d
            EntranceDirection.DOWN -> translationY = -d
            EntranceDirection.LEFT -> translationX = -d
            EntranceDirection.RIGHT -> translationX = d
            // Enters from the top-left corner, drifting toward bottom-right rest.
            EntranceDirection.DIAGONAL -> {
                translationX = -d
                translationY = -d
            }
            EntranceDirection.NONE -> Unit
        }
    }
    // Optional dissolve-in blur. Reads progress in composition (recomposes the
    // caller per frame during the entrance only), so callers must opt in.
    if (blurPx > 0f) {
        val blurRadius = (blurPx * (1f - progress.value)).coerceAtLeast(0f)
        if (blurRadius > 0.05f) {
            result = result.blur(with(LocalDensity.current) { blurRadius.toDp() })
        }
    }
    return result
}

private const val ENTRANCE_MAX_STAGGER_MS = 900
