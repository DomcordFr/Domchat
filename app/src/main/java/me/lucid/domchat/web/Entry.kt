package me.lucid.domchat.web

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.defaultheaders.DefaultHeaders

fun startWebServer(
    port: Int,
    host: String = "127.0.0.1",
    configure: Application.() -> Unit,
): EmbeddedServer<*, *> {
    return embeddedServer(
        factory = CIO,
        port = port,
        host = host,
    ) {
        install(DefaultHeaders)
        install(Compression)
        configure()
    }
}
