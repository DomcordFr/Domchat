package me.lucid.domchat.data.ai

import me.lucid.ai.provider.Model
import me.lucid.ai.provider.TextGenerationParams
import me.lucid.domchat.data.datastore.Settings

internal fun Settings.buildTitleGenerationParams(model: Model): TextGenerationParams {
    return TextGenerationParams(
        model = model,
        temperature = 0.3f,
        thinkingBudget = titleThinkingBudget,
    )
}

internal fun Settings.buildSuggestionGenerationParams(model: Model): TextGenerationParams {
    return TextGenerationParams(
        model = model,
        temperature = 1.0f,
        thinkingBudget = suggestionThinkingBudget,
    )
}

internal fun Settings.buildSummarizerGenerationParams(
    model: Model,
    temperature: Float
): TextGenerationParams {
    return TextGenerationParams(
        model = model,
        temperature = temperature,
        thinkingBudget = summarizerThinkingBudget,
    )
}

internal fun Settings.buildSubagentGenerationParams(
    model: Model,
    temperature: Float
): TextGenerationParams {
    return TextGenerationParams(
        model = model,
        temperature = temperature,
        thinkingBudget = subagentThinkingBudget,
    )
}

internal fun Settings.buildOcrGenerationParams(model: Model): TextGenerationParams {
    return TextGenerationParams(
        model = model,
        thinkingBudget = ocrThinkingBudget,
    )
}
