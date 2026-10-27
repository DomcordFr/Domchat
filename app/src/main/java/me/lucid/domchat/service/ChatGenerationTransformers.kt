package me.lucid.domchat.service

import me.lucid.domchat.data.ai.transformers.Base64ImageToLocalFileTransformer
import me.lucid.domchat.data.ai.transformers.DocumentAsPromptTransformer
import me.lucid.domchat.data.ai.transformers.InputMessageTransformer
import me.lucid.domchat.data.ai.transformers.OcrTransformer
import me.lucid.domchat.data.ai.transformers.OutputMessageTransformer
import me.lucid.domchat.data.ai.transformers.PlaceholderTransformer
import me.lucid.domchat.data.ai.transformers.RegexOutputTransformer
import me.lucid.domchat.data.ai.transformers.ThinkTagTransformer
import me.lucid.domchat.data.ai.transformers.UnsupportedFileTransformer

internal val defaultChatInputTransformers: List<InputMessageTransformer> = listOf(
    PlaceholderTransformer,
    DocumentAsPromptTransformer,
    OcrTransformer,
    UnsupportedFileTransformer,
)

internal val defaultChatOutputTransformers: List<OutputMessageTransformer> = listOf(
    ThinkTagTransformer,
    Base64ImageToLocalFileTransformer,
    RegexOutputTransformer,
)
