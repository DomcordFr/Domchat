package me.lucid.domchat.data.ai.rag

import me.lucid.ai.memory.MemoryVectorMath

object VectorEngine {
    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        return MemoryVectorMath.cosineSimilarity(v1, v2)
    }
}
