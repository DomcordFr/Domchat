package me.lucid.domchat.ui.pages.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import kotlin.math.roundToInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.R
import me.lucid.domchat.data.datastore.DisplaySetting
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.pages.setting.components.SettingsGroup
import me.lucid.domchat.ui.pages.setting.components.SettingGroupItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingUICustomizationPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var displaySetting by remember(settings) { mutableStateOf(settings.displaySetting) }

    fun updateDisplaySetting(setting: DisplaySetting) {
        displaySetting = setting
        vm.updateSettings(
            settings.copy(
                displaySetting = setting
            )
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = stringResource(R.string.setting_ui_customization_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton()
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // New Chat Settings
            item {
                SettingsGroup(
                    title = stringResource(R.string.setting_new_chat_title)
                ) {
                    SettingGroupItem(
                        title = stringResource(R.string.setting_new_chat_header),
                        subtitle = stringResource(R.string.setting_new_chat_header_desc),
                        trailing = {
                            me.lucid.domchat.ui.components.ui.Select(
                                options = me.lucid.domchat.data.datastore.NewChatHeaderStyle.entries.toList(),
                                selectedOption = displaySetting.newChatHeaderStyle,
                                onOptionSelected = { updateDisplaySetting(displaySetting.copy(newChatHeaderStyle = it)) },
                                optionToString = { style ->
                                    when (style) {
                                        me.lucid.domchat.data.datastore.NewChatHeaderStyle.NONE -> stringResource(R.string.setting_new_chat_header_none)
                                        me.lucid.domchat.data.datastore.NewChatHeaderStyle.GREETING -> stringResource(R.string.setting_new_chat_header_greeting)
                                        me.lucid.domchat.data.datastore.NewChatHeaderStyle.BIG_ICON -> stringResource(R.string.setting_new_chat_header_big_icon)
                                    }
                                },
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    )
                    
                    // Only show avatar toggle if header style is not NONE
                    if (displaySetting.newChatHeaderStyle != me.lucid.domchat.data.datastore.NewChatHeaderStyle.NONE) {
                        SettingGroupItem(
                            title = stringResource(R.string.setting_ui_show_avatar_header_title),
                            subtitle = stringResource(R.string.setting_ui_show_avatar_header_global_desc),
                            trailing = {
                                HapticSwitch(
                                    checked = displaySetting.newChatShowAvatar,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(newChatShowAvatar = it))
                                    }
                                )
                            }
                        )
                    }
                    
                    SettingGroupItem(
                        title = stringResource(R.string.setting_new_chat_content),
                        subtitle = stringResource(R.string.setting_new_chat_content_desc),
                        trailing = {
                            me.lucid.domchat.ui.components.ui.Select(
                                options = me.lucid.domchat.data.datastore.NewChatContentStyle.entries.toList(),
                                selectedOption = displaySetting.newChatContentStyle,
                                onOptionSelected = { updateDisplaySetting(displaySetting.copy(newChatContentStyle = it)) },
                                optionToString = { style ->
                                    when (style) {
                                        me.lucid.domchat.data.datastore.NewChatContentStyle.NONE -> stringResource(R.string.setting_new_chat_content_none)
                                        me.lucid.domchat.data.datastore.NewChatContentStyle.TEMPLATES -> stringResource(R.string.setting_new_chat_content_templates)
                                        me.lucid.domchat.data.datastore.NewChatContentStyle.ACTIONS -> stringResource(R.string.setting_new_chat_content_actions)
                                    }
                                },
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    )
                }
            }

            // Chat Display Settings
            item {
                SettingsGroup(
                    title = stringResource(R.string.setting_page_chat_settings)
                ) {
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_enable_blur_effect_title),
                        subtitle = stringResource(R.string.setting_display_page_enable_blur_effect_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.enableBlurEffect,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(enableBlurEffect = it))
                                }
                            )
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.setting_ui_move_toolbar_bottom_title),
                        subtitle = stringResource(R.string.setting_ui_move_toolbar_bottom_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.chatToolbarAtBottom,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(chatToolbarAtBottom = it))
                                }
                            )
                        }
                    )
                    SettingGroupItem(
                        title = "Next Message Suggestions",
                        subtitle = "Show suggested reply pills above the chat bar after responses",
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.showChatSuggestions,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showChatSuggestions = it))
                                }
                            )
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.setting_ui_show_character_avatar_title),
                        subtitle = stringResource(R.string.setting_ui_show_character_avatar_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.showModelIcon,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showModelIcon = it))
                                }
                            )
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_show_assistant_bubbles_title),
                        subtitle = stringResource(R.string.setting_display_page_show_assistant_bubbles_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.showAssistantBubbles,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showAssistantBubbles = it))
                                }
                            )
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_show_token_usage_title),
                        subtitle = stringResource(R.string.setting_display_page_show_token_usage_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.showTokenUsage,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showTokenUsage = it))
                                }
                            )
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_auto_collapse_thinking_title),
                        subtitle = stringResource(R.string.setting_display_page_auto_collapse_thinking_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.autoCloseThinking,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(autoCloseThinking = it))
                                }
                            )
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_reasoning_preview_title),
                        subtitle = stringResource(R.string.setting_display_page_reasoning_preview_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.reasoningPreviewEnabled,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(reasoningPreviewEnabled = it))
                                }
                            )
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.setting_ui_show_context_stacks_title),
                        subtitle = stringResource(R.string.setting_ui_show_context_stacks_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.showContextStacks,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showContextStacks = it))
                                }
                            )
                        }
                    )
                }
            }

            // AI Response Streaming Animation Settings
            item {
                SettingsGroup(
                    title = "AI Streaming Animation"
                ) {
                    SettingGroupItem(
                        title = "Smooth Fade-in Animation",
                        subtitle = "Fade new tokens smoothly into view instead of popping in word-by-word",
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.enableStreamingFadeIn,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(enableStreamingFadeIn = it))
                                }
                            )
                        }
                    )

                    if (displaySetting.enableStreamingFadeIn) {
                        SettingGroupItem(
                            title = "Continuous Fluid Stream",
                            subtitle = "Stream characters continuously for fluid liquid text motion",
                            trailing = {
                                HapticSwitch(
                                    checked = displaySetting.streamingContinuousFlow,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(streamingContinuousFlow = it))
                                    }
                                )
                            }
                        )

                        // Fade Duration Slider
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Fade Duration",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${displaySetting.streamingFadeDurationMs} ms",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = displaySetting.streamingFadeDurationMs.toFloat(),
                                onValueChange = {
                                    updateDisplaySetting(displaySetting.copy(streamingFadeDurationMs = it.toInt()))
                                },
                                valueRange = 100f..1200f,
                                steps = 21,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Starting Opacity Slider
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Starting Opacity",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${(displaySetting.streamingFadeStartAlpha * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = displaySetting.streamingFadeStartAlpha,
                                onValueChange = {
                                    updateDisplaySetting(displaySetting.copy(streamingFadeStartAlpha = (it * 100).roundToInt() / 100f))
                                },
                                valueRange = 0.0f..0.7f,
                                steps = 13,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            // Message Jumper Settings
            item {
                SettingsGroup(
                    title = stringResource(R.string.setting_ui_message_jumper_group)
                ) {
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_show_message_jumper_title),
                        subtitle = stringResource(R.string.setting_display_page_show_message_jumper_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.showMessageJumper,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showMessageJumper = it))
                                }
                            )
                        }
                    )
                    if (displaySetting.showMessageJumper) {
                        SettingGroupItem(
                            title = stringResource(R.string.setting_display_page_message_jumper_position_title),
                            subtitle = stringResource(R.string.setting_display_page_message_jumper_position_desc),
                            trailing = {
                                HapticSwitch(
                                    checked = displaySetting.messageJumperOnLeft,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(messageJumperOnLeft = it))
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // Haptics Settings
            item {
                SettingsGroup(
                    title = stringResource(R.string.setting_ui_haptics_group)
                ) {
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_enable_message_generation_haptic_effect_title),
                        subtitle = stringResource(R.string.setting_display_page_enable_message_generation_haptic_effect_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.enableMessageGenerationHapticEffect,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(enableMessageGenerationHapticEffect = it))
                                }
                            )
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.setting_ui_haptics_ui_title),
                        subtitle = stringResource(R.string.setting_ui_haptics_ui_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.enableUIHaptics,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(enableUIHaptics = it))
                                }
                            )
                        }
                    )
                }
            }

            // Code Blocks Settings
            item {
                SettingsGroup(
                    title = stringResource(R.string.setting_ui_code_blocks_group)
                ) {
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_code_block_auto_wrap_title),
                        subtitle = stringResource(R.string.setting_display_page_code_block_auto_wrap_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.codeBlockAutoWrap,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(codeBlockAutoWrap = it))
                                }
                            )
                        }
                    )
                    SettingGroupItem(
                        title = stringResource(R.string.setting_display_page_code_block_auto_collapse_title),
                        subtitle = stringResource(R.string.setting_display_page_code_block_auto_collapse_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.codeBlockAutoCollapse,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(codeBlockAutoCollapse = it))
                                }
                            )
                        }
                    )
                }
            }

            // Font Size Slider
            item {
                SettingsGroup(
                    title = stringResource(R.string.setting_display_page_font_size_title)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Slider(
                            value = displaySetting.fontSizeRatio,
                            onValueChange = {
                                updateDisplaySetting(displaySetting.copy(fontSizeRatio = it))
                            },
                            valueRange = 0.5f..2f,
                            steps = 11,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(displaySetting.fontSizeRatio * 100).toInt()}%",
                        )
                    }
                    MarkdownBlock(
                        content = stringResource(R.string.setting_display_page_font_size_preview),
                        modifier = Modifier.padding(8.dp),
                        style = LocalTextStyle.current.copy(
                            fontSize = LocalTextStyle.current.fontSize * displaySetting.fontSizeRatio,
                            lineHeight = LocalTextStyle.current.lineHeight * displaySetting.fontSizeRatio,
                        )
                    )
                }
            }

            item {
                SettingsGroup(
                    title = stringResource(R.string.setting_display_advanced)
                ) {
                    SettingGroupItem(
                        title = stringResource(R.string.setting_ui_context_token_summary_title),
                        subtitle = stringResource(R.string.setting_ui_context_token_summary_desc),
                        trailing = {
                            HapticSwitch(
                                checked = displaySetting.showContextTokenSummary,
                                onCheckedChange = {
                                    updateDisplaySetting(
                                        displaySetting.copy(showContextTokenSummary = it)
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}
