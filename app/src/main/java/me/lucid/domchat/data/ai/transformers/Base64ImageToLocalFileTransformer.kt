package me.lucid.domchat.data.ai.transformers

import me.lucid.ai.ui.UIMessage
import me.lucid.domchat.utils.convertBase64ImagePartToLocalFile

object Base64ImageToLocalFileTransformer : OutputMessageTransformer {
    override suspend fun onGenerationFinish(
        ctx: TransformerContext,
        messages: List<UIMessage>,
    ): List<UIMessage> {
        return messages.map { message ->
            ctx.context.convertBase64ImagePartToLocalFile(message)
        }
    }
}
