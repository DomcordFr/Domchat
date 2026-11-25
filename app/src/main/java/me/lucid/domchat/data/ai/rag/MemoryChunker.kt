package me.lucid.domchat.data.ai.rag

import me.lucid.ai.memory.PortableMemoryChunker

object MemoryChunker {
    const val MAX_CHUNK_SIZE = PortableMemoryChunker.MAX_CHUNK_SIZE
    const val MIN_CHUNK_SIZE = PortableMemoryChunker.MIN_CHUNK_SIZE

    fun chunkText(text: String): List<String> {
        return PortableMemoryChunker.chunkText(text)
    }
}
