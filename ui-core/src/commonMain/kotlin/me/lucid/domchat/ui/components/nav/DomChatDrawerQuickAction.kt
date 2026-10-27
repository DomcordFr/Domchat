package me.lucid.domchat.ui.components.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Exact grouped container used by the production drawer quick actions. */
@Composable
fun AetherDrawerQuickActionGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

/** Exact production drawer quick-action row; navigation and haptics stay platform-owned. */
@Composable
fun AetherDrawerQuickAction(
    label: String,
    onClick: () -> Unit,
    onHaptic: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val defaultBg = if (isDark) Color(0xFF141520).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.70f)
    val cardBg = if (containerColor != Color.Unspecified) containerColor else defaultBg
    val cardShape = RoundedCornerShape(12.dp)
    Surface(
        onClick = {
            onHaptic()
            onClick()
        },
        color = cardBg,
        contentColor = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurface,
        shape = cardShape,
        border = BorderStroke(
            0.5.dp,
            if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** Material Rounded BarChart from the Android production icon artifact. */
@Composable
fun DomChatBarChartIcon(
    contentDescription: String?,
    tint: Color = Color.Unspecified,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val resolvedTint = if (tint != Color.Unspecified) tint else if (isDark) Color(0xFFF2F2F7) else MaterialTheme.colorScheme.onSurfaceVariant
    Icon(
        imageVector = RoundedBarChartIcon,
        contentDescription = contentDescription,
        modifier = Modifier.size(24.dp),
        tint = resolvedTint,
    )
}

private val RoundedBarChartIcon by lazy {
    ImageVector.Builder(
        name = "Rounded.BarChart",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M6,20c1.1,0 2,-0.9 2,-2v-7c0,-1.1 -0.9,-2 -2,-2s-2,0.9 -2,2v7c0,1.1 0.9,2 2,2z" +
                    "M18,20c1.1,0 2,-0.9 2,-2v-3c0,-1.1 -0.9,-2 -2,-2s-2,0.9 -2,2v3c0,1.1 0.9,2 2,2z" +
                    "M12,20c1.1,0 2,-0.9 2,-2V6c0,-1.1 -0.9,-2 -2,-2s-2,0.9 -2,2v12c0,1.1 0.9,2 2,2z",
            ).toNodes(),
            fill = SolidColor(Color.Black),
        )
    }.build()
}
