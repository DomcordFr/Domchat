package me.lucid.domchat.ui.components.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.lucid.domchat.R
import me.lucid.domchat.ui.components.ui.icons.ModeIcons
import androidx.compose.ui.graphics.Color

@Composable
fun MaterialIconPickerDialog(
    onDismiss: () -> Unit,
    onIconSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredIcons = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ModeIcons.AllIcons.toList()
        } else {
            ModeIcons.AllIcons.toList().filter { (name, _) ->
                name.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    AlertDialog(
        containerColor = me.lucid.domchat.ui.modifier.domChatDialogContainerColor(),
        onDismissRequest = onDismiss,
        title = {
    me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
    Text(stringResource(R.string.setting_provider_page_select_icon)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.icon_picker_search_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(me.lucid.domchat.ui.theme.AppShapes.SearchField)
                        .border(0.5.dp, me.lucid.domchat.ui.theme.LiquidGlassTokens.SpecularBorderSubtle, me.lucid.domchat.ui.theme.AppShapes.SearchField),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, null)
                            }
                        }
                    } else null,
                    shape = me.lucid.domchat.ui.theme.AppShapes.SearchField,
                    colors = me.lucid.domchat.ui.theme.appFieldColors()
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredIcons, key = { it.first }) { (name, icon) ->
                        Surface(
                            onClick = { onIconSelected(name) },
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF000000)
                        ) {
                            androidx.compose.foundation.layout.Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
