package me.lucid.domchat.ui.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.lucid.domchat.R
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode

@Composable
fun SummarizerModelTipBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEmbedded: Boolean = false,
) {
    val bannerShape = RoundedCornerShape(16.dp)
    val isDark = LocalDarkMode.current
    val borderBrush = LiquidGlassTokens.SpecularBorderSubtle

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = bannerShape,
        border = null,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.TipsAndUpdates,
                    contentDescription = null,
                    tint = if (isDark) Color.White else Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = stringResource(R.string.summarizer_model_tip_banner),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color(0xFFEEEEEE) else Color.White,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
