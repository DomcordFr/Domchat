package me.lucid.domchat.ui.pages.assistant.detail

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.lucid.domchat.R
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.SpontaneousMessageMode
import me.lucid.domchat.data.model.Tag as DataTag
import me.lucid.domchat.service.SpontaneousWorker
import me.lucid.domchat.ui.components.ui.DebouncedTextField
import me.lucid.domchat.ui.components.ui.DomChatSlider
import me.lucid.domchat.ui.components.ui.HapticSwitch
import me.lucid.domchat.ui.components.ui.TagsInput
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.motion.ExpandableContent
import me.lucid.domchat.ui.pages.setting.components.SettingGroupInputItem
import me.lucid.domchat.ui.pages.setting.components.SettingGroupItem
import me.lucid.domchat.ui.pages.setting.components.SettingsGroup
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import kotlin.math.roundToInt

/**
 * Profile sub-page - Display & chat name, avatar, creator notes, tags, and spontaneous messaging.
 * Dedicated modular sub-page for character identity and spontaneous roleplay interactions.
 */
@Composable
fun AssistantProfileSubPage(
    assistant: Assistant,
    tags: List<DataTag>,
    onUpdate: (Assistant) -> Unit,
    vm: AssistantDetailVM
) {
    val context = LocalContext.current
    val haptics = rememberPremiumHaptics()
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onUpdate(assistant.copy(enableSpontaneous = true))
        }
    }

    var scheduleSelection by remember(
        assistant.id,
        assistant.notificationStartHour,
        assistant.notificationEndHour,
    ) {
        mutableStateOf(
            spontaneousScheduleSelectionFromHours(
                startHour = assistant.notificationStartHour,
                endHour = assistant.notificationEndHour,
            )
        )
    }

    var frequencySlider by remember(assistant.id, assistant.notificationFrequencyHours) {
        mutableFloatStateOf(assistant.notificationFrequencyHours.coerceIn(1, 24).toFloat())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ═════════════════════════════════════════════════════════════════════
        // AVATAR SECTION (prominent, centered)
        // ═════════════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF000000))
                    .border(
                        BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                UIAvatar(
                    value = assistant.avatar,
                    name = assistant.name.ifBlank { stringResource(R.string.assistant_page_default_assistant) },
                    onUpdate = { avatar ->
                        onUpdate(assistant.copy(avatar = avatar))
                    },
                    modifier = Modifier.size(96.dp)
                )
            }

            Text(
                text = stringResource(R.string.assistant_profile_tap_change_avatar),
                style = MaterialTheme.typography.labelSmall,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.60f)
            )
        }

        // ═══════════════════════════════════════════════════════════════════
        // IDENTITY GROUP (display name, chat name)
        // ═══════════════════════════════════════════════════════════════════
        SettingsGroup(title = stringResource(R.string.assistant_profile_identity)) {
            SettingGroupInputItem(
                title = "Character Name",
                subtitle = "Base bot name used by system and card metadata",
                content = {
                    DebouncedTextField(
                        value = assistant.name,
                        onValueChange = { onUpdate(assistant.copy(name = it)) },
                        stateKey = "${assistant.id}_name",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 5,
                        placeholder = stringResource(R.string.assistant_page_name_placeholder)
                    )
                }
            )
            SettingGroupInputItem(
                title = "Display Name",
                subtitle = "Name shown in Hub, lists, and character header",
                content = {
                    DebouncedTextField(
                        value = assistant.displayName ?: assistant.name,
                        onValueChange = { onUpdate(assistant.copy(displayName = it.takeIf { it.isNotBlank() })) },
                        stateKey = "${assistant.id}_display",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 5,
                        placeholder = stringResource(R.string.assistant_page_name_placeholder)
                    )
                }
            )
            SettingGroupInputItem(
                title = "Chat Name",
                subtitle = "Name used in dialogues and substituted for {{char}}",
                content = {
                    DebouncedTextField(
                        value = assistant.chatName ?: assistant.name,
                        onValueChange = { onUpdate(assistant.copy(chatName = it.takeIf { it.isNotBlank() })) },
                        stateKey = "${assistant.id}_chat",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 5,
                        placeholder = stringResource(R.string.assistant_page_chat_name_placeholder)
                    )
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════════
        // TAGS
        // ═══════════════════════════════════════════════════════════════════
        SettingsGroup(title = stringResource(R.string.assistant_page_tags)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.assistant_profile_tags_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TagsInput(
                    value = assistant.tags,
                    tags = tags,
                    onValueChange = { tagIds, updatedTags ->
                        vm.updateTags(tagIds, updatedTags)
                    },
                )
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // SPONTANEOUS MESSAGING (autonomous roleplay reach-outs)
        // ═══════════════════════════════════════════════════════════════════
        SettingsGroup(title = stringResource(R.string.assistant_advanced_spontaneous_messaging)) {
            SettingGroupItem(
                title = stringResource(R.string.assistant_advanced_enable_spontaneous),
                subtitle = stringResource(R.string.assistant_advanced_enable_spontaneous_desc),
                trailing = {
                    HapticSwitch(
                        checked = assistant.enableSpontaneous,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onUpdate(assistant.copy(enableSpontaneous = true))
                                }
                            } else {
                                onUpdate(assistant.copy(enableSpontaneous = false))
                            }
                        }
                    )
                }
            )

            ExpandableContent(visible = assistant.enableSpontaneous) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Delivery Schedule Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.assistant_advanced_delivery_schedule),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = buildSpontaneousWindowSummary(context, scheduleSelection),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Status Badges Row (clean mobile 3-column layout)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val cardShape = RoundedCornerShape(10.dp)
                                val cardBorder = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)

                                // Starts
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFF000000), cardShape)
                                        .border(cardBorder, cardShape)
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "Starts",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.55f),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = formatHourLabel(scheduleSelection.startHour),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Duration
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFF000000), cardShape)
                                        .border(cardBorder, cardShape)
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "Duration",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.55f),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (scheduleSelection.spanHours >= 24) "All Day" else "${scheduleSelection.spanHours}h",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Ends
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFF000000), cardShape)
                                        .border(cardBorder, cardShape)
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "Ends",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.55f),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = formatScheduleEndLabel(context, scheduleSelection),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            RangeSlider(
                                value = scheduleSelection.toSliderValues(),
                                onValueChange = { newRange ->
                                    scheduleSelection = normalizeSpontaneousScheduleSelection(
                                        rawStart = newRange.start.roundToInt(),
                                        rawEnd = newRange.endInclusive.roundToInt(),
                                    )
                                },
                                onValueChangeFinished = {
                                    val (newStartHour, newEndHour) =
                                        spontaneousScheduleSelectionToHours(scheduleSelection)
                                    if (
                                        newStartHour != assistant.notificationStartHour ||
                                        newEndHour != assistant.notificationEndHour
                                    ) {
                                        haptics.perform(HapticPattern.Selection)
                                        onUpdate(
                                            assistant.copy(
                                                notificationStartHour = newStartHour,
                                                notificationEndHour = newEndHour,
                                            )
                                        )
                                    }
                                },
                                valueRange = SPONTANEOUS_SCHEDULE_SLIDER_MIN.toFloat()..
                                    SPONTANEOUS_SCHEDULE_SLIDER_MAX.toFloat(),
                                steps = SPONTANEOUS_SCHEDULE_SLIDER_STEPS,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = formatHourLabel(0),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = stringResource(
                                        R.string.assistant_advanced_next_day,
                                        formatHourLabel(23)
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Text(
                                text = stringResource(R.string.assistant_advanced_schedule_tip),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Message Type Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.assistant_advanced_message_type),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(R.string.assistant_advanced_message_type_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val modes = listOf(
                                Triple(
                                    SpontaneousMessageMode.BOTH,
                                    stringResource(R.string.assistant_advanced_message_type_both),
                                    "Continue recent roleplay or start fresh topics"
                                ),
                                Triple(
                                    SpontaneousMessageMode.CONTINUE_ONLY,
                                    stringResource(R.string.assistant_advanced_message_type_continue),
                                    "Only follow up on conversations you already started"
                                ),
                                Triple(
                                    SpontaneousMessageMode.NEW_ONLY,
                                    stringResource(R.string.assistant_advanced_message_type_new),
                                    "Only initiate brand new conversations or scenes"
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                modes.forEach { (mode, label, desc) ->
                                    val isSelected = assistant.spontaneousMessageMode == mode
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF000000))
                                            .border(
                                                border = if (isSelected) {
                                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                                } else {
                                                    BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                if (!isSelected) {
                                                    haptics.perform(HapticPattern.Selection)
                                                    onUpdate(assistant.copy(spontaneousMessageMode = mode))
                                                }
                                            }
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = desc,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Minimum Gap Frequency Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                    ) {
                        val frequencyHours = frequencySlider.roundToInt().coerceIn(1, 24)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.assistant_advanced_minimum_gap),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF000000), RoundedCornerShape(8.dp))
                                        .border(BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = formatFrequencyLabel(context, frequencyHours),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = stringResource(R.string.assistant_advanced_minimum_gap_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            DomChatSlider(
                                value = frequencySlider,
                                onValueChange = { frequencySlider = it.roundToInt().toFloat() },
                                onValueChangeFinished = {
                                    val newFrequency = frequencySlider.roundToInt().coerceIn(1, 24)
                                    if (newFrequency != assistant.notificationFrequencyHours) {
                                        haptics.perform(HapticPattern.Selection)
                                        onUpdate(assistant.copy(notificationFrequencyHours = newFrequency))
                                    }
                                },
                                valueRange = 1f..24f,
                                steps = 22
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "1h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "24h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Test Notification Action Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                scope.launch {
                                    haptics.perform(HapticPattern.Selection)
                                    try {
                                        SpontaneousWorker.sendTestNotification(context, assistant)
                                        toaster.show("Test notification sent! Check your status bar.")
                                    } catch (e: Exception) {
                                        toaster.show("Failed to send test notification: ${e.message}")
                                    }
                                }
                            },
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF000000))
                                    .border(BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Send Test Notification",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Preview incoming message bubble and circular avatar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF000000))
                                    .border(BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Send",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
