package me.lucid.domchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.lucid.highlight.Highlighter
import me.lucid.highlight.LocalHighlighter
import me.lucid.common.platform.PlatformHttpClient
import me.lucid.domchat.RouteActivity
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.share.resolveSharePayload
import me.lucid.domchat.share.toRawSharePayload
import me.lucid.domchat.ui.components.textselection.TextSelectionSheet
import me.lucid.domchat.ui.components.ui.AppToasterHost
import me.lucid.domchat.ui.components.ui.rememberAppToasterState
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.theme.DomchatTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

typealias AskAetherChatShareActivity = AskDomChatShareActivity

class AskDomChatShareActivity : ComponentActivity() {
    private val highlighter by inject<Highlighter>()
    private val settingsStore by inject<SettingsStore>()
    private val httpClient by inject<PlatformHttpClient>()
    private val viewModel: TextSelectionVM by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val payload = resolveSharePayload(
                context = this@AskDomChatShareActivity,
                settingsStore = settingsStore,
                httpClient = httpClient,
                rawSharePayload = intent.toRawSharePayload()
            )
            if (!payload.hasContent()) {
                finish()
                return@launch
            }

            viewModel.updateInput(
                QuickAskInputData(
                    text = payload.text,
                    attachments = payload.attachments.map { attachment ->
                        QuickAskAttachment(
                            uri = attachment.uri,
                            fileName = attachment.fileName,
                            mimeType = attachment.mimeType
                        )
                    }
                )
            )

            setContent {
                val settings by settingsStore.settingsFlow.collectAsStateWithLifecycle()
                val toastState = rememberAppToasterState()

                DomchatTheme {
                    CompositionLocalProvider(
                        LocalSettings provides settings,
                        LocalHighlighter provides highlighter,
                        LocalToaster provides toastState,
                    ) {
                        TextSelectionSheet(
                            viewModel = viewModel,
                            onDismiss = { finish() },
                            onContinueInApp = {
                                val continuationData = viewModel.buildContinuationData()
                                val routeIntent =
                                    Intent(this@AskDomChatShareActivity, RouteActivity::class.java).apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        putQuickAskContinuationData(continuationData)
                                    }
                                startActivity(routeIntent)
                                finish()
                            }
                        )
                        AppToasterHost(state = toastState)
                    }
                }
            }
        }
    }
}
