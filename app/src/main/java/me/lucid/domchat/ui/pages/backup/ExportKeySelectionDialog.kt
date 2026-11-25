package me.lucid.domchat.ui.pages.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.lucid.ai.provider.ApiKeyEntry
import me.lucid.ai.provider.ProviderSetting
import me.lucid.domchat.R
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.AppShapes
import kotlin.uuid.Uuid

/**
 * Dialog to select which API keys to include when exporting backups.
 */
@Composable
fun ExportKeySelectionDialog(
    providers: List<ProviderSetting>,
    onDismiss: () -> Unit,
    onConfirm: (selectedKeyIds: Set<Uuid>) -> Unit,
    title: String = stringResource(R.string.backup_export_select_keys_title),
    description: String = stringResource(R.string.backup_export_select_keys_desc),
) {
    val haptics = rememberPremiumHaptics()

    // Collect all keys with their provider info
    data class KeyWithProvider(
        val entry: ApiKeyEntry,
        val providerName: String,
    )

    val allKeys = remember(providers) {
        providers.flatMap { provider ->
            provider.apiKeyPool.map { entry ->
                KeyWithProvider(entry, provider.name.ifBlank { "Provider" })
            }
        }
    }

    // Selected state map keyed by entry.id, initialized to true
    val selectedMap = remember(allKeys) {
        mutableStateMapOf<Uuid, Boolean>().apply {
            allKeys.forEach { put(it.entry.id, true) }
        }
    }

    AlertDialog(
        containerColor = Color(0xFF000000),
        shape = me.lucid.domchat.ui.theme.AppShapes.SettingDialog,
        modifier = Modifier.border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SettingDialog),
        titleContentColor = Color(0xFFF2F2F7),
        textContentColor = Color(0xFFF2F2F7),
        onDismissRequest = onDismiss,
        title = {
            me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Key,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA6A6A6)
                )

                if (allKeys.isEmpty()) {
                    Text(
                        text = stringResource(R.string.backup_export_no_keys),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA6A6A6),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // Select All / Deselect All Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF000000),
                            contentColor = Color(0xFFF2F2F7)
                            ),
            border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
                            ),

                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                allKeys.forEach { selectedMap[it.entry.id] = true }
                            },
                            shape = AppShapes.ButtonPill,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.backup_export_select_all))
                        }

                        OutlinedButton(                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF000000),
                            contentColor = Color(0xFFF2F2F7)
                            ),
            border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
                            ),

                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                allKeys.forEach { selectedMap[it.entry.id] = false }
                            },
                            shape = AppShapes.ButtonPill,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.backup_export_deselect_all))
                        }
                    }

                    // Key List grouped by provider
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val grouped = allKeys.groupBy { it.providerName }
                        grouped.forEach { (providerName, keys) ->
                            item(key = "header_$providerName") {
                                Text(
                                    text = providerName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                )
                            }

                            items(keys, key = { it.entry.id }) { item ->
                                val isChecked = selectedMap[item.entry.id] ?: false
                                Surface(
                                    shape = AppShapes.CardSmall,
                                    color = if (isChecked) Color.White.copy(alpha = 0.18f) else Color(0xFF000000),
                                    border = androidx.compose.foundation.BorderStroke(
                                        0.5.dp,
                                        if (isChecked)
                                            androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
                                        else
                                            me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle
                                    ),
                                    onClick = {
                                        haptics.perform(HapticPattern.Tick)
                                        selectedMap[item.entry.id] = !isChecked
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.entry.name.ifBlank { "API Key" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                haptics.perform(HapticPattern.Tick)
                                                selectedMap[item.entry.id] = checked
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFFFF),
                contentColor = Color(0xFF000000)
                ),

                onClick = {
                    haptics.perform(HapticPattern.Pop)
                    val selectedIds = selectedMap.filterValues { it }.keys.toSet()
                    onConfirm(selectedIds)
                },
                shape = AppShapes.ButtonRounded
            ) {
                Text(stringResource(R.string.backup_export_confirm))
            }
        },
        dismissButton = {
            TextButton(                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFFFF),
                contentColor = Color(0xFF000000)
                ),
onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
