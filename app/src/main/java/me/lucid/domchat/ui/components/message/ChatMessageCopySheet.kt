package me.lucid.domchat.ui.components.message

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalConfiguration
import me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle
import me.lucid.domchat.ui.components.chat.DomChatSheetHeader
import me.lucid.ai.ui.UIMessage
import me.lucid.ai.ui.UIMessagePart
import me.lucid.domchat.R
import me.lucid.domchat.utils.copyMessageToClipboard
import me.lucid.domchat.ui.theme.liquidGlassSurface
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.modifier.fadeEdges



@Composable
fun ChatMessageCopySheet(
    message: UIMessage,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        dragHandle = { DomChatSheetDragHandle() },
        maxHeight = screenHeight * 0.85f,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DomChatSheetHeader(
                title = stringResource(R.string.select_and_copy),
                subtitle = "Select text or copy message content",
                onCloseClick = onDismissRequest,
                actions = {
                    DomChatGlassPillButton(
                        text = stringResource(R.string.copy_all),
                        icon = Icons.Rounded.ContentCopy,
                        onClick = {
                            context.copyMessageToClipboard(message)
                            onDismissRequest()
                        }
                    )
                }
            )

            // Content
            val textParts =
                message.parts.filterIsInstance<UIMessagePart.Text>().filter { it.text.isNotBlank() }

            if (textParts.isEmpty()) {
                // No text content available
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_text_content_to_copy),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                SelectionContainer {
                    val textScrollState = rememberScrollState()
                    val canScrollUp by remember { derivedStateOf { textScrollState.canScrollBackward } }
                    val canScrollDown by remember { derivedStateOf { textScrollState.canScrollForward } }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassSurface(
                                shape = RoundedCornerShape(16.dp),
                                fillColor = Color.Transparent,
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fadeEdges(fadeTop = canScrollUp, fadeBottom = canScrollDown, fadeHeight = 24f)
                                .verticalScroll(textScrollState),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            textParts.fastForEach { textPart ->
                                Text(
                                    text = textPart.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.95f),
                                        lineHeight = 22.sp
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

