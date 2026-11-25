package me.lucid.domchat.ui.components.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.crossfade
import me.lucid.domchat.R
import me.lucid.domchat.utils.safeDrawableResId
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.ui.hooks.avatarCounterSpin
import me.lucid.domchat.ui.hooks.avatarLoadingSpin
import me.lucid.domchat.ui.hooks.rememberAvatarLoadingRotation
import me.lucid.domchat.ui.hooks.rememberAvatarShape
import me.lucid.domchat.utils.createChatFilesByContents

@Composable
fun TextAvatar(
    text: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    color: Color = Color(0xFF000000),
    contentColor: Color = MaterialTheme.colorScheme.onSecondary
) {
    val rotation = if (loading) rememberAvatarLoadingRotation(true) else null
    val spinModifier = if (rotation != null) Modifier.avatarLoadingSpin(rotation) else Modifier
    val counterSpinModifier = if (rotation != null) Modifier.avatarCounterSpin(rotation) else Modifier
    Box(
        modifier = modifier
            .then(spinModifier)
            .clip(shape = rememberAvatarShape(loading))
            .background(color)
            .size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.take(1).uppercase(),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = 32.sp,
                stepSize = 1.sp
            ),
            lineHeight = 0.8.em,
            modifier = counterSpinModifier
        )
    }
}

