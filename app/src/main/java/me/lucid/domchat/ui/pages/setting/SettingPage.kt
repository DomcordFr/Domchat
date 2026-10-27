package me.lucid.domchat.ui.pages.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Warning
import me.lucid.domchat.R
import me.lucid.domchat.Screen
import me.lucid.domchat.data.datastore.isNotConfigured
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.pages.setting.components.SettingGroupItem
import me.lucid.domchat.ui.pages.setting.components.SettingsGroup
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.utils.openUrl
import org.koin.androidx.compose.koinViewModel

private data class SettingNavItem(
    val title: String,
    val subtitle: String,
    val category: String,
    val icon: ImageVector,
    val target: Screen,
    val keywords: List<String> = emptyList()
)

@Composable
fun SettingPage(
    vm: SettingVM = koinViewModel(),
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = LocalNavController.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val haptics = rememberPremiumHaptics()
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val mainItemPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)

    val allSettings = remember {
        listOf(
            // Roleplay & Characters
            SettingNavItem(
                title = "Personas",
                subtitle = "Manage user identities, avatars, pronouns and descriptions",
                category = "Roleplay & Characters",
                icon = Icons.Rounded.Person,
                target = Screen.SettingPersonas,
                keywords = listOf("user", "persona", "profile", "identity", "avatar", "pronouns")
            ),
            SettingNavItem(
                title = "Character Hub",
                subtitle = "Browse, import, and manage roleplay character cards",
                category = "Roleplay & Characters",
                icon = Icons.Rounded.Group,
                target = Screen.CharacterHub,
                keywords = listOf("bot", "character", "card", "tavern", "janitor", "ai")
            ),
            SettingNavItem(
                title = "Lorebooks & World Info",
                subtitle = "Dynamic memory triggers, world entries, and keywords",
                category = "Roleplay & Characters",
                icon = Icons.Rounded.AutoStories,
                target = Screen.SettingLorebooks,
                keywords = listOf("lorebook", "world", "info", "memory", "keywords", "triggers")
            ),
            SettingNavItem(
                title = "Skills & Autonomous Tools",
                subtitle = "Autonomous capabilities, web retrieval, and agent extensions",
                category = "Roleplay & Characters",
                icon = Icons.Rounded.Category,
                target = Screen.SettingSkills(),
                keywords = listOf("skills", "tools", "automation", "capabilities", "autonomous", "extensions")
            ),
            SettingNavItem(
                title = "Text Styling & Dialogue Rules",
                subtitle = "Colors for speech, actions, thoughts, and narration",
                category = "Roleplay & Characters",
                icon = Icons.Rounded.Palette,
                target = Screen.SettingRpOptimizations,
                keywords = listOf("text", "style", "color", "dialogue", "thought", "action", "roleplay", "rp", "markdown")
            ),
            SettingNavItem(
                title = "Prompt Injections & Presets",
                subtitle = "System instructions, formatting locks, and presets",
                category = "Roleplay & Characters",
                icon = Icons.Rounded.Category,
                target = Screen.SettingPresets,
                keywords = listOf("prompt", "injection", "preset", "jailbreak", "system", "instructions")
            ),

            // Visuals & Atmosphere
            SettingNavItem(
                title = "Display, Themes & Glass",
                subtitle = "OLED Liquid Glass feel, theme palette, and global wallpapers",
                category = "Visuals & Atmosphere",
                icon = Icons.Rounded.Tune,
                target = Screen.SettingDisplay,
                keywords = listOf("theme", "glass", "liquid", "wallpaper", "oled", "dark", "color", "blur")
            ),
            SettingNavItem(
                title = "Fonts & Typography",
                subtitle = "Custom header, dialogue, and code fonts",
                category = "Visuals & Atmosphere",
                icon = Icons.Rounded.Build,
                target = Screen.SettingFonts,
                keywords = listOf("font", "typography", "text", "size", "custom font", "ttf", "otf")
            ),
            SettingNavItem(
                title = "Chat Layout & Motion",
                subtitle = "Bubble styles, message jumpers, haptics, and fluid streaming",
                category = "Visuals & Atmosphere",
                icon = Icons.Rounded.Tune,
                target = Screen.SettingUICustomization,
                keywords = listOf("ui", "layout", "bubbles", "animation", "streaming", "fade", "haptics")
            ),

            // AI Models & Endpoints
            SettingNavItem(
                title = "Model Providers & API Keys",
                subtitle = "Custom endpoints, OpenAI-compatible URLs, NanoGPT, and keys",
                category = "AI Models & Endpoints",
                icon = Icons.Rounded.Cloud,
                target = Screen.SettingProvider,
                keywords = listOf("provider", "api", "key", "url", "endpoint", "nanogpt", "openrouter", "openai", "claude")
            ),
            SettingNavItem(
                title = "Default Models & Catalog",
                subtitle = "Select active chat, reasoning, and vision models",
                category = "AI Models & Endpoints",
                icon = Icons.Rounded.AccountTree,
                target = Screen.SettingModels,
                keywords = listOf("model", "chat model", "reasoning", "catalog", "vision")
            ),
            SettingNavItem(
                title = "Context & Token Budget",
                subtitle = "Manage context limits, system/history/lorebook budgets, and pruning",
                category = "AI Models & Endpoints",
                icon = Icons.Rounded.Tune,
                target = Screen.SettingContextBudget,
                keywords = listOf("token", "budget", "context", "window", "tokens", "allocation", "history", "prune")
            ),
            SettingNavItem(
                title = "MCP Tools & Extensions",
                subtitle = "External capabilities, plugins, and custom agent tools",
                category = "AI Models & Endpoints",
                icon = Icons.Rounded.Extension,
                target = Screen.SettingMcp,
                keywords = listOf("mcp", "tools", "plugins", "skills", "extensions")
            ),

            // Data & Storage
            SettingNavItem(
                title = "All Chats & History",
                subtitle = "Search, sort, bulk delete, and organize all conversations",
                category = "Data & Storage",
                icon = Icons.Rounded.Chat,
                target = Screen.SettingChats,
                keywords = listOf("chats", "conversations", "history", "manage", "delete", "search")
            ),
            SettingNavItem(
                title = "Backup & Restore",
                subtitle = "Export chats, characters, and WebDAV cloud sync",
                category = "Data & Storage",
                icon = Icons.Rounded.CloudUpload,
                target = Screen.Backup(),
                keywords = listOf("backup", "sync", "webdav", "restore", "export", "import")
            ),
            SettingNavItem(
                title = "Storage & Cache",
                subtitle = "Manage local database, image cache, and files",
                category = "Data & Storage",
                icon = Icons.Rounded.Storage,
                target = Screen.SettingChatStorage,
                keywords = listOf("storage", "cache", "clear", "images", "database", "disk")
            ),

            // System & About
            SettingNavItem(
                title = "Android Integration",
                subtitle = "Quick settings tiles, system share target, and shortcuts",
                category = "System & About",
                icon = Icons.Rounded.PhoneAndroid,
                target = Screen.SettingAndroidIntegration,
                keywords = listOf("android", "tile", "share", "shortcuts", "notification")
            ),
            SettingNavItem(
                title = "Local Web Server",
                subtitle = "Access chats and characters from your desktop browser",
                category = "System & About",
                icon = Icons.Rounded.Language,
                target = Screen.SettingWeb,
                keywords = listOf("web", "server", "browser", "desktop", "port", "lan")
            ),
            SettingNavItem(
                title = "About Domchat",
                subtitle = "Version, license, updates, and credits",
                category = "System & About",
                icon = Icons.Rounded.Info,
                target = Screen.SettingAbout,
                keywords = listOf("about", "version", "update", "github", "license")
            )
        )
    }

    val trimmedQuery = searchQuery.trim()
    val filteredSettings = remember(trimmedQuery) {
        if (trimmedQuery.isBlank()) {
            emptyList()
        } else {
            allSettings.filter { item ->
                item.title.contains(trimmedQuery, ignoreCase = true) ||
                item.subtitle.contains(trimmedQuery, ignoreCase = true) ||
                item.category.contains(trimmedQuery, ignoreCase = true) ||
                item.keywords.any { it.contains(trimmedQuery, ignoreCase = true) }
            }
        }
    }

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = stringResource(R.string.settings),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton()
                },
                actions = {
                    if (settings.developerMode) {
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.Developer)
                            }
                        ) {
                            Icon(Icons.Rounded.Build, stringResource(R.string.developer))
                        }
                    }
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Live Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search settings, models, personas...") },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = AppShapes.InputField,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (settings.isNotConfigured()) {
                item {
                    ProviderConfigWarningCard(navController)
                }
            }

            if (trimmedQuery.isNotBlank()) {
                // Search Results
                item {
                    SettingsGroup(
                        title = "Search Results (${filteredSettings.size})"
                    ) {
                        if (filteredSettings.isEmpty()) {
                            Text(
                                text = "No settings matched \"$trimmedQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(20.dp)
                            )
                        } else {
                            filteredSettings.forEach { item ->
                                SettingGroupItem(
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    icon = {
                                        Icon(
                                            item.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailing = {
                                        Icon(
                                            Icons.Rounded.ChevronRight,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    contentPadding = mainItemPadding,
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        navController.navigate(item.target)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Categorized Layout
                val categories = listOf(
                    "Roleplay & Characters",
                    "Visuals & Atmosphere",
                    "AI Models & Endpoints",
                    "Data & Storage",
                    "System & About"
                )

                categories.forEach { cat ->
                    val itemsInCat = allSettings.filter { it.category == cat }
                    item {
                        SettingsGroup(title = cat) {
                            itemsInCat.forEach { item ->
                                SettingGroupItem(
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    icon = {
                                        Icon(
                                            item.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailing = {
                                        Icon(
                                            Icons.Rounded.ChevronRight,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    contentPadding = mainItemPadding,
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        navController.navigate(item.target)
                                    }
                                )
                            }


                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderConfigWarningCard(navController: NavHostController) {
    Card(
        onClick = { navController.navigate(Screen.SettingProvider) },
        shape = AppShapes.CardLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.setting_page_config_api_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(R.string.setting_page_config_api_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}
