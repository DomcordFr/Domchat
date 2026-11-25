package me.lucid.domchat.ui.pages.assistant.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.lucid.domchat.R
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.ui.pages.setting.components.SettingsGroup
import me.lucid.domchat.ui.theme.LiquidGlassTokens

/**
 * Core Roleplay sub-page shell.
 *
 * Content previously in this unified page has been separated into dedicated modular sub-pages:
 * - Profile (avatar, display name, chat name, creator notes, tags) → AssistantProfileSubPage
 * - Personality (system prompt) → AssistantPersonalitySubPage
 * - Scenario (author's note) → AssistantScenarioSubPage
 * - Greetings (initial message + alternate greetings) → AssistantGreetingsSubPage
 * - Example Dialogues → AssistantExampleDialoguesSubPage
 * - Lorebooks → AssistantLorebooksSubPage
 *
 * This shell is retained for route compatibility but contains no duplicated content.
 */
@Composable
fun AssistantCoreRoleplaySubPage(
    assistant: Assistant,
    tags: List<me.lucid.domchat.data.model.Tag>,
    providers: List<me.lucid.ai.provider.ProviderSetting>,
    settings: me.lucid.domchat.data.datastore.Settings,
    onUpdate: (Assistant) -> Unit,
    vm: me.lucid.domchat.ui.pages.assistant.detail.AssistantDetailVM,
    catalogSnapshot: me.lucid.domchat.data.ai.models.ModelCatalogSnapshot? = null,
    onNavigateToLorebooks: (() -> Unit)? = null,
    onNavigateToSummarizerSettings: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsGroup(title = stringResource(R.string.assistant_detail_section_core_roleplay)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Core roleplay content has been separated into dedicated sub-pages:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Profile • Personality • Scenario • Greetings • Example Dialogues • Lorebooks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
