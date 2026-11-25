/*
 * Originally vendored from Kyant0/backdrop v2.0.0 (io.github.kyant0:backdrop)
 * https://github.com/Kyant0/backdrop — Copyright 2025 Kyant0, Apache License 2.0
 *
 */
package me.lucid.domchat.ui.component.backdrop.effects

import androidx.compose.ui.graphics.RenderEffect
import me.lucid.domchat.ui.component.backdrop.BackdropEffectScope
import me.lucid.domchat.ui.component.backdrop.RuntimeShader
import me.lucid.domchat.ui.component.backdrop.internal.runtimeShaderEffect
import me.lucid.domchat.ui.component.backdrop.internal.chain
import me.lucid.domchat.ui.component.backdrop.isRenderEffectSupported
import me.lucid.domchat.ui.component.backdrop.isRuntimeShaderSupported
import org.intellij.lang.annotations.Language

fun BackdropEffectScope.effect(effect: RenderEffect) {
    if (!isRenderEffectSupported()) return

    renderEffect = renderEffect.chain(effect)
}

fun BackdropEffectScope.runtimeShaderEffect(
    key: String,
    @Language("AGSL") shaderString: String,
    uniformShaderName: String,
    block: RuntimeShader.() -> Unit
) {
    if (!isRuntimeShaderSupported()) return

    val effect =
        runtimeShaderEffect(
            runtimeShader = obtainRuntimeShader(key, shaderString).apply(block),
            uniformShaderName = uniformShaderName
        )
    renderEffect = renderEffect.chain(effect)
}
