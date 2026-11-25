package me.lucid.domchat.ui.components.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.togetherWith
import kotlin.math.roundToInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.FormatStrikethrough
import androidx.compose.material.icons.rounded.HorizontalRule
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.appFieldColors
import me.lucid.domchat.ui.theme.appFilterChipBorder
import me.lucid.domchat.ui.theme.appFilterChipColors
import me.lucid.domchat.ui.theme.liquidGlassSurface

/**
 * Standard Markdown Text Editor for Character Creation & Editing.
 * Provides:
 * - Write / Preview tabs with smooth crossfade
 * - Markdown formatting toolbar (Bold, Italic, Code, Quote, Image embedding)
 * - {{char}} and {{user}} macro pills
 * - Word and character count indicator
 * - AMOLED liquid glass aesthetics
 */
@Composable
fun DomChatMarkdownEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Write markdown here...",
    minLines: Int = 10,
    showMacros: Boolean = true,
    allowImageEmbed: Boolean = true,
    stateKey: Any? = null,
) {
    val haptics = rememberPremiumHaptics()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Write, 1: Preview

    val textFieldState = if (stateKey != null) {
        androidx.compose.runtime.key(stateKey) {
            rememberTextFieldState(initialText = value)
        }
    } else {
        rememberTextFieldState(initialText = value)
    }

    var isFocused by remember { mutableStateOf(false) }
    val latestValue by rememberUpdatedState(value)
    val latestOnValueChange by rememberUpdatedState(onValueChange)

    // Sync from external value when NOT focused (e.g. character import, external reset, or tab switch)
    LaunchedEffect(value, stateKey, isFocused) {
        if (!isFocused && textFieldState.text.toString() != value) {
            textFieldState.edit {
                replace(0, length, value)
            }
        }
    }

    // Debounced auto-sync to external state (200ms debounce)
    LaunchedEffect(stateKey, textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .drop(1)
            .debounce(200L)
            .collect { newText ->
                if (newText != latestValue) {
                    latestOnValueChange(newText)
                }
            }
    }

    // Immediate commit on losing focus or disposal
    DisposableEffect(isFocused) {
        if (!isFocused) {
            val currentText = textFieldState.text.toString()
            if (currentText != latestValue) {
                latestOnValueChange(currentText)
            }
        }
        onDispose {
            val currentText = textFieldState.text.toString()
            if (currentText != latestValue) {
                latestOnValueChange(currentText)
            }
        }
    }

    fun insertOrWrap(prefix: String, suffix: String, defaultPlaceholder: String = "") {
        haptics.perform(HapticPattern.Tick)
        textFieldState.edit {
            val sel = selection
            val start = minOf(sel.start, sel.end)
            val end = maxOf(sel.start, sel.end)
            if (start == end) {
                val insertContent = if (defaultPlaceholder.isNotEmpty()) {
                    "$prefix$defaultPlaceholder$suffix"
                } else {
                    "$prefix$suffix"
                }
                replace(start, end, insertContent)
                if (defaultPlaceholder.isNotEmpty()) {
                    selection = TextRange(start + prefix.length, start + prefix.length + defaultPlaceholder.length)
                } else {
                    selection = TextRange(start + prefix.length)
                }
            } else {
                val current = toString()
                val selectedText = current.substring(start, end)
                val replacement = "$prefix$selectedText$suffix"
                replace(start, end, replacement)
                selection = TextRange(start + prefix.length, start + prefix.length + selectedText.length)
            }
        }
    }

    fun insertTextAtCursor(textToInsert: String) {
        haptics.perform(HapticPattern.Tick)
        textFieldState.edit {
            val sel = selection
            val start = minOf(sel.start, sel.end)
            val end = maxOf(sel.start, sel.end)
            replace(start, end, textToInsert)
            selection = TextRange(start + textToInsert.length)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            haptics.perform(HapticPattern.Pop)
            val imageMarkdown = "\n![Image]($uri)\n"
            textFieldState.edit {
                val sel = selection
                val start = minOf(sel.start, sel.end)
                val end = maxOf(sel.start, sel.end)
                replace(start, end, imageMarkdown)
                selection = TextRange(start + imageMarkdown.length)
            }
        }
    }

    val currentText = textFieldState.text
    val wordCount = remember(currentText) {
        if (currentText.isBlank()) 0 else currentText.trim().split("\\s+".toRegex()).size
    }
    val tokenCount = remember(currentText) {
        if (currentText.isBlank()) 0 else (currentText.length / 3.8).roundToInt().coerceAtLeast(1)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tab Row & Counters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = {
                        haptics.perform(HapticPattern.Tick)
                        selectedTab = 0
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Write") },
                    shape = AppShapes.Chip,
                    colors = appFilterChipColors(selectedTab == 0),
                    border = appFilterChipBorder(selectedTab == 0)
                )

                FilterChip(
                    selected = selectedTab == 1,
                    onClick = {
                        haptics.perform(HapticPattern.Tick)
                        val textToCommit = textFieldState.text.toString()
                        if (textToCommit != latestValue) {
                            latestOnValueChange(textToCommit)
                        }
                        selectedTab = 1
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Preview") },
                    shape = AppShapes.Chip,
                    colors = appFilterChipColors(selectedTab == 1),
                    border = appFilterChipBorder(selectedTab == 1)
                )
            }

            // Word / Token count pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF000000).copy(alpha = 0.55f),
                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle)
            ) {
                Text(
                    text = "$wordCount words • $tokenCount tokens",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Markdown Toolbar (displayed inside Write tab without height collapse)
        @Composable
        fun MarkdownEditorToolbar() {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(12.dp),
                        borderWidth = 0.5.dp,
                        fillColor = Color(0xFF000000).copy(alpha = 0.40f)
                    ),
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EditorToolbarButton(
                        icon = Icons.Rounded.Title,
                        tooltip = "Heading",
                        onClick = {
                            insertOrWrap("### ", "", "Heading")
                        }
                    )
                    EditorToolbarButton(
                        icon = Icons.Rounded.FormatBold,
                        tooltip = "Bold",
                        onClick = {
                            insertOrWrap("**", "**", "bold")
                        }
                    )
                    EditorToolbarButton(
                        icon = Icons.Rounded.FormatItalic,
                        tooltip = "Italic",
                        onClick = {
                            insertOrWrap("*", "*", "italic")
                        }
                    )
                    EditorToolbarButton(
                        icon = Icons.Rounded.FormatStrikethrough,
                        tooltip = "Strikethrough",
                        onClick = {
                            insertOrWrap("~~", "~~", "strikethrough")
                        }
                    )
                    EditorToolbarButton(
                        icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                        tooltip = "Bulleted List",
                        onClick = {
                            haptics.perform(HapticPattern.Tick)
                            textFieldState.edit {
                                val sel = selection
                                val start = minOf(sel.start, sel.end)
                                val end = maxOf(sel.start, sel.end)
                                val current = toString()
                                if (start == end) {
                                    val prefix = if (start > 0 && current[start - 1] != '\n') "\n- " else "- "
                                    replace(start, end, "${prefix}item")
                                    selection = TextRange(start + prefix.length, start + prefix.length + 4)
                                } else {
                                    val selectedText = current.substring(start, end)
                                    val list = selectedText.lines().joinToString("\n") { line -> if (line.startsWith("- ")) line else "- $line" }
                                    replace(start, end, list)
                                    selection = TextRange(start, start + list.length)
                                }
                            }
                        }
                    )
                    EditorToolbarButton(
                        text = "1.",
                        tooltip = "Numbered List",
                        onClick = {
                            haptics.perform(HapticPattern.Tick)
                            textFieldState.edit {
                                val sel = selection
                                val start = minOf(sel.start, sel.end)
                                val end = maxOf(sel.start, sel.end)
                                val current = toString()
                                if (start == end) {
                                    val prefix = if (start > 0 && current[start - 1] != '\n') "\n1. " else "1. "
                                    replace(start, end, "${prefix}item")
                                    selection = TextRange(start + prefix.length, start + prefix.length + 4)
                                } else {
                                    val selectedText = current.substring(start, end)
                                    var num = 1
                                    val list = selectedText.lines().joinToString("\n") { line -> "${num++}. $line" }
                                    replace(start, end, list)
                                    selection = TextRange(start, start + list.length)
                                }
                            }
                        }
                    )
                    EditorToolbarButton(
                        icon = Icons.Rounded.Checklist,
                        tooltip = "Checklist",
                        onClick = {
                            haptics.perform(HapticPattern.Tick)
                            textFieldState.edit {
                                val sel = selection
                                val start = minOf(sel.start, sel.end)
                                val end = maxOf(sel.start, sel.end)
                                val current = toString()
                                if (start == end) {
                                    val prefix = if (start > 0 && current[start - 1] != '\n') "\n- [ ] " else "- [ ] "
                                    replace(start, end, "${prefix}task")
                                    selection = TextRange(start + prefix.length, start + prefix.length + 4)
                                } else {
                                    val selectedText = current.substring(start, end)
                                    val list = selectedText.lines().joinToString("\n") { line -> "- [ ] $line" }
                                    replace(start, end, list)
                                    selection = TextRange(start, start + list.length)
                                }
                            }
                        }
                    )
                    EditorToolbarButton(
                        icon = Icons.Rounded.FormatQuote,
                        tooltip = "Quote",
                        onClick = {
                            haptics.perform(HapticPattern.Tick)
                            textFieldState.edit {
                                val sel = selection
                                val start = minOf(sel.start, sel.end)
                                val end = maxOf(sel.start, sel.end)
                                val current = toString()
                                if (start == end) {
                                    val prefix = if (start > 0 && current[start - 1] != '\n') "\n> " else "> "
                                    replace(start, end, prefix)
                                    selection = TextRange(start + prefix.length)
                                } else {
                                    val selectedText = current.substring(start, end)
                                    val quoted = selectedText.lines().joinToString("\n") { line -> "> $line" }
                                    replace(start, end, quoted)
                                    selection = TextRange(start, start + quoted.length)
                                }
                            }
                        }
                    )
                    EditorToolbarButton(
                        icon = Icons.Rounded.Code,
                        tooltip = "Code",
                        onClick = {
                            insertOrWrap("`", "`", "code")
                        }
                    )
                    EditorToolbarButton(
                        icon = Icons.Rounded.Terminal,
                        tooltip = "Code Block",
                        onClick = {
                            insertOrWrap("```\n", "\n```", "code")
                        }
                    )
                    EditorToolbarButton(
                        icon = Icons.Rounded.Link,
                        tooltip = "Link",
                        onClick = {
                            insertOrWrap("[", "](url)", "title")
                        }
                    )
                    EditorToolbarButton(
                        icon = Icons.Rounded.HorizontalRule,
                        tooltip = "Horizontal Rule",
                        onClick = {
                            haptics.perform(HapticPattern.Tick)
                            textFieldState.edit {
                                val sel = selection
                                val start = minOf(sel.start, sel.end)
                                val end = maxOf(sel.start, sel.end)
                                val current = toString()
                                val hr = if (start > 0 && current[start - 1] != '\n') "\n---\n" else "---\n"
                                replace(start, end, hr)
                                selection = TextRange(start + hr.length)
                            }
                        }
                    )

                    if (allowImageEmbed) {
                        EditorToolbarButton(
                            icon = Icons.Rounded.Image,
                            tooltip = "Embed Image",
                            onClick = {
                                haptics.perform(HapticPattern.Pop)
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }

                    if (showMacros) {
                        Spacer(Modifier.width(4.dp))
                        MacroPill(
                            label = "{{char}}",
                            onClick = {
                                insertTextAtCursor("{{char}}")
                            }
                        )
                        MacroPill(
                            label = "{{user}}",
                            onClick = {
                                insertTextAtCursor("{{user}}")
                            }
                        )
                    }
                }
            }
        }

        // Fixed-height stationary toolbar / preview header container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState == 1) {
                        (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { it / 3 } + fadeIn(tween(180))) togetherWith
                        (slideOutHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { -it / 3 } + fadeOut(tween(140)))
                    } else {
                        (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { -it / 3 } + fadeIn(tween(180))) togetherWith
                        (slideOutHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { it / 3 } + fadeOut(tween(140)))
                    }.using(
                        SizeTransform(clip = false) { _, _ -> snap() }
                    )
                },
                label = "markdown_editor_toolbar_switch"
            ) { tab ->
                if (tab == 0) {
                    MarkdownEditorToolbar()
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassSurface(
                                shape = RoundedCornerShape(12.dp),
                                borderWidth = 0.5.dp,
                                fillColor = Color(0xFF000000).copy(alpha = 0.40f)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.70f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Previewing Formatted Styles",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Text(
                                text = "Live Markdown AST",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }
        }

        // Editor Body with smooth sliding transition between Write and Preview
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState == 1) {
                    (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { it / 2 } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { -it / 2 } + fadeOut(tween(160)))
                } else {
                    (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { -it / 2 } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(spring(dampingRatio = 0.85f, stiffness = 300f)) { it / 2 } + fadeOut(tween(160)))
                }.using(
                    SizeTransform(clip = false) { _, _ -> snap() }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            label = "markdown_editor_content"
        ) { tab ->
            if (tab == 0) {
                OutlinedTextField(
                    state = textFieldState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = (minLines * 24).dp)
                        .onFocusChanged { isFocused = it.isFocused }
                        .liquidGlassSurface(
                            shape = AppShapes.CardLarge,
                            borderWidth = 0.5.dp,
                            fillColor = Color(0xFF000000).copy(alpha = 0.45f)
                        ),
                    lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = minLines),
                    colors = appFieldColors(),
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = Color.White.copy(alpha = 0.45f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = AppShapes.CardLarge,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = (minLines * 24).dp)
                        .liquidGlassSurface(
                            shape = AppShapes.CardLarge,
                            borderWidth = 0.5.dp,
                            fillColor = Color(0xFF000000).copy(alpha = 0.45f)
                        ),
                    shape = AppShapes.CardLarge,
                    color = Color.Transparent
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        val previewText = textFieldState.text.toString()
                        if (previewText.isNotBlank()) {
                            androidx.compose.runtime.key(previewText) {
                                MarkdownBlock(
                                    content = previewText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        lineHeight = 22.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Text(
                                text = "Nothing to preview yet. Switch to 'Write' to compose.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorToolbarButton(
    icon: ImageVector? = null,
    text: String? = null,
    tooltip: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(34.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = tooltip,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun MacroPill(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF000000).copy(alpha = 0.55f),
        border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
