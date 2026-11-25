package me.lucid.domchat.utils

import android.content.Context
import me.lucid.locallm.LiteRtRuntime
import me.lucid.domchat.R

fun Throwable.toLocalInferenceUserMessage(context: Context): String? {
    val memoryError = generateSequence(this) { current ->
        current.cause?.takeUnless { it === current }
    }.filterIsInstance<LiteRtRuntime.InsufficientMemoryException>().firstOrNull()

    return memoryError?.info?.let { info ->
        context.getString(
            R.string.local_llm_insufficient_memory_format,
            info.requiredMb,
            info.modelMb,
            info.availableMb,
        )
    } ?: message
}
