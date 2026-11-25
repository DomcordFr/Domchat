package me.lucid.domchat.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil3.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.image.pager.ImagePager
import com.jvziyaoyao.scale.zoomable.pager.rememberZoomablePagerState
import kotlinx.coroutines.launch
import me.lucid.domchat.R
import me.lucid.domchat.ui.theme.LocalDarkMode
import me.lucid.domchat.utils.saveToDownloads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import coil3.request.allowHardware
import coil3.request.crossfade

@Composable
fun ImagePreviewDialog(
    images: List<String>,
    onDismissRequest: () -> Unit,
    prompt: String? = null, // Optional prompt to display
) {
    val context = LocalContext.current
    val state = rememberZoomablePagerState { images.size }
    val scope = rememberCoroutineScope()
    val isDarkMode = LocalDarkMode.current
    
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        )
    ) {
        // Full screen black background in dark mode
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkMode) Color.Black else Color.Black.copy(alpha = 0.95f))
        ) {
            // Image container - perfectly vertically centered
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ImagePager(
                    modifier = Modifier.fillMaxSize(),
                    pagerState = state,
                    imageLoader = { index ->
                        val context = LocalContext.current
                        val request = remember(images[index], context) {
                            coil3.request.ImageRequest.Builder(context)
                                .data(images[index])
                                .crossfade(true)
                                .allowHardware(true)
                                .apply {
                                    if (images[index].startsWith("data:")) {
                                        memoryCacheKey(images[index].take(200))
                                    }
                                }
                                .build()
                        }
                        val painter = rememberAsyncImagePainter(request)
                        return@ImagePager Pair(painter, painter.intrinsicSize)
                    },
                )
            }

            // Close button top-end
            Surface(
                onClick = onDismissRequest,
                shape = CircleShape,
                color = Color(0xFF000000),
                border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
                    .align(Alignment.TopEnd)
                    .size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Bottom controls: Prompt text and save button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Prompt text (if provided)
                    if (!prompt.isNullOrBlank()) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Save button row - aligned to the right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            onClick = {
                                scope.launch {
                                    val imgUrl = images[state.currentPage]
                                    val uri = imgUrl.toUri()
                                    // Generate filename from URI or use timestamp
                                    val fileName = uri.lastPathSegment?.let { 
                                        if (it.contains('.')) it else "image_${System.currentTimeMillis()}.png"
                                    } ?: "image_${System.currentTimeMillis()}.png"
                                    // Use saveToDownloads - same as text link downloads
                                    // Shows native Android Toast which is visible over dialogs
                                    context.saveToDownloads(uri, fileName)
                                    onDismissRequest()
                                }
                            },
                            shape = CircleShape,
                            color = Color(0xFF000000),
                            border = BorderStroke(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Download,
                                    contentDescription = stringResource(R.string.save),
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
