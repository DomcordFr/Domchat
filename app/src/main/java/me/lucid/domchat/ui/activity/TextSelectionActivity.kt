package me.lucid.domchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.highlight.Highlighter
import me.lucid.highlight.LocalHighlighter
import me.lucid.domchat.RouteActivity
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.ui.components.textselection.TextSelectionSheet
import me.lucid.domchat.ui.components.ui.AppToasterHost
import me.lucid.domchat.ui.components.ui.rememberAppToasterState
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.theme.DomchatTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Floating activity that handles Android text selection toolbar integration.
 * Appears as an overlay when user selects text and taps "Ask DomChat".
 */
class TextSelectionActivity : ComponentActivity() {
    private val highlighter by inject<Highlighter>()
    private val settingsStore by inject<SettingsStore>()
    private val viewModel: TextSelectionVM by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val selectedText = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
        if (selectedText.isBlank()) {
            finish()
            return
        }

        viewModel.updateInput(QuickAskInputData(text = selectedText))

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
                            val routeIntent = Intent(this@TextSelectionActivity, RouteActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
