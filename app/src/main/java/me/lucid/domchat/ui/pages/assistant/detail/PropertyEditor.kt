package me.lucid.domchat.ui.pages.assistant.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.theme.LocalDarkMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import me.lucid.ai.provider.CustomBody
import me.lucid.ai.provider.CustomHeader
import me.lucid.highlight.LocalHighlighter
import me.lucid.domchat.R
import me.lucid.domchat.ui.components.richtext.rememberHighlightCodeVisualTransformation
import androidx.compose.ui.text.font.FontFamily
import me.lucid.domchat.ui.theme.LocalDarkMode

private val jsonLenient = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
}

@Composable
fun CustomHeaders(headers: List<CustomHeader>, onUpdate: (List<CustomHeader>) -> Unit) {
    val isDark = LocalDarkMode.current
    val cardShape = RoundedCornerShape(18.dp)
    val inputShape = RoundedCornerShape(12.dp)
    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.22f else 0.12f),
            Color.White.copy(alpha = if (isDark) 0.06f else 0.03f)
        )
    )

    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.assistant_page_custom_headers))
        Spacer(Modifier.height(8.dp))

        headers.forEachIndexed { index, header ->
            var headerName by remember(header.name) { mutableStateOf(header.name) }
            var headerValue by remember(header.value) { mutableStateOf(header.value) }

            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = domChatCardContainerColor()),
                border = BorderStroke(0.5.dp, borderBrush)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = headerName,
                            onValueChange = {
                                headerName = it
                                val updatedHeaders = headers.toMutableList()
                                updatedHeaders[index] = updatedHeaders[index].copy(name = it.trim())
                                onUpdate(updatedHeaders)
                            },
                            label = { Text(stringResource(R.string.assistant_page_header_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                                unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
                                focusedBorderColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = headerValue,
                            onValueChange = {
                                headerValue = it
                                val updatedHeaders = headers.toMutableList()
                                updatedHeaders[index] =
                                    updatedHeaders[index].copy(value = it.trim())
                                onUpdate(updatedHeaders)
                            },
                            label = { Text(stringResource(R.string.assistant_page_header_value)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                                unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
                                focusedBorderColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                            )
                        )
                    }
                    IconButton(onClick = {
                        val updatedHeaders = headers.toMutableList()
                        updatedHeaders.removeAt(index)
                        onUpdate(updatedHeaders)
                    }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.assistant_page_delete_header)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                val updatedHeaders = headers.toMutableList()
                updatedHeaders.add(CustomHeader("", ""))
                onUpdate(updatedHeaders)
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color.White else Color.Black,
                contentColor = if (isDark) Color.Black else Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.assistant_page_add_header))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.assistant_page_add_header))
        }
    }
}

@Composable
fun CustomBodies(customBodies: List<CustomBody>, onUpdate: (List<CustomBody>) -> Unit) {
    val context = LocalContext.current
    val isDark = LocalDarkMode.current
    val cardShape = RoundedCornerShape(18.dp)
    val inputShape = RoundedCornerShape(12.dp)
    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.22f else 0.12f),
            Color.White.copy(alpha = if (isDark) 0.06f else 0.03f)
        )
    )

    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.assistant_page_custom_bodies))
        Spacer(Modifier.height(8.dp))

        customBodies.forEachIndexed { index, body ->
            var bodyKey by remember(body.key) { mutableStateOf(body.key) }
            var bodyValueString by remember(body.value) {
                mutableStateOf(jsonLenient.encodeToString(JsonElement.serializer(), body.value))
            }
            var jsonParseError by remember { mutableStateOf<String?>(null) }

            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = domChatCardContainerColor()),
                border = BorderStroke(0.5.dp, borderBrush)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = bodyKey,
                            onValueChange = {
                                bodyKey = it
                                val updatedBodies = customBodies.toMutableList()
                                updatedBodies[index] = updatedBodies[index].copy(key = it.trim())
                                onUpdate(updatedBodies)
                            },
                            label = { Text(stringResource(R.string.assistant_page_body_key)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = inputShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                                unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
                                focusedBorderColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bodyValueString,
                            onValueChange = { newString ->
                                bodyValueString = newString
                                try {
                                    val newJsonValue = jsonLenient.parseToJsonElement(newString)
                                    val updatedBodies = customBodies.toMutableList()
                                    updatedBodies[index] =
                                        updatedBodies[index].copy(value = newJsonValue)
                                    onUpdate(updatedBodies)
                                    jsonParseError = null // Clear error on successful parse
                                } catch (e: Exception) { // Catching general Exception, JsonException is common here
                                    jsonParseError =
                                        context.getString(
                                            R.string.assistant_page_invalid_json,
                                            e.message?.take(100) ?: ""
                                        ) // Truncate for very long messages
                                }
                            },
                            label = { Text(stringResource(R.string.assistant_page_body_value)) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = jsonParseError != null,
                            supportingText = {
                                if (jsonParseError != null) {
                                    Text(jsonParseError!!)
                                }
                            },
                            minLines = 3,
                            maxLines = 5,
                            visualTransformation = rememberHighlightCodeVisualTransformation(
                                language = "json",
                                code = bodyValueString
                            ),
                            textStyle = LocalTextStyle.current.merge(fontFamily = FontFamily.Monospace),
                            shape = inputShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                                unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
                                focusedBorderColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                            )
                        )
                    }
                    IconButton(onClick = {
                        val updatedBodies = customBodies.toMutableList()
                        updatedBodies.removeAt(index)
                        onUpdate(updatedBodies)
                    }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.assistant_page_delete_body)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                val updatedBodies = customBodies.toMutableList()
                updatedBodies.add(CustomBody("", JsonPrimitive("")))
                onUpdate(updatedBodies)
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color.White else Color.Black,
                contentColor = if (isDark) Color.Black else Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.assistant_page_add_body))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.assistant_page_add_body))
        }
    }
}
