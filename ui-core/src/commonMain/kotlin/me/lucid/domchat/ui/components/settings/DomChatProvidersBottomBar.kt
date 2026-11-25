package me.lucid.domchat.ui.components.settings

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class DomChatProviderTab(
    val id: String,
    val icon: ImageVector,
)

/** Android's production compact Models/Search/TTS bottom navigation. */
@Composable
fun DomChatProvidersBottomBar(
    tabs: List<DomChatProviderTab>,
    selectedId: String,
    useWideLayout: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    onHaptic: () -> Unit = {},
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxWidth().navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        if (!useWideLayout) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF000000),
                border = BorderStroke(
                    0.5.dp,
                    Brush.verticalGradient(listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent,
                    ))
                ),
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    tabs.forEach { tab ->
                        val selected = tab.id == selectedId
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .then(
                                    if (selected) {
                                        Modifier
                                            .background(Color.White.copy(alpha = 0.18f))
                                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                    } else {
                                        Modifier.clickable {
                                            onHaptic()
                                            onSelect(tab.id)
                                        }
                                    },
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (selected) Color.White else Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = actions,
        )
    }
}