@Composable
private fun AvatarContent(
    name: String = "",
    value: Avatar = Avatar.Dummy,
    rotation: Animatable<Float, AnimationVector1D>? = null,
) {
    val counterSpinModifier = if (rotation != null) Modifier.avatarCounterSpin(rotation) else Modifier
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .then(counterSpinModifier)
    ) {
        when (value) {
            is Avatar.Image -> {
                val context = LocalContext.current
                val avatarRequest = remember(value.url, context) {
                    coil3.request.ImageRequest.Builder(context)
                        .data(value.url)
                        .crossfade(false)
                        .build()
                }
                AsyncImage(
                    model = avatarRequest,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            is Avatar.Emoji -> {
                Text(
                    text = value.content,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 15.sp,
                        maxFontSize = 30.sp,
                    ),
                    lineHeight = 1.em,
                    modifier = Modifier.padding(2.dp)
                )
            }

            is Avatar.Resource -> {
                val context = LocalContext.current
                val safeId = remember(value.id, context) {
                    safeDrawableResId(context, value.id, me.lucid.domchat.R.drawable.default_generical_pfp)
                }
                AsyncImage(
                    model = safeId,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            is Avatar.Dummy -> {
                Text(
                    text = name
                        .ifBlank { stringResource(R.string.user_default_name) }
                        .takeIf { it.isNotEmpty() }
                        ?.firstOrNull()?.toString()?.uppercase() ?: "A",
                    fontSize = 20.sp,
                    lineHeight = 1.em
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyUIAvatar(
    name: String = "",
    value: Avatar = Avatar.Dummy,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val rotation = if (loading) rememberAvatarLoadingRotation(true) else null
    val shape = rememberAvatarShape(loading)

    val spinModifier = if (rotation != null) Modifier.avatarLoadingSpin(rotation) else Modifier
    if (onClick != null) {
        Surface(
            shape = shape,
            modifier = modifier
                .size(32.dp)
                .then(spinModifier),
            onClick = onClick,
            tonalElevation = 4.dp,
            color = Color(0xFF000000),
        ) {
            AvatarContent(name = name, value = value, rotation = rotation)
        }
    } else {
        Surface(
            shape = shape,
            modifier = modifier
                .size(32.dp)
                .then(spinModifier),
            tonalElevation = 4.dp,
            color = Color(0xFF000000),
        ) {
            AvatarContent(name = name, value = value, rotation = rotation)
        }
    }
}

@Composable
private fun EditableUIAvatar(
    name: String = "",
    value: Avatar = Avatar.Dummy,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onUpdate: (Avatar) -> Unit = {},
    onClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var showPickOption by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showUrlInput by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }

    // State for square cropper
    var showCropper by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Show the cropper instead of directly using the image
            selectedImageUri = it
            showCropper = true
        }
    }

    val rotation = if (loading) rememberAvatarLoadingRotation(true) else null
    val shape = rememberAvatarShape(loading)
    val spinModifier = if (rotation != null) Modifier.avatarLoadingSpin(rotation) else Modifier

    Surface(
        shape = shape,
        modifier = modifier
            .size(32.dp)
            .then(spinModifier),
        onClick = {
            onClick?.invoke()
            showPickOption = true
        },
        tonalElevation = 4.dp,
        color = Color(0xFF000000),
    ) {
        AvatarContent(name = name, value = value, rotation = rotation)
    }

    if (showPickOption) {
        AlertDialog(
            containerColor = me.lucid.domchat.ui.modifier.domChatDialogContainerColor(),
            onDismissRequest = {
                showPickOption = false
            },
            title = {
                me.lucid.domchat.ui.modifier.ApplyDialogWindowBlur()
                Text(text = stringResource(id = R.string.avatar_change_avatar))
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showPickOption = false
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.avatar_pick_image))
                    }
                    Button(
                        onClick = {
                            showPickOption = false
                            showEmojiPicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.avatar_pick_emoji))
                    }
                    Button(
                        onClick = {
                            showPickOption = false
                            urlInput = ""
                            showUrlInput = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.avatar_input_url))
                    }
                    Button(
                        onClick = {
                            showPickOption = false
                            if (name == "Generical") {
                                onUpdate?.invoke(Avatar.Resource(me.lucid.domchat.R.drawable.default_generical_pfp))
                            } else {
                                onUpdate?.invoke(Avatar.Dummy)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.avatar_reset))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPickOption = false
                    }
                ) {
                    Text(stringResource(id = R.string.avatar_cancel))
                }
            }
        )
    }

    if (showEmojiPicker) {
        me.lucid.domchat.ui.components.chat.DomChatInWindowBottomSheet(
            visible = true,
            containerColor = me.lucid.domchat.ui.modifier.domChatSheetContainerColor(),
            onDismissRequest = {
                showEmojiPicker = false
            },
            dragHandle = {
                me.lucid.domchat.ui.components.chat.DomChatSheetDragHandle()
            }
        ) {
            EmojiPicker(
                onEmojiSelected = { emoji ->
                    onUpdate?.invoke(Avatar.Emoji(content = emoji.emoji))
                    showEmojiPicker = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
            )
        }
    }

    if (showUrlInput) {
        AlertDialog(
            containerColor = me.lucid.domchat.ui.modifier.domChatDialogContainerColor(),
            onDismissRequest = {
                showUrlInput = false
            },
            title = {
                Text(text = stringResource(id = R.string.avatar_url_dialog_title))
            },
            text = {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text(stringResource(id = R.string.avatar_url_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = me.lucid.domchat.ui.theme.AppShapes.InputField
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            onUpdate?.invoke(Avatar.Image(urlInput.trim()))
                            showUrlInput = false
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.avatar_url_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUrlInput = false
                    }
                ) {
                    Text(stringResource(id = R.string.avatar_cancel))
                }
            }
        )
    }
    
    // Square cropper dialog
    if (showCropper && selectedImageUri != null) {
        me.lucid.domchat.ui.components.crop.SquareCropImageScreen(
            sourceUri = selectedImageUri!!,
            onCropComplete = { croppedUri ->
                showCropper = false
                selectedImageUri = null
                onUpdate(Avatar.Image(croppedUri.toString()))
            },
            onCancel = {
                showCropper = false
                selectedImageUri = null
            }
        )
    }
}

@Composable
fun UIAvatar(
    name: String = "",
    value: Avatar = Avatar.Dummy,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onUpdate: ((Avatar) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    if (onUpdate == null) {
        ReadOnlyUIAvatar(
            name = name,
            value = value,
            modifier = modifier,
            loading = loading,
            onClick = onClick
        )
    } else {
        EditableUIAvatar(
            name = name,
            value = value,
            modifier = modifier,
            loading = loading,
            onUpdate = onUpdate,
            onClick = onClick
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewUIAvatar() {
    var loading by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UIAvatar(
            name = "John Doe",
            value = Avatar.Dummy,
            loading = false
        )

        UIAvatar(
            name = "John Doe",
            value = Avatar.Dummy,
            loading = loading,
        )

        Button(
            onClick = {
                loading = !loading
            }
        ) {
            Text("Toggle Loading")
        }
    }
}
