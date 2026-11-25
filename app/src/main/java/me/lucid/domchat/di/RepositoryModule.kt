package me.lucid.domchat.di

import android.content.Context
import me.lucid.domchat.data.ai.rag.EmbeddingService
import me.lucid.domchat.data.ai.MemorySearchService
import me.lucid.domchat.data.repository.AppStorageRepository
import me.lucid.domchat.data.repository.ChatAttachmentRepository
import me.lucid.domchat.data.repository.ConversationRepository
import me.lucid.domchat.data.repository.GenMediaRepository
import me.lucid.domchat.data.repository.MemoryRepository
import me.lucid.workspace.ProotShellRunner
import me.lucid.workspace.RootfsInstaller
import me.lucid.workspace.WorkspaceBindMount
import me.lucid.workspace.WorkspaceManager
import org.koin.dsl.module
import java.io.File

val repositoryModule = module {
    single {
        ChatAttachmentRepository(
            context = get(),
            chatAttachmentDao = get(),
            conversationAttachmentRefDao = get(),
            conversationDao = get(),
            settingsStore = get(),
            appScope = get(),
        )
    }

    single {
        AppStorageRepository(
            context = get(),
            settingsStore = get(),
            chatAttachmentRepository = get(),
            appScope = get(),
        )
    }

    single {
        ConversationRepository(get(), get(), get(), get(), get(), get())
    }

    single {
        EmbeddingService(get(), get())
    }

    single {
        MemoryRepository(get(), get(), get(), get(), get())
    }

    single {
        MemorySearchService(get(), get(), get(), get())
    }

    single {
        GenMediaRepository(get())
    }

    single {
        val context: Context = get()
        WorkspaceManager(
            baseDir = File(context.filesDir, "workspaces"),
            shellRunner = ProotShellRunner(
                nativeLibraryDir = File(context.applicationInfo.nativeLibraryDir),
                extraBindMounts = listOf(
                    WorkspaceBindMount(
                        source = File(context.filesDir, "skills").apply { mkdirs() },
                        target = "/skills",
                    ),
                    WorkspaceBindMount(
                        source = File(context.filesDir, "tool_outputs").apply { mkdirs() },
                        target = "/tool_outputs",
                    ),
                ),
            ),
        )
    }

    single {
        RootfsInstaller(get())
    }

    single {
        me.lucid.domchat.data.repository.WorkspaceRepository(
            dao = get(),
            settingsStore = get(),
            manager = get(),
            rootfsInstaller = get(),
        )
    }

    single {
        me.lucid.domchat.service.workspace.WorkspaceEnvironmentManager(
            context = get(),
            workspaceRepository = get(),
        )
    }
}
