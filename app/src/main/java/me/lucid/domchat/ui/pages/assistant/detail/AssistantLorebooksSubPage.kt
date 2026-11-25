package me.lucid.domchat.ui.pages.assistant.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.R
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.components.ui.EmptyStateCard
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.appFieldColors

/**
 * Lorebooks sub-page - List all user's lorebooks with search, AMOLED black glassy cards, and toggles.
 */
@Composable
fun AssistantLorebooksSubPage(
    assistant: Assistant,
    onUpdate: (Assistant) -> Unit,
    vm: AssistantDetailVM
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val lorebooks = settings.lorebooks
    val navController = LocalNavController.current
    val haptics = rememberPremiumHaptics()

    var searchQuery by remember { mutableStateOf("") }

    val filteredLorebooks = remember(lorebooks, searchQuery) {
        if (searchQuery.isBlank()) {
            lorebooks
        } else {
            lorebooks.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ═══════════════════════════════════════════════════════════════════
        // SEARCH BAR AT TOP WITH PROPER SPACING
        // ═══════════════════════════════════════════════════════════════════
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search lorebooks...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = AppShapes.InputField,
            colors = appFieldColors()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attached Lorebooks (${assistant.enabledLorebookIds.size}/${lorebooks.size})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            DomChatGlassPillButton(
                text = "+ Create",
                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    navController.navigate(me.lucid.domchat.Screen.SettingLorebooks)
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════════
        // LOREBOOKS LIST (Black background glassy card design with toggles)
        // ═══════════════════════════════════════════════════════════════════
        if (lorebooks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EmptyStateCard(
                    icon = Icons.Rounded.Book,
                    title = stringResource(R.string.assistant_lorebooks_empty),
                    description = stringResource(R.string.assistant_lorebooks_empty_hint),
                    modifier = Modifier.fillMaxWidth()
                )
                DomChatGlassPillButton(
                    text = "Create Lorebook",
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        navController.navigate(me.lucid.domchat.Screen.SettingLorebooks)
                    }
                )
            }
        } else if (filteredLorebooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No lorebooks match '$searchQuery'",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLorebooks, key = { it.id }) { lorebook ->
                    val isEnabled = assistant.enabledLorebookIds.contains(lorebook.id)

                    Surface(
                        onClick = {
                            haptics.perform(HapticPattern.Tick)
                            val newIds = if (isEnabled) {
                                assistant.enabledLorebookIds - lorebook.id
                            } else {
                                assistant.enabledLorebookIds + lorebook.id
                            }
                            onUpdate(assistant.copy(enabledLorebookIds = newIds))
                        },
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            0.5.dp,
                            if (isEnabled) LiquidGlassTokens.SpecularBorderBrush
                            else LiquidGlassTokens.SpecularBorderSubtle
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoStories,
                                    contentDescription = null,
                                    tint = if (isEnabled) Color.White else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lorebook.name.ifEmpty { stringResource(R.string.lorebooks_page_unnamed) },
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${lorebook.entries.size} entries",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }

                            HapticSwitch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    val newIds = if (checked) {
                                        assistant.enabledLorebookIds + lorebook.id
                                    } else {
                                        assistant.enabledLorebookIds - lorebook.id
                                    }
                                    onUpdate(assistant.copy(enabledLorebookIds = newIds))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
