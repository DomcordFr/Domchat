package me.lucid.domchat.ui.pages.assistant.detail

import android.content.Context
import me.lucid.domchat.R
import me.lucid.domchat.utils.currentAppLocale
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal const val SPONTANEOUS_SCHEDULE_SLIDER_MIN = 0
internal const val SPONTANEOUS_SCHEDULE_START_MAX = 23
internal const val SPONTANEOUS_SCHEDULE_SLIDER_MAX = 47
internal const val SPONTANEOUS_SCHEDULE_SLIDER_STEPS = 46

internal data class SpontaneousScheduleSelection(
    val absoluteStart: Int,
    val absoluteEnd: Int,
) {
    val startHour: Int
        get() = absoluteStart.mod(24)

    val endHour: Int
        get() = absoluteEnd.mod(24)

    val spanHours: Int
        get() = (absoluteEnd - absoluteStart).coerceIn(1, 24)

    val isAllDay: Boolean
        get() = spanHours == 24

    val endsNextDay: Boolean
        get() = !isAllDay && absoluteEnd >= 24

    fun toSliderValues(): ClosedFloatingPointRange<Float> {
        return absoluteStart.toFloat()..absoluteEnd.toFloat()
    }
}

internal fun spontaneousScheduleSelectionFromHours(
    startHour: Int,
    endHour: Int,
): SpontaneousScheduleSelection {
    val normalizedStart = startHour.mod(24)
    val normalizedEnd = endHour.mod(24)
    val absoluteEnd = when {
        normalizedStart == normalizedEnd -> normalizedStart + 24
        normalizedStart < normalizedEnd -> normalizedEnd
        else -> normalizedEnd + 24
    }
    return normalizeSpontaneousScheduleSelection(
        rawStart = normalizedStart,
        rawEnd = absoluteEnd,
    )
}

internal fun normalizeSpontaneousScheduleSelection(
    rawStart: Int,
    rawEnd: Int,
): SpontaneousScheduleSelection {
    val clampedStart = rawStart.coerceIn(0, SPONTANEOUS_SCHEDULE_START_MAX)
    val minEnd = (clampedStart + 1).coerceAtMost(SPONTANEOUS_SCHEDULE_SLIDER_MAX)
    val maxEnd = (clampedStart + 24).coerceAtMost(SPONTANEOUS_SCHEDULE_SLIDER_MAX)
    val clampedEnd = rawEnd
        .coerceAtLeast(minEnd)
        .coerceAtMost(maxEnd)
    return SpontaneousScheduleSelection(
        absoluteStart = clampedStart,
        absoluteEnd = clampedEnd,
    )
}

internal fun spontaneousScheduleSelectionToHours(
    selection: SpontaneousScheduleSelection,
): Pair<Int, Int> {
    val normalizedSelection = normalizeSpontaneousScheduleSelection(
        rawStart = selection.absoluteStart,
        rawEnd = selection.absoluteEnd,
    )
    val startHour = normalizedSelection.startHour
    val endHour = if (normalizedSelection.isAllDay) {
        startHour
    } else {
        (startHour + normalizedSelection.spanHours).mod(24)
    }
    return startHour to endHour
}

internal fun formatHourLabel(hour: Int): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", currentAppLocale())
    return LocalTime.of(hour.mod(24), 0).format(formatter)
}

internal fun formatFrequencyLabel(context: Context, hours: Int): String {
    return if (hours == 1) {
        context.getString(R.string.assistant_advanced_frequency_hour)
    } else {
        context.getString(R.string.assistant_advanced_frequency_hours, hours)
    }
}

internal fun formatScheduleEndLabel(
    context: Context,
    selection: SpontaneousScheduleSelection,
): String {
    val endLabel = formatHourLabel(selection.endHour)
    return if (selection.endsNextDay) {
        context.getString(R.string.assistant_advanced_next_day, endLabel)
    } else {
        endLabel
    }
}

internal fun buildSpontaneousScheduleStatusLabel(
    context: Context,
    selection: SpontaneousScheduleSelection,
): String {
    return if (selection.isAllDay) {
        context.getString(R.string.assistant_advanced_all_day)
    } else {
        context.getString(
            R.string.assistant_advanced_duration,
            formatFrequencyLabel(context, selection.spanHours)
        )
    }
}

internal fun buildSpontaneousWindowSummary(
    context: Context,
    selection: SpontaneousScheduleSelection,
): String {
    val startLabel = formatHourLabel(selection.startHour)
    val endLabel = formatScheduleEndLabel(context, selection)
    return if (selection.isAllDay) {
        context.getString(R.string.assistant_advanced_reach_out_any_time)
    } else {
        context.getString(R.string.assistant_advanced_reach_out_between, startLabel, endLabel)
    }
}
