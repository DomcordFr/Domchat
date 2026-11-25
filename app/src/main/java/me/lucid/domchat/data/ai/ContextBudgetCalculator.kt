package me.lucid.domchat.data.ai

import me.lucid.ai.context.ContextCountConfidence
import me.lucid.ai.context.ContextTokenEstimator
import me.lucid.ai.context.ContextUsageBreakdown
import me.lucid.ai.context.calculateMinSafeFloorTokens
import me.lucid.ai.context.effectiveHistoryForContext
import me.lucid.ai.context.probableTemporaryTokenReserve
import me.lucid.ai.context.smartFitContext
import me.lucid.ai.context.smartInputBudget
import me.lucid.ai.core.MessageRole
import me.lucid.ai.provider.Model
import me.lucid.ai.provider.ModelAbility
import me.lucid.ai.provider.contextCapacityTokens
import me.lucid.ai.ui.UIMessage
import me.lucid.ai.ui.UIMessagePart
import me.lucid.domchat.data.ai.prompts.DEFAULT_LEARNING_MODE_PROMPT
import me.lucid.domchat.data.ai.tools.toDefinitionPreviews
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.AssistantMemory
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.data.model.LorebookActivationType
import me.lucid.domchat.data.model.ModeAttachment
import me.lucid.domchat.data.model.ModeAttachmentType
import me.lucid.domchat.service.ChatPersistenceMode

