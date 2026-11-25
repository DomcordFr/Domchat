package me.lucid.domchat.ui.components.settings

import androidx.compose.animation.animateContentSize
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
    val cardShape = RoundedCornerShape(18.dp)
    Card(
        modifier = modifier.border(
            0.5.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.06f),
                    Color.Transparent
                )
            ),
            cardShape
        ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF000000),
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f))
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f)),
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
    containerColor: Color = Color.White.copy(alpha = 0.08f),
    drawBackground: Boolean = true,
    drawBorder: Boolean = true,
    uniformCorners: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit,
    metadata: @Composable () -> Unit = {},
    titleTrailing: (@Composable RowScope.() -> Unit)? = null,
    tail: @Composable RowScope.() -> Unit = {},
    dragHandle: @Composable (RowScope.() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val target = if (uniformCorners) {
        ModelCornerRadii(18.dp, 18.dp, 18.dp, 18.dp)
    } else {
        groupedCornerRadii(selected, position)
    }
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
    val contentColor = Color(0xFFF2F2F7)
    val rowShape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)
    val rowBackground = containerColor
    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (selected) 0.80f else 0.18f),
            Color.White.copy(alpha = if (selected) 0.35f else 0.05f)
        )
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.5.dp, vertical = 0.5.dp)
            .then(if (drawBackground) Modifier.background(rowBackground, shape = rowShape) else Modifier)
            .then(if (drawBorder) Modifier.border(BorderStroke(if (selected) 1.dp else 0.5.dp, borderBrush), shape = rowShape) else Modifier)
            .clip(rowShape)
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
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = contentColor,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    titleTrailing?.invoke(this)
                }
                metadata()
            }
            tail()
        }
        dragHandle?.invoke(this)
    }
}

data class ModelCornerRadii(
    val topStart: androidx.compose.ui.unit.Dp,
    val topEnd: androidx.compose.ui.unit.Dp,
    val bottomStart: androidx.compose.ui.unit.Dp,
    val bottomEnd: androidx.compose.ui.unit.Dp,
)

fun groupedCornerRadii(
    selected: Boolean,
    position: DomChatModelGroupPosition,
): ModelCornerRadii {
    return when (position) {
        DomChatModelGroupPosition.First -> ModelCornerRadii(18.dp, 18.dp, 6.dp, 6.dp)
        DomChatModelGroupPosition.Middle -> ModelCornerRadii(6.dp, 6.dp, 6.dp, 6.dp)
        DomChatModelGroupPosition.Last -> ModelCornerRadii(6.dp, 6.dp, 18.dp, 18.dp)
        DomChatModelGroupPosition.Single -> ModelCornerRadii(18.dp, 18.dp, 18.dp, 18.dp)
    }
}
