package me.lucid.domchat.di

import me.lucid.domchat.ui.activity.TextSelectionVM
import me.lucid.domchat.ui.pages.assistant.AssistantVM
import me.lucid.domchat.ui.pages.assistant.detail.AssistantDetailVM
import me.lucid.domchat.ui.pages.backup.BackupVM
import me.lucid.domchat.ui.pages.chat.ChatVM
import me.lucid.domchat.ui.pages.developer.DeveloperVM
import me.lucid.domchat.ui.pages.imggen.ImgGenVM
import me.lucid.domchat.ui.pages.setting.SettingVM

import me.lucid.domchat.ui.pages.share.handler.ShareHandlerVM
import me.lucid.domchat.ui.pages.menu.MenuVM
import me.lucid.domchat.ui.pages.onboarding.OnboardingVM
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModel<ChatVM> { params ->
        ChatVM(
            id = params[0],
            context = get(),
            settingsStore = get(),
            conversationRepo = get(),
            chatAttachmentRepository = get(),
            memoryRepository = get(),
            chatService = get(),
            updateChecker = get(),
            appScope = get(),
            appStorageRepository = get(),
            modelMetadataResolver = get(),
        )
    }
    viewModel<SettingVM> {
        SettingVM(
            settingsStore = get(),
            mcpManager = get(),
            context = get(),
            platformHttpClient = get(),
            appStorageRepository = get(),
            modelCatalogService = get(),
            modelMetadataResolver = get(),
            memoryRepository = get(),
            localModelStore = get(),
            localModelInstall = get(),
            liteRtRuntime = get(),
            liteRtEmbedder = get(),
            sherpaModelStore = get(),
            sherpaModelInstall = get(),
            sherpaSttRuntime = get(),
        )
    }

    viewModelOf(::AssistantVM)
    viewModel<AssistantDetailVM> {
        AssistantDetailVM(
            id = it[0],
            settingsStore = get(),
            memoryRepository = get(),
            conversationRepository = get(),
            chatEpisodeDAO = get(),
            providerManager = get(),
            appStorageRepository = get(),
            embeddingService = get(),
        )
    }
    viewModel<ShareHandlerVM> {
        ShareHandlerVM(
            text = it[0],
            settingsStore = get(),
        )
    }
    viewModelOf(::BackupVM)
    viewModelOf(::ImgGenVM)
    viewModelOf(::DeveloperVM)
    viewModelOf(::MenuVM)
    viewModel<OnboardingVM> {
        OnboardingVM(
            settingsStore = get(),
            providerManager = get(),
            modelCatalogService = get(),
            modelMetadataResolver = get(),
        )
    }
    viewModel<TextSelectionVM> {
        TextSelectionVM(
            settingsStore = get(),
            generationHandler = get(),
            memoryRepository = get(),
            templateTransformer = get(),
        )
    }
    viewModel<me.lucid.domchat.ui.activity.AssistantOverlayVM> {
        me.lucid.domchat.ui.activity.AssistantOverlayVM(
            settingsStore = get(),
            chatService = get(),
        )
    }
    viewModel {
        me.lucid.domchat.ui.pages.setting.locallm.SettingLocalLlmViewModel(
            context = get(),
            store = get(),
            catalog = get(),
            downloadManager = get(),
            runtime = get(),
            install = get(),
            embedder = get(),
            inferenceManager = get(),
            settingsStore = get(),
            modelCatalogService = get(),
            secretKeyManager = get(),
        )
    }
    viewModel {
        me.lucid.domchat.ui.pages.setting.localstt.SettingLocalSttViewModel(
            store = get(),
            catalog = get(),
            downloadManager = get(),
            install = get(),
            runtime = get(),
            settingsStore = get(),
        )
    }
    viewModel { me.lucid.domchat.ui.pages.extensions.workspace.WorkspaceVM(get()) }
    viewModel<me.lucid.domchat.ui.pages.extensions.workspace.WorkspaceDetailVM> { params ->
        me.lucid.domchat.ui.pages.extensions.workspace.WorkspaceDetailVM(
            id = params[0],
            repository = get(),
            environmentManager = get(),
        )
    }
}