object ContextBudgetCalculator {
    fun calculate(
        model: Model?,
        conversation: Conversation,
        assistant: Assistant,
        settings: Settings,
        memoryCandidates: List<AssistantMemory>,
        persistenceMode: ChatPersistenceMode,
        pendingParts: List<UIMessagePart>,
        requestUsage: ContextUsageBreakdown?,
        isGenerating: Boolean = false,
    ): ContextUsageBreakdown? {
        val activeModel = model?.takeIf { (it.contextCapacityTokens ?: 0) > 0 } ?: return null
        val hasPendingInput = pendingParts.any { part ->
            part !is UIMessagePart.Text || part.text.isNotBlank()
        }

        // During active generation streaming, avoid recomputing 200k+ token breakdowns on the UI thread.
        // requestUsage contains the exact context breakdown computed at request start.
        if (isGenerating && !hasPendingInput && requestUsage != null) {
            return requestUsage
        }

        val smartActive = assistant.smartContextManagement && (activeModel.contextCapacityTokens ?: 0) > 0
        val rawMessages = conversation.currentMessages
        val messages = effectiveHistoryForContext(
            messages = rawMessages,
            smartManagement = smartActive,
            summaryUpToIndex = conversation.contextSummaryUpToIndex.takeIf {
                !conversation.contextSummary.isNullOrBlank()
            } ?: -1,
            truncateIndex = conversation.truncateIndex,
            manualHistoryLimit = assistant.maxHistoryMessages,
        )
        val memoryContextEnabled = assistant.enableMemory && persistenceMode == ChatPersistenceMode.NORMAL
        val eligibleMemoryCandidates = if (!memoryContextEnabled) {
            emptyList()
        } else if (!assistant.useRagMemoryRetrieval) {
            memoryCandidates.filter { memory -> memory.type == 0 }.take(50)
        } else {
            memoryCandidates.filter { memory ->
                (memory.type == 0 && assistant.ragIncludeCore) ||
                    (memory.type == 1 && assistant.ragIncludeEpisodes)
            }.take(1_000)
        }
        val memoryRevision = eligibleMemoryCandidates.map { memory ->
            listOf(memory.id, memory.content, memory.type, memory.timestamp)
        }.hashCode()

        val sourceKey = contextUsageSourceKey(conversation, assistant, activeModel, settings, memoryRevision)
        val matchingRequestUsage = requestUsage?.takeIf { usage -> usage.sourceKey == sourceKey }
        if (!hasPendingInput && matchingRequestUsage != null) return matchingRequestUsage

        val observedMemoryTokenTotals = rawMessages.asReversed().mapNotNull { message ->
            message.usedMemories.orEmpty()
                .sumOf { memory ->
                    memory.contextTokenCount
                        ?: ContextTokenEstimator.textTokens(memory.memoryContent, activeModel)
                }
                .takeIf { it > 0 }
        }.take(6)
        val availableSkills = settings.skills.filter { skill ->
            skill.enabled && skill.instructions.isNotBlank()
        }
        val allSkillIds = availableSkills.map { skill -> skill.id }.toSet()
        val assistantAvailableSkillIds = availableSkills
            .filter { skill -> skill.isAvailableForAssistant(assistant.id) }
            .map { skill -> skill.id }
            .toSet()
        val activeSkillIds = resolveActiveSkillIds(
            assistantDefaultSkillIds = assistant.enabledSkillIds.intersect(assistantAvailableSkillIds),
            conversationSkillIds = conversation.enabledModeIds,
            turnScopedSkillIds = emptySet(),
            allSkillIds = allSkillIds,
            alwaysEnabledSkillIds = availableSkills
                .filter { skill -> skill.alwaysEnabled && skill.id in assistantAvailableSkillIds }
                .map { skill -> skill.id }
                .toSet(),
        )
        val activeLorebookIds = conversation.enabledLorebookIds ?: assistant.enabledLorebookIds
        val activeLoreEntries = settings.lorebooks
            .filter { lorebook -> lorebook.enabled && lorebook.id in activeLorebookIds }
            .flatMap { lorebook -> lorebook.entries.filter { it.enabled } }

        val recentText = rawMessages.takeLast(10).joinToString(" ") { message -> message.toText() }
        val deterministicLoreEntries = activeLoreEntries.filter { entry ->
            when (entry.activationType) {
                LorebookActivationType.ALWAYS -> true
                LorebookActivationType.RAG -> false
                LorebookActivationType.KEYWORDS -> entry.keywords.any { keyword ->
                    if (entry.useRegex) {
                        runCatching {
                            val options = if (entry.caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
                            Regex(keyword, options).containsMatchIn(recentText)
                        }.getOrDefault(false)
                    } else if (entry.caseSensitive) {
                        recentText.contains(keyword)
                    } else {
                        recentText.contains(keyword, ignoreCase = true)
                    }
                }
            }
        }

        fun loreEntryTokenCost(entry: me.lucid.domchat.data.model.LorebookEntry): Int =
            ContextTokenEstimator.textTokens(entry.prompt, activeModel) +
                entry.attachments.sumOf { attachment ->
                    when (attachment.type) {
                        ModeAttachmentType.IMAGE -> 1_024
                        ModeAttachmentType.VIDEO -> 4_096
                        ModeAttachmentType.AUDIO -> 2_000
                        ModeAttachmentType.DOCUMENT -> 512
                    }
                }

        val loreEntryTokensById = activeLoreEntries.associate { entry ->
            entry.id.toString() to loreEntryTokenCost(entry)
        }

        val observedConditionalTokenTotals = rawMessages.asReversed().mapNotNull { message ->
            message.usedLorebookEntries.orEmpty()
                .filter { it.activationReason?.startsWith("RAG Match") == true }
                .sumOf { used ->
                    used.contextTokenCount ?: loreEntryTokensById[used.entryId] ?: 0
                }
                .takeIf { it > 0 }
        }.take(6)

        val conditionalContextCandidateTokens = activeLoreEntries
            .filter { it.activationType == LorebookActivationType.RAG }
            .sumOf { entry -> loreEntryTokensById[entry.id.toString()] ?: 0 }

        val eligibleMemoryCandidateTokens = eligibleMemoryCandidates.sumOf { memory ->
            ContextTokenEstimator.textTokens(memory.content, activeModel).toLong()
        }.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

        val probableTemporaryTokens = probableTemporaryTokenReserve(
            model = activeModel,
            requestedOutputTokens = assistant.maxTokens,
            memoryEnabled = memoryContextEnabled,
            memoryRecallIsConditional = assistant.useRagMemoryRetrieval,
            memoryCandidateLimit = if (assistant.ragLimit > 50) 1_000 else assistant.ragLimit,
            memoryCandidateTokens = eligibleMemoryCandidateTokens,
            memoryBudgetFraction = when (assistant.contextPriority) {
                me.lucid.domchat.data.model.ContextPriority.CHAT_HISTORY -> 0.20
                me.lucid.domchat.data.model.ContextPriority.BALANCED -> 0.40
                me.lucid.domchat.data.model.ContextPriority.MEMORIES -> 0.65
            },
            observedMemoryTokenTotals = observedMemoryTokenTotals,
            conditionalContextCandidateTokens = conditionalContextCandidateTokens,
            observedConditionalTokenTotals = observedConditionalTokenTotals,
        )

        val skillText = buildString {
            availableSkills
                .filter { skill -> skill.id in activeSkillIds }
                .forEach { skill ->
                    appendLine(skill.name)
                    appendLine(skill.instructions)
                }
        }
        val lorebookText = deterministicLoreEntries.joinToString("\n") { entry -> entry.prompt }
        val skillTokens = ContextTokenEstimator.textTokens(skillText, activeModel)
        val lorebookTokens = ContextTokenEstimator.textTokens(lorebookText, activeModel)

        fun ModeAttachment.toContextPart(): UIMessagePart = when (type) {
            ModeAttachmentType.IMAGE -> UIMessagePart.Image(url)
            ModeAttachmentType.VIDEO -> UIMessagePart.Video(url)
            ModeAttachmentType.AUDIO -> UIMessagePart.Audio(url)
            ModeAttachmentType.DOCUMENT -> UIMessagePart.Document(url, fileName, mime)
        }

        val knownContextAttachments = buildList {
            availableSkills
                .filter { skill -> skill.id in activeSkillIds }
                .flatMapTo(this) { skill -> skill.attachments.map { it.toContextPart() } }
            deterministicLoreEntries
                .flatMapTo(this) { entry -> entry.attachments.map { it.toContextPart() } }
        }
        val knownContextMediaTokens = knownContextAttachments.sumOf { part ->
            ContextTokenEstimator.partTokens(part, activeModel)
        }

        val toolAccounting = run {
            if (ModelAbility.TOOL !in activeModel.abilities) return@run "" to 0
            val activeMcpTools = settings.mcpServers
                .filter { server -> server.commonOptions.enable && server.id in assistant.mcpServers }
                .flatMap { server -> server.commonOptions.tools.filter { tool -> tool.enable } }
            val localPreviews = assistant.localTools.flatMap { it.toDefinitionPreviews() }
            val definitionText = buildString {
                localPreviews.forEach { tool ->
                    appendLine(
                        ContextTokenEstimator.toolDefinitionText(
                            name = tool.name,
                            description = tool.description,
                            schema = tool.schema,
                        )
                    )
                }
                activeMcpTools.forEach { tool ->
                    appendLine(
                        ContextTokenEstimator.toolDefinitionText(
                            name = tool.name,
                            description = tool.description.orEmpty(),
                            schema = tool.inputSchema,
                        )
                    )
                }
            }
            val localTokens = localPreviews.sumOf { tool ->
                ContextTokenEstimator.toolDefinitionTokens(
                    name = tool.name,
                    description = tool.description,
                    schema = tool.schema,
                    model = activeModel,
                ).toLong()
            }
            val mcpTokens = activeMcpTools.sumOf { tool ->
                ContextTokenEstimator.toolDefinitionTokens(
                    name = tool.name,
                    description = tool.description.orEmpty(),
                    schema = tool.inputSchema,
                    model = activeModel,
                ).toLong()
            }
            definitionText to (localTokens + mcpTokens)
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt()
        }

        val toolDefinitionText = toolAccounting.first
        val toolDefinitionTokens = matchingRequestUsage?.toolDefinitionTokens ?: toolAccounting.second

        val systemPromptText = buildString {
            append(assistant.systemPrompt)
            if (assistant.learningMode) {
                appendLine()
                append(settings.learningModePrompt.ifEmpty { DEFAULT_LEARNING_MODE_PROMPT })
            }
            buildTimeAwarenessBlock(
                enabled = assistant.enableTimeAwareness,
                fullMessages = rawMessages,
                retainedMessages = messages,
            )?.let { block ->
                appendLine()
                append(block)
            }
        }

        val baseSmartInputBudget = if (smartActive) {
            smartInputBudget(activeModel, assistant.maxTokens)
        } else {
            null
        }

        val deterministicContextTokens = ContextTokenEstimator.textTokens(systemPromptText, activeModel) +
            ContextTokenEstimator.textTokens(conversation.contextSummary.orEmpty(), activeModel) +
            skillTokens + lorebookTokens + toolDefinitionTokens + knownContextMediaTokens

        val fixedMemorySelection = if (
            !smartActive || assistant.useRagMemoryRetrieval ||
            eligibleMemoryCandidates.isEmpty() || baseSmartInputBudget == null
        ) {
            null
        } else {
            selectSmartMemoryContext(
                candidates = eligibleMemoryCandidates,
                model = activeModel,
                inputBudgetTokens = baseSmartInputBudget,
                requiredContextTokens = deterministicContextTokens,
                historyMessages = messages,
                contextPriority = assistant.contextPriority,
                episodeGroup = { "Older" },
            )
        }

        val fixedMemoryText = fixedMemorySelection?.promptText.orEmpty()
        val fixedMemoryTokens = fixedMemorySelection?.promptTokens ?: 0
        val effectiveSmartInputBudget = baseSmartInputBudget?.let { budget ->
            (budget - probableTemporaryTokens).coerceAtLeast(1)
        }

        val messagesToCount = if (smartActive) {
            val namedTokens = ContextTokenEstimator.textTokens(systemPromptText, activeModel) +
                ContextTokenEstimator.textTokens(conversation.contextSummary.orEmpty(), activeModel) +
                fixedMemoryTokens + skillTokens + lorebookTokens +
                toolDefinitionTokens +
                knownContextMediaTokens
            val messageBudget = ((effectiveSmartInputBudget ?: Int.MAX_VALUE) - namedTokens)
                .coerceAtLeast(1)
            smartFitContext(
                messages = messages,
                model = activeModel,
                messageBudgetTokens = messageBudget,
            )
        } else {
            messages
        }

        val baseBreakdown = ContextTokenEstimator.breakdown(
            messages = messagesToCount,
            model = activeModel,
            systemPromptText = systemPromptText,
            summaryText = conversation.contextSummary.orEmpty(),
            memoryText = fixedMemoryText,
            memoryTokensOverride = fixedMemoryTokens,
            skillText = skillText,
            lorebookText = lorebookText,
            skillTokensOverride = skillTokens,
            lorebookTokensOverride = lorebookTokens,
            toolDefinitionText = toolDefinitionText,
            toolDefinitionTokensOverride = toolDefinitionTokens,
            pendingParts = knownContextAttachments,
            usableInputTokens = effectiveSmartInputBudget,
            sourceKey = sourceKey,
        )

        if (!hasPendingInput) {
            return baseBreakdown
        }

        var pendingTextTokens = 0
        var pendingMediaTokens = 0
        var pendingImages = 0
        pendingParts.forEach { part ->
            when (part) {
                is UIMessagePart.Image -> {
                    pendingMediaTokens += ContextTokenEstimator.partTokens(part, activeModel)
                    pendingImages++
                }
                is UIMessagePart.Video, is UIMessagePart.Audio, is UIMessagePart.Document -> {
                    pendingMediaTokens += ContextTokenEstimator.partTokens(part, activeModel)
                }
                else -> {
                    pendingTextTokens += ContextTokenEstimator.partTokens(part, activeModel)
                }
            }
        }
        val framingDelta = if (messages.isEmpty()) 7 else 4
        val estimatedTotalTokens = baseBreakdown.usedTokens + pendingTextTokens + pendingMediaTokens + framingDelta
        val budgetLimit = effectiveSmartInputBudget ?: Int.MAX_VALUE

        return if (smartActive && estimatedTotalTokens > budgetLimit) {
            val pendingMessage = UIMessage(role = MessageRole.USER, parts = pendingParts)
            val namedTokens = ContextTokenEstimator.textTokens(systemPromptText, activeModel) +
                ContextTokenEstimator.textTokens(conversation.contextSummary.orEmpty(), activeModel) +
                fixedMemoryTokens + skillTokens + lorebookTokens +
                toolDefinitionTokens +
                knownContextMediaTokens
            val messageBudget = (budgetLimit - namedTokens).coerceAtLeast(1)
            val smartMessages = smartFitContext(
                messages = messages + listOf(pendingMessage),
                model = activeModel,
                messageBudgetTokens = messageBudget,
            )
            ContextTokenEstimator.breakdown(
                messages = smartMessages,
                model = activeModel,
                systemPromptText = systemPromptText,
                summaryText = conversation.contextSummary.orEmpty(),
                memoryText = fixedMemoryText,
                memoryTokensOverride = fixedMemoryTokens,
                skillText = skillText,
                lorebookText = lorebookText,
                skillTokensOverride = skillTokens,
                lorebookTokensOverride = lorebookTokens,
                toolDefinitionText = toolDefinitionText,
                toolDefinitionTokensOverride = toolDefinitionTokens,
                pendingParts = knownContextAttachments,
                usableInputTokens = effectiveSmartInputBudget,
                sourceKey = sourceKey,
            )
        } else {
            baseBreakdown.copy(
                conversationTokens = baseBreakdown.conversationTokens + pendingTextTokens + framingDelta,
                mediaTokens = baseBreakdown.mediaTokens + pendingMediaTokens,
                imageCount = baseBreakdown.imageCount + pendingImages,
                usedTokens = estimatedTotalTokens,
            )
        }
    }
}
