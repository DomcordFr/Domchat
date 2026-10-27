package me.lucid.domchat.data.ai.mcp

import io.modelcontextprotocol.kotlin.sdk.shared.AbstractTransport

fun interface McpTransportFactory {
    fun create(config: McpServerConfig): AbstractTransport
}
