package me.lucid.domchat.ui.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import me.lucid.domchat.Screen
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.context.LocalToaster
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.domchat.data.datastore.withActivePersona
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.modifier.domChatCardContainerColor
import me.lucid.domchat.ui.modifier.domChatDialogContainerColor
import me.lucid.domchat.ui.modifier.domChatSheetContainerColor
import me.lucid.domchat.data.model.UserPersona
import me.lucid.domchat.ui.components.ui.UIAvatar
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberAmoledDarkMode
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.data.model.LiquidGlassMode
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.frostedGlassSurface
import me.lucid.domchat.ui.theme.liquidGlassSurface
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaPickerBottomSheet(
    settings: Settings,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settingsStore: SettingsStore = koinInject()
    val scope = rememberCoroutineScope()
    val haptics = rememberPremiumHaptics(enabled = settings.displaySetting.enableUIHaptics)
    val amoledMode by rememberAmoledDarkMode()
    val isDark = LocalDarkMode.current
    val navController = LocalNavController.current
    val toaster = LocalToaster.current

    val personaIsFrosted = settings.displaySetting.liquidGlassMode == LiquidGlassMode.SOFT_FROSTED
    @Composable
    fun personaGlassCard(shape: Shape, isActive: Boolean) = if (personaIsFrosted) {
        Modifier.frostedGlassSurface(
            shape = shape,
            borderWidth = if (isActive) 1.dp else 0.5.dp,
            specularBrush = if (isActive) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle,
            fillColor = Color.Transparent,
        )
    } else {
        Modifier.liquidGlassSurface(
            shape = shape,
            borderWidth = if (isActive) 1.dp else 0.5.dp,
            specularBrush = if (isActive) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle,
            fillColor = Color.Transparent,
            glassMode = LiquidGlassMode.BALANCED_BLEND,
        )
    }

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

    val currentPersonas = remember(settings.displaySetting.personas, settings.displaySetting.userNickname) {
        if (settings.displaySetting.personas.isEmpty()) {
            listOf(
                UserPersona(
                    id = "default",
                    name = settings.displaySetting.userNickname.ifBlank { "User" },
                    description = "",
                    avatar = settings.displaySetting.userAvatar,
                    isDefault = true
                )
            )
        } else {
            settings.displaySetting.personas
        }
    }

    val activePersona = settings.displaySetting.getActivePersona()
    var personaToDelete by remember { mutableStateOf<UserPersona?>(null) }

    fun deletePersona(id: String) {
        scope.launch {
            settingsStore.update { cur ->
                val remaining = cur.displaySetting.personas.filterNot { it.id == id }
                val fallbackId = remaining.firstOrNull()?.id
                cur.copy(
                    displaySetting = cur.displaySetting.copy(
                        personas = remaining,
                        activePersonaId = if (cur.displaySetting.activePersonaId == id) fallbackId else cur.displaySetting.activePersonaId
                    )
                )
            }
        }
    }

    fun selectPersona(id: String) {
        if (id == activePersona.id) return
        haptics.perform(HapticPattern.Pop)
        val oldName = activePersona.name.ifBlank { "Default" }
        val newPersona = currentPersonas.find { it.id == id }
        val newName = newPersona?.name?.ifBlank { "Default" } ?: "Default"
        scope.launch {
            settingsStore.update { cur ->
                cur.copy(
                    displaySetting = cur.displaySetting.withActivePersona(id)
                )
            }
            toaster.show("Switched persona from $oldName to $newName")
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    val filteredPersonas = remember(currentPersonas, searchQuery) {
        if (searchQuery.isBlank()) currentPersonas
        else currentPersonas.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    DomChatInWindowBottomSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            DomChatSheetHeader(
                title = "Select Persona",
                actions = {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .liquidGlassSurface(
                                shape = CircleShape,
                                borderWidth = 0.5.dp,
                                specularBrush = LiquidGlassTokens.SpecularBorderSubtle,
                                isFloatingControl = true,
                            )
                            .clip(CircleShape)
                            .clickable {
                                haptics.perform(HapticPattern.Pop)
                                onDismissRequest()
                                navController.navigate(Screen.SettingPersonas)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "New Persona",
                            tint = Color.White.copy(alpha = 0.92f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                DomChatSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search personas...",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Personas List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .nestedScroll(overscrollAbsorber)
            ) {
                items(filteredPersonas, key = { it.id }) { persona ->
                    val isActive = persona.id == activePersona.id || (persona.isDefault && activePersona.id == null)
                    val cardShape = RoundedCornerShape(18.dp)

                    Surface(
                        onClick = { selectPersona(persona.id) },
                        color = Color.Transparent,
                        shape = cardShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .then(personaGlassCard(cardShape, isActive))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UIAvatar(
                                name = persona.name,
                                value = persona.avatar,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = persona.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (persona.pronouns.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Transparent,
                                            modifier = Modifier.liquidGlassSurface(
                                                shape = RoundedCornerShape(6.dp),
                                                borderWidth = 0.5.dp,
                                                specularBrush = LiquidGlassTokens.SpecularBorderBrush,
                                                fillColor = Color.Transparent
                                            )
                                        ) {
                                            Text(
                                                text = persona.pronouns,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = isActive,
                                        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.8f, animationSpec = tween(180)),
                                        exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.8f, animationSpec = tween(140))
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Transparent,
                                            modifier = Modifier.liquidGlassSurface(
                                                shape = RoundedCornerShape(6.dp),
                                                borderWidth = 0.5.dp,
                                                specularBrush = LiquidGlassTokens.SpecularBorderBrush,
                                                fillColor = Color.Transparent
                                            )
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (persona.description.isNotBlank()) {
                                    Text(
                                        text = persona.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.70f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        haptics.perform(HapticPattern.Pop)
                                        onDismissRequest()
                                        navController.navigate(Screen.SettingPersonas)
                                    },
                                    shape = CircleShape,
                                    color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                                    border = BorderStroke(0.5.dp, if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "Edit",
                                            tint = if (isDark) Color(0xFFD1D1D8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                if (currentPersonas.size > 1) {
                                    Surface(
                                        onClick = {
                                            haptics.perform(HapticPattern.Pop)
                                            personaToDelete = persona
                                        },
                                        shape = CircleShape,
                                        color = Color(0xFFE53935).copy(alpha = 0.18f),
                                        border = BorderStroke(0.5.dp, Color(0xFFE53935).copy(alpha = 0.40f)),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFFF6B6B),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isActive,
                                    enter = fadeIn(tween(180)) + scaleIn(tween(180)),
                                    exit = fadeOut(tween(140)) + scaleOut(tween(140))
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Persona delete confirmation dialog (glassy black, destructive)
    personaToDelete?.let { persona ->
        AlertDialog(
            onDismissRequest = { personaToDelete = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .widthIn(max = 320.dp)
                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, AppShapes.Dialog),
            shape = AppShapes.Dialog,
            containerColor = domChatDialogContainerColor(),
            title = {
                ApplyDialogWindowBlur()
                Text(
                    text = "Delete ${persona.name}?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This persona will be permanently removed from your identity list.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        deletePersona(persona.id)
                        personaToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { personaToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
