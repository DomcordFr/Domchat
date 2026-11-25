package me.lucid.domchat.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.DrawableRes
import me.lucid.domchat.R

/**
 * Validates that [resId] is actually a valid drawable or mipmap resource in the current APK context.
 * Guards against stale/deserialized resource integers throwing IllegalArgumentException or
 * Resources.NotFoundException across app updates or schema changes.
 *
 * If invalid, points to non-drawable, or throws, returns [fallback].
 */
fun safeDrawableResId(
    context: Context,
    resId: Int,
    @DrawableRes fallback: Int = R.drawable.default_generical_pfp
): Int {
    if (resId <= 0) return fallback
    return try {
        val typeName = context.resources.getResourceTypeName(resId)
        if (typeName == "drawable" || typeName == "mipmap") {
            val typedValue = TypedValue()
            context.resources.getValue(resId, typedValue, true)
            val path = typedValue.string?.toString()
            if (path != null && (
                path.endsWith(".xml") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                path.endsWith(".webp")
            )) {
                resId
            } else {
                fallback
            }
        } else {
            fallback
        }
    } catch (_: Throwable) {
        fallback
    }
}
