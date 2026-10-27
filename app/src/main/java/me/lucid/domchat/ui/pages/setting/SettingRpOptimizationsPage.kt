package me.lucid.domchat.ui.pages.setting

import me.lucid.domchat.ui.theme.LocalDarkMode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import me.lucid.domchat.ui.components.ui.HapticSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.lucid.domchat.R
import me.lucid.domchat.data.datastore.RpStyleRule
import me.lucid.domchat.ui.components.nav.BackButton
import me.lucid.domchat.ui.components.nav.OneUITopAppBar
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.utils.plus
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingRpOptimizationsPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var displaySetting by remember(settings) { mutableStateOf(settings.displaySetting) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<RpStyleRule?>(null) }

    fun updateDisplaySetting(rules: List<RpStyleRule>) {
        val newSetting = displaySetting.copy(rpStyleRules = rules)
        displaySetting = newSetting
        vm.updateSettings(settings.copy(displaySetting = newSetting))
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()

    Scaffold(
        topBar = {
            OneUITopAppBar(
                title = stringResource(R.string.setting_rp_optimizations_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton()
                }
            )
        },
        floatingActionButton = {
            val isDark = LocalDarkMode.current
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = CircleShape,
                containerColor = if (isDark) Color.White else Color.Black,
                contentColor = if (isDark) Color.Black else Color.White
            ) {
                Icon(Icons.Rounded.Add, stringResource(R.string.setting_rp_add_rule))
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(contentPadding),
            state = lazyListState,
            contentPadding = contentPadding + PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Description
            item {
                val isDark = LocalDarkMode.current
                val cardBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = AppShapes.CardLarge,
                    modifier = Modifier.border(1.dp, cardBorder, AppShapes.CardLarge)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.setting_rp_custom_text_styling),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.setting_rp_custom_text_styling_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Supported patterns section
                        Text(
                            text = stringResource(R.string.setting_rp_supported_patterns),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "*" to stringResource(R.string.setting_rp_italic),
                                "**" to stringResource(R.string.setting_rp_bold),
                                "~~" to stringResource(R.string.setting_rp_strikethrough),
                                "`" to stringResource(R.string.setting_rp_inline_code),
                                "#" to stringResource(R.string.setting_rp_heading_level, 1),
                                "##" to stringResource(R.string.setting_rp_heading_level, 2),
                                "###" to stringResource(R.string.setting_rp_heading_level, 3),
                                "####" to stringResource(R.string.setting_rp_heading_level, 4),
                                "#####" to stringResource(R.string.setting_rp_heading_level, 5),
                                "######" to stringResource(R.string.setting_rp_heading_level, 6),
                                ">" to stringResource(R.string.setting_rp_blockquotes)
                            ).forEach { (pattern, desc) ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pattern,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        modifier = Modifier
                                            .background(
                                                if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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

            // Quick 1-Tap Text Type Presets
            item {
                val isDark = LocalDarkMode.current
                val cardBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = AppShapes.CardLarge,
                    modifier = Modifier.border(1.dp, cardBorder, AppShapes.CardLarge)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "One-Tap Text Formatting Presets",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to instantly add or toggle formatting rules for common roleplay text styles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val defaultRpPresets = listOf(
                                Triple("Actions (*action*)", "*", "#87CEEB"),
                                Triple("Dialogue (\"speech\")", "\"", "#FFD700"),
                                Triple("Thoughts (~thought~)", "~", "#FFB6C1"),
                                Triple("Emphasis (**bold**)", "**", "#FF6B6B"),
                                Triple("Quotes (> quote)", ">", "#90EE90")
                            )
                            defaultRpPresets.forEach { (label, pat, hex) ->
                                val existing = displaySetting.rpStyleRules.find { it.pattern == pat }
                                FilterChip(
                                    selected = existing != null && existing.enabled,
                                    onClick = {
                                        if (existing == null) {
                                            updateDisplaySetting(
                                                displaySetting.rpStyleRules + RpStyleRule(
                                                    id = kotlin.uuid.Uuid.random().toString(),
                                                    pattern = pat,
                                                    colorHex = hex,
                                                    enabled = true
                                                )
                                            )
                                        } else {
                                            updateDisplaySetting(
                                                displaySetting.rpStyleRules.map {
                                                    if (it.id == existing.id) it.copy(enabled = !it.enabled) else it
                                                }
                                            )
                                        }
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = if (isDark) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Rules list
            if (displaySetting.rpStyleRules.isEmpty()) {
                item {
                    val isDark = LocalDarkMode.current
                    val cardBg = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f)
                    val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = AppShapes.CardLarge,
                        modifier = Modifier.border(1.dp, cardBorder, AppShapes.CardLarge)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.setting_rp_no_rules),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displaySetting.rpStyleRules, key = { it.id }) { rule ->
                    RpStyleRuleItem(
                        rule = rule,
                        onToggle = { enabled ->
                            updateDisplaySetting(
                                displaySetting.rpStyleRules.map {
                                    if (it.id == rule.id) it.copy(enabled = enabled) else it
                                }
                            )
                        },
                        onEdit = { editingRule = rule },
                        onDelete = {
                            updateDisplaySetting(
                                displaySetting.rpStyleRules.filter { it.id != rule.id }
                            )
                        }
                    )
                }
            }

            // Preview section
            if (displaySetting.rpStyleRules.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.setting_fonts_preview),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                item {
                    val isDark = LocalDarkMode.current
                    val cardBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                    val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = AppShapes.CardLarge,
                        modifier = Modifier.border(1.dp, cardBorder, AppShapes.CardLarge)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            displaySetting.rpStyleRules.filter { it.enabled }.forEach { rule ->
                                val color = try {
                                    Color(android.graphics.Color.parseColor(rule.colorHex))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.onSurface
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Text(
                                        text = stringResource(R.string.setting_rp_example_text, rule.pattern),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = color
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingRule != null) {
        RpStyleRuleDialog(
            rule = editingRule,
            onDismiss = {
                showAddDialog = false
                editingRule = null
            },
            onSave = { newRule ->
                if (editingRule != null) {
                    // Edit existing
                    updateDisplaySetting(
                        displaySetting.rpStyleRules.map {
                            if (it.id == editingRule!!.id) newRule else it
                        }
                    )
                } else {
                    // Add new
                    updateDisplaySetting(displaySetting.rpStyleRules + newRule)
                }
                showAddDialog = false
                editingRule = null
            }
        )
    }
}

@Composable
private fun RpStyleRuleItem(
    rule: RpStyleRule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val previewColor = try {
        Color(android.graphics.Color.parseColor(rule.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.onSurface
    }
    val isDark = LocalDarkMode.current
    val cardBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = AppShapes.CardLarge,
        modifier = Modifier.border(1.dp, cardBorder, AppShapes.CardLarge),
        onClick = onEdit
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                    )
                    Text(stringResource(R.string.setting_rp_custom_preview, rule.pattern))
                }
            },
            supportingContent = {
                Text(
                    text = rule.colorHex,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Rounded.Delete,
                            stringResource(R.string.setting_tts_filter_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    HapticSwitch(
                        checked = rule.enabled,
                        onCheckedChange = onToggle
                    )
                }
            }
        )
    }
}

@Composable
private fun RpStyleRuleDialog(
    rule: RpStyleRule?,
    onDismiss: () -> Unit,
    onSave: (RpStyleRule) -> Unit
) {
    var pattern by remember { mutableStateOf(rule?.pattern ?: "") }
    var colorHex by remember { mutableStateOf(rule?.colorHex ?: "#808080") }    
    // Parse current color for sliders
    val currentColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }
    var red by remember { mutableStateOf((currentColor.red * 255).toInt()) }
    var green by remember { mutableStateOf((currentColor.green * 255).toInt()) }
    var blue by remember { mutableStateOf((currentColor.blue * 255).toInt()) }
    
    // Update colorHex when RGB sliders change
    fun updateColorFromRgb() {
        colorHex = String.format("#%02X%02X%02X", red, green, blue)
    }

    val isDark = LocalDarkMode.current
    ApplyDialogWindowBlur()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF14151C).copy(alpha = 0.88f) else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.border(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f), AppShapes.CardLarge),
        title = {
            Text(
                if (rule != null) {
                    stringResource(R.string.setting_rp_edit_rule)
                } else {
                    stringResource(R.string.setting_rp_add_rule)
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Quick Style Presets",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("Actions (*action*)", "*", "#87CEEB"),
                        Triple("Dialogue (\"speech\")", "\"", "#FFD700"),
                        Triple("Thoughts (~thought~)", "~", "#FFB6C1"),
                        Triple("Emphasis (**bold**)", "**", "#FF6B6B"),
                        Triple("Quotes (> quote)", ">", "#90EE90")
                    ).forEach { (label, presetPat, presetHex) ->
                        FilterChip(
                            selected = pattern == presetPat,
                            onClick = {
                                pattern = presetPat
                                colorHex = presetHex
                                val c = Color(android.graphics.Color.parseColor(presetHex))
                                red = (c.red * 255).toInt()
                                green = (c.green * 255).toInt()
                                blue = (c.blue * 255).toInt()
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text(stringResource(R.string.setting_rp_pattern)) },
                    placeholder = { Text(stringResource(R.string.setting_rp_pattern_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        when (pattern) {
                            "#", "##", "###", "####", "#####", "######" -> 
                                Text(stringResource(R.string.setting_rp_heading_level, pattern.length))
                            ">" -> Text(stringResource(R.string.setting_rp_blockquotes))
                            "*" -> Text(stringResource(R.string.setting_rp_italic))
                            "**" -> Text(stringResource(R.string.setting_rp_bold))
                            "~~" -> Text(stringResource(R.string.setting_rp_strikethrough))
                            "`" -> Text(stringResource(R.string.setting_rp_inline_code))
                            else -> if (pattern.isNotEmpty()) {
                                Text(stringResource(R.string.setting_rp_custom_preview, pattern))
                            } else {
                                Text(stringResource(R.string.setting_rp_enter_pattern))
                            }
                        }
                    },
                    shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
                )
                
                OutlinedTextField(
                    value = colorHex,
                    onValueChange = { 
                        colorHex = it
                        // Try to update RGB sliders from hex
                        try {
                            val c = Color(android.graphics.Color.parseColor(it))
                            red = (c.red * 255).toInt()
                            green = (c.green * 255).toInt()
                            blue = (c.blue * 255).toInt()
                        } catch (e: Exception) { }
                    },
                    label = { Text(stringResource(R.string.setting_rp_color_hex)) },
                    placeholder = { Text("#808080") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        val color = try {
                            Color(android.graphics.Color.parseColor(colorHex))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    },
                    shape = me.lucid.domchat.ui.theme.AppShapes.InputField,
                )
                
                // Color presets row
                Text(
                    text = stringResource(R.string.setting_rp_presets),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "#808080", // Gray
                        "#FFD700", // Yellow
                        "#87CEEB", // Light Blue
                        "#90EE90", // Light Green
                        "#FFB6C1", // Pink
                        "#FF6B6B"  // Red
                    ).forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { 
                                    colorHex = hex
                                    val c = Color(android.graphics.Color.parseColor(hex))
                                    red = (c.red * 255).toInt()
                                    green = (c.green * 255).toInt()
                                    blue = (c.blue * 255).toInt()
                                }
                        )
                    }
                }
                
                // RGB sliders - always visible for custom color selection
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Red slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("R", modifier = Modifier.width(20.dp), color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                        androidx.compose.material3.Slider(
                            value = red.toFloat(),
                            onValueChange = { red = it.toInt(); updateColorFromRgb() },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF5252),
                                activeTrackColor = Color(0xFFFF5252),
                                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text("$red", modifier = Modifier.width(36.dp), color = if (isDark) Color.White else Color.Unspecified)
                    }
                    // Green slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("G", modifier = Modifier.width(20.dp), color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        androidx.compose.material3.Slider(
                            value = green.toFloat(),
                            onValueChange = { green = it.toInt(); updateColorFromRgb() },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF4CAF50),
                                activeTrackColor = Color(0xFF4CAF50),
                                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text("$green", modifier = Modifier.width(36.dp), color = if (isDark) Color.White else Color.Unspecified)
                    }
                    // Blue slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("B", modifier = Modifier.width(20.dp), color = Color(0xFF448AFF), fontWeight = FontWeight.Bold)
                        androidx.compose.material3.Slider(
                            value = blue.toFloat(),
                            onValueChange = { blue = it.toInt(); updateColorFromRgb() },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF448AFF),
                                activeTrackColor = Color(0xFF448AFF),
                                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text("$blue", modifier = Modifier.width(36.dp), color = if (isDark) Color.White else Color.Unspecified)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pattern.isNotBlank()) {
                        onSave(
                            RpStyleRule(
                                id = rule?.id ?: kotlin.uuid.Uuid.random().toString(),
                                pattern = pattern,
                                colorHex = colorHex,
                                enabled = rule?.enabled ?: true
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color.White else Color.Black,
                    contentColor = if (isDark) Color.Black else Color.White
                ),
                shape = AppShapes.CardMedium
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Unspecified)
            }
        }
    )
}

