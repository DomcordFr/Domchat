package me.lucid.domchat.ui.components.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.lucid.domchat.ui.theme.AppShapes

/** Android's production expandable drawer search, shared without platform resources. */
@Composable
fun DomChatDrawerSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    placeholder: String,
    hint: String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    shape: Shape = AppShapes.SearchField,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(query) {
        if (query.isNotEmpty()) onExpandedChange(true)
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .height(0.dp)
                .focusable(),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            val defaultContainerColor = if (isDark) Color(0xFF000000) else Color.White.copy(alpha = 0.70f)
            val searchContainerColor = if (containerColor != Color.Unspecified) containerColor else defaultContainerColor
            val searchBoardBrush = Brush.verticalGradient(
                // Mirrors LiquidGlassTokens.SpecularBorderSubtle (ui-core cannot import app tokens)
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.06f),
                    Color.Transparent,
                )
            )
            val borderModifier = if (fieldModifier != Modifier) Modifier else Modifier.border(BorderStroke(0.5.dp, searchBoardBrush), shape)

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(fieldModifier)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !expanded) onExpandedChange(true)
                    }
                    .then(borderModifier),
                shape = shape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = searchContainerColor,
                    unfocusedContainerColor = searchContainerColor,
                    disabledContainerColor = searchContainerColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                leadingIcon = {
                    Icon(
                        imageVector = RoundedSearchIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingIcon = if (expanded || query.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onQueryChange("")
                                onExpandedChange(false)
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = RoundedCloseIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                } else null,
                placeholder = { Text(placeholder) },
                singleLine = true,
            )

            AnimatedVisibility(visible = expanded || query.isNotBlank()) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            delay(100)
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
                // The drawer may have left composition during the focus delay.
            }
        }
    }
}

private val RoundedCloseIcon by lazy {
    ImageVector.Builder(
        name = "Rounded.Close",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M18.3,5.71c-0.39,-0.39 -1.02,-0.39 -1.41,0L12,10.59L7.11,5.7c-0.39,-0.39 -1.02,-0.39 -1.41,0c-0.39,0.39 -0.39,1.02 0,1.41L10.59,12L5.7,16.89c-0.39,0.39 -0.39,1.02 0,1.41c0.39,0.39 1.02,0.39 1.41,0L12,13.41l4.89,4.89c0.39,0.39 1.02,0.39 1.41,0c0.39,-0.39 0.39,-1.02 0,-1.41L13.41,12l4.89,-4.89c0.38,-0.38 0.38,-1.02 0,-1.4z",
            ).toNodes(),
            fill = SolidColor(Color.Black),
        )
    }.build()
}

private val RoundedSearchIcon by lazy {
    ImageVector.Builder(
        name = "Rounded.Search",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zm-6,0C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z",
            ).toNodes(),
            fill = SolidColor(Color.Black),
        )
    }.build()
}
