package me.lucid.domchat.di

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.serialization.json.Json
import me.lucid.highlight.Highlighter
import me.lucid.highlight.android.AndroidHighlighter
import me.lucid.domchat.AppScope
import me.lucid.domchat.data.ai.AILoggingManager
import me.lucid.domchat.data.ai.tools.LocalTools
import me.lucid.domchat.service.ChatService
import me.lucid.domchat.utils.EmojiData
import me.lucid.domchat.utils.EmojiUtils
import me.lucid.domchat.utils.JsonInstant
import me.lucid.domchat.utils.UpdateChecker
import me.lucid.domchat.web.WebServerManager
import me.lucid.tts.provider.android.TTSManager
import org.koin.dsl.module

val appModule = module {
    single<Json> { JsonInstant }

    single<Highlighter> {
        AndroidHighlighter(get())
    }

    single {
        LocalTools(get(), get(), get(), get(), get())
    }

    single {
        UpdateChecker(get())
    }

    single {
        AppScope()
    }

    single<EmojiData> {
        EmojiUtils.loadEmoji(get())
    }

    single {
        TTSManager(get())
    }

    single {
        Firebase.crashlytics
    }

    single {
        Firebase.remoteConfig
    }

    single {
        AILoggingManager()
    }

    single {
        ChatService(
            context = get(),
            appScope = get(),
            settingsStore = get(),
            conversationRepo = get(),
            chatAttachmentRepository = get(),
            memoryRepository = get(),
            generationHandler = get(),
            templateTransformer = get(),
            providerManager = get(),
            localTools = get(),
            workspaceRepository = get(),
            mcpManager = get(),
            platformHttpClient = get(),
        )
    }

    single {
        WebServerManager(
            context = get(),
            appScope = get(),
            chatService = get(),
            conversationRepo = get(),
            settingsStore = get(),
        )
    }
}
