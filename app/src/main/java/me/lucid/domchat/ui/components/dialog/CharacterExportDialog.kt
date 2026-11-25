package me.lucid.domchat.ui.components.dialog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.ui.components.ui.DomChatGlassPillButton
import me.lucid.domchat.ui.components.ui.ToastType
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.utils.AssistantExportImport

@Composable
fun CharacterExportDialog(
    assistant: Assistant,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    var pendingExportBytes by remember { mutableStateOf<ByteArray?>(null) }
    var pendingExportJson by remember { mutableStateOf("") }

    val exportPngLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri != null) {
            val bytes = pendingExportBytes
            if (bytes == null) {
                toaster.show("Export failed: no image data", ToastType.Error)
            } else {
                scope.launch {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { 
                            it.write(bytes)
                        }
                        toaster.show("Character PNG Card exported successfully")
                    } catch (e: Exception) {
                        toaster.show("Export failed: ${e.message}", ToastType.Error)
                    }
                    pendingExportBytes = null
                }
            }
        }
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { 
                        it.write(pendingExportJson.toByteArray(Charsets.UTF_8))
                    }
                    toaster.show("Character Card V2 exported successfully")
                } catch (e: Exception) {
                    toaster.show("Export failed: ${e.message}", ToastType.Error)
                }
                pendingExportJson = ""
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = AppShapes.Dialog,
        containerColor = Color(0xFF000000),
        modifier = Modifier.border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, AppShapes.Dialog),
        title = {
            me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
            Text(
                text = "Export ${assistant.name}",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Choose an export format for this character:",
                    fontSize = 13.sp,
                    color = Color(0xFFAAAAAA)
                )

                // Option 1: Character Card PNG
                Surface(
                    onClick = {
                        scope.launch {
                            val bytes = withContext(Dispatchers.IO) {
                                AssistantExportImport.exportToCharacterCardPng(assistant, context)
                            }
                            if (bytes != null) {
                                pendingExportBytes = bytes
                                exportPngLauncher.launch("${assistant.name.replace(" ", "_")}.png")
                            } else {
                                toaster.show("Failed to create PNG card with avatar", ToastType.Error)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF000000),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF000000), CircleShape)
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Character Card",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF000000), RoundedCornerShape(6.dp))
                                        .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PNG",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Embedded avatar card",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9EAA)
                            )
                        }
                    }
                }

                // Option 2: Character Card V2 JSON
                Surface(
                    onClick = {
                        scope.launch {
                            val jsonStr = withContext(Dispatchers.IO) {
                                AssistantExportImport.exportToCharacterCardV2(assistant, context)
                            }
                            pendingExportJson = jsonStr
                            exportJsonLauncher.launch("${assistant.name.replace(" ", "_")}.json")
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF000000),
                    border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF000000), CircleShape)
                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Character Card V2",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF000000), RoundedCornerShape(6.dp))
                                        .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "JSON",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Standard V2 JSON spec",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9EAA)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Surface(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF000000),
                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    )
}
