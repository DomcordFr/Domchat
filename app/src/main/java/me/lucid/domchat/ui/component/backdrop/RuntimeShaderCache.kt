/*
 * Originally vendored from Kyant0/backdrop v2.0.0 (io.github.kyant0:backdrop)
 * https://github.com/Kyant0/backdrop — Copyright 2025 Kyant0, Apache License 2.0
 *
 */
package me.lucid.domchat.ui.component.backdrop

import org.intellij.lang.annotations.Language

sealed interface RuntimeShaderCache {

    fun obtainRuntimeShader(key: String, @Language("AGSL") string: String): RuntimeShader
}

/** Thread-safe cache of compiled AGSL RuntimeShaders keyed by shader source. */
internal class RuntimeShaderCacheImpl(private val allowClear: Boolean = true) : RuntimeShaderCache {

    private val runtimeShaders = mutableMapOf<String, RuntimeShader>()

    override fun obtainRuntimeShader(key: String, string: String): RuntimeShader {
        return synchronized(runtimeShaders) {
            runtimeShaders.getOrPut(key) { RuntimeShader(string) }
        }
    }

    fun clear() {
        if (!allowClear) return
        synchronized(runtimeShaders) {
            runtimeShaders.clear()
        }
    }
}

internal val SharedRuntimeShaderCache = RuntimeShaderCacheImpl(allowClear = false)
