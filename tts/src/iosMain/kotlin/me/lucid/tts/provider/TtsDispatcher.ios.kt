package me.lucid.tts.provider

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val ttsIoDispatcher: CoroutineDispatcher = Dispatchers.Default
