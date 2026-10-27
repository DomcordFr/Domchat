package me.lucid.domchat.ui.components.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Android's production model-feature settings card. */
@Composable
fun DomChatModelFeatureCard(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    description: @Composable () -> Unit = {},
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    val cardShape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier.then(
            if (darkTheme) {
                Modifier.border(
                    0.5.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    ),
                    cardShape
                )
            } else {
                Modifier.border(
                    0.5.dp,
                    Color.Black.copy(alpha = 0.06f),
                    cardShape
                )
            }
        ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) {
                Color(0xFF0F111A).copy(alpha = 0.88f)
            } else {
                Color.White.copy(alpha = 0.90f)
            },
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (darkTheme) Color.White.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    ProvideTextStyle(
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    ) { title() }
                    ProvideTextStyle(
                        MaterialTheme.typography.bodySmall.copy(
                            color = LocalContentColor.current.copy(alpha = 0.72f),
                        ),
                    ) { description() }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}

enum class DomChatModelGroupPosition { First, Middle, Last, Single }

/** Android's grouped model-picker row, including selected-pill corner animation. */
@Composable
fun DomChatGroupedModelRow(
    title: String,
    selected: Boolean,
    position: DomChatModelGroupPosition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit,
    metadata: @Composable () -> Unit = {},
    tail: @Composable RowScope.() -> Unit = {},
    dragHandle: @Composable (RowScope.() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val target = groupedCornerRadii(selected, position)
    val topStart by animateDpAsState(
        target.topStart,
        spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "model_item_top_start",
    )
    val topEnd by animateDpAsState(
        target.topEnd,
        spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "model_item_top_end",
    )
    val bottomStart by animateDpAsState(
        target.bottomStart,
        spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "model_item_bottom_start",
    )
    val bottomEnd by animateDpAsState(
        target.bottomEnd,
        spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "model_item_bottom_end",
    )
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val rowShape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val rowBackground = if (selected) {
        if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.12f)
    } else {
        if (isDark) Color(0xFF1E2029).copy(alpha = 0.55f) else Color(0xFFE8E9F0).copy(alpha = 0.55f)
    }
    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (selected) 0.38f else 0.14f),
            Color.White.copy(alpha = if (selected) 0.14f else 0.04f)
        )
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(rowShape)
            .border(BorderStroke(if (selected) 1.dp else 0.5.dp, borderBrush), rowShape)
            .background(rowBackground)
            .padding(vertical = 12.dp, horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            icon()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor,
                )
                metadata()
            }
            tail()
        }
        dragHandle?.invoke(this)
    }
}

private data class ModelCornerRadii(
    val topStart: androidx.compose.ui.unit.Dp,
    val topEnd: androidx.compose.ui.unit.Dp,
    val bottomStart: androidx.compose.ui.unit.Dp,
    val bottomEnd: androidx.compose.ui.unit.Dp,
)

private fun groupedCornerRadii(
    selected: Boolean,
    position: DomChatModelGroupPosition,
): ModelCornerRadii {
    if (selected) return ModelCornerRadii(50.dp, 50.dp, 50.dp, 50.dp)
    return when (position) {
        DomChatModelGroupPosition.First -> ModelCornerRadii(24.dp, 24.dp, 10.dp, 10.dp)
        DomChatModelGroupPosition.Middle -> ModelCornerRadii(10.dp, 10.dp, 10.dp, 10.dp)
        DomChatModelGroupPosition.Last -> ModelCornerRadii(10.dp, 10.dp, 24.dp, 24.dp)
        DomChatModelGroupPosition.Single -> ModelCornerRadii(24.dp, 24.dp, 24.dp, 24.dp)
    }
}
