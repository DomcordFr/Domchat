package me.lucid.domchat.data.model

import kotlinx.serialization.Serializable

/**
 * Context Window and Token Allocation Budget settings.
 * Allows users to set ideal context size (e.g. 128k+) and fine-tune
 * allocation sliders across system prompt, chat history, lorebook, and response tokens.
 */
@Serializable
data class ContextBudgetConfig(
    val maxContextTokens: Int = 131072, // 128k default for modern high-context LLMs
    val maxResponseTokens: Int = 4096, // Dedicated output token reservation
    val characterSystemBudgetPercent: Int = 25, // % allocated to system prompt, character definition, persona
    val chatHistoryBudgetPercent: Int = 60, // % allocated to recent chat message history
    val memoryLorebookBudgetPercent: Int = 15, // % allocated to lorebooks, memories, and RAG
    val autoPruneOverflow: Boolean = true, // Whether to dynamically prune oldest history turns on overflow
    val enableCompaction: Boolean = false, // Whether to summarize older turns into a context summary when approaching the threshold
    val compactionThresholdPercent: Int = 80, // % of context window at which compaction triggers
) {
    /**
     * Effective tokens available for input context (total context minus response reservation)
     */
    val availableInputTokens: Int
        get() = (maxContextTokens - maxResponseTokens).coerceAtLeast(1024)

    /**
     * Absolute token budgets calculated from percentage allocation
     */
    val characterSystemTokenBudget: Int
        get() = ((availableInputTokens * characterSystemBudgetPercent) / 100).coerceAtLeast(512)

    val chatHistoryTokenBudget: Int
        get() = ((availableInputTokens * chatHistoryBudgetPercent) / 100).coerceAtLeast(1024)

    val memoryLorebookTokenBudget: Int
        get() = ((availableInputTokens * memoryLorebookBudgetPercent) / 100).coerceAtLeast(256)
}
