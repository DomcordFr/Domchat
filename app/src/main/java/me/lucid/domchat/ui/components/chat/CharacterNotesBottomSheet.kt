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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor

fun formatRoleplayTextWithCdnImages(text: String): String {
    val imageRegex = Regex("""(?<!\()https?://\S+\.(?:png|jpg|jpeg|webp|gif)(?!\))""", RegexOption.IGNORE_CASE)
    return text.replace(imageRegex) { match ->
        "\n![Image](${match.value})\n"
    }
}

@Composable
fun CharacterNotesBottomSheet(
    assistant: me.lucid.domchat.data.model.Assistant,
    onDismissRequest: () -> Unit,
) {
    CharacterNotesBottomSheet(
        characterName = assistant.name,
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
    AetherInWindowBottomSheet(
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
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notes,
                        contentDescription = null,
                        tint = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = characterName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
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
            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Content with Text Selection Support and boundary overscroll protection
            SelectionContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .nestedScroll(overscrollAbsorber)
                    .verticalScroll(scrollState)
                    .padding(bottom = 32.dp)
            ) {
                val formattedNotes = formatRoleplayTextWithCdnImages(notes.ifBlank { "No creator notes provided." })
                MarkdownBlock(
                    content = formattedNotes,
                    paragraphSpacing = 12.dp
                )
            }
        }
    }
}
