package me.lucid.domchat.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import me.lucid.domchat.ui.modifier.fadeEdges
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import me.lucid.domchat.utils.truncateCreator
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lucid.domchat.ui.components.richtext.MarkdownBlock
import me.lucid.domchat.ui.components.richtext.SimpleHtmlBlock
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor

private val CDN_IMAGE_REGEX = Regex("""(?<!\()https?://\S+\.(?:png|jpg|jpeg|webp|gif)(?!\))""", RegexOption.IGNORE_CASE)
private val HTML_IMG_REGEX = Regex("""<img[^>]+src=['"]([^'"]+)['"][^>]*>""", RegexOption.IGNORE_CASE)

fun formatRoleplayTextWithCdnImages(text: String): String {
    if (!text.contains("http", ignoreCase = true)) return text
    return text.replace(CDN_IMAGE_REGEX) { match ->
        "\n![Image](${match.value})\n"
    }
}

/**
 * Converts HTML `` tags into Markdown image syntax `![alt](url)` so that
 * `MarkdownBlock` can render them as full-size in-place embedded images.
 * Non-img HTML is left as-is for the markdown renderer to handle.
 */
fun cleanHtmlImgToMarkdown(text: String): String {
    if (!text.contains("<img", ignoreCase = true)) return text
    return text.replace(HTML_IMG_REGEX) { match ->
        val url = match.groupValues[1]
        "\n![Image]($url)\n"
    }
}

@Composable
fun CharacterNotesBottomSheet(
    assistant: me.lucid.domchat.data.model.Assistant,
    onDismissRequest: () -> Unit,
) {
    CharacterNotesBottomSheet(
        characterName = assistant.displayName?.takeIf { it.isNotBlank() } ?: assistant.name.ifBlank { "Assistant" },
        creatorName = assistant.creator,
        characterVersion = assistant.characterVersion,
        notes = assistant.creatorNotes.orEmpty(),
        onDismissRequest = onDismissRequest,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterNotesBottomSheet(
    characterName: String,
    creatorName: String?,
    characterVersion: String?,
    notes: String,
    onDismissRequest: () -> Unit
) {
    val isDark = LocalDarkMode.current
    val scrollState = rememberScrollState()
    val canScrollUp by remember { derivedStateOf { scrollState.value > 0 } }
    val canScrollDown by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }

    // Absorb boundary overscrolls and fling velocity so ModalBottomSheet never freaks out, bounces, or jitters
    val overscrollAbsorber = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return available
            }
        }
    }

    val sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        shape = sheetShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notes,
                        contentDescription = null,
                        tint = me.lucid.domchat.ui.modifier.domChatSheetContentColor(),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = characterName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = me.lucid.domchat.ui.modifier.domChatSheetContentColor()
                    )

                    val meta = buildString {
                        if (!creatorName.isNullOrBlank()) append("By ${creatorName.truncateCreator(10)}")
                        if (!characterVersion.isNullOrBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append("v$characterVersion")
                        }
                    }

                    if (meta.isNotEmpty()) {
                        Text(
                            text = meta,
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFFA0A0A0) else Color(0xFF666666)
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = if (isDark) Color(0xFFA0A0A0) else Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E5E5),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Content with Text Selection Support, smooth edge fading, and boundary overscroll protection
            SelectionContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 40f)
                    .nestedScroll(overscrollAbsorber)
                    .verticalScroll(scrollState)
                    .padding(top = 6.dp, bottom = 32.dp)
            ) {
                val hasHtml = remember(notes) {
                    notes.contains("<p", ignoreCase = true) ||
                    notes.contains("<div", ignoreCase = true) ||
                    notes.contains("<img", ignoreCase = true) ||
                    notes.contains("<a", ignoreCase = true) ||
                    notes.contains("<span", ignoreCase = true) ||
                    notes.contains("<center", ignoreCase = true) ||
                    notes.contains("<br", ignoreCase = true)
                }
                if (hasHtml) {
                    SimpleHtmlBlock(html = notes)
                } else {
                    val formattedNotes = formatRoleplayTextWithCdnImages(notes.ifBlank { "No creator notes provided." })
                    MarkdownBlock(
                        content = formattedNotes,
                        paragraphSpacing = 12.dp
                    )
                }
            }
        }
    }
}
