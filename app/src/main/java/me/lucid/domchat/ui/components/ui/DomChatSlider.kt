package me.lucid.domchat.ui.components.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.LocalDarkMode

/**
 * The single app-wide slider: bright white thumb/track on dark glass, subtly scaled for touch.
 * Includes tactile haptic ticks on step crossing and magnetic sticky snap points.
 * Every in-app slider (fade duration, samplers, context budgets, depth, etc.) must use this.
 */
@Composable
fun DomChatSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    stickyPoints: List<Float> = emptyList(),
    onValueChangeFinished: (() -> Unit)? = null,
    activeTrackColorOverride: Color? = null,
    thumbColorOverride: Color? = null,
) {
    val isDark = LocalDarkMode.current
    val haptics = rememberPremiumHaptics()

    val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
    val snapThreshold = rangeSpan * 0.035f

    // Calculate step interval for sticky snapping and haptics
    val stepCount = if (steps > 0) steps + 1 else 20
    val stepSize = rangeSpan / stepCount.toFloat()

    var lastHapticBucket by remember(valueRange, steps) {
        val initialBucket = if (stepSize > 0f) {
            ((value - valueRange.start) / stepSize).roundToInt().toFloat()
        } else 0f
        mutableFloatStateOf(initialBucket)
    }

    var wasSnappedToSticky by remember { mutableStateOf(false) }

    val sliderColors = SliderDefaults.colors(
        thumbColor = thumbColorOverride ?: if (isDark) Color.White else MaterialTheme.colorScheme.primary,
        activeTrackColor = activeTrackColorOverride
            ?: if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary,
        inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
    )

    Slider(
        value = value,
        onValueChange = { rawVal ->
            val nearSticky = stickyPoints.firstOrNull { kotlin.math.abs(rawVal - it) <= snapThreshold }
            val (resolvedVal, isStickySnap) = if (nearSticky != null) {
                nearSticky to true
            } else if (steps > 0 && stepSize > 0f) {
                val bucket = ((rawVal - valueRange.start) / stepSize).roundToInt()
                (valueRange.start + bucket * stepSize).coerceIn(valueRange.start, valueRange.endInclusive) to false
            } else {
                rawVal to false
            }

            if (isStickySnap) {
                if (!wasSnappedToSticky) {
                    haptics.perform(HapticPattern.Pop)
                    wasSnappedToSticky = true
                }
            } else {
                wasSnappedToSticky = false
                val currentBucket = if (stepSize > 0f) {
                    ((resolvedVal - valueRange.start) / stepSize).roundToInt().toFloat()
                } else 0f

                if (currentBucket != lastHapticBucket) {
                    lastHapticBucket = currentBucket
                    haptics.perform(HapticPattern.Tick)
                }
            }

            onValueChange(resolvedVal)
        },
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = sliderColors,
    )
}

